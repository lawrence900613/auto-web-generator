<template>
  <div id="homePage">
    <!-- Hero -->
    <div class="hero">
      <h1 class="hero-title">Auto Web Generator</h1>
      <p class="hero-desc">Describe your website — AI builds it instantly</p>
      <div class="prompt-box">
        <a-textarea
          v-model:value="initPrompt"
          placeholder="e.g. A personal blog about travel photography..."
          :maxlength="1000"
          :auto-size="{ minRows: 3, maxRows: 6 }"
          show-count
        />
        <div class="quick-prompts">
          <a-tag
            v-for="tpl in promptTemplates"
            :key="tpl"
            class="quick-tag"
            @click="initPrompt = tpl"
          >{{ tpl }}</a-tag>
        </div>
        <a-button type="primary" size="large" :loading="creating" @click="doCreateApp">
          Generate Website
        </a-button>
      </div>
    </div>

    <!-- My apps -->
    <div v-if="loginUserStore.loginUser?.id" class="section">
      <h2 class="section-title">My Apps</h2>
      <a-spin :spinning="loadingMy">
        <a-empty v-if="myApps.length === 0 && !loadingMy" description="No apps yet" />
        <div class="card-grid">
          <AppCard v-for="app in myApps" :key="app.id" :app="app" @click="goChat(app)" />
        </div>
        <div class="pagination">
          <a-pagination
            v-if="myTotal > 0"
            v-model:current="myPage"
            :total="myTotal"
            :page-size="PAGE_SIZE"
            show-less-items
            @change="fetchMyApps"
          />
        </div>
      </a-spin>
    </div>

    <!-- Featured apps -->
    <div class="section">
      <h2 class="section-title">Featured Apps</h2>
      <a-spin :spinning="loadingGood">
        <a-empty v-if="goodApps.length === 0 && !loadingGood" description="No featured apps yet" />
        <div class="card-grid">
          <AppCard v-for="app in goodApps" :key="app.id" :app="app" @click="goChat(app)" />
        </div>
        <div class="pagination">
          <a-pagination
            v-if="goodTotal > 0"
            v-model:current="goodPage"
            :total="goodTotal"
            :page-size="PAGE_SIZE"
            show-less-items
            @change="fetchGoodApps"
          />
        </div>
      </a-spin>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { addApp, listMyAppVoByPage, listGoodAppVoByPage } from '@/api/appController'
import { useLoginUserStore } from '@/stores/loginUser'
import AppCard from '@/components/AppCard.vue'

const router = useRouter()
const loginUserStore = useLoginUserStore()

const PAGE_SIZE = 6
const initPrompt = ref('')
const creating = ref(false)

const promptTemplates = [
  'Personal blog website',
  'Corporate homepage',
  'Online store',
  'Portfolio showcase',
]

const myApps = ref<API.AppVO[]>([])
const myTotal = ref(0)
const myPage = ref(1)
const loadingMy = ref(false)

const goodApps = ref<API.AppVO[]>([])
const goodTotal = ref(0)
const goodPage = ref(1)
const loadingGood = ref(false)

const doCreateApp = async () => {
  if (!initPrompt.value.trim()) {
    message.warning('Please enter a description')
    return
  }
  if (!loginUserStore.loginUser?.id) {
    router.push('/user/login')
    return
  }
  creating.value = true
  const res = await addApp({ initPrompt: initPrompt.value })
  creating.value = false
  if (res.data.code === 0 && res.data.data) {
    message.success('App created!')
    router.push(`/app/chat/${res.data.data}`)
  } else {
    message.error('Failed: ' + res.data.message)
  }
}

const fetchMyApps = async () => {
  if (!loginUserStore.loginUser?.id) return
  loadingMy.value = true
  const res = await listMyAppVoByPage({ page: myPage.value, size: PAGE_SIZE })
  loadingMy.value = false
  if (res.data.code === 0) {
    myApps.value = res.data.data?.records ?? []
    myTotal.value = res.data.data?.totalRow ?? 0
  }
}

const fetchGoodApps = async () => {
  loadingGood.value = true
  const res = await listGoodAppVoByPage({ page: goodPage.value, size: PAGE_SIZE })
  loadingGood.value = false
  if (res.data.code === 0) {
    goodApps.value = res.data.data?.records ?? []
    goodTotal.value = res.data.data?.totalRow ?? 0
  }
}

const goChat = (app: API.AppVO) => {
  router.push(`/app/chat/${app.id}`)
}

onMounted(() => {
  fetchMyApps()
  fetchGoodApps()
})
</script>

<style scoped>
#homePage {
  max-width: 1100px;
  margin: 0 auto;
  padding: 24px 24px 80px;
}

.hero {
  text-align: center;
  padding: 48px 0 32px;
}

.hero-title {
  font-size: 2.4rem;
  font-weight: 700;
  color: #1890ff;
  margin-bottom: 8px;
}

.hero-desc {
  color: #888;
  font-size: 1rem;
  margin-bottom: 24px;
}

.prompt-box {
  max-width: 680px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.quick-prompts {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.quick-tag {
  cursor: pointer;
  border-radius: 12px;
}

.section {
  margin-top: 48px;
}

.section-title {
  font-size: 1.3rem;
  font-weight: 600;
  margin-bottom: 16px;
}

.card-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 16px;
}

.pagination {
  margin-top: 24px;
  text-align: center;
}
</style>
