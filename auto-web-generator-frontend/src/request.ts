import axios from 'axios'
import { message } from 'ant-design-vue'

const myAxios = axios.create({
  baseURL: 'http://localhost:8123/api',
  timeout: 60000,
  withCredentials: true,
})

myAxios.interceptors.request.use(
  (config) => config,
  (error) => Promise.reject(error),
)

myAxios.interceptors.response.use(
  (response) => {
    const { data } = response as { data?: { code?: number } }
    if (data?.code === 40100) {
      const responseUrl = response.request?.responseURL ?? ''
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
  (error) => Promise.reject(error),
)

export default myAxios
