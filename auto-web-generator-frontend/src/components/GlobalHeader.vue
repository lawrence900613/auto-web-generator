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

      <!-- Center: Navigation menu (filtered by role) -->
      <a-col flex="auto">
        <a-menu
          v-model:selectedKeys="selectedKeys"
          mode="horizontal"
          :items="visibleMenuItems"
          @click="handleMenuClick"
        />
      </a-col>

      <!-- Right: Show avatar+dropdown if logged in, or Login button -->
      <a-col>
        <div class="user-login-status">
          <div v-if="loginUserStore.loginUser?.id">
            <a-dropdown>
              <a-space>
                <a-avatar :src="loginUserStore.loginUser.userAvatar" />
                {{ loginUserStore.loginUser.userName ?? 'Anonymous' }}
              </a-space>
              <template #overlay>
                <a-menu>
                  <a-menu-item @click="doLogout">
                    <LogoutOutlined />
                    Logout
                  </a-menu-item>
                </a-menu>
              </template>
            </a-dropdown>
          </div>
          <div v-else>
            <a-button type="primary" href="/user/login">Login</a-button>
          </div>
        </div>
      </a-col>
    </a-row>
  </a-layout-header>
</template>

<script setup lang="ts">
import { computed, h, ref } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import type { MenuProps } from 'ant-design-vue'
import { HomeOutlined, LogoutOutlined } from '@ant-design/icons-vue'
import { useLoginUserStore } from '@/stores/loginUser'
import { userLogout } from '@/api/userController'
import checkAccess from '@/access/checkAccess'
import ACCESS_ENUM from '@/access/accessEnum'

const router = useRouter()
const loginUserStore = useLoginUserStore()

const selectedKeys = ref<string[]>(['/'])
router.afterEach((to) => {
  selectedKeys.value = [to.path]
})

// All possible menu items — those with access require a role
const allMenuItems = [
  { key: '/', icon: () => h(HomeOutlined), label: 'Home', title: 'Home' },
  { key: '/about', label: 'About', title: 'About' },
  {
    key: '/admin/userManage',
    label: 'User Management',
    title: 'User Management',
    access: ACCESS_ENUM.ADMIN,
  },
  {
    key: '/admin/appManage',
    label: 'App Management',
    title: 'App Management',
    access: ACCESS_ENUM.ADMIN,
  },
]

// Only show items the current user is allowed to see
const visibleMenuItems = computed(() =>
  allMenuItems.filter((item) => {
    if (!item.access) return true
    return checkAccess(loginUserStore.loginUser, item.access)
  }),
)

const handleMenuClick: MenuProps['onClick'] = (e) => {
  const key = e.key as string
  selectedKeys.value = [key]
  if (key.startsWith('/')) {
    router.push(key)
  }
}

const doLogout = async () => {
  const res = await userLogout()
  if (res.data.code === 0) {
    loginUserStore.setLoginUser({ userName: 'Not logged in' })
    message.success('Logged out successfully')
    await router.push('/user/login')
  } else {
    message.error('Logout failed')
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
