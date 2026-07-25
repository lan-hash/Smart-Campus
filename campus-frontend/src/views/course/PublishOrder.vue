<template>
  <div class="publish-page">
    <div class="container page-wrap">
      <!-- 面包屑 -->
      <el-breadcrumb separator="/" class="bread">
        <el-breadcrumb-item :to="{ path: '/course' }">代课服务</el-breadcrumb-item>
        <el-breadcrumb-item>发布代课</el-breadcrumb-item>
      </el-breadcrumb>

      <div class="publish-card card-base">
        <div class="form-header">
          <el-icon class="header-icon"><EditPen /></el-icon>
          <div>
            <h2>发布代课需求</h2>
            <p>填写课程信息，让有时间的同学帮你解决课业</p>
          </div>
        </div>

        <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          label-width="100px"
          size="large"
          class="publish-form"
        >
          <el-form-item label="课程名称" prop="courseName">
            <el-input
              v-model="form.courseName"
              placeholder="如：高等数学A / 大学英语"
              maxlength="50"
              show-word-limit
            />
          </el-form-item>

          <el-form-item label="课程类型" prop="courseType">
            <el-select
              v-model="form.courseType"
              placeholder="请选择课程类型"
              style="width: 100%"
            >
              <el-option label="公共必修" value="公共必修" />
              <el-option label="专业必修" value="专业必修" />
              <el-option label="专业选修" value="专业选修" />
              <el-option label="公共选修" value="公共选修" />
              <el-option label="实验课" value="实验课" />
              <el-option label="体育课" value="体育课" />
              <el-option label="其他" value="其他" />
            </el-select>
          </el-form-item>

          <el-form-item label="上课时间" prop="classTime">
            <el-input
              v-model="form.classTime"
              placeholder="如：周一 3-4节 / 周三晚 19:00"
              :prefix-icon="Clock"
            />
          </el-form-item>

          <el-form-item label="上课地点" prop="location">
            <el-input
              v-model="form.location"
              placeholder="如：第三教学楼 201"
              :prefix-icon="Location"
            />
          </el-form-item>

          <el-form-item label="代课薪资" prop="salary">
            <el-input
              v-model="form.salary"
              type="number"
              placeholder="请输入愿意支付的报酬"
              :prefix-icon="Money"
            >
              <template #append>元 / 次</template>
            </el-input>
          </el-form-item>

          <el-form-item label="需求描述" prop="description">
            <el-input
              v-model="form.description"
              type="textarea"
              :rows="5"
              placeholder="详细说明代课需求，如课程进度、注意事项、是否需要签到等..."
              maxlength="500"
              show-word-limit
            />
          </el-form-item>

          <el-form-item label="联系方式" prop="contact">
            <el-input
              v-model="form.contact"
              placeholder="微信号 / 手机号 / QQ号"
              :prefix-icon="Phone"
            />
          </el-form-item>

          <el-form-item>
            <el-button
              type="primary"
              size="large"
              round
              class="submit-btn"
              :loading="submitting"
              @click="handleSubmit"
            >
              <el-icon><Check /></el-icon>
              立即发布
            </el-button>
            <el-button size="large" round @click="handleCancel">取消</el-button>
          </el-form-item>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  EditPen, Clock, Location, Money, Phone, Check
} from '@element-plus/icons-vue'
import { createOrder } from '@/api/course'

const router = useRouter()

const formRef = ref()
const submitting = ref(false)

const form = reactive({
  courseName: '',
  courseType: '',
  classTime: '',
  location: '',
  salary: '',
  description: '',
  contact: ''
})

const rules = {
  courseName: [
    { required: true, message: '请输入课程名称', trigger: 'blur' },
    { min: 2, max: 50, message: '课程名长度 2-50 个字符', trigger: 'blur' }
  ],
  courseType: [{ required: true, message: '请选择课程类型', trigger: 'change' }],
  classTime: [{ required: true, message: '请输入上课时间', trigger: 'blur' }],
  location: [{ required: true, message: '请输入上课地点', trigger: 'blur' }],
  salary: [
    { required: true, message: '请输入代课薪资', trigger: 'blur' },
    {
      validator: (rule, val, cb) => {
        if (val === '' || val === null || val === undefined) return cb(new Error('请输入代课薪资'))
        if (Number(val) <= 0) return cb(new Error('薪资必须大于0'))
        cb()
      },
      trigger: 'blur'
    }
  ],
  description: [
    { required: true, message: '请输入需求描述', trigger: 'blur' },
    { min: 10, message: '描述至少 10 个字符', trigger: 'blur' }
  ],
  contact: [{ required: true, message: '请输入联系方式', trigger: 'blur' }]
}

const handleSubmit = () => {
  if (!formRef.value) return
  formRef.value.validate(async (valid) => {
    if (!valid) return
    submitting.value = true
    try {
      await createOrder({
        ...form,
        salary: Number(form.salary)
      })
      ElMessage.success('发布成功')
      router.push('/course')
    } catch {
      /* 拦截器已处理 */
    } finally {
      submitting.value = false
    }
  })
}

const handleCancel = () => {
  router.back()
}
</script>

<style scoped>
.publish-page {
  min-height: 100vh;
  background: linear-gradient(180deg, rgba(99, 102, 241, 0.05), transparent 400px);
}

.bread {
  margin-bottom: 20px;
}

.publish-card {
  padding: 32px 40px;
  max-width: 760px;
  margin: 0 auto;
}

.form-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 30px;
  padding-bottom: 24px;
  border-bottom: 1px solid var(--border-light);
}

.header-icon {
  font-size: 40px;
  color: var(--primary);
  background: rgba(99, 102, 241, 0.1);
  padding: 12px;
  border-radius: var(--radius-md);
}

.form-header h2 {
  font-size: 22px;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 4px;
}

.form-header p {
  font-size: 13px;
  color: var(--text-secondary);
}

.publish-form {
  margin-top: 10px;
}

.submit-btn {
  background: var(--gradient-primary);
  border: none;
  font-weight: 600;
  padding: 0 40px;
  box-shadow: 0 8px 20px rgba(99, 102, 241, 0.3);
}

.submit-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 12px 26px rgba(99, 102, 241, 0.4);
}
</style>
