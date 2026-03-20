<template>
  <div class="payment-page">
    <div class="page-container">
      <el-breadcrumb separator="/">
        <el-breadcrumb-item :to="{ path: '/home' }">首页</el-breadcrumb-item>
        <el-breadcrumb-item :to="{ path: '/cart' }">购物车</el-breadcrumb-item>
        <el-breadcrumb-item :to="{ path: '/checkout' }">确认订单</el-breadcrumb-item>
        <el-breadcrumb-item>确认支付</el-breadcrumb-item>
      </el-breadcrumb>

      <div v-if="cartStore.checkedItems.length > 0" class="payment-shell">
        <section class="payment-main leyi-panel-strong">
          <div class="section-header">
            <div>
              <h1>确认支付</h1>
              <p>模拟支付流程，不接入真实支付通道</p>
            </div>
            <div class="amount-card">
              <span>待支付金额</span>
              <strong>￥{{ cartStore.checkedTotal.toFixed(2) }}</strong>
            </div>
          </div>

          <div class="payment-method leyi-panel">
            <div class="method-header">
              <el-icon><Wallet /></el-icon>
              <span>支付方式</span>
            </div>
            <div class="method-card selected">
              <div>
                <strong>模拟支付</strong>
                <p>点击确认后直接视为支付成功，生成取货码</p>
              </div>
              <el-icon><CircleCheckFilled /></el-icon>
            </div>
          </div>

          <div class="goods-section leyi-panel">
            <div class="method-header">
              <el-icon><Goods /></el-icon>
              <span>支付商品</span>
            </div>
            <article v-for="item in cartStore.checkedItems" :key="item.id" class="goods-row">
              <div class="goods-info">
                <img :src="getImageUrl(item.goodsImage)" :alt="item.goodsName" @error="handleImageError" />
                <div>
                  <h3>{{ item.goodsName }}</h3>
                  <p>数量 x {{ item.quantity }}</p>
                </div>
              </div>
              <span class="goods-price">￥{{ (item.goodsPrice * item.quantity).toFixed(2) }}</span>
            </article>
          </div>

          <div class="remark-section leyi-panel">
            <div class="method-header">
              <el-icon><EditPen /></el-icon>
              <span>订单备注</span>
            </div>
            <p class="remark-text">{{ remark || '无备注' }}</p>
          </div>
        </section>

        <aside class="payment-side leyi-panel-strong">
          <h3>支付确认</h3>
          <div class="summary-row">
            <span>商品总额</span>
            <strong>￥{{ cartStore.checkedTotal.toFixed(2) }}</strong>
          </div>
          <div class="summary-row">
            <span>支付方式</span>
            <strong>模拟支付</strong>
          </div>
          <div class="summary-total">
            <span>应付金额</span>
            <strong>￥{{ cartStore.checkedTotal.toFixed(2) }}</strong>
          </div>

          <el-button class="pay-btn" type="primary" :loading="loading" @click="handlePay">
            确认支付
          </el-button>
          <el-button class="back-btn" @click="goBack">返回修改</el-button>
        </aside>
      </div>

      <div v-else class="empty-panel leyi-panel-strong">
        <el-empty description="当前没有可支付商品">
          <el-button type="primary" @click="router.push('/cart')">返回购物车</el-button>
        </el-empty>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { CircleCheckFilled } from '@element-plus/icons-vue'
import { createOrder } from '@/api/order'
import { useCartStore } from '@/stores/cart'
import { getImageUrl, handleImageError } from '@/utils/image'

const router = useRouter()
const cartStore = useCartStore()

const loading = ref(false)
const remark = ref('')

onMounted(async () => {
  remark.value = sessionStorage.getItem('checkout.remark') || ''
  try {
    await cartStore.fetchCart()
  } catch (error) {
    console.error(error)
  }
})

const handlePay = async () => {
  if (cartStore.checkedItems.length === 0) {
    ElMessage.warning('当前没有可支付商品')
    router.replace('/cart')
    return
  }

  loading.value = true
  try {
    const res = await createOrder(remark.value)
    sessionStorage.removeItem('checkout.remark')
    await cartStore.fetchCart()
    router.replace({
      path: '/order/success',
      query: {
        orderNo: res.data.orderNo,
        verifyCode: res.data.verifyCode
      }
    })
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const goBack = () => {
  router.push('/checkout')
}
</script>

<style lang="scss" scoped>
.payment-shell {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 22rem;
  gap: var(--space-6);
  align-items: start;
}

.payment-main {
  padding: var(--space-6);
  display: flex;
  flex-direction: column;
  gap: var(--space-5);
}

.section-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: var(--space-4);

  h1 {
    margin: 0;
    font-size: 2rem;
    font-weight: 800;
  }

  p {
    margin: 0.5rem 0 0;
    color: var(--color-muted);
  }
}

.amount-card {
  min-width: 11rem;
  padding: var(--space-4) var(--space-5);
  border-radius: var(--radius-lg);
  background: rgba(238, 205, 43, 0.15);
  border: 1px solid rgba(238, 205, 43, 0.4);

  span {
    display: block;
    color: var(--color-muted);
    font-size: 0.82rem;
  }

  strong {
    display: block;
    margin-top: var(--space-2);
    font-size: 1.8rem;
    font-weight: 800;
  }
}

.payment-method,
.goods-section,
.remark-section {
  padding: var(--space-5);
}

.method-header {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.95rem;
  font-weight: 700;
  margin-bottom: var(--space-4);
}

.method-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-4);
  padding: var(--space-4);
  border-radius: var(--radius-lg);
  border: 1px solid rgba(24, 24, 24, 0.12);
  background: #fff;

  strong {
    display: block;
    font-size: 1rem;
  }

  p {
    margin: 0.35rem 0 0;
    color: var(--color-muted);
    font-size: 0.82rem;
  }

  &.selected {
    border-color: rgba(238, 205, 43, 0.9);
    background: rgba(238, 205, 43, 0.08);
  }
}

.goods-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-4);
  padding: var(--space-4) 0;
  border-bottom: 1px solid rgba(24, 24, 24, 0.08);

  &:last-child {
    border-bottom: none;
    padding-bottom: 0;
  }
}

.goods-info {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  min-width: 0;

  img {
    width: 4.5rem;
    height: 4.5rem;
    border-radius: var(--radius-md);
    object-fit: cover;
  }

  h3 {
    margin: 0;
    font-size: 0.92rem;
    font-weight: 700;
  }

  p {
    margin: 0.35rem 0 0;
    color: var(--color-muted);
    font-size: 0.8rem;
  }
}

.goods-price {
  font-size: 1rem;
  font-weight: 800;
}

.remark-text {
  margin: 0;
  color: var(--color-text);
  line-height: 1.7;
}

.payment-side {
  position: sticky;
  top: 8.7rem;
  padding: var(--space-6);
}

.payment-side h3 {
  margin: 0 0 var(--space-5);
  font-size: 1.05rem;
  font-weight: 700;
}

.summary-row {
  height: 2.2rem;
  display: flex;
  align-items: center;
  justify-content: space-between;
  color: var(--color-muted);

  strong {
    color: var(--color-text);
  }
}

.summary-total {
  margin-top: var(--space-4);
  padding-top: var(--space-4);
  border-top: 1px solid rgba(24, 24, 24, 0.1);
  display: flex;
  align-items: baseline;
  justify-content: space-between;

  strong {
    font-size: 1.35rem;
    font-weight: 800;
  }
}

.pay-btn,
.back-btn {
  width: 100%;
  margin-top: var(--space-4);
  height: 2.8rem;
}

.back-btn {
  margin-top: var(--space-2);
}

.empty-panel {
  padding: var(--space-16);
}
</style>
