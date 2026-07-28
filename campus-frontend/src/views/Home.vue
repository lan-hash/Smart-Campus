<template>
  <div class="home">
    <!-- Hero 区域 -->
    <section class="hero">
      <div class="hero-bg">
        <span class="hero-blob blob-a"></span>
        <span class="hero-blob blob-b"></span>
        <span class="hero-blob blob-c"></span>
      </div>
      <div class="container hero-content">
        <div class="hero-text">
          <span class="hero-badge">
            <el-icon><MagicStick /></el-icon>
            AI 驱动 · 智慧校园
          </span>
          <h1 class="hero-title">
            智能校园综合服务平台
          </h1>
          <p class="hero-subtitle">
            融合学习论坛、游戏社区、表白墙、二手市场、代课服务与 AI 助手，
            <br />基于 Spring AI Alibaba，让校园生活更智能、更便捷
          </p>

          <div class="hero-search">
            <el-input
              v-model="keyword"
              placeholder="搜索帖子、商品、用户..."
              size="large"
              clearable
              @keyup.enter="handleSearch"
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
              <template #append>
                <el-button type="primary" @click="handleSearch">搜索</el-button>
              </template>
            </el-input>
          </div>

          <div class="hero-actions">
            <template v-if="userStore.isLogin">
              <p class="welcome-text">
                <el-icon><Promotion /></el-icon>
                欢迎回来，{{ userStore.userInfo?.nickname || '同学' }}！
              </p>
            </template>
            <template v-else>
              <el-button type="primary" size="large" round class="cta-btn" @click="router.push('/register')">
                <el-icon><Right /></el-icon>
                立即开始
              </el-button>
              <el-button size="large" round plain class="cta-plain" @click="router.push('/login')">
                我已有账号
              </el-button>
            </template>
          </div>
        </div>
      </div>
    </section>

    <!-- 功能入口卡片 -->
    <section class="container section">
      <div class="section-header">
        <h2 class="section-title">功能导航</h2>
        <p class="section-desc">六大核心模块，覆盖校园生活方方面面</p>
      </div>

      <div class="feature-grid">
        <div
          class="feature-card"
          v-for="(item, idx) in features"
          :key="item.path"
          :style="{ animationDelay: `${idx * 0.08}s` }"
          @click="router.push(item.path)"
        >
          <div class="feature-icon-wrap" :style="{ background: item.gradient }">
            <el-icon><component :is="item.icon" /></el-icon>
          </div>
          <h3 class="feature-title">{{ item.title }}</h3>
          <p class="feature-desc">{{ item.desc }}</p>
          <span class="feature-arrow">
            进入 <el-icon><ArrowRight /></el-icon>
          </span>
        </div>
      </div>
    </section>

    <!-- 快捷统计区 -->
    <section class="container section">
      <div class="stats-card">
        <div class="stats-item" v-for="s in stats" :key="s.label">
          <div class="stats-icon" :style="{ background: s.gradient }">
            <el-icon><component :is="s.icon" /></el-icon>
          </div>
          <div class="stats-info">
            <strong>{{ s.value }}</strong>
            <span>{{ s.label }}</span>
          </div>
        </div>
      </div>
    </section>

    <!-- 系统公告 -->
    <section class="container section">
      <div class="section-header">
        <h2 class="section-title">
          <el-icon><Bell /></el-icon>
          系统公告
        </h2>
        <p class="section-desc">了解平台最新动态与活动</p>
      </div>

      <div class="notice-list" v-loading="noticeLoading">
        <template v-if="notices.length">
          <div
            class="notice-item"
            v-for="(n, idx) in notices"
            :key="n.id || idx"
          >
            <div class="notice-tag" :style="{ background: noticeColors[idx % noticeColors.length] }">
              公告
            </div>
            <div class="notice-body">
              <h4 class="notice-title">{{ n.title }}</h4>
              <p class="notice-content">{{ n.content }}</p>
              <span class="notice-time">
                <el-icon><Clock /></el-icon>
                {{ formatTime(n.createTime) }}
              </span>
            </div>
          </div>
        </template>
        <EmptyState
          v-else-if="!noticeLoading"
          icon="Bell"
          text="暂无系统公告"
          sub-text="平台暂未发布新公告"
        />
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getNotices } from '@/api/message'
import EmptyState from '@/components/EmptyState.vue'
import {
  Search, Right, ArrowRight, MagicStick, Promotion, Bell, Clock,
  Reading, Aim, ChatLineSquare, ShoppingCart, Document, User,
  TrendCharts, Goods, DataLine
} from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()

const keyword = ref('')
const notices = ref([])
const noticeLoading = ref(false)

const features = [
  {
    path: '/forum/study',
    title: '学习论坛',
    desc: '课程答疑、资料共享、学霸经验交流',
    icon: Reading,
    gradient: 'linear-gradient(135deg, #6366f1, #818cf8)'
  },
  {
    path: '/forum/game',
    title: '游戏社区',
    desc: '组队开黑、攻略分享、电竞赛事讨论',
    icon: Aim,
    gradient: 'linear-gradient(135deg, #8b5cf6, #a78bfa)'
  },
  {
    path: '/confession',
    title: '表白墙',
    desc: '勇敢表达心意，匿名告白、寻人启事',
    icon: ChatLineSquare,
    gradient: 'linear-gradient(135deg, #ec4899, #f472b6)'
  },
  {
    path: '/secondhand',
    title: '二手市场',
    desc: '闲置物品流转，教材、数码、生活好物',
    icon: ShoppingCart,
    gradient: 'linear-gradient(135deg, #10b981, #34d399)'
  },
  {
    path: '/course',
    title: '代课服务',
    desc: '便捷课业互助，发布与接取代课订单',
    icon: Document,
    gradient: 'linear-gradient(135deg, #f59e0b, #fbbf24)'
  },
  {
    path: '/ai',
    title: 'AI 助手',
    desc: '基于 AI 的智能问答，学习生活好帮手',
    icon: MagicStick,
    gradient: 'linear-gradient(135deg, #06b6d4, #22d3ee)'
  }
]

const stats = [
  { label: '在线用户', value: '2,486', icon: User, gradient: 'linear-gradient(135deg, #6366f1, #818cf8)' },
  { label: '今日新帖', value: '328', icon: TrendCharts, gradient: 'linear-gradient(135deg, #8b5cf6, #a78bfa)' },
  { label: '在售商品', value: '1,052', icon: Goods, gradient: 'linear-gradient(135deg, #10b981, #34d399)' },
  { label: '累计互动', value: '50w+', icon: DataLine, gradient: 'linear-gradient(135deg, #06b6d4, #22d3ee)' }
]

const noticeColors = [
  'linear-gradient(135deg, #6366f1, #8b5cf6)',
  'linear-gradient(135deg, #ec4899, #f472b6)',
  'linear-gradient(135deg, #f59e0b, #fbbf24)',
  'linear-gradient(135deg, #10b981, #34d399)'
]

const handleSearch = () => {
  if (keyword.value.trim()) {
    router.push({ path: '/forum/study', query: { keyword: keyword.value } })
  }
}

const formatTime = (t) => {
  if (!t) return ''
  return String(t).replace('T', ' ').slice(0, 16)
}

const fetchNotices = async () => {
  noticeLoading.value = true
  try {
    const data = await getNotices()
    const list = data?.records || data || []
    notices.value = Array.isArray(list) ? list.slice(0, 4) : []
  } catch (e) {
    notices.value = []
  } finally {
    noticeLoading.value = false
  }
}

onMounted(() => {
  fetchNotices()
})
</script>

<style scoped>
.home {
  padding-bottom: 40px;
}

/* Hero 区域 */
.hero {
  position: relative;
  padding: 70px 0 80px;
  overflow: hidden;
  background: var(--gradient-hero);
}

.hero-bg {
  position: absolute;
  inset: 0;
  z-index: 0;
}

.hero-blob {
  position: absolute;
  border-radius: 50%;
  filter: blur(90px);
  opacity: 0.55;
  animation: float 9s ease-in-out infinite;
}

.blob-a {
  width: 460px;
  height: 460px;
  background: #6366f1;
  top: -160px;
  right: -80px;
}

.blob-b {
  width: 380px;
  height: 380px;
  background: #8b5cf6;
  bottom: -160px;
  left: -60px;
  animation-delay: 3s;
}

.blob-c {
  width: 260px;
  height: 260px;
  background: #06b6d4;
  top: 30%;
  left: 45%;
  animation-delay: 5s;
  opacity: 0.35;
}

@keyframes float {
  0%, 100% { transform: translate(0, 0) scale(1); }
  50% { transform: translate(24px, -32px) scale(1.1); }
}

.hero-content {
  position: relative;
  z-index: 1;
  text-align: center;
  color: #fff;
}

.hero-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 6px 16px;
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.18);
  backdrop-filter: blur(8px);
  font-size: 13px;
  font-weight: 500;
  margin-bottom: 22px;
}

.hero-title {
  font-size: 44px;
  font-weight: 800;
  letter-spacing: 2px;
  margin-bottom: 18px;
  text-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
}

.hero-subtitle {
  font-size: 15px;
  line-height: 1.9;
  opacity: 0.92;
  margin-bottom: 34px;
}

.hero-search {
  max-width: 560px;
  margin: 0 auto 30px;
}

:deep(.hero-search .el-input__wrapper) {
  border-radius: 26px 0 0 26px;
  padding-left: 18px;
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.15);
}

:deep(.hero-search .el-input-group__append) {
  border-radius: 0 26px 26px 0;
  padding: 0;
  overflow: hidden;
}

:deep(.hero-search .el-input-group__append .el-button) {
  height: 100%;
  padding: 0 28px;
  border: none;
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  color: #fff;
  font-weight: 600;
}

.hero-actions {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16px;
  flex-wrap: wrap;
}

.welcome-text {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 10px 22px;
  border-radius: 26px;
  background: rgba(255, 255, 255, 0.18);
  backdrop-filter: blur(8px);
  font-size: 15px;
  font-weight: 500;
}

.cta-btn {
  padding: 0 32px !important;
  height: 46px;
  font-size: 15px;
  font-weight: 600;
  background: #fff !important;
  color: var(--primary) !important;
  border: none !important;
  box-shadow: 0 10px 24px rgba(0, 0, 0, 0.18);
}

.cta-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 14px 30px rgba(0, 0, 0, 0.24);
}

.cta-plain {
  padding: 0 28px !important;
  height: 46px;
  color: #fff !important;
  border-color: rgba(255, 255, 255, 0.6) !important;
  background: rgba(255, 255, 255, 0.1) !important;
}

.cta-plain:hover {
  background: rgba(255, 255, 255, 0.2) !important;
  border-color: #fff !important;
}

/* 通用 section */
.section {
  margin-top: 48px;
}

.section-header {
  text-align: center;
  margin-bottom: 32px;
}

.section-title {
  font-size: 26px;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 8px;
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.section-title .el-icon {
  color: var(--primary);
}

.section-desc {
  font-size: 14px;
  color: var(--text-secondary);
}

/* 功能卡片网格 */
.feature-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 22px;
}

.feature-card {
  background: var(--bg-card);
  border-radius: var(--radius-lg);
  padding: 30px 26px;
  box-shadow: var(--shadow-md);
  transition: all 0.35s ease;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  opacity: 0;
  animation: fadeInUp 0.5s ease forwards;
}

.feature-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 4px;
  background: var(--gradient-primary);
  transform: scaleX(0);
  transform-origin: left;
  transition: transform 0.35s ease;
}

.feature-card:hover {
  transform: translateY(-8px);
  box-shadow: var(--shadow-hover);
}

.feature-card:hover::before {
  transform: scaleX(1);
}

.feature-icon-wrap {
  width: 60px;
  height: 60px;
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 30px;
  color: #fff;
  margin-bottom: 18px;
  box-shadow: 0 8px 18px rgba(99, 102, 241, 0.2);
  transition: transform 0.35s ease;
}

.feature-card:hover .feature-icon-wrap {
  transform: scale(1.1) rotate(-6deg);
}

.feature-title {
  font-size: 19px;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 8px;
}

.feature-desc {
  font-size: 13px;
  line-height: 1.7;
  color: var(--text-secondary);
  margin-bottom: 16px;
}

.feature-arrow {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  font-weight: 600;
  color: var(--primary);
  transition: gap 0.25s;
}

.feature-card:hover .feature-arrow {
  gap: 8px;
}

/* 统计区 */
.stats-card {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 22px;
  background: var(--bg-card);
  border-radius: var(--radius-lg);
  padding: 32px;
  box-shadow: var(--shadow-md);
}

.stats-item {
  display: flex;
  align-items: center;
  gap: 16px;
}

.stats-icon {
  width: 54px;
  height: 54px;
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 26px;
  color: #fff;
  flex-shrink: 0;
  box-shadow: 0 6px 16px rgba(99, 102, 241, 0.18);
}

.stats-info strong {
  display: block;
  font-size: 26px;
  font-weight: 800;
  color: var(--text-primary);
  line-height: 1.2;
}

.stats-info span {
  font-size: 13px;
  color: var(--text-secondary);
}

/* 公告列表 */
.notice-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.notice-item {
  display: flex;
  gap: 18px;
  background: var(--bg-card);
  border-radius: var(--radius-md);
  padding: 22px 24px;
  box-shadow: var(--shadow-sm);
  transition: all 0.3s ease;
  border-left: 4px solid transparent;
}

.notice-item:hover {
  box-shadow: var(--shadow-hover);
  transform: translateX(4px);
  border-left-color: var(--primary);
}

.notice-tag {
  width: 56px;
  height: 28px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 12px;
  font-weight: 600;
  flex-shrink: 0;
  margin-top: 2px;
}

.notice-body {
  flex: 1;
  min-width: 0;
}

.notice-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 6px;
}

.notice-content {
  font-size: 13px;
  line-height: 1.7;
  color: var(--text-regular);
  margin-bottom: 8px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.notice-time {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: var(--text-secondary);
}

@media (max-width: 992px) {
  .feature-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  .stats-card {
    grid-template-columns: repeat(2, 1fr);
  }
  .hero-title {
    font-size: 32px;
  }
}

@media (max-width: 576px) {
  .feature-grid {
    grid-template-columns: 1fr;
  }
  .stats-card {
    grid-template-columns: 1fr;
  }
}
</style>
