<template>
  <div class="dashboard-page">
    <div class="stats-grid">
      <div class="stat-card">
        <div class="stat-header">
          <span class="stat-title">今日营业额</span>
          <div class="stat-icon blue"><el-icon :size="20"><Money /></el-icon></div>
        </div>
        <div class="stat-value">¥{{ Number(stats.todaySales || 0).toFixed(2) }}</div>
        <div class="stat-desc">
          较昨日
          <span :class="stats.salesGrowth >= 0 ? 'up' : 'down'">
            {{ stats.salesGrowth >= 0 ? '+' : '' }}{{ Number(stats.salesGrowth || 0).toFixed(2) }}%
          </span>
        </div>
      </div>

      <div class="stat-card">
        <div class="stat-header">
          <span class="stat-title">今日订单</span>
          <div class="stat-icon green"><el-icon :size="20"><Document /></el-icon></div>
        </div>
        <div class="stat-value">{{ stats.todayOrders || 0 }}</div>
        <div class="stat-desc">已核销 <span class="highlight">{{ stats.todayVerified || 0 }}</span> 单</div>
      </div>

      <div class="stat-card">
        <div class="stat-header">
          <span class="stat-title">核销转化率</span>
          <div class="stat-icon yellow"><el-icon :size="20"><TrendCharts /></el-icon></div>
        </div>
        <div class="stat-value">{{ Number(stats.verifyRate || 0).toFixed(2) }}%</div>
        <div class="stat-desc">待取货 <span class="highlight">{{ stats.pendingOrders || 0 }}</span> 单</div>
      </div>

      <div class="stat-card" style="cursor: pointer" @click="showLowStock = true">
        <div class="stat-header">
          <span class="stat-title">库存预警</span>
          <div class="stat-icon red"><el-icon :size="20"><Warning /></el-icon></div>
        </div>
        <div class="stat-value">{{ stats.lowStockCount || 0 }}</div>
        <div class="stat-desc">{{ stats.expiringCount || 0 }} 件临期商品</div>
      </div>
    </div>

    <div class="charts-row">
      <div class="page-card chart-card flex-2">
        <div class="card-header">
          <h3>营业趋势</h3>
          <el-radio-group v-model="trendDays" size="small" @change="fetchTrend">
            <el-radio-button :value="7">近7天</el-radio-button>
            <el-radio-button :value="30">近30天</el-radio-button>
          </el-radio-group>
        </div>
        <div class="card-body">
          <div ref="trendChart" class="chart"></div>
        </div>
      </div>

      <div class="page-card chart-card flex-1">
        <div class="card-header">
          <h3>热销商品 TOP10</h3>
          <el-radio-group v-model="topDays" size="small" @change="fetchTopSales">
            <el-radio-button :value="7">本周</el-radio-button>
            <el-radio-button :value="30">本月</el-radio-button>
          </el-radio-group>
        </div>
        <div class="card-body">
          <div ref="topChart" class="chart"></div>
        </div>
      </div>
    </div>

    <div class="charts-row three-col">
      <div class="page-card chart-card">
        <div class="card-header">
          <h3>订单状态分布</h3>
        </div>
        <div class="card-body">
          <div ref="statusChart" class="chart"></div>
        </div>
      </div>

      <div class="page-card chart-card">
        <div class="card-header">
          <h3>分类销售占比</h3>
        </div>
        <div class="card-body">
          <div ref="categoryChart" class="chart"></div>
        </div>
      </div>

      <div class="page-card chart-card">
        <div class="card-header">
          <h3>时段订单分布</h3>
        </div>
        <div class="card-body">
          <div ref="hourlyChart" class="chart"></div>
        </div>
      </div>
    </div>

    <div class="page-card">
      <div class="card-header">
        <h3>最近订单</h3>
        <el-button type="primary" size="small" @click="router.push('/orders')">查看全部</el-button>
      </div>
      <div class="card-body">
        <el-table :data="recentOrders" style="width: 100%">
          <el-table-column prop="orderNo" label="订单编号" min-width="220" show-overflow-tooltip />
          <el-table-column prop="userPhone" label="用户手机" width="130" />
          <el-table-column prop="totalPrice" label="订单金额" width="120">
            <template #default="{ row }">
              <span class="price">¥{{ Number(row.totalPrice || 0).toFixed(2) }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="getStatusType(row.status)" size="small">{{ getStatusText(row.status) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="verifyCode" label="取货码" width="100" />
          <el-table-column prop="createdAt" label="下单时间" width="180">
            <template #default="{ row }">{{ formatTime(row.createdAt) }}</template>
          </el-table-column>
          <el-table-column label="操作" width="120">
            <template #default="{ row }">
              <el-button v-if="row.status === 'pending'" type="success" size="small" @click="handleVerify(row)">核销</el-button>
              <el-button v-else type="info" size="small" disabled>已处理</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>

    <el-dialog v-model="showLowStock" title="库存预警" width="600px">
      <el-tabs v-model="alertTab">
        <el-tab-pane label="库存不足" name="lowStock">
          <el-table :data="lowStockList" max-height="400">
            <el-table-column prop="name" label="商品名称" />
            <el-table-column prop="stock" label="当前库存" width="100">
              <template #default="{ row }">
                <span class="danger-text">{{ row.stock }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="safetyStock" label="安全库存" width="100" />
          </el-table>
        </el-tab-pane>
        <el-tab-pane label="临期商品" name="expiring">
          <el-table :data="expiringList" max-height="400">
            <el-table-column prop="name" label="商品名称" />
            <el-table-column prop="expireDate" label="过期日期" width="120" />
            <el-table-column prop="stock" label="库存" width="80" />
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </el-dialog>
  </div>
</template>

<script setup>
import { nextTick, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import * as echarts from 'echarts'
import dayjs from 'dayjs'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getCategorySales,
  getDashboard,
  getExpiring,
  getHourlyOrders,
  getLowStock,
  getOrderStatus,
  getOrders,
  getSalesTrend,
  getTopSales,
  verifyOrder
} from '@/api'

const router = useRouter()

const stats = ref({
  todaySales: 0,
  salesGrowth: 0,
  todayOrders: 0,
  todayVerified: 0,
  verifyRate: 0,
  pendingOrders: 0,
  lowStockCount: 0,
  expiringCount: 0
})

const trendDays = ref(7)
const topDays = ref(7)
const showLowStock = ref(false)
const alertTab = ref('lowStock')
const lowStockList = ref([])
const expiringList = ref([])
const recentOrders = ref([])

const trendChart = ref()
const topChart = ref()
const statusChart = ref()
const categoryChart = ref()
const hourlyChart = ref()

let trendChartInstance = null
let topChartInstance = null
let statusChartInstance = null
let categoryChartInstance = null
let hourlyChartInstance = null

const colors = {
  blue: '#54A0FF',
  green: '#1DD1A1',
  yellow: '#FECA57',
  red: '#FF6B6B',
  purple: '#A29BFE',
  cyan: '#48DBFB',
  orange: '#FF9F43'
}

const statusText = {
  pending: '待取货',
  completed: '已完成',
  cancelled: '已取消'
}

const resizeCharts = () => {
  trendChartInstance?.resize()
  topChartInstance?.resize()
  statusChartInstance?.resize()
  categoryChartInstance?.resize()
  hourlyChartInstance?.resize()
}

const createNoDataGraphic = (text = '暂无数据') => ({
  type: 'text',
  left: 'center',
  top: 'middle',
  style: {
    text,
    fill: '#909399',
    fontSize: 16,
    fontWeight: 500
  }
})

const formatTime = (value) => {
  if (!value) return '--'
  const time = dayjs(value)
  return time.isValid() ? time.format('YYYY-MM-DD HH:mm:ss') : '--'
}

const getStatusType = (status) => {
  return { pending: 'warning', completed: 'success', cancelled: 'info' }[status] || 'info'
}

const getStatusText = (status) => {
  return statusText[status] || status || '--'
}

const initCharts = () => {
  trendChartInstance = echarts.init(trendChart.value)
  topChartInstance = echarts.init(topChart.value)
  statusChartInstance = echarts.init(statusChart.value)
  categoryChartInstance = echarts.init(categoryChart.value)
  hourlyChartInstance = echarts.init(hourlyChart.value)
  window.addEventListener('resize', resizeCharts)
}

const fetchDashboard = async () => {
  const res = await getDashboard()
  stats.value = {
    ...stats.value,
    ...(res.data || {})
  }
}

const fetchTrend = async () => {
  const res = await getSalesTrend({ days: trendDays.value })
  const data = res.data || []

  trendChartInstance.setOption({
    tooltip: { trigger: 'axis' },
    graphic: data.length === 0 ? createNoDataGraphic() : null,
    grid: { left: '3%', right: '4%', bottom: '3%', top: 30, containLabel: true },
    xAxis: {
      type: 'category',
      data: data.map(item => item.date),
      axisLine: { lineStyle: { color: '#e0e6ed' } },
      axisLabel: { color: '#576574' }
    },
    yAxis: {
      type: 'value',
      axisLine: { show: false },
      splitLine: { lineStyle: { color: '#f0f3f7' } },
      axisLabel: { color: '#576574' }
    },
    series: [{
      name: '销售额',
      type: 'line',
      smooth: true,
      data: data.map(item => Number(item.amount || 0)),
      lineStyle: { color: colors.blue, width: 3 },
      itemStyle: { color: colors.blue },
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(84,160,255,0.35)' },
          { offset: 1, color: 'rgba(84,160,255,0.05)' }
        ])
      }
    }]
  }, true)
}

const fetchTopSales = async () => {
  const res = await getTopSales({ days: topDays.value, limit: 10 })
  const data = (res.data || []).slice().reverse()

  topChartInstance.setOption({
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    graphic: data.length === 0 ? createNoDataGraphic() : null,
    grid: { left: '3%', right: '8%', bottom: '3%', top: 10, containLabel: true },
    xAxis: {
      type: 'value',
      axisLine: { show: false },
      splitLine: { lineStyle: { color: '#f0f3f7' } },
      axisLabel: { color: '#576574' }
    },
    yAxis: {
      type: 'category',
      data: data.map(item => item.goods_name),
      axisLine: { lineStyle: { color: '#e0e6ed' } },
      axisLabel: { color: '#576574', width: 110, overflow: 'truncate' }
    },
    series: [{
      type: 'bar',
      barWidth: 16,
      data: data.map(item => Number(item.total_quantity || 0)),
      itemStyle: {
        color: colors.green,
        borderRadius: [0, 8, 8, 0]
      },
      label: {
        show: true,
        position: 'right',
        color: '#576574'
      }
    }]
  }, true)
}

const fetchStatusData = async () => {
  const res = await getOrderStatus()
  const raw = res.data || []
  const data = ['pending', 'completed', 'cancelled']
    .map(status => {
      const target = raw.find(item => item.status === status)
      return {
        name: getStatusText(status),
        value: Number(target?.count || 0),
        itemStyle: {
          color: {
            pending: colors.yellow,
            completed: colors.green,
            cancelled: colors.red
          }[status]
        }
      }
    })
    .filter(item => item.value > 0)

  statusChartInstance.setOption({
    tooltip: { trigger: 'item' },
    legend: { bottom: 10, left: 'center' },
    graphic: data.length === 0 ? createNoDataGraphic() : null,
    series: [{
      type: 'pie',
      radius: ['45%', '70%'],
      center: ['50%', '45%'],
      label: { show: data.length > 0 },
      data
    }]
  }, true)
}

const fetchCategoryData = async () => {
  const res = await getCategorySales({ days: 30 })
  const data = res.data || []

  categoryChartInstance.setOption({
    tooltip: { trigger: 'item', formatter: '{b}: ¥{c}' },
    legend: { bottom: 10, left: 'center' },
    graphic: data.length === 0 ? createNoDataGraphic() : null,
    series: [{
      type: 'pie',
      radius: ['0%', '70%'],
      center: ['50%', '45%'],
      roseType: 'area',
      label: {
        show: data.length > 0,
        formatter: ({ name, value }) => `${name}\n¥${Number(value || 0).toFixed(2)}`
      },
      data: data.map((item, index) => ({
        name: item.categoryName,
        value: Number(item.amount || 0),
        itemStyle: {
          color: [colors.purple, colors.cyan, colors.orange, colors.blue, colors.green][index % 5]
        }
      }))
    }]
  }, true)
}

const fetchHourlyData = async () => {
  const res = await getHourlyOrders()
  const raw = res.data || []
  const countMap = new Map(raw.map(item => [item.hour, Number(item.count || 0)]))
  const hours = Array.from({ length: 24 }, (_, index) => `${String(index).padStart(2, '0')}:00`)
  const values = hours.map(hour => countMap.get(hour.slice(0, 2)) || 0)
  const hasData = raw.length > 0

  hourlyChartInstance.setOption({
    tooltip: { trigger: 'axis' },
    graphic: hasData ? null : createNoDataGraphic(),
    grid: { left: '3%', right: '4%', bottom: '3%', top: 20, containLabel: true },
    xAxis: {
      type: 'category',
      data: hours,
      axisLine: { lineStyle: { color: '#e0e6ed' } },
      axisLabel: { color: '#576574', rotate: 45 }
    },
    yAxis: {
      type: 'value',
      axisLine: { show: false },
      splitLine: { lineStyle: { color: '#f0f3f7' } },
      axisLabel: { color: '#576574' }
    },
    series: [{
      type: 'bar',
      barWidth: 18,
      data: values,
      itemStyle: {
        color: colors.orange,
        borderRadius: [4, 4, 0, 0]
      }
    }]
  }, true)
}

const fetchAlerts = async () => {
  const [lowRes, expRes] = await Promise.all([
    getLowStock(),
    getExpiring({ days: 30 })
  ])
  lowStockList.value = lowRes.data || []
  expiringList.value = expRes.data || []
}

const fetchRecentOrders = async () => {
  const res = await getOrders({ pageNum: 1, pageSize: 5 })
  recentOrders.value = res.data?.list || []
}

const refreshDashboard = async () => {
  await Promise.all([
    fetchDashboard(),
    fetchTrend(),
    fetchTopSales(),
    fetchStatusData(),
    fetchCategoryData(),
    fetchHourlyData(),
    fetchAlerts(),
    fetchRecentOrders()
  ])
}

const handleVerify = async (order) => {
  await ElMessageBox.confirm(`确认核销订单 ${order.orderNo}？`, '核销确认')
  try {
    await verifyOrder(order.id)
    ElMessage.success('核销成功')
    await refreshDashboard()
  } catch (e) {
    console.error(e)
  }
}

onMounted(async () => {
  await nextTick()
  initCharts()
  await refreshDashboard()
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', resizeCharts)
  trendChartInstance?.dispose()
  topChartInstance?.dispose()
  statusChartInstance?.dispose()
  categoryChartInstance?.dispose()
  hourlyChartInstance?.dispose()
})
</script>

<style lang="scss" scoped>
.stats-grid {
  .stat-desc {
    .up { color: #1DD1A1; }
    .down { color: #FF6B6B; }
    .highlight { color: #54A0FF; font-weight: 600; }
  }
}

.charts-row {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 20px;
  margin-bottom: 20px;

  &.three-col {
    grid-template-columns: repeat(3, 1fr);
  }

  .flex-2 { grid-column: span 1; }
  .flex-1 { grid-column: span 1; }
}

.chart-card {
  .chart {
    height: 320px;
  }
}

.price {
  color: #FF6B6B;
  font-weight: 600;
}

.danger-text {
  color: #FF6B6B;
  font-weight: 600;
}
</style>
