<template>
  <div class="orders-page">
    <div class="page-container">
      <el-breadcrumb separator="/" class="breadcrumb">
        <el-breadcrumb-item :to="{ path: '/home' }">首页</el-breadcrumb-item>
        <el-breadcrumb-item>我的订单</el-breadcrumb-item>
      </el-breadcrumb>

      <div class="orders-shell card">
        <div class="orders-top">
          <h2 class="orders-title">我的订单</h2>
          <el-radio-group v-model="activeStatus" class="status-chips" @change="fetchOrders">
            <el-radio-button label="">全部</el-radio-button>
            <el-radio-button label="unpaid">待付款</el-radio-button>
            <el-radio-button label="shipping">待发货</el-radio-button>
            <el-radio-button label="pending">待收货</el-radio-button>
            <el-radio-button label="completed">已完成</el-radio-button>
            <el-radio-button label="cancelled">已取消</el-radio-button>
          </el-radio-group>
        </div>

        <div class="orders-content">
          <div v-if="orders.length > 0" class="orders-list">
            <div v-for="order in orders" :key="order.id" class="order-item">
              <div class="order-header">
                <div class="order-info">
                  <span class="order-no">订单编号：{{ order.orderNo }}</span>
                  <span class="order-time">{{ formatTime(order.createdAt) }}</span>
                </div>
                <el-tag :class="['status-badge', `status-${order.status || 'default'}`]">
                  {{ statusText[order.status] || '处理中' }}
                </el-tag>
              </div>

              <div class="order-goods">
                <div v-for="item in order.items" :key="item.id" class="goods-item">
                  <div class="goods-info">
                    <img :src="getImageUrl(item.goodsImage)" :alt="item.goodsName" @error="handleImageError">
                    <div class="goods-meta">
                      <h4>{{ item.goodsName }}</h4>
                      <div class="meta-row">
                        <span class="price">￥{{ item.price?.toFixed(2) }}</span>
                        <span class="qty">x{{ item.quantity }}</span>
                      </div>
                    </div>
                  </div>

                  <div class="item-side">
                    <span class="line-subtotal">￥{{ (item.price * item.quantity).toFixed(2) }}</span>
                    <div class="item-actions" v-if="order.status === 'completed' && !item.isCommented">
                      <el-button type="primary" size="small" @click="goComment(item.id)">评价</el-button>
                    </div>
                  </div>
                </div>
              </div>

              <div class="order-footer">
                <div class="order-total">
                  共 {{ order.items?.length }} 件商品，合计：
                  <span class="total-price">￥{{ order.totalPrice?.toFixed(2) }}</span>
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

          <div v-else class="orders-empty">
            <el-empty description="暂无订单，先去挑点快乐零食吧" />
          </div>
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
import { getImageUrl, handleImageError } from '@/utils/image'

const route = useRoute()
const router = useRouter()

const activeStatus = ref(route.query.status || '')
const orders = ref([])
const codeVisible = ref(false)
const currentOrder = ref(null)

const statusText = {
  unpaid: '待付款',
  shipping: '待发货',
  pending: '待收货',
  completed: '已完成',
  cancelled: '已取消'
}

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
.orders-shell {
  border: 2px solid #000;
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-md);
  background: #fffef7;
  padding: 18px;
}

.orders-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 18px;
}

.orders-title {
  font-size: 28px;
  line-height: 1.2;
  color: #000;
  font-weight: 900;
}

.status-chips {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.status-chips :deep(.el-radio-button__inner) {
  border: 2px solid #000 !important;
  border-radius: var(--radius-pill) !important;
  box-shadow: 3px 3px 0 #000;
  background: #fff;
  color: #000;
  font-weight: 800;
}

.status-chips :deep(.el-radio-button__original-radio:checked + .el-radio-button__inner) {
  background: #ffd700 !important;
  color: #000 !important;
}

.orders-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.order-item {
  border: 2px solid #000;
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-sm);
  overflow: hidden;
  background: #fff;
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  padding: 14px 16px;
  background: #fff4b4;
  border-bottom: 2px solid #000;
}

.order-info {
  display: flex;
  align-items: center;
  gap: 20px;
}

.order-no {
  color: #000;
  font-weight: 800;
}

.order-time {
  color: #333;
  font-weight: 700;
}

.status-badge {
  border: 2px solid #000;
  border-radius: 999px;
  box-shadow: 2px 2px 0 #000;
  color: #000;
  font-weight: 800;
  background: #fff;
}

.status-pending {
  background: #00bfff;
}

.status-completed {
  background: #00e676;
}

.status-cancelled {
  background: #d7dde3;
}

.status-unpaid {
  background: #ffd700;
}

.status-shipping {
  background: #ff69b4;
}

.order-goods {
  padding: 14px 16px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.goods-item {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 150px;
  align-items: center;
  gap: 12px;
  padding: 10px 12px;
  border: 2px solid #000;
  border-radius: 18px;
  box-shadow: 3px 3px 0 #000;
  background: #fffef7;
}

.goods-info {
  display: flex;
  align-items: center;
  gap: 12px;
  min-width: 0;

  img {
    width: 72px;
    height: 72px;
    object-fit: cover;
    border: 2px solid #000;
    border-radius: 14px;
    box-shadow: 2px 2px 0 #000;
    background: #fff;
    display: block;
  }
}

.goods-meta {
  min-width: 0;

  h4 {
    margin: 0 0 6px;
    font-size: 15px;
    line-height: 1.4;
    color: #000;
    font-weight: 800;
  }
}

.meta-row {
  display: flex;
  gap: 12px;
  align-items: center;
}

.price {
  color: #000;
  font-weight: 800;
}

.qty {
  color: #444;
  font-weight: 700;
}

.item-side {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 8px;
}

.line-subtotal {
  color: #000;
  font-size: 18px;
  font-weight: 900;
}

.order-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  padding: 14px 16px;
  background: #fff4b4;
  border-top: 2px solid #000;
}

.order-total {
  color: #111;
  font-weight: 700;
}

.total-price {
  color: #000;
  font-size: 24px;
  font-weight: 900;
}

.order-actions {
  display: flex;
  gap: 10px;
}

.orders-empty {
  border: 2px solid #000;
  border-radius: 20px;
  box-shadow: 4px 4px 0 #000;
  background: #fff4b4;
  padding: 26px;
}

.verify-code-dialog {
  text-align: center;
  padding: 16px 0;
  border: 2px solid #000;
  border-radius: 20px;
  box-shadow: 4px 4px 0 #000;
  background: #fffef7;

  .code {
    font-size: 46px;
    font-weight: 900;
    letter-spacing: 8px;
    color: #000;
    margin-bottom: 10px;
  }

  p {
    color: #222;
    margin-bottom: 16px;
    font-weight: 700;
  }
}
</style>
