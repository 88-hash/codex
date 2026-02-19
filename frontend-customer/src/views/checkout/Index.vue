<template>
  <div class="checkout-page">
    <div class="page-container">
      <el-breadcrumb separator="/" class="breadcrumb">
        <el-breadcrumb-item :to="{ path: '/home' }">首页</el-breadcrumb-item>
        <el-breadcrumb-item :to="{ path: '/cart' }">购物车</el-breadcrumb-item>
        <el-breadcrumb-item>确认订单</el-breadcrumb-item>
      </el-breadcrumb>

      <div class="checkout-layout">
        <div class="checkout-main">
          <div class="checkout-card pickup-card">
            <div class="card-header">
              <el-icon><Location /></el-icon>
              收货信息
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

          <div class="checkout-card payment-card">
            <div class="card-header">
              <el-icon><Goods /></el-icon>
              支付方式
            </div>
            <div class="card-body">
              <div class="payment-chip">线上支付（下单后到店核销）</div>
            </div>
          </div>

          <div class="checkout-card goods-card">
            <div class="card-header">
              <el-icon><Goods /></el-icon>
              商品清单
            </div>
            <div class="card-body">
              <div class="goods-list">
                <div v-for="row in cartStore.checkedItems" :key="row.id" class="goods-row">
                  <div class="goods-info">
                    <img :src="getImageUrl(row.goodsImage)" :alt="row.goodsName" @error="handleImageError">
                    <div class="goods-meta">
                      <h4>{{ row.goodsName }}</h4>
                      <p>数量 x {{ row.quantity }}</p>
                    </div>
                  </div>
                  <div class="goods-price">￥{{ row.goodsPrice?.toFixed(2) }}</div>
                  <div class="goods-subtotal">￥{{ (row.goodsPrice * row.quantity).toFixed(2) }}</div>
                </div>
              </div>
            </div>
          </div>

          <div class="checkout-card remark-card">
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
        </div>

        <div class="checkout-summary checkout-card">
          <div class="card-header">
            <el-icon><Location /></el-icon>
            订单汇总
          </div>
          <div class="card-body">
            <div class="summary-row">
              <span>商品件数</span>
              <span>{{ cartStore.checkedItems.length }} 件</span>
            </div>
            <div class="summary-row">
              <span>商品金额</span>
              <span>￥{{ cartStore.checkedTotal.toFixed(2) }}</span>
            </div>
            <div class="summary-row">
              <span>运费</span>
              <span>￥0.00</span>
            </div>
            <div class="summary-row total">
              <span>应付金额</span>
              <span class="total-price">￥{{ cartStore.checkedTotal.toFixed(2) }}</span>
            </div>
            <div class="summary-actions">
              <el-button class="ghost-btn" @click="router.push('/cart')">返回购物车</el-button>
              <el-button class="submit-btn" type="primary" :loading="loading" @click="handleSubmit">
                提交订单
              </el-button>
            </div>
          </div>
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
import { getImageUrl, handleImageError } from '@/utils/image'

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
.checkout-layout {
  display: grid;
  grid-template-columns: minmax(0, 7fr) minmax(320px, 3fr);
  gap: 24px;
  align-items: flex-start;
}

.checkout-main {
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.checkout-card {
  border: 2px solid #000;
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-md);
  background: #fffef7;
  overflow: hidden;

  .card-header {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 18px 22px;
    border-bottom: 2px solid #000;
    background: #fff4b4;
    font-size: 18px;
    font-weight: 900;
    color: #000;

    .el-icon {
      color: #000;
      font-size: 18px;
    }
  }

  .card-body {
    padding: 18px 22px;
  }
}

.pickup-card .card-header {
  background: #ffcf7d;
}

.payment-card .card-header {
  background: #00e676;
}

.goods-card .card-header {
  background: #00bfff;
}

.remark-card .card-header {
  background: #ff69b4;
}

.address-info {
  display: flex;
  flex-direction: column;
  gap: 12px;

  .info-row {
    display: flex;
    align-items: center;
    min-height: 44px;
    padding: 0 12px;
    border: 2px solid #000;
    border-radius: var(--radius-sm);
    box-shadow: var(--shadow-sm);
    background: #fff;

    .label {
      width: 92px;
      flex: 0 0 92px;
      color: #000;
      font-weight: 800;
    }

    .value {
      color: #111;
      font-weight: 700;
    }
  }
}

.payment-chip {
  display: inline-flex;
  align-items: center;
  min-height: 38px;
  padding: 0 14px;
  border: 2px solid #000;
  border-radius: var(--radius-pill);
  box-shadow: var(--shadow-sm);
  background: #fff;
  color: #000;
  font-weight: 800;
}

.goods-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.goods-row {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 120px 140px;
  align-items: center;
  gap: 10px;
  border: 2px solid #000;
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-sm);
  background: #fff;
  padding: 12px;
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
    border-radius: var(--radius-sm);
    border: 2px solid #000;
    background: #fff;
    box-shadow: var(--shadow-sm);
    display: block;
  }
}

.goods-meta {
  min-width: 0;

  h4 {
    margin: 0 0 6px;
    font-size: 15px;
    line-height: 1.45;
    color: #000;
    font-weight: 800;
  }

  p {
    margin: 0;
    font-size: 12px;
    color: #333;
    font-weight: 700;
  }
}

.goods-price,
.goods-subtotal {
  text-align: center;
  color: #000;
  font-size: 18px;
  font-weight: 900;
}

.remark-card :deep(.el-textarea__inner) {
  min-height: 96px;
}

.checkout-summary {
  position: sticky;
  top: 132px;

  .card-header {
    background: #ffd700;
  }

  .summary-row {
    display: flex;
    justify-content: space-between;
    align-items: center;
    min-height: 44px;
    border-bottom: 2px solid #000;
    color: #000;
    font-weight: 700;
  }

  .summary-row.total {
    margin-top: 8px;
    padding-top: 8px;
    border-bottom: 0;
    font-weight: 900;
  }

  .total-price {
    color: #000;
    font-size: 30px;
    font-weight: 900;
  }
}

.summary-actions {
  margin-top: 18px;
  display: flex;
  flex-direction: column;
  gap: 12px;

  .ghost-btn,
  .submit-btn {
    width: 100%;
    min-height: 44px;
    border: 2px solid #000;
    border-radius: var(--radius-pill);
    box-shadow: var(--shadow-sm);
    font-weight: 900;
    color: #000;
  }

  .ghost-btn {
    background: #fff;
  }

  .submit-btn {
    background: #ffd700;
  }

  .submit-btn:active {
    transform: translate(2px, 2px);
    box-shadow: 2px 2px 0 #000;
  }
}
</style>
