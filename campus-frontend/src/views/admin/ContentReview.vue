<template>
  <div class="admin-page">
    <!-- 顶部操作 -->
    <div class="page-toolbar">
      <div class="toolbar-left">
        <h2 class="page-title">内容审核</h2>
        <span class="page-desc">对用户发布的内容进行人工审核，可参考 AI 审核结果</span>
      </div>
    </div>

    <!-- Tab 切换 -->
    <el-tabs v-model="activeTab" class="review-tabs" @tab-change="handleTabChange">
      <el-tab-pane label="帖子审核" name="post">
        <el-icon><Document /></el-icon>
      </el-tab-pane>
      <el-tab-pane label="表白审核" name="confession">
        <el-icon><ChatLineSquare /></el-icon>
      </el-tab-pane>
      <el-tab-pane label="商品审核" name="product">
        <el-icon><ShoppingCart /></el-icon>
      </el-tab-pane>
    </el-tabs>

    <!-- 筛选 -->
    <div class="filter-row">
      <el-radio-group v-model="statusFilter" @change="handleSearch">
        <el-radio-button value="">全部</el-radio-button>
        <el-radio-button value="0">待审核</el-radio-button>
        <el-radio-button value="1">已通过</el-radio-button>
        <el-radio-button value="2">已驳回</el-radio-button>
      </el-radio-group>
      <el-input
        v-model="keyword"
        placeholder="搜索内容关键词"
        clearable
        style="width: 240px"
        @keyup.enter="handleSearch"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
    </div>

    <!-- 列表 -->
    <div class="table-card">
      <el-table :data="list" v-loading="loading" stripe style="width: 100%">
        <el-table-column label="内容" min-width="320">
          <template #default="{ row }">
            <div class="content-cell">
              <div class="content-title">{{ row.title || '无标题' }}</div>
              <div class="content-text">{{ row.content || row.description || '-' }}</div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="发布者" width="150">
          <template #default="{ row }">
            <div class="publisher-cell">
              <el-avatar :size="28" :src="row.userAvatar">{{ row.userName?.charAt(0) }}</el-avatar>
              <span>{{ row.userName || '匿名' }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="AI 审核" width="120" align="center">
          <template #default="{ row }">
            <el-tag
              v-if="row.aiReview !== undefined && row.aiReview !== null"
              :type="aiTagType(row.aiReview)"
              effect="dark"
              size="small"
            >
              {{ aiTagText(row.aiReview) }}
            </el-tag>
            <span v-else class="text-muted">未审核</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)" effect="plain">
              {{ statusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="提交时间" width="170">
          <template #default="{ row }">
            {{ formatTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right" align="center">
          <template #default="{ row }">
            <template v-if="row.status === 0">
              <el-button type="success" size="small" plain @click="handleReview(row, 1)">
                <el-icon><Select /></el-icon>通过
              </el-button>
              <el-button type="danger" size="small" plain @click="handleReview(row, 2)">
                <el-icon><CloseBold /></el-icon>驳回
              </el-button>
            </template>
            <span v-else class="text-muted">已处理</span>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-wrap">
        <el-pagination
          v-model:current-page="page"
          v-model:page-size="size"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          background
          @current-change="loadList"
          @size-change="loadList"
        />
      </div>
    </div>

    <!-- 审核对话框 -->
    <el-dialog v-model="reviewVisible" :title="`${reviewAction === 1 ? '通过' : '驳回'}审核`" width="460px">
      <el-form :model="reviewForm" label-width="80px">
        <el-form-item label="审核结果">
          <el-tag :type="reviewAction === 1 ? 'success' : 'danger'" effect="dark">
            {{ reviewAction === 1 ? '通过' : '驳回' }}
          </el-tag>
        </el-form-item>
        <el-form-item label="审核备注">
          <el-input
            v-model="reviewForm.remark"
            type="textarea"
            :rows="3"
            placeholder="请填写审核备注（可选）"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="reviewVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitReview">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import {
  Search, Document, ChatLineSquare, ShoppingCart,
  Select, CloseBold
} from '@element-plus/icons-vue'
import { getReviewList, reviewItem } from '@/api/admin'

const activeTab = ref('post')
const statusFilter = ref('')
const keyword = ref('')
const list = ref([])
const loading = ref(false)
const page = ref(1)
const size = ref(10)
const total = ref(0)

const reviewVisible = ref(false)
const submitting = ref(false)
const reviewAction = ref(1)
const reviewForm = reactive({ remark: '' })
const currentRow = ref(null)

const formatTime = (t) => {
  if (!t) return '-'
  return new Date(t).toLocaleString('zh-CN')
}

const statusText = (s) => ['待审核', '已通过', '已驳回'][s] || '待审核'
const statusTagType = (s) => ['warning', 'success', 'danger'][s] || 'warning'
const aiTagText = (a) => ['待审', '通过', '违规'][a] || '未知'
const aiTagType = (a) => ['info', 'success', 'danger'][a] || 'info'

const loadList = async () => {
  loading.value = true
  try {
    const params = {
      type: activeTab.value,
      page: page.value,
      size: size.value
    }
    if (statusFilter.value !== '') params.status = statusFilter.value
    if (keyword.value.trim()) params.keyword = keyword.value.trim()
    const data = await getReviewList(params)
    list.value = data.records || []
    total.value = data.total || 0
  } catch {
    list.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

const handleTabChange = () => {
  page.value = 1
  loadList()
}

const handleSearch = () => {
  page.value = 1
  loadList()
}

const handleReview = (row, action) => {
  currentRow.value = row
  reviewAction.value = action
  reviewForm.remark = ''
  reviewVisible.value = true
}

const submitReview = async () => {
  submitting.value = true
  try {
    await reviewItem(currentRow.value.id, {
      status: reviewAction.value,
      remark: reviewForm.remark
    })
    ElMessage.success(reviewAction.value === 1 ? '已通过审核' : '已驳回')
    reviewVisible.value = false
    loadList()
  } catch {
    /* 拦截器已处理 */
  } finally {
    submitting.value = false
  }
}

onMounted(loadList)
</script>

<style scoped>
.admin-page {
  padding-bottom: 20px;
}

.page-toolbar {
  margin-bottom: 20px;
}

.page-title {
  font-size: 22px;
  font-weight: 700;
  color: var(--text-primary);
}

.page-desc {
  font-size: 13px;
  color: var(--text-secondary);
  margin-left: 10px;
}

.review-tabs {
  margin-bottom: 16px;
}

.filter-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.table-card {
  background: var(--bg-card);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-md);
  padding: 20px;
}

.content-cell {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.content-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
}

.content-text {
  font-size: 13px;
  color: var(--text-secondary);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.publisher-cell {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: var(--text-regular);
}

.text-muted {
  color: var(--text-secondary);
  font-size: 12px;
}

.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}
</style>
