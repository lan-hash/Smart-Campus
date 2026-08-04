<template>
  <div class="post-edit-page">
    <div class="container page-wrap">
      <!-- 未登录提示 -->
      <div v-if="!userStore.isLogin" class="login-tip card-base">
        <el-icon class="tip-icon"><Lock /></el-icon>
        <h2>请先登录后再发帖</h2>
        <p>登录后即可分享你的精彩内容</p>
        <el-button type="primary" round @click="goLogin">去登录</el-button>
      </div>

      <template v-else>
        <!-- 顶部标题 -->
        <div class="edit-header">
          <el-button text @click="goBack">
            <el-icon><ArrowLeft /></el-icon>返回
          </el-button>
          <h1 class="edit-title">
            <el-icon><EditPen /></el-icon>
            {{ isEdit ? '编辑帖子' : '发布帖子' }}
          </h1>
          <div class="header-placeholder"></div>
        </div>

        <div class="edit-layout">
          <!-- 表单主体 -->
          <div class="edit-form card-base">
            <el-form ref="formRef" :model="form" :rules="rules" label-position="top" size="large">
              <!-- 版块选择 -->
              <el-form-item label="选择版块" prop="categoryId">
                <el-select
                  v-model="form.categoryId"
                  placeholder="请选择帖子所属版块"
                  class="full-width"
                >
                  <el-option
                    v-for="cat in categories"
                    :key="cat.id"
                    :label="cat.name"
                    :value="cat.id"
                  >
                    <span class="cat-option">
                      <el-icon><component :is="cat.icon || 'Collection'" /></el-icon>
                      <span>{{ cat.name }}</span>
                    </span>
                  </el-option>
                </el-select>
              </el-form-item>

              <!-- 标题 -->
              <el-form-item label="帖子标题" prop="title">
                <el-input
                  v-model="form.title"
                  placeholder="请输入吸引人的标题..."
                  maxlength="50"
                  show-word-limit
                />
              </el-form-item>

              <!-- 正文 -->
              <el-form-item label="正文内容" prop="content">
                <MdEditor
                  v-model="form.content"
                  placeholder="详细描述你的内容，支持 Markdown 语法：标题、列表、代码块、图片、链接、表格、引用..."
                  :height="500"
                  :max-length="10000"
                />
              </el-form-item>

              <!-- 图片上传 -->
              <el-form-item label="配图 (可选)">
                <ImageUpload v-model="form.images" :limit="9" />
              </el-form-item>
            </el-form>
          </div>

          <!-- 右侧操作面板 -->
          <aside class="edit-side">
            <!-- AI 智能分类 -->
            <div class="side-card ai-card">
              <h3 class="side-title">
                <el-icon class="ai-icon"><MagicStick /></el-icon>AI 智能分类
              </h3>
              <p class="ai-desc">让 AI 根据标题和内容自动推荐合适的版块</p>
              <el-button
                type="primary"
                round
                :loading="aiLoading"
                :disabled="!form.title || !form.content"
                class="full-width"
                @click="runAiClassify"
              >
                <el-icon><MagicStick /></el-icon>
                {{ aiLoading ? 'AI 分析中...' : '开始智能分类' }}
              </el-button>

              <transition name="fade-slide">
                <div v-if="aiResult" class="ai-result">
                  <div class="ai-result-tag">
                    <el-icon><MagicStick /></el-icon>
                    <span>AI 推荐：{{ aiResult }}</span>
                  </div>
                  <el-button
                    type="success"
                    size="small"
                    round
                    plain
                    class="full-width"
                    @click="applyAiResult"
                  >
                    <el-icon><Check /></el-icon>应用此分类
                  </el-button>
                </div>
              </transition>
            </div>

            <!-- 发布操作 -->
            <div class="side-card">
              <h3 class="side-title">
                <el-icon><Promotion /></el-icon>发布设置
              </h3>
              <div class="setting-item">
                <span>帖子类型</span>
                <el-tag :type="forumType === 1 ? 'danger' : 'primary'" effect="light">
                  {{ forumType === 1 ? '游戏社区' : '学习论坛' }}
                </el-tag>
              </div>
              <div class="setting-item">
                <span>状态</span>
                <el-tag type="success" effect="light">
                  {{ isEdit ? '编辑中' : '草稿' }}
                </el-tag>
              </div>

              <el-button
                type="primary"
                size="large"
                round
                class="full-width publish-btn"
                :loading="submitting"
                @click="submit"
              >
                <el-icon><Promotion /></el-icon>
                {{ isEdit ? '保存修改' : '发布帖子' }}
              </el-button>
              <el-button size="large" round class="full-width" @click="goBack">
                取消
              </el-button>
            </div>

            <!-- 发帖小贴士 -->
            <div class="side-card tip-card">
              <h3 class="side-title">
                <el-icon><Sunny /></el-icon>发帖小贴士
              </h3>
              <ul class="tip-list">
                <li>标题简洁明了，突出重点</li>
                <li>支持 Markdown 语法排版</li>
                <li>可直接粘贴或拖拽上传图片</li>
                <li>代码块支持语法高亮</li>
                <li>选择正确的版块获得更多关注</li>
                <li>文明发言，遵守社区规范</li>
              </ul>
            </div>
          </aside>
        </div>
      </template>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  ArrowLeft, EditPen, MagicStick, Check, Promotion, Sunny, Lock, Collection
} from '@element-plus/icons-vue'
import {
  getCategories, getPostDetail, createPost, updatePost, aiClassify
} from '@/api/forum'
import { useUserStore } from '@/stores/user'
import ImageUpload from '@/components/ImageUpload.vue'
import MdEditor from '@/components/MdEditor.vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const formRef = ref(null)
const categories = ref([])
const aiResult = ref('')
const aiLoading = ref(false)
const submitting = ref(false)

const forumType = computed(() => {
  const t = Number(route.query.type)
  return t === 1 ? 1 : 0
})

const editId = computed(() => route.query.id || '')
const isEdit = computed(() => !!editId.value)

const forumPath = computed(() =>
  forumType.value === 1 ? '/forum/game' : '/forum/study'
)

const form = reactive({
  categoryId: '',
  title: '',
  content: '',
  images: []
})

const rules = {
  categoryId: [{ required: true, message: '请选择版块', trigger: 'change' }],
  title: [
    { required: true, message: '请输入标题', trigger: 'blur' },
    { min: 2, max: 50, message: '标题长度 2-50 个字符', trigger: 'blur' }
  ],
  content: [
    { required: true, message: '请输入正文内容', trigger: 'blur' },
    { min: 10, message: '正文至少 10 个字符', trigger: 'blur' }
  ]
}

const fetchCategories = async () => {
  try {
    const data = await getCategories(forumType.value)
    categories.value = data || []
    // 路由携带 categoryId 预选
    if (route.query.categoryId) {
      const id = Number(route.query.categoryId)
      if (categories.value.some((c) => c.id === id)) {
        form.categoryId = id
      }
    }
  } catch (e) {
    // ignore
  }
}

const fetchPostDetail = async () => {
  try {
    const data = await getPostDetail(editId.value)
    form.categoryId = data.categoryId
    form.title = data.title
    form.content = data.content
    if (data.images) {
      try {
        const arr = typeof data.images === 'string'
          ? JSON.parse(data.images)
          : data.images
        form.images = Array.isArray(arr) ? arr : []
      } catch {
        form.images = []
      }
    }
  } catch (e) {
    ElMessage.error('加载帖子失败')
    router.push(forumPath.value)
  }
}

// AI 智能分类
const runAiClassify = async () => {
  if (!form.title || !form.content) {
    ElMessage.warning('请先填写标题和内容')
    return
  }
  aiLoading.value = true
  aiResult.value = ''
  try {
    const data = await aiClassify({ title: form.title, content: form.content })
    aiResult.value = data.category || data || ''
    ElMessage.success('AI 分类完成')
  } catch (e) {
    ElMessage.error('AI 分类失败，请稍后重试')
  } finally {
    aiLoading.value = false
  }
}

const applyAiResult = () => {
  if (!aiResult.value) return
  // 优先匹配同名版块
  const matched = categories.value.find(
    (c) => c.name === aiResult.value || c.name.includes(aiResult.value)
  )
  if (matched) {
    form.categoryId = matched.id
    ElMessage.success(`已应用分类：${matched.name}`)
  } else {
    // 未匹配到版块，仅提示
    ElMessage.info(`AI 推荐分类「${aiResult.value}」未找到对应版块，请手动选择`)
  }
}

const submit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) {
      ElMessage.warning('请完善表单内容')
      return
    }
    submitting.value = true
    const payload = {
      categoryId: form.categoryId,
      title: form.title,
      content: form.content,
      images: JSON.stringify(form.images || [])
    }
    try {
      if (isEdit.value) {
        await updatePost(editId.value, payload)
        ElMessage.success('修改成功')
        router.push(`/forum/post/${editId.value}`)
      } else {
        await createPost(payload)
        ElMessage.success('发布成功')
        router.push(forumPath.value)
      }
    } catch (e) {
      ElMessage.error(isEdit.value ? '修改失败' : '发布失败')
    } finally {
      submitting.value = false
    }
  })
}

const goBack = () => {
  router.back()
}

const goLogin = () => {
  router.push({ path: '/login', query: { redirect: route.fullPath } })
}

onMounted(async () => {
  if (!userStore.isLogin) return
  await fetchCategories()
  if (isEdit.value) {
    await fetchPostDetail()
  }
})
</script>

<style scoped>
.post-edit-page {
  min-height: calc(100vh - 64px);
  background: var(--bg-page);
}

/* 未登录 */
.login-tip {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 80px 20px;
  text-align: center;
}

.login-tip .tip-icon {
  font-size: 56px;
  color: var(--primary);
  margin-bottom: 20px;
}

.login-tip h2 {
  font-size: 22px;
  color: var(--text-primary);
  margin-bottom: 8px;
}

.login-tip p {
  color: var(--text-secondary);
  margin-bottom: 24px;
}

/* 头部 */
.edit-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}

.edit-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 24px;
  font-weight: 700;
  background: var(--gradient-primary);
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
}

.header-placeholder {
  width: 80px;
}

/* 布局 */
.edit-layout {
  display: flex;
  gap: 24px;
  align-items: flex-start;
}

.edit-form {
  flex: 1;
  min-width: 0;
  padding: 28px 32px;
}

.full-width {
  width: 100%;
}

.cat-option {
  display: flex;
  align-items: center;
  gap: 6px;
}

/* 侧边栏 */
.edit-side {
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

.side-title {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 12px;
}

/* AI 卡片 */
.ai-card {
  background: linear-gradient(135deg, #f5f7ff 0%, #ede9fe 100%);
  border: 1px solid rgba(99, 102, 241, 0.15);
}

.ai-icon {
  color: var(--primary);
}

.ai-desc {
  font-size: 12px;
  color: var(--text-secondary);
  line-height: 1.6;
  margin-bottom: 14px;
}

.ai-result {
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px dashed rgba(99, 102, 241, 0.2);
}

.ai-result-tag {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 14px;
  border-radius: var(--radius-sm);
  background: var(--gradient-primary);
  color: #fff;
  font-size: 13px;
  font-weight: 600;
  margin-bottom: 12px;
  box-shadow: 0 4px 12px rgba(99, 102, 241, 0.25);
}

/* 设置项 */
.setting-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 0;
  font-size: 13px;
  color: var(--text-regular);
}

.publish-btn {
  margin: 14px 0 10px;
  background: var(--gradient-primary);
  border: none;
  font-weight: 600;
}

.publish-btn:hover {
  opacity: 0.92;
}

/* 小贴士 */
.tip-card {
  background: linear-gradient(135deg, #fffbeb 0%, #fef3c7 100%);
}

.tip-card .side-title .el-icon {
  color: var(--warning);
}

.tip-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.tip-list li {
  position: relative;
  padding-left: 16px;
  font-size: 13px;
  color: var(--text-regular);
  line-height: 1.6;
}

.tip-list li::before {
  content: '';
  position: absolute;
  left: 0;
  top: 8px;
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: var(--warning);
}

/* 动画 */
.fade-slide-enter-active,
.fade-slide-leave-active {
  transition: all 0.3s ease;
}

.fade-slide-enter-from,
.fade-slide-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}

@media (max-width: 980px) {
  .edit-layout {
    flex-direction: column;
  }
  .edit-side {
    width: 100%;
    position: static;
  }
}
</style>
