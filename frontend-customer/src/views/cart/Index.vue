<template>
  <div class="cart-page">
    <div class="page-container">
      <el-breadcrumb separator="/" class="breadcrumb">
        <el-breadcrumb-item :to="{ path: '/home' }">首页</el-breadcrumb-item>
        <el-breadcrumb-item>购物车</el-breadcrumb-item>
      </el-breadcrumb>
      
      <div class="cart-layout" v-if="cartStore.cartList.length > 0">
        <div class="cart-main card">
          <div class="card-header">
            <el-checkbox 
              :model-value="isAllChecked"
              @change="handleCheckAll"
            >全选</el-checkbox>
            <span class="header-text">商品信息</span>
            <span class="header-text">单价</span>
            <span class="header-text">数量</span>
            <span class="header-text">小计</span>
            <span class="header-text">操作</span>
          </div>
          
          <div class="cart-list">
            <div 
              v-for="item in cartStore.cartList" 
              :key="item.id" 
              class="cart-item"
              :class="{ invalid: !item.goodsIsOnSale }"
            >
              <el-checkbox 
                :model-value="item.isChecked === 1"
                :disabled="!item.goodsIsOnSale"
                @change="(val) => handleCheck(item.id, val)"
              />
              
              <div class="item-info" @click="goDetail(item.goodsId)">
                <img :src="item.goodsImage || '/placeholder.png'" :alt="item.goodsName">
                <div class="info-text">
                  <h4>{{ item.goodsName }}</h4>
                  <span v-if="!item.goodsIsOnSale" class="invalid-tag">商品已下架</span>
                </div>
              </div>
              
              <div class="item-price">
                <span class="price">{{ item.goodsPrice?.toFixed(2) }}</span>
              </div>
              
              <div class="item-quantity">
                <el-input-number 
                  v-if="item.goodsIsOnSale"
                  :model-value="item.quantity"
                  :min="1"
                  :max="item.goodsStock"
                  size="small"
                  @change="(val) => handleQuantity(item.id, val)"
                />
                <span v-else>{{ item.quantity }}</span>
              </div>
              
              <div class="item-subtotal">
                <span class="price">{{ (item.goodsPrice * item.quantity).toFixed(2) }}</span>
              </div>
              
              <div class="item-action">
                <el-button type="danger" text @click="handleDelete(item.id)">删除</el-button>
              </div>
            </div>
          </div>
        </div>
        
        <div class="cart-summary card">
          <div class="card-header">结算信息</div>
          <div class="card-body">
            <div class="summary-row">
              <span>已选商品</span>
              <span>{{ cartStore.checkedItems.length }} 件</span>
            </div>
            <div class="summary-row">
              <span>商品金额</span>
              <span>¥{{ cartStore.checkedTotal.toFixed(2) }}</span>
            </div>
            <div class="summary-row total">
              <span>应付金额</span>
              <span class="price">{{ cartStore.checkedTotal.toFixed(2) }}</span>
            </div>
            <el-button 
              type="primary" 
              size="large" 
              class="checkout-btn"
              :disabled="cartStore.checkedItems.length === 0"
              @click="goCheckout"
            >
              去结算 ({{ cartStore.checkedItems.length }})
            </el-button>
          </div>
        </div>
      </div>
      
      <el-empty v-else description="购物车是空的" class="empty-cart">
        <el-button type="primary" @click="router.push('/home')">去逛逛</el-button>
      </el-empty>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import { useCartStore } from '@/stores/cart'

const router = useRouter()
const cartStore = useCartStore()

onMounted(() => {
  cartStore.fetchCart()
})

const isAllChecked = computed(() => {
  const validItems = cartStore.cartList.filter(item => item.goodsIsOnSale)
  return validItems.length > 0 && validItems.every(item => item.isChecked === 1)
})

const handleCheck = (id, val) => {
  cartStore.updateChecked(id, val ? 1 : 0)
}

const handleCheckAll = (val) => {
  cartStore.updateAllChecked(val ? 1 : 0)
}

const handleQuantity = (id, val) => {
  if (val) {
    cartStore.updateQuantity(id, val)
  }
}

const handleDelete = async (id) => {
  await ElMessageBox.confirm('确定删除该商品吗？', '提示')
  cartStore.removeItem(id)
}

const goDetail = (id) => router.push(`/goods/${id}`)
const goCheckout = () => router.push('/checkout')
</script>

<style lang="scss" scoped>
.cart-layout {
  display: flex;
  gap: 24px;
  align-items: flex-start;
}

.cart-main {
  flex: 1;
  
  .card-header {
    display: grid;
    grid-template-columns: 50px 1fr 120px 150px 120px 100px;
    align-items: center;
    padding: 16px 20px;
    background: #f8f9fa;
    border-bottom: 1px solid #e8eaed;
    gap: 16px;
    
    .header-text {
      text-align: center;
      color: #5f6368;
      font-size: 14px;
    }
  }
}

.cart-list {
  .cart-item {
    display: grid;
    grid-template-columns: 50px 1fr 120px 150px 120px 100px;
    align-items: center;
    padding: 20px;
    border-bottom: 1px solid #f0f0f0;
    gap: 16px;
    
    &.invalid {
      background: #fafafa;
      
      .item-info img {
        opacity: 0.5;
      }
    }
    
    &:last-child {
      border-bottom: none;
    }
  }
  
  .item-info {
    display: flex;
    gap: 16px;
    cursor: pointer;
    
    img {
      width: 80px;
      height: 80px;
      object-fit: cover;
      border-radius: 8px;
      background: #f5f5f5;
    }
    
    .info-text {
      h4 {
        font-size: 14px;
        font-weight: 500;
        margin-bottom: 8px;
        
        &:hover {
          color: #1a73e8;
        }
      }
      
      .invalid-tag {
        color: #ea4335;
        font-size: 12px;
      }
    }
  }
  
  .item-price, .item-subtotal {
    text-align: center;
    
    .price {
      color: #ea4335;
      font-weight: 600;
      
      &::before {
        content: '¥';
        font-size: 12px;
      }
    }
  }
  
  .item-quantity {
    display: flex;
    justify-content: center;
  }
  
  .item-action {
    text-align: center;
  }
}

.cart-summary {
  width: 320px;
  flex-shrink: 0;
  position: sticky;
  top: 180px;
  
  .summary-row {
    display: flex;
    justify-content: space-between;
    padding: 12px 0;
    border-bottom: 1px solid #f0f0f0;
    
    &.total {
      border-bottom: none;
      padding-top: 16px;
      font-size: 16px;
      font-weight: 500;
      
      .price {
        font-size: 24px;
      }
    }
  }
  
  .checkout-btn {
    width: 100%;
    margin-top: 20px;
  }
}

.empty-cart {
  padding: 80px 0;
}
</style>
