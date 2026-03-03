<template>
  <div id="appEditPage">
    <a-spin :spinning="loading">
      <a-card v-if="app" :title="'Edit App — ' + (app.appName ?? 'Untitled')">
        <a-form layout="vertical" :model="formState" @finish="doUpdate">
          <!-- App name -->
          <a-form-item label="App Name" name="appName">
            <a-input v-model:value="formState.appName" :maxlength="50" show-count />
          </a-form-item>

          <!-- Cover (admin only) -->
          <a-form-item v-if="isAdmin" label="Cover Image URL" name="cover">
            <a-input v-model:value="formState.cover" placeholder="https://..." />
          </a-form-item>

          <!-- Priority (admin only) -->
          <a-form-item v-if="isAdmin" label="Priority (99 = featured)" name="priority">
            <a-input-number v-model:value="formState.priority" :min="0" :max="99" />
          </a-form-item>

          <!-- Read-only fields -->
          <a-descriptions bordered size="small" :column="1">
            <a-descriptions-item label="Initial Prompt">{{ app.initPrompt }}</a-descriptions-item>
            <a-descriptions-item label="Code Gen Type">{{ app.codeGenType }}</a-descriptions-item>
            <a-descriptions-item label="Deploy Key">{{ app.deployKey ?? 'Not deployed' }}</a-descriptions-item>
            <a-descriptions-item label="Created">{{ app.createTime }}</a-descriptions-item>
          </a-descriptions>

          <div style="margin-top: 16px; display: flex; gap: 8px">
            <a-button type="primary" html-type="submit">Save</a-button>
            <a-button @click="router.push(`/app/chat/${app.id}`)">Go to Chat</a-button>
            <a-button
              v-if="app.deployKey"
              @click="openDeploy"
            >Preview Deployed</a-button>
          </div>
        </a-form>
      </a-card>
    </a-spin>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { getAppVoById, updateApp, updateAppByAdmin } from '@/api/appController'
import { useLoginUserStore } from '@/stores/loginUser'

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
    window.open(`/deploy/${app.value.deployKey}/index.html`, '_blank')
  }
}

onMounted(fetchApp)
</script>

<style scoped>
#appEditPage {
  max-width: 720px;
  margin: 24px auto;
  padding: 0 24px;
}
</style>
