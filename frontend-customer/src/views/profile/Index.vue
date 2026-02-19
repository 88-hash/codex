<template>
  <div class="profile-page">
    <div class="page-container">
      <el-breadcrumb separator="/" class="breadcrumb">
        <el-breadcrumb-item :to="{ path: '/home' }">首页</el-breadcrumb-item>
        <el-breadcrumb-item>个人资料</el-breadcrumb-item>
      </el-breadcrumb>

      <div class="profile-layout">
        <section class="profile-main">
          <div class="profile-card account-card">
            <div class="card-header sticker-header">
              <span>个人资料</span>
              <span class="head-tag">可编辑</span>
            </div>
            <div class="card-body account-body">
              <div class="avatar-panel">
                <el-avatar :size="104" :src="avatarPreview" class="profile-avatar">{{ displayInitial }}</el-avatar>
                <input ref="fileInputRef" type="file" accept="image/*" class="hidden-input" @change="handleAvatarChange">
                <div class="avatar-actions">
                  <el-button class="btn-white" @click="selectAvatar">上传自定义头像</el-button>
                  <el-button class="btn-white" @click="clearAvatar">恢复默认头像</el-button>
                </div>
                <div class="preset-section">
                  <p class="preset-title">系统预设头像</p>
                  <div class="preset-grid">
                    <button
                      v-for="item in presetAvatars"
                      :key="item.id"
                      type="button"
                      class="preset-item"
                      :class="{ active: selectedPreset === item.url }"
                      @click="selectPreset(item.url)"
                    >
                      <img :src="item.url" :alt="item.name">
                    </button>
                  </div>
                </div>
              </div>

              <div class="form-panel">
                <div class="form-row">
                  <label>用户名</label>
                  <el-input v-model="profileForm.name" maxlength="20" placeholder="请输入用户名" />
                </div>
                <div class="form-row">
                  <label>手机号</label>
                  <el-input :model-value="profileForm.phone" disabled />
                </div>
                <div class="form-row">
                  <label>账号说明</label>
                  <div class="info-chip">头像与用户名当前保存为本地资料，可后续切换为后端接口</div>
                </div>
                <el-button class="save-btn" type="primary" @click="saveProfile">保存资料</el-button>
              </div>
            </div>
          </div>

          <div class="profile-card frequent-card">
            <div class="card-header sticker-header">
              <span>常买商品</span>
              <span class="head-tag">{{ frequentHint }}</span>
            </div>
            <div class="card-body">
              <div v-if="favoriteGoods.length > 0" class="frequent-grid">
                <div v-for="item in favoriteGoods" :key="item.key" class="frequent-item" @click="goGoods(item)">
                  <div class="img-wrap">
                    <img :src="getImageUrl(item.imageUrl)" :alt="item.name" @error="handleImageError">
                  </div>
                  <h4>{{ item.name }}</h4>
                  <p>购买频次 {{ item.count }} 次</p>
                  <span class="price">￥{{ Number(item.price || 0).toFixed(2) }}</span>
                </div>
              </div>
              <el-empty v-else description="暂无常买商品" />
            </div>
          </div>
        </section>

        <aside class="profile-side">
          <div class="profile-card side-card">
            <div class="card-header sticker-header">快捷入口</div>
            <div class="card-body side-links">
              <el-button class="quick-btn" @click="router.push('/orders')">我的订单</el-button>
              <el-button class="quick-btn" @click="router.push('/comments')">我的评价</el-button>
              <el-button class="quick-btn" @click="router.push('/cart')">购物车</el-button>
            </div>
          </div>

          <div class="profile-card side-card">
            <div class="card-header sticker-header">账号信息</div>
            <div class="card-body info-list">
              <div class="info-row"><span>用户ID</span><strong>{{ userStore.userInfo.id || '--' }}</strong></div>
              <div class="info-row"><span>登录状态</span><strong>{{ userStore.isLoggedIn() ? '已登录' : '未登录' }}</strong></div>
              <div class="info-row"><span>常买商品数</span><strong>{{ favoriteGoods.length }}</strong></div>
            </div>
          </div>
        </aside>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { useCartStore } from '@/stores/cart'
import { getOrderList } from '@/api/order'
import { getImageUrl, handleImageError } from '@/utils/image'
import { PRESET_AVATARS, DEFAULT_PRESET_AVATAR } from '@/utils/avatar'

const router = useRouter()
const userStore = useUserStore()
const cartStore = useCartStore()

const fileInputRef = ref(null)
const profileForm = ref({
  name: '',
  phone: ''
})
const avatarPreview = ref('')
const selectedPreset = ref('')
const favoriteGoods = ref([])
const frequentHint = ref('数据来源：正在加载')
const presetAvatars = PRESET_AVATARS

const mockFrequentGoods = [
  { key: 'm1', name: '脆香薯片', imageUrl: '/placeholder.png', price: 9.9, count: 3 },
  { key: 'm2', name: '巧克力威化', imageUrl: '/placeholder.png', price: 12.5, count: 2 },
  { key: 'm3', name: '蜜桃气泡水', imageUrl: '/placeholder.png', price: 6.8, count: 4 },
  { key: 'm4', name: '海盐夹心饼干', imageUrl: '/placeholder.png', price: 13.9, count: 2 }
]

const displayInitial = computed(() => {
  const text = `${profileForm.value.name || userStore.userInfo.phone || 'U'}`.trim()
  return text ? text.slice(0, 1).toUpperCase() : 'U'
})

onMounted(async () => {
  initProfile()
  await loadFrequentGoods()
})

const initProfile = () => {
  profileForm.value.name = userStore.userInfo.name || userStore.userInfo.phone || '用户4567'
  profileForm.value.phone = userStore.userInfo.phone || '--'
  avatarPreview.value = userStore.userInfo.avatarUrl || userStore.userInfo.avatar || DEFAULT_PRESET_AVATAR
  selectedPreset.value = PRESET_AVATARS.find((item) => item.url === avatarPreview.value)?.url || ''
}

const selectAvatar = () => {
  fileInputRef.value?.click()
}

const handleAvatarChange = (event) => {
  const file = event.target?.files?.[0]
  if (!file) return

  const reader = new FileReader()
  reader.onload = () => {
    avatarPreview.value = `${reader.result || ''}`
    selectedPreset.value = ''
  }
  reader.readAsDataURL(file)
  event.target.value = ''
}

const selectPreset = (url) => {
  selectedPreset.value = url
  avatarPreview.value = url
}

const clearAvatar = () => {
  avatarPreview.value = DEFAULT_PRESET_AVATAR
  selectedPreset.value = DEFAULT_PRESET_AVATAR
}

const saveProfile = () => {
  const finalAvatar = avatarPreview.value || DEFAULT_PRESET_AVATAR
  const updated = {
    ...userStore.userInfo,
    name: profileForm.value.name || '用户4567',
    avatarUrl: finalAvatar,
    avatar: finalAvatar
  }
  userStore.userInfo = updated
  localStorage.setItem('userInfo', JSON.stringify(updated))
  ElMessage.success('个人资料已保存')
}

const buildFromOrders = (orders) => {
  const map = new Map()
  ;(orders || []).forEach((order) => {
    ;(order.items || []).forEach((item) => {
      const key = item.goodsId || item.goodsName
      if (!key) return
      if (!map.has(key)) {
        map.set(key, {
          key,
          goodsId: item.goodsId,
          name: item.goodsName,
          imageUrl: item.goodsImage,
          price: item.price,
          count: 0
        })
      }
      map.get(key).count += Number(item.quantity || 1)
    })
  })
  return [...map.values()].sort((a, b) => b.count - a.count).slice(0, 8)
}

const buildFromCart = (cartItems) => {
  return (cartItems || [])
    .map((item) => ({
      key: item.goodsId || item.id,
      goodsId: item.goodsId,
      name: item.goodsName,
      imageUrl: item.goodsImage,
      price: item.goodsPrice,
      count: Number(item.quantity || 1)
    }))
    .slice(0, 8)
}

const loadFrequentGoods = async () => {
  try {
    const orderRes = await getOrderList('')
    const fromOrders = buildFromOrders(orderRes.data || [])
    if (fromOrders.length > 0) {
      favoriteGoods.value = fromOrders
      frequentHint.value = '数据来源：订单记录自动推导'
      return
    }
  } catch (e) {
    console.warn('order source unavailable', e)
  }

  try {
    await cartStore.fetchCart()
    const fromCart = buildFromCart(cartStore.cartList)
    if (fromCart.length > 0) {
      favoriteGoods.value = fromCart
      frequentHint.value = '数据来源：购物车记录推导'
      return
    }
  } catch (e) {
    console.warn('cart source unavailable', e)
  }

  favoriteGoods.value = mockFrequentGoods
  frequentHint.value = '演示数据（后续可替换为推荐接口）'
}

const goGoods = (item) => {
  if (item.goodsId) {
    router.push(`/goods/${item.goodsId}`)
    return
  }
  router.push('/category')
}
</script>

<style lang="scss" scoped>
.profile-layout {
  display: grid;
  grid-template-columns: minmax(0, 7fr) minmax(0, 3fr);
  gap: 24px;
  align-items: flex-start;
}

.profile-main {
  display: flex;
  flex-direction: column;
  gap: 20px;
  min-width: 0;
}

.profile-side {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.profile-card {
  border: 2px solid #000;
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-md);
  background: #fffef7;
  overflow: hidden;
}

.sticker-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 16px 20px;
  border-bottom: 2px solid #000;
  background: #ffd700;
  font-size: 20px;
  font-weight: 900;
  color: #000;
}

.head-tag {
  font-size: 12px;
  font-weight: 800;
  border: 2px solid #000;
  border-radius: 999px;
  box-shadow: 2px 2px 0 #000;
  background: #fff;
  padding: 2px 10px;
}

.account-body {
  display: grid;
  grid-template-columns: 220px minmax(0, 1fr);
  gap: 18px;
  padding: 18px 20px;
}

.avatar-panel {
  border: 2px solid #000;
  border-radius: 20px;
  box-shadow: 4px 4px 0 #000;
  background: #fff;
  padding: 14px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
}

.profile-avatar {
  border: 2px solid #000;
  box-shadow: 3px 3px 0 #000;
  background: #00bfff;
  color: #000;
  font-weight: 900;
}

.hidden-input {
  display: none;
}

.avatar-actions {
  display: flex;
  flex-direction: column;
  gap: 8px;
  width: 100%;
}

.preset-section {
  width: 100%;
  border: 2px solid #000;
  border-radius: 16px;
  box-shadow: 2px 2px 0 #000;
  background: #fffef7;
  padding: 10px;
}

.preset-title {
  font-size: 12px;
  font-weight: 800;
  color: #222;
  margin-bottom: 8px;
}

.preset-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 8px;
}

.preset-item {
  border: 2px solid #000;
  border-radius: 12px;
  box-shadow: 2px 2px 0 #000;
  background: #fff;
  padding: 4px;
  cursor: pointer;

  img {
    width: 100%;
    height: auto;
    display: block;
    border-radius: 8px;
  }
}

.preset-item.active {
  background: #ffd700;
}

.form-panel {
  border: 2px solid #000;
  border-radius: 20px;
  box-shadow: 4px 4px 0 #000;
  background: #fff;
  padding: 14px;
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.form-row {
  display: flex;
  flex-direction: column;
  gap: 6px;

  label {
    font-size: 13px;
    font-weight: 800;
    color: #000;
  }
}

.info-chip {
  border: 2px solid #000;
  border-radius: 14px;
  box-shadow: 2px 2px 0 #000;
  background: #fff4b4;
  padding: 8px 10px;
  font-size: 12px;
  font-weight: 700;
  color: #111;
}

.save-btn,
.btn-white,
.quick-btn {
  border: 2px solid #000;
  border-radius: 999px;
  box-shadow: 3px 3px 0 #000;
  font-weight: 900;
  color: #000;
}

.save-btn {
  background: #ffd700;
  min-height: 42px;
}

.btn-white,
.quick-btn {
  background: #fff;
}

.frequent-card .card-body,
.side-card .card-body {
  padding: 18px 20px;
}

.frequent-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
}

.frequent-item {
  border: 2px solid #000;
  border-radius: 18px;
  box-shadow: 3px 3px 0 #000;
  background: #fff;
  padding: 10px;
  cursor: pointer;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.img-wrap {
  aspect-ratio: 1 / 1;
  border: 2px solid #000;
  border-radius: 14px;
  box-shadow: 2px 2px 0 #000;
  overflow: hidden;
  background: #f4f4f4;

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
    display: block;
  }
}

.frequent-item h4 {
  font-size: 14px;
  line-height: 1.35;
  font-weight: 800;
  color: #000;
}

.frequent-item p {
  font-size: 12px;
  color: #333;
  font-weight: 700;
}

.frequent-item .price {
  color: #000;
  font-size: 20px;
  font-weight: 900;
}

.side-links {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.info-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.info-row {
  min-height: 40px;
  border: 2px solid #000;
  border-radius: 14px;
  box-shadow: 2px 2px 0 #000;
  background: #fff;
  padding: 0 12px;
  display: flex;
  align-items: center;
  justify-content: space-between;

  span {
    font-weight: 700;
    color: #333;
  }

  strong {
    font-weight: 900;
    color: #000;
  }
}
</style>
