 
import request from '@/request'

/** Get chat history for an app (cursor-based) GET /chat-history/app/{appId} */
export async function listAppChatHistory(params: {
  appId: string
  pageSize?: number
  lastCreateTime?: number
}) {
  const { appId, ...query } = params
  return request<API.ApiResponsePageChatHistory>(`/chat-history/app/${appId}`, {
    method: 'GET',
    params: query,
  })
}

/** Admin: list all chat history GET /chat-history/admin/list */
export async function listAllChatHistoryForAdmin(params: {
  page?: number
  pageSize?: number
  appId?: string
}) {
  return request<API.ApiResponsePageChatHistory>('/chat-history/admin/list', {
    method: 'GET',
    params,
  })
}

