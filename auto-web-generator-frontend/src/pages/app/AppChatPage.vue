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
                <template v-for="(seg, si) in parseSegments(msg.content)" :key="si">
                  <div v-if="seg.type === 'code'" class="code-block">
                    <div class="code-header">
                      <span class="code-lang">{{ seg.language || 'code' }}</span>
                    </div>
                    <pre class="code-content"><code>{{ seg.content }}</code></pre>
                  </div>
                  <div v-else class="ai-text">{{ seg.content }}</div>
                </template>
              </div>
            </div>

          </template>

          <!-- Streaming bubble -->
          <div v-if="streaming" class="ai-msg-wrap">
            <div class="ai-avatar"><RobotFilled /></div>
            <div class="ai-body">
              <div class="ai-text stream-text">{{ streamBuffer }}<span class="cursor">|</span></div>
            </div>
          </div>
        </div>

        <!-- Input -->
        <div class="input-wrap">
          <a-tooltip :title="!isOwner ? 'Cannot chat in someone else\'s app' : ''">
            <div class="input-card" :class="{ focused: inputFocused }">
              <a-textarea
                v-model:value="inputText"
                :placeholder="isOwner ? 'Describe the website you want — more detail means better results' : 'Only the app owner can send messages'"
                :auto-size="{ minRows: 3, maxRows: 6 }"
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
import { computed, nextTick, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import {
  RobotFilled,
  CodeOutlined,
  InfoCircleOutlined,
  CloudUploadOutlined,
  ArrowUpOutlined,
  ReloadOutlined,
  ExportOutlined,
} from '@ant-design/icons-vue'
import { getAppVoById, deployApp } from '@/api/appController'
import { useLoginUserStore } from '@/stores/loginUser'
import AppDetailModal from '@/components/AppDetailModal.vue'
import DeploySuccessModal from '@/components/DeploySuccessModal.vue'

const route = useRoute()
const router = useRouter()
const loginUserStore = useLoginUserStore()

const apiBase = (import.meta.env.VITE_API_BASE ?? 'http://localhost:8123/api') as string
const deployDomain = (import.meta.env.VITE_DEPLOY_DOMAIN ?? 'http://localhost') as string
const appId = route.params.id as string
const viewOnly = route.query.view === '1'

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

const isOwner = computed(
  () =>
    loginUserStore.loginUser?.id != null &&
    String(app.value?.userId) === String(loginUserStore.loginUser.id),
)

const isAdmin = computed(() => loginUserStore.loginUser?.userRole === 'admin')

const codeGenLabel = computed(() => {
  const t = app.value?.codeGenType
  if (t === 'html') return 'HTML Mode'
  if (t === 'multi_file') return 'Multi-file Mode'
  return t ?? ''
})

// ---- Message segment parser ----
interface Segment { type: 'text' | 'code'; content: string; language: string }

const parseSegments = (content: string): Segment[] => {
  const result: Segment[] = []
  const re = /```(\w*)\n?([\s\S]*?)```/g
  let pos = 0
  let m: RegExpExecArray | null
  while ((m = re.exec(content)) !== null) {
    if (m.index > pos) {
      result.push({ type: 'text', content: content.slice(pos, m.index).trim(), language: '' })
    }
    result.push({ type: 'code', content: m[2].trim(), language: m[1] || 'code' })
    pos = m.index + m[0].length
  }
  const tail = content.slice(pos).trim()
  if (tail) result.push({ type: 'text', content: tail, language: '' })
  return result.filter((s) => s.content)
}

// ---- App fetch ----
const fetchApp = async () => {
  const res = await getAppVoById({ id: appId })
  if (res.data.code === 0 && res.data.data) {
    app.value = res.data.data
    if (res.data.data.deployKey) {
      previewUrl.value = `${apiBase}/deploy/${res.data.data.deployKey}/index.html`
    }
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
    streamBuffer.value += JSON.parse(e.data).d as string
    await scrollToBottom()
  }

  es.addEventListener('done', () => {
    es.close()
    streaming.value = false
    messages.value.push({ role: 'assistant', content: streamBuffer.value })
    streamBuffer.value = ''
    canDeploy.value = true
    previewUrl.value = `${apiBase}/app-output/${appId}/index.html`
    refreshPreview()
    scrollToBottom()
  })

  es.onerror = () => {
    es.close()
    streaming.value = false
    streamBuffer.value = ''
    message.error('Generation failed. Please try again.')
  }
}

const doSend = async () => {
  const text = inputText.value.trim()
  if (!text || !isOwner.value) return
  inputText.value = ''
  await sendMessage(text)
}

const refreshPreview = () => {
  if (iframeRef.value && previewUrl.value) {
    iframeRef.value.src = previewUrl.value + '?t=' + Date.now()
  }
}

const openInNewWindow = () => {
  if (previewUrl.value) window.open(previewUrl.value, '_blank')
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


onMounted(async () => {
  await fetchApp()
  if (!viewOnly && messages.value.length === 0 && app.value?.initPrompt && isOwner.value) {
    await sendMessage(app.value.initPrompt)
  }
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

.ai-text {
  font-size: 16px;
  color: #333;
  line-height: 1.75;
  white-space: pre-wrap;
  word-break: break-word;
}

.stream-text {
  color: #555;
}

/* ---- Code block ---- */
.code-block {
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid #2d2d2d;
}

.code-header {
  background: #2d2d2d;
  padding: 6px 12px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.code-lang {
  font-size: 12px;
  color: #858585;
  font-family: 'SFMono-Regular', Consolas, monospace;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.code-content {
  margin: 0;
  background: #1e1e1e;
  padding: 12px 14px;
  overflow-x: auto;
  max-height: 280px;
  overflow-y: auto;
}

.code-content code {
  font-family: 'SFMono-Regular', Consolas, 'Courier New', monospace;
  font-size: 14px;
  color: #d4d4d4;
  line-height: 1.6;
  white-space: pre;
}

/* ---- Blinking cursor ---- */
.cursor {
  display: inline-block;
  animation: blink 0.8s step-end infinite;
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
