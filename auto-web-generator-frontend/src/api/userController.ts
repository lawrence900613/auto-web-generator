 
import request from '@/request'

/** Register a new account POST /user/register */
export async function userRegister(body: API.UserRegisterRequest) {
  return request<API.ApiResponseLong>('/user/register', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    data: body,
  })
}

/** Login POST /user/login */
export async function userLogin(body: API.UserLoginRequest) {
  return request<API.ApiResponseLoginUserVO>('/user/login', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    data: body,
  })
}

/** Logout POST /user/logout */
export async function userLogout() {
  return request<API.ApiResponseBoolean>('/user/logout', {
    method: 'POST',
  })
}

/** Get currently logged-in user GET /user/get/login */
export async function getLoginUser() {
  return request<API.ApiResponseLoginUserVO>('/user/get/login', {
    method: 'GET',
  })
}

/** Get a user's public VO by id GET /user/get/vo */
export async function getUserVoById(params: { id: string }) {
  return request<API.ApiResponseUserVO>('/user/get/vo', {
    method: 'GET',
    params,
  })
}

/** [Admin] Add a user POST /user/add */
export async function addUser(body: API.UserAddRequest) {
  return request<API.ApiResponseLong>('/user/add', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    data: body,
  })
}

/** [Admin] Delete a user by id POST /user/delete */
export async function deleteUser(body: API.DeleteRequest) {
  return request<API.ApiResponseBoolean>('/user/delete', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    data: body,
  })
}

/** [Admin] Update a user POST /user/update */
export async function updateUser(body: API.UserUpdateRequest) {
  return request<API.ApiResponseBoolean>('/user/update', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    data: body,
  })
}

/** [Admin] Paginated user VO list POST /user/list/page/vo */
export async function listUserVoByPage(body: API.UserQueryRequest) {
  return request<API.ApiResponsePageUserVO>('/user/list/page/vo', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    data: body,
  })
}

