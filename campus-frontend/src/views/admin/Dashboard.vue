<template>
  <div class="dashboard">
    <!-- 欢迎横幅 -->
    <div class="welcome-banner">
      <div class="welcome-text">
        <h1>欢迎回来，管理员</h1>
        <p>今日平台运行平稳，以下是核心数据概览</p>
      </div>
      <div class="welcome-time">{{ currentTime }}</div>
    </div>

    <!-- 统计卡片 -->
    <div class="stat-cards">
      <div
        v-for="card in statCards"
        :key="card.key"
        class="stat-card"
        :style="{ background: card.bg }"
      >
        <div class="stat-icon">
          <el-icon><component :is="card.icon" /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ card.value }}</div>
          <div class="stat-label">{{ card.label }}</div>
        </div>
        <div class="stat-trend" :class="card.trend >= 0 ? 'up' : 'down'">
          <el-icon><CaretTop v-if="card.trend >= 0" /><CaretBottom v-else /></el-icon>
          {{ Math.abs(card.trend || 0) }}%
        </div>
      </div>
    </div>

    <!-- 图表区 -->
    <div class="chart-section">
      <div class="chart-card">
        <div class="chart-header">
          <h3 class="chart-title">
            <el-icon><TrendCharts /></el-icon>用户增长趋势
          </h3>
          <el-tag type="info" effect="plain">近 7 天</el-tag>
        </div>
        <div ref="lineChartRef" class="chart-box"></div>
      </div>

      <div class="chart-card">
        <div class="chart-header">
          <h3 class="chart-title">
            <el-icon><PieChart /></el-icon>各模块数据占比
          </h3>
          <el-tag type="info" effect="plain">总量分布</el-tag>
        </div>
        <div ref="pieChartRef" class="chart-box"></div>
      </div>
    </div>

    <!-- 待办事项 -->
    <div class="todo-section">
      <div class="chart-card">
        <div class="chart-header">
          <h3 class="chart-title">
            <el-icon><Warning /></el-icon>待处理事项
          </h3>
        </div>
        <div class="todo-list">
          <div v-for="todo in todoList" :key="todo.label" class="todo-item">
            <div class="todo-info">
              <el-icon class="todo-icon" :style="{ color: todo.color }">
                <component :is="todo.icon" />
              </el-icon>
              <span class="todo-label">{{ todo.label }}</span>
            </div>
            <div class="todo-right">
              <span class="todo-count">{{ todo.count }}</span>
              <el-button text type="primary" @click="goTo(todo.path)">
                去处理<el-icon><ArrowRight /></el-icon>
              </el-button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import * as echarts from 'echarts'
import {
  User, Document, ShoppingCart, Reading, Warning,
  TrendCharts, PieChart, CaretTop, CaretBottom,
  ArrowRight, Bell, ChatLineSquare, EditPen
} from '@element-plus/icons-vue'
import { getDashboardStats } from '@/api/admin'

const router = useRouter()

const currentTime = ref('')
const lineChartRef = ref(null)
const pieChartRef = ref(null)
let lineChart = null
let pieChart = null
let timer = null

const statCards = ref([
  { key: 'user', label: '注册用户', value: 0, icon: User, bg: 'linear-gradient(135deg, #6366f1, #818cf8)', trend: 0 },
  { key: 'post', label: '论坛帖子', value: 0, icon: Document, bg: 'linear-gradient(135deg, #8b5cf6, #a78bfa)', trend: 0 },
  { key: 'product', label: '二手商品', value: 0, icon: ShoppingCart, bg: 'linear-gradient(135deg, #06b6d4, #22d3ee)', trend: 0 },
  { key: 'order', label: '代课订单', value: 0, icon: Reading, bg: 'linear-gradient(135deg, #f59e0b, #fbbf24)', trend: 0 },
  { key: 'report', label: '待处理举报', value: 0, icon: Warning, bg: 'linear-gradient(135deg, #ef4444, #f87171)', trend: 0 }
])

const todoList = ref([
  { label: '待审核内容', count: 0, icon: EditPen, color: '#f59e0b', path: '/admin/review' },
  { label: '待处理举报', count: 0, icon: Warning, color: '#ef4444', path: '/admin/reports' },
  { label: '用户反馈', count: 0, icon: ChatLineSquare, color: '#6366f1', path: '/admin/users' },
  { label: '系统公告', count: 0, icon: Bell, color: '#8b5cf6', path: '/admin/notice' }
])

const updateTime = () => {
  const d = new Date()
  const week = ['周日', '周一', '周二', '周三', '周四', '周五', '周六'][d.getDay()]
  currentTime.value = `${d.getFullYear()}年${d.getMonth() + 1}月${d.getDate()}日 ${week} ${d.toLocaleTimeString('zh-CN')}`
}

const loadStats = async () => {
  try {
    const data = await getDashboardStats()
    const map = {
      user: 'userCount', post: 'postCount', product: 'productCount',
      order: 'orderCount', report: 'reportCount'
    }
    statCards.value.forEach((c) => {
      c.value = data[map[c.key]] ?? 0
      c.trend = data[`${map[c.key]}Trend`] ?? 0
    })
    if (data.todoList) {
      todoList.value = data.todoList
    } else {
      todoList.value[0].count = data.pendingReview || 0
      todoList.value[1].count = data.reportCount || 0
    }
    initCharts(data)
  } catch {
    // 接口未就绪，使用假数据兜底
    const mock = {
      userCount: 1286, postCount: 3452, productCount: 878,
      orderCount: 426, reportCount: 12,
      userTrend: 8.5, postTrend: 12.3, productTrend: -3.2, orderTrend: 6.8, reportTrend: -15
    }
    const map = {
      user: 'userCount', post: 'postCount', product: 'productCount',
      order: 'orderCount', report: 'reportCount'
    }
    statCards.value.forEach((c) => {
      c.value = mock[map[c.key]]
      c.trend = mock[`${map[c.key]}Trend`]
    })
    todoList.value[0].count = 18
    todoList.value[1].count = 12
    initCharts(null)
  }
}

const initCharts = (data) => {
  nextTick(() => {
    initLineChart(data)
    initPieChart(data)
  })
}

const initLineChart = (data) => {
  if (!lineChartRef.value) return
  if (lineChart) lineChart.dispose()
  lineChart = echarts.init(lineChartRef.value)

  const days = ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
  const userData = data?.userTrendData || [42, 65, 58, 89, 102, 156, 134]
  const activeData = data?.activeTrendData || [320, 432, 501, 634, 790, 930, 860]

  lineChart.setOption({
    tooltip: { trigger: 'axis' },
    legend: { data: ['新增用户', '活跃用户'], bottom: 0 },
    grid: { left: '3%', right: '4%', bottom: '12%', top: '8%', containLabel: true },
    xAxis: { type: 'category', data: days, boundaryGap: false },
    yAxis: { type: 'value' },
    series: [
      {
        name: '新增用户',
        type: 'line',
        smooth: true,
        data: userData,
        itemStyle: { color: '#6366f1' },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(99, 102, 241, 0.4)' },
            { offset: 1, color: 'rgba(99, 102, 241, 0.05)' }
          ])
        }
      },
      {
        name: '活跃用户',
        type: 'line',
        smooth: true,
        data: activeData,
        itemStyle: { color: '#8b5cf6' },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(139, 92, 246, 0.4)' },
            { offset: 1, color: 'rgba(139, 92, 246, 0.05)' }
          ])
        }
      }
    ]
  })
}

const initPieChart = (data) => {
  if (!pieChartRef.value) return
  if (pieChart) pieChart.dispose()
  pieChart = echarts.init(pieChartRef.value)

  const pieData = data?.moduleData || [
    { value: 3452, name: '论坛帖子' },
    { value: 878, name: '二手商品' },
    { value: 426, name: '代课订单' },
    { value: 568, name: '表白墙' }
  ]

  pieChart.setOption({
    tooltip: { trigger: 'item', formatter: '{a} <br/>{b}: {c} ({d}%)' },
    legend: { orient: 'vertical', left: 'left', top: 'center' },
    series: [
      {
        name: '模块数据',
        type: 'pie',
        radius: ['45%', '70%'],
        center: ['60%', '50%'],
        avoidLabelOverlap: false,
        itemStyle: { borderRadius: 10, borderColor: '#fff', borderWidth: 2 },
        label: { show: false, position: 'center' },
        emphasis: {
          label: { show: true, fontSize: 18, fontWeight: 'bold' }
        },
        labelLine: { show: false },
        data: pieData,
        color: ['#6366f1', '#06b6d4', '#f59e0b', '#8b5cf6']
      }
    ]
  })
}

const goTo = (path) => {
  router.push(path)
}

const handleResize = () => {
  lineChart?.resize()
  pieChart?.resize()
}

onMounted(() => {
  updateTime()
  timer = setInterval(updateTime, 1000)
  loadStats()
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  if (timer) clearInterval(timer)
  window.removeEventListener('resize', handleResize)
  lineChart?.dispose()
  pieChart?.dispose()
})
</script>

<style scoped>
.dashboard {
  padding-bottom: 20px;
}

/* 欢迎横幅 */
.welcome-banner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 28px 32px;
  background: var(--gradient-hero);
  border-radius: var(--radius-lg);
  color: #fff;
  margin-bottom: 24px;
  position: relative;
  overflow: hidden;
}

.welcome-banner::before {
  content: '';
  position: absolute;
  top: -50%;
  right: -10%;
  width: 300px;
  height: 300px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 50%;
}

.welcome-text {
  position: relative;
  z-index: 1;
}

.welcome-banner h1 {
  font-size: 24px;
  font-weight: 700;
  margin-bottom: 6px;
}

.welcome-banner p {
  font-size: 14px;
  opacity: 0.9;
}

.welcome-time {
  font-size: 14px;
  opacity: 0.95;
  position: relative;
  z-index: 1;
}

/* 统计卡片 */
.stat-cards {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 18px;
  margin-bottom: 24px;
}

.stat-card {
  padding: 22px 20px;
  border-radius: var(--radius-lg);
  color: #fff;
  display: flex;
  align-items: center;
  gap: 14px;
  position: relative;
  box-shadow: var(--shadow-md);
  transition: all 0.3s;
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-hover);
}

.stat-icon {
  width: 48px;
  height: 48px;
  background: rgba(255, 255, 255, 0.22);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  flex-shrink: 0;
}

.stat-info {
  flex: 1;
  min-width: 0;
}

.stat-value {
  font-size: 26px;
  font-weight: 800;
  line-height: 1.2;
}

.stat-label {
  font-size: 13px;
  opacity: 0.92;
  margin-top: 2px;
}

.stat-trend {
  display: flex;
  align-items: center;
  gap: 1px;
  font-size: 12px;
  font-weight: 600;
  background: rgba(255, 255, 255, 0.2);
  padding: 2px 8px;
  border-radius: 20px;
}

/* 图表区 */
.chart-section {
  display: grid;
  grid-template-columns: 1.5fr 1fr;
  gap: 20px;
  margin-bottom: 24px;
}

.chart-card {
  background: var(--bg-card);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-md);
  padding: 22px 24px;
}

.chart-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.chart-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
}

.chart-box {
  height: 320px;
}

/* 待办 */
.todo-list {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.todo-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 12px;
  border-radius: var(--radius-sm);
  transition: all 0.2s;
}

.todo-item:hover {
  background: var(--bg-page);
}

.todo-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.todo-icon {
  font-size: 22px;
}

.todo-label {
  font-size: 14px;
  color: var(--text-regular);
  font-weight: 500;
}

.todo-right {
  display: flex;
  align-items: center;
  gap: 14px;
}

.todo-count {
  font-size: 20px;
  font-weight: 700;
  color: var(--primary);
}

@media (max-width: 1200px) {
  .stat-cards {
    grid-template-columns: repeat(3, 1fr);
  }
  .chart-section {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .stat-cards {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
