<template>
  <div class="course-card card-base" @click="goDetail">
    <div class="course-header">
      <span class="course-status" :class="`status-${order.status}`">{{ statusText }}</span>
      <span class="course-salary">¥{{ order.salary }}</span>
    </div>
    <div class="course-body">
      <h3 class="course-name">
        <el-icon><Reading /></el-icon>
        {{ order.courseName }}
      </h3>
      <div class="course-info">
        <div class="info-item">
          <el-icon><Clock /></el-icon>
          <span>{{ order.classTime }}</span>
        </div>
        <div class="info-item" v-if="order.location">
          <el-icon><Location /></el-icon>
          <span>{{ order.location }}</span>
        </div>
        <div class="info-item" v-if="order.courseType">
          <el-icon><Collection /></el-icon>
          <span>{{ order.courseType }}</span>
        </div>
      </div>
      <p class="course-desc" v-if="order.description">{{ order.description }}</p>
    </div>
    <div class="course-footer">
      <div class="publisher">
        <el-avatar :size="22" :src="order.publisherAvatar">
          {{ order.publisherName?.charAt(0) }}
        </el-avatar>
        <span>{{ order.publisherName }}</span>
      </div>
      <span class="view-count">
        <el-icon><View /></el-icon>{{ order.viewCount || 0 }}
      </span>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import {
  Reading, Clock, Location, Collection, View
} from '@element-plus/icons-vue'

const props = defineProps({
  order: { type: Object, required: true }
})
const router = useRouter()

const statusText = computed(() => {
  return ['待接单', '进行中', '已完成', '已取消'][props.order.status] || '待接单'
})

const goDetail = () => router.push(`/course/${props.order.id}`)
</script>

<style scoped>
.course-card {
  cursor: pointer;
  padding: 18px;
}

.course-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.course-status {
  padding: 3px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
}

.status-0 {
  background: rgba(245, 158, 11, 0.12);
  color: var(--warning);
}

.status-1 {
  background: rgba(99, 102, 241, 0.12);
  color: var(--primary);
}

.status-2 {
  background: rgba(16, 185, 129, 0.12);
  color: var(--success);
}

.status-3 {
  background: rgba(148, 163, 184, 0.15);
  color: var(--text-secondary);
}

.course-salary {
  font-size: 22px;
  font-weight: 700;
  color: var(--danger);
}

.course-name {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 12px;
}

.course-info {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  margin-bottom: 10px;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: var(--text-regular);
}

.course-desc {
  font-size: 13px;
  color: var(--text-secondary);
  line-height: 1.6;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.course-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 14px;
  padding-top: 12px;
  border-top: 1px solid var(--border-light);
}

.publisher {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: var(--text-secondary);
}

.view-count {
  display: flex;
  align-items: center;
  gap: 3px;
  font-size: 12px;
  color: var(--text-secondary);
}
</style>
