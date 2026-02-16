<template>
  <div class="orders-page">
    <div class="page-card">
      <div class="card-header">
        <div class="filter-row">
          <el-select v-model="filters.status" placeholder="订单状态" clearable style="width: 120px">
            <el-option label="待取货" value="pending" />
            <el-option label="已完成" value="completed" />
            <el-option label="已取消" value="cancelled" />
          </el-select>
          <el-input 
            v-model="filters.keyword" 
            placeholder="搜索订单号/手机号/取货码" 
            style="width: 240px"
            clearable
          />
          <el-button type="primary" @click="fetchOrders">搜索</el-button>
        </div>
      </div>
      <div class="card-body">
        <el-table :data="orders" v-loading="loading" border>
          <el-table-column prop="orderNo" label="订单编号" width="160" />
          <el-table-column prop="userPhone" label="顾客手机" width="120" />
          <el-table-column prop="verifyCode" label="取货码" width="100" />
          <el-table-column label="商品" min-width="200">
            <template #default="{ row }">
              <div v-for="item in row.items" :key="item.id" class="goods-item">
                {{ item.goodsName }} x{{ item.quantity }}
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="totalPrice" label="金额" width="100">
            <template #default="{ row }">¥{{ row.totalPrice?.toFixed(2) }}</template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="statusType[row.status]">{{ statusText[row.status] }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createdAt" label="下单时间" width="170">
            <template #default="{ row }">{{ formatTime(row.createdAt) }}</template>
          </el-table-column>
          <el-table-column label="操作" width="150" fixed="right">
            <template #default="{ row }">
              <el-button v-if="row.status === 'pending'" type="primary" text size="small" @click="handleVerify(row)">
                核销
              </el-button>
              <el-button v-if="row.status === 'pending'" type="danger" text size="small" @click="handleCancel(row)">
                取消
              </el-button>
            </template>
          </el-table-column>
        </el-table>
        
        <el-pagination
          v-model:current-page="pagination.pageNum"
          v-model:page-size="pagination.pageSize"
          :total="pagination.total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          @change="fetchOrders"
          style="margin-top: 16px; justify-content: flex-end"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import dayjs from 'dayjs'
import { getOrders, verifyOrder, cancelOrder } from '@/api'

const loading = ref(false)
const orders = ref([])
const filters = reactive({ status: '', keyword: '' })
const pagination = reactive({ pageNum: 1, pageSize: 10, total: 0 })

const statusText = { pending: '待取货', completed: '已完成', cancelled: '已取消' }
const statusType = { pending: 'primary', completed: 'success', cancelled: 'info' }

const formatTime = (time) => dayjs(time).format('YYYY-MM-DD HH:mm:ss')

onMounted(() => fetchOrders())

const fetchOrders = async () => {
  loading.value = true
  try {
    const res = await getOrders({
      status: filters.status,
      keyword: filters.keyword,
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize
    })
    orders.value = res.data?.list || []
    pagination.total = res.data?.total || 0
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const handleVerify = async (row) => {
  await ElMessageBox.confirm(`确认核销订单 ${row.orderNo}？`, '确认核销')
  try {
    await verifyOrder(row.id)
    ElMessage.success('核销成功')
    fetchOrders()
  } catch (e) {
    console.error(e)
  }
}

const handleCancel = async (row) => {
  const { value: reason } = await ElMessageBox.prompt('请输入取消原因', '取消订单')
  try {
    await cancelOrder(row.id, reason)
    ElMessage.success('订单已取消')
    fetchOrders()
  } catch (e) {
    console.error(e)
  }
}
</script>

<style lang="scss" scoped>
.filter-row {
  display: flex;
  gap: 12px;
}

.goods-item {
  font-size: 13px;
  color: #5f6368;
  line-height: 1.5;
}
</style>



