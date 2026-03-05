<template>
  <div id="homePage">
    <!-- Hero -->
    <div class="hero">
      <h1 class="hero-title">AI Web Generator</h1>
      <p class="hero-desc">Create any website with just one sentence</p>
      <div class="prompt-box">
        <div class="prompt-input-wrap">
          <a-textarea
            v-model:value="initPrompt"
            placeholder="Build me a personal blog website..."
            :maxlength="10000"
            :auto-size="{ minRows: 3, maxRows: 1000 }"
            @keydown.enter.exact.prevent="doCreateApp"
          />
          <a-button
            type="primary"
            shape="circle"
            class="submit-btn"
            :loading="creating"
            @click="doCreateApp"
          >
            <template #icon><ArrowUpOutlined /></template>
          </a-button>
        </div>
        <div class="quick-prompts">
          <span
            v-for="tpl in promptTemplates"
            :key="tpl.label"
            class="quick-chip"
            @click="initPrompt = tpl.prompt"
          >{{ tpl.label }}</span>
        </div>
      </div>
    </div>

    <!-- My apps -->
    <div v-if="loginUserStore.loginUser?.id" class="section">
      <h2 class="section-title">My Apps</h2>
      <a-spin :spinning="loadingMy">
        <a-empty v-if="myApps.length === 0 && !loadingMy" description="No apps yet" />
        <div class="card-grid">
          <AppCard v-for="appItem in myApps" :key="appItem.id" :app="appItem" />
        </div>
        <div class="pagination">
          <a-pagination
            v-if="myTotal > 0"
            v-model:current="myPage"
            :total="myTotal"
            :page-size="PAGE_SIZE"
            show-less-items
            :show-total="(t: number) => `Total ${t} apps`"
            @change="fetchMyApps"
          />
        </div>
      </a-spin>
    </div>

    <!-- Featured apps -->
    <div class="section section-last">
      <h2 class="section-title">Featured Apps</h2>
      <a-spin :spinning="loadingGood">
        <a-empty v-if="goodApps.length === 0 && !loadingGood" description="No featured apps yet" />
        <div class="card-grid">
          <AppCard v-for="appItem in goodApps" :key="appItem.id" :app="appItem" />
        </div>
        <div class="pagination">
          <a-pagination
            v-if="goodTotal > 0"
            v-model:current="goodPage"
            :total="goodTotal"
            :page-size="PAGE_SIZE"
            show-less-items
            :show-total="(t: number) => `Total ${t} apps`"
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
import { ArrowUpOutlined } from '@ant-design/icons-vue'
import { addApp, listMyAppVoByPage, listGoodAppVoByPage } from '@/api/appController'
import { useLoginUserStore } from '@/stores/loginUser'
import AppCard from '@/components/AppCard.vue'

const router = useRouter()
const loginUserStore = useLoginUserStore()

const PAGE_SIZE = 6
const initPrompt = ref('')
const creating = ref(false)

const promptTemplates = [
  {
    label: 'Personal Blog',
    prompt:
      'Build me a clean personal blog website. The homepage should display a list of latest posts with category and tag filtering. Each post has a detail page with a table of contents, comments section, and a search bar at the top. Support dark mode toggle. Overall style: fresh and minimal.',
  },
  {
    label: 'Corporate Site',
    prompt:
      'Build a corporate website for a tech startup. Include a full-screen hero banner with animation, followed by sections for core product features, customer testimonials, partner logos, team member profiles, and an online contact form. Style: modern, professional, and tech-forward.',
  },
  {
    label: 'Online Store',
    prompt:
      'Build an online store for a specialty coffee shop. The homepage has a hero carousel and best-seller recommendations. Product listings support filtering by type and sorting by price. Each product has a detail page with reviews. Include a shopping cart and a simulated checkout flow. Style: warm and modern, brown tones.',
  },
  {
    label: 'Portfolio',
    prompt:
      'Build a personal portfolio website for a UI designer. Include an animated hero section, a project gallery organized by category, a skills and work experience section, client testimonials, and a contact page. Style: clean, creative, and minimalist with black and white palette.',
  },
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

onMounted(() => {
  fetchMyApps()
  fetchGoodApps()
})
</script>

<style scoped>
#homePage {
  min-height: 100%;
}

/* ---- Hero ---- */
.hero {
  padding: 80px 24px 64px;
  text-align: center;
}

.hero-title {
  font-size: 3.8rem;
  font-weight: 900;
  letter-spacing: -2px;
  margin-bottom: 14px;
  background: linear-gradient(135deg, #3a5fa8 0%, #7b4fa8 50%, #b02070 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  filter: drop-shadow(0 2px 12px rgba(120, 60, 160, 0.15));
}

.hero-desc {
  font-size: 1.2rem;
  color: rgba(60, 40, 100, 0.75);
  margin-bottom: 36px;
  font-weight: 500;
}

.prompt-box {
  max-width: 680px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.prompt-input-wrap {
  position: relative;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 8px 40px rgba(120, 80, 160, 0.18);
  padding: 16px 60px 16px 18px;
}

.prompt-input-wrap :deep(.ant-input) {
  border: none !important;
  box-shadow: none !important;
  resize: none;
  font-size: 16px;
  padding: 0;
  background: transparent;
}

.submit-btn {
  position: absolute;
  right: 14px;
  bottom: 14px;
  width: 40px;
  height: 40px;
}

/* Quick chips */
.quick-prompts {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 8px;
}

.quick-chip {
  cursor: pointer;
  border-radius: 20px;
  padding: 7px 20px;
  font-size: 15px;
  background: rgba(255, 255, 255, 0.28);
  border: 1px solid rgba(255, 255, 255, 0.55);
  color: #fff;
  transition: all 0.18s;
  user-select: none;
  backdrop-filter: blur(4px);
}

.quick-chip:hover {
  background: rgba(255, 255, 255, 0.45);
  border-color: rgba(255, 255, 255, 0.9);
}

/* ---- App sections ---- */
.section {
  max-width: 1160px;
  margin: 0 auto;
  padding: 0 28px 40px;
}

.section-last {
  padding-bottom: 80px;
}

.section-title {
  font-size: 1.4rem;
  font-weight: 700;
  margin-bottom: 18px;
  color: #fff;
  text-shadow: 0 1px 8px rgba(100, 80, 160, 0.12);
}

.card-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 18px;
}

.pagination {
  margin-top: 28px;
  text-align: center;
}
</style>
