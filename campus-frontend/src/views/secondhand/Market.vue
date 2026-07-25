<template>
  <div class="market-page">
    <!-- 顶部渐变 Banner -->
    <div class="page-banner">
      <div class="container banner-inner">
        <div class="banner-text">
          <h1 class="banner-title">
            <el-icon class="title-icon"><ShoppingCart /></el-icon>
            二手市场
          </h1>
          <p class="banner-desc">闲置流转 · 物尽其用 · 校园好物等你淘</p>
        </div>
        <div class="banner-actions">
          <div class="search-box">
            <el-input
              v-model="keyword"
              placeholder="搜索你想要的宝贝..."
              size="large"
              clearable
              @keyup.enter="handleSearch"
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
            <el-button type="primary" size="large" round @click="handleSearch">
              搜索
            </el-button>
          </div>
          <el-button class="publish-btn" size="large" round @click="goPublish">
            <el-icon><Plus /></el-icon>
            <span>发布商品</span>
          </el-button>
        </div>
      </div>
      <div class="banner-deco deco-1"></div>
      <div class="banner-deco deco-2"></div>
    </div>

    <div class="container page-wrap">
      <div class="market-layout">
        <!-- 左侧分类筛选 -->
        <aside class="category-side">
          <div class="side-card">
            <h3 class="side-title">
              <el-icon><Files /></el-icon>
              <span>全部分类</span>
            </h3>
            <ul class="category-list">
              <li
                class="category-item"
                :class="{ active: activeCategory === null }"
                @click="selectCategory(null)"
              >
                <el-icon class="cat-icon"><Grid /></el-icon>
                <span class="cat-name">全部宝贝</span>
              </li>
              <li
                v-for="cat in categories"
                :key="cat.id"
                class="category-item"
                :class="{ active: activeCategory === cat.id }"
                @click="selectCategory(cat.id)"
              >
                <el-icon class="cat-icon"><Collection /></el-icon>
                <span class="cat-name">{{ cat.name }}</span>
              </li>
            </ul>
          </div>
        </aside>

        <!-- 右侧商品区 -->
        <section class="market-main">
          <!-- 筛选条 -->
          <div class="filter-bar card-base">
            <div class="filter-left">
              <span class="filter-label">排序：</span>
              <el-radio-group v-model="sortBy" size="default" @change="loadProducts">
                <el-radio-button value="latest">最新发布</el-radio-button>
                <el-radio-button value="price_asc">价格低到高</el-radio-button>
                <el-radio-button value="price_desc">价格高到低</el-radio-button>
              </el-radio-group>
            </div>
            <div class="filter-right">
              <span class="filter-label">价格区间：</span>
              <el-input
                v-model="priceMin"
                placeholder="最低"
                size="small"
                style="width: 80px"
                type="number"
              />
              <span class="price-line">-</span>
              <el-input
                v-model="priceMax"
                placeholder="最高"
                size="small"
                style="width: 80px"
                type="number"
              />
              <el-button type="primary" size="small" @click="handleSearch">筛选</el-button>
            </div>
          </div>

          <!-- 商品网格 -->
          <div v-loading="loading">
            <div v-if="products.length" class="product-grid">
              <ProductCard
                v-for="p in products"
                :key="p.id"
                :product="p"
              />
            </div>
            <EmptyState
              v-else
              icon="ShoppingBag"
              text="暂无符合的商品"
              sub-text="换个关键词或分类试试吧"
              action-text="去发布商品"
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
              @current-change="loadProducts"
              @size-change="loadProducts"
            />
          </div>
        </section>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  ShoppingCart, Search, Plus, Files, Grid, Collection
} from '@element-plus/icons-vue'
import ProductCard from '@/components/ProductCard.vue'
import EmptyState from '@/components/EmptyState.vue'
import { getCategories, getProducts } from '@/api/secondhand'

const router = useRouter()

const keyword = ref('')
const categories = ref([])
const activeCategory = ref(null)
const sortBy = ref('latest')
const priceMin = ref('')
const priceMax = ref('')

const products = ref([])
const loading = ref(false)
const page = ref(1)
const size = ref(12)
const total = ref(0)

const loadCategories = async () => {
  try {
    const data = await getCategories()
    categories.value = data || []
  } catch {
    categories.value = []
  }
}

const loadProducts = async () => {
  loading.value = true
  try {
    const params = {
      page: page.value,
      size: size.value
    }
    if (activeCategory.value !== null) params.categoryId = activeCategory.value
    if (keyword.value.trim()) params.keyword = keyword.value.trim()
    if (sortBy.value !== 'latest') params.sort = sortBy.value
    if (priceMin.value) params.minPrice = priceMin.value
    if (priceMax.value) params.maxPrice = priceMax.value
    const data = await getProducts(params)
    products.value = data.records || []
    total.value = data.total || 0
  } catch {
    products.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

const selectCategory = (id) => {
  activeCategory.value = id
  page.value = 1
  loadProducts()
}

const handleSearch = () => {
  page.value = 1
  loadProducts()
}

const goPublish = () => {
  router.push('/secondhand/publish')
}

onMounted(() => {
  loadCategories()
  loadProducts()
})
</script>

<style scoped>
.market-page {
  min-height: 100vh;
}

/* Banner */
.page-banner {
  position: relative;
  background: var(--gradient-hero);
  padding: 40px 0;
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
  gap: 14px;
}

.search-box {
  display: flex;
  align-items: center;
  gap: 8px;
  background: #fff;
  padding: 6px 6px 6px 16px;
  border-radius: 30px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
}

.search-box .el-input {
  width: 260px;
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
  opacity: 0.35;
}

.deco-1 {
  width: 300px;
  height: 300px;
  background: #6366f1;
  top: -100px;
  right: -60px;
}

.deco-2 {
  width: 240px;
  height: 240px;
  background: #06b6d4;
  bottom: -120px;
  left: 10%;
}

/* 主体布局 */
.market-layout {
  display: grid;
  grid-template-columns: 220px 1fr;
  gap: 24px;
}

.category-side {
  position: sticky;
  top: 84px;
  align-self: start;
}

.side-card {
  background: var(--bg-card);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-md);
  padding: 18px 14px;
}

.side-title {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
  padding: 0 8px 14px;
  border-bottom: 1px solid var(--border-light);
  margin-bottom: 12px;
}

.category-list {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.category-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 12px;
  border-radius: var(--radius-sm);
  cursor: pointer;
  color: var(--text-regular);
  font-size: 14px;
  transition: all 0.25s;
}

.category-item:hover {
  background: rgba(99, 102, 241, 0.08);
  color: var(--primary);
}

.category-item.active {
  background: var(--gradient-primary);
  color: #fff;
  font-weight: 600;
  box-shadow: 0 4px 12px rgba(99, 102, 241, 0.3);
}

.cat-icon {
  font-size: 16px;
}

/* 筛选条 */
.filter-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 16px;
  padding: 14px 20px;
  margin-bottom: 20px;
}

.filter-left,
.filter-right {
  display: flex;
  align-items: center;
  gap: 8px;
}

.filter-label {
  font-size: 14px;
  color: var(--text-secondary);
  font-weight: 500;
}

.price-line {
  color: var(--text-secondary);
}

/* 商品网格 */
.product-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}

@media (max-width: 1100px) {
  .product-grid {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (max-width: 860px) {
  .market-layout {
    grid-template-columns: 1fr;
  }
  .product-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

.pagination-wrap {
  display: flex;
  justify-content: center;
  margin-top: 30px;
}
</style>
