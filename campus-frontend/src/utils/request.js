import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'
import router from '@/router'

const service = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 15000
})

// 防止重复弹出相同错误提示
let lastErrorMessage = ''
let lastErrorTime = 0
const showError = (msg) => {
  const now = Date.now()
  if (msg === lastErrorMessage && now - lastErrorTime < 3000) return
  lastErrorMessage = msg
  lastErrorTime = now
  ElMessage.error(msg)
}

// 请求拦截器：附带 token
service.interceptors.request.use(
  (config) => {
    const userStore = useUserStore()
    if (userStore.token) {
      config.headers['Authorization'] = `Bearer ${userStore.token}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

// 响应拦截器：统一处理业务码
service.interceptors.response.use(
  (response) => {
    const res = response.data
    // 文件流直接返回
    if (response.config.responseType === 'blob') {
      return response
    }
    if (res.code === 200 || res.code === undefined) {
      return res.data !== undefined ? res.data : res
    }
    // 401 未登录 / token 失效
    if (res.code === 401) {
      ElMessageBox.confirm('登录状态已失效，请重新登录', '提示', {
        confirmButtonText: '重新登录',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        const userStore = useUserStore()
        userStore.logout()
        router.push('/login')
      })
      return Promise.reject(new Error(res.message || '未登录'))
    }
    showError(res.message || '请求失败')
    return Promise.reject(new Error(res.message || 'Error'))
  },
  (error) => {
    const status = error.response?.status
    if (status === 401) {
      const userStore = useUserStore()
      userStore.logout()
      router.push('/login')
    } else if (status === 403) {
      showError('没有权限访问该资源')
    } else if (status >= 500) {
      showError('服务器开小差了，请稍后重试')
    } else if (error.code === 'ECONNREFUSED' || !error.response) {
      showError('服务器未启动，请检查后端服务')
    } else {
      showError(error.response?.data?.message || error.message || '网络异常')
    }
    return Promise.reject(error)
  }
)

export default service
