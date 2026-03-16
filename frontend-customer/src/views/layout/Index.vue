<template>
  <div class="layout-shell">
    <header class="site-header">
      <div class="header-top">
        <div class="desktop-container top-inner">
          <div class="top-copy">门店现货自提，随买随取</div>
        </div>
      </div>

      <div class="header-main desktop-container">
        <div class="brand-block" @click="router.push('/home')">
          <div class="brand-icon">
            <el-icon><Shop /></el-icon>
          </div>
          <div class="brand-copy">
            <span class="brand-title">LeYi零食店</span>
            <span class="brand-sub">LeYi Snack Boutique</span>
          </div>
        </div>

        <div class="search-box">
          <el-input
            v-model="keyword"
            placeholder="搜索零食、品牌或口味"
            size="large"
            @keyup.enter="handleSearch"
          />
          <el-button type="primary" size="large" @click="handleSearch">搜索</el-button>
        </div>

        <div class="header-actions">
          <el-dropdown
            trigger="click"
            placement="bottom-end"
            popper-class="user-dropdown-menu"
            @command="handleUserMenuCommand"
          >
            <button class="user-trigger" type="button">
              <el-avatar :size="34" :src="displayAvatar" class="user-avatar">
                {{ displayInitial }}
              </el-avatar>
              <span class="user-name">{{ displayName }}</span>
              <el-icon><ArrowDown /></el-icon>
            </button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人资料</el-dropdown-item>
                <el-dropdown-item command="orders">我的订单</el-dropdown-item>
                <el-dropdown-item command="comments">我的评价</el-dropdown-item>
                <el-dropdown-item command="frequent">常买清单</el-dropdown-item>
                <el-dropdown-item v-if="userStore.isLoggedIn()" command="logout" divided>退出登录</el-dropdown-item>
                <el-dropdown-item v-else command="login" divided>登录 / 注册</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>

          <button class="cart-btn" type="button" @click="router.push('/cart')">
            <el-icon><ShoppingCart /></el-icon>
            <span>购物车</span>
            <span v-if="cartStore.totalCount > 0" class="cart-count">{{ cartStore.totalCount }}</span>
          </button>
        </div>
      </div>

      <nav class="header-nav">
        <div class="desktop-container nav-inner">
          <span
            class="nav-item"
            :class="{ active: route.path === '/home' }"
            @click="router.push('/home')"
          >首页</span>
          <span
            class="nav-item"
            :class="{ active: route.path === '/category' && !activeCategory }"
            @click="router.push('/category')"
          >全部分类</span>
          <span
            v-for="cat in categories.slice(0, 8)"
            :key="cat.id"
            class="nav-item"
            :class="{ active: activeCategory === cat.id }"
            @click="goCategory(cat.id)"
          >
            {{ cat.name }}
          </span>
          <span class="nav-highlight">每周上新</span>
        </div>
      </nav>
    </header>

    <main class="site-main">
      <router-view />
    </main>

    <footer class="site-footer">
      <div class="desktop-container footer-inner">
        <div class="footer-col">
          <h4>LeYi零食店</h4>
          <p>甄选品质零食，打造更高级的日常味觉体验。</p>
          <p>门店自提 + 在线下单，订单状态与核销流程可追踪。</p>
        </div>
        <div class="footer-col">
          <h4>购物服务</h4>
          <p>下单流程</p>
          <p>支付与发票</p>
          <p>到店核销说明</p>
        </div>
        <div class="footer-col">
          <h4>会员权益</h4>
          <p>积分规则</p>
          <p>成长等级</p>
          <p>企业采购</p>
        </div>
        <div class="footer-col">
          <h4>联系我们</h4>
          <p>地址：XX市XX区XX路XX号</p>
          <p>客服：400-888-8888</p>
          <p>服务时间：09:00 - 22:00</p>
        </div>
      </div>
      <div class="footer-copy">© 2026 LeYi零食店 · All Rights Reserved.</div>
    </footer>
  </div>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import { useCartStore } from '@/stores/cart'
import { useUserStore } from '@/stores/user'
import { getCategoryList } from '@/api/goods'
import { DEFAULT_PRESET_AVATAR } from '@/utils/avatar'

const router = useRouter()
const route = useRoute()
const cartStore = useCartStore()
const userStore = useUserStore()

const keyword = ref('')
const categories = ref([])
const activeCategory = ref(null)

const displayName = computed(() => {
  if (!userStore.isLoggedIn()) return '游客'
  return userStore.userInfo.name || userStore.userInfo.phone || 'LeYi会员'
})

const displayAvatar = computed(() => {
  const savedAvatar = userStore.userInfo.avatarUrl || userStore.userInfo.avatar || ''
  return savedAvatar || DEFAULT_PRESET_AVATAR
})

const displayInitial = computed(() => {
  const text = `${displayName.value || ''}`.trim()
  return text ? text.slice(0, 1).toUpperCase() : 'L'
})

watch(
  () => route.query.id,
  (id) => {
    const parsed = Number(id)
    activeCategory.value = Number.isNaN(parsed) ? null : parsed
  },
  { immediate: true }
)

onMounted(async () => {
  if (userStore.isLoggedIn()) {
    cartStore.fetchCart()
  }

  try {
    const res = await getCategoryList()
    categories.value = res.data || []
  } catch (error) {
    console.error(error)
  }
})

const handleSearch = () => {
  const value = keyword.value.trim()
  if (!value) return
  router.push({ path: '/search', query: { keyword: value } })
}

const goCategory = (id) => {
  activeCategory.value = id
  router.push({ path: '/category', query: { id } })
}

const handleLogout = async () => {
  await ElMessageBox.confirm('确定退出登录吗？', '提示')
  userStore.logout()
  cartStore.clearCart()
  router.push('/home')
}

const handleUserMenuCommand = async (command) => {
  if (command === 'profile') {
    router.push('/profile')
    return
  }
  if (command === 'orders') {
    router.push('/orders')
    return
  }
  if (command === 'comments') {
    router.push('/comments')
    return
  }
  if (command === 'frequent') {
    router.push({ path: '/search', query: { keyword: '常买' } })
    return
  }
  if (command === 'logout') {
    await handleLogout()
    return
  }
  if (command === 'login') {
    router.push('/login')
  }
}
</script>

<style lang="scss" scoped>
.layout-shell {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.site-header {
  position: sticky;
  top: 0;
  z-index: 200;
  background: rgba(248, 248, 246, 0.92);
  backdrop-filter: blur(10px);
  border-bottom: 1px solid rgba(24, 24, 24, 0.08);
}

.header-top {
  border-bottom: 1px solid rgba(24, 24, 24, 0.08);
  background: linear-gradient(90deg, #1f1f1f 0%, #3b3320 100%);
}

.top-inner {
  height: 2.2rem;
  display: flex;
  align-items: center;
  justify-content: center;
  color: rgba(255, 255, 255, 0.9);
  font-size: 0.76rem;
}

.header-main {
  height: 5.4rem;
  display: grid;
  grid-template-columns: 17rem minmax(24rem, 1fr) auto;
  align-items: center;
  gap: 1.15rem;
}

.brand-block {
  display: flex;
  align-items: center;
  gap: 0.8rem;
  cursor: pointer;
}

.brand-icon {
  width: 2.4rem;
  height: 2.4rem;
  border-radius: 0.7rem;
  background: var(--color-primary);
  color: #1d1a0e;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  box-shadow: var(--shadow-xs);
  font-size: 1.2rem;
}

.brand-copy {
  display: inline-flex;
  flex-direction: column;
}

.brand-title {
  font-size: 1.2rem;
  font-weight: 800;
  line-height: 1.1;
  letter-spacing: -0.01em;
}

.brand-sub {
  margin-top: 0.16rem;
  font-size: 0.68rem;
  color: var(--color-muted);
  letter-spacing: 0.05em;
}

.search-box {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  gap: 0.55rem;
}

.header-actions {
  display: inline-flex;
  align-items: center;
  gap: 0.7rem;
}

.user-trigger {
  height: 2.5rem;
  padding: 0.2rem 0.75rem 0.2rem 0.25rem;
  border: 1px solid rgba(24, 24, 24, 0.15);
  border-radius: var(--radius-pill);
  background: #fff;
  display: inline-flex;
  align-items: center;
  gap: 0.55rem;
  cursor: pointer;
}

.user-avatar {
  border: 1px solid rgba(24, 24, 24, 0.18);
}

.user-name {
  max-width: 6.2rem;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: 0.82rem;
  font-weight: 700;
}

.cart-btn {
  height: 2.5rem;
  padding: 0 0.9rem;
  border: 1px solid rgba(24, 24, 24, 0.15);
  border-radius: var(--radius-pill);
  background: #fff;
  display: inline-flex;
  align-items: center;
  gap: 0.4rem;
  font-size: 0.82rem;
  font-weight: 700;
  cursor: pointer;
}

.cart-count {
  min-width: 1.2rem;
  height: 1.2rem;
  border-radius: var(--radius-pill);
  background: var(--color-primary);
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 0.7rem;
  font-weight: 800;
}

.header-nav {
  border-top: 1px solid rgba(24, 24, 24, 0.08);
  border-bottom: 1px solid rgba(24, 24, 24, 0.08);
}

.nav-inner {
  height: 3rem;
  display: flex;
  align-items: center;
  gap: 0.55rem;
  overflow-x: auto;
}

.nav-item {
  height: 2rem;
  padding: 0 0.85rem;
  border-radius: var(--radius-pill);
  border: 1px solid rgba(24, 24, 24, 0.12);
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 0.8rem;
  font-weight: 600;
  color: #3d3d3d;
  cursor: pointer;
  white-space: nowrap;
}

.nav-item.active {
  background: rgba(238, 205, 43, 0.25);
  border-color: rgba(238, 205, 43, 0.7);
  color: #27220f;
}

.nav-highlight {
  margin-left: 0.25rem;
  padding: 0 0.7rem;
  height: 1.85rem;
  display: inline-flex;
  align-items: center;
  border-radius: var(--radius-pill);
  background: #1f1f1f;
  color: #fff;
  font-size: 0.74rem;
  white-space: nowrap;
}

.site-main {
  flex: 1;
}

.site-footer {
  margin-top: 2.4rem;
  border-top: 1px solid rgba(24, 24, 24, 0.1);
  background: #fbfbf8;
}

.footer-inner {
  padding-block: 2.5rem 1.8rem;
  display: grid;
  grid-template-columns: 2fr 1fr 1fr 1.3fr;
  gap: 1.5rem;
}

.footer-col h4 {
  margin: 0 0 0.8rem;
  font-size: 1rem;
  font-weight: 700;
}

.footer-col p {
  margin: 0 0 0.45rem;
  font-size: 0.82rem;
  color: var(--color-muted);
}

.footer-copy {
  border-top: 1px solid rgba(24, 24, 24, 0.08);
  text-align: center;
  padding: 0.95rem 0;
  font-size: 0.78rem;
  color: var(--color-muted);
}
</style>
