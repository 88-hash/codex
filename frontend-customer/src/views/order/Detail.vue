<template>
  <div class="order-detail-page" v-if="order">
    <div class="page-container">
      <el-breadcrumb separator="/" class="breadcrumb">
        <el-breadcrumb-item :to="{ path: '/home' }">首页</el-breadcrumb-item>
        <el-breadcrumb-item :to="{ path: '/orders' }">我的订单</el-breadcrumb-item>
        <el-breadcrumb-item>订单详情</el-breadcrumb-item>
      </el-breadcrumb>
      
      <!-- 订单状态 -->
      <div class="status-card card" :class="order.status">
        <div class="status-icon">
          <el-icon :size="48">
            <Tickets v-if="order.status === 'pending'" />
            <CircleCheck v-else-if="order.status === 'completed'" />
            <CircleClose v-else />
          </el-icon>
        </div>
        <div class="status-info">
          <h2>{{ statusText[order.status] }}</h2>
          <p v-if="order.status === 'pending'">请凭取货码到店取货</p>
          <p v-else-if="order.status === 'completed'">感谢您的惠顾</p>
          <p v-else>订单已取消</p>
        </div>
        <div class="status-actions" v-if="order.status === 'pending'">
          <div class="verify-code-mini">
            <span class="label">取货码</span>
            <span class="code">{{ order.verifyCode }}</span>
          </div>
          <el-button type="primary" @click="copyCode">复制取货码</el-button>
        </div>
      </div>
      
      <div class="detail-layout">
        <div class="detail-main">
          <!-- 取货信息 -->
          <div class="card">
            <div class="card-header">取货信息</div>
            <div class="card-body">
              <div class="info-row">
                <span class="label">取货地址</span>
                <span>乐逸零食店 - XX市XX区XX路XX号</span>
              </div>
              <div class="info-row">
                <span class="label">联系电话</span>
                <span>{{ order.userPhone }}</span>
              </div>
              <div class="info-row" v-if="order.remark">
                <span class="label">备注</span>
                <span>{{ order.remark }}</span>
              </div>
            </div>
          </div>
          
          <!-- 商品信息 -->
          <div class="card">
            <div class="card-header">商品信息</div>
            <div class="card-body">
              <el-table :data="order.items" border>
                <el-table-column label="商品" min-width="250">
                  <template #default="{ row }">
                    <div class="goods-cell">
                      <img :src="getImageUrl(row.goodsImage)" :alt="row.goodsName" @error="handleImageError">
                      <span>{{ row.goodsName }}</span>
                    </div>
                  </template>
                </el-table-column>
                <el-table-column prop="price" label="单价" width="100" align="center">
                  <template #default="{ row }">¥{{ row.price?.toFixed(2) }}</template>
                </el-table-column>
                <el-table-column prop="quantity" label="数量" width="80" align="center" />
                <el-table-column prop="subtotal" label="小计" width="100" align="center">
                  <template #default="{ row }">
                    <span class="subtotal">¥{{ row.subtotal?.toFixed(2) }}</span>
                  </template>
                </el-table-column>
                <el-table-column label="评价" width="100" align="center" v-if="order.status === 'completed'">
                  <template #default="{ row }">
                    <el-button v-if="!row.isCommented" type="primary" text size="small" @click="router.push(`/comment/${row.id}`)">
                      去评价
                    </el-button>
                    <span v-else class="commented">已评价</span>
                  </template>
                </el-table-column>
              </el-table>
            </div>
          </div>
        </div>
        
        <div class="detail-sidebar">
          <!-- 订单信息 -->
          <div class="card">
            <div class="card-header">订单信息</div>
            <div class="card-body">
              <div class="info-row">
                <span class="label">订单编号</span>
                <span>{{ order.orderNo }}</span>
              </div>
              <div class="info-row">
                <span class="label">下单时间</span>
                <span>{{ formatTime(order.createdAt) }}</span>
              </div>
              <div class="info-row" v-if="order.verifyTime">
                <span class="label">核销时间</span>
                <span>{{ formatTime(order.verifyTime) }}</span>
              </div>
            </div>
          </div>
          
          <!-- 费用信息 -->
          <div class="card">
            <div class="card-header">费用信息</div>
            <div class="card-body">
              <div class="info-row">
                <span class="label">商品金额</span>
                <span>¥{{ order.totalPrice?.toFixed(2) }}</span>
              </div>
              <div class="info-row total">
                <span class="label">实付金额</span>
                <span class="total-price">¥{{ order.totalPrice?.toFixed(2) }}</span>
              </div>
            </div>
          </div>
          
          <!-- 操作按钮 -->
          <div v-if="order.status === 'pending'" class="action-card">
            <el-button size="large" block @click="handleCancel">取消订单</el-button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import dayjs from 'dayjs'
import { getOrderDetail, cancelOrder } from '@/api/order'
import { getImageUrl, handleImageError } from '@/utils/image'

const route = useRoute()
const router = useRouter()

const order = ref(null)
const statusText = { pending: '待取货', completed: '已完成', cancelled: '已取消' }

const formatTime = (time) => dayjs(time).format('YYYY-MM-DD HH:mm:ss')

onMounted(async () => {
  try {
    const res = await getOrderDetail(route.params.id)
    order.value = res.data
  } catch (e) {
    console.error(e)
  }
})

const copyCode = async () => {
  try {
    await navigator.clipboard.writeText(order.value.verifyCode)
    ElMessage.success('取货码已复制')
  } catch (e) {
    ElMessage.error('复制失败')
  }
}

const handleCancel = async () => {
  await ElMessageBox.confirm('确定要取消订单吗？', '提示', { type: 'warning' })
  try {
    await cancelOrder(order.value.id)
    ElMessage.success('订单已取消')
    order.value.status = 'cancelled'
  } catch (e) {
    console.error(e)
  }
}
</script>

<style lang="scss" scoped>
.status-card {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 24px;
  margin-bottom: 20px;
  color: var(--text);
  border: 1px solid var(--border);

  &.pending {
    background: #eff6ff;
    border-color: #bfdbfe;
  }

  &.completed {
    background: #ecfdf5;
    border-color: #bbf7d0;
  }

  &.cancelled {
    background: #f3f4f6;
    border-color: #e5e7eb;
  }

  .status-icon {
    width: 52px;
    height: 52px;
    border-radius: var(--radius-md);
    display: inline-flex;
    align-items: center;
    justify-content: center;
    background: rgba(255, 255, 255, 0.75);
  }

  .status-info {
    flex: 1;

    h2 {
      font-size: 24px;
      line-height: 32px;
      margin-bottom: 2px;
      color: var(--text);
    }

    p {
      color: var(--text-secondary);
      margin: 0;
    }
  }

  .status-actions {
    display: flex;
    align-items: center;
    gap: 14px;
  }

  .verify-code-mini {
    text-align: center;

    .label {
      display: block;
      font-size: 12px;
      color: var(--muted);
      margin-bottom: 4px;
    }

    .code {
      font-size: 28px;
      font-weight: 700;
      letter-spacing: 4px;
      color: var(--text);
    }
  }
}

.detail-layout {
  display: flex;
  gap: 20px;
}

.detail-main {
  flex: 1;

  .card {
    margin-bottom: 18px;
  }
}

.detail-sidebar {
  width: 320px;

  .card {
    margin-bottom: 18px;
  }
}

.info-row {
  display: flex;
  justify-content: space-between;
  padding: 10px 0;
  border-bottom: 1px solid var(--border);

  &:last-child {
    border-bottom: none;
  }

  .label {
    color: var(--muted);
  }

  &.total {
    padding-top: 14px;
    margin-top: 8px;
    border-top: 1px solid var(--border);
    border-bottom: none;

    .total-price {
      color: var(--primary);
      font-size: 20px;
      font-weight: 700;
    }
  }
}

.goods-cell {
  display: flex;
  align-items: center;
  gap: 12px;

  img {
    width: 50px;
    height: 50px;
    object-fit: cover;
    border-radius: 8px;
    border: 1px solid var(--border);
    background: var(--surface-soft);
  }
}

.subtotal {
  color: var(--primary);
  font-weight: 600;
}

.commented {
  color: var(--success);
  font-size: 12px;
}

.action-card {
  .el-button {
    width: 100%;
  }
}
</style>

