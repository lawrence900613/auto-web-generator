<template>
  <div id="appChatPage">
    <!-- App-level sub-header -->
    <div class="app-header">
      <div class="app-header-left">
        <span class="app-title">{{ app?.appName ?? 'Loading...' }}</span>
        <a-tag v-if="app?.codeGenType" color="blue" class="gen-tag">{{ codeGenLabel }}</a-tag>
      </div>
      <div class="app-header-right">
        <a-button type="text" @click="detailVisible = true">
          <template #icon><InfoCircleOutlined /></template>
          App Details
        </a-button>
        <a-button
          type="default"
          :disabled="!canDeploy && !app?.deployKey"
          @click="doDownload"
        >
          <template #icon><DownloadOutlined /></template>
          Download Code
        </a-button>
        <a-button
          type="primary"
          ghost
          :disabled="!canDeploy && !app?.deployKey"
          :loading="deploying"
          @click="doDeploy"
        >
          <template #icon><CloudUploadOutlined /></template>
          Deploy
        </a-button>
      </div>
    </div>

    <!-- Two-column content -->
    <div class="content-area">
      <!-- Left: chat panel -->
      <div class="chat-panel">
        <div ref="msgListRef" class="msg-list">
          <!-- Load more history -->
          <div v-if="hasMoreHistory" class="load-more-wrap">
            <a-button size="small" :loading="loadingHistory" @click="loadMoreHistory">
              Load earlier messages
            </a-button>
          </div>

          <template v-for="(msg, idx) in messages" :key="idx">

            <!-- User message -->
            <div v-if="msg.role === 'user'" class="user-msg-wrap">
              <div class="user-bubble">
                <span class="user-text">{{ msg.content }}</span>
                <CodeOutlined class="user-icon" />
              </div>
            </div>

            <!-- AI message -->
            <div v-else class="ai-msg-wrap">
              <div class="ai-avatar"><RobotFilled /></div>
              <div class="ai-body">
                <div class="markdown-body" v-html="renderMarkdown(msg.content)" />
              </div>
            </div>

          </template>

          <!-- Streaming bubble -->
          <div v-if="streaming" class="ai-msg-wrap">
            <div class="ai-avatar"><RobotFilled /></div>
            <div class="ai-body">
              <div v-if="!streamBuffer" class="thinking">
                <span class="dot" /><span class="dot" /><span class="dot" />
                AI is thinking...
              </div>
              <div class="markdown-body stream-body" v-html="renderMarkdown(streamBuffer)" />
              <div class="stream-working">
                <span class="dot" /><span class="dot" /><span class="dot" />
                Writing files...
              </div>
            </div>
          </div>
        </div>

        <!-- Input -->
        <div class="input-wrap">
          <!-- Selected element info -->
          <a-alert
            v-if="selectedElementInfo"
            class="element-alert"
            type="info"
            closable
            @close="clearSelectedElement"
          >
            <template #message>
              <span class="element-alert-tag">{{ selectedElementInfo.tagName.toLowerCase() }}</span>
              <span v-if="selectedElementInfo.id" class="element-alert-id">#{{ selectedElementInfo.id }}</span>
              <span v-if="selectedElementInfo.className" class="element-alert-class">.{{ selectedElementInfo.className.split(' ').filter(Boolean).join('.') }}</span>
              <span v-if="selectedElementInfo.textContent" class="element-alert-text"> — {{ selectedElementInfo.textContent.substring(0, 60) }}{{ selectedElementInfo.textContent.length > 60 ? '…' : '' }}</span>
            </template>
          </a-alert>

          <a-tooltip :title="!isOwner ? 'Cannot chat in someone else\'s app' : ''">
            <div class="input-card" :class="{ focused: inputFocused }">
              <a-textarea
                v-model:value="inputText"
                :placeholder="selectedElementInfo ? `Editing ${selectedElementInfo.tagName.toLowerCase()} element — describe the change you want` : isOwner ? 'Describe the website you want — more detail means better results' : 'Only the app owner can send messages'"
                :auto-size="{ minRows: 3, maxRows: 1000 }"
                :disabled="streaming || !isOwner"
                @focus="inputFocused = true"
                @blur="inputFocused = false"
                @keydown.enter.exact.prevent="doSend"
              />
              <a-button
                type="primary"
                shape="circle"
                size="small"
                class="send-btn"
                :loading="streaming"
                :disabled="!isOwner || !inputText.trim()"
                @click="doSend"
              >
                <template #icon><ArrowUpOutlined /></template>
              </a-button>
            </div>
          </a-tooltip>
        </div>
      </div>

      <!-- Right: preview panel -->
      <div class="preview-panel">
        <div class="preview-header">
          <span class="preview-label">Generated Website</span>
          <div class="preview-actions">
            <a-button
              v-if="isOwner && previewUrl && !streaming"
              :type="isEditMode ? 'primary' : 'default'"
              :ghost="isEditMode"
              size="small"
              @click="toggleEditMode"
            >
              <template #icon><EditOutlined /></template>
              {{ isEditMode ? 'Exit Edit' : 'Edit Mode' }}
            </a-button>
            <a-button type="text" :disabled="!previewUrl" @click="refreshPreview">
              <template #icon><ReloadOutlined /></template>
              Refresh
            </a-button>
            <a-button type="text" :disabled="!previewUrl" @click="openInNewWindow">
              <template #icon><ExportOutlined /></template>
              New Window
            </a-button>
          </div>
        </div>
        <iframe
          v-if="previewUrl && !streaming"
          ref="iframeRef"
          :src="previewUrl"
          class="preview-iframe"
          sandbox="allow-scripts allow-same-origin"
          @load="onIframeLoad"
        />
        <div v-else-if="streaming" class="preview-generating">
          <div class="generating-anim">
            <div class="gen-ring" />
            <RobotFilled class="gen-robot" />
          </div>
          <p class="gen-title">Generating website...</p>
          <p class="gen-sub">AI is writing your code, please wait</p>
        </div>
        <div v-else class="preview-empty">
          <CodeOutlined class="empty-icon" />
          <p class="empty-title">Generated website will appear here</p>
          <p class="empty-sub">Send a message to start generating</p>
        </div>
      </div>
    </div>

    <DeploySuccessModal v-model:open="deploySuccessVisible" :deployed-url="deployedUrl" />
    <AppDetailModal
      v-model:open="detailVisible"
      :app="app"
      :is-owner="isOwner"
      :is-admin="isAdmin"
      @deleted="router.push('/')"
    />
  </div>
</template>

<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, onMounted, ref } from 'vue'
import { onBeforeRouteLeave, useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import {
  RobotFilled,
  CodeOutlined,
  InfoCircleOutlined,
  CloudUploadOutlined,
  DownloadOutlined,
  ArrowUpOutlined,
  ReloadOutlined,
  ExportOutlined,
  EditOutlined,
} from '@ant-design/icons-vue'
import { VisualEditor, type ElementInfo } from '@/utils/visualEditor'
import { marked } from 'marked'
import { markedHighlight } from 'marked-highlight'
import hljs from 'highlight.js'
import 'highlight.js/styles/github-dark.css'
import { getAppVoById, deployApp } from '@/api/appController'
import { listAppChatHistory } from '@/api/chatHistoryController'
import { useLoginUserStore } from '@/stores/loginUser'
import AppDetailModal from '@/components/AppDetailModal.vue'
import DeploySuccessModal from '@/components/DeploySuccessModal.vue'

// Configure marked with highlight.js
marked.use(
  markedHighlight({
    langPrefix: 'hljs language-',
    highlight(code, lang) {
      const language = hljs.getLanguage(lang) ? lang : 'plaintext'
      return hljs.highlight(code, { language }).value
    },
  }),
)
marked.use({ breaks: true, gfm: true })

const renderMarkdown = (content: string): string => {
  return marked.parse(content) as string
}

const route = useRoute()
const router = useRouter()
const loginUserStore = useLoginUserStore()

const rawApiBase = ((import.meta.env.VITE_API_BASE || '/api') as string).trim()
const apiBase = /^https?:\/\//.test(rawApiBase)
  ? rawApiBase
  : rawApiBase.startsWith('/')
    ? rawApiBase
    : `/${rawApiBase}`
const deployDomain = ((import.meta.env.VITE_DEPLOY_DOMAIN || window.location.origin) as string).trim()
const appId = route.params.id as string

const app = ref<API.AppVO>()
const messages = ref<{ role: 'user' | 'assistant'; content: string }[]>([])
const inputText = ref('')
const inputFocused = ref(false)
const streaming = ref(false)
const streamBuffer = ref('')
const msgListRef = ref<HTMLElement>()
const iframeRef = ref<HTMLIFrameElement>()
const previewUrl = ref('')
const canDeploy = ref(false)
const deploying = ref(false)
const detailVisible = ref(false)
const deploySuccessVisible = ref(false)
const deployedUrl = ref('')
const hasMoreHistory = ref(false)
const loadingHistory = ref(false)
const lastCreateTime = ref<number | null>(null)

// Visual editor
const isEditMode = ref(false)
const selectedElementInfo = ref<ElementInfo | null>(null)
const visualEditor = new VisualEditor({
  onElementSelected: (info: ElementInfo) => { selectedElementInfo.value = info },
})

const isOwner = computed(
  () =>
    loginUserStore.loginUser?.id != null &&
    String(app.value?.userId) === String(loginUserStore.loginUser.id),
)

const isAdmin = computed(() => loginUserStore.loginUser?.userRole === 'admin')

const codeGenLabel = computed(() => {
  const t = app.value?.codeGenType
  if (t === 'vue_project') return 'Vue Project'
  // Legacy labels are temporarily disabled:
  // if (t === 'html') return 'HTML Mode'
  // if (t === 'multi_file') return 'Multi-file Mode'
  return t ?? ''
})

const getStaticPreviewUrl = () => {
  return `${apiBase}/app-output/vue_project_${appId}/dist/index.html`
}


// ---- Chat history ----
const fetchChatHistory = async (prepend = false) => {
  if (loadingHistory.value) return
  loadingHistory.value = true
  try {
    const params: { appId: string; pageSize: number; lastCreateTime?: number } = {
      appId,
      pageSize: 10,
    }
    if (lastCreateTime.value !== null) params.lastCreateTime = lastCreateTime.value

    const res = await listAppChatHistory(params)
    if (res.data.code === 0 && res.data.data) {
      const records = res.data.data.records ?? []
      hasMoreHistory.value = records.length >= 10

      // Records come DESC (newest first) → reverse to chronological order for display
      const historyMessages = [...records].reverse().map((r) => ({
        role: (r.messageType === 'user' ? 'user' : 'assistant') as 'user' | 'assistant',
        content: r.message ?? '',
      }))

      if (prepend) {
        messages.value = [...historyMessages, ...messages.value]
      } else {
        messages.value = historyMessages
      }

      // Update cursor to the oldest message in this batch (last item in DESC list)
      if (records.length > 0) {
        lastCreateTime.value = Number(records[records.length - 1].createTime) || null
      }
    }
  } finally {
    loadingHistory.value = false
  }
}

const loadMoreHistory = async () => {
  const scrollEl = msgListRef.value
  const prevScrollHeight = scrollEl?.scrollHeight ?? 0
  await fetchChatHistory(true)
  // Restore scroll position so the view doesn't jump to top
  await nextTick()
  if (scrollEl) scrollEl.scrollTop = scrollEl.scrollHeight - prevScrollHeight
}

// ---- App fetch ----
const fetchApp = async () => {
  const res = await getAppVoById({ id: appId })
  if (res.data.code === 0 && res.data.data) {
    app.value = res.data.data
  } else {
    message.error(res.data.message ?? 'App not found')
    router.push('/')
  }
}

const scrollToBottom = async () => {
  await nextTick()
  if (msgListRef.value) msgListRef.value.scrollTop = msgListRef.value.scrollHeight
}

// ---- Send / stream ----
const sendMessage = async (text: string) => {
  messages.value.push({ role: 'user', content: text })
  streaming.value = true
  streamBuffer.value = ''
  canDeploy.value = false
  await scrollToBottom()

  const url = `${apiBase}/app/chat/gen/code?appId=${appId}&message=${encodeURIComponent(text)}`
  const es = new EventSource(url, { withCredentials: true })

  es.onmessage = async (e) => {
    try {
      const msg = JSON.parse(e.data) as {
        type: string
        data?: string
        display?: string
      }
      if (msg.type === 'ai_response') {
        streamBuffer.value += msg.data ?? ''
      } else if (msg.display) {
        streamBuffer.value += msg.display
      }
    } catch {
      streamBuffer.value += e.data
    }
    await scrollToBottom()
  }

  es.addEventListener('done', async () => {
    es.close()
    streaming.value = false
    messages.value.push({ role: 'assistant', content: streamBuffer.value })
    streamBuffer.value = ''
    canDeploy.value = true
    previewUrl.value = getStaticPreviewUrl()
    await nextTick()
    refreshPreview()
    scrollToBottom()
  })

  es.addEventListener('error', (e: MessageEvent) => {
    es.close()
    streaming.value = false
    streamBuffer.value = ''
    try {
      const errorMsg = JSON.parse(e.data).msg as string
      message.error(errorMsg)
    } catch {
      message.error('Generation failed. Please try again.')
    }
  })

  es.onerror = () => {
    if (!streaming.value) return
    es.close()
    streaming.value = false
    streamBuffer.value = ''
    message.error('Connection lost. Please try again.')
  }
}

const doSend = async () => {
  let text = inputText.value.trim()
  if (!text || !isOwner.value) return
  inputText.value = ''

  // Append selected element context to the prompt
  if (selectedElementInfo.value) {
    const el = selectedElementInfo.value
    let ctx = `\n\nSelected element:`
    if (el.pagePath) ctx += `\n- Page path: ${el.pagePath}`
    ctx += `\n- Tag: ${el.tagName.toLowerCase()}\n- Selector: ${el.selector}`
    if (el.textContent) ctx += `\n- Current content: ${el.textContent.substring(0, 100)}`
    text += ctx
    clearSelectedElement()
    if (isEditMode.value) toggleEditMode()
  }

  await nextTick()
  await sendMessage(text)
}

const toggleEditMode = () => {
  if (!iframeRef.value) { return }
  visualEditor.init(iframeRef.value)
  isEditMode.value = visualEditor.toggleEditMode()
}

const clearSelectedElement = () => {
  selectedElementInfo.value = null
  visualEditor.clearSelection()
}

const refreshPreview = () => {
  if (iframeRef.value && previewUrl.value) {
    iframeRef.value.src = previewUrl.value + '?t=' + Date.now()
  }
}

const openInNewWindow = () => {
  if (previewUrl.value) window.open(previewUrl.value, '_blank')
}

const doDownload = () => {
  const url = `${apiBase}/app/download/${appId}`
  const a = document.createElement('a')
  a.href = url
  a.download = `${appId}.zip`
  document.body.appendChild(a)
  a.click()
  document.body.removeChild(a)
}

const doDeploy = async () => {
  deploying.value = true
  const res = await deployApp({ appId })
  deploying.value = false
  if (res.data.code === 0 && res.data.data) {
    const key = res.data.data
    previewUrl.value = `${apiBase}/deploy/${key}/index.html`
    deployedUrl.value = `${deployDomain}/${key}`
    deploySuccessVisible.value = true
    await fetchApp()
  } else {
    message.error(res.data.message ?? 'Deploy failed')
  }
}


const onIframeLoad = () => {
  if (iframeRef.value) {
    visualEditor.init(iframeRef.value)
    visualEditor.onIframeLoad()
  }
}

const beforeUnloadHandler = (event: BeforeUnloadEvent) => {
  if (!streaming.value) return
  // Prompt the browser's native "leave site?" dialog.
  event.preventDefault()
  event.returnValue = ''
}

onBeforeRouteLeave((_to, _from, next) => {
  if (!streaming.value) {
    next()
    return
  }
  const ok = window.confirm(
    'Generation is still in progress. Leaving now may interrupt streaming and lose in-progress output. Leave anyway?',
  )
  next(ok)
})

onMounted(async () => {
  window.addEventListener('beforeunload', beforeUnloadHandler)
  window.addEventListener('message', (e) => visualEditor.handleIframeMessage(e))
  await fetchApp()
  await fetchChatHistory()

  // Show static preview if there are at least 2 chat records
  if (messages.value.length >= 2) {
    previewUrl.value = getStaticPreviewUrl()
    canDeploy.value = true
  }

  // Auto-send initPrompt only if owner and there is no chat history yet
  if (isOwner.value && messages.value.length === 0 && app.value?.initPrompt) {
    await sendMessage(app.value.initPrompt)
  } else {
    await scrollToBottom()
  }
})

onBeforeUnmount(() => {
  window.removeEventListener('beforeunload', beforeUnloadHandler)
})
</script>

<style scoped>
/* ===== Shell ===== */
#appChatPage {
  display: flex;
  flex-direction: column;
  height: calc(100vh - 72px);
  overflow: hidden;
  background: #f7f8fc;
}

/* ===== App sub-header ===== */
.app-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 24px;
  background: #fff;
  border-bottom: 1px solid #eef0f4;
  flex-shrink: 0;
  min-height: 60px;
}

.app-header-left {
  display: flex;
  align-items: center;
  gap: 14px;
  flex: 1;
  min-width: 0;
}

.app-title {
  font-weight: 700;
  font-size: 22px;
  color: #1a1a2e;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.gen-tag {
  font-size: 14px;
  border-radius: 4px;
  flex-shrink: 0;
  padding: 2px 10px;
}

.app-header-right {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-shrink: 0;
}

/* ===== Content split ===== */
.content-area {
  display: flex;
  flex: 1;
  overflow: hidden;
  gap: 10px;
  padding: 10px;
  background: #eef0f6;
}

/* ===== Left: chat panel ===== */
.chat-panel {
  flex: 2;
  min-width: 0;
  display: flex;
  flex-direction: column;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  overflow: hidden;
}

.msg-list {
  flex: 1;
  overflow-y: auto;
  padding: 14px 14px 10px;
  display: flex;
  flex-direction: column;
  gap: 14px;
}

/* ---- Load more history ---- */
.load-more-wrap {
  display: flex;
  justify-content: center;
  padding: 4px 0 8px;
}

/* ---- User message ---- */
.user-msg-wrap {
  display: flex;
  justify-content: flex-end;
}

.user-bubble {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  background: #1677ff;
  color: #fff;
  padding: 14px 16px;
  border-radius: 12px 12px 2px 12px;
  max-width: 88%;
  font-size: 16px;
  line-height: 1.6;
}

.user-text {
  flex: 1;
  word-break: break-word;
  white-space: pre-wrap;
}

.user-icon {
  flex-shrink: 0;
  font-size: 16px;
  opacity: 0.8;
  margin-top: 2px;
}

/* ---- AI message ---- */
.ai-msg-wrap {
  display: flex;
  align-items: flex-start;
  gap: 10px;
}

.ai-avatar {
  width: 34px;
  height: 34px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 16px;
  flex-shrink: 0;
  margin-top: 2px;
}

.ai-body {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

/* ---- Markdown body ---- */
.ai-body :deep(.markdown-body) {
  font-size: 15px;
  color: #333;
  line-height: 1.75;
  word-break: break-word;
  background: #f8f9fe;
  border: 1px solid #eef0f8;
  border-radius: 12px;
  padding: 14px 16px;
}

.stream-body {
  position: relative;
}

.ai-body :deep(.markdown-body p) {
  margin: 0 0 10px;
}
.ai-body :deep(.markdown-body p:last-child) {
  margin-bottom: 0;
}

.ai-body :deep(.markdown-body h1),
.ai-body :deep(.markdown-body h2),
.ai-body :deep(.markdown-body h3),
.ai-body :deep(.markdown-body h4) {
  margin: 14px 0 8px;
  font-weight: 700;
  color: #1a1a2e;
  line-height: 1.4;
}
.ai-body :deep(.markdown-body h1) { font-size: 20px; }
.ai-body :deep(.markdown-body h2) { font-size: 18px; }
.ai-body :deep(.markdown-body h3) { font-size: 16px; }

.ai-body :deep(.markdown-body ul),
.ai-body :deep(.markdown-body ol) {
  padding-left: 22px;
  margin: 6px 0 10px;
}
.ai-body :deep(.markdown-body li) {
  margin-bottom: 4px;
}

.ai-body :deep(.markdown-body strong) {
  font-weight: 600;
  color: #1a1a2e;
}

.ai-body :deep(.markdown-body em) {
  font-style: italic;
  color: #555;
}

.ai-body :deep(.markdown-body blockquote) {
  border-left: 3px solid #667eea;
  padding: 4px 0 4px 12px;
  margin: 8px 0;
  color: #666;
  font-style: italic;
}

.ai-body :deep(.markdown-body hr) {
  border: none;
  border-top: 1px solid #e4e6ee;
  margin: 12px 0;
}

/* Inline code */
.ai-body :deep(.markdown-body code:not(pre code)) {
  background: #eef0f8;
  border: 1px solid #dde0ee;
  border-radius: 4px;
  padding: 1px 5px;
  font-family: 'SFMono-Regular', Consolas, monospace;
  font-size: 13px;
  color: #c7254e;
}

/* Code blocks */
.ai-body :deep(.markdown-body pre) {
  margin: 10px 0;
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid #2d2d2d;
  max-height: 320px;
  overflow-y: auto;
}

.ai-body :deep(.markdown-body pre code) {
  display: block;
  padding: 12px 14px;
  font-family: 'SFMono-Regular', Consolas, 'Courier New', monospace;
  font-size: 13.5px;
  line-height: 1.6;
  white-space: pre;
  overflow-x: auto;
}

/* Table */
.ai-body :deep(.markdown-body table) {
  border-collapse: collapse;
  width: 100%;
  margin: 10px 0;
  font-size: 14px;
}
.ai-body :deep(.markdown-body th),
.ai-body :deep(.markdown-body td) {
  border: 1px solid #dde0ee;
  padding: 6px 10px;
  text-align: left;
}
.ai-body :deep(.markdown-body th) {
  background: #eef0f8;
  font-weight: 600;
}

/* ---- Thinking indicator ---- */
.thinking {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 15px;
  color: #888;
}

.dot {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: #aaa;
  animation: bounce 1.2s ease-in-out infinite;
}

.dot:nth-child(2) { animation-delay: 0.2s; }
.dot:nth-child(3) { animation-delay: 0.4s; }

.stream-working {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-top: 10px;
  font-size: 13px;
  color: #aaa;
}

@keyframes bounce {
  0%, 80%, 100% { transform: translateY(0); opacity: 0.4; }
  40%           { transform: translateY(-6px); opacity: 1; }
}

/* ---- Blinking cursor on streaming body ---- */
.stream-body::after {
  content: '▋';
  display: inline-block;
  color: #667eea;
  animation: blink 0.8s step-end infinite;
  font-size: 14px;
  vertical-align: middle;
  margin-left: 2px;
}

@keyframes blink {
  50% { opacity: 0; }
}

/* ===== Input area ===== */
.input-wrap {
  padding: 8px 12px 12px;
  border-top: 1px solid #eef0f4;
  flex-shrink: 0;
}

.input-card {
  position: relative;
  border: 1.5px solid #e4e6ee;
  border-radius: 12px;
  background: #fff;
  transition: border-color 0.2s;
}

.input-card.focused {
  border-color: #1677ff;
  box-shadow: 0 0 0 2px rgba(22, 119, 255, 0.08);
}

.input-card :deep(.ant-input) {
  border: none !important;
  box-shadow: none !important;
  border-radius: 10px !important;
  padding: 14px 16px 44px;
  font-size: 16px;
  resize: none;
  background: transparent;
  color: #333;
}

.input-card :deep(.ant-input::placeholder) {
  color: #b0b7cc;
}

.send-btn {
  position: absolute;
  bottom: 10px;
  right: 10px;
  width: 30px !important;
  height: 30px !important;
  min-width: 30px !important;
}

.element-alert {
  margin-bottom: 8px;
  border-radius: 8px;
  font-size: 13px;
}

.element-alert-tag {
  font-family: 'SFMono-Regular', Consolas, monospace;
  font-weight: 600;
  color: #1677ff;
}

.element-alert-id {
  color: #389e0d;
  margin-left: 2px;
}

.element-alert-class {
  color: #d46b08;
  margin-left: 2px;
}

.element-alert-text {
  color: #555;
  margin-left: 4px;
}

/* ===== Right: preview panel ===== */
.preview-panel {
  flex: 3;
  min-width: 0;
  display: flex;
  flex-direction: column;
  background: #f7f8fc;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  overflow: hidden;
}

.preview-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 16px;
  border-bottom: 1px solid #eef0f4;
  background: #fff;
  flex-shrink: 0;
}

.preview-label {
  font-size: 16px;
  font-weight: 600;
  color: #555;
}

.preview-actions {
  display: flex;
  align-items: center;
  gap: 2px;
}

.preview-iframe {
  flex: 1;
  border: none;
  width: 100%;
  background: #fff;
}

.preview-empty {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 10px;
}

/* ---- Generating state ---- */
.preview-generating {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 16px;
}

.generating-anim {
  position: relative;
  width: 80px;
  height: 80px;
  display: flex;
  align-items: center;
  justify-content: center;
  animation: float 2.4s ease-in-out infinite;
}

.gen-ring {
  position: absolute;
  inset: 0;
  border-radius: 50%;
  border: 3px solid transparent;
  border-top-color: #1677ff;
  border-right-color: #1677ff;
  animation: spin 1.2s linear infinite;
}

.gen-robot {
  font-size: 38px;
  color: #667eea;
}

.gen-title {
  font-size: 15px;
  font-weight: 600;
  color: #444;
  margin: 0;
}

.gen-sub {
  font-size: 13px;
  color: #aaa;
  margin: 0;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

@keyframes float {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-8px); }
}

.empty-icon {
  font-size: 52px;
  color: #d0d5e8;
}

.empty-title {
  font-size: 15px;
  font-weight: 500;
  color: #8c93ad;
  margin: 0;
}

.empty-sub {
  font-size: 13px;
  color: #b0b7cc;
  margin: 0;
}

</style>
