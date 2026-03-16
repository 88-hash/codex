<template>
  <div class="checkout-page">
    <div class="page-container">
      <el-breadcrumb separator="/">
        <el-breadcrumb-item :to="{ path: '/home' }">首页</el-breadcrumb-item>
        <el-breadcrumb-item :to="{ path: '/cart' }">购物车</el-breadcrumb-item>
        <el-breadcrumb-item>确认订单</el-breadcrumb-item>
      </el-breadcrumb>

      <div v-if="cartStore.checkedItems.length > 0" class="checkout-shell">
        <section class="checkout-main">
          <article class="checkout-card leyi-panel-strong">
            <header>
              <el-icon><Location /></el-icon>
              收货信息
            </header>
            <div class="body address-card">
              <p><span>收货人</span><strong>{{ userStore.userInfo.name || 'LeYi会员' }}</strong></p>
              <p><span>联系电话</span><strong>{{ userStore.userInfo.phone || '未绑定' }}</strong></p>
              <p><span>收货地址</span><strong>LeYi零食店 - XX市XX区XX路XX号</strong></p>
              <el-button text>修改地址</el-button>
            </div>
          </article>

          <article class="checkout-card leyi-panel-strong">
            <header>
              <el-icon><Wallet /></el-icon>
              支付方式
            </header>
            <div class="body payment-card">
              <div class="pay-item selected">
                <span>线上支付（到店核销）</span>
                <el-icon><CircleCheck /></el-icon>
              </div>
            </div>
          </article>

          <article class="checkout-card leyi-panel-strong">
            <header>
              <el-icon><Goods /></el-icon>
              商品清单 ({{ cartStore.checkedItems.length }})
            </header>
            <div class="body items-card">
              <article v-for="row in cartStore.checkedItems" :key="row.id" class="item-row">
                <div class="item-main">
                  <img :src="getImageUrl(row.goodsImage)" :alt="row.goodsName" @error="handleImageError" />
                  <div>
                    <h4>{{ row.goodsName }}</h4>
                    <p>数量 x {{ row.quantity }}</p>
                  </div>
                </div>
                <span class="item-price">¥{{ row.goodsPrice?.toFixed(2) }}</span>
                <span class="item-total">¥{{ (row.goodsPrice * row.quantity).toFixed(2) }}</span>
              </article>
            </div>
          </article>

          <article class="checkout-card leyi-panel-strong">
            <header>
              <el-icon><EditPen /></el-icon>
              订单备注
            </header>
            <div class="body">
              <el-input
                v-model="remark"
                type="textarea"
                :rows="3"
                placeholder="选填，可以告诉店家你的包装/配送备注"
                maxlength="100"
                show-word-limit
              />
            </div>
          </article>
        </section>

        <aside class="checkout-summary leyi-panel-strong">
          <h3>订单摘要</h3>
          <div class="line"><span>商品总额</span><strong>¥{{ cartStore.checkedTotal.toFixed(2) }}</strong></div>
          <div class="line"><span>税费</span><strong>¥0.00</strong></div>

          <div class="total">
            <span>应付金额</span>
            <strong>¥{{ cartStore.checkedTotal.toFixed(2) }}</strong>
          </div>

          <el-button class="submit-btn" type="primary" :loading="loading" @click="handleSubmit">提交订单</el-button>
          <el-button class="back-btn" @click="router.push('/cart')">返回购物车</el-button>
          <p class="safe-tip">提交订单表示你已同意《服务条款》与《隐私政策》</p>
        </aside>
      </div>

      <div v-else class="empty-panel leyi-panel-strong">
        <el-empty description="当前没有可结算商品">
          <el-button type="primary" @click="router.push('/cart')">返回购物车</el-button>
        </el-empty>
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
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}
</script>

<style lang="scss" scoped>
.checkout-shell {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 22rem;
  gap: var(--space-6);
  align-items: start;
}

.checkout-main {
  display: flex;
  flex-direction: column;
  gap: var(--space-5);
}

.checkout-card {
  overflow: hidden;
}

.checkout-card > header {
  height: 3.1rem;
  padding: 0 var(--space-6);
  display: flex;
  align-items: center;
  gap: 0.45rem;
  border-bottom: 1px solid rgba(24, 24, 24, 0.1);
  font-size: 0.95rem;
  font-weight: 700;
  background: #fbfbf8;
}

.checkout-card .body {
  padding: var(--space-6);
}

.address-card p {
  margin: 0 0 var(--space-3);
  display: grid;
  grid-template-columns: 5.2rem minmax(0, 1fr);
  gap: var(--space-3);
  align-items: flex-start;
}

.address-card p:last-child {
  margin-bottom: var(--space-4);
}

.address-card span {
  color: var(--color-muted);
  font-size: 0.82rem;
}

.address-card strong {
  font-size: 0.9rem;
  line-height: 1.5;
}

.payment-card {
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
}

.pay-item {
  height: 2.7rem;
  padding: 0 var(--space-4);
  border-radius: var(--radius-md);
  border: 1px solid rgba(24, 24, 24, 0.12);
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.pay-item.selected {
  border-color: rgba(238, 205, 43, 0.8);
  background: rgba(238, 205, 43, 0.12);
}

.items-card {
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
}

.item-row {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 6.2rem 6.6rem;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-3);
  border-radius: var(--radius-md);
  border: 1px solid rgba(24, 24, 24, 0.08);
}

.item-main {
  min-width: 0;
  display: flex;
  align-items: center;
  gap: var(--space-3);
}

.item-main img {
  width: 4.3rem;
  height: 4.3rem;
  border-radius: var(--radius-sm);
  object-fit: cover;
}

.item-main h4 {
  margin: 0;
  font-size: 0.88rem;
  line-height: 1.45;
  font-weight: 700;
}

.item-main p {
  margin: 0.35rem 0 0;
  font-size: 0.78rem;
  color: var(--color-muted);
}

.item-price,
.item-total {
  text-align: right;
  font-size: 0.88rem;
  font-weight: 700;
}

.checkout-summary {
  position: sticky;
  top: 8.7rem;
  padding: var(--space-6);
}

.checkout-summary h3 {
  margin: 0 0 var(--space-4);
  font-size: 1.05rem;
  font-weight: 700;
}

.line {
  height: 2.1rem;
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 0.85rem;
}

.line span {
  color: var(--color-muted);
}

.total {
  margin-top: var(--space-4);
  padding-top: var(--space-4);
  border-top: 1px solid rgba(24, 24, 24, 0.1);
  display: flex;
  align-items: baseline;
  justify-content: space-between;
}

.total strong {
  font-size: 1.38rem;
  font-weight: 800;
  letter-spacing: -0.02em;
}

.submit-btn,
.back-btn {
  width: 100%;
  margin-top: var(--space-4);
  height: 2.75rem;
}

.back-btn {
  margin-top: var(--space-2);
}

.safe-tip {
  margin: var(--space-3) 0 0;
  color: var(--color-muted);
  font-size: 0.75rem;
  line-height: 1.5;
}

.empty-panel {
  padding: var(--space-16);
}
</style>
