<template>
  <div class="comments-page">
    <div class="page-container">
      <el-breadcrumb separator="/" class="breadcrumb">
        <el-breadcrumb-item :to="{ path: '/home' }">首页</el-breadcrumb-item>
        <el-breadcrumb-item>我的评价</el-breadcrumb-item>
      </el-breadcrumb>
      
      <div class="card">
        <div class="card-header">我的评价</div>
        <div class="card-body">
          <div v-if="comments.length > 0" class="comments-list">
            <div v-for="comment in comments" :key="comment.id" class="comment-item">
              <div class="comment-left">
                <div class="comment-header">
                  <span class="goods-name">{{ comment.goodsName }}</span>
                  <el-rate :model-value="comment.rating" disabled />
                </div>
                <p class="comment-content">{{ comment.content }}</p>
              </div>
              <div class="comment-right">
                <span class="comment-time">{{ formatTime(comment.createdAt) }}</span>
              </div>
            </div>
          </div>
          
          <el-empty v-else description="暂无评价" />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import dayjs from 'dayjs'
import { getMyComments } from '@/api/order'

const comments = ref([])

const formatTime = (time) => dayjs(time).format('YYYY-MM-DD HH:mm')

onMounted(async () => {
  try {
    const res = await getMyComments()
    comments.value = res.data || []
  } catch (e) {
    console.error(e)
  }
})
</script>

<style lang="scss" scoped>
.comments-list {
  .comment-item {
    display: flex;
    justify-content: space-between;
    padding: 20px 0;
    border-bottom: 1px solid #f0f0f0;
    
    &:last-child {
      border-bottom: none;
    }
  }
  
  .comment-left {
    flex: 1;
  }
  
  .comment-header {
    display: flex;
    align-items: center;
    gap: 16px;
    margin-bottom: 12px;
    
    .goods-name {
      font-weight: 500;
      font-size: 15px;
    }
  }
  
  .comment-content {
    color: #5f6368;
    margin: 0;
    line-height: 1.6;
  }
  
  .comment-right {
    .comment-time {
      color: #9aa0a6;
      font-size: 13px;
    }
  }
}
</style>
