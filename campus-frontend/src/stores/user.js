import { defineStore } from 'pinia'
import { login as loginApi, getUserInfo } from '@/api/user'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('campus_token') || '',
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
      this.token = data.token
      localStorage.setItem('campus_token', data.token)
      this.userInfo = data
      localStorage.setItem('campus_user', JSON.stringify(data))
      return data
    },

    // 拉取最新用户信息
    async fetchUserInfo() {
      if (!this.token) return null
      const data = await getUserInfo()
      this.userInfo = data
      localStorage.setItem('campus_user', JSON.stringify(data))
      return data
    },

    // 更新本地用户信息(编辑资料后)
    setUserInfo(info) {
      this.userInfo = { ...this.userInfo, ...info }
      localStorage.setItem('campus_user', JSON.stringify(this.userInfo))
    },

    // 退出登录
    logout() {
      this.token = ''
      this.userInfo = null
      localStorage.removeItem('campus_token')
      localStorage.removeItem('campus_user')
    }
  }
})
