<template>
  <div class="admin-page">
    <!-- 顶部操作 -->
    <div class="page-toolbar">
      <div class="toolbar-left">
        <h2 class="page-title">内容审核</h2>
        <span class="page-desc">对论坛帖子进行审核管理，可标记违规、置顶、加精</span>
      </div>
      <div class="toolbar-right">
        <el-radio-group v-model="statusFilter" @change="handleSearch">
          <el-radio-button value="">全部</el-radio-button>
          <el-radio-button :value="0">正常</el-radio-button>
          <el-radio-button :value="1">违规</el-radio-button>
        </el-radio-group>
        <el-input
          v-model="keyword"
          placeholder="搜索标题/内容"
          clearable
          style="width: 240px"
          @keyup.enter="handleSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
      </div>
    </div>

    <!-- 列表 -->
    <div class="table-card">
      <el-table :data="list" v-loading="loading" stripe style="width: 100%">
        <el-table-column label="帖子内容" min-width="320">
          <template #default="{ row }">
            <div class="content-cell">
              <div class="content-title">{{ row.title || '无标题' }}</div>
              <div class="content-text">{{ row.content || '-' }}</div>
              <div class="content-tags">
                <el-tag v-if="row.isTop === 1" type="danger" size="small" effect="dark">置顶</el-tag>
                <el-tag v-if="row.isEssence === 1" type="warning" size="small" effect="dark">精华</el-tag>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="发布者" width="150">
          <template #default="{ row }">
            <div class="publisher-cell">
              <el-avatar :size="28" :src="row.authorAvatar">{{ (row.authorNickname || '匿')?.charAt(0) }}</el-avatar>
              <span>{{ row.authorNickname || '匿名' }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="浏览" prop="viewCount" width="70" align="center" />
        <el-table-column label="点赞" prop="likeCount" width="70" align="center" />
        <el-table-column label="评论" prop="commentCount" width="70" align="center" />
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 0 ? 'success' : 'danger'" effect="plain">
              {{ row.status === 0 ? '正常' : '违规' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="发布时间" width="170">
          <template #default="{ row }">
            {{ formatTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="240" fixed="right" align="center">
          <template #default="{ row }">
            <el-button
              :type="row.status === 0 ? 'danger' : 'success'"
              size="small"
              plain
              @click="handleStatus(row)"
            >
              {{ row.status === 0 ? '标记违规' : '恢复正常' }}
            </el-button>
            <el-button
              :type="row.isTop === 1 ? 'warning' : 'primary'"
              size="small"
              plain
              @click="handleTop(row)"
            >
              {{ row.isTop === 1 ? '取消置顶' : '置顶' }}
            </el-button>
            <el-button
              :type="row.isEssence === 1 ? 'warning' : 'primary'"
              size="small"
              plain
              @click="handleEssence(row)"
            >
              {{ row.isEssence === 1 ? '取消加精' : '加精' }}
            </el-button>
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
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { getPosts, updatePostStatus, updatePostTop, updatePostEssence } from '@/api/admin'

const statusFilter = ref('')
const keyword = ref('')
const list = ref([])
const loading = ref(false)
const page = ref(1)
const size = ref(10)
const total = ref(0)

const formatTime = (t) => {
  if (!t) return '-'
  return new Date(t).toLocaleString('zh-CN')
}

const loadList = async () => {
  loading.value = true
  try {
    const params = { page: page.value, size: size.value }
    if (statusFilter.value !== '') params.status = statusFilter.value
    if (keyword.value.trim()) params.keyword = keyword.value.trim()
    const data = await getPosts(params)
    list.value = data.records || []
    total.value = data.total || 0
  } catch {
    list.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  page.value = 1
  loadList()
}

const handleStatus = async (row) => {
  const newStatus = row.status === 0 ? 1 : 0
  const action = newStatus === 1 ? '标记违规' : '恢复正常'
  try {
    await ElMessageBox.confirm(`确认${action}帖子「${row.title}」吗？`, '确认', { type: 'warning' })
  } catch {
    return
  }
  try {
    await updatePostStatus(row.id, newStatus)
    ElMessage.success(`${action}成功`)
    loadList()
  } catch { /* 拦截器已处理 */ }
}

const handleTop = async (row) => {
  const newVal = row.isTop === 1 ? 0 : 1
  try {
    await updatePostTop(row.id, newVal)
    ElMessage.success(newVal === 1 ? '置顶成功' : '取消置顶成功')
    loadList()
  } catch { /* 拦截器已处理 */ }
}

const handleEssence = async (row) => {
  const newVal = row.isEssence === 1 ? 0 : 1
  try {
    await updatePostEssence(row.id, newVal)
    ElMessage.success(newVal === 1 ? '加精成功' : '取消加精成功')
    loadList()
  } catch { /* 拦截器已处理 */ }
}

onMounted(loadList)
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

.content-tags {
  display: flex;
  gap: 4px;
  margin-top: 2px;
}

.publisher-cell {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: var(--text-regular);
}

.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}
</style>
