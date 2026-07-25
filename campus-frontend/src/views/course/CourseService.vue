<template>
  <div class="course-page">
    <!-- 顶部渐变 Banner -->
    <div class="page-banner">
      <div class="container banner-inner">
        <div class="banner-text">
          <h1 class="banner-title">
            <el-icon class="title-icon"><Reading /></el-icon>
            代课服务
          </h1>
          <p class="banner-desc">课业互助 · 灵活兼职 · 校园诚信代课平台</p>
        </div>
        <div class="banner-actions">
          <el-input
            v-model="keyword"
            placeholder="搜索课程名 / 地点..."
            size="large"
            clearable
            class="search-input"
            @keyup.enter="handleSearch"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
          <el-button class="publish-btn" size="large" round @click="goPublish">
            <el-icon><Plus /></el-icon>
            <span>发布代课</span>
          </el-button>
        </div>
      </div>
      <div class="banner-deco deco-1"></div>
      <div class="banner-deco deco-2"></div>
    </div>

    <div class="container page-wrap">
      <!-- 状态筛选 -->
      <div class="filter-bar card-base">
        <div class="filter-left">
          <span class="filter-label">
            <el-icon><Filter /></el-icon>订单状态：
          </span>
          <el-radio-group v-model="activeStatus" @change="handleStatusChange">
            <el-radio-button :value="-1">全部</el-radio-button>
            <el-radio-button :value="0">待接单</el-radio-button>
            <el-radio-button :value="1">进行中</el-radio-button>
            <el-radio-button :value="2">已完成</el-radio-button>
          </el-radio-group>
        </div>
        <div class="stat-info">
          <span class="stat-item">
            <el-icon><Document /></el-icon>共 {{ total }} 个订单
          </span>
        </div>
      </div>

      <!-- 订单卡片列表 -->
      <div v-loading="loading">
        <div v-if="orders.length" class="order-grid">
          <CourseCard v-for="o in orders" :key="o.id" :order="o" />
        </div>
        <EmptyState
          v-else
          icon="Document"
          text="暂无匹配的代课订单"
          sub-text="换个筛选条件或发布一个需求吧"
          action-text="发布代课"
          @action="goPublish"
        />
      </div>

      <!-- 分页 -->
      <div class="pagination-wrap" v-if="total > 0">
        <el-pagination
          v-model:current-page="page"
          v-model:page-size="size"
          :total="total"
          :page-sizes="[12, 24, 36]"
          layout="total, sizes, prev, pager, next, jumper"
          background
          @current-change="loadOrders"
          @size-change="loadOrders"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import {
  Reading, Search, Plus, Filter, Document
} from '@element-plus/icons-vue'
import CourseCard from '@/components/CourseCard.vue'
import EmptyState from '@/components/EmptyState.vue'
import { getOrders } from '@/api/course'

const router = useRouter()

const keyword = ref('')
const activeStatus = ref(-1)
const orders = ref([])
const loading = ref(false)
const page = ref(1)
const size = ref(12)
const total = ref(0)

const loadOrders = async () => {
  loading.value = true
  try {
    const params = { page: page.value, size: size.value }
    if (activeStatus.value !== -1) params.status = activeStatus.value
    if (keyword.value.trim()) params.keyword = keyword.value.trim()
    const data = await getOrders(params)
    orders.value = data.records || []
    total.value = data.total || 0
  } catch {
    orders.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

const handleStatusChange = () => {
  page.value = 1
  loadOrders()
}

const handleSearch = () => {
  page.value = 1
  loadOrders()
}

const goPublish = () => {
  router.push('/course/publish')
}

onMounted(loadOrders)
</script>

<style scoped>
.course-page {
  min-height: 100vh;
}

/* Banner */
.page-banner {
  position: relative;
  background: linear-gradient(135deg, #6366f1 0%, #06b6d4 100%);
  padding: 44px 0;
  overflow: hidden;
}

.banner-inner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 24px;
  position: relative;
  z-index: 1;
}

.banner-text {
  color: #fff;
}

.banner-title {
  font-size: 32px;
  font-weight: 800;
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
}

.title-icon {
  font-size: 36px;
}

.banner-desc {
  font-size: 15px;
  opacity: 0.92;
}

.banner-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.search-input {
  width: 300px;
}

.search-input :deep(.el-input__wrapper) {
  border-radius: 30px;
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.18);
}

.publish-btn {
  background: #fff;
  color: var(--primary);
  border: none;
  font-weight: 600;
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.18);
}

.publish-btn:hover {
  background: #fff;
  color: var(--primary-dark);
  transform: translateY(-2px);
}

.banner-deco {
  position: absolute;
  border-radius: 50%;
  filter: blur(60px);
  opacity: 0.4;
}

.deco-1 {
  width: 320px;
  height: 320px;
  background: #8b5cf6;
  top: -100px;
  right: -80px;
}

.deco-2 {
  width: 260px;
  height: 260px;
  background: #06b6d4;
  bottom: -130px;
  left: 20%;
}

/* 筛选条 */
.filter-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 16px;
  padding: 16px 22px;
  margin-bottom: 24px;
}

.filter-left {
  display: flex;
  align-items: center;
  gap: 10px;
}

.filter-label {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 14px;
  color: var(--text-secondary);
  font-weight: 500;
}

.stat-info {
  font-size: 13px;
  color: var(--text-secondary);
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 4px;
}

/* 订单列表 */
.order-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
}

@media (max-width: 860px) {
  .order-grid {
    grid-template-columns: 1fr;
  }
}

.pagination-wrap {
  display: flex;
  justify-content: center;
  margin-top: 30px;
}
</style>
