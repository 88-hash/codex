<template>
  <div class="home-page">
    <!-- 轮播图 -->
    <div class="banner-section">
      <div class="page-container banner-container">
        <el-carousel height="420px" :interval="4000">
          <el-carousel-item v-for="(item, index) in banners" :key="index">
            <div class="banner-item" :class="`banner-${index + 1}`">
              <div class="banner-content">
                <h2>{{ item.title }}</h2>
                <p>{{ item.desc }}</p>
                <el-button type="primary" size="large" class="banner-btn" @click="router.push('/category')">
                  立即选购 <el-icon><ArrowRight /></el-icon>
                </el-button>
              </div>
              <div class="banner-img">
                <el-icon :size="180" :color="item.iconColor"><component :is="item.icon" /></el-icon>
              </div>
            </div>
          </el-carousel-item>
        </el-carousel>
      </div>
    </div>
    
    <!-- 分类导航 -->
    <div class="page-container main-container">
      <div class="category-section card">
        <div class="category-grid">
          <div 
            v-for="(cat, index) in categories.slice(0, 3)" 
            :key="cat.id" 
            class="category-item"
            :class="`sticker-${index + 1}`"
            @click="goCategory(cat.id)"
          >
            <div class="category-emoji">
              {{ stickerMeta[index]?.emoji || 'SN' }}
            </div>
            <div class="category-copy">
              <strong>{{ cat.name }}</strong>
              <small>{{ stickerMeta[index]?.subtitle || '点我逛逛' }}</small>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 热销商品 -->
      <div class="section">
        <div class="section-header">
          <h2 class="title-with-icon">
            <span class="icon-bg red"><el-icon><TrendCharts /></el-icon></span>
            热销商品
          </h2>
          <el-button round class="view-more" @click="router.push('/category')">
            查看更多 <el-icon><ArrowRight /></el-icon>
          </el-button>
        </div>
        <div class="goods-grid">
          <div 
            v-for="goods in goodsList" 
            :key="goods.id" 
            class="goods-card"
            @click="goDetail(goods.id)"
          >
            <div class="goods-image">
              <img :src="getImageUrl(goods.imageUrl)" :alt="goods.name" @error="handleImageError">
              <span v-if="goods.promotionTag" class="promo-tag">{{ goods.promotionTag }}</span>
            </div>
            <div class="goods-info">
              <div class="goods-name">{{ goods.name }}</div>
              <div class="goods-bottom">
                <span class="price">{{ goods.price.toFixed(2) }}</span>
                <el-button 
                  type="primary" 
                  size="small" 
                  class="add-cart-btn"
                  @click.stop="addCart(goods)"
                >
                  <el-icon><ShoppingCart /></el-icon>
                </el-button>
              </div>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 新品推荐 -->
      <div class="section new-arrival">
        <div class="section-header">
          <h2 class="title-with-icon">
            <span class="icon-bg blue"><el-icon><Star /></el-icon></span>
            新品推荐
          </h2>
        </div>
        <div class="goods-grid">
          <div 
            v-for="goods in newGoodsList" 
            :key="goods.id" 
            class="goods-card"
            @click="goDetail(goods.id)"
          >
            <div class="goods-image">
              <img :src="getImageUrl(goods.imageUrl)" :alt="goods.name" @error="handleImageError">
              <span v-if="goods.promotionTag" class="promo-tag">{{ goods.promotionTag }}</span>
            </div>
            <div class="goods-info">
              <div class="goods-name">{{ goods.name }}</div>
              <div class="goods-bottom">
                <span class="price">{{ goods.price.toFixed(2) }}</span>
                <el-button 
                  type="primary" 
                  size="small" 
                  class="add-cart-btn"
                  @click.stop="addCart(goods)"
                >
                  <el-icon><ShoppingCart /></el-icon>
                </el-button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getGoodsList, getCategoryList } from '@/api/goods'
import { useCartStore } from '@/stores/cart'
import { useUserStore } from '@/stores/user'
import { getImageUrl, handleImageError } from '@/utils/image'

const router = useRouter()
const cartStore = useCartStore()
const userStore = useUserStore()

const banners = [
  { title: '快乐零食节', desc: '全场满99减20，欢乐享不停', icon: 'Present', iconColor: '#000000' },
  { title: '夏日清凉饮', desc: '冰镇饮料第二件半价', icon: 'ColdDrink', iconColor: '#000000' },
  { title: '网红爆款', desc: '本周热销TOP榜单出炉', icon: 'Trophy', iconColor: '#000000' }
]

const categories = ref([])
const goodsList = ref([])
const newGoodsList = ref([])

const stickerMeta = [
  { emoji: '01', subtitle: '糖果贴贴区' },
  { emoji: '02', subtitle: '饮料冲冲冲' },
  { emoji: '03', subtitle: '饼干超满足' }
]

onMounted(async () => {
  try {
    const catRes = await getCategoryList()
    categories.value = catRes.data || []
    
    const goodsRes = await getGoodsList({ pageNum: 1, pageSize: 10, isOnSale: 1 })
    goodsList.value = goodsRes.data?.list || []
    
    const newRes = await getGoodsList({ pageNum: 1, pageSize: 5, isOnSale: 1 })
    newGoodsList.value = newRes.data?.list || []
  } catch (e) {
    console.error(e)
  }
})

const goCategory = (id) => router.push({ path: '/category', query: { id } })
const goDetail = (id) => router.push(`/goods/${id}`)

const addCart = async (goods) => {
  if (!userStore.isLoggedIn()) {
    router.push('/login')
    return
  }
  try {
    await cartStore.addToCart(goods.id, 1)
    ElMessage.success('已加入购物车')
  } catch (e) {
    console.error(e)
  }
}
</script>

<style lang="scss" scoped>
.banner-container {
  padding-bottom: 0 !important;
  min-height: auto !important;
}

.main-container {
  padding-top: 0 !important;
}

.banner-section {
  padding: 20px 0 76px;
  background: transparent;

  .banner-item {
    height: 100%;
    border: 2px solid #000;
    border-radius: 40px;
    box-shadow: 8px 8px 0 #000;
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 0 72px;
    position: relative;
    overflow: hidden;

    &.banner-1 {
      background: #ffd700;
    }

    &.banner-2 {
      background: #ff69b4;
    }

    &.banner-3 {
      background: #00bfff;
    }

    .banner-content {
      color: #000;
      z-index: 2;

      h2 {
        font-size: 54px;
        line-height: 1.1;
        margin-bottom: 14px;
        font-weight: 900;
        letter-spacing: 1px;
      }

      p {
        font-size: 19px;
        font-weight: 800;
        margin-bottom: 30px;
      }

      .banner-btn {
        background: #ffd700;
        color: #000;
        border: 2px solid #000;
        border-radius: 999px;
        box-shadow: 6px 6px 0 #000;
        font-weight: 900;
        padding: 0 28px;

        &:hover {
          transform: translate(-2px, -2px);
          box-shadow: 8px 8px 0 #000;
        }
      }
    }

    .banner-img {
      z-index: 1;
      opacity: 0.95;
      transform: rotate(-8deg);
      color: #000;
    }
  }
}

.category-section {
  margin-top: -46px;
  position: relative;
  z-index: 10;
  padding: 28px;

  .category-grid {
    display: grid;
    grid-template-columns: repeat(3, minmax(0, 1fr));
    gap: 20px;
  }

  .category-item {
    min-height: 132px;
    border: 2px solid #000;
    border-radius: 32px;
    box-shadow: 6px 6px 0 #000;
    padding: 18px 20px;
    display: flex;
    align-items: center;
    gap: 14px;
    cursor: pointer;
    transition: transform 0.15s ease;

    &.sticker-1 {
      background: #ff69b4;
    }

    &.sticker-2 {
      background: #00bfff;
    }

    &.sticker-3 {
      background: #00e676;
    }

    &:hover {
      transform: translate(-2px, -2px);
    }

    .category-emoji {
      width: 56px;
      height: 56px;
      border: 2px solid #000;
      border-radius: 18px;
      background: #fff;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 30px;
      flex-shrink: 0;
    }

    .category-copy {
      display: flex;
      flex-direction: column;
      gap: 6px;
      color: #000;

      strong {
        font-size: 24px;
        line-height: 30px;
        font-weight: 900;
      }

      small {
        font-size: 14px;
        line-height: 20px;
        font-weight: 700;
      }
    }
  }
}

.section {
  margin-top: 64px;

  .section-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 26px;

    .title-with-icon {
      display: flex;
      align-items: center;
      gap: 12px;
      font-size: 36px;
      line-height: 42px;
      font-weight: 900;
      color: #000;

      .icon-bg {
        width: 46px;
        height: 46px;
        border: 2px solid #000;
        border-radius: 14px;
        box-shadow: 4px 4px 0 #000;
        display: flex;
        align-items: center;
        justify-content: center;
        color: #000;

        &.red {
          background: #ff69b4;
        }

        &.blue {
          background: #00bfff;
        }
      }
    }

    .view-more {
      background: #fff;
      color: #000;
      border: 2px solid #000;
      border-radius: 999px;
      box-shadow: 4px 4px 0 #000;
      font-weight: 900;
      padding: 0 16px;

      &:hover {
        transform: translate(-2px, -2px);
        box-shadow: 6px 6px 0 #000;
      }
    }
  }
}

.goods-grid {
  grid-template-columns: repeat(5, minmax(0, 1fr));
  gap: 24px;
}

.goods-card {
  border: 2px solid #000;
  border-radius: 30px;
  box-shadow: 6px 6px 0 #000;
  background: #fff;

  &:hover {
    transform: translate(-2px, -2px);
    box-shadow: 8px 8px 0 #000;
  }

  &:nth-child(3n + 1) .goods-image {
    background: #fff0fa;
  }

  &:nth-child(3n + 2) .goods-image {
    background: #e9f8ff;
  }

  &:nth-child(3n + 3) .goods-image {
    background: #f2ffef;
  }

  .goods-image {
    border-bottom: 2px solid #000;

    .promo-tag {
      border: 2px solid #000;
      border-radius: 999px;
      background: #ffd700;
      color: #000;
      font-weight: 900;
      box-shadow: 3px 3px 0 #000;
    }
  }

  .goods-info {
    padding: 14px;

    .goods-name {
      font-size: 15px;
      line-height: 22px;
      font-weight: 800;
      min-height: 44px;
      color: #000;
    }

    .goods-bottom .price {
      color: #000;
      font-size: 24px;
      font-weight: 900;
    }

    .goods-bottom .add-cart-btn {
      width: 36px;
      height: 36px;
      min-height: 36px;
      border: 2px solid #000;
      border-radius: 999px;
      background: #00bfff;
      color: #000;
      box-shadow: 3px 3px 0 #000;
      padding: 0;

      &:hover {
        transform: translate(-1px, -1px);
      }
    }
  }
}
</style>



