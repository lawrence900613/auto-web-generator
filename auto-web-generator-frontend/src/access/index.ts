import router from '@/router'
import { useLoginUserStore } from '@/stores/loginUser'
import ACCESS_ENUM from './accessEnum'
import checkAccess from './checkAccess'

/**
 * Global route guard.
 * - On first page load, fetches login user from backend.
 * - Redirects to /user/login if route requires auth.
 * - Redirects to /noAuth when role is insufficient.
 */
router.beforeEach(async (to, _from, next) => {
  // Temporary switch: disable all admin routes.
  if (to.path.startsWith('/admin')) {
    next('/noAuth')
    return
  }

  // Pinia must be initialized inside the callback (not at module level).
  const loginUserStore = useLoginUserStore()
  let loginUser = loginUserStore.loginUser
  const isAuthPage = to.path.startsWith('/user/login') || to.path.startsWith('/user/register')

  // Avoid blocking login/register routes when backend is slow/unreachable.
  if (!loginUser.userRole && !isAuthPage) {
    try {
      await loginUserStore.fetchLoginUser()
    } catch {
      // Backend may be unreachable; continue as unauthenticated.
    }
    loginUser = loginUserStore.loginUser
  }

  const needAccess = (to.meta?.access as string) ?? ACCESS_ENUM.NOT_LOGIN

  if (needAccess !== ACCESS_ENUM.NOT_LOGIN) {
    if (!loginUser.userRole || loginUser.userRole === ACCESS_ENUM.NOT_LOGIN) {
      next(`/user/login?redirect=${to.fullPath}`)
      return
    }
    if (!checkAccess(loginUser, needAccess)) {
      next('/noAuth')
      return
    }
  }

  next()
})
