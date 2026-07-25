<template>
  <div class="auth-page">
    <!-- 背景装饰 -->
    <div class="bg-decoration">
      <span class="blob blob-1"></span>
      <span class="blob blob-2"></span>
      <span class="blob blob-3"></span>
    </div>

    <div class="auth-card">
      <!-- 左侧品牌区 -->
      <div class="brand-side">
        <div class="brand-content">
          <div class="brand-logo">
            <el-icon><School /></el-icon>
          </div>
          <h1 class="brand-name">智校园</h1>
          <p class="brand-slogan">基于 Spring AI Alibaba 的<br />智能校园综合服务平台</p>

          <ul class="feature-list">
            <li class="feature-item" v-for="f in features" :key="f.text">
              <div class="feature-icon">
                <el-icon><component :is="f.icon" /></el-icon>
              </div>
              <div class="feature-text">
                <h4>{{ f.text }}</h4>
                <p>{{ f.desc }}</p>
              </div>
            </li>
          </ul>
        </div>
      </div>

      <!-- 右侧表单区 -->
      <div class="form-side">
        <div class="form-header">
          <h2>欢迎回来</h2>
          <p>登录账号，开启智能校园生活</p>
        </div>

        <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          size="large"
          @submit.prevent="handleLogin"
        >
          <el-form-item prop="username">
            <el-input
              v-model="form.username"
              placeholder="请输入用户名"
              :prefix-icon="User"
              clearable
            />
          </el-form-item>

          <el-form-item prop="password">
            <el-input
              v-model="form.password"
              type="password"
              placeholder="请输入密码"
              :prefix-icon="Lock"
              show-password
              @keyup.enter="handleLogin"
            />
          </el-form-item>

          <div class="form-options">
            <el-checkbox v-model="form.remember">记住我</el-checkbox>
            <el-link type="primary" :underline="false" @click="forgotPassword">
              忘记密码？
            </el-link>
          </div>

          <el-button
            type="primary"
            class="submit-btn"
            :loading="loading"
            @click="handleLogin"
          >
            登 录
          </el-button>
        </el-form>

        <div class="form-footer">
          没有账号？
          <router-link to="/register" class="link-register">去注册</router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock, School, Reading, Aim, ChatLineSquare, ShoppingCart, Document, MagicStick } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const formRef = ref()
const loading = ref(false)

const form = reactive({
  username: '',
  password: '',
  remember: false
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const features = [
  { icon: Reading, text: '学习论坛', desc: '知识分享互助' },
  { icon: Aim, text: '游戏社区', desc: '组队开黑交流' },
  { icon: ChatLineSquare, text: '表白墙', desc: '勇敢表达心意' },
  { icon: ShoppingCart, text: '二手市场', desc: '闲置物品流转' },
  { icon: Document, text: '代课服务', desc: '便捷课业互助' },
  { icon: MagicStick, text: 'AI 助手', desc: '智能问答陪伴' }
]

const handleLogin = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    loading.value = true
    try {
      await userStore.login({
        username: form.username,
        password: form.password
      })
      ElMessage.success('登录成功，欢迎回来！')
      const redirect = route.query.redirect || '/'
      router.push(redirect)
    } catch (e) {
      // 错误已在拦截器处理
    } finally {
      loading.value = false
    }
  })
}

const forgotPassword = () => {
  ElMessage.info('请联系管理员重置密码')
}
</script>

<style scoped>
.auth-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
  position: relative;
  overflow: hidden;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

/* 背景装饰 */
.bg-decoration {
  position: absolute;
  inset: 0;
  overflow: hidden;
  z-index: 0;
}

.blob {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  opacity: 0.5;
  animation: float 8s ease-in-out infinite;
}

.blob-1 {
  width: 420px;
  height: 420px;
  background: #6366f1;
  top: -120px;
  left: -100px;
}

.blob-2 {
  width: 360px;
  height: 360px;
  background: #8b5cf6;
  bottom: -120px;
  right: -80px;
  animation-delay: 2s;
}

.blob-3 {
  width: 280px;
  height: 280px;
  background: #06b6d4;
  top: 40%;
  left: 50%;
  animation-delay: 4s;
}

@keyframes float {
  0%, 100% { transform: translate(0, 0) scale(1); }
  50% { transform: translate(20px, -30px) scale(1.08); }
}

.auth-card {
  position: relative;
  z-index: 1;
  width: 920px;
  max-width: 100%;
  min-height: 560px;
  display: grid;
  grid-template-columns: 1fr 1fr;
  background: rgba(255, 255, 255, 0.16);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.3);
  border-radius: var(--radius-xl);
  box-shadow: 0 24px 64px rgba(31, 38, 135, 0.3);
  overflow: hidden;
}

/* 左侧品牌区 */
.brand-side {
  background: var(--gradient-primary);
  padding: 48px 40px;
  display: flex;
  align-items: center;
  position: relative;
  overflow: hidden;
}

.brand-side::before {
  content: '';
  position: absolute;
  inset: 0;
  background: radial-gradient(circle at 80% 20%, rgba(255, 255, 255, 0.18), transparent 50%);
}

.brand-content {
  position: relative;
  z-index: 1;
  color: #fff;
}

.brand-logo {
  width: 56px;
  height: 56px;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.22);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 30px;
  margin-bottom: 18px;
  backdrop-filter: blur(8px);
}

.brand-name {
  font-size: 34px;
  font-weight: 800;
  letter-spacing: 2px;
  margin-bottom: 10px;
}

.brand-slogan {
  font-size: 14px;
  line-height: 1.7;
  opacity: 0.9;
  margin-bottom: 32px;
}

.feature-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 14px;
}

.feature-icon {
  width: 40px;
  height: 40px;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.2);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  flex-shrink: 0;
  transition: all 0.3s;
}

.feature-item:hover .feature-icon {
  background: rgba(255, 255, 255, 0.35);
  transform: scale(1.1);
}

.feature-text h4 {
  font-size: 15px;
  font-weight: 600;
  margin-bottom: 2px;
}

.feature-text p {
  font-size: 12px;
  opacity: 0.85;
}

/* 右侧表单区 */
.form-side {
  background: rgba(255, 255, 255, 0.95);
  padding: 48px 44px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.form-header {
  margin-bottom: 32px;
}

.form-header h2 {
  font-size: 26px;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 8px;
}

.form-header p {
  font-size: 14px;
  color: var(--text-secondary);
}

.form-options {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 22px;
}

.submit-btn {
  width: 100%;
  height: 46px;
  font-size: 16px;
  font-weight: 600;
  letter-spacing: 4px;
  background: var(--gradient-primary);
  border: none;
  box-shadow: 0 8px 20px rgba(99, 102, 241, 0.3);
  transition: all 0.3s;
}

.submit-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 12px 28px rgba(99, 102, 241, 0.4);
}

.form-footer {
  text-align: center;
  margin-top: 28px;
  font-size: 14px;
  color: var(--text-secondary);
}

.link-register {
  color: var(--primary);
  font-weight: 600;
  transition: opacity 0.2s;
}

.link-register:hover {
  opacity: 0.8;
}

@media (max-width: 768px) {
  .auth-card {
    grid-template-columns: 1fr;
    width: 100%;
  }
  .brand-side {
    display: none;
  }
}
</style>
