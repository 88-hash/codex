<template>
  <div class="detail-page" v-if="goods">
    <div class="page-container">
      <el-breadcrumb separator="/" class="breadcrumb">
        <el-breadcrumb-item :to="{ path: '/home' }">首页</el-breadcrumb-item>
        <el-breadcrumb-item :to="{ path: '/category' }">商品分类</el-breadcrumb-item>
        <el-breadcrumb-item>{{ goods.name }}</el-breadcrumb-item>
      </el-breadcrumb>
      
      <div class="detail-main card">
        <div class="detail-layout">
          <!-- 商品图片 -->
          <div class="goods-gallery">
            <div class="main-image">
              <img :src="currentImage || goods.imageUrl || '/placeholder.png'" :alt="goods.name">
            </div>
            <div class="thumb-list" v-if="images.length > 1">
              <div 
                v-for="img in images" 
                :key="img.id" 
                class="thumb-item"
                :class="{ active: currentImage === img.imageUrl }"
                @mouseenter="currentImage = img.imageUrl"
              >
                <img :src="img.imageUrl" :alt="goods.name">
              </div>
            </div>
          </div>
          
          <!-- 商品信息 -->
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
      
      <!-- 商品评价 -->
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
    goods.value = res.data.goods
    images.value = res.data.goods.images || []
    currentImage.value = images.value[0]?.imageUrl || goods.value.imageUrl
    comments.value = res.data.comments || []
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
.detail-main {
  margin-bottom: 24px;
}

.detail-layout {
  display: flex;
  gap: 40px;
  padding: 24px;
}

.goods-gallery {
  width: 450px;
  flex-shrink: 0;
  
  .main-image {
    width: 100%;
    aspect-ratio: 1;
    border-radius: 8px;
    overflow: hidden;
    background: #f5f5f5;
    margin-bottom: 16px;
    
    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }
  }
  
  .thumb-list {
    display: flex;
    gap: 12px;
  }
  
  .thumb-item {
    width: 80px;
    height: 80px;
    border-radius: 4px;
    overflow: hidden;
    cursor: pointer;
    border: 2px solid transparent;
    transition: border-color 0.2s;
    
    &:hover, &.active {
      border-color: #1a73e8;
    }
    
    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }
  }
}

.goods-info {
  flex: 1;
  
  .goods-name {
    font-size: 24px;
    font-weight: 600;
    margin-bottom: 12px;
    line-height: 1.4;
  }
  
  .goods-desc {
    color: #5f6368;
    margin-bottom: 20px;
    line-height: 1.6;
  }
  
  .price-box {
    background: #fef7f7;
    padding: 20px;
    margin-bottom: 20px;
    border-radius: 8px;
    
    .price-row {
      display: flex;
      align-items: baseline;
      gap: 16px;
      
      .label {
        color: #5f6368;
      }
      
      .price-value {
        color: #ea4335;
        font-size: 32px;
        font-weight: 700;
      }
      
      .promo-tag {
        background: linear-gradient(135deg, #ea4335 0%, #c62828 100%);
        color: white;
        padding: 4px 12px;
        border-radius: 4px;
        font-size: 13px;
      }
    }
  }
  
  .info-rows {
    margin-bottom: 20px;
  }
  
  .info-row {
    display: flex;
    padding: 8px 0;
    border-bottom: 1px solid #f0f0f0;
    
    .label {
      width: 80px;
      color: #5f6368;
    }
  }
  
  .quantity-row {
    display: flex;
    align-items: center;
    gap: 16px;
    margin-bottom: 24px;
    
    .label {
      color: #5f6368;
    }
    
    .stock-tip {
      color: #9aa0a6;
      font-size: 13px;
    }
  }
  
  .action-row {
    display: flex;
    gap: 16px;
    
    .el-button {
      padding: 12px 40px;
      font-size: 16px;
    }
  }
}

.detail-tabs {
  .tab-content {
    padding: 20px;
    min-height: 300px;
  }
}

.comment-summary {
  display: flex;
  align-items: center;
  gap: 16px;
  padding-bottom: 20px;
  margin-bottom: 20px;
  border-bottom: 1px solid #f0f0f0;
  
  .avg-rating {
    .rating-value {
      font-size: 36px;
      font-weight: 700;
      color: #f9ab00;
    }
    
    .rating-text {
      color: #5f6368;
    }
  }
}

.comments-list {
  .comment-item {
    padding: 20px 0;
    border-bottom: 1px solid #f0f0f0;
    
    &:last-child {
      border-bottom: none;
    }
  }
  
  .comment-header {
    display: flex;
    align-items: center;
    gap: 12px;
    margin-bottom: 12px;
    
    .comment-user {
      flex: 1;
      
      .user-name {
        display: block;
        font-weight: 500;
        margin-bottom: 4px;
      }
    }
    
    .comment-time {
      color: #9aa0a6;
      font-size: 13px;
    }
  }
  
  .comment-content {
    color: #5f6368;
    line-height: 1.6;
    margin: 0;
  }
}
</style>
