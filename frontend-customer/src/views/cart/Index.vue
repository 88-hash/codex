<template>
  <div class="cart-page">
    <div class="page-container">
      <el-breadcrumb separator="/">
        <el-breadcrumb-item :to="{ path: '/home' }">首页</el-breadcrumb-item>
        <el-breadcrumb-item>购物车</el-breadcrumb-item>
      </el-breadcrumb>

      <div v-if="cartStore.cartList.length > 0" class="cart-shell">
        <section class="cart-list leyi-panel-strong">
          <header class="list-toolbar">
            <el-checkbox :model-value="isAllChecked" @change="handleCheckAll">全选</el-checkbox>
            <p>{{ cartStore.cartList.length }} 件商品</p>
          </header>

          <div class="cart-items">
            <article
              v-for="item in cartStore.cartList"
              :key="item.id"
              class="cart-item"
              :class="{ invalid: !item.goodsIsOnSale }"
            >
              <div class="item-check">
                <el-checkbox
                  :model-value="item.isChecked === 1"
                  :disabled="!item.goodsIsOnSale"
                  @change="(val) => handleCheck(item.id, val)"
                />
              </div>

              <div class="item-product" @click="goDetail(item.goodsId)">
                <img :src="getImageUrl(item.goodsImage)" :alt="item.goodsName" @error="handleImageError" />
                <div class="item-copy">
                  <h4>{{ item.goodsName }}</h4>
                  <p>库存 {{ item.goodsStock }} 件</p>
                  <span v-if="!item.goodsIsOnSale" class="invalid-tag">商品已下架</span>
                </div>
              </div>

              <div class="item-price">¥{{ item.goodsPrice?.toFixed(2) }}</div>

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

              <div class="item-subtotal">¥{{ (item.goodsPrice * item.quantity).toFixed(2) }}</div>

              <div class="item-action">
                <el-button text @click="handleDelete(item.id)">删除</el-button>
              </div>
            </article>
          </div>
        </section>

        <aside class="cart-summary leyi-panel-strong">
          <h3>订单概览</h3>
          <div class="summary-row">
            <span>已选商品</span>
            <strong>{{ cartStore.checkedItems.length }} 件</strong>
          </div>
          <div class="summary-row">
            <span>商品金额</span>
            <strong>¥{{ cartStore.checkedTotal.toFixed(2) }}</strong>
          </div>

          <div class="summary-total">
            <span>应付金额</span>
            <strong>¥{{ cartStore.checkedTotal.toFixed(2) }}</strong>
          </div>

          <el-button
            type="primary"
            class="checkout-btn"
            :disabled="cartStore.checkedItems.length === 0"
            @click="goCheckout"
          >
            去结算
          </el-button>

          <button type="button" class="continue-btn" @click="router.push('/category')">继续购物</button>
        </aside>
      </div>

      <div v-else class="empty-panel leyi-panel-strong">
        <el-empty description="购物车还是空的，去挑点好吃的吧">
          <el-button type="primary" @click="router.push('/home')">去首页逛逛</el-button>
        </el-empty>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import { useCartStore } from '@/stores/cart'
import { getImageUrl, handleImageError } from '@/utils/image'

const router = useRouter()
const cartStore = useCartStore()

onMounted(() => {
  cartStore.fetchCart()
})

const isAllChecked = computed(() => {
  const validItems = cartStore.cartList.filter((item) => item.goodsIsOnSale)
  return validItems.length > 0 && validItems.every((item) => item.isChecked === 1)
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
.cart-shell {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 20rem;
  gap: var(--space-6);
  align-items: flex-start;
}

.cart-list {
  padding: var(--space-6);
}

.list-toolbar {
  height: 2.9rem;
  padding: 0 var(--space-5);
  border-radius: var(--radius-lg);
  background: #fbfbf8;
  border: 1px solid rgba(24, 24, 24, 0.08);
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.list-toolbar p {
  margin: 0;
  font-size: 0.86rem;
  color: var(--color-muted);
}

.cart-items {
  margin-top: var(--space-5);
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
}

.cart-item {
  display: grid;
  grid-template-columns: 2.2rem minmax(0, 1fr) 7.5rem 9rem 7.5rem 4rem;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-4);
  border-radius: var(--radius-lg);
  border: 1px solid rgba(24, 24, 24, 0.1);
  background: #fff;
}

.cart-item.invalid {
  background: #f4f4f4;
}

.item-product {
  display: flex;
  align-items: center;
  gap: var(--space-4);
  min-width: 0;
  cursor: pointer;
}

.item-product img {
  width: 5.5rem;
  height: 5.5rem;
  border-radius: var(--radius-md);
  object-fit: cover;
  flex-shrink: 0;
}

.item-copy {
  min-width: 0;
}

.item-copy h4 {
  margin: 0;
  font-size: 0.92rem;
  line-height: 1.45;
  font-weight: 700;
}

.item-copy p {
  margin: 0.4rem 0 0;
  color: var(--color-muted);
  font-size: 0.78rem;
}

.invalid-tag {
  margin-top: 0.5rem;
  display: inline-flex;
  height: 1.35rem;
  align-items: center;
  padding: 0 0.5rem;
  border-radius: var(--radius-pill);
  background: rgba(255, 107, 142, 0.18);
  color: #a42348;
  font-size: 0.72rem;
  font-weight: 700;
}

.item-price,
.item-subtotal {
  font-size: 0.96rem;
  font-weight: 700;
  text-align: center;
}

.item-quantity {
  display: flex;
  justify-content: center;
}

.item-quantity :deep(.el-input-number) {
  width: 7.5rem;
}

.item-action {
  text-align: right;
}

.item-action .el-button {
  color: #d84a63;
}

.cart-summary {
  position: sticky;
  top: 8.7rem;
  padding: var(--space-6);
}

.cart-summary h3 {
  margin: 0 0 var(--space-5);
  font-size: 1.08rem;
  font-weight: 700;
}

.summary-row {
  height: 2.1rem;
  display: flex;
  align-items: center;
  justify-content: space-between;
  color: var(--color-muted);
  font-size: 0.86rem;
}

.summary-row strong {
  color: var(--color-text);
}

.summary-total {
  margin-top: var(--space-4);
  padding-top: var(--space-4);
  border-top: 1px solid rgba(24, 24, 24, 0.1);
  display: flex;
  justify-content: space-between;
  align-items: baseline;
}

.summary-total strong {
  font-size: 1.35rem;
  font-weight: 800;
  letter-spacing: -0.02em;
}

.checkout-btn {
  width: 100%;
  margin-top: var(--space-5);
  height: 2.8rem;
}

.continue-btn {
  margin-top: var(--space-3);
  width: 100%;
  height: 2.5rem;
  border: 1px solid rgba(24, 24, 24, 0.12);
  background: #fff;
  border-radius: var(--radius-md);
  font-weight: 600;
  cursor: pointer;
}

.empty-panel {
  padding: var(--space-16);
}
</style>
