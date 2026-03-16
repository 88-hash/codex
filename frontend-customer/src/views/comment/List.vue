<template>
  <div class="comments-page">
    <div class="page-container">
      <el-breadcrumb separator="/" class="breadcrumb">
        <el-breadcrumb-item :to="{ path: '/home' }">首页</el-breadcrumb-item>
        <el-breadcrumb-item>我的评价</el-breadcrumb-item>
      </el-breadcrumb>

      <section class="comments-shell leyi-panel">
        <header class="comments-head">
          <div>
            <h2>我的评价</h2>
            <p>记录你在 LeYi零食店 的每一次口味反馈</p>
          </div>
          <div class="filter-tabs">
            <button
              v-for="tab in filterTabs"
              :key="tab.value"
              type="button"
              class="filter-tab"
              :class="{ active: activeFilter === tab.value }"
              @click="activeFilter = tab.value"
            >
              {{ tab.label }}
            </button>
          </div>
        </header>

        <div v-if="displayComments.length > 0" class="comments-list">
          <article v-for="comment in displayComments" :key="comment.id" class="comment-card">
            <div class="comment-top">
              <div class="author-block">
                <el-avatar :size="44" :src="displayAvatar">{{ displayInitial }}</el-avatar>
                <div class="author-meta">
                  <strong>{{ displayName }}</strong>
                  <span>{{ formatTime(comment.createdAt) }}</span>
                </div>
              </div>
              <el-rate :model-value="Number(comment.rating || 0)" disabled allow-half size="small" />
            </div>

            <div class="goods-line">
              <img :src="getImageUrl(comment.goodsImage || comment.imageUrl)" :alt="comment.goodsName" @error="handleImageError">
              <div>
                <h3>{{ comment.goodsName || '未命名商品' }}</h3>
                <p>订单评价</p>
              </div>
            </div>

            <p class="content-line">{{ comment.content || '用户未填写评价内容' }}</p>

            <div v-if="getCommentImages(comment).length > 0" class="image-grid">
              <img
                v-for="(image, index) in getCommentImages(comment)"
                :key="`${comment.id}-${index}`"
                :src="getImageUrl(image)"
                :alt="`评价图片${index + 1}`"
                @error="handleImageError"
              >
            </div>
          </article>
        </div>

        <div v-else class="empty-wrap leyi-panel">
          <el-empty description="暂无评价记录">
            <template #default>
              <p class="empty-tip">完成订单后可在订单详情页继续发表评价。</p>
            </template>
          </el-empty>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import dayjs from 'dayjs'
import { getMyComments } from '@/api/order'
import { useUserStore } from '@/stores/user'
import { DEFAULT_PRESET_AVATAR } from '@/utils/avatar'
import { getImageUrl, handleImageError } from '@/utils/image'

const userStore = useUserStore()

const comments = ref([])
const activeFilter = ref('all')

const filterTabs = [
  { label: '全部评价', value: 'all' },
  { label: '有图评价', value: 'with_images' },
  { label: '高分评价', value: 'high_score' }
]

const displayAvatar = computed(() => {
  return userStore.userInfo.avatarUrl || userStore.userInfo.avatar || DEFAULT_PRESET_AVATAR
})

const displayName = computed(() => {
  return userStore.userInfo.name || userStore.userInfo.phone || 'LeYi会员'
})

const displayInitial = computed(() => {
  const text = `${displayName.value || ''}`.trim()
  return text ? text.slice(0, 1).toUpperCase() : 'L'
})

const getCommentImages = (comment) => {
  if (Array.isArray(comment.images)) {
    return comment.images.filter(Boolean)
  }

  if (typeof comment.images === 'string' && comment.images.trim()) {
    return comment.images
      .split(',')
      .map((item) => item.trim())
      .filter(Boolean)
  }

  if (typeof comment.imageUrls === 'string' && comment.imageUrls.trim()) {
    return comment.imageUrls
      .split(',')
      .map((item) => item.trim())
      .filter(Boolean)
  }

  return []
}

const displayComments = computed(() => {
  if (activeFilter.value === 'with_images') {
    return comments.value.filter((item) => getCommentImages(item).length > 0)
  }

  if (activeFilter.value === 'high_score') {
    return comments.value.filter((item) => Number(item.rating || 0) >= 4)
  }

  return comments.value
})

const formatTime = (value) => {
  if (!value) return '--'
  const time = dayjs(value)
  return time.isValid() ? time.format('YYYY-MM-DD HH:mm') : '--'
}

onMounted(async () => {
  try {
    const res = await getMyComments()
    comments.value = res.data || []
  } catch (error) {
    console.error(error)
  }
})
</script>

<style lang="scss" scoped>
.comments-shell {
  padding: var(--space-6);
}

.comments-head {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: var(--space-5);
  margin-bottom: var(--space-6);
}

.comments-head h2 {
  margin: 0;
  font-size: clamp(1.3rem, 1.6vw, 1.7rem);
  font-weight: 800;
}

.comments-head p {
  margin: 0.35rem 0 0;
  color: var(--color-muted);
  font-size: 0.84rem;
}

.filter-tabs {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  flex-wrap: wrap;
}

.filter-tab {
  height: 2rem;
  padding: 0 0.8rem;
  border-radius: var(--radius-pill);
  border: 1px solid rgba(24, 24, 24, 0.12);
  background: #fff;
  color: #4a4a4a;
  font-size: 0.76rem;
  font-weight: 700;
  cursor: pointer;
}

.filter-tab.active {
  background: rgba(238, 205, 43, 0.23);
  border-color: rgba(238, 205, 43, 0.92);
  color: #231d09;
}

.comments-list {
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
}

.comment-card {
  border: 1px solid rgba(24, 24, 24, 0.1);
  border-radius: var(--radius-lg);
  background: #fff;
  box-shadow: var(--shadow-xs);
  padding: var(--space-5);
}

.comment-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-4);
}

.author-block {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.author-meta {
  display: flex;
  flex-direction: column;
  gap: 0.1rem;
}

.author-meta strong {
  font-size: 0.92rem;
  font-weight: 800;
  color: #202020;
}

.author-meta span {
  font-size: 0.76rem;
  color: var(--color-muted);
}

.goods-line {
  margin-top: var(--space-4);
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.65rem;
  border-radius: var(--radius-md);
  background: #f8f8f5;
}

.goods-line img {
  width: 3.3rem;
  height: 3.3rem;
  object-fit: cover;
  border-radius: var(--radius-sm);
  border: 1px solid rgba(24, 24, 24, 0.12);
}

.goods-line h3 {
  margin: 0;
  font-size: 0.9rem;
  font-weight: 700;
  color: #262626;
}

.goods-line p {
  margin: 0.2rem 0 0;
  font-size: 0.75rem;
  color: var(--color-muted);
}

.content-line {
  margin: var(--space-4) 0 0;
  color: #313131;
  font-size: 0.86rem;
  line-height: 1.7;
}

.image-grid {
  margin-top: var(--space-4);
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 0.55rem;
}

.image-grid img {
  width: 100%;
  aspect-ratio: 1 / 1;
  object-fit: cover;
  border-radius: var(--radius-sm);
  border: 1px solid rgba(24, 24, 24, 0.08);
}

.empty-wrap {
  padding: var(--space-10) var(--space-6);
}

.empty-tip {
  margin: 0.35rem 0 0;
  font-size: 0.82rem;
  color: var(--color-muted);
}
</style>
