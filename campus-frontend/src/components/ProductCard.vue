<template>
  <div class="product-card card-base" @click="goDetail">
    <div class="product-cover">
      <img :src="coverImage" :alt="product.title" />
      <span class="status-tag" :class="`status-${product.status}`">
        {{ statusText }}
      </span>
    </div>
    <div class="product-body">
      <h3 class="product-title">{{ product.title }}</h3>
      <div class="product-price-row">
        <span class="price">¥{{ product.price }}</span>
        <span v-if="product.originalPrice" class="origin-price">
          ¥{{ product.originalPrice }}
        </span>
      </div>
      <div class="product-meta">
        <span class="condition">{{ conditionText }}</span>
        <span class="location" v-if="product.location">
          <el-icon><Location /></el-icon>{{ product.location }}
        </span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { Location } from '@element-plus/icons-vue'

const props = defineProps({
  product: { type: Object, required: true }
})
const router = useRouter()

const coverImage = computed(() => {
  if (props.product.images) {
    try {
      const arr = typeof props.product.images === 'string'
        ? JSON.parse(props.product.images) : props.product.images
      return Array.isArray(arr) && arr.length ? arr[0] : '/placeholder.png'
    } catch {
      return '/placeholder.png'
    }
  }
  return '/placeholder.png'
})

const statusText = computed(() => {
  return ['在售', '已售', '已下架'][props.product.status] || '在售'
})

const conditionText = computed(() => `${props.product.conditionLevel || 9}成新`)

const goDetail = () => router.push(`/secondhand/${props.product.id}`)
</script>

<style scoped>
.product-card {
  cursor: pointer;
  overflow: hidden;
}

.product-cover {
  position: relative;
  height: 200px;
  overflow: hidden;
  background: #f1f5f9;
}

.product-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.4s;
}

.product-card:hover .product-cover img {
  transform: scale(1.06);
}

.status-tag {
  position: absolute;
  top: 12px;
  right: 12px;
  padding: 3px 10px;
  border-radius: 20px;
  font-size: 12px;
  color: #fff;
  font-weight: 600;
}

.status-0 {
  background: var(--success);
}

.status-1 {
  background: #94a3b8;
}

.status-2 {
  background: var(--danger);
}

.product-body {
  padding: 14px;
}

.product-title {
  font-size: 15px;
  font-weight: 600;
  margin-bottom: 8px;
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.product-price-row {
  display: flex;
  align-items: baseline;
  gap: 8px;
  margin-bottom: 8px;
}

.price {
  font-size: 20px;
  font-weight: 700;
  color: var(--danger);
}

.origin-price {
  font-size: 12px;
  color: var(--text-secondary);
  text-decoration: line-through;
}

.product-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 12px;
  color: var(--text-secondary);
}

.condition {
  padding: 2px 8px;
  border-radius: 6px;
  background: rgba(16, 185, 129, 0.1);
  color: var(--success);
}

.location {
  display: flex;
  align-items: center;
  gap: 2px;
}
</style>
