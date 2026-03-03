import router from '@/router'
import { useLoginUserStore } from '@/stores/loginUser'
import ACCESS_ENUM from './accessEnum'
import checkAccess from './checkAccess'

/**
 * Global route guard.
 * - On the first page load, fetches the login user from the backend.
 * - Redirects to /user/login if the page requires authentication.
 * - Redirects to /noAuth if the user lacks the required role.
 */
router.beforeEach(async (to, _from, next) => {
  // Pinia must be initialised inside the callback (not at module level)
  const loginUserStore = useLoginUserStore()
  let loginUser = loginUserStore.loginUser

  // First visit — try to fetch login state from the backend
  if (!loginUser.userRole) {
    try {
      await loginUserStore.fetchLoginUser()
    } catch {
      // Backend may be unreachable; continue as unauthenticated
    }
    loginUser = loginUserStore.loginUser
  }

  const needAccess = (to.meta?.access as string) ?? ACCESS_ENUM.NOT_LOGIN

  if (needAccess !== ACCESS_ENUM.NOT_LOGIN) {
    // Not logged in → redirect to login
    if (!loginUser.userRole || loginUser.userRole === ACCESS_ENUM.NOT_LOGIN) {
      next(`/user/login?redirect=${to.fullPath}`)
      return
    }
    // Logged in but insufficient role → redirect to no-auth page
    if (!checkAccess(loginUser, needAccess)) {
      next('/noAuth')
      return
    }
  }

  next()
})
