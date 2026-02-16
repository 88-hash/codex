<template>
  <div class="mine-page">
    <div class="page-container">
      <el-breadcrumb separator="/" class="breadcrumb">
        <el-breadcrumb-item :to="{ path: '/home' }">首页</el-breadcrumb-item>
        <el-breadcrumb-item>个人中心</el-breadcrumb-item>
      </el-breadcrumb>
      
      <div class="mine-layout">
        <!-- 左侧菜单 -->
        <div class="mine-sidebar card">
          <div class="user-info">
            <el-avatar :size="64" :src="userStore.userInfo.avatar">
              <el-icon :size="32"><User /></el-icon>
            </el-avatar>
            <div class="user-detail">
              <h3>{{ userStore.userInfo.name || '用户' }}</h3>
              <p>{{ userStore.userInfo.phone }}</p>
            </div>
          </div>
          
          <el-menu :default-active="activeMenu" @select="handleMenuSelect">
            <el-menu-item index="profile">
              <el-icon><User /></el-icon>
              <span>个人信息</span>
            </el-menu-item>
            <el-menu-item index="orders">
              <el-icon><Document /></el-icon>
              <span>我的订单</span>
            </el-menu-item>
            <el-menu-item index="comments">
              <el-icon><ChatDotRound /></el-icon>
              <span>我的评价</span>
            </el-menu-item>
          </el-menu>
        </div>
        
        <!-- 右侧内容 -->
        <div class="mine-main">
          <!-- 个人信息 -->
          <div v-if="activeMenu === 'profile'" class="card">
            <div class="card-header">个人信息</div>
            <div class="card-body">
              <el-form :model="profileForm" label-width="100px" style="max-width: 500px">
                <el-form-item label="头像">
                  <el-avatar :size="80" :src="profileForm.avatar">
                    <el-icon :size="40"><User /></el-icon>
                  </el-avatar>
                </el-form-item>
                <el-form-item label="昵称">
                  <el-input v-model="profileForm.name" placeholder="请输入昵称" />
                </el-form-item>
                <el-form-item label="手机号">
                  <el-input :value="userStore.userInfo.phone" disabled />
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" @click="handleUpdateProfile">保存修改</el-button>
                </el-form-item>
              </el-form>
            </div>
          </div>
          
          <!-- 我的订单 -->
          <div v-if="activeMenu === 'orders'" class="card">
            <div class="card-header">
              <span>我的订单</span>
              <el-radio-group v-model="orderStatus" size="small" @change="fetchOrders">
                <el-radio-button label="">全部</el-radio-button>
                <el-radio-button label="pending">待取货</el-radio-button>
                <el-radio-button label="completed">已完成</el-radio-button>
                <el-radio-button label="cancelled">已取消</el-radio-button>
              </el-radio-group>
            </div>
            <div class="card-body">
              <el-table :data="orders" v-loading="loadingOrders">
                <el-table-column prop="orderNo" label="订单编号" width="160" />
                <el-table-column label="商品" min-width="200">
                  <template #default="{ row }">
                    <div v-for="item in row.items?.slice(0, 2)" :key="item.id" class="order-goods">
                      {{ item.goodsName }} x{{ item.quantity }}
                    </div>
                    <span v-if="row.items?.length > 2" class="more-goods">等{{ row.items.length }}件商品</span>
                  </template>
                </el-table-column>
                <el-table-column prop="totalPrice" label="金额" width="100">
                  <template #default="{ row }">¥{{ row.totalPrice?.toFixed(2) }}</template>
                </el-table-column>
                <el-table-column prop="status" label="状态" width="100">
                  <template #default="{ row }">
                    <el-tag :type="statusType[row.status]" size="small">{{ statusText[row.status] }}</el-tag>
                  </template>
                </el-table-column>
                <el-table-column label="操作" width="150">
                  <template #default="{ row }">
                    <el-button type="primary" text size="small" @click="router.push(`/order/${row.id}`)">
                      查看详情
                    </el-button>
                  </template>
                </el-table-column>
              </el-table>
            </div>
          </div>
          
          <!-- 我的评价 -->
          <div v-if="activeMenu === 'comments'" class="card">
            <div class="card-header">我的评价</div>
            <div class="card-body">
              <div v-if="comments.length > 0" class="comments-list">
                <div v-for="comment in comments" :key="comment.id" class="comment-item">
                  <div class="comment-header">
                    <span class="goods-name">{{ comment.goodsName }}</span>
                    <el-rate :model-value="comment.rating" disabled size="small" />
                    <span class="comment-time">{{ formatTime(comment.createdAt) }}</span>
                  </div>
                  <p class="comment-content">{{ comment.content }}</p>
                </div>
              </div>
              <el-empty v-else description="暂无评价" />
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import dayjs from 'dayjs'
import { useUserStore } from '@/stores/user'
import { getOrderList, getMyComments } from '@/api/order'

const router = useRouter()
const userStore = useUserStore()

const activeMenu = ref('profile')
const orderStatus = ref('')
const orders = ref([])
const loadingOrders = ref(false)
const comments = ref([])

const profileForm = reactive({
  name: userStore.userInfo.name || '',
  avatar: userStore.userInfo.avatar || ''
})

const statusText = { pending: '待取货', completed: '已完成', cancelled: '已取消' }
const statusType = { pending: 'primary', completed: 'success', cancelled: 'info' }

const formatTime = (time) => dayjs(time).format('YYYY-MM-DD HH:mm')

const handleMenuSelect = (index) => {
  activeMenu.value = index
  if (index === 'orders') {
    fetchOrders()
  } else if (index === 'comments') {
    fetchComments()
  }
}

const fetchOrders = async () => {
  loadingOrders.value = true
  try {
    const res = await getOrderList(orderStatus.value)
    orders.value = res.data || []
  } catch (e) {
    console.error(e)
  } finally {
    loadingOrders.value = false
  }
}

const fetchComments = async () => {
  try {
    const res = await getMyComments()
    comments.value = res.data || []
  } catch (e) {
    console.error(e)
  }
}

const handleUpdateProfile = async () => {
  try {
    await userStore.updateUserInfo(profileForm)
    ElMessage.success('保存成功')
  } catch (e) {
    console.error(e)
  }
}
</script>

<style lang="scss" scoped>
.mine-layout {
  display: flex;
  gap: 24px;
}

.mine-sidebar {
  width: 240px;
  flex-shrink: 0;
  
  .user-info {
    display: flex;
    align-items: center;
    gap: 16px;
    padding: 24px;
    border-bottom: 1px solid #e8eaed;
    
    .user-detail {
      h3 {
        font-size: 16px;
        margin-bottom: 4px;
      }
      
      p {
        color: #5f6368;
        font-size: 13px;
        margin: 0;
      }
    }
  }
  
  .el-menu {
    border-right: none;
  }
}

.mine-main {
  flex: 1;
  
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
}

.order-goods {
  font-size: 13px;
  color: #5f6368;
  line-height: 1.5;
}

.more-goods {
  font-size: 12px;
  color: #9aa0a6;
}

.comments-list {
  .comment-item {
    padding: 16px 0;
    border-bottom: 1px solid #f0f0f0;
    
    &:last-child {
      border-bottom: none;
    }
  }
  
  .comment-header {
    display: flex;
    align-items: center;
    gap: 16px;
    margin-bottom: 8px;
    
    .goods-name {
      font-weight: 500;
    }
    
    .comment-time {
      margin-left: auto;
      color: #9aa0a6;
      font-size: 13px;
    }
  }
  
  .comment-content {
    color: #5f6368;
    margin: 0;
    line-height: 1.6;
  }
}
</style>
