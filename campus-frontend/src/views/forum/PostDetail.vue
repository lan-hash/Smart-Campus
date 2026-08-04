<template>
  <div class="post-detail-page">
    <div class="container page-wrap" v-loading="loading">
      <div class="detail-layout" v-if="post">
        <!-- 主内容 -->
        <div class="detail-main">
          <!-- 面包屑 -->
          <div class="breadcrumb">
            <el-button text @click="goBack">
              <el-icon><ArrowLeft /></el-icon>返回
            </el-button>
            <el-breadcrumb separator="/">
              <el-breadcrumb-item :to="{ path: forumPath }">{{ forumTitle }}</el-breadcrumb-item>
              <el-breadcrumb-item>帖子详情</el-breadcrumb-item>
            </el-breadcrumb>
          </div>

          <!-- 帖子头部 -->
          <div class="post-header card-base">
            <div class="header-top">
              <span v-if="post.isTop" class="badge badge-top">置顶</span>
              <span v-if="post.isEssence" class="badge badge-essence">精华</span>
              <span v-if="post.categoryName" class="cat-tag">
                <el-icon><Folder /></el-icon>{{ post.categoryName }}
              </span>
              <span v-if="post.aiCategory" class="ai-tag">
                <el-icon><MagicStick /></el-icon>AI分类 · {{ post.aiCategory }}
              </span>
            </div>
            <h1 class="post-title">{{ post.title }}</h1>

            <div class="author-row">
              <div class="author-info" @click="goAuthor">
                <el-avatar :size="44" :src="post.authorAvatar">
                  {{ post.authorName?.charAt(0) }}
                </el-avatar>
                <div class="author-meta">
                  <span class="author-name">{{ post.authorName }}</span>
                  <span class="author-time">
                    发布于 {{ formatTime(post.createTime) }} · {{ post.viewCount || 0 }} 浏览
                  </span>
                </div>
              </div>

              <div class="header-actions" v-if="isAuthor">
                <el-button size="small" plain @click="editPost">
                  <el-icon><Edit /></el-icon>编辑
                </el-button>
                <el-button size="small" plain type="danger" @click="removePost">
                  <el-icon><Delete /></el-icon>删除
                </el-button>
              </div>
            </div>
          </div>

          <!-- 正文内容 -->
          <div class="post-content card-base">
            <MdPreview :model-value="post.content" />
            <div class="content-images" v-if="imageList.length">
              <el-image
                v-for="(img, i) in imageList"
                :key="i"
                :src="img"
                :preview-src-list="imageList"
                :initial-index="i"
                fit="cover"
                class="content-img"
                hide-on-click-modal
              />
            </div>
          </div>

          <!-- 操作栏 -->
          <div class="action-bar card-base">
            <div
              class="action-item"
              :class="{ active: post.liked }"
              @click="handleLike"
            >
              <el-icon><Pointer /></el-icon>
              <span>{{ post.liked ? '已点赞' : '点赞' }}</span>
              <span class="action-count">{{ post.likeCount || 0 }}</span>
            </div>
            <div
              class="action-item"
              :class="{ active: post.collected }"
              @click="handleCollect"
            >
              <el-icon><Star /></el-icon>
              <span>{{ post.collected ? '已收藏' : '收藏' }}</span>
              <span class="action-count">{{ post.collectCount || 0 }}</span>
            </div>
            <div class="action-item" @click="scrollToComment">
              <el-icon><ChatDotRound /></el-icon>
              <span>评论</span>
              <span class="action-count">{{ post.commentCount || 0 }}</span>
            </div>
            <div class="action-item report" @click="handleReport">
              <el-icon><Warning /></el-icon>
              <span>举报</span>
            </div>
          </div>

          <!-- 评论区 -->
          <div ref="commentRef" class="comment-wrap card-base">
            <CommentSection
              :list="commentList"
              :total="commentTotal"
              title="全部评论"
              placeholder="说点什么吧..."
              @submit="submitComment"
              @reply="replyCommentHandler"
              @like="likeCommentHandler"
            />

            <div v-if="commentTotal > commentList.length" class="load-more">
              <el-button text @click="loadMoreComments">加载更多评论</el-button>
            </div>
          </div>
        </div>

        <!-- 侧边栏 -->
        <aside class="detail-side">
          <!-- 作者信息卡 -->
          <div class="side-card author-card">
            <div class="author-card-top">
              <el-avatar :size="64" :src="post.authorAvatar">
                {{ post.authorName?.charAt(0) }}
              </el-avatar>
              <h3 class="author-card-name">{{ post.authorName }}</h3>
              <p class="author-card-desc">{{ post.authorBio || '这个人很神秘，什么都没留下' }}</p>
            </div>
            <div class="author-stats">
              <div class="stat">
                <span class="stat-num">{{ post.authorPostCount || 0 }}</span>
                <span class="stat-label">帖子</span>
              </div>
              <div class="stat">
                <span class="stat-num">{{ post.authorFollowerCount || 0 }}</span>
                <span class="stat-label">粉丝</span>
              </div>
              <div class="stat">
                <span class="stat-num">{{ post.authorLikeCount || 0 }}</span>
                <span class="stat-label">获赞</span>
              </div>
            </div>
            <el-button
              v-if="!isAuthor"
              class="follow-btn"
              :type="followed ? 'info' : 'primary'"
              :plain="followed"
              round
              @click="toggleFollow"
            >
              {{ followed ? '已关注' : '+ 关注' }}
            </el-button>
            <el-button v-else class="follow-btn" round @click="goAuthor">
              查看主页
            </el-button>
          </div>

          <!-- 相关推荐 -->
          <div class="side-card">
            <h3 class="side-title">
              <el-icon><MagicStick /></el-icon>相关推荐
            </h3>
            <ul class="relate-list">
              <li
                v-for="item in relatedList"
                :key="item.id"
                class="relate-item"
                @click="goRelate(item.id)"
              >
                <p class="relate-title">{{ item.title }}</p>
                <div class="relate-meta">
                  <span><el-icon><View /></el-icon>{{ item.viewCount || 0 }}</span>
                  <span><el-icon><Pointer /></el-icon>{{ item.likeCount || 0 }}</span>
                </div>
              </li>
            </ul>
            <EmptyState
              v-if="!relatedList.length"
              icon="Document"
              text="暂无推荐"
            />
          </div>
        </aside>
      </div>

      <EmptyState
        v-if="!loading && !post"
        icon="DocumentDelete"
        text="帖子不存在或已被删除"
        sub-text="去看看其他帖子吧"
        action-text="返回论坛"
        @action="goBack"
      />
    </div>

    <!-- 举报弹框 -->
    <el-dialog v-model="reportVisible" title="举报帖子" width="440px">
      <el-form label-position="top">
        <el-form-item label="举报原因">
          <el-radio-group v-model="reportReason" class="report-group">
            <el-radio label="垃圾广告">垃圾广告</el-radio>
            <el-radio label="违规内容">违规内容</el-radio>
            <el-radio label="抄袭侵权">抄袭侵权</el-radio>
            <el-radio label="引战谩骂">引战谩骂</el-radio>
            <el-radio label="其他">其他</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="补充说明">
          <el-input
            v-model="reportDesc"
            type="textarea"
            :rows="3"
            maxlength="200"
            show-word-limit
            placeholder="请描述具体情况..."
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="reportVisible = false">取消</el-button>
        <el-button type="primary" @click="submitReport">提交举报</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch, nextTick } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft, Folder, MagicStick, Edit, Delete, Pointer, Star, ChatDotRound, Warning, View } from '@element-plus/icons-vue'
import {
  getPostDetail, toggleLike, toggleCollect, deletePost,
  getComments, addComment, replyComment, likeComment, getPosts
} from '@/api/forum'
import { useUserStore } from '@/stores/user'
import CommentSection from '@/components/CommentSection.vue'
import EmptyState from '@/components/EmptyState.vue'
import MdPreview from '@/components/MdPreview.vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const post = ref(null)
const loading = ref(false)
const commentList = ref([])
const commentTotal = ref(0)
const commentPage = ref(1)
const commentSize = ref(10)
const relatedList = ref([])
const followed = ref(false)
const commentRef = ref(null)

// 举报
const reportVisible = ref(false)
const reportReason = ref('垃圾广告')
const reportDesc = ref('')

const postId = computed(() => route.params.id)

const isAuthor = computed(
  () => userStore.isLogin && post.value && userStore.userId === post.value.authorId
)

const forumPath = computed(() => {
  if (!post.value) return '/forum/study'
  return post.value.type === 1 ? '/forum/game' : '/forum/study'
})

const forumTitle = computed(() => {
  if (!post.value) return '学习论坛'
  return post.value.type === 1 ? '游戏社区' : '学习论坛'
})

const imageList = computed(() => {
  if (!post.value?.images) return []
  try {
    const arr = typeof post.value.images === 'string'
      ? JSON.parse(post.value.images)
      : post.value.images
    return Array.isArray(arr) ? arr : []
  } catch {
    return []
  }
})

const formatTime = (t) => {
  if (!t) return ''
  const d = new Date(t)
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
}

const fetchDetail = async () => {
  loading.value = true
  try {
    const data = await getPostDetail(postId.value)
    post.value = data
    document.title = `${data.title} - 智能校园综合服务平台`
    // 加载评论与推荐
    fetchComments()
    fetchRelated()
  } catch (e) {
    ElMessage.error('加载帖子失败')
  } finally {
    loading.value = false
  }
}

const fetchComments = async () => {
  try {
    const data = await getComments(postId.value, {
      page: commentPage.value,
      size: commentSize.value
    })
    if (commentPage.value === 1) {
      commentList.value = data.records || []
    } else {
      commentList.value = [...commentList.value, ...(data.records || [])]
    }
    commentTotal.value = data.total || 0
  } catch (e) {
    // ignore
  }
}

const loadMoreComments = () => {
  commentPage.value += 1
  fetchComments()
}

const fetchRelated = async () => {
  try {
    const data = await getPosts({
      type: post.value.type,
      categoryId: post.value.categoryId,
      page: 1,
      size: 5
    })
    relatedList.value = (data.records || []).filter((i) => i.id !== post.value.id).slice(0, 4)
  } catch (e) {
    // ignore
  }
}

// 点赞
const handleLike = async () => {
  if (!checkLogin()) return
  try {
    await toggleLike(postId.value)
    post.value.liked = !post.value.liked
    post.value.likeCount += post.value.liked ? 1 : -1
    ElMessage.success(post.value.liked ? '点赞成功' : '已取消点赞')
  } catch (e) {
    // ignore
  }
}

// 收藏
const handleCollect = async () => {
  if (!checkLogin()) return
  try {
    await toggleCollect(postId.value)
    post.value.collected = !post.value.collected
    post.value.collectCount = (post.value.collectCount || 0) + (post.value.collected ? 1 : -1)
    ElMessage.success(post.value.collected ? '收藏成功' : '已取消收藏')
  } catch (e) {
    // ignore
  }
}

const checkLogin = () => {
  if (!userStore.isLogin) {
    ElMessage.warning('请先登录')
    router.push({ path: '/login', query: { redirect: route.fullPath } })
    return false
  }
  return true
}

const scrollToComment = () => {
  commentRef.value?.scrollIntoView({ behavior: 'smooth', block: 'start' })
}

// 评论提交
const submitComment = ({ content, done }) => {
  addComment(postId.value, { content })
    .then((data) => {
      // 插入到列表头部
      commentList.value.unshift({
        id: data?.id || Date.now(),
        content,
        userName: userStore.userInfo?.nickname,
        userAvatar: userStore.userInfo?.avatar,
        createTime: new Date().toISOString(),
        likeCount: 0,
        children: []
      })
      commentTotal.value += 1
      if (post.value) post.value.commentCount = (post.value.commentCount || 0) + 1
      ElMessage.success('评论成功')
      done?.()
    })
    .catch(() => {
      ElMessage.error('评论失败')
      done?.()
    })
}

const replyCommentHandler = ({ target, content, done }) => {
  replyComment(target.id, { content })
    .then((data) => {
      if (!target.children) target.children = []
      target.children.push({
        id: data?.id || Date.now(),
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

const likeCommentHandler = (item) => {
  if (!checkLogin()) return
  likeComment(item.id)
    .then(() => {
      item.likeCount = (item.likeCount || 0) + 1
    })
    .catch(() => {})
}

// 编辑 / 删除
const editPost = () => {
  router.push({ path: '/forum/post/edit', query: { id: postId.value } })
}

const removePost = () => {
  ElMessageBox.confirm('确定要删除这篇帖子吗？删除后不可恢复。', '删除确认', {
    type: 'warning',
    confirmButtonText: '确认删除',
    cancelButtonText: '取消'
  })
    .then(async () => {
      try {
        await deletePost(postId.value)
        ElMessage.success('删除成功')
        router.push(forumPath.value)
      } catch (e) {
        ElMessage.error('删除失败')
      }
    })
    .catch(() => {})
}

// 举报
const handleReport = () => {
  if (!checkLogin()) return
  reportReason.value = '垃圾广告'
  reportDesc.value = ''
  reportVisible.value = true
}

const submitReport = () => {
  ElMessage.success('举报已提交，我们会尽快处理')
  reportVisible.value = false
}

// 关注(模拟)
const toggleFollow = () => {
  if (!checkLogin()) return
  followed.value = !followed.value
  ElMessage.success(followed.value ? '关注成功' : '已取消关注')
}

const goAuthor = () => {
  if (post.value?.authorId) {
    router.push(`/user/${post.value.authorId}`)
  }
}

const goRelate = (id) => {
  router.push(`/forum/post/${id}`)
}

const goBack = () => {
  router.push(forumPath.value)
}

// 切换帖子时重新加载
watch(
  () => route.params.id,
  (id) => {
    if (id) {
      commentPage.value = 1
      commentList.value = []
      fetchDetail()
      window.scrollTo({ top: 0 })
    }
  }
)

onMounted(() => {
  fetchDetail()
})
</script>

<style scoped>
.post-detail-page {
  min-height: calc(100vh - 64px);
  background: var(--bg-page);
}

.detail-layout {
  display: flex;
  gap: 24px;
  align-items: flex-start;
}

.detail-main {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.breadcrumb {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

/* 帖子头部 */
.post-header {
  padding: 24px 28px;
}

.header-top {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 14px;
  flex-wrap: wrap;
}

.badge {
  padding: 2px 10px;
  border-radius: 12px;
  font-size: 12px;
  color: #fff;
  font-weight: 600;
}

.badge-top {
  background: linear-gradient(135deg, #f59e0b, #ef4444);
}

.badge-essence {
  background: var(--gradient-primary);
}

.cat-tag {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 3px 12px;
  border-radius: 12px;
  background: rgba(99, 102, 241, 0.1);
  color: var(--primary);
  font-size: 12px;
}

.ai-tag {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 3px 12px;
  border-radius: 12px;
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  color: #fff;
  font-size: 12px;
  font-weight: 600;
  box-shadow: 0 2px 8px rgba(99, 102, 241, 0.3);
}

.post-title {
  font-size: 26px;
  font-weight: 700;
  line-height: 1.4;
  color: var(--text-primary);
  margin-bottom: 18px;
}

.author-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.author-info {
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
}

.author-meta {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.author-name {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
}

.author-info:hover .author-name {
  color: var(--primary);
}

.author-time {
  font-size: 12px;
  color: var(--text-secondary);
}

.header-actions {
  display: flex;
  gap: 8px;
}

/* 正文 */
.post-content {
  padding: 28px;
}

.content-images {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
  gap: 12px;
  margin-top: 20px;
}

.content-img {
  width: 100%;
  height: 180px;
  border-radius: var(--radius-sm);
  cursor: pointer;
  transition: transform 0.3s;
}

.content-img:hover {
  transform: scale(1.02);
}

/* 操作栏 */
.action-bar {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  padding: 18px 24px;
  position: sticky;
  top: 84px;
  z-index: 10;
}

.action-item {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 20px;
  border-radius: 24px;
  background: var(--bg-page);
  color: var(--text-regular);
  font-size: 14px;
  cursor: pointer;
  transition: all 0.25s;
}

.action-item:hover {
  background: rgba(99, 102, 241, 0.1);
  color: var(--primary);
  transform: translateY(-2px);
}

.action-item.active {
  background: var(--gradient-primary);
  color: #fff;
  box-shadow: 0 4px 12px rgba(99, 102, 241, 0.3);
}

.action-item.report:hover {
  background: rgba(239, 68, 68, 0.1);
  color: var(--danger);
}

.action-count {
  font-size: 12px;
  color: var(--text-secondary);
}

.action-item.active .action-count {
  color: rgba(255, 255, 255, 0.9);
}

/* 评论区 */
.comment-wrap {
  padding: 24px 28px;
}

.load-more {
  text-align: center;
  margin-top: 16px;
}

/* 侧边栏 */
.detail-side {
  width: 300px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  gap: 18px;
  position: sticky;
  top: 84px;
}

.side-card {
  background: var(--bg-card);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-md);
  padding: 22px;
}

.author-card-top {
  text-align: center;
  margin-bottom: 18px;
}

.author-card-name {
  font-size: 17px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 10px 0 4px;
}

.author-card-desc {
  font-size: 13px;
  color: var(--text-secondary);
  line-height: 1.6;
}

.author-stats {
  display: flex;
  justify-content: space-around;
  padding: 16px 0;
  border-top: 1px solid var(--border-light);
  border-bottom: 1px solid var(--border-light);
  margin-bottom: 16px;
}

.stat {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.stat-num {
  font-size: 18px;
  font-weight: 700;
  color: var(--text-primary);
}

.stat-label {
  font-size: 12px;
  color: var(--text-secondary);
}

.follow-btn {
  width: 100%;
}

.side-title {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 14px;
}

.side-title .el-icon {
  color: var(--primary);
}

.relate-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.relate-item {
  cursor: pointer;
  padding: 10px;
  border-radius: var(--radius-sm);
  transition: background 0.25s;
}

.relate-item:hover {
  background: var(--bg-page);
}

.relate-title {
  font-size: 13px;
  font-weight: 500;
  color: var(--text-primary);
  line-height: 1.5;
  margin-bottom: 6px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.relate-item:hover .relate-title {
  color: var(--primary);
}

.relate-meta {
  display: flex;
  gap: 14px;
  font-size: 12px;
  color: var(--text-secondary);
}

.relate-meta span {
  display: flex;
  align-items: center;
  gap: 3px;
}

.report-group {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

@media (max-width: 980px) {
  .detail-layout {
    flex-direction: column;
  }
  .detail-side {
    width: 100%;
    position: static;
    flex-direction: row;
    flex-wrap: wrap;
  }
  .detail-side > .side-card {
    flex: 1;
    min-width: 280px;
  }
}

@media (max-width: 640px) {
  .action-bar {
    flex-wrap: wrap;
    position: static;
  }
  .action-item {
    padding: 8px 14px;
  }
}
</style>
