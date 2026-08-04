<template>
  <div class="md-preview-wrapper" :class="{ 'preview-only': previewOnly }">
    <MdPreview
      :theme="theme"
      :preview-theme="previewTheme"
      :code-theme="codeTheme"
      :model-value="modelValue"
      :language="language"
    />
  </div>
</template>

<script setup>
import { MdPreview } from 'md-editor-v3'
import 'md-editor-v3/lib/preview.css'

defineProps({
  modelValue: {
    type: String,
    default: ''
  },
  theme: {
    type: String,
    default: 'light'
  },
  previewTheme: {
    type: String,
    default: 'github'
  },
  codeTheme: {
    type: String,
    default: 'atom'
  },
  language: {
    type: String,
    default: 'zh-CN'
  },
  previewOnly: {
    type: Boolean,
    default: true
  }
})
</script>

<style scoped>
.md-preview-wrapper {
  width: 100%;
  font-size: 15px;
  line-height: 1.8;
  color: var(--text-regular, #606266);
  word-break: break-word;
}

.md-preview-wrapper :deep(.md-editor-preview-wrapper) {
  padding: 0 !important;
  background: transparent !important;
}

.md-preview-wrapper :deep(.md-editor-preview) {
  padding: 0 !important;
  background: transparent !important;
  font-size: 15px;
  line-height: 1.9;
  color: var(--text-regular, #606266);
}

/* 标题样式 */
.md-preview-wrapper :deep(.md-editor-preview h1),
.md-preview-wrapper :deep(.md-editor-preview h2),
.md-preview-wrapper :deep(.md-editor-preview h3),
.md-preview-wrapper :deep(.md-editor-preview h4),
.md-preview-wrapper :deep(.md-editor-preview h5),
.md-preview-wrapper :deep(.md-editor-preview h6) {
  color: var(--text-primary, #303133);
  font-weight: 600;
  margin-top: 1.5em;
  margin-bottom: 0.8em;
}

.md-preview-wrapper :deep(.md-editor-preview h1) {
  font-size: 1.8em;
  border-bottom: 2px solid var(--primary, #6366f1);
  padding-bottom: 0.3em;
}

.md-preview-wrapper :deep(.md-editor-preview h2) {
  font-size: 1.5em;
  border-bottom: 1px solid var(--border-light, #ebeef5);
  padding-bottom: 0.3em;
}

.md-preview-wrapper :deep(.md-editor-preview h3) {
  font-size: 1.25em;
}

.md-preview-wrapper :deep(.md-editor-preview h4) {
  font-size: 1.1em;
}

/* 段落 */
.md-preview-wrapper :deep(.md-editor-preview p) {
  margin: 1em 0;
}

/* 链接 */
.md-preview-wrapper :deep(.md-editor-preview a) {
  color: var(--primary, #6366f1);
  text-decoration: none;
  border-bottom: 1px solid transparent;
  transition: all 0.2s;
}

.md-preview-wrapper :deep(.md-editor-preview a:hover) {
  border-bottom-color: var(--primary, #6366f1);
}

/* 列表 */
.md-preview-wrapper :deep(.md-editor-preview ul),
.md-preview-wrapper :deep(.md-editor-preview ol) {
  padding-left: 1.5em;
  margin: 1em 0;
}

.md-preview-wrapper :deep(.md-editor-preview li) {
  margin: 0.4em 0;
}

/* 引用块 */
.md-preview-wrapper :deep(.md-editor-preview blockquote) {
  border-left: 4px solid var(--primary, #6366f1);
  padding: 0.8em 1em;
  margin: 1em 0;
  background: rgba(99, 102, 241, 0.05);
  border-radius: 0 6px 6px 0;
  color: var(--text-secondary, #909399);
}

.md-preview-wrapper :deep(.md-editor-preview blockquote p) {
  margin: 0;
}

/* 代码块 */
.md-preview-wrapper :deep(.md-editor-preview pre) {
  background: #1e1e1e !important;
  border-radius: 8px;
  padding: 1em 1.2em;
  margin: 1em 0;
  overflow-x: auto;
}

.md-preview-wrapper :deep(.md-editor-preview pre code) {
  background: transparent !important;
  padding: 0 !important;
  font-size: 0.9em;
  font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
}

/* 行内代码 */
.md-preview-wrapper :deep(.md-editor-preview code) {
  background: rgba(99, 102, 241, 0.1);
  color: var(--primary, #6366f1);
  padding: 0.2em 0.4em;
  border-radius: 4px;
  font-size: 0.9em;
  font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
}

/* 图片 */
.md-preview-wrapper :deep(.md-editor-preview img) {
  max-width: 100%;
  border-radius: 8px;
  margin: 1em auto;
  display: block;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

/* 表格 */
.md-preview-wrapper :deep(.md-editor-preview table) {
  width: 100%;
  border-collapse: collapse;
  margin: 1em 0;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.md-preview-wrapper :deep(.md-editor-preview th),
.md-preview-wrapper :deep(.md-editor-preview td) {
  border: 1px solid var(--border-light, #ebeef5);
  padding: 0.6em 1em;
  text-align: left;
}

.md-preview-wrapper :deep(.md-editor-preview th) {
  background: var(--bg-page, #f5f7fa);
  font-weight: 600;
  color: var(--text-primary, #303133);
}

.md-preview-wrapper :deep(.md-editor-preview tr:nth-child(even)) {
  background: rgba(0, 0, 0, 0.01);
}

/* 分割线 */
.md-preview-wrapper :deep(.md-editor-preview hr) {
  border: none;
  border-top: 1px solid var(--border-light, #ebeef5);
  margin: 2em 0;
}

/* 任务列表 */
.md-preview-wrapper :deep(.md-editor-preview .task-list-item) {
  list-style-type: none;
}

.md-preview-wrapper :deep(.md-editor-preview .task-list-item input[type="checkbox"]) {
  margin-right: 0.5em;
}
</style>
