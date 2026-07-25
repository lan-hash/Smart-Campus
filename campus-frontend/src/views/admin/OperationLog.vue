<template>
  <div class="admin-page">
    <!-- 顶部操作 -->
    <div class="page-toolbar">
      <div class="toolbar-left">
        <h2 class="page-title">操作日志</h2>
        <span class="page-desc">记录管理员在后台的所有操作行为，仅查看</span>
      </div>
      <div class="toolbar-right">
        <el-input
          v-model="keyword"
          placeholder="搜索操作/方法/IP"
          clearable
          style="width: 240px"
          @keyup.enter="handleSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-button type="primary" @click="handleSearch">
          <el-icon><Search /></el-icon>查询
        </el-button>
      </div>
    </div>

    <!-- 列表 -->
    <div class="table-card">
      <el-table :data="logs" v-loading="loading" stripe style="width: 100%">
        <el-table-column type="index" label="#" width="60" align="center" />
        <el-table-column label="管理员" width="170">
          <template #default="{ row }">
            <div class="admin-cell">
              <el-avatar :size="28" :src="row.adminAvatar">{{ row.adminName?.charAt(0) }}</el-avatar>
              <span>{{ row.adminName || '系统' }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="operation" label="操作描述" min-width="220" show-overflow-tooltip />
        <el-table-column prop="method" label="请求方法" width="180" show-overflow-tooltip>
          <template #default="{ row }">
            <el-tag :type="methodTagType(row.method)" effect="plain" size="small">
              {{ row.method || '-' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="ip" label="IP 地址" width="150">
          <template #default="{ row }">
            <span class="ip-text">{{ row.ip || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="耗时" width="100" align="center">
          <template #default="{ row }">
            <span :class="['cost-text', costLevel(row.costTime)]">
              {{ row.costTime ? `${row.costTime}ms` : '-' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="operateTime" label="操作时间" width="180">
          <template #default="{ row }">
            {{ formatTime(row.operateTime || row.createTime) }}
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-wrap">
        <el-pagination
          v-model:current-page="page"
          v-model:page-size="size"
          :total="total"
          :page-sizes="[20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          background
          @current-change="loadLogs"
          @size-change="loadLogs"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Search } from '@element-plus/icons-vue'
import { getLogs } from '@/api/admin'

const keyword = ref('')
const logs = ref([])
const loading = ref(false)
const page = ref(1)
const size = ref(20)
const total = ref(0)

const formatTime = (t) => {
  if (!t) return '-'
  return new Date(t).toLocaleString('zh-CN')
}

const methodTagType = (m) => {
  if (!m) return 'info'
  const method = m.toUpperCase()
  if (method.includes('GET')) return 'primary'
  if (method.includes('POST')) return 'success'
  if (method.includes('PUT')) return 'warning'
  if (method.includes('DELETE')) return 'danger'
  return 'info'
}

const costLevel = (cost) => {
  if (!cost && cost !== 0) return ''
  if (cost < 200) return 'fast'
  if (cost < 1000) return 'normal'
  return 'slow'
}

const loadLogs = async () => {
  loading.value = true
  try {
    const params = { page: page.value, size: size.value }
    if (keyword.value.trim()) params.keyword = keyword.value.trim()
    const data = await getLogs(params)
    logs.value = data.records || []
    total.value = data.total || 0
  } catch {
    logs.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  page.value = 1
  loadLogs()
}

onMounted(loadLogs)
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

.admin-cell {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: var(--text-regular);
}

.ip-text {
  font-family: 'Courier New', monospace;
  font-size: 13px;
  color: var(--text-regular);
}

.cost-text {
  font-weight: 600;
  font-size: 13px;
}

.cost-text.fast {
  color: var(--success);
}

.cost-text.normal {
  color: var(--warning);
}

.cost-text.slow {
  color: var(--danger);
}

.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}
</style>
