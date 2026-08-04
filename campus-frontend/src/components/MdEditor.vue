<template>
  <div class="md-editor-wrapper">
    <MdEditor
      v-model="editorText"
      :theme="theme"
      :preview-theme="previewTheme"
      :code-theme="codeTheme"
      :placeholder="placeholder"
      :height="height"
      :show-toolbar="showToolbar"
      :toolbars="toolbars"
      :toolbars-exclude="toolbarsExclude"
      :auto-focus="autoFocus"
      :disabled="disabled"
      :read-only="readOnly"
      :max-length="maxLength"
      @on-change="handleChange"
      @on-upload-img="handleUploadImg"
      @on-save="handleSave"
    />
    <div v-if="showWordCount" class="word-count">
      字数：{{ wordCount }} / {{ maxLength || '∞' }}
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { MdEditor } from 'md-editor-v3'
import 'md-editor-v3/lib/style.css'
import { uploadImage } from '@/api/forum'

const props = defineProps({
  modelValue: {
    type: String,
    default: ''
  },
  placeholder: {
    type: String,
    default: '请输入内容，支持 Markdown 语法...'
  },
  height: {
    type: [String, Number],
    default: 500
  },
  showToolbar: {
    type: Boolean,
    default: true
  },
  autoFocus: {
    type: Boolean,
    default: false
  },
  disabled: {
    type: Boolean,
    default: false
  },
  readOnly: {
    type: Boolean,
    default: false
  },
  maxLength: {
    type: Number,
    default: 10000
  },
  showWordCount: {
    type: Boolean,
    default: true
  },
  theme: {
    type: String,
    default: 'light' // light | dark
  }
})

const emit = defineEmits(['update:modelValue', 'change', 'save'])

const editorText = ref(props.modelValue)

// 工具栏配置 - 只保留常用功能
const toolbars = [
  'bold',
  'underline',
  'italic',
  'strikeThrough',
  '-',
  'title',
  'sub',
  'sup',
  'quote',
  'unorderedList',
  'orderedList',
  'task',
  '-',
  'codeRow',
  'code',
  'link',
  'image',
  'table',
  'mermaid',
  'katex',
  '-',
  'revoke',
  'next',
  'save',
  '=',
  'pageFullscreen',
  'fullscreen',
  'preview',
  'previewOnly',
  'htmlPreview',
  'catalog'
]

const toolbarsExclude = []

// 预览主题
const previewTheme = 'github'
const codeTheme = 'atom'

// 字数统计
const wordCount = computed(() => {
  return editorText.value?.length || 0
})

// 双向绑定
watch(
  () => props.modelValue,
  (val) => {
    if (val !== editorText.value) {
      editorText.value = val
    }
  }
)

const handleChange = (val) => {
  emit('update:modelValue', val)
  emit('change', val)
}

const handleSave = (val) => {
  emit('save', val)
}

// 图片上传处理
const handleUploadImg = async (files, callback) => {
  const ret = []
  for (const file of files) {
    try {
      const formData = new FormData()
      formData.append('file', file)
      const url = await uploadImage(formData)
      ret.push({
        url,
        alt: file.name,
        title: file.name
      })
    } catch (e) {
      console.error('图片上传失败:', e)
    }
  }
  callback(ret)
}
</script>

<style scoped>
.md-editor-wrapper {
  width: 100%;
  border-radius: 8px;
  overflow: hidden;
}

.word-count {
  text-align: right;
  padding: 8px 12px;
  font-size: 12px;
  color: var(--el-text-color-secondary, #909399);
  background: var(--el-fill-color-light, #f5f7fa);
  border-top: 1px solid var(--el-border-color-lighter, #ebeef5);
}

/* 覆盖md-editor-v3的一些样式以适配项目风格 */
.md-editor-wrapper :deep(.md-editor) {
  border: 1px solid var(--el-border-color, #dcdfe6);
  border-radius: 8px;
}

.md-editor-wrapper :deep(.md-editor-input-wrapper),
.md-editor-wrapper :deep(.md-editor-preview-wrapper) {
  background: var(--el-bg-color, #fff);
}

.md-editor-wrapper :deep(.md-editor-toolbar) {
  background: var(--el-bg-color-page, #f5f7fa);
  border-bottom: 1px solid var(--el-border-color-lighter, #ebeef5);
}

.md-editor-wrapper :deep(.md-editor-toolbar button:hover) {
  background: var(--el-fill-color, #f0f2f5);
  color: var(--el-color-primary, #409eff);
}

.md-editor-wrapper :deep(.md-editor-toolbar .menu-item:hover) {
  background: var(--el-fill-color, #f0f2f5);
}

.md-editor-wrapper :deep(.md-editor-textarea) {
  font-size: 14px;
  line-height: 1.8;
  color: var(--el-text-color-primary, #303133);
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Noto Sans CJK SC', 'WenQuanYi Micro Hei', sans-serif;
}

.md-editor-wrapper :deep(.md-editor-preview) {
  font-size: 14px;
  line-height: 1.8;
  color: var(--el-text-color-primary, #303133);
  padding: 20px;
}
</style>
