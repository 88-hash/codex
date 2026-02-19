<template>
  <div class="detail-page goods-detail" v-if="goods">
    <div class="page-container">
      <el-breadcrumb separator="/" class="breadcrumb">
        <el-breadcrumb-item :to="{ path: '/home' }">首页</el-breadcrumb-item>
        <el-breadcrumb-item :to="{ path: '/category' }">商品分类</el-breadcrumb-item>
        <el-breadcrumb-item>{{ goods.name }}</el-breadcrumb-item>
      </el-breadcrumb>

      <div class="detail-main card">
        <div class="detail-layout">
          <div class="goods-gallery">
            <div class="main-image">
              <img :src="currentImage || getImageUrl(goods.imageUrl)" :alt="goods.name" @error="handleImageError">
            </div>
            <div class="thumb-list" v-if="images.length > 1">
              <div
                v-for="img in images"
                :key="img.id"
                class="thumb-item"
                :class="{ active: currentImage === img.imageUrl }"
                @mouseenter="currentImage = img.imageUrl"
              >
                <img :src="getImageUrl(img.imageUrl)" :alt="goods.name" @error="handleImageError">
              </div>
            </div>
          </div>

          <div class="goods-info">
            <h1 class="goods-name">{{ goods.name }}</h1>
            <p class="goods-desc" v-if="goods.description">{{ goods.description }}</p>

            <div class="price-box">
              <div class="price-row">
                <span class="label">价格</span>
                <span class="price-value">¥{{ goods.price?.toFixed(2) }}</span>
                <span v-if="goods.promotionTag" class="promo-tag">{{ goods.promotionTag }}</span>
              </div>
            </div>

            <div class="info-rows">
              <div class="info-row">
                <span class="label">库存</span>
                <span>{{ goods.stock }} 件</span>
              </div>
              <div class="info-row" v-if="goods.expireDate">
                <span class="label">保质期至</span>
                <span>{{ goods.expireDate }}</span>
              </div>
              <div class="info-row" v-if="goods.barCode">
                <span class="label">条码</span>
                <span>{{ goods.barCode }}</span>
              </div>
            </div>

            <div class="quantity-row">
              <span class="label">数量</span>
              <el-input-number v-model="quantity" :min="1" :max="goods.stock" size="large" />
              <span class="stock-tip">库存 {{ goods.stock }} 件</span>
            </div>

            <div class="action-row">
              <el-button size="large" @click="addToCart">
                <el-icon><ShoppingCart /></el-icon>
                加入购物车
              </el-button>
              <el-button type="primary" size="large" @click="buyNow">立即购买</el-button>
            </div>
          </div>
        </div>
      </div>

      <div class="detail-tabs card">
        <el-tabs v-model="activeTab">
          <el-tab-pane label="商品详情" name="detail">
            <div class="tab-content">
              <p v-if="goods.description">{{ goods.description }}</p>
              <el-empty v-else description="暂无详情" />
            </div>
          </el-tab-pane>
          <el-tab-pane :label="`商品评价 (${commentCount})`" name="comments">
            <div class="tab-content">
              <div class="comment-summary" v-if="avgRating">
                <div class="avg-rating">
                  <span class="rating-value">{{ avgRating.toFixed(1) }}</span>
                  <span class="rating-text">分</span>
                </div>
                <el-rate :model-value="avgRating" disabled show-score />
              </div>

              <div v-if="comments.length > 0" class="comments-list">
                <div v-for="comment in comments" :key="comment.id" class="comment-item">
                  <div class="comment-header">
                    <el-avatar :size="40" :src="comment.userAvatar">
                      {{ comment.userName?.charAt(0) || 'U' }}
                    </el-avatar>
                    <div class="comment-user">
                      <span class="user-name">{{ comment.userName || comment.userPhone?.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2') }}</span>
                      <el-rate :model-value="comment.rating" disabled size="small" />
                    </div>
                    <span class="comment-time">{{ formatTime(comment.createdAt) }}</span>
                  </div>
                  <p class="comment-content">{{ comment.content }}</p>
                </div>
              </div>
              <el-empty v-else description="暂无评价" />
            </div>
          </el-tab-pane>
        </el-tabs>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
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
  } catch (e) {
    console.error(e)
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
  } catch (e) {
    console.error(e)
  }
}

const buyNow = async () => {
  if (!userStore.isLoggedIn()) {
    router.push({ path: '/login', query: { redirect: route.fullPath } })
    return
  }
  try {
    await cartStore.addToCart(goods.value.id, quantity.value)
    router.push('/checkout')
  } catch (e) {
    console.error(e)
  }
}
</script>

<style lang="scss" scoped>
.goods-detail {
  background: #fffbe6;
}

.goods-detail .page-container {
  max-width: 1200px;
  margin: 0 auto;
}

.goods-detail .detail-main {
  margin-bottom: 24px;
  background: #fffef7;
  border: 2px solid #000;
  border-radius: 28px;
  box-shadow: 6px 6px 0 #000;
}

.goods-detail .detail-layout {
  display: flex;
  align-items: flex-start;
  gap: 24px;
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px;
}

.goods-detail .goods-gallery {
  width: 420px;
  flex: 0 0 420px;
}

.goods-detail .main-image {
  width: 100%;
  aspect-ratio: 1 / 1;
  border: 2px solid #000;
  border-radius: 24px;
  overflow: hidden;
  background: #f3f4f6;
  box-shadow: 5px 5px 0 #000;
  margin-bottom: 16px;
}

.goods-detail .main-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.goods-detail .thumb-list {
  display: flex;
  gap: 12px;
  overflow-x: auto;
  padding-bottom: 4px;
}

.goods-detail .thumb-item {
  width: 76px;
  height: 76px;
  flex: 0 0 76px;
  border: 2px solid #000;
  border-radius: 16px;
  overflow: hidden;
  background: #fff;
  box-shadow: 3px 3px 0 #000;
  cursor: pointer;
}

.goods-detail .thumb-item.active {
  transform: translate(-1px, -1px);
  box-shadow: 4px 4px 0 #000;
  background: #00bfff;
}

.goods-detail .thumb-item img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.goods-detail .goods-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  row-gap: 16px;
}

.goods-detail .goods-name {
  margin: 0;
  font-size: 32px;
  font-weight: 900;
  line-height: 1.25;
  color: #000;
}

.goods-detail .goods-desc {
  margin: 0;
  font-size: 14px;
  line-height: 1.6;
  color: #444;
}

.goods-detail .price-box {
  background: #ffd700;
  border: 2px solid #000;
  border-radius: 18px;
  box-shadow: 4px 4px 0 #000;
  padding: 14px 16px;
}

.goods-detail .price-row {
  display: flex;
  align-items: baseline;
  gap: 12px;
  flex-wrap: wrap;
}

.goods-detail .price-row .label {
  color: #000;
  font-weight: 700;
}

.goods-detail .price-row .price-value {
  color: #000;
  font-size: 40px;
  font-weight: 900;
  line-height: 1;
}

.goods-detail .price-row .promo-tag {
  border: 2px solid #000;
  border-radius: 999px;
  background: #ff69b4;
  color: #000;
  font-size: 12px;
  font-weight: 700;
  padding: 2px 10px;
}

.goods-detail .info-rows {
  display: flex;
  flex-direction: column;
  row-gap: 10px;
}

.goods-detail .info-row {
  display: flex;
  align-items: center;
  min-height: 40px;
  border: 2px solid #000;
  border-radius: 14px;
  background: #fff;
  box-shadow: 3px 3px 0 #000;
  padding: 0 12px;
}

.goods-detail .info-row .label {
  width: 84px;
  flex: 0 0 84px;
  color: #000;
  font-weight: 700;
}

.goods-detail .quantity-row {
  display: flex;
  align-items: center;
  gap: 12px;
}

.goods-detail .quantity-row .label {
  color: #000;
  font-weight: 700;
}

.goods-detail .quantity-row .stock-tip {
  color: #555;
  font-size: 13px;
}

.goods-detail .action-row {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: nowrap;
}

.goods-detail .action-row .el-button {
  height: 42px;
  padding: 0 22px;
  border: 2px solid #000;
  border-radius: 999px;
  box-shadow: 4px 4px 0 #000;
  font-size: 14px;
  font-weight: 700;
  color: #000;
  white-space: nowrap;
}

.goods-detail .action-row .el-button--default {
  background: #ffd700;
}

.goods-detail .action-row .el-button--primary {
  background: #00bfff;
}

.goods-detail .detail-tabs {
  border: 2px solid #000;
  border-radius: 24px;
  box-shadow: 6px 6px 0 #000;
  background: #fff;
}

.goods-detail .tab-content {
  padding: 20px;
  min-height: 220px;
}

.goods-detail .comment-summary {
  display: flex;
  align-items: center;
  gap: 16px;
  padding-bottom: 20px;
  margin-bottom: 20px;
  border-bottom: 2px solid #000;
}

.goods-detail .rating-value {
  font-size: 36px;
  font-weight: 900;
  color: #000;
}

.goods-detail .rating-text {
  color: #444;
}

.goods-detail .comment-item {
  padding: 20px 0;
  border-bottom: 1px solid #d9d9d9;
}

.goods-detail .comment-item:last-child {
  border-bottom: none;
}

.goods-detail .comment-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.goods-detail .comment-user {
  flex: 1;
}

.goods-detail .user-name {
  display: block;
  font-weight: 700;
  margin-bottom: 4px;
}

.goods-detail .comment-time {
  color: #666;
  font-size: 13px;
}

.goods-detail .comment-content {
  color: #333;
  line-height: 1.6;
  margin: 0;
}
</style>
