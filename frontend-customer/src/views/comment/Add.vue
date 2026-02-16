<template>
  <div class="add-comment-page">
    <div class="page-container">
      <el-breadcrumb separator="/" class="breadcrumb">
        <el-breadcrumb-item :to="{ path: '/home' }">首页</el-breadcrumb-item>
        <el-breadcrumb-item :to="{ path: '/orders' }">我的订单</el-breadcrumb-item>
        <el-breadcrumb-item>发表评价</el-breadcrumb-item>
      </el-breadcrumb>
      
      <div class="card" style="max-width: 700px;">
        <div class="card-header">发表评价</div>
        <div class="card-body">
          <el-form label-position="top">
            <el-form-item label="商品评分">
              <div class="rating-row">
                <el-rate 
                  v-model="form.rating" 
                  :texts="['很差', '较差', '一般', '较好', '很好']" 
                  show-text
                  size="large"
                />
              </div>
            </el-form-item>
            
            <el-form-item label="评价内容">
              <el-input 
                v-model="form.content"
                type="textarea"
                :rows="6"
                placeholder="请分享您的使用感受，帮助其他用户做出选择"
                maxlength="300"
                show-word-limit
              />
            </el-form-item>
            
            <el-form-item>
              <el-button @click="router.back()">取消</el-button>
              <el-button type="primary" :loading="loading" @click="handleSubmit">提交评价</el-button>
            </el-form-item>
          </el-form>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { addComment } from '@/api/order'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const form = reactive({
  orderItemId: Number(route.params.orderItemId),
  rating: 5,
  content: ''
})

const handleSubmit = async () => {
  if (!form.rating) {
    ElMessage.warning('请选择评分')
    return
  }
  
  loading.value = true
  try {
    await addComment(form)
    ElMessage.success('评价成功')
    router.back()
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}
</script>

<style lang="scss" scoped>
.rating-row {
  padding: 8px 0;
}
</style>
