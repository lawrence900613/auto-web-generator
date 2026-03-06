 
import request from '@/request'

/** Create an app POST /app/add */
export async function addApp(body: API.AppAddRequest) {
  return request<API.ApiResponseString>('/app/add', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    data: body,
  })
}

/** Update own app (name only) POST /app/update */
export async function updateApp(body: API.AppUpdateRequest) {
  return request<API.ApiResponseBoolean>('/app/update', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    data: body,
  })
}

/** Delete own app POST /app/delete */
export async function deleteApp(body: API.DeleteRequest) {
  return request<API.ApiResponseBoolean>('/app/delete', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    data: body,
  })
}

/** Get app VO by id GET /app/get/vo */
export async function getAppVoById(params: { id: string }) {
  return request<API.ApiResponseAppVO>('/app/get/vo', {
    method: 'GET',
    params,
  })
}

/** Current user's apps POST /app/my/list/page/vo */
export async function listMyAppVoByPage(body: API.AppQueryRequest) {
  return request<API.ApiResponsePageAppVO>('/app/my/list/page/vo', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    data: body,
  })
}

/** Featured apps POST /app/good/list/page/vo */
export async function listGoodAppVoByPage(body: API.AppQueryRequest) {
  return request<API.ApiResponsePageAppVO>('/app/good/list/page/vo', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    data: body,
  })
}

/** Deploy an app POST /app/deploy */
export async function deployApp(params: { appId: string }) {
  return request<API.ApiResponseString>('/app/deploy', {
    method: 'POST',
    params,
  })
}

/** [Admin] Delete any app POST /app/admin/delete */
export async function deleteAppByAdmin(body: API.DeleteRequest) {
  return request<API.ApiResponseBoolean>('/app/admin/delete', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    data: body,
  })
}

/** [Admin] Update any app POST /app/admin/update */
export async function updateAppByAdmin(body: API.AppAdminUpdateRequest) {
  return request<API.ApiResponseBoolean>('/app/admin/update', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    data: body,
  })
}

/** [Admin] Get app VO GET /app/admin/get/vo */
export async function getAppVoByIdByAdmin(params: { id: string }) {
  return request<API.ApiResponseAppVO>('/app/admin/get/vo', {
    method: 'GET',
    params,
  })
}

/** [Admin] Paginated app list POST /app/admin/list/page/vo */
export async function listAppVoByPageByAdmin(body: API.AppQueryRequest) {
  return request<API.ApiResponsePageAppVO>('/app/admin/list/page/vo', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    data: body,
  })
}

