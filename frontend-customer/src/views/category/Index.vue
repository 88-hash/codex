<template>
  <div class="category-page">
    <div class="page-container">
      <el-breadcrumb separator="/" class="breadcrumb">
        <el-breadcrumb-item :to="{ path: '/home' }">首页</el-breadcrumb-item>
        <el-breadcrumb-item>商品分类</el-breadcrumb-item>
      </el-breadcrumb>
      
      <div class="category-layout">
        <!-- 左侧分类 -->
        <div class="category-sidebar card">
          <div class="sidebar-header">商品分类</div>
          <div class="category-list">
            <div 
              v-for="cat in categories" 
              :key="cat.id"
              class="category-group"
            >
              <div 
                class="category-title"
                :class="{ active: activeCategory === cat.id && !activeSubCategory }"
                @click="selectCategory(cat)"
              >
                {{ cat.name }}
              </div>
              <div class="sub-categories">
                <span 
                  v-for="sub in cat.children" 
                  :key="sub.id"
                  class="sub-item"
                  :class="{ active: activeSubCategory === sub.id }"
                  @click="selectSubCategory(sub.id, cat.id)"
                >
                  {{ sub.name }}
                </span>
              </div>
            </div>
          </div>
        </div>
        
        <!-- 右侧商品 -->
        <div class="category-main">
          <div class="filter-bar card">
            <div class="filter-left">
              <span>共 <strong>{{ total }}</strong> 件商品</span>
            </div>
            <div class="filter-right">
              <el-select v-model="sortBy" placeholder="排序" size="small" style="width: 120px">
                <el-option label="默认排序" value="" />
                <el-option label="价格从低到高" value="price_asc" />
                <el-option label="价格从高到低" value="price_desc" />
              </el-select>
            </div>
          </div>
          
          <div v-if="goodsList.length > 0" class="goods-grid">
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
          
          <el-empty v-else description="暂无商品" />
          
          <el-pagination
            v-if="total > 0"
            v-model:current-page="pageNum"
            :page-size="pageSize"
            :total="total"
            layout="prev, pager, next"
            @current-change="fetchGoods"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getCategoryList, getGoodsList } from '@/api/goods'
import { useCartStore } from '@/stores/cart'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const route = useRoute()
const cartStore = useCartStore()
const userStore = useUserStore()

const categories = ref([])
const activeCategory = ref(null)
const activeSubCategory = ref(null)
const goodsList = ref([])
const pageNum = ref(1)
const pageSize = ref(15)
const total = ref(0)
const sortBy = ref('')

onMounted(async () => {
  try {
    const res = await getCategoryList()
    categories.value = res.data || []
    
    if (route.query.id) {
      const catId = Number(route.query.id)
      const cat = categories.value.find(c => c.id === catId)
      if (cat) {
        selectCategory(cat)
        return
      }
    }
    
    if (categories.value.length > 0) {
      selectCategory(categories.value[0])
    }
  } catch (e) {
    console.error(e)
  }
})

watch(() => route.query.id, (newId) => {
  if (newId) {
    const cat = categories.value.find(c => c.id === Number(newId))
    if (cat) {
      selectCategory(cat)
    }
  }
})

const selectCategory = (cat) => {
  activeCategory.value = cat.id
  activeSubCategory.value = null
  pageNum.value = 1
  fetchGoods()
}

const selectSubCategory = (subId, parentId) => {
  activeCategory.value = parentId
  activeSubCategory.value = subId
  pageNum.value = 1
  fetchGoods()
}

const fetchGoods = async () => {
  try {
    const res = await getGoodsList({
      categoryId: activeSubCategory.value || activeCategory.value,
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      isOnSale: 1
    })
    goodsList.value = res.data?.list || []
    total.value = res.data?.total || 0
  } catch (e) {
    console.error(e)
  }
}

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
.category-layout {
  display: flex;
  gap: 24px;
}

.category-sidebar {
  width: 240px;
  flex-shrink: 0;
  height: fit-content;
  position: sticky;
  top: 100px; /* 调整顶部吸附位置 */
  
  .sidebar-header {
    padding: 16px 20px;
    font-weight: 600;
    font-size: 16px;
    border-bottom: 2px solid var(--bg-secondary);
    color: var(--primary-color);
  }
  
  .category-list {
    padding: 12px 0;
  }
  
  .category-group {
    margin-bottom: 8px;
  }
  
  .category-title {
    padding: 10px 20px;
    font-weight: 500;
    cursor: pointer;
    transition: all 0.2s;
    border-left: 4px solid transparent;
    
    &:hover, &.active {
      color: var(--primary-color);
      background: var(--bg-secondary);
      border-left-color: var(--primary-color);
    }
  }
  
  .sub-categories {
    padding: 0 20px 8px;
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
  }
  
  .sub-item {
    font-size: 13px;
    color: var(--text-secondary);
    cursor: pointer;
    padding: 4px 12px;
    border-radius: 14px;
    transition: all 0.2s;
    
    &:hover {
      color: var(--primary-color);
      background: rgba(255, 107, 107, 0.1);
    }
    
    &.active {
      background: var(--primary-color);
      color: white;
      box-shadow: 0 2px 6px rgba(255, 107, 107, 0.3);
    }
  }
}

.category-main {
  flex: 1;
  min-width: 0;
}

.filter-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 24px;
  margin-bottom: 20px;
  
  .filter-left {
    color: var(--text-secondary);
    
    strong {
      color: var(--primary-color);
      font-size: 18px;
    }
  }
}

.goods-grid {
  grid-template-columns: repeat(4, 1fr);
  
  @media (max-width: 1200px) {
    grid-template-columns: repeat(3, 1fr);
  }
}
</style>
