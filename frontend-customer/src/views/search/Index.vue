<template>
  <div class="search-page">
    <div class="page-container">
      <el-breadcrumb separator="/" class="breadcrumb">
        <el-breadcrumb-item :to="{ path: '/home' }">首页</el-breadcrumb-item>
        <el-breadcrumb-item>搜索结果</el-breadcrumb-item>
      </el-breadcrumb>
      
      <div class="search-header card">
        <span class="search-info">
          搜索 "<strong>{{ keyword }}</strong>" 共找到 <strong>{{ total }}</strong> 件商品
        </span>
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
      
      <el-empty v-else description="未找到相关商品" />
      
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
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getGoodsList } from '@/api/goods'
import { useCartStore } from '@/stores/cart'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const cartStore = useCartStore()
const userStore = useUserStore()

const keyword = ref('')
const goodsList = ref([])
const pageNum = ref(1)
const pageSize = ref(20)
const total = ref(0)

onMounted(() => {
  keyword.value = route.query.keyword || ''
  if (keyword.value) {
    fetchGoods()
  }
})

watch(() => route.query.keyword, (newKeyword) => {
  keyword.value = newKeyword || ''
  pageNum.value = 1
  fetchGoods()
})

const fetchGoods = async () => {
  if (!keyword.value) return
  try {
    const res = await getGoodsList({
      keyword: keyword.value,
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
.search-header {
  padding: 16px 20px;
  margin-bottom: 20px;
  
  .search-info {
    color: #5f6368;
    
    strong {
      color: #1a73e8;
    }
  }
}
</style>
