<template>
  <div class="admin-page">
    <!-- 顶部操作 -->
    <div class="page-toolbar">
      <div class="toolbar-left">
        <h2 class="page-title">举报处理</h2>
        <span class="page-desc">处理用户提交的各类举报，维护平台秩序</span>
      </div>
      <div class="toolbar-right">
        <el-select v-model="statusFilter" placeholder="状态筛选" style="width: 140px" @change="handleSearch">
          <el-option label="全部状态" value="" />
          <el-option label="待处理" :value="0" />
          <el-option label="已通过" :value="1" />
          <el-option label="已驳回" :value="2" />
        </el-select>
        <el-select v-model="typeFilter" placeholder="类型筛选" style="width: 140px" @change="handleSearch">
          <el-option label="全部类型" value="" />
          <el-option label="帖子" value="post" />
          <el-option label="表白" value="confession" />
          <el-option label="商品" value="product" />
          <el-option label="代课" value="course" />
        </el-select>
      </div>
    </div>

    <!-- 列表 -->
    <div class="table-card">
      <el-table :data="reports" v-loading="loading" stripe style="width: 100%">
        <el-table-column label="举报人" width="160">
          <template #default="{ row }">
            <div class="reporter-cell">
              <el-avatar :size="30" :src="row.reporterAvatar">{{ row.reporterName?.charAt(0) }}</el-avatar>
              <span>{{ row.reporterName || '匿名' }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="目标类型" width="110" align="center">
          <template #default="{ row }">
            <el-tag :type="targetTagType(row.targetType)" effect="plain">
              {{ targetText(row.targetType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="targetTitle" label="目标内容" min-width="180" show-overflow-tooltip />
        <el-table-column prop="reason" label="举报原因" min-width="200" show-overflow-tooltip />
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)" effect="dark">
              {{ statusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="举报时间" width="170">
          <template #default="{ row }">
            {{ formatTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right" align="center">
          <template #default="{ row }">
            <el-button
              v-if="row.status === 0"
              type="primary"
              size="small"
              @click="openHandle(row)"
            >
              <el-icon><Edit /></el-icon>处理
            </el-button>
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
          @current-change="loadReports"
          @size-change="loadReports"
        />
      </div>
    </div>

    <!-- 处理对话框 -->
    <el-dialog v-model="handleVisible" title="处理举报" width="480px">
      <div class="handle-info">
        <div class="info-row">
          <span class="info-label">举报对象：</span>
          <span>{{ currentRow?.targetTitle || '-' }}</span>
        </div>
        <div class="info-row">
          <span class="info-label">举报原因：</span>
          <span>{{ currentRow?.reason || '-' }}</span>
        </div>
      </div>
      <el-divider />
      <el-form :model="handleForm" label-width="90px">
        <el-form-item label="处理结果">
          <el-radio-group v-model="handleForm.status">
            <el-radio :value="1">通过（处罚）</el-radio>
            <el-radio :value="2">驳回（无效）</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="处理备注">
          <el-input
            v-model="handleForm.handleRemark"
            type="textarea"
            :rows="4"
            placeholder="请填写处理说明..."
            maxlength="300"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="handleVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitHandle">确认处理</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Edit } from '@element-plus/icons-vue'
import { getReports, handleReport } from '@/api/admin'

const statusFilter = ref('')
const typeFilter = ref('')
const reports = ref([])
const loading = ref(false)
const page = ref(1)
const size = ref(10)
const total = ref(0)

const handleVisible = ref(false)
const submitting = ref(false)
const currentRow = ref(null)
const handleForm = reactive({ status: 1, handleRemark: '' })

const formatTime = (t) => {
  if (!t) return '-'
  return new Date(t).toLocaleString('zh-CN')
}

const statusText = (s) => ['待处理', '已通过', '已驳回'][s] || '待处理'
const statusTagType = (s) => ['warning', 'success', 'info'][s] || 'warning'

const targetText = (t) => ({
  post: '帖子', confession: '表白', product: '商品', course: '代课', comment: '评论'
}[t] || t || '其他')

const targetTagType = (t) => ({
  post: 'primary', confession: 'danger', product: 'warning', course: 'success', comment: 'info'
}[t] || 'info')

const loadReports = async () => {
  loading.value = true
  try {
    const params = { page: page.value, size: size.value }
    if (statusFilter.value !== '') params.status = statusFilter.value
    if (typeFilter.value) params.targetType = typeFilter.value
    const data = await getReports(params)
    reports.value = data.records || []
    total.value = data.total || 0
  } catch {
    reports.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  page.value = 1
  loadReports()
}

const openHandle = (row) => {
  currentRow.value = row
  handleForm.status = 1
  handleForm.handleRemark = ''
  handleVisible.value = true
}

const submitHandle = async () => {
  submitting.value = true
  try {
    await handleReport(currentRow.value.id, {
      status: handleForm.status,
      handleRemark: handleForm.handleRemark
    })
    ElMessage.success('处理成功')
    handleVisible.value = false
    loadReports()
  } catch {
    /* 拦截器已处理 */
  } finally {
    submitting.value = false
  }
}

onMounted(loadReports)
</script>

<style scoped>
.admin-page {
  padding-bottom: 20px;
}

.page-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 16px;
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

.toolbar-right {
  display: flex;
  align-items: center;
  gap: 10px;
}

.table-card {
  background: var(--bg-card);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-md);
  padding: 20px;
}

.reporter-cell {
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

.handle-info {
  background: var(--bg-page);
  padding: 14px 16px;
  border-radius: var(--radius-md);
}

.info-row {
  font-size: 14px;
  margin-bottom: 8px;
  color: var(--text-regular);
}

.info-row:last-child {
  margin-bottom: 0;
}

.info-label {
  color: var(--text-secondary);
  font-weight: 500;
}

.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}
</style>
