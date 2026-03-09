<template>
  <div id="appEditPage">
    <!-- Page header -->
    <div class="page-header">
      <a-button type="text" class="back-btn" @click="router.back()">
        <template #icon><LeftOutlined /></template>
        Back
      </a-button>
      <h2 class="page-title">Edit App Info</h2>
    </div>

    <a-spin :spinning="loading">
      <!-- Basic Info card -->
      <a-card class="info-card" title="Basic Info">
        <a-form layout="vertical" :model="formState" @finish="doUpdate">

          <!-- App Name (everyone) -->
          <a-form-item label="App Name" name="appName" :required="true">
            <a-input v-model:value="formState.appName" :maxlength="50" show-count />
          </a-form-item>

          <!-- Cover (admin only) -->
          <template v-if="isAdmin">
            <a-form-item label="Cover Image URL" name="cover">
              <a-input v-model:value="formState.cover" placeholder="https://..." />
              <div v-if="formState.cover" class="cover-preview">
                <img :src="formState.cover" alt="cover preview" />
              </div>
              <div class="field-hint">Supports image URL, recommended size: 400x300</div>
            </a-form-item>

            <!-- Priority (admin only) -->
            <a-form-item label="Priority" name="priority">
              <a-input-number v-model:value="formState.priority" :min="0" :max="99" style="width: 180px" />
              <div class="field-hint">Set to 99 to mark as featured app</div>
            </a-form-item>
          </template>

          <!-- Initial Prompt (read-only) -->
          <a-form-item label="Initial Prompt">
            <a-textarea
              :value="app?.initPrompt"
              :maxlength="1000"
              show-count
              disabled
              :auto-size="{ minRows: 3, maxRows: 6 }"
            />
            <div class="field-hint">Initial prompt cannot be modified</div>
          </a-form-item>

          <!-- Code Gen Type (read-only) -->
          <a-form-item label="Code Gen Type">
            <a-input :value="app?.codeGenType" disabled />
            <div class="field-hint">Code gen type cannot be modified</div>
          </a-form-item>

          <!-- Deploy Key (read-only) -->
          <a-form-item label="Deploy Key">
            <a-input :value="app?.deployKey ?? ''" disabled />
            <div class="field-hint">Deploy key cannot be modified</div>
          </a-form-item>

          <!-- Actions -->
          <div class="form-actions">
            <a-button type="primary" html-type="submit">Save</a-button>
            <a-button @click="resetForm">Reset</a-button>
            <a-button type="link" @click="router.push(`/app/chat/${appId}`)">Go to Chat</a-button>
          </div>
        </a-form>
      </a-card>

      <!-- App Info card -->
      <a-card v-if="app" class="info-card" title="App Info">
        <a-descriptions :column="2" bordered size="small">
          <a-descriptions-item label="App ID">{{ app.id }}</a-descriptions-item>
          <a-descriptions-item label="Creator">
            <div class="creator-cell">
              <a-avatar v-if="app.user?.userAvatar" :src="app.user.userAvatar" :size="20" />
              <a-avatar v-else :size="20">{{ (app.user?.userName ?? 'U')[0].toUpperCase() }}</a-avatar>
              <span>{{ app.user?.userName ?? 'Unknown' }}</span>
            </div>
          </a-descriptions-item>
          <a-descriptions-item label="Create Time">{{ app.createTime }}</a-descriptions-item>
          <a-descriptions-item label="Update Time">{{ app.updateTime }}</a-descriptions-item>
          <a-descriptions-item label="Deploy Time">{{ app.deployedTime ?? '—' }}</a-descriptions-item>
          <a-descriptions-item label="Access Link">
            <a v-if="app.deployKey" @click.prevent="openDeploy">View Preview</a>
            <span v-else style="color: #aaa">Not deployed</span>
          </a-descriptions-item>
        </a-descriptions>
      </a-card>
    </a-spin>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { LeftOutlined } from '@ant-design/icons-vue'
import { getAppVoById, updateApp, updateAppByAdmin } from '@/api/appController'
import { useLoginUserStore } from '@/stores/loginUser'

const apiBase = ((import.meta.env.VITE_API_BASE || '/api') as string).trim()

const route = useRoute()
const router = useRouter()
const loginUserStore = useLoginUserStore()

const appId = route.params.id as string
const app = ref<API.AppVO>()
const loading = ref(false)
const isAdmin = loginUserStore.loginUser?.userRole === 'admin'

const formState = reactive({
  appName: '',
  cover: '',
  priority: 0,
})

const fetchApp = async () => {
  loading.value = true
  const res = await getAppVoById({ id: appId })
  loading.value = false
  if (res.data.code === 0 && res.data.data) {
    app.value = res.data.data
    formState.appName = res.data.data.appName ?? ''
    formState.cover = res.data.data.cover ?? ''
    formState.priority = res.data.data.priority ?? 0
  }
}

const resetForm = () => {
  formState.appName = app.value?.appName ?? ''
  formState.cover = app.value?.cover ?? ''
  formState.priority = app.value?.priority ?? 0
}

const doUpdate = async () => {
  if (isAdmin) {
    const res = await updateAppByAdmin({
      id: appId,
      appName: formState.appName,
      cover: formState.cover,
      priority: formState.priority,
    })
    if (res.data.code === 0) {
      message.success('Updated')
      fetchApp()
    } else {
      message.error(res.data.message)
    }
  } else {
    const res = await updateApp({ id: appId, appName: formState.appName })
    if (res.data.code === 0) {
      message.success('Updated')
      fetchApp()
    } else {
      message.error(res.data.message)
    }
  }
}

const openDeploy = () => {
  if (app.value?.deployKey) {
    window.open(`${apiBase}/deploy/${app.value.deployKey}/index.html`, '_blank')
  }
}

onMounted(fetchApp)
</script>

<style scoped>
#appEditPage {
  max-width: 760px;
  margin: 0 auto;
  padding: 24px;
}

.page-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 20px;
}

.back-btn {
  color: #666;
  padding: 0 4px;
}

.page-title {
  font-size: 1.2rem;
  font-weight: 600;
  margin: 0;
  color: #1a1a2e;
}

.info-card {
  margin-bottom: 20px;
  border-radius: 8px;
}

.cover-preview {
  margin-top: 10px;
  border-radius: 6px;
  overflow: hidden;
  max-width: 220px;
  border: 1px solid #eee;
}

.cover-preview img {
  width: 100%;
  display: block;
}

.field-hint {
  font-size: 12px;
  color: #999;
  margin-top: 4px;
}

.form-actions {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-top: 8px;
}

.creator-cell {
  display: flex;
  align-items: center;
  gap: 6px;
}
</style>
