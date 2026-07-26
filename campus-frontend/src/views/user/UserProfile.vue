<template>
  <div class="profile-page" v-loading="loading">
    <!-- Banner 区 -->
    <div class="banner">
      <div class="banner-bg">
        <span class="banner-blob b1"></span>
        <span class="banner-blob b2"></span>
      </div>
      <div class="container banner-inner">
        <div class="avatar-wrap">
          <el-avatar :size="120" :src="userInfo.avatar" class="user-avatar">
            {{ userInfo.nickname?.charAt(0) }}
          </el-avatar>
          <span v-if="userInfo.verified" class="verify-badge" title="校园认证">
            <el-icon><CircleCheck /></el-icon>
          </span>
        </div>

        <div class="user-info">
          <div class="name-row">
            <h1 class="user-name">{{ userInfo.nickname || '校园用户' }}</h1>
            <el-tag
              v-if="userInfo.verified"
              type="success"
              size="small"
              effect="dark"
              round
              class="verify-tag"
            >
              <el-icon><School /></el-icon> 校园认证
            </el-tag>
            <el-tag v-if="userInfo.role === 1" type="warning" size="small" round effect="dark">
              管理员
            </el-tag>
          </div>
          <p class="user-bio" v-if="userInfo.bio">{{ userInfo.bio }}</p>
          <p class="user-bio placeholder" v-else>这个人很神秘，什么都没留下~</p>

          <div class="user-meta">
            <span v-if="userInfo.campus" class="meta-item">
              <el-icon><Location /></el-icon>{{ userInfo.campus }}
            </span>
            <span v-if="userInfo.email" class="meta-item">
              <el-icon><Message /></el-icon>{{ userInfo.email }}
            </span>
            <span class="meta-item">
              <el-icon><Clock /></el-icon>加入于 {{ formatTime(userInfo.createTime) }}
            </span>
          </div>
        </div>

        <!-- 操作按钮 -->
        <div class="action-bar">
          <template v-if="isSelf">
            <el-button type="primary" round @click="router.push('/profile/edit')">
              <el-icon><Edit /></el-icon>编辑资料
            </el-button>
          </template>
          <template v-else>
            <el-button
              :type="isFollowing ? 'default' : 'primary'"
              round
              :loading="followLoading"
              @click="toggleFollow"
            >
              <el-icon><component :is="isFollowing ? 'Check' : 'Plus'" /></el-icon>
              {{ isFollowing ? '已关注' : '关注' }}
            </el-button>
            <el-button round @click="goChat">
              <el-icon><ChatDotRound /></el-icon>私信
            </el-button>
            <el-dropdown trigger="click" @command="handleMore">
              <el-button circle>
                <el-icon><More /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="block">
                    <el-icon><CircleClose /></el-icon>拉黑用户
                  </el-dropdown-item>
                  <el-dropdown-item command="report">
                    <el-icon><Warning /></el-icon>举报用户
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
        </div>
      </div>
    </div>

    <!-- 统计栏 -->
    <div class="container">
      <div class="stats-bar">
        <div class="stat-item" v-for="s in statItems" :key="s.label">
          <strong>{{ s.value }}</strong>
          <span>{{ s.label }}</span>
        </div>
      </div>
    </div>

    <!-- Tab 内容 -->
    <div class="container">
      <div class="tab-card">
        <el-tabs v-model="activeTab" class="profile-tabs">
          <el-tab-pane label="帖子" name="posts">
            <div class="tab-content" v-loading="tabLoading">
              <div class="post-grid" v-if="posts.length">
                <PostCard v-for="p in posts" :key="p.id" :post="p" />
              </div>
              <EmptyState
                v-else
                icon="Document"
                :text="isSelf ? '你还没有发布过帖子' : 'TA还没有发布帖子'"
                :action-text="isSelf ? '去发帖' : ''"
                @action="router.push('/forum/post/edit')"
              />
            </div>
          </el-tab-pane>

          <el-tab-pane label="收藏" name="collects">
            <div class="tab-content" v-loading="tabLoading">
              <div class="post-grid" v-if="collects.length">
                <PostCard v-for="p in collects" :key="p.id" :post="p" />
              </div>
              <EmptyState
                v-else
                icon="Star"
                :text="isSelf ? '你还没有收藏内容' : 'TA还没有公开收藏'"
              />
            </div>
          </el-tab-pane>

          <el-tab-pane label="二手商品" name="products">
            <div class="tab-content" v-loading="tabLoading">
              <div class="product-grid" v-if="products.length">
                <ProductCard v-for="p in products" :key="p.id" :product="p" />
              </div>
              <EmptyState
                v-else
                icon="ShoppingCart"
                :text="isSelf ? '你还没有发布商品' : 'TA还没有发布商品'"
                :action-text="isSelf ? '去发布' : ''"
                @action="router.push('/secondhand/publish')"
              />
            </div>
          </el-tab-pane>
        </el-tabs>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'
import {
  getUserDetail, getUserStats, followUser, unfollowUser, blockUser
} from '@/api/user'
import { getMyPosts, getMyCollects } from '@/api/forum'
import { getMyProducts } from '@/api/secondhand'
import PostCard from '@/components/PostCard.vue'
import ProductCard from '@/components/ProductCard.vue'
import EmptyState from '@/components/EmptyState.vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const loading = ref(false)
const tabLoading = ref(false)
const followLoading = ref(false)

const userId = computed(() => Number(route.params.id))
const isSelf = computed(() => userId.value === userStore.userId)

const userInfo = ref({})
const stats = ref({ postCount: 0, followingCount: 0, fansCount: 0 })
const isFollowing = ref(false)

const activeTab = ref('posts')
const posts = ref([])
const collects = ref([])
const products = ref([])

const statItems = computed(() => [
  { label: '帖子', value: stats.value.postCount ?? 0 },
  { label: '关注', value: stats.value.followingCount ?? 0 },
  { label: '粉丝', value: stats.value.fansCount ?? 0 }
])

const formatTime = (t) => {
  if (!t) return ''
  return String(t).replace('T', ' ').slice(0, 10)
}

const fetchUserInfo = async () => {
  // 查看自己的主页：直接用 store 数据，避免多余请求
  if (isSelf.value && userStore.userInfo) {
    userInfo.value = userStore.userInfo
    stats.value = {
      postCount: userStore.userInfo.postCount,
      followingCount: userStore.userInfo.followCount,
      fansCount: userStore.userInfo.fansCount
    }
    return
  }
  loading.value = true
  try {
    const detail = await getUserDetail(userId.value)
    userInfo.value = detail || {}
    stats.value = {
      postCount: detail?.postCount,
      followingCount: detail?.followCount,
      fansCount: detail?.fansCount
    }
    isFollowing.value = !!detail?.isFollowing
  } catch (e) {
    // 错误已处理
  } finally {
    loading.value = false
  }
}

const fetchTabData = async () => {
  tabLoading.value = true
  try {
    if (activeTab.value === 'posts') {
      const data = await getMyPosts({ userId: userId.value, page: 1, size: 12 })
      posts.value = data?.records || data?.list || data || []
    } else if (activeTab.value === 'collects') {
      const data = await getMyCollects({ userId: userId.value, page: 1, size: 12 })
      collects.value = data?.records || data?.list || data || []
    } else if (activeTab.value === 'products') {
      const data = await getMyProducts({ userId: userId.value, page: 1, size: 12 })
      products.value = data?.records || data?.list || data || []
    }
  } catch (e) {
    // 错误已处理
  } finally {
    tabLoading.value = false
  }
}

const toggleFollow = async () => {
  if (!userStore.isLogin) {
    router.push({ path: '/login', query: { redirect: route.fullPath } })
    return
  }
  followLoading.value = true
  try {
    if (isFollowing.value) {
      await unfollowUser(userId.value)
      isFollowing.value = false
      stats.value.fansCount = Math.max(0, (stats.value.fansCount || 0) - 1)
      ElMessage.success('已取消关注')
    } else {
      await followUser(userId.value)
      isFollowing.value = true
      stats.value.fansCount = (stats.value.fansCount || 0) + 1
      ElMessage.success('关注成功')
    }
  } catch (e) {
    // 错误已处理
  } finally {
    followLoading.value = false
  }
}

const goChat = () => {
  if (!userStore.isLogin) {
    router.push({ path: '/login', query: { redirect: route.fullPath } })
    return
  }
  router.push({ path: '/message', query: { userId: userId.value } })
}

const handleMore = (cmd) => {
  if (cmd === 'block') {
    ElMessageBox.confirm('确定要拉黑该用户吗？拉黑后将不再接收其消息。', '拉黑用户', {
      type: 'warning',
      confirmButtonText: '确定拉黑',
      cancelButtonText: '取消'
    }).then(async () => {
      try {
        await blockUser(userId.value)
        ElMessage.success('已拉黑该用户')
      } catch (e) {
        // 错误已处理
      }
    }).catch(() => {})
  } else if (cmd === 'report') {
    ElMessageBox.prompt('请输入举报理由', '举报用户', {
      confirmButtonText: '提交举报',
      cancelButtonText: '取消',
      inputType: 'textarea'
    }).then(() => {
      ElMessage.success('举报已提交，我们会尽快处理')
    }).catch(() => {})
  }
}

watch(activeTab, fetchTabData)

watch(
  () => route.params.id,
  () => {
    if (userId.value) {
      fetchUserInfo()
      activeTab.value = 'posts'
      fetchTabData()
    }
  }
)

onMounted(() => {
  fetchUserInfo()
  fetchTabData()
})
</script>

<style scoped>
.profile-page {
  padding-bottom: 40px;
}

/* Banner */
.banner {
  position: relative;
  padding: 56px 0 40px;
  overflow: hidden;
  background: var(--gradient-hero);
}

.banner-bg {
  position: absolute;
  inset: 0;
  z-index: 0;
}

.banner-blob {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  opacity: 0.5;
  animation: float 9s ease-in-out infinite;
}

.b1 {
  width: 360px;
  height: 360px;
  background: #6366f1;
  top: -120px;
  right: -60px;
}

.b2 {
  width: 300px;
  height: 300px;
  background: #8b5cf6;
  bottom: -140px;
  left: -40px;
  animation-delay: 3s;
}

@keyframes float {
  0%, 100% { transform: translate(0, 0) scale(1); }
  50% { transform: translate(20px, -26px) scale(1.08); }
}

.banner-inner {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: center;
  gap: 28px;
  color: #fff;
  flex-wrap: wrap;
}

.avatar-wrap {
  position: relative;
  flex-shrink: 0;
}

:deep(.user-avatar) {
  border: 4px solid rgba(255, 255, 255, 0.9);
  box-shadow: 0 12px 28px rgba(0, 0, 0, 0.25);
  font-size: 42px;
  font-weight: 700;
  background: linear-gradient(135deg, #818cf8, #a78bfa);
  color: #fff;
}

.verify-badge {
  position: absolute;
  bottom: 6px;
  right: 6px;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: var(--success);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  border: 3px solid #fff;
  box-shadow: 0 4px 10px rgba(16, 185, 129, 0.4);
}

.user-info {
  flex: 1;
  min-width: 220px;
}

.name-row {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
  margin-bottom: 8px;
}

.user-name {
  font-size: 28px;
  font-weight: 700;
  text-shadow: 0 2px 12px rgba(0, 0, 0, 0.15);
}

.verify-tag .el-icon {
  margin-right: 2px;
}

.user-bio {
  font-size: 14px;
  line-height: 1.7;
  opacity: 0.92;
  margin-bottom: 12px;
  max-width: 560px;
}

.user-bio.placeholder {
  opacity: 0.7;
  font-style: italic;
}

.user-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 18px;
}

.meta-item {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  font-size: 13px;
  opacity: 0.88;
}

.action-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-shrink: 0;
}

.action-bar .el-button {
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.12);
}

/* 统计栏 */
.stats-bar {
  display: flex;
  gap: 0;
  margin-top: -24px;
  position: relative;
  z-index: 2;
  background: var(--bg-card);
  border-radius: var(--radius-lg);
  padding: 24px 0;
  box-shadow: var(--shadow-md);
}

.stat-item {
  flex: 1;
  text-align: center;
  border-right: 1px solid var(--border-light);
  cursor: pointer;
  transition: transform 0.25s;
}

.stat-item:last-child {
  border-right: none;
}

.stat-item:hover {
  transform: translateY(-3px);
}

.stat-item strong {
  display: block;
  font-size: 26px;
  font-weight: 800;
  background: var(--gradient-primary);
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
}

.stat-item span {
  font-size: 13px;
  color: var(--text-secondary);
}

/* Tab 卡片 */
.tab-card {
  margin-top: 24px;
  background: var(--bg-card);
  border-radius: var(--radius-lg);
  padding: 8px 28px 28px;
  box-shadow: var(--shadow-md);
  min-height: 400px;
}

:deep(.profile-tabs .el-tabs__header) {
  margin-bottom: 24px;
}

:deep(.profile-tabs .el-tabs__item) {
  font-size: 15px;
  font-weight: 500;
  height: 52px;
}

:deep(.profile-tabs .el-tabs__active-bar) {
  background: var(--gradient-primary);
  height: 3px;
  border-radius: 2px;
}

.tab-content {
  min-height: 300px;
}

.post-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}

@media (max-width: 992px) {
  .post-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  .product-grid {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (max-width: 768px) {
  .banner-inner {
    flex-direction: column;
    text-align: center;
  }
  .user-meta {
    justify-content: center;
  }
  .stats-bar {
    flex-wrap: wrap;
  }
  .stat-item {
    flex: 1 1 33%;
    border-right: none;
  }
  .post-grid,
  .product-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 576px) {
  .post-grid,
  .product-grid {
    grid-template-columns: 1fr;
  }
}
</style>
