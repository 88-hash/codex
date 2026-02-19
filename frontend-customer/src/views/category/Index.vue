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
                <span class="title-dot">●</span>
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
            <div class="filter-left chip">
              <span>共 <strong>{{ total }}</strong> 件商品</span>
            </div>
            <div class="filter-right">
              <el-button
                class="tool-chip"
                :class="{ active: sortBy === '' }"
                @click="sortBy = ''"
              >
                默认
              </el-button>
              <el-button
                class="tool-chip"
                :class="{ active: sortBy === 'price_asc' }"
                @click="sortBy = 'price_asc'"
              >
                价格↑
              </el-button>
              <el-button
                class="tool-chip"
                :class="{ active: sortBy === 'price_desc' }"
                @click="sortBy = 'price_desc'"
              >
                价格↓
              </el-button>
            </div>
          </div>
          
          <div v-if="goodsList.length > 0" class="goods-grid">
            <div 
              v-for="(goods, index) in goodsList" 
              :key="goods.id" 
              class="goods-card"
              @click="goDetail(goods.id)"
            >
              <div class="goods-image">
                <img :src="getImageUrl(goods.imageUrl)" :alt="goods.name" @error="handleImageError">
                <span class="sticker-badge" :class="index % 2 === 0 ? 'hot' : 'new'">
                  {{ getStickerTag(goods, index) }}
                </span>
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
          
          <el-empty v-else class="sticker-empty" description="">
            <template #image>
              <div class="empty-icon">🍬</div>
            </template>
            <p class="empty-title">货架正在补货中</p>
            <p class="empty-tip">换个分类看看，惊喜还在下一排。</p>
            <el-button class="empty-btn" type="primary" @click="fetchGoods">刷新陈列</el-button>
          </el-empty>
          
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
import { getImageUrl, handleImageError } from '@/utils/image'

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
  const firstSubId = cat.children?.[0]?.id || null
  activeSubCategory.value = firstSubId
  pageNum.value = 1
  console.log('[category-debug] parent-click', {
    parentCategoryId: cat.id,
    autoSelectedSubCategoryId: firstSubId,
    childCount: cat.children?.length || 0
  })
  fetchGoods('parent-click')
}

const selectSubCategory = (subId, parentId) => {
  activeCategory.value = parentId
  activeSubCategory.value = subId
  pageNum.value = 1
  console.log('[category-debug] sub-click', {
    parentCategoryId: parentId,
    subCategoryId: subId
  })
  fetchGoods('sub-click')
}

const fetchGoods = async (source = 'manual') => {
  const triggerSource = typeof source === 'string' ? source : 'page-change'
  const requestParams = {
    categoryId: activeSubCategory.value || activeCategory.value,
    pageNum: pageNum.value,
    pageSize: pageSize.value,
    isOnSale: 1
  }

  console.log('[category-debug] request', {
    source: triggerSource,
    activeCategoryId: activeCategory.value,
    activeSubCategoryId: activeSubCategory.value,
    params: requestParams
  })

  try {
    const res = await getGoodsList(requestParams)
    goodsList.value = res.data?.list || []
    total.value = res.data?.total || 0
    console.log('[category-debug] response', {
      source: triggerSource,
      activeCategoryId: activeCategory.value,
      activeSubCategoryId: activeSubCategory.value,
      returnedCount: goodsList.value.length,
      total: total.value
    })
  } catch (e) {
    console.log('[category-debug] response-error', {
      source: triggerSource,
      activeCategoryId: activeCategory.value,
      activeSubCategoryId: activeSubCategory.value,
      message: e?.message
    })
    console.error(e)
  }
}

const getStickerTag = (goods, index) => {
  if (goods.promotionTag) return goods.promotionTag
  return index % 2 === 0 ? '热卖' : '新品'
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
  width: 250px;
  flex-shrink: 0;
  height: fit-content;
  padding: 14px;
  position: sticky;
  top: 98px;

  .sidebar-header {
    padding: 14px 14px;
    font-weight: 900;
    font-size: 20px;
    line-height: 1.2;
    border: 2px solid #000;
    border-radius: 20px;
    background: #ffd700;
    box-shadow: 4px 4px 0 #000;
    margin-bottom: 12px;
  }

  .category-list {
    padding: 2px 0;
  }

  .category-group {
    margin-bottom: 10px;
  }

  .category-title {
    padding: 10px 14px;
    font-size: 14px;
    line-height: 20px;
    font-weight: 900;
    color: #000;
    cursor: pointer;
    border: 2px solid #000;
    border-radius: 999px;
    background: #fff;
    box-shadow: 4px 4px 0 #000;
    transition: transform 0.15s ease;
    display: flex;
    align-items: center;
    gap: 8px;

    .title-dot {
      font-size: 10px;
      line-height: 1;
      transform: translateY(-1px);
    }

    &:hover {
      transform: translate(-2px, -2px);
    }

    &.active {
      transform: translate(1px, 1px);
      background: #00bfff;
      color: #000;
      box-shadow: 2px 2px 0 #000;
    }
  }

  .sub-categories {
    padding: 8px 8px 4px;
    display: flex;
    flex-wrap: wrap;
    gap: 6px;
  }

  .sub-item {
    font-size: 12px;
    line-height: 18px;
    color: #000;
    cursor: pointer;
    padding: 3px 10px;
    border-radius: 999px;
    border: 2px solid #000;
    background: #fff;
    box-shadow: 3px 3px 0 #000;
    transition: transform 0.15s ease;

    &:hover {
      transform: translate(-1px, -1px);
    }

    &.active {
      background: #00e676;
      color: #000;
      transform: translate(1px, 1px);
      box-shadow: 1px 1px 0 #000;
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
  padding: 14px 16px;
  margin-bottom: 24px;
  background: #fff;

  .filter-left {
    color: #000;
    background: #ffd700;

    strong {
      color: #000;
      font-size: 18px;
      margin: 0 2px;
    }
  }

  .filter-right {
    display: flex;
    align-items: center;
    gap: 10px;
  }

  .tool-chip {
    min-height: 34px;
    padding: 0 14px;
    border: 2px solid #000;
    border-radius: 999px;
    box-shadow: 4px 4px 0 #000;
    background: #fff;
    color: #000;
    font-weight: 900;
    transition: transform 0.15s ease;

    &:hover {
      transform: translate(-1px, -1px);
    }

    &.active {
      background: #ff69b4;
      transform: translate(1px, 1px);
      box-shadow: 2px 2px 0 #000;
    }
  }
}

.goods-grid {
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 24px;
}

.goods-card {
  border: 2px solid #000;
  border-radius: 30px;
  box-shadow: 6px 6px 0 #000;
  overflow: hidden;
  background: #fffdf2;
  transition: transform 0.15s ease;

  &:hover {
    transform: translate(-2px, -2px);
    box-shadow: 8px 8px 0 #000;
  }

  .goods-image {
    position: relative;
    background: #fff;
    border-bottom: 2px solid #000;

    img {
      width: 100%;
      aspect-ratio: 1;
      object-fit: cover;
      display: block;
    }

    .sticker-badge {
      position: absolute;
      top: 10px;
      left: 10px;
      min-height: 24px;
      padding: 0 10px;
      border: 2px solid #000;
      border-radius: 999px;
      box-shadow: 3px 3px 0 #000;
      font-size: 12px;
      font-weight: 900;
      display: inline-flex;
      align-items: center;
      color: #000;

      &.hot {
        background: #ffd700;
      }

      &.new {
        background: #ff69b4;
      }
    }
  }

  .goods-info {
    padding: 14px;

    .goods-name {
      min-height: 44px;
      font-size: 15px;
      line-height: 22px;
      font-weight: 800;
      color: #000;
      margin-bottom: 12px;
      overflow: hidden;
      display: -webkit-box;
      -webkit-line-clamp: 2;
      -webkit-box-orient: vertical;
    }

    .goods-bottom {
      display: flex;
      align-items: center;
      justify-content: space-between;

      .price {
        font-size: 24px;
        line-height: 1;
        font-weight: 900;
        color: #000;
      }

      .add-cart-btn {
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
}

.sticker-empty {
  margin: 28px 0;
  padding: 20px;
  border: 2px solid #000;
  border-radius: 30px;
  background: #fff;
  box-shadow: 6px 6px 0 #000;

  .empty-icon {
    width: 98px;
    height: 98px;
    margin: 0 auto 14px;
    border: 2px solid #000;
    border-radius: 28px;
    background: #ffd700;
    box-shadow: 4px 4px 0 #000;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 44px;
  }

  .empty-title {
    font-size: 20px;
    line-height: 28px;
    font-weight: 900;
    color: #000;
    margin-bottom: 6px;
  }

  .empty-tip {
    font-size: 14px;
    line-height: 22px;
    font-weight: 700;
    color: #000;
    margin-bottom: 14px;
  }

  .empty-btn {
    border: 2px solid #000;
    background: #ffd700;
    color: #000;
    box-shadow: 4px 4px 0 #000;
    font-weight: 900;
  }
}
</style>

