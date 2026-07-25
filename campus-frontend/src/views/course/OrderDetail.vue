<template>
  <div class="order-detail" v-loading="loading">
    <div class="container page-wrap" v-if="order">
      <!-- 面包屑 -->
      <el-breadcrumb separator="/" class="bread">
        <el-breadcrumb-item :to="{ path: '/course' }">代课服务</el-breadcrumb-item>
        <el-breadcrumb-item>订单详情</el-breadcrumb-item>
      </el-breadcrumb>

      <div class="detail-layout">
        <!-- 左侧主信息 -->
        <div class="detail-main">
          <!-- 订单信息卡片 -->
          <div class="info-card card-base">
            <div class="card-head">
              <h2 class="card-title">
                <el-icon><Reading /></el-icon>
                {{ order.courseName }}
              </h2>
              <el-tag :type="statusTagType" effect="dark" round size="large">
                {{ statusText }}
              </el-tag>
            </div>

            <div class="info-grid">
              <div class="info-item">
                <div class="info-label">
                  <el-icon><Collection /></el-icon>课程类型
                </div>
                <div class="info-value">{{ order.courseType || '未指定' }}</div>
              </div>
              <div class="info-item">
                <div class="info-label">
                  <el-icon><Clock /></el-icon>上课时间
                </div>
                <div class="info-value">{{ order.classTime || '待协商' }}</div>
              </div>
              <div class="info-item">
                <div class="info-label">
                  <el-icon><Location /></el-icon>上课地点
                </div>
                <div class="info-value">{{ order.location || '待协商' }}</div>
              </div>
              <div class="info-item salary-item">
                <div class="info-label">
                  <el-icon><Money /></el-icon>代课薪资
                </div>
                <div class="info-value salary">¥{{ order.salary }}</div>
              </div>
            </div>

            <div class="desc-block">
              <div class="desc-title">需求描述</div>
              <p class="desc-content">{{ order.description || '发布者暂未填写详细需求' }}</p>
            </div>
          </div>

          <!-- 状态时间轴 -->
          <div class="timeline-card card-base">
            <h3 class="card-title">
              <el-icon><Clock /></el-icon>订单进度
            </h3>
            <el-timeline class="status-timeline">
              <el-timeline-item
                v-for="(node, idx) in timeline"
                :key="idx"
                :type="node.active ? 'primary' : 'info'"
                :hollow="!node.active"
                :timestamp="node.time"
                placement="top"
              >
                <div class="timeline-node">
                  <span class="node-title">{{ node.title }}</span>
                  <span v-if="node.desc" class="node-desc">{{ node.desc }}</span>
                </div>
              </el-timeline-item>
            </el-timeline>
          </div>

          <!-- 评价区 -->
          <div v-if="order.status === 2" class="evaluate-card card-base">
            <h3 class="card-title">
              <el-icon><Star /></el-icon>交易评价
            </h3>
            <div v-if="order.evaluation" class="eval-content">
              <el-rate :model-value="order.evaluation.score" disabled show-score />
              <p class="eval-text">{{ order.evaluation.content }}</p>
            </div>
            <div v-else-if="!isOwner" class="eval-form">
              <p class="eval-tip">订单已完成，请对本次代课进行评价</p>
              <el-form label-position="top">
                <el-form-item label="评分">
                  <el-rate v-model="evalForm.score" :texts="['很差', '较差', '一般', '满意', '非常满意']" show-text />
                </el-form-item>
                <el-form-item label="评价内容">
                  <el-input
                    v-model="evalForm.content"
                    type="textarea"
                    :rows="3"
                    placeholder="说说你对本次代课的满意度..."
                    maxlength="200"
                    show-word-limit
                  />
                </el-form-item>
                <el-button type="primary" round :loading="evaluating" @click="submitEvaluate">
                  提交评价
                </el-button>
              </el-form>
            </div>
            <div v-else class="eval-empty">
              <el-empty description="对方暂未评价" :image-size="80" />
            </div>
          </div>
        </div>

        <!-- 右侧操作区 -->
        <div class="detail-side">
          <!-- 发布者信息 -->
          <div class="publisher-card card-base">
            <h3 class="side-title">
              <el-icon><User /></el-icon>发布者信息
            </h3>
            <div class="publisher-info">
              <el-avatar :size="60" :src="order.publisherAvatar">
                {{ order.publisherName?.charAt(0) }}
              </el-avatar>
              <div class="publisher-meta">
                <div class="publisher-name">{{ order.publisherName || '匿名' }}</div>
                <div class="publisher-stat">
                  <el-rate :model-value="order.publisherScore || 5" disabled size="small" />
                </div>
              </div>
            </div>
            <el-button type="primary" plain round class="contact-btn" @click="contactPublisher">
              <el-icon><ChatDotRound /></el-icon>联系发布者
            </el-button>
          </div>

          <!-- 操作按钮 -->
          <div class="action-card card-base">
            <h3 class="side-title">
              <el-icon><Operation /></el-icon>订单操作
            </h3>

            <!-- 非发布者可见的接单按钮 -->
            <el-button
              v-if="!isOwner && order.status === 0"
              type="primary"
              size="large"
              round
              class="action-btn accept-btn"
              :loading="accepting"
              @click="handleAccept"
            >
              <el-icon><Select /></el-icon>我要接单
            </el-button>

            <!-- 状态流转按钮 -->
            <el-button
              v-if="isOwner && order.status === 1"
              type="success"
              size="large"
              round
              class="action-btn"
              :loading="flowing"
              @click="handleComplete"
            >
              <el-icon><CircleCheck /></el-icon>确认完成
            </el-button>

            <el-button
              v-if="!isOwner && order.status === 1 && order.acceptorId === userStore.userId"
              type="warning"
              size="large"
              round
              class="action-btn"
              :loading="flowing"
              @click="handleComplete"
            >
              <el-icon><CircleCheck /></el-icon>申请完成
            </el-button>

            <el-button
              v-if="order.status === 0 || order.status === 1"
              size="large"
              round
              class="action-btn"
              @click="reportVisible = true"
            >
              <el-icon><Warning /></el-icon>举报订单
            </el-button>

            <div v-if="order.status === 3" class="order-canceled">
              <el-icon><CircleClose /></el-icon>
              该订单已取消
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 举报对话框 -->
    <el-dialog v-model="reportVisible" title="举报订单" width="460px">
      <el-form :model="reportForm" label-width="80px">
        <el-form-item label="举报类型">
          <el-select v-model="reportForm.type" placeholder="请选择" style="width: 100%">
            <el-option label="虚假信息" value="fake" />
            <el-option label="违规内容" value="illegal" />
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
      v-if="!loading && !order"
      icon="Warning"
      text="订单不存在或已被删除"
      sub-text="去看看其他代课需求吧"
      action-text="返回列表"
      @action="router.push('/course')"
    />
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Reading, Collection, Clock, Location, Money, Star, User,
  ChatDotRound, Operation, Select, CircleCheck, CircleClose, Warning
} from '@element-plus/icons-vue'
import EmptyState from '@/components/EmptyState.vue'
import { useUserStore } from '@/stores/user'
import {
  getOrderDetail, acceptOrder, updateOrderStatus,
  evaluateOrder, reportOrder
} from '@/api/course'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const order = ref(null)
const loading = ref(true)
const accepting = ref(false)
const flowing = ref(false)
const evaluating = ref(false)
const reporting = ref(false)
const reportVisible = ref(false)
const reportForm = reactive({ type: '', content: '' })
const evalForm = reactive({ score: 5, content: '' })

const isOwner = computed(() =>
  order.value && userStore.userId === order.value.publisherId
)

const statusText = computed(() =>
  ['待接单', '进行中', '已完成', '已取消'][order.value?.status] || '待接单'
)

const statusTagType = computed(() =>
  ['warning', 'primary', 'success', 'info'][order.value?.status] || 'warning'
)

const timeline = computed(() => {
  if (!order.value) return []
  const nodes = [
    { title: '需求发布', desc: '等待接单', time: formatTime(order.value.createTime), active: true }
  ]
  if (order.value.status >= 1) {
    nodes.push({
      title: '已接单',
      desc: order.value.acceptorName ? `${order.value.acceptorName} 已接单` : '接单成功',
      time: formatTime(order.value.acceptTime),
      active: true
    })
  }
  if (order.value.status >= 2) {
    nodes.push({
      title: '订单完成',
      desc: '代课已完成',
      time: formatTime(order.value.completeTime),
      active: true
    })
  }
  if (order.value.status === 3) {
    nodes.push({
      title: '订单取消',
      desc: '订单已取消',
      time: formatTime(order.value.cancelTime),
      active: true
    })
  }
  return nodes
})

const formatTime = (t) => {
  if (!t) return ''
  return new Date(t).toLocaleString('zh-CN')
}

const loadDetail = async () => {
  loading.value = true
  try {
    const data = await getOrderDetail(route.params.id)
    order.value = data
  } catch {
    order.value = null
  } finally {
    loading.value = false
  }
}

const contactPublisher = () => {
  if (!userStore.isLogin) {
    ElMessage.warning('请先登录')
    return
  }
  router.push({
    path: '/message',
    query: { userId: order.value.publisherId, orderId: order.value.id }
  })
}

const handleAccept = async () => {
  if (!userStore.isLogin) {
    ElMessage.warning('请先登录')
    return
  }
  try {
    await ElMessageBox.confirm(
      `确认接单「${order.value.courseName}」吗？接单后需按要求完成代课。`,
      '接单确认',
      { confirmButtonText: '确认接单', cancelButtonText: '再想想', type: 'info' }
    )
  } catch {
    return
  }
  accepting.value = true
  try {
    await acceptOrder(order.value.id)
    ElMessage.success('接单成功，请联系发布者确认细节')
    loadDetail()
  } catch {
    /* 拦截器已处理 */
  } finally {
    accepting.value = false
  }
}

const handleComplete = async () => {
  try {
    await ElMessageBox.confirm(
      '确认将订单状态更新为已完成吗？',
      '完成确认',
      { confirmButtonText: '确认完成', cancelButtonText: '取消', type: 'warning' }
    )
  } catch {
    return
  }
  flowing.value = true
  try {
    await updateOrderStatus(order.value.id, 2)
    ElMessage.success('订单已完成')
    loadDetail()
  } catch {
    /* 拦截器已处理 */
  } finally {
    flowing.value = false
  }
}

const submitEvaluate = async () => {
  if (!evalForm.content.trim()) {
    ElMessage.warning('请填写评价内容')
    return
  }
  evaluating.value = true
  try {
    await evaluateOrder(order.value.id, {
      score: evalForm.score,
      content: evalForm.content
    })
    ElMessage.success('评价已提交')
    loadDetail()
  } catch {
    /* 拦截器已处理 */
  } finally {
    evaluating.value = false
  }
}

const submitReport = async () => {
  if (!reportForm.type) {
    ElMessage.warning('请选择举报类型')
    return
  }
  reporting.value = true
  try {
    await reportOrder(order.value.id, reportForm)
    ElMessage.success('举报已提交')
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
.order-detail {
  min-height: 100vh;
  padding-bottom: 40px;
}

.bread {
  margin-bottom: 20px;
}

.detail-layout {
  display: grid;
  grid-template-columns: 1fr 320px;
  gap: 24px;
}

.card-base {
  background: var(--bg-card);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-md);
  transition: all 0.3s ease;
}

.info-card {
  padding: 26px 28px;
  margin-bottom: 24px;
}

.card-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 22px;
  padding-bottom: 18px;
  border-bottom: 1px solid var(--border-light);
}

.card-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 20px;
  font-weight: 700;
  color: var(--text-primary);
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 18px;
  margin-bottom: 24px;
}

.info-item {
  padding: 14px 16px;
  background: var(--bg-page);
  border-radius: var(--radius-md);
}

.info-label {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: var(--text-secondary);
  margin-bottom: 8px;
}

.info-value {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
}

.salary-item {
  background: linear-gradient(135deg, rgba(239, 68, 68, 0.08), rgba(245, 158, 11, 0.08));
}

.salary {
  font-size: 26px;
  color: var(--danger);
}

.desc-block {
  padding-top: 6px;
}

.desc-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-regular);
  margin-bottom: 10px;
}

.desc-content {
  font-size: 15px;
  line-height: 1.8;
  color: var(--text-regular);
  white-space: pre-wrap;
}

/* 时间轴 */
.timeline-card {
  padding: 24px 28px;
  margin-bottom: 24px;
}

.timeline-card .card-title {
  margin-bottom: 20px;
}

.status-timeline {
  padding-left: 4px;
}

.timeline-node {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.node-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
}

.node-desc {
  font-size: 13px;
  color: var(--text-secondary);
}

/* 评价 */
.evaluate-card {
  padding: 24px 28px;
}

.eval-content {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.eval-text {
  font-size: 15px;
  color: var(--text-regular);
  line-height: 1.7;
}

.eval-tip {
  font-size: 14px;
  color: var(--text-secondary);
  margin-bottom: 16px;
}

.eval-empty {
  padding: 10px 0;
}

/* 右侧 */
.detail-side {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.publisher-card,
.action-card {
  padding: 22px 24px;
}

.side-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 18px;
  color: var(--text-primary);
}

.publisher-info {
  display: flex;
  align-items: center;
  gap: 14px;
  margin-bottom: 16px;
}

.publisher-meta {
  flex: 1;
}

.publisher-name {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 4px;
}

.contact-btn {
  width: 100%;
}

.action-card {
  display: flex;
  flex-direction: column;
}

.action-btn {
  width: 100%;
  margin-bottom: 10px;
  margin-left: 0 !important;
}

.accept-btn {
  background: var(--gradient-primary);
  border: none;
  font-weight: 600;
  box-shadow: 0 6px 16px rgba(99, 102, 241, 0.3);
}

.accept-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 10px 22px rgba(99, 102, 241, 0.4);
}

.order-canceled {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 20px;
  color: var(--text-secondary);
  font-size: 15px;
}

@media (max-width: 900px) {
  .detail-layout {
    grid-template-columns: 1fr;
  }
  .info-grid {
    grid-template-columns: 1fr;
  }
}
</style>
