declare namespace API {
  type ApiResponseString = {
    code?: number
    data?: string
    message?: string
  }

  type ApiResponseLong = {
    code?: number
    data?: string // Long is serialised as string to preserve Snowflake ID precision
    message?: string
  }

  type ApiResponseBoolean = {
    code?: number
    data?: boolean
    message?: string
  }

  type ApiResponseLoginUserVO = {
    code?: number
    data?: LoginUserVO
    message?: string
  }

  type ApiResponseUserVO = {
    code?: number
    data?: UserVO
    message?: string
  }

  type ApiResponsePageUserVO = {
    code?: number
    data?: PageUserVO
    message?: string
  }

  type PageUserVO = {
    records?: UserVO[]
    totalRow?: number
    pageNumber?: number
    pageSize?: number
  }

  /** Desensitised user info returned after login */
  type LoginUserVO = {
    id?: string
    userAccount?: string
    userName?: string
    userAvatar?: string
    userProfile?: string
    userRole?: string
    createTime?: string
    updateTime?: string
  }

  /** Desensitised user info for admin lists */
  type UserVO = {
    id?: string
    userAccount?: string
    userName?: string
    userAvatar?: string
    userProfile?: string
    userRole?: string
    createTime?: string
    updateTime?: string
  }

  type UserRegisterRequest = {
    userAccount?: string
    userPassword?: string
    checkPassword?: string
  }

  type UserLoginRequest = {
    userAccount?: string
    userPassword?: string
  }

  type UserAddRequest = {
    userAccount?: string
    userPassword?: string
    userName?: string
    userAvatar?: string
    userProfile?: string
    userRole?: string
  }

  type UserUpdateRequest = {
    id?: string
    userName?: string
    userAvatar?: string
    userProfile?: string
    userRole?: string
  }

  type UserQueryRequest = {
    id?: string
    userAccount?: string
    userName?: string
    userRole?: string
    page?: number
    size?: number
    sortBy?: string
    sortOrder?: string
  }

  type DeleteRequest = {
    id?: string
  }

  // ---- App types ----

  type AppVO = {
    id?: string
    appName?: string
    cover?: string
    initPrompt?: string
    codeGenType?: string
    deployKey?: string
    deployedTime?: string
    priority?: number
    userId?: string
    createTime?: string
    updateTime?: string
    user?: UserVO
  }

  type PageAppVO = {
    records?: AppVO[]
    totalRow?: number
    pageNumber?: number
    pageSize?: number
  }

  type ApiResponseAppVO = {
    code?: number
    data?: AppVO
    message?: string
  }

  type ApiResponsePageAppVO = {
    code?: number
    data?: PageAppVO
    message?: string
  }

  type AppAddRequest = {
    initPrompt?: string
  }

  type AppUpdateRequest = {
    id?: string
    appName?: string
  }

  type AppAdminUpdateRequest = {
    id?: string
    appName?: string
    cover?: string
    priority?: number
  }

  type AppQueryRequest = {
    id?: string
    appName?: string
    initPrompt?: string
    codeGenType?: string
    deployKey?: string
    priority?: number
    userId?: string
    page?: number
    size?: number
    sortBy?: string
    sortOrder?: string
  }
}
