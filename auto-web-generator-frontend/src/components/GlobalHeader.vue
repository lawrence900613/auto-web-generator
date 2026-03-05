<template>
  <a-layout-header class="header">
    <a-row :wrap="false">
      <!-- Left: Logo and title -->
      <a-col flex="260px">
        <RouterLink to="/">
          <div class="header-left">
            <img class="logo" src="@/assets/logo.svg" alt="Logo" />
            <h1 class="site-title">AI Web Generator</h1>
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
  {
    key: '/admin/chatManage',
    label: 'Chat Management',
    title: 'Chat Management',
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
  padding: 0 28px;
  height: 72px;
  line-height: 72px;
  box-shadow: 0 1px 12px rgba(160, 100, 200, 0.1);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.logo {
  height: 52px;
  width: 52px;
}

.site-title {
  margin: 0;
  font-size: 22px;
  font-weight: 800;
  white-space: nowrap;
  background: linear-gradient(135deg, #6a8fd8 0%, #9b6ed4 50%, #d4509a 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  letter-spacing: -0.3px;
}

.user-login-status {
  display: flex;
  align-items: center;
  height: 100%;
}

:deep(.ant-menu-horizontal) {
  border-bottom: none !important;
}

:deep(.ant-menu-item),
:deep(.ant-menu-submenu-title) {
  font-size: 16px !important;
  font-weight: 500;
}

:deep(.ant-avatar) {
  width: 36px !important;
  height: 36px !important;
  line-height: 36px !important;
}

:deep(.ant-space) {
  font-size: 16px;
}
</style>
