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

          <div class="benefits">
            <div class="benefit-card" v-for="b in benefits" :key="b.title">
              <el-icon><component :is="b.icon" /></el-icon>
              <div>
                <h4>{{ b.title }}</h4>
                <p>{{ b.desc }}</p>
              </div>
            </div>
          </div>

          <div class="brand-stats">
            <div class="stat">
              <strong>10w+</strong>
              <span>校园用户</span>
            </div>
            <div class="stat">
              <strong>50w+</strong>
              <span>互动内容</span>
            </div>
            <div class="stat">
              <strong>AI</strong>
              <span>智能驱动</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧表单区 -->
      <div class="form-side">
        <div class="form-header">
          <h2>创建账号</h2>
          <p>加入智校园，开启全新校园体验</p>
        </div>

        <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          size="large"
          label-position="top"
          @submit.prevent="handleRegister"
        >
          <el-form-item prop="username" label="用户名">
            <el-input
              v-model="form.username"
              placeholder="3-20位字符"
              :prefix-icon="User"
              clearable
            />
          </el-form-item>

          <el-form-item prop="nickname" label="昵称">
            <el-input
              v-model="form.nickname"
              placeholder="给自己取个昵称"
              :prefix-icon="UserFilled"
              clearable
            />
          </el-form-item>

          <el-form-item prop="password" label="密码">
            <el-input
              v-model="form.password"
              type="password"
              placeholder="6-20位字符"
              :prefix-icon="Lock"
              show-password
            />
          </el-form-item>

          <el-form-item prop="confirmPassword" label="确认密码">
            <el-input
              v-model="form.confirmPassword"
              type="password"
              placeholder="请再次输入密码"
              :prefix-icon="Lock"
              show-password
              @keyup.enter="handleRegister"
            />
          </el-form-item>

          <el-form-item prop="campus" label="所在学校（可选）">
            <el-input
              v-model="form.campus"
              placeholder="如：清华大学"
              :prefix-icon="School"
              clearable
            />
          </el-form-item>

          <el-button
            type="primary"
            class="submit-btn"
            :loading="loading"
            @click="handleRegister"
          >
            注 册
          </el-button>
        </el-form>

        <div class="form-footer">
          已有账号？
          <router-link to="/login" class="link-login">去登录</router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, UserFilled, Lock, School, ChatDotRound, Connection, TrendCharts } from '@element-plus/icons-vue'
import { register } from '@/api/user'

const router = useRouter()

const formRef = ref()
const loading = ref(false)

const form = reactive({
  username: '',
  nickname: '',
  password: '',
  confirmPassword: '',
  campus: ''
})

const validatePass = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请再次输入密码'))
  } else if (value !== form.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度为 3-20 位字符', trigger: 'blur' }
  ],
  nickname: [
    { required: true, message: '请输入昵称', trigger: 'blur' },
    { max: 20, message: '昵称不能超过 20 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度为 6-20 位字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, validator: validatePass, trigger: 'blur' }
  ]
}

const benefits = [
  { icon: ChatDotRound, title: '校园社交', desc: '结识同学，畅快交流' },
  { icon: Connection, title: '互帮互助', desc: '学习生活一站搞定' },
  { icon: TrendCharts, title: '智能推荐', desc: 'AI 精准匹配需求' }
]

const handleRegister = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    loading.value = true
    try {
      await register({
        username: form.username,
        password: form.password,
        nickname: form.nickname,
        campus: form.campus
      })
      ElMessage.success('注册成功，请登录')
      router.push('/login')
    } catch (e) {
      // 错误已在拦截器处理
    } finally {
      loading.value = false
    }
  })
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
  width: 960px;
  max-width: 100%;
  min-height: 600px;
  display: grid;
  grid-template-columns: 1fr 1.05fr;
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
  padding: 44px 36px;
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
  width: 100%;
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
  margin-bottom: 28px;
}

.benefits {
  display: flex;
  flex-direction: column;
  gap: 14px;
  margin-bottom: 28px;
}

.benefit-card {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 14px;
  border-radius: var(--radius-md);
  background: rgba(255, 255, 255, 0.12);
  backdrop-filter: blur(6px);
  transition: all 0.3s;
}

.benefit-card:hover {
  background: rgba(255, 255, 255, 0.22);
  transform: translateX(4px);
}

.benefit-card .el-icon {
  font-size: 22px;
  flex-shrink: 0;
}

.benefit-card h4 {
  font-size: 15px;
  font-weight: 600;
  margin-bottom: 2px;
}

.benefit-card p {
  font-size: 12px;
  opacity: 0.85;
}

.brand-stats {
  display: flex;
  gap: 20px;
  padding-top: 22px;
  border-top: 1px solid rgba(255, 255, 255, 0.2);
}

.stat strong {
  display: block;
  font-size: 22px;
  font-weight: 800;
  margin-bottom: 2px;
}

.stat span {
  font-size: 12px;
  opacity: 0.85;
}

/* 右侧表单区 */
.form-side {
  background: rgba(255, 255, 255, 0.95);
  padding: 40px 44px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.form-header {
  margin-bottom: 24px;
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

:deep(.el-form-item) {
  margin-bottom: 18px;
}

:deep(.el-form-item__label) {
  padding-bottom: 4px;
  font-size: 13px;
  color: var(--text-regular);
}

.submit-btn {
  width: 100%;
  height: 46px;
  margin-top: 6px;
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
  margin-top: 22px;
  font-size: 14px;
  color: var(--text-secondary);
}

.link-login {
  color: var(--primary);
  font-weight: 600;
  transition: opacity 0.2s;
}

.link-login:hover {
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
