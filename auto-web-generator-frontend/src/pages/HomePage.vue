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
      'Build a polished, production-ready one-page portfolio site for Alex Rivera, a freelance UI/UX Designer & Creative Technologist, using Vue with clean, component-based code and responsive design. ' +
      'Include: (1) a full-screen hero with animated gradient background, name/title, and short tagline; (2) a filterable Projects section with tabs All/Web/Mobile/Branding and 6 cards (Lumina Finance App, Orbit Dashboard, Breeze Mobile Banking, Bloom E-commerce, Nova Brand Identity, Pixel CMS), each with image placeholder, category badge, and hover overlay + "View Case Study" CTA; (3) an About section with avatar placeholder, 3-paragraph bio, timeline (Freelance 2022-present, Shopify 2020-2022, Google 2018-2020), and animated skill bars (Figma 95%, Vue.js 80%, Motion Design 75%, Prototyping 90%); (4) Testimonials with 3 client quotes including avatar, name, and company; (5) Contact form with name, email, project type dropdown (Web/Mobile/Branding/Other), and message; (6) Footer with social links (Dribbble, Behance, LinkedIn, GitHub). ' +
      'Style direction: dark premium aesthetic with background #0f0f0f, accent #7c3aed, Inter font, subtle glow effects, strong visual hierarchy, smooth scrolling, and tasteful scroll-triggered animations throughout.',
  },
  {
    label: '✍️ Personal Blog',
    prompt:
      'Build a modern editorial blog website for Sarah Chen, a tech writer focused on productivity, indie hacking, and software engineering, using Vue and responsive component-based architecture. ' +
      'Include a sticky header with logo "Sarah Chen", nav links (Home, Essays, Projects, About, Newsletter), search icon, and dark mode toggle; a homepage with one featured hero post and a 3-column grid of recent posts with category badge, title, 2-sentence excerpt, date, and reading time. ' +
      'Add 6 mock posts: "How I Shipped a SaaS in 30 Days" (Indie Hacking, 8 min), "The Pomodoro Myth" (Productivity, 5 min), "Why I Quit Big Tech" (Life, 12 min), "Building in Public: Month 3" (Indie Hacking, 6 min), "My Obsidian Setup for 2024" (Productivity, 9 min), and "On Saying No" (Life, 4 min). ' +
      'Create a post detail layout with full-width cover image, floating table of contents, rich text body with code blocks and pull quotes, tags, author bio card, and 3 related posts; add a sidebar with about blurb, newsletter form ("Get my weekly essay"), top posts, and tag cloud; add an About page with photo, 4-paragraph bio, and tools list (Mac M2, Notion, Obsidian, Raycast, VS Code). ' +
      'Style direction: warm editorial look with background #faf8f5, Playfair Display headings, clean sans-serif body text, amber accent #d97706, readable 700px article width, and smooth scroll and reveal animations.',
  },
  {
    label: '🛍️ Online Store',
    prompt:
      'Build a premium e-commerce website for a skincare brand named "Lumiere Skin" with clean Vue components, responsive pages, and polished shopping UX. ' +
      'Homepage: full-screen hero with tagline "Science-backed skincare for radiant skin" and Shop Now CTA, best-sellers row (4 products), brand values strip (Clean Ingredients, Cruelty-Free, Sustainable Packaging, Dermatologist Tested), before/after results section, and customer review highlights. ' +
      'Products page: filterable product grid with filters for Skin Type (Oily, Dry, Combination, Sensitive), Concern (Hydration, Anti-Aging, Brightening), and price range; include 6 mock products with prices: Luminous Vitamin C Serum 30ml ($48), Deep Hydration Cream 50ml ($62), Gentle Foam Cleanser 150ml ($34), SPF 50 Daily Shield Fluid 30ml ($55), Retinol Night Repair Serum 30ml ($72), Soothing Rose Toner 200ml ($38). ' +
      'Product detail page: image gallery, rating summary (4.8/5 from 124 reviews), size selector, quantity stepper, Add to Cart and Wishlist buttons, ingredients accordion, how-to-use steps, and 3 written reviews with avatar and rating; include slide-out cart with subtotal, free-shipping progress, and checkout CTA plus a checkout page with shipping form, order summary, and card payment fields. ' +
      'Style direction: minimal luxury with ivory background #faf9f7, gold accent #c9a96e, elegant serif headings, soft shadows, and smooth micro-interactions.',
  },
  {
    label: '🏢 Company Site',
    prompt:
      'Build a high-converting B2B SaaS marketing website for "Streamline AI", an AI-powered project management and team collaboration platform, using Vue and reusable responsive sections. ' +
      'Hero section: headline "Your team\'s work, finally in sync", supporting copy, email capture input, primary CTA "Start Free Trial", and a browser-frame product screenshot mockup; include social proof logos for Nexus Corp, Atlas Health, Orbit Ventures, Meridian Bank, and Crestline Media. ' +
      'Add a features grid with 6 cards (AI Task Prioritization, Real-Time Collaboration, Gantt Chart View, Slack & Jira Integration, Automated Weekly Reports, 99.9% Uptime SLA), an animated 3-step "How It Works" flow, and a pricing table with 3 tiers: Starter $0/mo, Pro $29/mo, Enterprise $99/mo, each with feature bullets and CTA. ' +
      'Include testimonials (3 cards with photo, quote, name, title, company) and a 6-item FAQ accordion; ensure strong CTA placement throughout and polished mobile behavior. ' +
      'Style direction: dark navy hero (#0d1b2a), clean white content sections, electric blue accent (#3b82f6), Inter font, subtle glassmorphism cards, and smooth scroll-triggered animations.',
  },
  {
    label: '📰 News & Media',
    prompt:
      'Build an online news magazine website called "The Daily Pulse" covering Tech, Business, Culture, and Science, with a fast, content-first layout built in Vue. ' +
      'Include a breaking-news ticker with 3 rotating headlines, a header with logo, nav (Home/Tech/Business/Culture/Science/Opinion), search, dark mode toggle, and Subscribe button; homepage should feature one hero story and a 3-column article grid. ' +
      'Add 8 mock articles with realistic authors, relative timestamps, and two-sentence excerpts: "The Rise of Autonomous Warehouses" (James Okafor, Business), "James Webb Captures Earliest Galaxy Yet" (Dr. Priya Nair, Science), "How Gen Z is Redefining Luxury" (Mia Torres, Culture), "EU Passes Landmark AI Act" (Lena Hoffmann, Tech), "Inside the Battery Startup That Could Kill Tesla" (Ryan Cho, Business), "CRISPR Cure for Sickle Cell Approved" (Dr. Amara Diallo, Science), "The Death of the Album" (Finn Walsh, Culture), "Quantum Computing Just Got 10x Cheaper" (Yuki Tanaka, Tech). ' +
      'Create an article detail page with byline block, publish metadata, share actions, long-form body structure (intro + 3 sections + blockquote + inline image), tags, and a "You May Also Like" row; add a sidebar with trending topics, most-read list, and newsletter form. ' +
      'Style direction: crisp editorial visual system with white background, bold serif headlines, red category accents, dense but readable grid, and high contrast typography.',
  },
  {
    label: '🍽️ Restaurant',
    prompt:
      'Build an upscale restaurant website for "Osteria Bellavia" (123 Mulberry Street, New York, NY) with immersive visuals, conversion-focused reservations, and responsive design in Vue. ' +
      'Hero: full-screen atmospheric background, elegant logotype, tagline "Authentic flavors from the heart of Tuscany since 2008", and dual CTAs ("Reserve a Table" and "View Menu"); About section: two-column founder story (Chef Marco Bellavia, from Florence, established 2008) with award highlights (Michelin Bib Gourmand 2022, James Beard Nominee 2023, New York Times Critics\' Pick). ' +
      'Menu section: tabbed categories (Antipasti, Primi, Secondi, Dolci, Wine & Drinks) with 5 items each including Italian name, English description, and price; include sample dishes such as Burrata con Prosciutto di Parma ($22), Tagliatelle al Tartufo Nero ($34), and Branzino al Forno ($48). ' +
      'Add a 9-image gallery grid, chef spotlight cards for Marco Bellavia and Pastry Chef Sofia Rossi, a reservation form (date, time 5:00 PM-10:00 PM in 30-minute slots, party size 1-12, name, phone, email, special requests), and a Hours & Location section (Tue-Thu 5 PM-10 PM, Fri-Sat 5 PM-11 PM, Sun 4 PM-9 PM, closed Monday) with map placeholder and phone +1 (212) 555-0198. ' +
      'Style direction: cinematic fine-dining look with deep black (#0c0c0c), burgundy (#8b1a1a), cream typography, candlelight gold accents (#d4a843), Cormorant Garamond headings, and subtle motion transitions.',
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
