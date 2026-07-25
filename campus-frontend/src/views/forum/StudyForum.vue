<template>
  <div class="forum-page">
    <!-- 顶部渐变 Banner -->
    <div class="page-banner">
      <div class="container banner-inner">
        <div class="banner-text">
          <h1 class="banner-title">
            <el-icon class="title-icon"><Reading /></el-icon>
            学习论坛
          </h1>
          <p class="banner-desc">分享学习笔记 · 交流课程心得 · 一起进步成长</p>
        </div>
        <el-button class="post-btn" size="large" round @click="goPost">
          <el-icon><Plus /></el-icon>
          <span>发布帖子</span>
        </el-button>
      </div>
      <div class="banner-deco deco-1"></div>
      <div class="banner-deco deco-2"></div>
    </div>

    <div class="container page-wrap">
      <div class="forum-layout">
        <!-- 左侧版块分类 -->
        <aside class="category-side">
          <div class="side-card">
            <h3 class="side-title">
              <el-icon><Files /></el-icon>
              <span>版块分类</span>
            </h3>
            <ul class="category-list">
              <li
                class="category-item"
                :class="{ active: activeCategory === null }"
                @click="selectCategory(null)"
              >
                <el-icon class="cat-icon"><Grid /></el-icon>
                <span class="cat-name">全部帖子</span>
              </li>
              <li
                v-for="cat in categories"
                :key="cat.id"
                class="category-item"
                :class="{ active: activeCategory === cat.id }"
                @click="selectCategory(cat.id)"
              >
                <el-icon class="cat-icon">
                  <component :is="cat.icon || 'Collection'" />
                </el-icon>
                <span class="cat-name">{{ cat.name }}</span>
              </li>
            </ul>
          </div>

          <div class="side-card side-tip">
            <el-icon class="tip-icon"><Sunny /></el-icon>
            <p>文明发言，友善交流</p>
            <p class="tip-sub">分享优质内容可获得精华标识</p>
          </div>
        </aside>

        <!-- 右侧内容区 -->
        <div class="forum-main">
          <!-- 搜索 + 排序 -->
          <div class="toolbar card-base">
            <el-input
              v-model="keyword"
              class="search-input"
              placeholder="搜索帖子标题或内容..."
              :prefix-icon="Search"
              clearable
              @keyup.enter="handleSearch"
              @clear="handleSearch"
            />
            <div class="sort-group">
              <span class="sort-label">排序</span>
              <el-radio-group v-model="sort" size="default" @change="fetchPosts">
                <el-radio-button label="new">最新</el-radio-button>
                <el-radio-button label="hot">最热</el-radio-button>
              </el-radio-group>
            </div>
          </div>

          <!-- 当前筛选条件 -->
          <div v-if="currentCategoryName || keyword" class="filter-tag">
            <span v-if="currentCategoryName">
              <el-icon><Folder /></el-icon>{{ currentCategoryName }}
            </span>
            <span v-if="keyword">
              <el-icon><Search /></el-icon>"{{ keyword }}"
            </span>
            <el-button text size="small" @click="clearFilter">清除</el-button>
          </div>

          <!-- 帖子网格 -->
          <div v-loading="loading" class="post-grid">
            <PostCard
              v-for="post in postList"
              :key="post.id"
              :post="post"
              class="grid-item"
            />
          </div>

          <EmptyState
            v-if="!loading && !postList.length"
            icon="Document"
            text="暂无相关帖子"
            sub-text="快来发布第一篇帖子吧"
            action-text="发布帖子"
            @action="goPost"
          />

          <!-- 分页 -->
          <div v-if="total > 0" class="pagination">
            <el-pagination
              v-model:current-page="page"
              v-model:page-size="size"
              :total="total"
              :page-sizes="[12, 24, 36]"
              layout="total, prev, pager, next, jumper"
              background
              @current-change="fetchPosts"
              @size-change="fetchPosts"
            />
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  Reading, Plus, Files, Grid, Search, Folder, Sunny, Collection
} from '@element-plus/icons-vue'
import { getCategories, getPosts } from '@/api/forum'
import { useUserStore } from '@/stores/user'
import PostCard from '@/components/PostCard.vue'
import EmptyState from '@/components/EmptyState.vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

// 论坛类型：0=学习
const FORUM_TYPE = 0

const categories = ref([])
const postList = ref([])
const loading = ref(false)
const activeCategory = ref(null)
const keyword = ref('')
const sort = ref('new')
const page = ref(1)
const size = ref(12)
const total = ref(0)

const currentCategoryName = computed(() => {
  const c = categories.value.find((i) => i.id === activeCategory.value)
  return c ? c.name : ''
})

const fetchCategories = async () => {
  try {
    const data = await getCategories(FORUM_TYPE)
    categories.value = data || []
    // 若路由带 categoryId 则预选
    if (route.query.categoryId) {
      const id = Number(route.query.categoryId)
      if (categories.value.some((c) => c.id === id)) {
        activeCategory.value = id
      }
    }
  } catch (e) {
    // ignore
  }
}

const fetchPosts = async () => {
  loading.value = true
  try {
    const params = {
      type: FORUM_TYPE,
      page: page.value,
      size: size.value,
      sort: sort.value
    }
    if (activeCategory.value !== null) params.categoryId = activeCategory.value
    if (keyword.value.trim()) params.keyword = keyword.value.trim()
    const data = await getPosts(params)
    postList.value = data.records || []
    total.value = data.total || 0
  } catch (e) {
    ElMessage.error('加载帖子失败')
  } finally {
    loading.value = false
  }
}

const selectCategory = (id) => {
  activeCategory.value = id
  page.value = 1
  fetchPosts()
}

const handleSearch = () => {
  page.value = 1
  fetchPosts()
}

const clearFilter = () => {
  activeCategory.value = null
  keyword.value = ''
  page.value = 1
  fetchPosts()
}

const goPost = () => {
  if (!userStore.isLogin) {
    ElMessage.warning('请先登录后再发帖')
    router.push({ path: '/login', query: { redirect: route.fullPath } })
    return
  }
  router.push({
    path: '/forum/post/edit',
    query: { type: FORUM_TYPE, categoryId: activeCategory.value || '' }
  })
}

// 监听路由 keyword 变化(顶部全局搜索跳转过来)
watch(
  () => route.query.keyword,
  (val) => {
    keyword.value = val || ''
    page.value = 1
    fetchPosts()
  }
)

onMounted(async () => {
  keyword.value = route.query.keyword || ''
  await fetchCategories()
  await fetchPosts()
})
</script>

<style scoped>
.forum-page {
  min-height: calc(100vh - 64px);
}

/* 顶部 Banner */
.page-banner {
  position: relative;
  background: var(--gradient-primary);
  overflow: hidden;
  padding: 40px 0;
}

.banner-inner {
  position: relative;
  z-index: 2;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
}

.banner-title {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 32px;
  font-weight: 700;
  color: #fff;
  margin-bottom: 10px;
}

.title-icon {
  font-size: 36px;
}

.banner-desc {
  font-size: 15px;
  color: rgba(255, 255, 255, 0.9);
  letter-spacing: 1px;
}

.post-btn {
  background: #fff;
  color: var(--primary);
  border: none;
  font-weight: 600;
  padding: 0 28px;
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.15);
  transition: all 0.3s;
}

.post-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 12px 28px rgba(0, 0, 0, 0.2);
  background: #fff;
  color: var(--primary-dark);
}

.banner-deco {
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.12);
}

.deco-1 {
  width: 220px;
  height: 220px;
  top: -80px;
  right: 10%;
}

.deco-2 {
  width: 140px;
  height: 140px;
  bottom: -60px;
  right: 30%;
}

/* 布局 */
.forum-layout {
  display: flex;
  gap: 24px;
  align-items: flex-start;
}

.category-side {
  width: 220px;
  flex-shrink: 0;
  position: sticky;
  top: 84px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.side-card {
  background: var(--bg-card);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-md);
  padding: 18px;
}

.side-title {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 14px;
  padding-bottom: 12px;
  border-bottom: 1px solid var(--border-light);
}

.category-list {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.category-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  border-radius: var(--radius-sm);
  cursor: pointer;
  font-size: 14px;
  color: var(--text-regular);
  transition: all 0.25s;
}

.category-item .cat-icon {
  font-size: 16px;
  color: var(--text-secondary);
}

.category-item:hover {
  background: rgba(99, 102, 241, 0.06);
  color: var(--primary);
}

.category-item:hover .cat-icon {
  color: var(--primary);
}

.category-item.active {
  background: var(--gradient-primary);
  color: #fff;
  font-weight: 600;
  box-shadow: 0 4px 12px rgba(99, 102, 241, 0.3);
}

.category-item.active .cat-icon {
  color: #fff;
}

.side-tip {
  text-align: center;
  color: var(--text-regular);
  font-size: 13px;
  line-height: 1.7;
}

.tip-icon {
  font-size: 28px;
  color: var(--warning);
  margin-bottom: 8px;
}

.tip-sub {
  color: var(--text-secondary);
  font-size: 12px;
  margin-top: 4px;
}

/* 主内容区 */
.forum-main {
  flex: 1;
  min-width: 0;
}

.toolbar {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 14px 18px;
  margin-bottom: 18px;
}

.search-input {
  flex: 1;
}

.sort-group {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
}

.sort-label {
  font-size: 13px;
  color: var(--text-secondary);
}

.filter-tag {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 16px;
  font-size: 13px;
  color: var(--text-regular);
}

.filter-tag span {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 4px 12px;
  background: rgba(99, 102, 241, 0.1);
  color: var(--primary);
  border-radius: 16px;
}

/* 帖子网格 */
.post-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 18px;
  min-height: 200px;
}

.grid-item {
  animation: fadeInUp 0.5s ease forwards;
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 36px;
}

@media (max-width: 1100px) {
  .post-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .forum-layout {
    flex-direction: column;
  }
  .category-side {
    width: 100%;
    position: static;
  }
  .post-grid {
    grid-template-columns: 1fr;
  }
  .banner-inner {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
