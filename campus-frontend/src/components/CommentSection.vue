<template>
  <div class="comment-section">
    <div class="comment-header">
      <h3>{{ title }} <span class="count">{{ total }}</span></h3>
    </div>

    <!-- 发表评论 -->
    <div class="comment-input">
      <el-avatar :size="36" :src="userStore.userInfo?.avatar">
        {{ userStore.userInfo?.nickname?.charAt(0) }}
      </el-avatar>
      <div class="input-area">
        <el-input
          v-model="content"
          type="textarea"
          :rows="2"
          :placeholder="placeholder"
          maxlength="500"
          show-word-limit
          resize="none"
        />
        <div class="input-action">
          <el-button type="primary" size="small" :loading="submitting" @click="submit">
            发表
          </el-button>
        </div>
      </div>
    </div>

    <!-- 评论列表 -->
    <div class="comment-list">
      <div v-for="item in list" :key="item.id" class="comment-item">
        <el-avatar :size="36" :src="item.userAvatar">
          {{ item.userName?.charAt(0) }}
        </el-avatar>
        <div class="comment-content">
          <div class="comment-top">
            <span class="comment-name">{{ item.userName }}</span>
            <span class="comment-time">{{ formatTime(item.createTime) }}</span>
          </div>
          <p class="comment-text">{{ item.content }}</p>
          <div class="comment-actions">
            <span class="action" @click="$emit('like', item)">
              <el-icon><Pointer /></el-icon>
              {{ item.likeCount || 0 }}
            </span>
            <span class="action" @click="reply(item)">
              <el-icon><ChatDotRound /></el-icon>回复
            </span>
          </div>

          <!-- 回复输入框 -->
          <div v-if="replyTarget?.id === item.id" class="reply-box">
            <el-input
              v-model="replyContent"
              size="small"
              :placeholder="`回复 ${item.userName}...`"
              @keyup.enter="submitReply"
            />
            <el-button size="small" type="primary" @click="submitReply">回复</el-button>
            <el-button size="small" @click="replyTarget = null">取消</el-button>
          </div>

          <!-- 子回复 -->
          <div v-if="item.children?.length" class="reply-list">
            <div v-for="child in item.children" :key="child.id" class="reply-item">
              <el-avatar :size="28" :src="child.userAvatar">
                {{ child.userName?.charAt(0) }}
              </el-avatar>
              <div class="reply-content">
                <span class="reply-name">{{ child.userName }}</span>
                <span v-if="child.replyToUserName" class="reply-to">
                  回复 <span class="reply-target">{{ child.replyToUserName }}</span>：
                </span>
                <span class="reply-text">{{ child.content }}</span>
                <div class="comment-actions">
                  <span class="comment-time">{{ formatTime(child.createTime) }}</span>
                  <span class="action" @click="reply(child)">回复</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <EmptyState v-if="!list.length" icon="ChatLineSquare" text="还没有评论，快来抢沙发吧" />
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Pointer, ChatDotRound } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import EmptyState from './EmptyState.vue'

const props = defineProps({
  list: { type: Array, default: () => [] },
  total: { type: Number, default: 0 },
  title: { type: String, default: '评论' },
  placeholder: { type: String, default: '说点什么吧...' }
})

const emit = defineEmits(['submit', 'reply', 'like'])

const userStore = useUserStore()
const content = ref('')
const replyContent = ref('')
const replyTarget = ref(null)
const submitting = ref(false)

const formatTime = (t) => {
  if (!t) return ''
  const d = new Date(t)
  const now = Date.now()
  const diff = now - d.getTime()
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`
  if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`
  return d.toLocaleDateString('zh-CN')
}

const submit = async () => {
  if (!content.value.trim()) {
    ElMessage.warning('请输入评论内容')
    return
  }
  if (!userStore.isLogin) {
    ElMessage.warning('请先登录')
    return
  }
  submitting.value = true
  try {
    emit('submit', { content: content.value, done: () => {
      content.value = ''
      submitting.value = false
    }})
  } catch {
    submitting.value = false
  }
}

const reply = (item) => {
  if (!userStore.isLogin) {
    ElMessage.warning('请先登录')
    return
  }
  replyTarget.value = item
  replyContent.value = ''
}

const submitReply = () => {
  if (!replyContent.value.trim()) {
    ElMessage.warning('请输入回复内容')
    return
  }
  emit('reply', {
    target: replyTarget.value,
    content: replyContent.value,
    done: () => {
      replyTarget.value = null
      replyContent.value = ''
    }
  })
}
</script>

<style scoped>
.comment-header h3 {
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 20px;
}

.count {
  color: var(--text-secondary);
  font-size: 14px;
  font-weight: 400;
}

.comment-input {
  display: flex;
  gap: 12px;
  margin-bottom: 28px;
}

.input-area {
  flex: 1;
}

.input-action {
  display: flex;
  justify-content: flex-end;
  margin-top: 8px;
}

.comment-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.comment-item {
  display: flex;
  gap: 12px;
}

.comment-content {
  flex: 1;
}

.comment-top {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 4px;
}

.comment-name {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
}

.comment-time {
  font-size: 12px;
  color: var(--text-secondary);
}

.comment-text {
  font-size: 14px;
  color: var(--text-regular);
  line-height: 1.7;
  margin-bottom: 6px;
}

.comment-actions {
  display: flex;
  gap: 18px;
  font-size: 12px;
  color: var(--text-secondary);
}

.action {
  display: flex;
  align-items: center;
  gap: 3px;
  cursor: pointer;
  transition: color 0.2s;
}

.action:hover {
  color: var(--primary);
}

.reply-box {
  display: flex;
  gap: 8px;
  margin-top: 10px;
  align-items: center;
}

.reply-list {
  margin-top: 12px;
  padding: 12px 16px;
  background: var(--bg-page);
  border-radius: var(--radius-sm);
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.reply-item {
  display: flex;
  gap: 8px;
}

.reply-content {
  flex: 1;
  font-size: 13px;
}

.reply-name {
  font-weight: 600;
  color: var(--primary);
}

.reply-to {
  color: var(--text-secondary);
}

.reply-target {
  color: var(--primary);
}

.reply-text {
  color: var(--text-regular);
}

.comment-time {
  font-size: 12px;
  color: var(--text-secondary);
}
</style>
