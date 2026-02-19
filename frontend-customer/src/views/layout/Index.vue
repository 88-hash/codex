<template>
  <div class="layout">
    <header class="top-header">
      <div class="header-bg-pattern" aria-hidden="true"></div>

      <div class="header-container">
        <div class="header-top">
          <div class="welcome">
            <span class="welcome-chip">今日热卖</span>
            <span class="welcome-text">欢迎来到乐逸零食店，快乐补给站</span>
          </div>
        </div>

        <div class="header-main">
          <div class="header-main-left">
            <div class="logo" @click="router.push('/home')">
              <el-icon><Shop /></el-icon>
              <div class="brand-text">
                <span class="brand-name">乐逸零食店</span>
                <span class="brand-sub">Snack Joy Daily</span>
              </div>
            </div>
          </div>

          <div class="header-main-center">
            <div class="search-box">
              <el-input
                v-model="keyword"
                placeholder="搜索你想吃的零食"
                size="large"
                @keyup.enter="handleSearch"
              />
              <el-button type="primary" size="large" class="search-btn" @click="handleSearch">
                搜索
              </el-button>
            </div>
          </div>

          <div class="header-main-right">
            <div class="user-actions">
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
                  <el-icon class="user-arrow"><ArrowDown /></el-icon>
                </button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item command="profile">个人资料</el-dropdown-item>
                    <el-dropdown-item command="orders">我的订单</el-dropdown-item>
                    <el-dropdown-item command="comments">我的评价</el-dropdown-item>
                    <el-dropdown-item command="frequent">常买商品</el-dropdown-item>
                    <el-dropdown-item v-if="userStore.isLoggedIn()" command="logout" divided>退出登录</el-dropdown-item>
                    <el-dropdown-item v-else command="login" divided>登录 / 注册</el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>

            <div class="cart-btn" @click="router.push('/cart')">
              <el-icon :size="24"><ShoppingCart /></el-icon>
              <span>购物车</span>
              <span v-if="cartStore.totalCount > 0" class="cart-count">{{ cartStore.totalCount }}</span>
            </div>
          </div>
        </div>

        <nav class="header-nav">
          <span
            v-for="cat in categories"
            :key="cat.id"
            class="nav-item"
            :class="{ active: activeCategory === cat.id }"
            @click="goCategory(cat.id)"
          >
            {{ cat.name }}
          </span>
          <span class="nav-extra">今日上新</span>
        </nav>
      </div>
    </header>

    <main>
      <router-view />
    </main>

    <footer class="page-footer">
      <div class="footer-container">
        <div class="footer-content">
          <div class="footer-section">
            <h4>乐逸零食店</h4>
            <p>美味零食，快乐生活</p>
            <p>线下自提，新鲜保障</p>
          </div>
          <div class="footer-section">
            <h4>购物指南</h4>
            <a href="#">购物流程</a>
            <a href="#">支付方式</a>
            <a href="#">取货说明</a>
          </div>
          <div class="footer-section">
            <h4>服务保障</h4>
            <a href="#">售后服务</a>
            <a href="#">退换政策</a>
            <a href="#">品质保证</a>
          </div>
          <div class="footer-section">
            <h4>联系我们</h4>
            <p>地址：XX市XX区XX路XX号</p>
            <p>电话：400-888-8888</p>
          </div>
        </div>
        <div class="footer-bottom">© 2024 乐逸零食店 版权所有</div>
      </div>
    </footer>
  </div>
</template>

<script setup>
import { computed, ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
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
const fallbackUserName = '用户4567'

const displayName = computed(() => {
  if (!userStore.isLoggedIn()) return fallbackUserName
  return userStore.userInfo.name || userStore.userInfo.phone || fallbackUserName
})
const displayAvatar = computed(() => {
  const savedAvatar = userStore.userInfo.avatarUrl || userStore.userInfo.avatar || ''
  return savedAvatar || DEFAULT_PRESET_AVATAR
})
const displayInitial = computed(() => {
  const text = `${displayName.value || ''}`.trim()
  return text ? text.slice(0, 1).toUpperCase() : 'U'
})

onMounted(async () => {
  if (userStore.isLoggedIn()) {
    cartStore.fetchCart()
  }

  try {
    const res = await getCategoryList()
    categories.value = res.data || []
    const queryId = Number(route.query.id)
    activeCategory.value = Number.isNaN(queryId) ? null : queryId
  } catch (e) {
    console.error(e)
  }
})

const handleSearch = () => {
  if (keyword.value.trim()) {
    router.push({ path: '/search', query: { keyword: keyword.value } })
  }
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
.layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;

  main {
    flex: 1;
  }
}
</style>
