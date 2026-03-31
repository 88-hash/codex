<template>
  <div class="verify-page">
    <div class="page-card">
      <div class="card-header">
        <h3>未核销订单</h3>
        <el-button @click="fetchOrders" :loading="loading">刷新列表</el-button>
      </div>
      <div class="card-body">
        <el-table :data="orders" v-loading="loading" border empty-text="暂无未核销订单">
          <el-table-column prop="orderNo" label="订单编号" min-width="220" show-overflow-tooltip />
          <el-table-column prop="userPhone" label="顾客手机" width="120" />
          <el-table-column prop="verifyCode" label="取货码" width="100" />
          <el-table-column label="商品" min-width="200">
            <template #default="{ row }">
              <div v-for="item in row.items" :key="item.id" class="goods-item">
                {{ item.goodsName }} x{{ item.quantity }}
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="remark" label="备注" min-width="150" show-overflow-tooltip>
            <template #default="{ row }">{{ row.remark || '--' }}</template>
          </el-table-column>
          <el-table-column prop="totalPrice" label="订单总价" width="110">
            <template #default="{ row }">￥{{ row.totalPrice?.toFixed(2) }}</template>
          </el-table-column>
          <el-table-column prop="createdAt" label="下单时间" width="170">
            <template #default="{ row }">{{ formatTime(row.createdAt) }}</template>
          </el-table-column>
          <el-table-column label="操作" width="150" fixed="right">
            <template #default="{ row }">
              <el-button type="primary" text size="small" @click="handleVerify(row)">核销</el-button>
              <el-button type="danger" text size="small" @click="handleCancel(row)">取消</el-button>
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
const pagination = reactive({ pageNum: 1, pageSize: 10, total: 0 })

const formatTime = (time) => dayjs(time).format('YYYY-MM-DD HH:mm:ss')

onMounted(() => fetchOrders())

const fetchOrders = async () => {
  loading.value = true
  try {
    const res = await getOrders({
      verificationStatus: 'UNVERIFIED',
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

const refreshAfterAction = async () => {
  if (orders.value.length === 1 && pagination.pageNum > 1) {
    pagination.pageNum -= 1
  }
  await fetchOrders()
}

const handleVerify = async (row) => {
  await ElMessageBox.confirm(
    `确认核销订单？\n顾客手机：${row.userPhone}\n订单金额：￥${row.totalPrice?.toFixed(2)}`,
    '确认核销',
    { confirmButtonText: '确认核销', cancelButtonText: '取消', type: 'warning' }
  )

  try {
    await verifyOrder(row.id)
    ElMessage.success('核销成功')
    await refreshAfterAction()
  } catch (e) {
    console.error(e)
  }
}

const handleCancel = async (row) => {
  const { value: reason } = await ElMessageBox.prompt('请输入取消原因', '取消订单', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    inputPlaceholder: '如：顾客未到店'
  })

  try {
    await cancelOrder(row.id, reason)
    ElMessage.success('订单已取消')
    await refreshAfterAction()
  } catch (e) {
    console.error(e)
  }
}
</script>

<style lang="scss" scoped>
.goods-item {
  font-size: 13px;
  color: #5f6368;
  line-height: 1.5;
}
</style>
