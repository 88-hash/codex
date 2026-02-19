<template>
  <div class="cart-page">
    <div class="page-container">
      <el-breadcrumb separator="/" class="breadcrumb">
        <el-breadcrumb-item :to="{ path: '/home' }">首页</el-breadcrumb-item>
        <el-breadcrumb-item>购物车</el-breadcrumb-item>
      </el-breadcrumb>

      <div class="cart-layout" v-if="cartStore.cartList.length > 0">
        <div class="cart-main">
          <div class="list-header card">
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
          </div>

          <div class="cart-list">
            <div
              v-for="item in cartStore.cartList"
              :key="item.id"
              class="cart-item card"
              :class="{ invalid: !item.goodsIsOnSale }"
            >
              <el-checkbox
                :model-value="item.isChecked === 1"
                :disabled="!item.goodsIsOnSale"
                @change="(val) => handleCheck(item.id, val)"
              />

              <div class="item-info" @click="goDetail(item.goodsId)">
                <img :src="getImageUrl(item.goodsImage)" :alt="item.goodsName" @error="handleImageError">
                <div class="info-text">
                  <h4>{{ item.goodsName }}</h4>
                  <p class="meta-text">库存 {{ item.goodsStock }} 件</p>
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
                <el-button class="delete-btn" type="danger" plain @click="handleDelete(item.id)">删除</el-button>
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
              <span>￥{{ cartStore.checkedTotal.toFixed(2) }}</span>
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

      <div v-else class="empty-cart card">
        <el-empty description="购物车还是空空的，去挑点快乐零食吧">
          <el-button type="primary" @click="router.push('/home')">去逛逛</el-button>
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
.cart-page {
  background: var(--bg);
}

.cart-layout {
  display: grid;
  grid-template-columns: minmax(0, 7fr) minmax(320px, 3fr);
  gap: 24px;
  align-items: flex-start;
}

.cart-main {
  min-width: 0;
}

.list-header {
  margin-bottom: 16px;

  .card-header {
    display: grid;
    grid-template-columns: 50px 1fr 120px 150px 120px 100px;
    align-items: center;
    padding: 14px 20px;
    background: #fff4b4;
    border-bottom: 2px solid #000;
    gap: 12px;

    .header-text {
      text-align: center;
      color: #000;
      font-size: 13px;
      font-weight: 800;
    }
  }

  :deep(.el-checkbox__inner) {
    width: 18px;
    height: 18px;
    border: 2px solid #000;
    border-radius: 6px;
  }

  :deep(.el-checkbox__label) {
    color: #000;
    font-weight: 800;
  }
}

.cart-list {
  display: flex;
  flex-direction: column;
  gap: 16px;

  :deep(.el-checkbox__inner) {
    width: 18px;
    height: 18px;
    border: 2px solid #000;
    border-radius: 6px;
  }

  .cart-item {
    display: grid;
    grid-template-columns: 50px 1fr 120px 150px 120px 100px;
    align-items: center;
    padding: 16px 18px;
    gap: 12px;
    border: 2px solid #000;
    border-radius: 24px;
    box-shadow: 6px 6px 0 #000;
    background: #fffef7;

    &.invalid {
      background: #f6f6f6;

      .item-info img {
        opacity: 0.5;
      }

      .delete-btn {
        opacity: 0.6;
      }
    }
  }

  .item-info {
    display: flex;
    gap: 14px;
    cursor: pointer;
    min-width: 0;

    img {
      width: 92px;
      aspect-ratio: 1 / 1;
      object-fit: cover;
      display: block;
      border-radius: 16px;
      border: 2px solid #000;
      background: #fff;
      box-shadow: 3px 3px 0 #000;
      flex-shrink: 0;
    }

    .info-text {
      min-width: 0;

      h4 {
        font-size: 15px;
        line-height: 1.45;
        font-weight: 800;
        margin-bottom: 6px;
        color: #000;
      }

      .meta-text {
        margin-bottom: 6px;
        color: #333;
        font-size: 12px;
      }

      .invalid-tag {
        display: inline-flex;
        align-items: center;
        padding: 2px 10px;
        border: 2px solid #000;
        border-radius: 999px;
        background: #ff69b4;
        color: #000;
        font-size: 12px;
        font-weight: 800;
      }
    }
  }

  .item-price,
  .item-subtotal {
    text-align: center;

    .price {
      color: #000;
      font-weight: 900;
      font-size: 20px;

      &::before {
        content: '￥';
        font-size: 12px;
        margin-right: 2px;
      }
    }
  }

  .item-quantity {
    display: flex;
    justify-content: center;

    :deep(.el-input-number) {
      width: 130px;
    }

    :deep(.el-input-number__decrease),
    :deep(.el-input-number__increase) {
      border: 2px solid #000;
      background: #fff;
      color: #000;
      font-weight: 900;
    }

    :deep(.el-input-number .el-input__wrapper) {
      box-shadow: 3px 3px 0 #000;
    }
  }

  .item-action {
    text-align: center;

    .delete-btn {
      min-height: 34px;
      padding: 0 14px;
      border: 2px solid #000;
      border-radius: 999px;
      background: #00bfff;
      color: #000;
      font-weight: 800;
      box-shadow: 3px 3px 0 #000;
    }
  }
}

.cart-summary {
  position: sticky;
  top: 132px;
  border: 2px solid #000;
  border-radius: 28px;
  box-shadow: 6px 6px 0 #000;
  background: #fff;

  .card-header {
    background: #ff69b4;
    color: #000;
    border-bottom: 2px solid #000;
    font-weight: 900;
  }

  .summary-row {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 12px 0;
    border-bottom: 2px solid #000;
    color: #000;
    font-weight: 700;

    &.total {
      border-bottom: none;
      padding-top: 16px;
      font-size: 15px;
      font-weight: 800;

      .price {
        color: #000;
        font-size: 30px;
        font-weight: 900;

        &::before {
          content: '￥';
          font-size: 14px;
          margin-right: 3px;
        }
      }
    }
  }

  .checkout-btn {
    width: 100%;
    margin-top: 20px;
    min-height: 46px;
    border: 2px solid #000;
    border-radius: 999px;
    background: #ffd700;
    color: #000;
    font-size: 16px;
    font-weight: 900;
    box-shadow: 4px 4px 0 #000;
    transition: transform 0.15s ease;

    &:hover {
      transform: translate(-2px, -2px);
    }
  }
}

.empty-cart {
  padding: 72px 20px;
  border: 2px solid #000;
  border-radius: 28px;
  box-shadow: 6px 6px 0 #000;
  background: #fffef7;

  :deep(.el-empty__description p) {
    color: #000;
    font-weight: 800;
  }

  :deep(.el-empty__image svg) {
    border: 2px solid #000;
    border-radius: 20px;
    box-shadow: 4px 4px 0 #000;
    background: #fff4b4;
  }
}
</style>
