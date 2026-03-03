<template>
  <div class="app-card" @click="$emit('click', app)">
    <div class="app-cover">
      <img v-if="app.cover" :src="app.cover" :alt="app.appName" />
      <div v-else class="cover-placeholder">
        <CodeOutlined style="font-size: 32px; color: #1890ff" />
      </div>
    </div>
    <div class="app-info">
      <div class="app-name">{{ app.appName || 'Untitled' }}</div>
      <div class="app-prompt">{{ app.initPrompt }}</div>
      <div class="app-meta">
        <a-avatar v-if="app.user?.userAvatar" :src="app.user.userAvatar" :size="18" />
        <span class="app-author">{{ app.user?.userName ?? 'Unknown' }}</span>
        <a-tag v-if="app.deployedTime" color="green" style="margin-left: auto">Deployed</a-tag>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { CodeOutlined } from '@ant-design/icons-vue'

defineProps<{ app: API.AppVO }>()
defineEmits<{ (e: 'click', app: API.AppVO): void }>()
</script>

<style scoped>
.app-card {
  border: 1px solid #f0f0f0;
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
  transition: box-shadow 0.2s;
  background: #fff;
}

.app-card:hover {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
}

.app-cover {
  height: 140px;
  background: #f5f5f5;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.app-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.cover-placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
}

.app-info {
  padding: 12px;
}

.app-name {
  font-weight: 600;
  font-size: 15px;
  margin-bottom: 4px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.app-prompt {
  font-size: 12px;
  color: #888;
  height: 36px;
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  margin-bottom: 8px;
}

.app-meta {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: #aaa;
}

.app-author {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>
