<template>
  <div id="appChatPage">
    <!-- Left: chat panel -->
    <div class="chat-panel">
      <div class="chat-header">
        <span class="app-name">{{ app?.appName ?? 'Loading...' }}</span>
        <a-button size="small" @click="router.push(`/app/edit/${appId}`)">Edit Info</a-button>
      </div>

      <div ref="msgListRef" class="msg-list">
        <div v-for="(msg, idx) in messages" :key="idx" :class="['msg', msg.role]">
          <div class="msg-bubble">{{ msg.content }}</div>
        </div>
        <div v-if="streaming" class="msg assistant">
          <div class="msg-bubble streaming">{{ streamBuffer }}<span class="cursor">|</span></div>
        </div>
      </div>

      <div class="chat-input">
        <a-textarea
          v-model:value="inputText"
          placeholder="Describe changes or new features..."
          :auto-size="{ minRows: 2, maxRows: 4 }"
          :disabled="streaming || !isOwner"
          @press-enter.prevent="doSend"
        />
        <div class="chat-actions">
          <a-button
            type="primary"
            :loading="streaming"
            :disabled="!isOwner"
            @click="doSend"
          >Generate</a-button>
          <a-button
            :disabled="!app?.deployedTime && !canDeploy"
            :loading="deploying"
            @click="doDeploy"
          >Deploy</a-button>
          <a-button v-if="app?.deployKey" @click="openDeployed">View Site</a-button>
        </div>
        <div v-if="!isOwner" class="no-perm">Only the app owner can send messages.</div>
      </div>
    </div>

    <!-- Right: preview iframe -->
    <div class="preview-panel">
      <div class="preview-header">
        <span>Live Preview</span>
        <a-button size="small" :disabled="!previewUrl" @click="refreshPreview">Refresh</a-button>
      </div>
      <iframe
        v-if="previewUrl"
        ref="iframeRef"
        :src="previewUrl"
        class="preview-iframe"
        sandbox="allow-scripts allow-same-origin"
      />
      <div v-else class="preview-empty">
        <CodeOutlined style="font-size: 48px; color: #d9d9d9" />
        <p>Generate code to see a preview</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, nextTick, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { CodeOutlined } from '@ant-design/icons-vue'
import { getAppVoById, deployApp } from '@/api/appController'
import { useLoginUserStore } from '@/stores/loginUser'

const route = useRoute()
const router = useRouter()
const loginUserStore = useLoginUserStore()

const appId = route.params.id as string
const app = ref<API.AppVO>()
const loading = ref(false)

const messages = ref<{ role: 'user' | 'assistant'; content: string }[]>([])
const inputText = ref('')
const streaming = ref(false)
const streamBuffer = ref('')
const msgListRef = ref<HTMLElement>()
const iframeRef = ref<HTMLIFrameElement>()
const previewUrl = ref('')
const canDeploy = ref(false)
const deploying = ref(false)

const isOwner = computed(() =>
  loginUserStore.loginUser?.id != null &&
  app.value?.userId === loginUserStore.loginUser.id,
)

const fetchApp = async () => {
  loading.value = true
  const res = await getAppVoById({ id: appId })
  loading.value = false
  if (res.data.code === 0 && res.data.data) {
    app.value = res.data.data
    if (res.data.data.deployKey) {
      previewUrl.value = `/deploy/${res.data.data.deployKey}/index.html`
    }
  } else {
    message.error(res.data.message ?? 'App not found')
    router.push('/')
  }
}

const scrollToBottom = async () => {
  await nextTick()
  if (msgListRef.value) {
    msgListRef.value.scrollTop = msgListRef.value.scrollHeight
  }
}

const doSend = async () => {
  const text = inputText.value.trim()
  if (!text) {
    message.warning('Please enter a message')
    return
  }
  if (!isOwner.value) return

  messages.value.push({ role: 'user', content: text })
  inputText.value = ''
  streaming.value = true
  streamBuffer.value = ''
  canDeploy.value = false
  await scrollToBottom()

  const apiBase = (import.meta.env.VITE_API_BASE ?? '') as string
  const url = `${apiBase}/app/chat/gen/code?appId=${appId}&message=${encodeURIComponent(text)}`

  const eventSource = new EventSource(url, { withCredentials: true })

  eventSource.onmessage = async (e) => {
    streamBuffer.value += e.data
    await scrollToBottom()
  }

  eventSource.onerror = () => {
    eventSource.close()
    streaming.value = false
    if (streamBuffer.value) {
      messages.value.push({ role: 'assistant', content: streamBuffer.value })
      streamBuffer.value = ''
      canDeploy.value = true
      // Reload preview after generation
      previewUrl.value = `/app-output/${appId}/index.html`
      refreshPreview()
    }
    scrollToBottom()
  }
}

const refreshPreview = () => {
  if (iframeRef.value && previewUrl.value) {
    iframeRef.value.src = previewUrl.value + '?t=' + Date.now()
  }
}

const doDeploy = async () => {
  deploying.value = true
  const res = await deployApp({ appId })
  deploying.value = false
  if (res.data.code === 0 && res.data.data) {
    const deployKey = res.data.data
    message.success(`Deployed! Key: ${deployKey}`)
    previewUrl.value = `/deploy/${deployKey}/index.html`
    await fetchApp()
  } else {
    message.error(res.data.message ?? 'Deploy failed')
  }
}

const openDeployed = () => {
  if (app.value?.deployKey) {
    window.open(`/deploy/${app.value.deployKey}/index.html`, '_blank')
  }
}

onMounted(fetchApp)
</script>

<style scoped>
#appChatPage {
  display: flex;
  height: calc(100vh - 64px);
  overflow: hidden;
}

/* ---- Left panel ---- */
.chat-panel {
  width: 380px;
  min-width: 320px;
  display: flex;
  flex-direction: column;
  border-right: 1px solid #f0f0f0;
}

.chat-header {
  padding: 12px 16px;
  border-bottom: 1px solid #f0f0f0;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.app-name {
  font-weight: 600;
  font-size: 15px;
}

.msg-list {
  flex: 1;
  overflow-y: auto;
  padding: 12px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.msg {
  display: flex;
}

.msg.user {
  justify-content: flex-end;
}

.msg.assistant {
  justify-content: flex-start;
}

.msg-bubble {
  max-width: 80%;
  padding: 8px 12px;
  border-radius: 12px;
  font-size: 14px;
  white-space: pre-wrap;
  word-break: break-word;
}

.msg.user .msg-bubble {
  background: #1890ff;
  color: #fff;
  border-bottom-right-radius: 4px;
}

.msg.assistant .msg-bubble {
  background: #f5f5f5;
  color: #333;
  border-bottom-left-radius: 4px;
}

.msg-bubble.streaming {
  background: #f5f5f5;
}

.cursor {
  display: inline-block;
  animation: blink 0.8s step-end infinite;
}

@keyframes blink {
  50% { opacity: 0; }
}

.chat-input {
  padding: 12px;
  border-top: 1px solid #f0f0f0;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.chat-actions {
  display: flex;
  gap: 8px;
}

.no-perm {
  font-size: 12px;
  color: #aaa;
  text-align: center;
}

/* ---- Right panel ---- */
.preview-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: #fafafa;
}

.preview-header {
  padding: 10px 16px;
  border-bottom: 1px solid #f0f0f0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-weight: 500;
}

.preview-iframe {
  flex: 1;
  border: none;
  width: 100%;
}

.preview-empty {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #bbb;
  gap: 8px;
}
</style>
