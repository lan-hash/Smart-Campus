<template>
  <div class="publish-page">
    <div class="container page-wrap">
      <!-- 面包屑 -->
      <el-breadcrumb separator="/" class="bread">
        <el-breadcrumb-item :to="{ path: '/secondhand' }">二手市场</el-breadcrumb-item>
        <el-breadcrumb-item>{{ isEdit ? '编辑商品' : '发布商品' }}</el-breadcrumb-item>
      </el-breadcrumb>

      <div class="publish-card card-base">
        <div class="form-header">
          <el-icon class="header-icon"><EditPen /></el-icon>
          <div>
            <h2>{{ isEdit ? '编辑商品信息' : '发布闲置商品' }}</h2>
            <p>填写完整信息，让你的宝贝更快被有缘人发现</p>
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
          <el-form-item label="商品分类" prop="categoryId">
            <el-select
              v-model="form.categoryId"
              placeholder="请选择分类"
              style="width: 100%"
            >
              <el-option
                v-for="cat in categories"
                :key="cat.id"
                :label="cat.name"
                :value="cat.id"
              />
            </el-select>
          </el-form-item>

          <el-form-item label="商品标题" prop="title">
            <el-input
              v-model="form.title"
              placeholder="一句话描述你的宝贝（最多50字）"
              maxlength="50"
              show-word-limit
            />
          </el-form-item>

          <el-form-item label="商品价格" prop="price">
            <el-input
              v-model="form.price"
              type="number"
              placeholder="请输入出售价格"
              :prefix-icon="Money"
            >
              <template #append>元</template>
            </el-input>
          </el-form-item>

          <el-form-item label="原价" prop="originalPrice">
            <el-input
              v-model="form.originalPrice"
              type="number"
              placeholder="选填，原购买价格"
              :prefix-icon="Money"
            >
              <template #append>元</template>
            </el-input>
          </el-form-item>

          <el-form-item label="新旧程度" prop="conditionLevel">
            <div class="condition-slider">
              <el-slider
                v-model="form.conditionLevel"
                :min="1"
                :max="10"
                :marks="conditionMarks"
                style="flex: 1; margin-right: 20px"
              />
              <el-tag type="success" effect="dark" round size="large">
                {{ form.conditionLevel }} 成新
              </el-tag>
            </div>
          </el-form-item>

          <el-form-item label="商品图片" prop="images">
            <ImageUpload v-model="images" :limit="9" />
          </el-form-item>

          <el-form-item label="商品描述" prop="description">
            <el-input
              v-model="form.description"
              type="textarea"
              :rows="5"
              placeholder="详细描述商品的品牌、型号、使用情况、瑕疵等..."
              maxlength="500"
              show-word-limit
            />
          </el-form-item>

          <el-form-item label="交易地点" prop="location">
            <el-input
              v-model="form.location"
              placeholder="如：图书馆一楼 / 北区宿舍"
              :prefix-icon="Location"
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
              {{ isEdit ? '保存修改' : '立即发布' }}
            </el-button>
            <el-button size="large" round @click="handleCancel">取消</el-button>
          </el-form-item>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  EditPen, Money, Location, Phone, Check
} from '@element-plus/icons-vue'
import ImageUpload from '@/components/ImageUpload.vue'
import {
  getCategories, createProduct, updateProduct, getProductDetail
} from '@/api/secondhand'

const route = useRoute()
const router = useRouter()

const formRef = ref()
const submitting = ref(false)
const categories = ref([])
const images = ref([])

const isEdit = computed(() => !!route.query.id)

const form = reactive({
  categoryId: '',
  title: '',
  price: '',
  originalPrice: '',
  conditionLevel: 9,
  description: '',
  location: '',
  contact: ''
})

const rules = {
  categoryId: [{ required: true, message: '请选择商品分类', trigger: 'change' }],
  title: [
    { required: true, message: '请输入商品标题', trigger: 'blur' },
    { min: 4, max: 50, message: '标题长度 4-50 个字符', trigger: 'blur' }
  ],
  price: [
    { required: true, message: '请输入商品价格', trigger: 'blur' },
    {
      validator: (rule, val, cb) => {
        if (val === '' || val === null || val === undefined) return cb(new Error('请输入商品价格'))
        if (Number(val) <= 0) return cb(new Error('价格必须大于0'))
        cb()
      },
      trigger: 'blur'
    }
  ],
  description: [{ required: true, message: '请输入商品描述', trigger: 'blur' }],
  location: [{ required: true, message: '请输入交易地点', trigger: 'blur' }],
  contact: [{ required: true, message: '请输入联系方式', trigger: 'blur' }]
}

const conditionMarks = {
  1: '1',
  3: '3',
  5: '5',
  7: '7',
  10: '10'
}

const loadCategories = async () => {
  try {
    const data = await getCategories()
    categories.value = data || []
  } catch {
    categories.value = []
  }
}

const loadDetail = async () => {
  if (!route.query.id) return
  try {
    const data = await getProductDetail(route.query.id)
    Object.keys(form).forEach((k) => {
      if (data[k] !== undefined) form[k] = data[k]
    })
    if (data.images) {
      try {
        images.value = typeof data.images === 'string'
          ? JSON.parse(data.images) : data.images
      } catch {
        images.value = []
      }
    }
  } catch {
    /* 拦截器已处理 */
  }
}

const handleSubmit = () => {
  if (!formRef.value) return
  formRef.value.validate(async (valid) => {
    if (!valid) return
    submitting.value = true
    try {
      const payload = {
        ...form,
        price: Number(form.price),
        originalPrice: form.originalPrice ? Number(form.originalPrice) : null,
        images: JSON.stringify(images.value)
      }
      if (isEdit.value) {
        await updateProduct(route.query.id, payload)
        ElMessage.success('修改成功')
      } else {
        await createProduct(payload)
        ElMessage.success('发布成功')
      }
      router.push('/secondhand')
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

onMounted(() => {
  loadCategories()
  loadDetail()
})
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

.condition-slider {
  display: flex;
  align-items: center;
  width: 100%;
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
