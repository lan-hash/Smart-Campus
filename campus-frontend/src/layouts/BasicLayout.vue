<template>
  <div class="layout">
    <!-- 顶部导航 -->
    <header class="navbar">
      <div class="navbar-inner container">
        <!-- Logo -->
        <div class="logo" @click="router.push('/')">
          <div class="logo-icon">
            <el-icon><School /></el-icon>
          </div>
          <span class="logo-text">智校园</span>
        </div>

        <!-- 导航菜单 -->
        <nav class="nav-menu">
          <router-link
            v-for="item in navItems"
            :key="item.path"
            :to="item.path"
            class="nav-item"
            :class="{ active: isActive(item) }"
          >
            <el-icon><component :is="item.icon" /></el-icon>
            <span>{{ item.label }}</span>
          </router-link>
        </nav>

        <!-- 右侧操作区 -->
        <div class="nav-actions">
          <!-- 搜索 -->
          <div class="search-box">
            <el-input
              v-model="keyword"
              placeholder="搜索帖子、商品..."
              :prefix-icon="Search"
              clearable
              @keyup.enter="handleSearch"
            />
          </div>

          <!-- 通知 -->
          <el-badge :value="unreadCount" :hidden="unreadCount === 0" :max="99">
            <el-button circle text @click="router.push('/message')">
              <el-icon :size="20"><Bell /></el-icon>
            </el-button>
          </el-badge>

          <!-- 用户菜单 -->
          <el-dropdown v-if="userStore.isLogin" trigger="click" @command="handleCommand">
            <div class="user-trigger">
              <el-avatar :size="34" :src="userStore.userInfo?.avatar">
                {{ userStore.userInfo?.nickname?.charAt(0) }}
              </el-avatar>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">
                  <el-icon><User /></el-icon> 个人主页
                </el-dropdown-item>
                <el-dropdown-item command="edit">
                  <el-icon><Edit /></el-icon> 编辑资料
                </el-dropdown-item>
                <el-dropdown-item command="message">
                  <el-icon><ChatDotRound /></el-icon> 我的消息
                </el-dropdown-item>
                <el-dropdown-item v-if="userStore.isAdmin" command="admin" divided>
                  <el-icon><Setting /></el-icon> 后台管理
                </el-dropdown-item>
                <el-dropdown-item command="logout" divided>
                  <el-icon><SwitchButton /></el-icon> 退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>

          <!-- 未登录 -->
          <template v-else>
            <el-button text @click="router.push('/login')">登录</el-button>
            <el-button type="primary" @click="router.push('/register')">注册</el-button>
          </template>
        </div>
      </div>
    </header>

    <!-- 主体内容 -->
    <main class="main-content">
      <router-view v-slot="{ Component }">
        <transition name="fade" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </main>

    <!-- 悬浮 AI 助手按钮 -->
    <div class="ai-float" @click="router.push('/ai')" title="AI智能助手">
      <el-icon :size="26"><MagicStick /></el-icon>
      <span class="ai-float-label">AI</span>
    </div>

    <!-- 页脚 -->
    <footer class="footer">
      <div class="container">
        <p>智能校园综合服务平台 · 基于Spring AI Alibaba</p>
        <p class="footer-sub">让校园生活更智能、更便捷</p>
      </div>
    </footer>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getUnreadCount } from '@/api/message'
import { ElMessageBox, ElMessage } from 'element-plus'
import {
  Search, Bell, User, Edit, ChatDotRound, Setting, SwitchButton,
  School, MagicStick, HomeFilled, Reading, Aim,
  ShoppingCart, Document, ChatLineSquare
} from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const keyword = ref('')
const unreadCount = ref(0)

const navItems = [
  { path: '/', label: '首页', icon: HomeFilled },
  { path: '/forum/study', label: '学习论坛', icon: Reading },
  { path: '/forum/game', label: '游戏社区', icon: Aim },
  { path: '/confession', label: '表白墙', icon: ChatLineSquare },
  { path: '/secondhand', label: '二手市场', icon: ShoppingCart },
  { path: '/course', label: '代课服务', icon: Document }
]

const isActive = (item) => {
  if (item.path === '/') return route.path === '/'
  return route.path.startsWith(item.path)
}

const handleSearch = () => {
  if (keyword.value.trim()) {
    router.push({ path: '/forum/study', query: { keyword: keyword.value } })
  }
}

const handleCommand = (cmd) => {
  switch (cmd) {
    case 'profile':
      router.push(`/user/${userStore.userId}`)
      break
    case 'edit':
      router.push('/profile/edit')
      break
    case 'message':
      router.push('/message')
      break
    case 'admin':
      router.push('/admin')
      break
    case 'logout':
      ElMessageBox.confirm('确定要退出登录吗？', '提示', {
        type: 'warning'
      }).then(() => {
        userStore.logout()
        ElMessage.success('已退出登录')
        router.push('/')
      }).catch(() => {})
      break
  }
}

const fetchUnread = async () => {
  if (!userStore.isLogin) return
  try {
    const data = await getUnreadCount()
    unreadCount.value = data || 0
  } catch (e) {
    // 静默处理
  }
}

onMounted(async () => {
  if (userStore.isLogin) {
    try {
      await userStore.fetchUserInfo()
    } catch (e) {
      // token 失效则跳转登录
    }
    fetchUnread()
  }
})
</script>

<style scoped>
.layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

/* 导航栏 */
.navbar {
  position: sticky;
  top: 0;
  z-index: 100;
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(12px);
  border-bottom: 1px solid var(--border-light);
}

.navbar-inner {
  height: 64px;
  display: flex;
  align-items: center;
  gap: 24px;
}

.logo {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  flex-shrink: 0;
}

.logo-icon {
  width: 38px;
  height: 38px;
  border-radius: 10px;
  background: var(--gradient-primary);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 22px;
  box-shadow: var(--shadow-md);
}

.logo-text {
  font-size: 20px;
  font-weight: 700;
  background: var(--gradient-primary);
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
}

.nav-menu {
  display: flex;
  align-items: center;
  gap: 4px;
  flex: 1;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 5px;
  padding: 8px 14px;
  border-radius: var(--radius-sm);
  color: var(--text-regular);
  font-size: 14px;
  font-weight: 500;
  transition: all 0.25s;
  cursor: pointer;
}

.nav-item:hover {
  color: var(--primary);
  background: rgba(99, 102, 241, 0.06);
}

.nav-item.active {
  color: var(--primary);
  background: rgba(99, 102, 241, 0.1);
  font-weight: 600;
}

.nav-item .el-icon {
  font-size: 17px;
}

.nav-actions {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-shrink: 0;
}

.search-box {
  width: 200px;
}

.user-trigger {
  cursor: pointer;
  padding: 3px;
  border-radius: 50%;
  transition: box-shadow 0.25s;
}

.user-trigger:hover {
  box-shadow: 0 0 0 3px rgba(99, 102, 241, 0.15);
}

/* 主体 */
.main-content {
  flex: 1;
}

/* AI 悬浮按钮 */
.ai-float {
  position: fixed;
  right: 32px;
  bottom: 40px;
  width: 60px;
  height: 60px;
  border-radius: 50%;
  background: var(--gradient-primary);
  color: #fff;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  box-shadow: var(--shadow-lg);
  z-index: 99;
  transition: all 0.3s;
  animation: pulse-glow 2.5s infinite;
}

.ai-float:hover {
  transform: scale(1.1) rotate(8deg);
}

.ai-float-label {
  font-size: 10px;
  font-weight: 700;
  margin-top: -2px;
}

/* 页脚 */
.footer {
  background: #1e293b;
  color: #94a3b8;
  padding: 28px 0;
  text-align: center;
  font-size: 13px;
}

.footer-sub {
  margin-top: 6px;
  font-size: 12px;
  color: #64748b;
}

/* 路由过渡 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.25s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

@media (max-width: 900px) {
  .nav-menu {
    display: none;
  }
  .search-box {
    width: 140px;
  }
}
</style>
