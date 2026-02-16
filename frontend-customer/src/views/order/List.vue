<template>
  <div class="orders-page">
    <div class="page-container">
      <el-breadcrumb separator="/" class="breadcrumb">
        <el-breadcrumb-item :to="{ path: '/home' }">首页</el-breadcrumb-item>
        <el-breadcrumb-item>我的订单</el-breadcrumb-item>
      </el-breadcrumb>
      
      <div class="card">
        <div class="card-header">
          <span>我的订单</span>
          <el-radio-group v-model="activeStatus" @change="fetchOrders">
            <el-radio-button label="">全部</el-radio-button>
            <el-radio-button label="pending">待取货</el-radio-button>
            <el-radio-button label="completed">已完成</el-radio-button>
            <el-radio-button label="cancelled">已取消</el-radio-button>
          </el-radio-group>
        </div>
        <div class="card-body">
          <div v-if="orders.length > 0" class="orders-list">
            <div v-for="order in orders" :key="order.id" class="order-item">
              <div class="order-header">
                <div class="order-info">
                  <span class="order-no">订单编号：{{ order.orderNo }}</span>
                  <span class="order-time">{{ formatTime(order.createdAt) }}</span>
                </div>
                <el-tag :type="statusType[order.status]">{{ statusText[order.status] }}</el-tag>
              </div>
              
              <div class="order-goods">
                <div v-for="item in order.items" :key="item.id" class="goods-item">
                  <img :src="item.goodsImage || '/placeholder.png'" :alt="item.goodsName">
                  <div class="goods-info">
                    <h4>{{ item.goodsName }}</h4>
                    <div class="goods-meta">
                      <span class="price">¥{{ item.price?.toFixed(2) }}</span>
                      <span class="qty">x{{ item.quantity }}</span>
                    </div>
                  </div>
                  <div class="item-actions" v-if="order.status === 'completed' && !item.isCommented">
                    <el-button type="primary" size="small" @click="goComment(item.id)">评价</el-button>
                  </div>
                </div>
              </div>
              
              <div class="order-footer">
                <div class="order-total">
                  共 {{ order.items?.length }} 件商品，合计：
                  <span class="total-price">¥{{ order.totalPrice?.toFixed(2) }}</span>
                </div>
                <div class="order-actions">
                  <template v-if="order.status === 'pending'">
                    <el-button @click="showVerifyCode(order)">查看取货码</el-button>
                    <el-button type="danger" plain @click="handleCancel(order.id)">取消订单</el-button>
                  </template>
                  <el-button type="primary" @click="router.push(`/order/${order.id}`)">订单详情</el-button>
                </div>
              </div>
            </div>
          </div>
          
          <el-empty v-else description="暂无订单" />
        </div>
      </div>
    </div>
    
    <el-dialog v-model="codeVisible" title="取货码" width="400px" center>
      <div class="verify-code-dialog">
        <div class="code">{{ currentOrder?.verifyCode }}</div>
        <p>请凭此取货码到店取货</p>
        <el-button type="primary" @click="copyCode">复制取货码</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import dayjs from 'dayjs'
import { getOrderList, cancelOrder } from '@/api/order'

const route = useRoute()
const router = useRouter()

const activeStatus = ref(route.query.status || '')
const orders = ref([])
const codeVisible = ref(false)
const currentOrder = ref(null)

const statusText = { pending: '待取货', completed: '已完成', cancelled: '已取消' }
const statusType = { pending: 'primary', completed: 'success', cancelled: 'info' }

const formatTime = (time) => dayjs(time).format('YYYY-MM-DD HH:mm:ss')

onMounted(() => fetchOrders())

const fetchOrders = async () => {
  try {
    const res = await getOrderList(activeStatus.value)
    orders.value = res.data || []
  } catch (e) {
    console.error(e)
  }
}

const showVerifyCode = (order) => {
  currentOrder.value = order
  codeVisible.value = true
}

const copyCode = async () => {
  try {
    await navigator.clipboard.writeText(currentOrder.value.verifyCode)
    ElMessage.success('取货码已复制')
  } catch (e) {
    ElMessage.error('复制失败')
  }
}

const handleCancel = async (id) => {
  await ElMessageBox.confirm('确定要取消订单吗？', '提示', { type: 'warning' })
  try {
    await cancelOrder(id)
    ElMessage.success('订单已取消')
    fetchOrders()
  } catch (e) {
    console.error(e)
  }
}

const goComment = (itemId) => router.push(`/comment/${itemId}`)
</script>

<style lang="scss" scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.orders-list {
  .order-item {
    border: 1px solid #e8eaed;
    border-radius: 8px;
    margin-bottom: 16px;
    overflow: hidden;
    
    &:last-child {
      margin-bottom: 0;
    }
  }
  
  .order-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 12px 16px;
    background: #f8f9fa;
    border-bottom: 1px solid #e8eaed;
    
    .order-info {
      display: flex;
      gap: 24px;
      
      .order-no {
        font-weight: 500;
      }
      
      .order-time {
        color: #5f6368;
      }
    }
  }
  
  .order-goods {
    padding: 16px;
  }
  
  .goods-item {
    display: flex;
    align-items: center;
    gap: 16px;
    padding: 12px 0;
    border-bottom: 1px solid #f0f0f0;
    
    &:last-child {
      border-bottom: none;
    }
    
    img {
      width: 80px;
      height: 80px;
      object-fit: cover;
      border-radius: 4px;
      background: #f5f5f5;
    }
    
    .goods-info {
      flex: 1;
      
      h4 {
        font-size: 14px;
        font-weight: 500;
        margin-bottom: 8px;
      }
      
      .goods-meta {
        display: flex;
        gap: 16px;
        
        .price {
          color: #ea4335;
        }
        
        .qty {
          color: #5f6368;
        }
      }
    }
  }
  
  .order-footer {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 16px;
    background: #fafafa;
    border-top: 1px solid #e8eaed;
    
    .order-total {
      color: #5f6368;
      
      .total-price {
        color: #ea4335;
        font-size: 20px;
        font-weight: 600;
      }
    }
    
    .order-actions {
      display: flex;
      gap: 12px;
    }
  }
}

.verify-code-dialog {
  text-align: center;
  padding: 20px 0;
  
  .code {
    font-size: 48px;
    font-weight: 700;
    letter-spacing: 8px;
    color: #1a73e8;
    margin-bottom: 12px;
  }
  
  p {
    color: #5f6368;
    margin-bottom: 20px;
  }
}
</style>
