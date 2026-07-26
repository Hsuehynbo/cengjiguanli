import axios from 'axios'
import { message } from 'ant-design-vue'

const instance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL ?? '',
  timeout: 15000
})

instance.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

instance.interceptors.response.use(
  response => {
    return response.data
  },
  error => {
    if (error.response) {
      const status = error.response.status
      if (status === 401) {
        if (error.config?.url !== '/api/auth/login') {
          message.error('登录已过期，请重新登录')
          localStorage.removeItem('token')
          localStorage.removeItem('user')
          setTimeout(() => {
            window.location.href = '/#/login'
            window.location.reload()
          }, 500)
        }
        return Promise.reject(error)
      }
      if (status === 403) {
        const msg = error.response.data?.message || error.response.data?.error || '无权限执行此操作'
        message.error(msg)
        return Promise.reject(error)
      }
      const msg = error.response.data?.message || error.response.data?.error || '请求失败'
      message.error(msg)
    } else {
      message.error('网络异常，请检查后端服务是否启动')
    }
    return Promise.reject(error)
  }
)

export default instance

const API_BASE = import.meta.env.VITE_API_BASE_URL ?? ''

export function getFileUrl(path) {
  if (!path) return ''
  if (path.startsWith('http://') || path.startsWith('https://')) return path
  return API_BASE + path
}