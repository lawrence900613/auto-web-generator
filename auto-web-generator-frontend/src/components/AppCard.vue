<template>
  <div class="app-card" @click="goChat">
    <div class="app-cover">
      <img v-if="coverSrc" :src="coverSrc" :alt="app.appName" />
      <div v-else class="cover-placeholder">
        <div class="cover-icon"><CodeOutlined /></div>
      </div>
      <!-- Hover overlay buttons -->
      <div class="card-overlay">
        <a-button class="overlay-btn" ghost @click.stop="goChat">View Chat</a-button>
        <a-button v-if="app.deployKey" class="overlay-btn" ghost @click.stop="viewWork">View Work</a-button>
      </div>
    </div>
    <div class="app-info">
      <div class="app-icon"><CodeOutlined /></div>
      <div class="app-text">
        <div class="app-name">{{ app.appName || 'Untitled' }}</div>
        <div class="app-author">{{ app.user?.userName ?? 'Unknown' }}</div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { CodeOutlined } from '@ant-design/icons-vue'

const props = defineProps<{ app: API.AppVO }>()
const router = useRouter()

// Strip /api suffix to get bare host (e.g. "http://localhost:8123")
const rawApiBase = ((import.meta.env.VITE_API_BASE || '/api') as string).trim()
const apiBase = /^https?:\/\//.test(rawApiBase)
  ? rawApiBase
  : rawApiBase.startsWith('/')
    ? rawApiBase
    : `/${rawApiBase}`
const deployDomain = ((import.meta.env.VITE_DEPLOY_DOMAIN || window.location.origin) as string).trim()
const apiHost = apiBase.replace(/\/api$/, '')

// Resolve cover URL: absolute URLs (https://...) pass through as-is;
// relative paths (/api/covers/...) get the host prepended.
const coverSrc = computed(() => {
  const cover = props.app.cover
  if (!cover) return ''
  if (cover.startsWith('http')) return cover
  return apiHost + cover
})

const goChat = () => router.push(`/app/chat/${props.app.id}?view=1`)
const viewWork = () => window.open(`${deployDomain}/${props.app.deployKey}`, '_blank')
</script>

<style scoped>
.app-card {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  overflow: hidden;
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
}

.app-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 28px rgba(0, 0, 0, 0.12);
}

/* Cover image */
.app-cover {
  position: relative;
  height: 175px;
  overflow: hidden;
  background: linear-gradient(135deg, #dde8f8, #c8d8f0);
}

.app-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.cover-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.cover-icon {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  background: linear-gradient(135deg, #4f86f7, #2563eb);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 22px;
}

/* Hover overlay */
.card-overlay {
  position: absolute;
  inset: 0;
  background: rgba(0, 0, 0, 0.4);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 10px;
  opacity: 0;
  transition: opacity 0.2s;
}

.app-card:hover .card-overlay {
  opacity: 1;
}

.overlay-btn {
  width: 120px;
  color: #fff !important;
  border-color: rgba(255, 255, 255, 0.8) !important;
  font-size: 13px;
}

/* Info row */
.app-info {
  padding: 12px 14px 14px;
  display: flex;
  align-items: center;
  gap: 10px;
}

.app-icon {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: linear-gradient(135deg, #4f86f7, #2563eb);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 15px;
  flex-shrink: 0;
}

.app-text {
  flex: 1;
  min-width: 0;
}

.app-name {
  font-weight: 700;
  font-size: 14px;
  color: #1a1a2e;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.app-author {
  font-size: 12px;
  color: #999;
  margin-top: 2px;
}
</style>
