<template>
  <div id="homePage">
    <!-- Hero -->
    <div class="hero">
      <a
        href="https://github.com/lawrence900613/auto-web-generator"
        target="_blank"
        rel="noopener noreferrer"
        class="github-badge"
      >
        <svg height="16" width="16" viewBox="0 0 16 16" fill="currentColor" aria-hidden="true"><path d="M8 0C3.58 0 0 3.58 0 8c0 3.54 2.29 6.53 5.47 7.59.4.07.55-.17.55-.38 0-.19-.01-.82-.01-1.49-2.01.37-2.53-.49-2.69-.94-.09-.23-.48-.94-.82-1.13-.28-.15-.68-.52-.01-.53.63-.01 1.08.58 1.23.82.72 1.21 1.87.87 2.33.66.07-.52.28-.87.51-1.07-1.78-.2-3.64-.89-3.64-3.95 0-.87.31-1.59.82-2.15-.08-.2-.36-1.02.08-2.12 0 0 .67-.21 2.2.82.64-.18 1.32-.27 2-.27.68 0 1.36.09 2 .27 1.53-1.04 2.2-.82 2.2-.82.44 1.1.16 1.92.08 2.12.51.56.82 1.27.82 2.15 0 3.07-1.87 3.75-3.65 3.95.29.25.54.73.54 1.48 0 1.07-.01 1.93-.01 2.2 0 .21.15.46.55.38A8.013 8.013 0 0016 8c0-4.42-3.58-8-8-8z"/></svg>
        Star on GitHub
      </a>
      <h1 class="hero-title">AI Web Generator</h1>
      <p class="hero-desc">Tell us what you need. We'll handle the rest. No code, no tech background needed.</p>
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
    label: '🎨 Portfolio',
    prompt:
      'Build a polished, production-ready one-page portfolio site for Alex Rivera, a freelance UI/UX Designer & Creative Technologist, using Vue with clean, component-based code and responsive design. '
  },
  {
    label: '✍️ Personal Blog',
    prompt:
      'Build a modern editorial blog website for Sarah Chen, a tech writer focused on productivity, indie hacking, and software engineering, using Vue and responsive component-based architecture.'
  },
  {
    label: '🛍️ Online Store',
    prompt:
      'Build a polished, production-ready e-commerce website for a modern skincare brand, using Vue with clean, component-based code and responsive design. Create a premium shopping experience with a strong brand story, engaging product presentation, intuitive browsing and filtering, persuasive product detail pages, and a smooth cart-to-checkout flow. Focus on a minimal luxury visual direction with refined typography, soft depth, elegant interactions, and clear information hierarchy.',
  },
  {
    label: '🏢 Company Site',
    prompt:
      'Build a high-converting B2B SaaS marketing website for a modern AI-powered productivity platform, using Vue with clean, reusable, responsive sections. Create a clear conversion-focused funnel with a compelling hero, strong social proof, benefit-driven feature highlights, simple process explanation, transparent pricing tiers, customer testimonials, and an FAQ section. Emphasize strategic CTA placement, polished mobile behavior, and a contemporary visual style with strong contrast, modern typography, subtle depth, and smooth scroll-based motion.',
  },
  {
    label: '📰 News & Media',
    prompt:
      'Build a polished, content-first online news magazine website, using Vue with clean, component-based architecture and responsive design. Include a clear editorial navigation system, prominent headline storytelling, structured category coverage, searchable content discovery, and engaging article detail pages with rich reading layouts and related-content recommendations. Add common media-platform elements such as breaking updates, trending/most-read side modules, newsletter capture, and subscription-focused CTAs. Emphasize readability, fast scanning, and a refined editorial visual style with strong typography, high contrast, and balanced information density.',
  },
  {
    label: '🍽️ Restaurant',
    prompt:
      'Build a premium, conversion-focused restaurant website for an upscale dining brand, using Vue with clean, component-based code and fully responsive design. Create an immersive first impression with strong visual storytelling, clear reservation-first CTAs, and an elegant brand narrative. Include key hospitality sections such as about/story, structured menu presentation, gallery, chef highlights, reservation flow, and practical visit information (hours, location, contact). Emphasize a cinematic fine-dining aesthetic with refined typography, rich contrast, warm accent tones, and subtle motion that enhances atmosphere without hurting usability.',
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

.github-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 20px;
  padding: 6px 14px;
  border-radius: 20px;
  border: 1px solid #d0d7de;
  background: #f6f8fa;
  color: #24292f;
  font-size: 13px;
  font-weight: 500;
  text-decoration: none;
  transition: background 0.15s, border-color 0.15s;
}

.github-badge:hover {
  background: #eaeef2;
  border-color: #b0b8c1;
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
