<template>
  <div class="not-found">
    <!-- 背景装饰 -->
    <div class="nf-bg">
      <span class="nf-blob nb1"></span>
      <span class="nf-blob nb2"></span>
      <span class="nf-blob nb3"></span>
    </div>

    <div class="nf-content">
      <div class="nf-code">
        <span class="digit">4</span>
        <span class="digit zero">
          <el-icon><Search /></el-icon>
        </span>
        <span class="digit">4</span>
      </div>

      <h2 class="nf-title">页面走丢了</h2>
      <p class="nf-desc">
        抱歉，您访问的页面不存在或已被移除
        <br />请检查地址是否正确，或返回首页继续探索
      </p>

      <div class="nf-actions">
        <el-button type="primary" size="large" round @click="goHome">
          <el-icon><HomeFilled /></el-icon>返回首页
        </el-button>
        <el-button size="large" round plain @click="goBack">
          <el-icon><ArrowLeft /></el-icon>返回上一页
        </el-button>
      </div>

      <div class="nf-suggest">
        <p>您还可以试试：</p>
        <div class="suggest-links">
          <router-link to="/forum/study" class="suggest-link">
            <el-icon><Reading /></el-icon>学习论坛
          </router-link>
          <router-link to="/secondhand" class="suggest-link">
            <el-icon><ShoppingCart /></el-icon>二手市场
          </router-link>
          <router-link to="/ai" class="suggest-link">
            <el-icon><MagicStick /></el-icon>AI 助手
          </router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import {
  Search, HomeFilled, ArrowLeft, Reading, ShoppingCart, MagicStick
} from '@element-plus/icons-vue'

const router = useRouter()

const goHome = () => router.push('/')
const goBack = () => {
  if (window.history.length > 1) {
    router.back()
  } else {
    router.push('/')
  }
}
</script>

<style scoped>
.not-found {
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
.nf-bg {
  position: absolute;
  inset: 0;
  z-index: 0;
}

.nf-blob {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  opacity: 0.5;
  animation: float 9s ease-in-out infinite;
}

.nb1 {
  width: 420px;
  height: 420px;
  background: #6366f1;
  top: -140px;
  left: -100px;
}

.nb2 {
  width: 360px;
  height: 360px;
  background: #8b5cf6;
  bottom: -120px;
  right: -80px;
  animation-delay: 3s;
}

.nb3 {
  width: 260px;
  height: 260px;
  background: #06b6d4;
  top: 50%;
  left: 60%;
  animation-delay: 5s;
  opacity: 0.35;
}

@keyframes float {
  0%, 100% { transform: translate(0, 0) scale(1); }
  50% { transform: translate(20px, -30px) scale(1.08); }
}

.nf-content {
  position: relative;
  z-index: 1;
  text-align: center;
  color: #fff;
}

/* 404 数字 */
.nf-code {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  margin-bottom: 20px;
}

.digit {
  font-size: 160px;
  font-weight: 900;
  line-height: 1;
  background: linear-gradient(180deg, #fff 0%, rgba(255, 255, 255, 0.7) 100%);
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
  text-shadow: 0 12px 40px rgba(0, 0, 0, 0.15);
  animation: bounceIn 0.8s ease;
}

.digit.zero {
  width: 130px;
  height: 130px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(10px);
  border: 4px solid rgba(255, 255, 255, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  -webkit-text-fill-color: initial;
  font-size: 60px;
  animation: spin 4s ease-in-out infinite, bounceIn 0.8s ease;
}

.digit.zero .el-icon {
  color: #fff;
  font-size: 64px;
}

@keyframes bounceIn {
  0% { opacity: 0; transform: scale(0.3) translateY(-40px); }
  60% { opacity: 1; transform: scale(1.1) translateY(0); }
  100% { transform: scale(1); }
}

@keyframes spin {
  0%, 100% { transform: rotate(0deg); }
  50% { transform: rotate(20deg); }
}

.nf-title {
  font-size: 32px;
  font-weight: 700;
  margin-bottom: 14px;
  text-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
}

.nf-desc {
  font-size: 15px;
  line-height: 1.9;
  opacity: 0.9;
  margin-bottom: 34px;
}

.nf-actions {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16px;
  margin-bottom: 44px;
  flex-wrap: wrap;
}

.nf-actions .el-button--primary {
  padding: 0 30px;
  background: #fff !important;
  color: var(--primary) !important;
  border: none !important;
  font-weight: 600;
  box-shadow: 0 10px 24px rgba(0, 0, 0, 0.2);
}

.nf-actions .el-button--primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 14px 30px rgba(0, 0, 0, 0.28);
}

.nf-actions .el-button.is-round.is-plain {
  padding: 0 26px;
  color: #fff !important;
  border-color: rgba(255, 255, 255, 0.6) !important;
  background: rgba(255, 255, 255, 0.1) !important;
}

.nf-actions .el-button.is-round.is-plain:hover {
  background: rgba(255, 255, 255, 0.2) !important;
  border-color: #fff !important;
}

/* 推荐链接 */
.nf-suggest {
  padding: 22px 32px;
  border-radius: var(--radius-lg);
  background: rgba(255, 255, 255, 0.12);
  backdrop-filter: blur(10px);
  display: inline-block;
}

.nf-suggest > p {
  font-size: 13px;
  opacity: 0.85;
  margin-bottom: 14px;
}

.suggest-links {
  display: flex;
  align-items: center;
  gap: 18px;
  flex-wrap: wrap;
  justify-content: center;
}

.suggest-link {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 18px;
  border-radius: 22px;
  background: rgba(255, 255, 255, 0.18);
  color: #fff;
  font-size: 13px;
  font-weight: 500;
  transition: all 0.3s;
}

.suggest-link:hover {
  background: rgba(255, 255, 255, 0.32);
  transform: translateY(-3px);
}

@media (max-width: 576px) {
  .digit {
    font-size: 110px;
  }
  .digit.zero {
    width: 90px;
    height: 90px;
    font-size: 44px;
  }
  .digit.zero .el-icon {
    font-size: 44px;
  }
  .nf-title {
    font-size: 26px;
  }
}
</style>
