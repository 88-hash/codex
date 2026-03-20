<template>
  <div class="profile-page">
    <div class="page-container">
      <el-breadcrumb separator="/" class="breadcrumb">
        <el-breadcrumb-item :to="{ path: '/home' }">首页</el-breadcrumb-item>
        <el-breadcrumb-item>个人资料</el-breadcrumb-item>
      </el-breadcrumb>

      <section class="profile-shell">
        <div class="profile-main leyi-panel">
          <header class="main-head">
            <h2>个人资料</h2>
            <span>支持头像、昵称与个人说明更新</span>
          </header>

          <div class="main-content">
            <aside class="avatar-area">
              <el-avatar :size="116" :src="avatarPreview" class="profile-avatar">{{ displayInitial }}</el-avatar>
              <input ref="fileInputRef" type="file" accept="image/png,image/jpeg" class="hidden-input" @change="handleAvatarChange">

              <div class="avatar-actions">
                <el-button @click="selectAvatar">上传头像</el-button>
                <el-button @click="resetAvatar">恢复默认</el-button>
              </div>

              <div class="preset-box">
                <p>预设头像</p>
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
            </aside>

            <div class="form-area">
              <div class="form-line">
                <label>用户名</label>
                <el-input v-model="profileForm.name" maxlength="20" placeholder="请输入用户名" />
              </div>

              <div class="form-line">
                <label>手机号</label>
                <el-input :model-value="profileForm.phone" disabled />
              </div>

              <div class="form-line">
                <label>账号说明</label>
                <el-input
                  v-model="profileForm.signature"
                  type="textarea"
                  :rows="3"
                  maxlength="80"
                  show-word-limit
                  placeholder="例如：今天也要认真吃零食"
                />
              </div>

              <div class="hint-box">
                保存资料后会同步更新到后端用户资料，并刷新本地登录信息。
              </div>

              <el-button type="primary" class="save-btn" @click="saveProfile" :loading="saving">保存资料</el-button>
            </div>
          </div>
        </div>

        <aside class="profile-side">
          <div class="side-card leyi-panel">
            <h3>快捷入口</h3>
            <div class="quick-links">
              <button type="button" @click="router.push('/orders')">我的订单</button>
              <button type="button" @click="router.push('/comments')">我的评价</button>
              <button type="button" @click="router.push('/cart')">购物车</button>
              <button type="button" @click="router.push({ path: '/search', query: { keyword: '常买' } })">常买清单</button>
            </div>
          </div>

          <div class="side-card leyi-panel">
            <h3>常买预览</h3>
            <p class="source-line">{{ frequentHint }}</p>

            <div v-if="favoriteGoods.length > 0" class="frequent-list">
              <article v-for="item in favoriteGoods.slice(0, 4)" :key="item.key" class="frequent-item" @click="goFrequent(item)">
                <img :src="getImageUrl(item.imageUrl)" :alt="item.name" @error="handleImageError">
                <div>
                  <strong>{{ item.name }}</strong>
                  <span>回购 {{ item.count }} 次</span>
                </div>
                <em>￥{{ Number(item.price || 0).toFixed(2) }}</em>
              </article>
            </div>

            <el-empty v-else description="暂无常买数据" />
          </div>
        </aside>
      </section>
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
const saving = ref(false)
const profileForm = ref({
  name: '',
  phone: '',
  signature: ''
})
const avatarPreview = ref('')
const selectedPreset = ref('')
const presetAvatars = PRESET_AVATARS

const favoriteGoods = ref([])
const frequentHint = ref('数据来源：订单明细聚合')

const displayInitial = computed(() => {
  const text = `${profileForm.value.name || userStore.userInfo.phone || 'L'}`.trim()
  return text ? text.slice(0, 1).toUpperCase() : 'L'
})

const hydrateProfile = () => {
  profileForm.value.name = userStore.userInfo.name || userStore.userInfo.phone || 'LeYi会员'
  profileForm.value.phone = userStore.userInfo.phone || '--'
  profileForm.value.signature = userStore.userInfo.signature || ''
  avatarPreview.value = userStore.userInfo.avatar || userStore.userInfo.avatarUrl || DEFAULT_PRESET_AVATAR
  selectedPreset.value = PRESET_AVATARS.find(item => item.url === avatarPreview.value)?.url || ''
}

const selectAvatar = () => {
  fileInputRef.value?.click()
}

const handleAvatarChange = (event) => {
  const file = event.target?.files?.[0]
  if (!file) return

  if (!['image/png', 'image/jpeg'].includes(file.type)) {
    ElMessage.error('仅支持 PNG 或 JPEG 图片')
    event.target.value = ''
    return
  }

  const reader = new FileReader()
  reader.onload = () => {
    avatarPreview.value = `${reader.result || ''}`
    selectedPreset.value = ''
  }
  reader.onerror = () => {
    ElMessage.error('头像读取失败，请重试')
  }
  reader.readAsDataURL(file)
  event.target.value = ''
}

const selectPreset = (url) => {
  selectedPreset.value = url
  avatarPreview.value = url
}

const resetAvatar = () => {
  avatarPreview.value = DEFAULT_PRESET_AVATAR
  selectedPreset.value = DEFAULT_PRESET_AVATAR
}

const saveProfile = async () => {
  const payload = {
    name: profileForm.value.name || 'LeYi会员',
    signature: profileForm.value.signature || '',
    avatar: avatarPreview.value || DEFAULT_PRESET_AVATAR
  }

  saving.value = true
  try {
    const updatedUser = await userStore.updateUserInfo(payload)
    profileForm.value.name = updatedUser.name || payload.name
    profileForm.value.signature = updatedUser.signature || ''
    avatarPreview.value = updatedUser.avatar || DEFAULT_PRESET_AVATAR
    ElMessage.success('个人资料已保存')
  } catch (error) {
    console.error(error)
    ElMessage.error(error?.message || '保存失败，请稍后重试')
  } finally {
    saving.value = false
  }
}

const buildFromOrders = (orders) => {
  const map = new Map()
  ;(orders || []).forEach(order => {
    ;(order.items || []).forEach(item => {
      const key = item.goodsId || item.goodsName
      if (!key) return

      if (!map.has(key)) {
        map.set(key, {
          key,
          goodsId: item.goodsId,
          name: item.goodsName || '未命名商品',
          imageUrl: item.goodsImage || item.imageUrl,
          price: Number(item.price || 0),
          count: 0,
          lastBuyAt: order.createdAt || order.createTime || ''
        })
      }

      const target = map.get(key)
      target.count += Number(item.quantity || 1)
    })
  })
  return [...map.values()].sort((a, b) => Number(b.count || 0) - Number(a.count || 0))
}

const buildFromCart = (cartItems) => {
  return (cartItems || [])
    .map(item => ({
      key: item.goodsId || item.id || item.goodsName,
      goodsId: item.goodsId,
      name: item.goodsName || '未命名商品',
      imageUrl: item.goodsImage || item.imageUrl,
      price: Number(item.goodsPrice || item.price || 0),
      count: Number(item.quantity || 1)
    }))
    .filter(item => item.key)
}

const loadFrequentGoods = async () => {
  favoriteGoods.value = []

  try {
    const orderRes = await getOrderList('')
    const fromOrders = buildFromOrders(orderRes.data || [])
    if (fromOrders.length > 0) {
      favoriteGoods.value = fromOrders
      frequentHint.value = '数据来源：订单明细聚合'
      return
    }
  } catch (error) {
    console.warn('profile frequent from orders failed', error)
  }

  try {
    await cartStore.fetchCart()
    const fromCart = buildFromCart(cartStore.cartList)
    if (fromCart.length > 0) {
      favoriteGoods.value = fromCart
      frequentHint.value = '数据来源：购物车聚合'
      return
    }
  } catch (error) {
    console.warn('profile frequent from cart failed', error)
  }

  frequentHint.value = '暂无可用数据'
}

const goFrequent = (item) => {
  if (item.goodsId) {
    router.push(`/goods/${item.goodsId}`)
    return
  }
  router.push({ path: '/search', query: { keyword: item.name } })
}

onMounted(async () => {
  hydrateProfile()
  await loadFrequentGoods()
})
</script>

<style lang="scss" scoped>
.profile-shell {
  display: grid;
  grid-template-columns: minmax(0, 2fr) minmax(0, 1fr);
  gap: var(--space-6);
  align-items: flex-start;
}

.profile-main {
  padding: var(--space-6);
}

.main-head h2 {
  margin: 0;
  font-size: clamp(1.3rem, 1.55vw, 1.75rem);
  font-weight: 800;
}

.main-head span {
  display: block;
  margin-top: 0.35rem;
  color: var(--color-muted);
  font-size: 0.84rem;
}

.main-content {
  margin-top: var(--space-6);
  display: grid;
  grid-template-columns: 15.5rem minmax(0, 1fr);
  gap: var(--space-5);
}

.avatar-area {
  border: 1px solid rgba(24, 24, 24, 0.1);
  border-radius: var(--radius-lg);
  padding: var(--space-4);
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--space-4);
  background: #fdfdf9;
}

.profile-avatar {
  border: 1px solid rgba(24, 24, 24, 0.16);
}

.hidden-input {
  display: none;
}

.avatar-actions {
  width: 100%;
  display: grid;
  gap: 0.5rem;
}

.avatar-actions :deep(.el-button) {
  margin: 0;
}

.preset-box {
  width: 100%;
  border: 1px solid rgba(24, 24, 24, 0.1);
  border-radius: var(--radius-md);
  padding: 0.65rem;
  background: #fff;
}

.preset-box p {
  margin: 0 0 0.55rem;
  font-size: 0.74rem;
  color: var(--color-muted);
  font-weight: 700;
}

.preset-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 0.45rem;
}

.preset-item {
  border: 1px solid rgba(24, 24, 24, 0.14);
  border-radius: var(--radius-sm);
  background: #fff;
  padding: 0.2rem;
  cursor: pointer;
}

.preset-item img {
  display: block;
  width: 100%;
  border-radius: 0.35rem;
}

.preset-item.active {
  border-color: rgba(238, 205, 43, 0.95);
  box-shadow: inset 0 0 0 1px rgba(238, 205, 43, 0.95);
}

.form-area {
  border: 1px solid rgba(24, 24, 24, 0.1);
  border-radius: var(--radius-lg);
  padding: var(--space-5);
  background: #fff;
}

.form-line + .form-line {
  margin-top: var(--space-4);
}

.form-line label {
  display: block;
  margin-bottom: 0.45rem;
  font-size: 0.78rem;
  color: #404040;
  font-weight: 700;
}

.hint-box {
  margin-top: var(--space-4);
  border-radius: var(--radius-md);
  background: #f7f7f4;
  border: 1px solid rgba(24, 24, 24, 0.08);
  padding: 0.7rem;
  font-size: 0.76rem;
  color: var(--color-muted);
}

.save-btn {
  margin-top: var(--space-5);
  width: 100%;
  min-height: 2.7rem;
}

.profile-side {
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
}

.side-card {
  padding: var(--space-5);
}

.side-card h3 {
  margin: 0;
  font-size: 1rem;
  font-weight: 800;
}

.quick-links {
  margin-top: var(--space-4);
  display: grid;
  gap: 0.55rem;
}

.quick-links button {
  height: 2.25rem;
  border-radius: var(--radius-pill);
  border: 1px solid rgba(24, 24, 24, 0.12);
  background: #fff;
  cursor: pointer;
  text-align: left;
  padding: 0 0.85rem;
  font-size: 0.8rem;
  font-weight: 700;
  color: #2f2f2f;
}

.quick-links button:hover {
  border-color: rgba(238, 205, 43, 0.9);
}

.source-line {
  margin: 0.35rem 0 0;
  color: var(--color-muted);
  font-size: 0.76rem;
}

.frequent-list {
  margin-top: var(--space-4);
  display: flex;
  flex-direction: column;
  gap: 0.55rem;
}

.frequent-item {
  border: 1px solid rgba(24, 24, 24, 0.08);
  border-radius: var(--radius-md);
  background: #fff;
  display: grid;
  grid-template-columns: 2.7rem minmax(0, 1fr) auto;
  gap: 0.6rem;
  align-items: center;
  padding: 0.45rem;
  cursor: pointer;
}

.frequent-item img {
  width: 2.7rem;
  height: 2.7rem;
  border-radius: 0.5rem;
  object-fit: cover;
  border: 1px solid rgba(24, 24, 24, 0.08);
}

.frequent-item strong {
  display: block;
  font-size: 0.8rem;
  line-height: 1.35;
  font-weight: 700;
  color: #292929;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.frequent-item span {
  display: block;
  margin-top: 0.08rem;
  font-size: 0.7rem;
  color: var(--color-muted);
}

.frequent-item em {
  font-style: normal;
  font-size: 0.78rem;
  font-weight: 800;
  color: #221f10;
}
</style>
