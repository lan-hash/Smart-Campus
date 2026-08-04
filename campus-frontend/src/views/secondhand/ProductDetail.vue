<template>
  <div class="detail-page" v-loading="loading">
    <div class="container page-wrap" v-if="product">
      <!-- 面包屑 -->
      <el-breadcrumb separator="/" class="bread">
        <el-breadcrumb-item :to="{ path: '/secondhand' }">二手市场</el-breadcrumb-item>
        <el-breadcrumb-item>商品详情</el-breadcrumb-item>
      </el-breadcrumb>

      <div class="detail-layout">
        <!-- 左侧图片展示 -->
        <div class="detail-left">
          <div class="image-box card-base">
            <el-carousel
              v-if="imageList.length"
              :autoplay="false"
              indicator-position="outside"
              height="460px"
              arrow="always"
            >
              <el-carousel-item v-for="(img, idx) in imageList" :key="idx">
                <img :src="img" class="carousel-img" @click="previewImage(idx)" />
              </el-carousel-item>
            </el-carousel>
            <div v-else class="no-image">
              <el-icon><Picture /></el-icon>
              <span>暂无图片</span>
            </div>
          </div>

          <!-- 缩略图 -->
          <div class="thumbs" v-if="imageList.length > 1">
            <div
              v-for="(img, idx) in imageList"
              :key="idx"
              class="thumb-item"
              :class="{ active: currentImg === idx }"
              @click="currentImg = idx"
            >
              <img :src="img" />
            </div>
          </div>
        </div>

        <!-- 右侧商品信息 -->
        <div class="detail-right">
          <div class="info-card card-base">
            <div class="info-status">
              <el-tag :type="statusTagType" effect="dark" round>{{ statusText }}</el-tag>
              <span class="view-count">
                <el-icon><View /></el-icon>{{ product.viewCount || 0 }} 浏览
              </span>
            </div>

            <h1 class="info-title">{{ product.title }}</h1>

            <div class="price-block">
              <span class="price-current">¥{{ product.price }}</span>
              <span v-if="product.originalPrice" class="price-origin">
                原价 ¥{{ product.originalPrice }}
              </span>
              <span v-if="discountText" class="price-discount">{{ discountText }}</span>
            </div>

            <ul class="info-list">
              <li>
                <span class="info-label">新旧程度</span>
                <span class="info-value">
                  <el-rate
                    :model-value="(product.conditionLevel || 9) / 2"
                    disabled
                    show-score
                    :score-template="`${product.conditionLevel || 9}成新`"
                  />
                </span>
              </li>
              <li>
                <span class="info-label">交易地点</span>
                <span class="info-value">
                  <el-icon><Location /></el-icon>{{ product.location || '面议' }}
                </span>
              </li>
              <li>
                <span class="info-label">联系方式</span>
                <span class="info-value">{{ product.contact || '私信联系' }}</span>
              </li>
              <li>
                <span class="info-label">发布时间</span>
                <span class="info-value">{{ formatTime(product.createTime) }}</span>
              </li>
            </ul>

            <!-- 卖家信息卡片 -->
            <div class="seller-card">
              <el-avatar :size="48" :src="product.authorAvatar">
                {{ product.authorNickname?.charAt(0) }}
              </el-avatar>
              <div class="seller-info">
                <div class="seller-name">{{ product.authorNickname || '匿名卖家' }}</div>
                <div class="seller-meta">
                  <el-rate
                    :model-value="5"
                    disabled
                    size="small"
                  />
                  <span class="seller-score">5.0 分</span>
                </div>
              </div>
              <el-button text type="primary" @click="goSeller">
                <el-icon><User /></el-icon>查看主页
              </el-button>
            </div>

            <!-- 操作按钮 -->
            <div class="action-bar">
              <!-- 自己的商品：显示编辑和删除 -->
              <template v-if="isOwner">
                <el-button size="large" round @click="goEdit">
                  <el-icon><EditPen /></el-icon>编辑
                </el-button>
                <el-button size="large" round type="danger" @click="handleDelete">
                  <el-icon><Delete /></el-icon>删除
                </el-button>
              </template>
              <!-- 他人的商品：显示正常操作 -->
              <template v-else>
                <el-button
                  size="large"
                  round
                  :type="isFavorited ? 'warning' : 'default'"
                  @click="handleFavorite"
                >
                  <el-icon><Star v-if="!isFavorited" /><StarFilled v-else /></el-icon>
                  {{ isFavorited ? '已收藏' : '收藏' }}
                </el-button>
                <el-button size="large" round @click="contactSeller">
                  <el-icon><ChatDotRound /></el-icon>联系卖家
                </el-button>
                <el-button size="large" round @click="reportVisible = true">
                  <el-icon><Warning /></el-icon>举报
                </el-button>
                <el-button
                  size="large"
                  type="primary"
                  round
                  class="buy-btn"
                  :disabled="product.status !== 0"
                  @click="handleBuy"
                >
                  <el-icon><ShoppingCart /></el-icon>
                  {{ product.status === 0 ? '立即购买' : '已售出' }}
                </el-button>
              </template>
            </div>
          </div>
        </div>
      </div>

      <!-- 商品描述详情 -->
      <div class="desc-section card-base">
        <h2 class="section-title">
          <el-icon><Document /></el-icon>商品详情
        </h2>
        <div class="desc-content">
          <MdPreview :model-value="product.description || '卖家暂未填写详细描述'" />
        </div>
      </div>
    </div>

    <!-- 举报对话框 -->
    <el-dialog v-model="reportVisible" title="举报商品" width="460px">
      <el-form :model="reportForm" label-width="80px">
        <el-form-item label="举报类型">
          <el-select v-model="reportForm.type" placeholder="请选择" style="width: 100%">
            <el-option label="虚假信息" value="fake" />
            <el-option label="违规商品" value="illegal" />
            <el-option label="诈骗行为" value="fraud" />
            <el-option label="其他" value="other" />
          </el-select>
        </el-form-item>
        <el-form-item label="详细描述">
          <el-input
            v-model="reportForm.content"
            type="textarea"
            :rows="4"
            placeholder="请详细描述举报原因..."
            maxlength="300"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="reportVisible = false">取消</el-button>
        <el-button type="primary" :loading="reporting" @click="submitReport">提交举报</el-button>
      </template>
    </el-dialog>

    <EmptyState
      v-if="!loading && !product"
      icon="Warning"
      text="商品不存在或已下架"
      sub-text="去看看其他好物吧"
      action-text="返回市场"
      @action="router.push('/secondhand')"
    />
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Picture, View, Location, User, Star, StarFilled, ChatDotRound,
  Warning, ShoppingCart, Document, EditPen, Delete
} from '@element-plus/icons-vue'
import EmptyState from '@/components/EmptyState.vue'
import MdPreview from '@/components/MdPreview.vue'
import { useUserStore } from '@/stores/user'
import {
  getProductDetail, toggleFavorite, reportProduct, createTransaction
} from '@/api/secondhand'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const product = ref(null)
const loading = ref(true)
const currentImg = ref(0)
const isFavorited = ref(false)
const reporting = ref(false)
const reportVisible = ref(false)
const reportForm = reactive({ type: '', content: '' })

const imageList = computed(() => {
  if (!product.value?.images) return []
  try {
    const arr = typeof product.value.images === 'string'
      ? JSON.parse(product.value.images) : product.value.images
    return Array.isArray(arr) ? arr : []
  } catch {
    return []
  }
})

const isOwner = computed(() =>
  product.value && userStore.userId === product.value.authorId
)

const statusText = computed(() =>
  ['在售', '已售', '已下架'][product.value?.status] || '在售'
)

const statusTagType = computed(() =>
  ['success', 'info', 'danger'][product.value?.status] || 'success'
)

const discountText = computed(() => {
  if (!product.value?.originalPrice || !product.value?.price) return ''
  const dis = Math.round((product.value.price / product.value.originalPrice) * 100)
  return `${dis}%折`
})

const formatTime = (t) => {
  if (!t) return ''
  return new Date(t).toLocaleString('zh-CN')
}

const loadDetail = async () => {
  loading.value = true
  try {
    const data = await getProductDetail(route.params.id)
    product.value = data
    isFavorited.value = !!data.favorited
  } catch {
    product.value = null
  } finally {
    loading.value = false
  }
}

const previewImage = (idx) => {
  // 简单预览，可扩展为 el-image-viewer
  currentImg.value = idx
}

const handleFavorite = async () => {
  if (!userStore.isLogin) {
    ElMessage.warning('请先登录')
    return
  }
  try {
    await toggleFavorite(product.value.id)
    isFavorited.value = !isFavorited.value
    ElMessage.success(isFavorited.value ? '已加入收藏' : '已取消收藏')
  } catch {
    /* 拦截器已处理 */
  }
}

const contactSeller = () => {
  if (!userStore.isLogin) {
    ElMessage.warning('请先登录')
    return
  }
  if (isOwner.value) {
    ElMessage.info('这是您自己的商品')
    return
  }
  router.push({
    path: '/message',
    query: { userId: product.value.authorId, productId: product.value.id }
  })
}

const goSeller = () => {
  if (product.value.authorId) {
    router.push(`/user/${product.value.authorId}`)
  }
}

const goEdit = () => {
  router.push({ path: '/secondhand/publish', query: { id: product.value.id } })
}

const handleDelete = async () => {
  try {
    await ElMessageBox.confirm('确定要删除这个商品吗？', '删除确认', {
      type: 'warning',
      confirmButtonText: '确认删除',
      cancelButtonText: '取消'
    })
  } catch {
    return
  }
  try {
    const { deleteProduct } = await import('@/api/secondhand')
    await deleteProduct(product.value.id)
    ElMessage.success('删除成功')
    router.push('/secondhand')
  } catch {
    ElMessage.error('删除失败')
  }
}

const handleBuy = async () => {
  if (!userStore.isLogin) {
    ElMessage.warning('请先登录')
    return
  }
  try {
    await ElMessageBox.confirm(
      `确认购买「${product.value.title}」吗？将创建一笔交易订单。`,
      '购买确认',
      { confirmButtonText: '确认购买', cancelButtonText: '再想想', type: 'info' }
    )
  } catch {
    return
  }
  try {
    await createTransaction({ productId: product.value.id })
    ElMessage.success('已发起购买，请前往消息联系卖家完成交易')
    loadDetail()
  } catch {
    /* 拦截器已处理 */
  }
}

const submitReport = async () => {
  if (!reportForm.type) {
    ElMessage.warning('请选择举报类型')
    return
  }
  reporting.value = true
  try {
    await reportProduct(product.value.id, reportForm)
    ElMessage.success('举报已提交，平台将尽快处理')
    reportVisible.value = false
    reportForm.type = ''
    reportForm.content = ''
  } catch {
    /* 拦截器已处理 */
  } finally {
    reporting.value = false
  }
}

onMounted(loadDetail)
</script>

<style scoped>
.detail-page {
  min-height: 100vh;
  padding-bottom: 40px;
}

.bread {
  margin-bottom: 20px;
}

.detail-layout {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 28px;
}

/* 左侧图片 */
.image-box {
  overflow: hidden;
  border-radius: var(--radius-lg);
  background: #f1f5f9;
}

.carousel-img {
  width: 100%;
  height: 460px;
  object-fit: cover;
  cursor: zoom-in;
}

.no-image {
  height: 460px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  color: var(--text-secondary);
  font-size: 14px;
}

.no-image .el-icon {
  font-size: 60px;
  color: #cbd5e1;
}

.thumbs {
  display: flex;
  gap: 10px;
  margin-top: 14px;
}

.thumb-item {
  width: 76px;
  height: 76px;
  border-radius: var(--radius-sm);
  overflow: hidden;
  border: 2px solid transparent;
  cursor: pointer;
  transition: all 0.25s;
}

.thumb-item img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.thumb-item.active,
.thumb-item:hover {
  border-color: var(--primary);
  transform: translateY(-2px);
}

/* 右侧信息 */
.info-card {
  padding: 24px 26px;
}

.info-status {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 14px;
}

.view-count {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: var(--text-secondary);
}

.info-title {
  font-size: 24px;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 18px;
  line-height: 1.4;
}

.price-block {
  display: flex;
  align-items: baseline;
  gap: 14px;
  padding: 16px 20px;
  background: linear-gradient(135deg, rgba(99, 102, 241, 0.06), rgba(139, 92, 246, 0.06));
  border-radius: var(--radius-md);
  margin-bottom: 22px;
}

.price-current {
  font-size: 34px;
  font-weight: 800;
  color: var(--danger);
}

.price-origin {
  font-size: 14px;
  color: var(--text-secondary);
  text-decoration: line-through;
}

.price-discount {
  padding: 2px 10px;
  background: var(--danger);
  color: #fff;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
}

.info-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
  margin-bottom: 22px;
}

.info-list li {
  display: flex;
  align-items: center;
  font-size: 14px;
}

.info-label {
  width: 80px;
  color: var(--text-secondary);
}

.info-value {
  display: flex;
  align-items: center;
  gap: 4px;
  color: var(--text-regular);
  font-weight: 500;
}

/* 卖家卡片 */
.seller-card {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 16px;
  background: var(--bg-page);
  border-radius: var(--radius-md);
  margin-bottom: 22px;
}

.seller-info {
  flex: 1;
}

.seller-name {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 4px;
}

.seller-meta {
  display: flex;
  align-items: center;
  gap: 6px;
}

.seller-score {
  font-size: 12px;
  color: var(--text-secondary);
}

/* 操作按钮 */
.action-bar {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.action-bar .el-button {
  flex: 1;
  min-width: 100px;
}

.buy-btn {
  flex: 2;
  background: var(--gradient-primary);
  border: none;
  font-weight: 600;
  box-shadow: 0 6px 16px rgba(99, 102, 241, 0.3);
}

.buy-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 10px 22px rgba(99, 102, 241, 0.4);
}

/* 详情区 */
.desc-section {
  margin-top: 28px;
  padding: 26px 28px;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 18px;
  padding-bottom: 14px;
  border-bottom: 1px solid var(--border-light);
}

.desc-content {
  font-size: 15px;
  line-height: 1.9;
  color: var(--text-regular);
  white-space: pre-wrap;
}

@media (max-width: 900px) {
  .detail-layout {
    grid-template-columns: 1fr;
  }
}
</style>
