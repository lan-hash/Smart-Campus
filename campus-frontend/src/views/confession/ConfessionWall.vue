<template>
  <div class="confession-page">
    <!-- 顶部浪漫 Banner -->
    <div class="page-banner">
      <div class="banner-deco deco-heart deco-1">
        <el-icon><Star /></el-icon>
      </div>
      <div class="banner-deco deco-heart deco-2">
        <el-icon><Star /></el-icon>
      </div>
      <div class="banner-deco deco-heart deco-3">
        <el-icon><Star /></el-icon>
      </div>
      <div class="container banner-inner">
        <div class="banner-text">
          <h1 class="banner-title">
            <el-icon class="title-icon"><Sunrise /></el-icon>
            表白墙
          </h1>
          <p class="banner-desc">匿名说心事 · 把藏在心里的话说出来</p>
        </div>
        <el-button class="post-btn" size="large" round @click="openDialog">
          <el-icon><EditPen /></el-icon>
          <span>发布表白</span>
        </el-button>
      </div>
    </div>

    <div class="container page-wrap">
      <!-- 统计条 -->
      <div class="stat-bar card-base">
        <div class="stat-item">
          <el-icon class="stat-icon pink"><Sunrise /></el-icon>
          <div>
            <span class="stat-num">{{ total }}</span>
            <span class="stat-label">条表白</span>
          </div>
        </div>
        <div class="stat-divider"></div>
        <div class="stat-item">
          <el-icon class="stat-icon rose"><MagicStick /></el-icon>
          <div>
            <span class="stat-num">{{ totalLikes }}</span>
            <span class="stat-label">个心动</span>
          </div>
        </div>
        <div class="stat-divider"></div>
        <div class="stat-tip">
          <el-icon><Sunny /></el-icon>
          <span>勇敢说爱，温暖整个校园</span>
        </div>
      </div>

      <!-- 表白卡片网格 -->
      <div v-loading="loading" class="card-grid">
        <div
          v-for="(item, idx) in list"
          :key="item.id"
          class="confession-card"
          :class="`card-color-${idx % 4}`"
        >
          <!-- 顶部装饰 -->
          <div class="card-deco-top">
            <span class="deco-quote">“</span>
          </div>

          <!-- 作者信息 -->
          <div class="card-author">
            <el-avatar :size="40" :src="item.isAnonymous ? '' : item.authorAvatar">
              {{ item.isAnonymous ? '匿' : (item.authorName?.charAt(0) || '心') }}
            </el-avatar>
            <div class="author-meta">
              <span class="author-name">
                {{ item.isAnonymous ? '匿名用户' : (item.authorName || '心动同学') }}
              </span>
              <span class="author-time">{{ formatTime(item.createTime) }}</span>
            </div>
            <el-dropdown
              v-if="!item.isAnonymous && item.authorId === userStore.userId"
              trigger="click"
              @command="(cmd) => handleCommand(cmd, item)"
            >
              <el-icon class="more-icon"><MoreFilled /></el-icon>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="delete">删除</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>

          <!-- 内容 -->
          <p class="card-content" @click="toggleComment(item)">{{ item.content }}</p>

          <!-- 图片 -->
          <div class="card-images" v-if="parseImages(item.images).length">
            <el-image
              v-for="(img, i) in parseImages(item.images)"
              :key="i"
              :src="img"
              :preview-src-list="parseImages(item.images)"
              :initial-index="i"
              fit="cover"
              class="card-img"
              hide-on-click-modal
              @click.stop
            />
          </div>

          <!-- 底部操作 -->
          <div class="card-footer">
            <div
              class="footer-action"
              :class="{ liked: item.liked }"
              @click="handleLike(item)"
            >
              <el-icon><Pointer /></el-icon>
              <span>{{ item.likeCount || 0 }}</span>
            </div>
            <div class="footer-action" @click="toggleComment(item)">
              <el-icon><ChatDotRound /></el-icon>
              <span>{{ item.commentCount || 0 }}</span>
            </div>
          </div>

          <!-- 展开评论区 -->
          <transition name="comment-slide">
            <div v-if="item._expanded" class="card-comment" @click.stop>
              <CommentSection
                :list="item._comments || []"
                :total="item._commentTotal || 0"
                title="暖心评论"
                placeholder="给TA一点鼓励..."
                @submit="(payload) => submitComment(item, payload)"
                @reply="(payload) => replyCommentHandler(item, payload)"
                @like="(c) => likeCommentHandler(item, c)"
              />
            </div>
          </transition>
        </div>
      </div>

      <EmptyState
        v-if="!loading && !list.length"
        icon="Sunrise"
        text="还没有表白内容"
        sub-text="勇敢迈出第一步，说出你的心意吧"
        action-text="发布表白"
        @action="openDialog"
      />

      <!-- 分页 -->
      <div v-if="total > 0" class="pagination">
        <el-pagination
          v-model:current-page="page"
          v-model:page-size="size"
          :total="total"
          :page-sizes="[12, 24, 36]"
          layout="prev, pager, next, jumper"
          background
          @current-change="fetchList"
          @size-change="fetchList"
        />
      </div>
    </div>

    <!-- 浮动发布按钮 -->
    <div class="float-btn" @click="openDialog">
      <el-icon :size="24"><EditPen /></el-icon>
    </div>

    <!-- 发布表白对话框 -->
    <el-dialog
      v-model="dialogVisible"
      title="发布表白"
      width="540px"
      class="confession-dialog"
    >
      <el-form label-position="top">
        <el-form-item label="你的心事">
          <el-input
            v-model="form.content"
            type="textarea"
            :rows="6"
            maxlength="500"
            show-word-limit
            placeholder="把想说的话写在这里，可以是表白、感谢、倾诉..."
            resize="none"
          />
        </el-form-item>
        <el-form-item label="配图 (可选)">
          <ImageUpload v-model="form.images" :limit="3" />
        </el-form-item>
        <el-form-item>
          <div class="anon-switch">
            <el-switch
              v-model="form.isAnonymous"
              active-color="#ec4899"
              inactive-color="#cbd5e1"
            />
            <div class="anon-text">
              <span class="anon-label">匿名发布</span>
              <span class="anon-desc">开启后他人无法看到你的身份</span>
            </div>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submit">
          <el-icon><Promotion /></el-icon>发布
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Sunrise, EditPen, Star, Pointer, ChatDotRound, MoreFilled,
  Sunny, MagicStick, Promotion
} from '@element-plus/icons-vue'
import {
  getConfessions, createConfession, deleteConfession,
  toggleConfessionLike, getConfessionComments, addConfessionComment
} from '@/api/confession'
import { useUserStore } from '@/stores/user'
import CommentSection from '@/components/CommentSection.vue'
import EmptyState from '@/components/EmptyState.vue'
import ImageUpload from '@/components/ImageUpload.vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const list = ref([])
const loading = ref(false)
const page = ref(1)
const size = ref(12)
const total = ref(0)

const dialogVisible = ref(false)
const submitting = ref(false)

const form = reactive({
  content: '',
  images: [],
  isAnonymous: true
})

const totalLikes = computed(() =>
  list.value.reduce((sum, i) => sum + (i.likeCount || 0), 0)
)

const parseImages = (images) => {
  if (!images) return []
  try {
    const arr = typeof images === 'string' ? JSON.parse(images) : images
    return Array.isArray(arr) ? arr : []
  } catch {
    return []
  }
}

const formatTime = (t) => {
  if (!t) return ''
  const d = new Date(t)
  const now = Date.now()
  const diff = now - d.getTime()
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`
  if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`
  if (diff < 604800000) return `${Math.floor(diff / 86400000)}天前`
  return d.toLocaleDateString('zh-CN')
}

const fetchList = async () => {
  loading.value = true
  try {
    const data = await getConfessions({ page: page.value, size: size.value })
    list.value = (data.records || []).map((i) => ({ ...i, _expanded: false, _comments: [], _commentTotal: 0 }))
    total.value = data.total || 0
  } catch (e) {
    ElMessage.error('加载表白列表失败')
  } finally {
    loading.value = false
  }
}

const openDialog = () => {
  if (!userStore.isLogin) {
    ElMessage.warning('请先登录后再发布表白')
    router.push({ path: '/login', query: { redirect: route.fullPath } })
    return
  }
  form.content = ''
  form.images = []
  form.isAnonymous = true
  dialogVisible.value = true
}

const submit = async () => {
  if (!form.content.trim()) {
    ElMessage.warning('请输入表白内容')
    return
  }
  submitting.value = true
  try {
    await createConfession({
      content: form.content,
      images: JSON.stringify(form.images || []),
      isAnonymous: form.isAnonymous
    })
    ElMessage.success('发布成功，愿你的心意被听见')
    dialogVisible.value = false
    page.value = 1
    fetchList()
  } catch (e) {
    ElMessage.error('发布失败')
  } finally {
    submitting.value = false
  }
}

const handleLike = async (item) => {
  if (!userStore.isLogin) {
    ElMessage.warning('请先登录')
    router.push({ path: '/login', query: { redirect: route.fullPath } })
    return
  }
  try {
    await toggleConfessionLike(item.id)
    item.liked = !item.liked
    item.likeCount += item.liked ? 1 : -1
  } catch (e) {
    // ignore
  }
}

const handleCommand = (cmd, item) => {
  if (cmd === 'delete') removeConfession(item)
}

const removeConfession = (item) => {
  ElMessageBox.confirm('确定要删除这条表白吗？', '删除确认', {
    type: 'warning',
    confirmButtonText: '确认删除',
    cancelButtonText: '取消'
  })
    .then(async () => {
      try {
        await deleteConfession(item.id)
        ElMessage.success('删除成功')
        fetchList()
      } catch (e) {
        ElMessage.error('删除失败')
      }
    })
    .catch(() => {})
}

// 评论展开
const toggleComment = async (item) => {
  item._expanded = !item._expanded
  if (item._expanded && !item._comments.length) {
    await fetchComments(item)
  }
}

const fetchComments = async (item) => {
  try {
    const data = await getConfessionComments(item.id, { page: 1, size: 20 })
    item._comments = data.records || []
    item._commentTotal = data.total || 0
  } catch (e) {
    // ignore
  }
}

const submitComment = (item, { content, done }) => {
  if (!userStore.isLogin) {
    ElMessage.warning('请先登录')
    done?.()
    return
  }
  addConfessionComment(item.id, { content })
    .then(() => {
      item._comments.unshift({
        id: Date.now(),
        content,
        userName: userStore.userInfo?.nickname,
        userAvatar: userStore.userInfo?.avatar,
        createTime: new Date().toISOString(),
        likeCount: 0,
        children: []
      })
      item._commentTotal += 1
      item.commentCount = (item.commentCount || 0) + 1
      ElMessage.success('评论成功')
      done?.()
    })
    .catch(() => {
      ElMessage.error('评论失败')
      done?.()
    })
}

const replyCommentHandler = (item, { target, content, done }) => {
  addConfessionComment(item.id, { content, parentId: target.id })
    .then(() => {
      if (!target.children) target.children = []
      target.children.push({
        id: Date.now(),
        content,
        userName: userStore.userInfo?.nickname,
        userAvatar: userStore.userInfo?.avatar,
        createTime: new Date().toISOString()
      })
      ElMessage.success('回复成功')
      done?.()
    })
    .catch(() => {
      ElMessage.error('回复失败')
      done?.()
    })
}

const likeCommentHandler = (item, c) => {
  if (!userStore.isLogin) {
    ElMessage.warning('请先登录')
    return
  }
  // 表白评论点赞复用 addConfessionComment 不合适，这里仅前端计数
  c.likeCount = (c.likeCount || 0) + 1
  ElMessage.success('已点赞')
}

onMounted(() => {
  fetchList()
})
</script>

<style scoped>
.confession-page {
  min-height: calc(100vh - 64px);
}

/* 顶部浪漫粉紫渐变 Banner */
.page-banner {
  position: relative;
  background: linear-gradient(135deg, #ec4899 0%, #a855f7 100%);
  overflow: hidden;
  padding: 50px 0;
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
  font-size: 34px;
  font-weight: 700;
  color: #fff;
  margin-bottom: 10px;
  text-shadow: 0 2px 10px rgba(0, 0, 0, 0.15);
}

.title-icon {
  font-size: 38px;
}

.banner-desc {
  font-size: 15px;
  color: rgba(255, 255, 255, 0.95);
  letter-spacing: 1px;
}

.post-btn {
  background: #fff;
  color: #ec4899;
  border: none;
  font-weight: 600;
  padding: 0 28px;
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.18);
  transition: all 0.3s;
}

.post-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 12px 28px rgba(0, 0, 0, 0.25);
  background: #fff;
  color: #db2777;
}

.banner-deco {
  position: absolute;
  color: rgba(255, 255, 255, 0.25);
  animation: float 6s ease-in-out infinite;
}

.deco-heart {
  font-size: 40px;
}

.deco-1 {
  top: 20%;
  left: 8%;
  animation-delay: 0s;
}

.deco-2 {
  top: 60%;
  right: 12%;
  font-size: 28px;
  animation-delay: 1.5s;
}

.deco-3 {
  bottom: 15%;
  left: 25%;
  font-size: 22px;
  animation-delay: 3s;
}

@keyframes float {
  0%, 100% { transform: translateY(0) rotate(0deg); }
  50% { transform: translateY(-16px) rotate(10deg); }
}

/* 统计条 */
.stat-bar {
  display: flex;
  align-items: center;
  gap: 24px;
  padding: 20px 28px;
  margin-bottom: 24px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 12px;
}

.stat-icon {
  font-size: 28px;
  padding: 8px;
  border-radius: var(--radius-sm);
}

.stat-icon.pink {
  color: #ec4899;
  background: rgba(236, 72, 153, 0.1);
}

.stat-icon.rose {
  color: #a855f7;
  background: rgba(168, 85, 247, 0.1);
}

.stat-item div {
  display: flex;
  flex-direction: column;
}

.stat-num {
  font-size: 22px;
  font-weight: 700;
  color: var(--text-primary);
}

.stat-label {
  font-size: 12px;
  color: var(--text-secondary);
}

.stat-divider {
  width: 1px;
  height: 32px;
  background: var(--border-light);
}

.stat-tip {
  margin-left: auto;
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: var(--text-secondary);
}

.stat-tip .el-icon {
  color: var(--warning);
}

/* 卡片网格 */
.card-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
  min-height: 200px;
}

.confession-card {
  background: var(--bg-card);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-md);
  padding: 22px;
  position: relative;
  overflow: hidden;
  transition: all 0.3s;
  animation: fadeInUp 0.5s ease forwards;
}

.confession-card:hover {
  box-shadow: var(--shadow-hover);
  transform: translateY(-4px);
}

.card-deco-top {
  position: absolute;
  top: -6px;
  right: 16px;
  font-size: 60px;
  line-height: 1;
  font-family: Georgia, serif;
  opacity: 0.12;
  pointer-events: none;
}

/* 不同卡片配色点缀 */
.card-color-0 .deco-quote { color: #ec4899; }
.card-color-1 .deco-quote { color: #a855f7; }
.card-color-2 .deco-quote { color: #f43f5e; }
.card-color-3 .deco-quote { color: #d946ef; }

.card-author {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 14px;
}

.card-author :deep(.el-avatar) {
  background: linear-gradient(135deg, #fce7f3, #f3e8ff);
  color: #ec4899;
  font-weight: 600;
}

.author-meta {
  display: flex;
  flex-direction: column;
  gap: 2px;
  flex: 1;
}

.author-name {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
}

.author-time {
  font-size: 12px;
  color: var(--text-secondary);
}

.more-icon {
  cursor: pointer;
  color: var(--text-secondary);
  font-size: 18px;
  padding: 4px;
  border-radius: 50%;
  transition: all 0.2s;
}

.more-icon:hover {
  background: var(--bg-page);
  color: var(--primary);
}

.card-content {
  font-size: 14px;
  line-height: 1.8;
  color: var(--text-regular);
  margin-bottom: 14px;
  cursor: pointer;
  white-space: pre-wrap;
  word-break: break-word;
}

.card-images {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 6px;
  margin-bottom: 14px;
}

.card-img {
  width: 100%;
  height: 80px;
  border-radius: 8px;
  cursor: pointer;
}

.card-footer {
  display: flex;
  gap: 18px;
  padding-top: 12px;
  border-top: 1px solid var(--border-light);
}

.footer-action {
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 13px;
  color: var(--text-secondary);
  cursor: pointer;
  transition: all 0.25s;
}

.footer-action:hover {
  color: #ec4899;
}

.footer-action.liked {
  color: #ec4899;
}

.footer-action.liked .el-icon {
  animation: heart-beat 0.4s ease;
}

@keyframes heart-beat {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.3); }
}

/* 展开评论 */
.card-comment {
  margin-top: 14px;
  padding-top: 14px;
  border-top: 1px dashed var(--border-light);
}

.comment-slide-enter-active,
.comment-slide-leave-active {
  transition: all 0.3s ease;
  max-height: 800px;
  overflow: hidden;
}

.comment-slide-enter-from,
.comment-slide-leave-to {
  opacity: 0;
  max-height: 0;
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 36px;
}

/* 浮动按钮 */
.float-btn {
  position: fixed;
  right: 32px;
  bottom: 110px;
  width: 56px;
  height: 56px;
  border-radius: 50%;
  background: linear-gradient(135deg, #ec4899, #a855f7);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  box-shadow: 0 8px 24px rgba(236, 72, 153, 0.4);
  z-index: 50;
  transition: all 0.3s;
}

.float-btn:hover {
  transform: scale(1.1) rotate(-8deg);
  box-shadow: 0 12px 32px rgba(236, 72, 153, 0.5);
}

/* 对话框匿名开关 */
.anon-switch {
  display: flex;
  align-items: center;
  gap: 12px;
}

.anon-text {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.anon-label {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
}

.anon-desc {
  font-size: 12px;
  color: var(--text-secondary);
}

@media (max-width: 980px) {
  .card-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  .stat-tip {
    display: none;
  }
}

@media (max-width: 640px) {
  .card-grid {
    grid-template-columns: 1fr;
  }
  .banner-inner {
    flex-direction: column;
    align-items: flex-start;
  }
  .stat-bar {
    flex-wrap: wrap;
    gap: 16px;
  }
}
</style>
