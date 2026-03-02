<template>
  <a-layout-header class="header">
    <a-row :wrap="false">
      <!-- Left: Logo and title -->
      <a-col flex="220px">
        <RouterLink to="/">
          <div class="header-left">
            <img class="logo" src="@/assets/logo.svg" alt="Logo" />
            <h1 class="site-title">Auto Web Generator</h1>
          </div>
        </RouterLink>
      </a-col>
      <!-- Center: Navigation menu -->
      <a-col flex="auto">
        <a-menu
          v-model:selectedKeys="selectedKeys"
          mode="horizontal"
          :items="menuItems"
          @click="handleMenuClick"
        />
      </a-col>
      <!-- Right: User area -->
      <a-col>
        <div class="user-login-status">
          <a-button type="primary">Login</a-button>
        </div>
      </a-col>
    </a-row>
  </a-layout-header>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import type { MenuProps } from 'ant-design-vue'

const route = useRoute()
const router = useRouter()

const selectedKeys = ref<string[]>([route.path])

router.afterEach((to) => {
  selectedKeys.value = [to.path]
})

const menuItems = ref([
  {
    key: '/',
    label: 'Home',
    title: 'Home',
  },
  {
    key: '/about',
    label: 'About',
    title: 'About',
  },
])

const handleMenuClick: MenuProps['onClick'] = (e) => {
  const key = e.key as string
  selectedKeys.value = [key]
  if (key.startsWith('/')) {
    router.push(key)
  }
}
</script>

<style scoped>
.header {
  background: #fff;
  padding: 0 24px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.logo {
  height: 48px;
  width: 48px;
}

.site-title {
  margin: 0;
  font-size: 18px;
  color: #1890ff;
  white-space: nowrap;
}

.user-login-status {
  display: flex;
  align-items: center;
  height: 100%;
}

:deep(.ant-menu-horizontal) {
  border-bottom: none !important;
}
</style>
