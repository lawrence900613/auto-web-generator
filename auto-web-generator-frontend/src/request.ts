import axios from 'axios'
import { message } from 'ant-design-vue'

// Replace bare JSON integers ≥ 16 digits with quoted strings before JSON.parse
// so that JavaScript never loses precision on Snowflake IDs.
const safeParse = (raw: string) =>
  JSON.parse(raw.replace(/:(\s*)(\d{16,})([,}\]])/g, ':$1"$2"$3'))

const rawApiBase = (import.meta.env.VITE_API_BASE || '/api').trim()
const apiBase = /^https?:\/\//.test(rawApiBase)
  ? rawApiBase
  : rawApiBase.startsWith('/')
    ? rawApiBase
    : `/${rawApiBase}`

const myAxios = axios.create({
  baseURL: apiBase,
  timeout: 60000,
  withCredentials: true,
  transformResponse: [safeParse],
})

myAxios.interceptors.request.use(
  (config) => config,
  (error: unknown) => Promise.reject(error),
)

myAxios.interceptors.response.use(
  (response) => {
    const data = response.data as { code?: number } | undefined
    if (data?.code === 40100) {
      const responseUrl = (response.request as { responseURL?: string })?.responseURL ?? ''
      if (
        !responseUrl.includes('user/get/login') &&
        !window.location.pathname.includes('/user/login')
      ) {
        message.warning('Please log in first')
        window.location.href = `/user/login?redirect=${window.location.href}`
      }
    }
    return response
  },
  (error: unknown) => Promise.reject(error),
)

export default myAxios
