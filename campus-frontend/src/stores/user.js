import { defineStore } from 'pinia'
import { login as loginApi, getUserInfo, logout as logoutApi } from '@/api/user'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('campus_token') || '',
    refreshToken: localStorage.getItem('campus_refresh_token') || '',
    userInfo: JSON.parse(localStorage.getItem('campus_user') || 'null')
  }),

  getters: {
    isLogin: (state) => !!state.token,
    isAdmin: (state) => state.userInfo?.role === 1,
    userId: (state) => state.userInfo?.id
  },

  actions: {
    // 登录
    async login(loginForm) {
      const data = await loginApi(loginForm)
      // 后端返回结构: { accessToken, refreshToken, expiresIn, user: {...} }
      this.token = data.accessToken
      this.refreshToken = data.refreshToken || ''
      this.userInfo = data.user
      localStorage.setItem('campus_token', data.accessToken)
      if (data.refreshToken) {
        localStorage.setItem('campus_refresh_token', data.refreshToken)
      }
      localStorage.setItem('campus_user', JSON.stringify(data.user))
      return data
    },

    // 拉取最新用户信息
    async fetchUserInfo() {
      if (!this.token) return null
      try {
        const data = await getUserInfo()
        this.userInfo = data
        localStorage.setItem('campus_user', JSON.stringify(data))
        return data
      } catch (e) {
        // token 失效，清除登录状态
        this.logout()
        throw e
      }
    },

    // 更新本地用户信息(编辑资料后)
    setUserInfo(info) {
      this.userInfo = { ...this.userInfo, ...info }
      localStorage.setItem('campus_user', JSON.stringify(this.userInfo))
    },

    // 退出登录
    async logout() {
      // 尝试调用后端退出接口（黑名单token），失败也不影响前端清除
      try {
        if (this.token) {
          await logoutApi()
        }
      } catch (e) {
        // 静默处理
      }
      this.token = ''
      this.refreshToken = ''
      this.userInfo = null
      localStorage.removeItem('campus_token')
      localStorage.removeItem('campus_refresh_token')
      localStorage.removeItem('campus_user')
    }
  }
})
