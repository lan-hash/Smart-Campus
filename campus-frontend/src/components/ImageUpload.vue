<template>
  <div class="image-upload">
    <el-upload
      :action="uploadUrl"
      :headers="headers"
      :file-list="fileList"
      list-type="picture-card"
      :limit="limit"
      :on-success="handleSuccess"
      :on-remove="handleRemove"
      :on-exceed="handleExceed"
      :before-upload="beforeUpload"
      name="file"
    >
      <el-icon><Plus /></el-icon>
      <template #tip>
        <div class="upload-tip">最多上传 {{ limit }} 张，单张不超过 5MB</div>
      </template>
    </el-upload>
  </div>
</template>

<script setup>
import { ref, watch, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'

const props = defineProps({
  modelValue: { type: Array, default: () => [] },
  limit: { type: Number, default: 9 },
  action: { type: String, default: '/api/upload/image' }
})

const emit = defineEmits(['update:modelValue'])
const userStore = useUserStore()

const uploadUrl = computed(() => props.action)
const headers = computed(() => ({
  Authorization: `Bearer ${userStore.token}`
}))

const fileList = ref([])

watch(
  () => props.modelValue,
  (val) => {
    if (val && val.length) {
      fileList.value = val.map((url, i) => ({
        name: `image-${i}`,
        url,
        status: 'success'
      }))
    }
  },
  { immediate: true }
)

const beforeUpload = (file) => {
  const isImg = file.type.startsWith('image/')
  const isLt5M = file.size / 1024 / 1024 < 5
  if (!isImg) {
    ElMessage.error('只能上传图片文件')
    return false
  }
  if (!isLt5M) {
    ElMessage.error('图片大小不能超过 5MB')
    return false
  }
  return true
}

const handleSuccess = (res) => {
  const url = res.data || res.url || res
  const list = [...props.modelValue, url]
  emit('update:modelValue', list)
}

const handleRemove = (file) => {
  const list = props.modelValue.filter((url) => url !== file.url)
  emit('update:modelValue', list)
}

const handleExceed = () => {
  ElMessage.warning(`最多上传 ${props.limit} 张图片`)
}
</script>

<style scoped>
.upload-tip {
  font-size: 12px;
  color: var(--text-secondary);
  margin-top: 6px;
}

:deep(.el-upload-list--picture-card) {
  gap: 10px;
}

:deep(.el-upload--picture-card) {
  width: 100px;
  height: 100px;
  border-radius: 10px;
  border: 2px dashed #cbd5e1;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--text-secondary);
  transition: all 0.25s;
}

:deep(.el-upload--picture-card:hover) {
  border-color: var(--primary);
  color: var(--primary);
}
</style>
