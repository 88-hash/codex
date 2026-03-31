<template>
  <div class="detail-page" v-if="goods">
    <div class="page-container">
      <el-breadcrumb separator="/">
        <el-breadcrumb-item :to="{ path: '/home' }">首页</el-breadcrumb-item>
        <el-breadcrumb-item :to="{ path: '/category' }">商品分类</el-breadcrumb-item>
        <el-breadcrumb-item>{{ goods.name }}</el-breadcrumb-item>
      </el-breadcrumb>

      <section class="detail-main leyi-panel-strong">
        <div class="detail-grid">
          <div class="gallery-col">
            <div class="main-image">
              <img :src="getImageUrl(currentImage || goods.imageUrl)" :alt="goods.name" @error="handleImageError" />
            </div>
            <div class="thumb-row" v-if="images.length > 0">
              <button
                v-for="img in images"
                :key="img.id"
                type="button"
                class="thumb-item"
                :class="{ active: currentImage === img.imageUrl }"
                @mouseenter="currentImage = img.imageUrl"
              >
                <img :src="getImageUrl(img.imageUrl)" :alt="goods.name" @error="handleImageError" />
              </button>
            </div>
          </div>

          <div class="info-col">
            <div class="meta-line">
              <span class="badge-hot">精选推荐</span>
              <div class="rating-wrap" v-if="avgRating">
                <el-rate :model-value="avgRating" disabled size="small" />
                <span>{{ avgRating.toFixed(1) }} / 5.0 ({{ commentCount }})</span>
              </div>
            </div>

            <h1>{{ goods.name }}</h1>
            <p class="desc" v-if="goods.description">{{ goods.description }}</p>

            <div class="spec-grid">
              <div v-for="item in displaySpecs" :key="item.label" class="spec-item">
                <span>{{ item.label }}</span>
                <strong>{{ item.value }}</strong>
              </div>
            </div>

            <div class="price-box">
              <div class="price-left">
                <p class="origin" v-if="oldPrice > 0">¥{{ oldPrice.toFixed(2) }}</p>
                <p class="current">¥{{ goods.price?.toFixed(2) }}</p>
              </div>
              <span class="stock">库存 {{ maxStock }} 件</span>
            </div>

            <div class="quantity-row">
              <span>购买数量</span>
              <el-input-number v-model="quantity" :min="1" :max="maxStock" size="large" />
            </div>

            <div class="action-row">
              <el-button size="large" @click="addToCart">
                <el-icon><ShoppingCart /></el-icon>
                加入购物车
              </el-button>
              <el-button type="primary" size="large" @click="buyNow">立即购买</el-button>
            </div>

            <div class="service-row">
              <span><el-icon><Box /></el-icon> 官方正品保障</span>
              <span><el-icon><Van /></el-icon> 支持快速发货</span>
              <span><el-icon><RefreshRight /></el-icon> 完善售后服务</span>
            </div>
          </div>
        </div>
      </section>

      <section class="detail-tabs leyi-panel">
        <el-tabs v-model="activeTab">
          <el-tab-pane label="商品详情" name="detail">
            <div class="tab-content">
              <p v-if="goods.description">{{ goods.description }}</p>
              <el-empty v-else description="暂无详情说明" />
            </div>
          </el-tab-pane>
          <el-tab-pane :label="`商品评价 (${commentCount})`" name="comments">
            <div class="tab-content">
              <div class="comment-summary" v-if="avgRating">
                <div class="score">
                  <strong>{{ avgRating.toFixed(1) }}</strong>
                  <span>综合评分</span>
                </div>
                <el-rate :model-value="avgRating" disabled show-score />
              </div>

              <div v-if="comments.length > 0" class="comments-list">
                <article v-for="comment in comments" :key="comment.id" class="comment-item">
                  <div class="comment-head">
                    <el-avatar :size="40" :src="comment.userAvatar">
                      {{ comment.userName?.charAt(0) || 'U' }}
                    </el-avatar>
                    <div class="comment-user">
                      <p>{{ comment.userName || comment.userPhone?.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2') }}</p>
                      <el-rate :model-value="comment.rating" disabled size="small" />
                    </div>
                    <span class="comment-time">{{ formatTime(comment.createdAt) }}</span>
                  </div>
                  <p class="comment-body">{{ comment.content }}</p>
                </article>
              </div>
              <el-empty v-else description="暂无用户评价" />
            </div>
          </el-tab-pane>
        </el-tabs>
      </section>
    </div>
  </div>

  <div v-else class="page-container">
    <el-skeleton :rows="8" animated />
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import dayjs from 'dayjs'
import { getGoodsDetail } from '@/api/goods'
import { useCartStore } from '@/stores/cart'
import { useUserStore } from '@/stores/user'
import { getImageUrl, handleImageError, normalizeImageUrl } from '@/utils/image'

const route = useRoute()
const router = useRouter()
const cartStore = useCartStore()
const userStore = useUserStore()

const goods = ref(null)
const images = ref([])
const currentImage = ref('')
const comments = ref([])
const avgRating = ref(0)
const commentCount = ref(0)
const quantity = ref(1)
const activeTab = ref('detail')

const maxStock = computed(() => Math.max(Number(goods.value?.stock) || 1, 1))
const oldPrice = computed(() => {
  const p = Number(goods.value?.price || 0)
  return p > 0 ? p * 1.35 : 0
})

const displaySpecs = computed(() => {
  return [
    { label: '库存', value: `${maxStock.value} 件` },
    { label: '条码', value: goods.value?.barCode || '暂无' },
    { label: '保质期', value: goods.value?.expireDate || '请见包装' }
  ]
})

const formatTime = (time) => dayjs(time).format('YYYY-MM-DD HH:mm')

onMounted(async () => {
  try {
    const res = await getGoodsDetail(route.params.id)
    const rawGoods = res.data.goods || {}
    goods.value = {
      ...rawGoods,
      imageUrl: normalizeImageUrl(rawGoods.imageUrl)
    }
    images.value = (rawGoods.images || []).map((item) => ({
      ...item,
      imageUrl: normalizeImageUrl(item.imageUrl)
    }))
    currentImage.value = images.value[0]?.imageUrl || goods.value.imageUrl
    comments.value = (res.data.comments || []).map((comment) => ({
      ...comment,
      userAvatar: getImageUrl(comment.userAvatar)
    }))
    avgRating.value = res.data.avgRating || 0
    commentCount.value = res.data.commentCount || 0
  } catch (error) {
    console.error(error)
  }
})

const addToCart = async () => {
  if (!userStore.isLoggedIn()) {
    router.push({ path: '/login', query: { redirect: route.fullPath } })
    return
  }
  try {
    await cartStore.addToCart(goods.value.id, quantity.value)
    ElMessage.success('已加入购物车')
  } catch (error) {
    console.error(error)
  }
}

const buyNow = async () => {
  if (!userStore.isLoggedIn()) {
    router.push({ path: '/login', query: { redirect: route.fullPath } })
    return
  }
  try {
    await cartStore.addToCart(goods.value.id, quantity.value)
    sessionStorage.removeItem('checkout.remark')
    router.push('/payment')
  } catch (error) {
    console.error(error)
  }
}
</script>

<style lang="scss" scoped>
.detail-main {
  padding: var(--space-8);
}

.detail-grid {
  display: grid;
  grid-template-columns: 1fr 1.1fr;
  gap: var(--space-8);
}

.main-image {
  aspect-ratio: 1 / 1;
  border-radius: var(--radius-xl);
  overflow: hidden;
  background: #f0f1eb;
}

.main-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.thumb-row {
  margin-top: var(--space-4);
  display: flex;
  gap: var(--space-3);
}

.thumb-item {
  width: 5.1rem;
  height: 5.1rem;
  border-radius: var(--radius-md);
  border: 1px solid rgba(24, 24, 24, 0.12);
  background: #fff;
  padding: 0;
  cursor: pointer;
  overflow: hidden;
}

.thumb-item.active {
  border-color: rgba(238, 205, 43, 0.9);
  box-shadow: 0 0 0 2px rgba(238, 205, 43, 0.32);
}

.thumb-item img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.meta-line {
  display: flex;
  align-items: center;
  gap: var(--space-4);
}

.badge-hot {
  height: 1.6rem;
  padding: 0 0.65rem;
  border-radius: var(--radius-pill);
  background: rgba(238, 205, 43, 0.26);
  color: #59490a;
  font-size: 0.74rem;
  font-weight: 700;
  display: inline-flex;
  align-items: center;
}

.rating-wrap {
  display: inline-flex;
  align-items: center;
  gap: var(--space-2);
  color: var(--color-muted);
  font-size: 0.82rem;
}

.info-col h1 {
  margin: var(--space-4) 0 var(--space-3);
  font-size: clamp(1.85rem, 2.2vw, 2.4rem);
  line-height: 1.2;
  letter-spacing: -0.01em;
}

.desc {
  margin: 0;
  color: var(--color-muted);
  line-height: 1.7;
}

.spec-grid {
  margin-top: var(--space-6);
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: var(--space-3);
}

.spec-item {
  background: #fbfbf8;
  border: 1px solid rgba(24, 24, 24, 0.08);
  border-radius: var(--radius-md);
  padding: var(--space-4);
}

.spec-item span {
  display: block;
  font-size: 0.78rem;
  color: var(--color-muted);
}

.spec-item strong {
  display: block;
  margin-top: var(--space-2);
  font-size: 1rem;
  font-weight: 700;
}

.price-box {
  margin-top: var(--space-6);
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  padding: var(--space-5) var(--space-6);
  border-radius: var(--radius-lg);
  background: rgba(238, 205, 43, 0.18);
  border: 1px solid rgba(238, 205, 43, 0.5);
}

.origin {
  margin: 0;
  color: #8a8a8a;
  text-decoration: line-through;
}

.current {
  margin: 0.2rem 0 0;
  font-size: 2rem;
  font-weight: 800;
  letter-spacing: -0.02em;
}

.stock {
  color: var(--color-muted);
  font-size: 0.82rem;
}

.quantity-row {
  margin-top: var(--space-5);
  display: flex;
  align-items: center;
  gap: var(--space-4);
  font-weight: 600;
}

.action-row {
  margin-top: var(--space-6);
  display: flex;
  gap: var(--space-3);
}

.action-row .el-button {
  height: 2.8rem;
  min-width: 8.8rem;
}

.service-row {
  margin-top: var(--space-5);
  display: flex;
  gap: var(--space-5);
  color: var(--color-muted);
  font-size: 0.82rem;
}

.service-row span {
  display: inline-flex;
  align-items: center;
  gap: 0.35rem;
}

.detail-tabs {
  margin-top: var(--space-8);
  padding: 0 var(--space-6) var(--space-6);
}

.tab-content {
  padding-top: var(--space-6);
  min-height: 12rem;
}

.comment-summary {
  display: flex;
  align-items: center;
  gap: var(--space-6);
  margin-bottom: var(--space-6);
  padding-bottom: var(--space-6);
  border-bottom: 1px solid rgba(24, 24, 24, 0.1);
}

.score strong {
  font-size: 2rem;
  line-height: 1;
}

.score span {
  display: block;
  margin-top: var(--space-2);
  color: var(--color-muted);
  font-size: 0.82rem;
}

.comment-item {
  padding: var(--space-5) 0;
  border-bottom: 1px solid rgba(24, 24, 24, 0.08);
}

.comment-item:last-child {
  border-bottom: 0;
}

.comment-head {
  display: flex;
  align-items: center;
  gap: var(--space-3);
}

.comment-user {
  flex: 1;
}

.comment-user p {
  margin: 0 0 var(--space-2);
  font-weight: 600;
}

.comment-time {
  color: var(--color-muted);
  font-size: 0.78rem;
}

.comment-body {
  margin: var(--space-4) 0 0;
  color: #323232;
  line-height: 1.65;
}
</style>
