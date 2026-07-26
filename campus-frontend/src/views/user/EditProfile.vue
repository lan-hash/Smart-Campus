<template>
  <div class="edit-profile page-wrap">
    <div class="container">
      <div class="page-header">
        <div class="header-back" @click="goBack">
          <el-icon><ArrowLeft /></el-icon>
          返回
        </div>
        <h1 class="page-title">编辑个人资料</h1>
      </div>

      <div class="edit-card" v-loading="pageLoading">
        <!-- 头像上传区 -->
        <div class="avatar-section">
          <div class="avatar-box" @click="triggerUpload">
            <el-avatar :size="110" :src="form.avatar" class="preview-avatar">
              {{ form.nickname?.charAt(0) }}
            </el-avatar>
            <div class="avatar-mask">
              <el-icon><Camera /></el-icon>
              <span>更换头像</span>
            </div>
            <input
              ref="fileInput"
              type="file"
              accept="image/*"
              hidden
              @change="handleAvatarChange"
            />
          </div>
          <div class="avatar-tip">
            <h4>头像设置</h4>
            <p>支持 JPG、PNG 格式，文件不超过 5MB</p>
            <p>建议尺寸 200x200，方形头像效果更佳</p>
          </div>
        </div>

        <el-divider />

        <!-- 表单区 -->
        <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          label-width="100px"
          label-position="right"
          size="large"
          class="profile-form"
        >
          <el-form-item label="昵称" prop="nickname">
            <el-input
              v-model="form.nickname"
              placeholder="请输入昵称"
              maxlength="20"
              show-word-limit
              clearable
            />
          </el-form-item>

          <el-form-item label="性别" prop="gender">
            <el-radio-group v-model="form.gender">
              <el-radio :label="1">
                <el-icon><Male /></el-icon> 男
              </el-radio>
              <el-radio :label="2">
                <el-icon><Female /></el-icon> 女
              </el-radio>
              <el-radio :label="0">保密</el-radio>
            </el-radio-group>
          </el-form-item>

          <el-form-item label="邮箱" prop="email">
            <el-input
              v-model="form.email"
              placeholder="请输入邮箱"
              :prefix-icon="Message"
              clearable
            />
          </el-form-item>

          <el-form-item label="所在学校" prop="campus">
            <el-input
              v-model="form.campus"
              placeholder="请输入所在学校"
              :prefix-icon="School"
              clearable
            />
          </el-form-item>

          <el-form-item label="个人简介" prop="bio">
            <el-input
              v-model="form.bio"
              type="textarea"
              :rows="4"
              placeholder="介绍一下自己吧~"
              maxlength="100"
              show-word-limit
              resize="none"
            />
          </el-form-item>

          <el-form-item>
            <div class="form-actions">
              <el-button @click="goBack">取消</el-button>
              <el-button type="primary" :loading="saving" @click="handleSave">
                <el-icon><Check /></el-icon>保存资料
              </el-button>
            </div>
          </el-form-item>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { updateProfile, uploadAvatar } from '@/api/user'
import {
  ArrowLeft, Camera, Message, School, Male, Female, Check
} from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()

const formRef = ref()
const fileInput = ref()
const pageLoading = ref(false)
const saving = ref(false)

const form = reactive({
  avatar: '',
  nickname: '',
  gender: 0,
  email: '',
  campus: '',
  bio: ''
})

const rules = {
  nickname: [
    { required: true, message: '请输入昵称', trigger: 'blur' },
    { max: 20, message: '昵称不能超过 20 个字符', trigger: 'blur' }
  ],
  email: [
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  bio: [
    { max: 100, message: '个人简介不能超过 100 个字符', trigger: 'blur' }
  ]
}

const fillForm = () => {
  const info = userStore.userInfo || {}
  form.avatar = info.avatar || ''
  form.nickname = info.nickname || ''
  form.gender = info.gender ?? 0
  form.email = info.email || ''
  form.campus = info.campus || ''
  form.bio = info.bio || ''
}

const triggerUpload = () => {
  fileInput.value?.click()
}

const handleAvatarChange = async (e) => {
  const file = e.target.files?.[0]
  if (!file) return

  if (!file.type.startsWith('image/')) {
    ElMessage.error('只能上传图片文件')
    e.target.value = ''
    return
  }
  if (file.size / 1024 / 1024 > 5) {
    ElMessage.error('图片大小不能超过 5MB')
    e.target.value = ''
    return
  }

  try {
    const formData = new FormData()
    formData.append('file', file)
    const res = await uploadAvatar(formData)
    const url = typeof res === 'string' ? res : (res?.url || res?.data || res)
    if (url) {
      form.avatar = url
      userStore.setUserInfo({ avatar: url })
      ElMessage.success('头像上传成功')
    }
  } catch (err) {
    // 错误已处理
  } finally {
    e.target.value = ''
  }
}

const handleSave = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    saving.value = true
    try {
      const payload = {
        nickname: form.nickname,
        gender: form.gender,
        email: form.email,
        campus: form.campus,
        bio: form.bio
      }
      if (form.avatar) payload.avatar = form.avatar

      await updateProfile(payload)
      // 更新本地用户信息
      userStore.setUserInfo({
        nickname: form.nickname,
        gender: form.gender,
        email: form.email,
        campus: form.campus,
        bio: form.bio,
        avatar: form.avatar
      })
      ElMessage.success('资料保存成功')
      router.back()
    } catch (e) {
      // 错误已处理
    } finally {
      saving.value = false
    }
  })
}

const goBack = () => {
  router.back()
}

onMounted(async () => {
  pageLoading.value = true
  try {
    // 拉取最新用户信息确保数据准确
    if (userStore.token) {
      await userStore.fetchUserInfo()
    }
    fillForm()
  } catch (e) {
    fillForm()
  } finally {
    pageLoading.value = false
  }
})
</script>

<style scoped>
.page-wrap {
  padding: 24px 0 48px;
}

.page-header {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-bottom: 24px;
}

.header-back {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 8px 16px;
  border-radius: var(--radius-sm);
  background: var(--bg-card);
  box-shadow: var(--shadow-sm);
  color: var(--text-regular);
  font-size: 14px;
  cursor: pointer;
  transition: all 0.25s;
}

.header-back:hover {
  color: var(--primary);
  box-shadow: var(--shadow-md);
  transform: translateX(-2px);
}

.page-title {
  font-size: 24px;
  font-weight: 700;
  color: var(--text-primary);
}

.edit-card {
  background: var(--bg-card);
  border-radius: var(--radius-lg);
  padding: 36px 40px;
  box-shadow: var(--shadow-md);
  max-width: 760px;
  margin: 0 auto;
}

/* 头像上传区 */
.avatar-section {
  display: flex;
  align-items: center;
  gap: 32px;
}

.avatar-box {
  position: relative;
  width: 110px;
  height: 110px;
  border-radius: 50%;
  cursor: pointer;
  flex-shrink: 0;
  overflow: hidden;
  box-shadow: 0 8px 22px rgba(99, 102, 241, 0.2);
}

:deep(.preview-avatar) {
  width: 110px !important;
  height: 110px !important;
  font-size: 40px;
  font-weight: 700;
  background: linear-gradient(135deg, #818cf8, #a78bfa);
  color: #fff;
}

.avatar-mask {
  position: absolute;
  inset: 0;
  border-radius: 50%;
  background: rgba(0, 0, 0, 0.55);
  color: #fff;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 4px;
  font-size: 12px;
  opacity: 0;
  transition: opacity 0.3s;
}

.avatar-mask .el-icon {
  font-size: 22px;
}

.avatar-box:hover .avatar-mask {
  opacity: 1;
}

.avatar-tip h4 {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 6px;
}

.avatar-tip p {
  font-size: 13px;
  color: var(--text-secondary);
  line-height: 1.7;
}

/* 表单 */
.profile-form {
  margin-top: 8px;
}

:deep(.profile-form .el-form-item__label) {
  font-weight: 500;
  color: var(--text-regular);
}

:deep(.profile-form .el-radio__label) {
  display: inline-flex;
  align-items: center;
  gap: 3px;
}

:deep(.profile-form .el-radio__input.is-checked .el-radio__inner) {
  background: var(--primary);
  border-color: var(--primary);
}

:deep(.profile-form .el-radio__input.is-checked + .el-radio__label) {
  color: var(--primary);
}

.form-actions {
  display: flex;
  gap: 14px;
  width: 100%;
}

.form-actions .el-button {
  min-width: 120px;
}

.form-actions .el-button--primary {
  background: var(--gradient-primary);
  border: none;
  box-shadow: 0 8px 20px rgba(99, 102, 241, 0.3);
}

.form-actions .el-button--primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 12px 28px rgba(99, 102, 241, 0.4);
}

@media (max-width: 576px) {
  .edit-card {
    padding: 24px 20px;
  }
  .avatar-section {
    flex-direction: column;
    text-align: center;
    gap: 16px;
  }
  :deep(.profile-form .el-form-item__label) {
    float: none;
    text-align: left;
  }
}
</style>
