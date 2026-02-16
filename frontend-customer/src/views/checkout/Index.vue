<template>
  <div class="checkout-page">
    <div class="page-container">
      <el-breadcrumb separator="/" class="breadcrumb">
        <el-breadcrumb-item :to="{ path: '/home' }">首页</el-breadcrumb-item>
        <el-breadcrumb-item :to="{ path: '/cart' }">购物车</el-breadcrumb-item>
        <el-breadcrumb-item>确认订单</el-breadcrumb-item>
      </el-breadcrumb>
      
      <!-- 取货信息 -->
      <div class="card">
        <div class="card-header">
          <el-icon><Location /></el-icon>
          取货信息
        </div>
        <div class="card-body">
          <div class="address-info">
            <div class="info-row">
              <span class="label">取货地址</span>
              <span class="value">乐逸零食店 - XX市XX区XX路XX号</span>
            </div>
            <div class="info-row">
              <span class="label">联系电话</span>
              <span class="value">{{ userStore.userInfo.phone }}</span>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 商品清单 -->
      <div class="card">
        <div class="card-header">
          <el-icon><Goods /></el-icon>
          商品清单
        </div>
        <div class="card-body">
          <el-table :data="cartStore.checkedItems" border>
            <el-table-column label="商品信息" min-width="300">
              <template #default="{ row }">
                <div class="goods-info">
                  <img :src="row.goodsImage || '/placeholder.png'" :alt="row.goodsName">
                  <span>{{ row.goodsName }}</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="goodsPrice" label="单价" width="120" align="center">
              <template #default="{ row }">¥{{ row.goodsPrice?.toFixed(2) }}</template>
            </el-table-column>
            <el-table-column prop="quantity" label="数量" width="100" align="center" />
            <el-table-column label="小计" width="120" align="center">
              <template #default="{ row }">
                <span class="subtotal">¥{{ (row.goodsPrice * row.quantity).toFixed(2) }}</span>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>
      
      <!-- 订单备注 -->
      <div class="card">
        <div class="card-header">
          <el-icon><EditPen /></el-icon>
          订单备注
        </div>
        <div class="card-body">
          <el-input 
            v-model="remark" 
            type="textarea" 
            :rows="3"
            placeholder="选填，可以告诉店家您的特殊需求"
            maxlength="100"
            show-word-limit
          />
        </div>
      </div>
      
      <!-- 结算栏 -->
      <div class="checkout-bar card">
        <div class="bar-left">
          <span>共 <strong>{{ cartStore.checkedItems.length }}</strong> 件商品</span>
        </div>
        <div class="bar-right">
          <div class="total-info">
            <span>应付金额：</span>
            <span class="total-price">¥{{ cartStore.checkedTotal.toFixed(2) }}</span>
          </div>
          <el-button type="primary" size="large" :loading="loading" @click="handleSubmit">
            提交订单
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { createOrder } from '@/api/order'
import { useCartStore } from '@/stores/cart'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const cartStore = useCartStore()
const userStore = useUserStore()

const remark = ref('')
const loading = ref(false)

const handleSubmit = async () => {
  if (cartStore.checkedItems.length === 0) {
    ElMessage.warning('请选择要结算的商品')
    return
  }
  
  loading.value = true
  try {
    const res = await createOrder(remark.value)
    await cartStore.fetchCart()
    router.replace({ 
      path: '/order/success', 
      query: { 
        orderNo: res.data.orderNo,
        verifyCode: res.data.verifyCode 
      } 
    })
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}
</script>

<style lang="scss" scoped>
.card {
  margin-bottom: 20px;
  
  .card-header {
    display: flex;
    align-items: center;
    gap: 8px;
    
    .el-icon {
      color: #1a73e8;
    }
  }
}

.address-info {
  .info-row {
    display: flex;
    padding: 8px 0;
    
    .label {
      width: 100px;
      color: #5f6368;
    }
    
    .value {
      font-weight: 500;
    }
  }
}

.goods-info {
  display: flex;
  align-items: center;
  gap: 12px;
  
  img {
    width: 60px;
    height: 60px;
    object-fit: cover;
    border-radius: 4px;
    background: #f5f5f5;
  }
}

.subtotal {
  color: #ea4335;
  font-weight: 600;
}

.checkout-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  position: sticky;
  bottom: 20px;
  
  .bar-left {
    color: #5f6368;
    
    strong {
      color: #1a73e8;
    }
  }
  
  .bar-right {
    display: flex;
    align-items: center;
    gap: 24px;
    
    .total-info {
      .total-price {
        color: #ea4335;
        font-size: 28px;
        font-weight: 700;
      }
    }
    
    .el-button {
      padding: 12px 48px;
      font-size: 16px;
    }
  }
}
</style>
