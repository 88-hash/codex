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
            v-for="(cat, index) in categories.slice(0, 8)" 
            :key="cat.id" 
            class="category-item"
            @click="goCategory(cat.id)"
          >
            <div class="category-icon" :style="{ background: getCategoryColor(index) }">
              <el-icon :size="32"><Goods /></el-icon>
            </div>
            <span>{{ cat.name }}</span>
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
              <img :src="goods.imageUrl || '/placeholder.png'" :alt="goods.name">
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
              <img :src="goods.imageUrl || '/placeholder.png'" :alt="goods.name">
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

const router = useRouter()
const cartStore = useCartStore()
const userStore = useUserStore()

const banners = [
  { title: '快乐零食节', desc: '全场满99减20，欢乐享不停', icon: 'Present', iconColor: '#FF6B6B' },
  { title: '夏日清凉饮', desc: '冰镇饮料第二件半价', icon: 'ColdDrink', iconColor: '#48DBFB' },
  { title: '网红爆款', desc: '本周热销TOP榜单出炉', icon: 'Trophy', iconColor: '#FECA57' }
]

const categories = ref([])
const goodsList = ref([])
const newGoodsList = ref([])

const categoryColors = [
  '#FF9FF3', '#FECA57', '#48DBFB', '#FF6B6B', 
  '#1DD1A1', '#54A0FF', '#5F27CD', '#FF9F43'
]

const getCategoryColor = (index) => {
  return categoryColors[index % categoryColors.length]
}

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
  min-height: auto !important; /* 关键修复：消除全局 min-height 带来的巨大空隙 */
}

.main-container {
  padding-top: 0 !important;
}

.banner-section {
  background: #FFF0F5;
  padding: 20px 0 80px; /* 稍微减小顶部和底部 padding */
  
  .banner-item {
    height: 100%;
    border-radius: 24px;
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 0 100px;
    overflow: hidden;
    position: relative;
    box-shadow: 0 10px 30px rgba(0,0,0,0.1);
    
    &.banner-1 { background: linear-gradient(135deg, #FFEAA7 0%, #FAB1A0 100%); }
    &.banner-2 { background: linear-gradient(135deg, #74B9FF 0%, #A29BFE 100%); }
    &.banner-3 { background: linear-gradient(135deg, #55EFC4 0%, #81ECEC 100%); }
    
    .banner-content {
      color: white;
      text-shadow: 0 2px 4px rgba(0,0,0,0.1);
      z-index: 2;
      
      h2 {
        font-size: 48px;
        margin-bottom: 16px;
        font-weight: 800;
      }
      
      p {
        font-size: 20px;
        margin-bottom: 32px;
        font-weight: 500;
      }
      
      .banner-btn {
        background: white;
        color: #FF6B6B;
        border: none;
        font-weight: 700;
        padding: 12px 32px;
        box-shadow: 0 4px 10px rgba(0,0,0,0.1);
        
        &:hover {
          transform: scale(1.05);
        }
      }
    }
    
    .banner-img {
      z-index: 1;
      opacity: 0.9;
      transform: rotate(-10deg);
    }
  }
}

.category-section {
  margin-top: -80px;
  position: relative;
  z-index: 10;
  
  .category-grid {
    display: flex;
    justify-content: space-around;
    padding: 32px;
    flex-wrap: wrap;
    gap: 20px;
  }
  
  .category-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 12px;
    cursor: pointer;
    transition: all 0.3s;
    
    &:hover {
      transform: translateY(-5px);
      
      .category-icon {
        transform: rotate(10deg);
        box-shadow: 0 8px 16px rgba(0,0,0,0.15);
      }
      
      span {
        color: #FF6B6B;
        font-weight: 600;
      }
    }
    
    .category-icon {
      width: 70px;
      height: 70px;
      border-radius: 20px;
      display: flex;
      align-items: center;
      justify-content: center;
      color: white;
      transition: all 0.3s;
      box-shadow: 0 4px 8px rgba(0,0,0,0.1);
    }
    
    span {
      font-size: 15px;
      color: #576574;
      transition: color 0.3s;
    }
  }
}

.section {
  margin-top: 50px;
  
  .section-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 24px;
    
    .title-with-icon {
      display: flex;
      align-items: center;
      gap: 12px;
      font-size: 24px;
      font-weight: 800;
      color: #222F3E;
      
      .icon-bg {
        width: 40px;
        height: 40px;
        border-radius: 12px;
        display: flex;
        align-items: center;
        justify-content: center;
        color: white;
        
        &.red { background: #FF6B6B; }
        &.blue { background: #48DBFB; }
      }
    }
    
    .view-more {
      font-size: 14px;
      color: #8395A7;
      
      &:hover {
        color: #FF6B6B;
        background: #FFF0F5;
      }
    }
  }
}
</style>
