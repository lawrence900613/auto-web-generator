/* eslint-disable */
import request from '@/request'

/** Health check endpoint GET /health/ */
export async function healthCheck(options?: { [key: string]: any }) {
  return request<API.ApiResponseString>('/health/', {
    method: 'GET',
    ...(options || {}),
  })
}

