<template>
  <div class="admin-layout">
    <aside class="admin-sidebar">
      <div class="logo">
        <el-icon><Shop /></el-icon>
        <span>乐逸零食店</span>
      </div>
      
      <el-menu
        :default-active="$route.path"
        router
        background-color="transparent"
      >
        <el-menu-item index="/dashboard">
          <el-icon><DataLine /></el-icon>
          <span>数据概览</span>
        </el-menu-item>
        
        <el-menu-item index="/verify">
          <el-icon><Finished /></el-icon>
          <span>订单核销</span>
        </el-menu-item>
        
        <el-menu-item index="/orders">
          <el-icon><List /></el-icon>
          <span>订单管理</span>
        </el-menu-item>
        
        <template v-if="adminStore.isManager()">
          <el-menu-item index="/goods">
            <el-icon><Goods /></el-icon>
            <span>商品管理</span>
          </el-menu-item>
          
          <el-menu-item index="/category">
            <el-icon><Menu /></el-icon>
            <span>分类管理</span>
          </el-menu-item>
          
          <el-menu-item index="/staff">
            <el-icon><User /></el-icon>
            <span>店员管理</span>
          </el-menu-item>
        </template>
      </el-menu>
    </aside>
    
    <main class="admin-main">
      <header class="admin-header">
        <div class="header-left">
          <h2>{{ $route.meta.title }}</h2>
        </div>
        <div class="header-right">
          <span class="admin-name">{{ adminStore.adminInfo.name }}</span>
          <el-tag size="small" :type="adminStore.isManager() ? 'primary' : 'info'">
            {{ adminStore.isManager() ? '店长' : '店员' }}
          </el-tag>
          <el-button type="danger" text @click="handleLogout">
            <el-icon><SwitchButton /></el-icon>
            退出
          </el-button>
        </div>
      </header>
      
      <div class="admin-content">
        <router-view />
      </div>
    </main>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import { useAdminStore } from '@/stores/admin'

const router = useRouter()
const adminStore = useAdminStore()

const handleLogout = async () => {
  await ElMessageBox.confirm('确定退出登录吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消'
  })
  adminStore.logout()
  router.push('/login')
}
</script>

<style lang="scss" scoped>
.admin-name {
  color: #5f6368;
}
</style>



