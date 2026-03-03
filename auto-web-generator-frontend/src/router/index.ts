import { createRouter, createWebHistory } from 'vue-router'
import HomePage from '../pages/HomePage.vue'
import ACCESS_ENUM from '@/access/accessEnum'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: HomePage,
    },
    {
      path: '/about',
      name: 'about',
      component: () => import('../pages/AboutPage.vue'),
    },
    {
      path: '/user/login',
      name: 'userLogin',
      component: () => import('../pages/user/UserLoginPage.vue'),
    },
    {
      path: '/user/register',
      name: 'userRegister',
      component: () => import('../pages/user/UserRegisterPage.vue'),
    },
    {
      path: '/app/chat/:id',
      name: 'appChat',
      component: () => import('../pages/app/AppChatPage.vue'),
      meta: { access: ACCESS_ENUM.USER },
    },
    {
      path: '/app/edit/:id',
      name: 'appEdit',
      component: () => import('../pages/app/AppEditPage.vue'),
      meta: { access: ACCESS_ENUM.USER },
    },
    {
      path: '/admin/userManage',
      name: 'adminUserManage',
      component: () => import('../pages/admin/UserManagePage.vue'),
      meta: { access: ACCESS_ENUM.ADMIN },
    },
    {
      path: '/admin/appManage',
      name: 'adminAppManage',
      component: () => import('../pages/admin/AppManagePage.vue'),
      meta: { access: ACCESS_ENUM.ADMIN },
    },
    {
      path: '/noAuth',
      name: 'noAuth',
      component: () => import('../pages/NoAuthPage.vue'),
    },
  ],
})

export default router
