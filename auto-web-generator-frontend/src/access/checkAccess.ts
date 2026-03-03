import ACCESS_ENUM from './accessEnum'

/**
 * Check whether a login user has sufficient access.
 * @param loginUser  - the currently logged-in user (from Pinia store)
 * @param needAccess - the minimum access level required (defaults to NOT_LOGIN)
 * @returns true if the user may proceed, false otherwise
 */
const checkAccess = (loginUser: API.LoginUserVO, needAccess = ACCESS_ENUM.NOT_LOGIN): boolean => {
  const loginUserAccess = loginUser?.userRole ?? ACCESS_ENUM.NOT_LOGIN

  // Page requires no login — always allow
  if (needAccess === ACCESS_ENUM.NOT_LOGIN) {
    return true
  }

  // Page requires a regular user — deny if not logged in
  if (needAccess === ACCESS_ENUM.USER) {
    return loginUserAccess !== ACCESS_ENUM.NOT_LOGIN
  }

  // Page requires admin — deny if not admin
  if (needAccess === ACCESS_ENUM.ADMIN) {
    return loginUserAccess === ACCESS_ENUM.ADMIN
  }

  return true
}

export default checkAccess
