<template>
  <div class="post-card card-base" @click="goDetail">
    <div class="post-cover" v-if="coverImage">
      <img :src="coverImage" :alt="post.title" />
      <span v-if="post.isTop" class="badge badge-top">置顶</span>
      <span v-else-if="post.isEssence" class="badge badge-essence">精华</span>
    </div>
    <div class="post-body">
      <h3 class="post-title">{{ post.title }}</h3>
      <p class="post-excerpt">{{ stripContent }}</p>
      <div class="post-footer">
        <div class="post-author">
          <el-avatar :size="24" :src="post.authorAvatar">
            {{ post.authorName?.charAt(0) }}
          </el-avatar>
          <span>{{ post.authorName }}</span>
          <span v-if="post.aiCategory" class="ai-tag">
            <el-icon><MagicStick /></el-icon>{{ post.aiCategory }}
          </span>
        </div>
        <div class="post-stats">
          <span><el-icon><View /></el-icon>{{ post.viewCount || 0 }}</span>
          <span><el-icon><Pointer /></el-icon>{{ post.likeCount || 0 }}</span>
          <span><el-icon><ChatDotRound /></el-icon>{{ post.commentCount || 0 }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { MagicStick, View, Pointer, ChatDotRound } from '@element-plus/icons-vue'

const props = defineProps({
  post: { type: Object, required: true }
})
const router = useRouter()

const coverImage = computed(() => {
  if (props.post.images) {
    try {
      const arr = typeof props.post.images === 'string'
        ? JSON.parse(props.post.images) : props.post.images
      return Array.isArray(arr) && arr.length ? arr[0] : null
    } catch {
      return null
    }
  }
  return null
})

const stripContent = computed(() => {
  const c = props.post.content || ''
  return c.replace(/<[^>]+>/g, '').slice(0, 80) + (c.length > 80 ? '...' : '')
})

const goDetail = () => router.push(`/forum/post/${props.post.id}`)
</script>

<style scoped>
.post-card {
  cursor: pointer;
  overflow: hidden;
}

.post-cover {
  position: relative;
  height: 180px;
  overflow: hidden;
}

.post-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.4s;
}

.post-card:hover .post-cover img {
  transform: scale(1.06);
}

.badge {
  position: absolute;
  top: 12px;
  left: 12px;
  padding: 3px 10px;
  border-radius: 20px;
  font-size: 12px;
  color: #fff;
  font-weight: 600;
}

.badge-top {
  background: linear-gradient(135deg, #f59e0b, #ef4444);
}

.badge-essence {
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
}

.post-body {
  padding: 16px;
}

.post-title {
  font-size: 16px;
  font-weight: 600;
  line-height: 1.5;
  margin-bottom: 8px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.post-excerpt {
  font-size: 13px;
  color: var(--text-secondary);
  line-height: 1.6;
  margin-bottom: 14px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.post-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.post-author {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: var(--text-secondary);
}

.ai-tag {
  display: inline-flex;
  align-items: center;
  gap: 2px;
  padding: 1px 8px;
  border-radius: 10px;
  background: rgba(99, 102, 241, 0.1);
  color: var(--primary);
  font-size: 11px;
  margin-left: 4px;
}

.post-stats {
  display: flex;
  gap: 12px;
  font-size: 12px;
  color: var(--text-secondary);
}

.post-stats span {
  display: flex;
  align-items: center;
  gap: 3px;
}
</style>
