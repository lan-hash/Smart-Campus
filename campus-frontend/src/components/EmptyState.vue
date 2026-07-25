<template>
  <div class="empty-state">
    <el-icon><component :is="icon" /></el-icon>
    <p class="empty-text">{{ text }}</p>
    <p v-if="subText" class="empty-sub">{{ subText }}</p>
    <el-button v-if="actionText" type="primary" round @click="$emit('action')">
      {{ actionText }}
    </el-button>
  </div>
</template>

<script setup>
defineProps({
  icon: { type: [String, Object], default: 'Box' },
  text: { type: String, default: '暂无数据' },
  subText: { type: String, default: '' },
  actionText: { type: String, default: '' }
})
defineEmits(['action'])
</script>

<style scoped>
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 60px 20px;
}

.empty-state .el-icon {
  font-size: 64px;
  color: #cbd5e1;
  margin-bottom: 20px;
}

.empty-text {
  font-size: 16px;
  color: var(--text-secondary);
  font-weight: 500;
}

.empty-sub {
  font-size: 13px;
  color: #cbd5e1;
  margin-top: 8px;
  margin-bottom: 20px;
}
</style>
