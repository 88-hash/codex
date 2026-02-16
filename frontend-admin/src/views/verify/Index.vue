<template>
  <div class="verify-page">
    <div class="verify-input page-card">
      <div class="card-body">
        <h3>请输入取货码</h3>
        <div class="input-row">
          <el-input 
            v-model="verifyCode" 
            placeholder="请输入6位取货码"
            size="large"
            maxlength="6"
            @keyup.enter="handleSearch"
          >
            <template #prefix>
              <el-icon><Ticket /></el-icon>
            </template>
          </el-input>
          <el-button type="primary" size="large" @click="handleSearch" :loading="searching">
            查询订单
          </el-button>
        </div>
      </div>
    </div>
    
    <div v-if="order" class="order-card page-card">
      <div class="card-header">
        <h3>订单信息</h3>
        <el-tag :type="order.status === 'pending' ? 'primary' : 'success'">
          {{ order.status === 'pending' ? '待取货' : '已完成' }}
        </el-tag>
      </div>
      <div class="card-body">
        <div class="order-info">
          <div class="info-row">
            <span class="label">订单编号</span>
            <span>{{ order.orderNo }}</span>
          </div>
          <div class="info-row">
            <span class="label">顾客手机</span>
            <span>{{ order.userPhone }}</span>
          </div>
          <div class="info-row">
            <span class="label">下单时间</span>
            <span>{{ formatTime(order.createdAt) }}</span>
          </div>
          <div class="info-row" v-if="order.remark">
            <span class="label">备注</span>
            <span>{{ order.remark }}</span>
          </div>
        </div>
        
        <el-divider />
        
        <div class="goods-list">
          <h4>商品清单</h4>
          <el-table :data="order.items" border>
            <el-table-column prop="goodsName" label="商品名称" />
            <el-table-column prop="price" label="单价" width="100">
              <template #default="{ row }">¥{{ row.price?.toFixed(2) }}</template>
            </el-table-column>
            <el-table-column prop="quantity" label="数量" width="80" />
            <el-table-column prop="subtotal" label="小计" width="100">
              <template #default="{ row }">¥{{ row.subtotal?.toFixed(2) }}</template>
            </el-table-column>
          </el-table>
        </div>
        
        <div class="order-total">
          <span>订单总价：</span>
          <span class="price">¥{{ order.totalPrice?.toFixed(2) }}</span>
        </div>
        
        <div class="order-actions" v-if="order.status === 'pending'">
          <el-button size="large" @click="handleCancel">取消订单</el-button>
          <el-button type="primary" size="large" @click="handleVerify" :loading="verifying">
            确认核销
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import dayjs from 'dayjs'
import { searchOrder, verifyOrder, cancelOrder } from '@/api'

const verifyCode = ref('')
const order = ref(null)
const searching = ref(false)
const verifying = ref(false)

const formatTime = (time) => dayjs(time).format('YYYY-MM-DD HH:mm:ss')

const handleSearch = async () => {
  if (!verifyCode.value || verifyCode.value.length !== 6) {
    ElMessage.warning('请输入6位取货码')
    return
  }
  
  searching.value = true
  try {
    const res = await searchOrder(verifyCode.value)
    order.value = res.data
  } catch (e) {
    order.value = null
  } finally {
    searching.value = false
  }
}

const handleVerify = async () => {
  await ElMessageBox.confirm(
    `确认核销订单？\n顾客手机：${order.value.userPhone}\n订单金额：¥${order.value.totalPrice?.toFixed(2)}`,
    '确认核销',
    { confirmButtonText: '确认核销', cancelButtonText: '取消', type: 'warning' }
  )
  
  verifying.value = true
  try {
    await verifyOrder(order.value.id)
    ElMessage.success('核销成功')
    order.value.status = 'completed'
    verifyCode.value = ''
  } catch (e) {
    console.error(e)
  } finally {
    verifying.value = false
  }
}

const handleCancel = async () => {
  const { value: reason } = await ElMessageBox.prompt('请输入取消原因', '取消订单', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    inputPlaceholder: '如：顾客未到店'
  })
  
  try {
    await cancelOrder(order.value.id, reason)
    ElMessage.success('订单已取消')
    order.value = null
    verifyCode.value = ''
  } catch (e) {
    console.error(e)
  }
}
</script>

<style lang="scss" scoped>
.verify-input {
  .card-body {
    text-align: center;
    padding: 40px;
    
    h3 {
      margin-bottom: 24px;
      color: #5f6368;
    }
    
    .input-row {
      display: flex;
      gap: 16px;
      max-width: 500px;
      margin: 0 auto;
      
      .el-input {
        flex: 1;
      }
    }
  }
}

.order-card {
  margin-top: 20px;
  
  .order-info {
    .info-row {
      display: flex;
      padding: 8px 0;
      
      .label {
        width: 100px;
        color: #5f6368;
      }
    }
  }
  
  .goods-list {
    h4 {
      margin-bottom: 12px;
      font-size: 15px;
    }
  }
  
  .order-total {
    text-align: right;
    padding: 16px 0;
    font-size: 16px;
    
    .price {
      color: #ea4335;
      font-size: 24px;
      font-weight: 600;
    }
  }
  
  .order-actions {
    display: flex;
    justify-content: flex-end;
    gap: 12px;
    padding-top: 16px;
    border-top: 1px solid #e8eaed;
  }
}
</style>



