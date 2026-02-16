<template>
  <div class="dashboard-page">
    <!-- 统计卡片 -->
    <div class="stats-grid">
      <div class="stat-card">
        <div class="stat-header">
          <span class="stat-title">今日营业额</span>
          <div class="stat-icon blue"><el-icon :size="20"><Money /></el-icon></div>
        </div>
        <div class="stat-value">¥{{ stats.todaySales?.toFixed(2) || '0.00' }}</div>
        <div class="stat-desc">较昨日 <span :class="stats.salesGrowth >= 0 ? 'up' : 'down'">{{ stats.salesGrowth >= 0 ? '+' : '' }}{{ stats.salesGrowth || 0 }}%</span></div>
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
        <div class="stat-value">{{ stats.verifyRate || 0 }}%</div>
        <div class="stat-desc">待取货 <span class="highlight">{{ stats.pendingOrders || 0 }}</span> 单</div>
      </div>
      
      <div class="stat-card" @click="showLowStock = true" style="cursor: pointer">
        <div class="stat-header">
          <span class="stat-title">库存预警</span>
          <div class="stat-icon red"><el-icon :size="20"><Warning /></el-icon></div>
        </div>
        <div class="stat-value">{{ stats.lowStockCount || 0 }}</div>
        <div class="stat-desc">{{ stats.expiringCount || 0 }} 件临期商品</div>
      </div>
    </div>
    
    <!-- 第一行图表：营业趋势 + 热销商品 -->
    <div class="charts-row">
      <div class="page-card chart-card flex-2">
        <div class="card-header">
          <h3>📈 营业趋势</h3>
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
          <h3>🏆 热销商品 TOP10</h3>
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
    
    <!-- 第二行图表：订单状态 + 分类销售 + 时段分布 -->
    <div class="charts-row three-col">
      <div class="page-card chart-card">
        <div class="card-header">
          <h3>📊 订单状态分布</h3>
        </div>
        <div class="card-body">
          <div ref="statusChart" class="chart"></div>
        </div>
      </div>
      
      <div class="page-card chart-card">
        <div class="card-header">
          <h3>🍕 分类销售占比</h3>
        </div>
        <div class="card-body">
          <div ref="categoryChart" class="chart"></div>
        </div>
      </div>
      
      <div class="page-card chart-card">
        <div class="card-header">
          <h3>⏰ 时段订单分布</h3>
        </div>
        <div class="card-body">
          <div ref="hourlyChart" class="chart"></div>
        </div>
      </div>
    </div>
    
    <!-- 第三行：最近订单 -->
    <div class="page-card">
      <div class="card-header">
        <h3>📋 最近订单</h3>
        <el-button type="primary" size="small" @click="$router.push('/order')">查看全部</el-button>
      </div>
      <div class="card-body">
        <el-table :data="recentOrders" style="width: 100%">
          <el-table-column prop="orderNo" label="订单编号" width="180" />
          <el-table-column prop="userPhone" label="用户手机" width="130" />
          <el-table-column prop="totalPrice" label="订单金额" width="120">
            <template #default="{ row }">
              <span class="price">¥{{ row.totalPrice?.toFixed(2) }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="getStatusType(row.status)" size="small">{{ getStatusText(row.status) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="verifyCode" label="取货码" width="100">
            <template #default="{ row }">
              <span class="verify-code">{{ row.verifyCode }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="createdAt" label="下单时间" width="180" />
          <el-table-column label="操作" width="120">
            <template #default="{ row }">
              <el-button v-if="row.status === 'pending'" type="success" size="small" @click="handleVerify(row)">核销</el-button>
              <el-button v-else type="info" size="small" disabled>已处理</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>
    
    <!-- 库存预警弹窗 -->
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
import { ref, onMounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getDashboard, getSalesTrend, getTopSales, getLowStock, getExpiring, getOrderList, verifyOrder } from '@/api'

const stats = ref({
  todaySales: 626.40,
  salesGrowth: 15.2,
  todayOrders: 7,
  todayVerified: 3,
  verifyRate: 42.9,
  pendingOrders: 4,
  lowStockCount: 2,
  expiringCount: 1
})
const trendDays = ref(7)
const topDays = ref(7)
const trendChart = ref()
const topChart = ref()
const statusChart = ref()
const categoryChart = ref()
const hourlyChart = ref()
const showLowStock = ref(false)
const alertTab = ref('lowStock')
const lowStockList = ref([])
const expiringList = ref([])
const recentOrders = ref([])

let trendChartInstance = null
let topChartInstance = null
let statusChartInstance = null
let categoryChartInstance = null
let hourlyChartInstance = null

// 多巴胺配色
const colors = {
  blue: '#54A0FF',
  green: '#1DD1A1',
  yellow: '#FECA57',
  red: '#FF6B6B',
  purple: '#A29BFE',
  cyan: '#48DBFB',
  orange: '#FF9F43',
  pink: '#FF9FF3'
}

onMounted(async () => {
  await fetchDashboard()
  await nextTick()
  initCharts()
  fetchTrend()
  fetchTopSales()
  fetchStatusData()
  fetchCategoryData()
  fetchHourlyData()
  fetchAlerts()
  fetchRecentOrders()
})

const fetchDashboard = async () => {
  try {
    const res = await getDashboard()
    if (res.data) {
      stats.value = { ...stats.value, ...res.data }
    }
  } catch (e) {
    console.error(e)
  }
}

const initCharts = () => {
  trendChartInstance = echarts.init(trendChart.value)
  topChartInstance = echarts.init(topChart.value)
  statusChartInstance = echarts.init(statusChart.value)
  categoryChartInstance = echarts.init(categoryChart.value)
  hourlyChartInstance = echarts.init(hourlyChart.value)
  
  window.addEventListener('resize', () => {
    trendChartInstance?.resize()
    topChartInstance?.resize()
    statusChartInstance?.resize()
    categoryChartInstance?.resize()
    hourlyChartInstance?.resize()
  })
}

const fetchTrend = async () => {
  try {
    const res = await getSalesTrend({ days: trendDays.value })
    let data = res.data || []
    
    // 如果没有数据，使用测试数据
    if (data.length === 0) {
      data = generateTrendTestData(trendDays.value)
    }
    
    trendChartInstance.setOption({
      tooltip: { 
        trigger: 'axis',
        backgroundColor: 'rgba(255,255,255,0.95)',
        borderColor: '#eee',
        textStyle: { color: '#333' }
      },
      legend: { data: ['销售额', '订单数'], top: 10 },
      grid: { left: '3%', right: '4%', bottom: '3%', top: 60, containLabel: true },
      xAxis: {
        type: 'category',
        data: data.map(d => d.date),
        axisLine: { lineStyle: { color: '#e0e6ed' } },
        axisLabel: { color: '#576574' }
      },
      yAxis: [
        {
          type: 'value',
          name: '销售额',
          axisLine: { show: false },
          splitLine: { lineStyle: { color: '#f0f3f7' } },
          axisLabel: { color: '#576574' }
        },
        {
          type: 'value',
          name: '订单数',
          axisLine: { show: false },
          splitLine: { show: false },
          axisLabel: { color: '#576574' }
        }
      ],
      series: [
        {
          name: '销售额',
          data: data.map(d => d.amount),
          type: 'line',
          smooth: true,
          yAxisIndex: 0,
          areaStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: 'rgba(84, 160, 255, 0.4)' },
              { offset: 1, color: 'rgba(84, 160, 255, 0.05)' }
            ])
          },
          lineStyle: { color: colors.blue, width: 3 },
          itemStyle: { color: colors.blue }
        },
        {
          name: '订单数',
          data: data.map(d => d.orders || Math.floor(d.amount / 80)),
          type: 'bar',
          yAxisIndex: 1,
          barWidth: 20,
          itemStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: colors.green },
              { offset: 1, color: '#10AC84' }
            ]),
            borderRadius: [4, 4, 0, 0]
          }
        }
      ]
    })
  } catch (e) {
    console.error(e)
  }
}

const fetchTopSales = async () => {
  try {
    const res = await getTopSales({ days: topDays.value, limit: 10 })
    let data = res.data || []
    
    // 如果没有数据，使用测试数据
    if (data.length === 0) {
      data = [
        { goods_name: '三只松鼠坚果礼盒', total_quantity: 28 },
        { goods_name: '蒙牛纯牛奶', total_quantity: 24 },
        { goods_name: '伊利安慕希酸奶', total_quantity: 20 },
        { goods_name: '百草味每日坚果', total_quantity: 18 },
        { goods_name: '可口可乐', total_quantity: 15 },
        { goods_name: '乐事薯片原味', total_quantity: 14 },
        { goods_name: '维达抽纸', total_quantity: 10 },
        { goods_name: '奥利奥原味夹心饼干', total_quantity: 8 },
        { goods_name: '康师傅冰红茶', total_quantity: 6 },
        { goods_name: '阿尔卑斯棒棒糖', total_quantity: 5 }
      ]
    }
    
    const barColors = [colors.red, colors.orange, colors.yellow, colors.green, colors.cyan, colors.blue, colors.purple, colors.pink, '#8395A7', '#B2BEC3']
    
    topChartInstance.setOption({
      tooltip: { 
        trigger: 'axis', 
        axisPointer: { type: 'shadow' },
        backgroundColor: 'rgba(255,255,255,0.95)',
        borderColor: '#eee',
        textStyle: { color: '#333' }
      },
      grid: { left: '3%', right: '8%', bottom: '3%', top: 10, containLabel: true },
      xAxis: {
        type: 'value',
        axisLine: { show: false },
        splitLine: { lineStyle: { color: '#f0f3f7' } },
        axisLabel: { color: '#576574' }
      },
      yAxis: {
        type: 'category',
        data: data.map(d => d.goods_name).reverse(),
        axisLine: { lineStyle: { color: '#e0e6ed' } },
        axisLabel: { 
          color: '#576574',
          width: 100,
          overflow: 'truncate'
        }
      },
      series: [{
        data: data.map((d, i) => ({
          value: d.total_quantity,
          itemStyle: { color: barColors[data.length - 1 - i] || colors.blue }
        })).reverse(),
        type: 'bar',
        barWidth: 16,
        itemStyle: { borderRadius: [0, 8, 8, 0] },
        label: {
          show: true,
          position: 'right',
          color: '#576574'
        }
      }]
    })
  } catch (e) {
    console.error(e)
  }
}

const fetchStatusData = () => {
  // 订单状态分布测试数据
  const data = [
    { name: '待取货', value: 4, color: colors.yellow },
    { name: '已完成', value: 38, color: colors.green },
    { name: '已取消', value: 2, color: colors.red }
  ]
  
  statusChartInstance.setOption({
    tooltip: { 
      trigger: 'item',
      backgroundColor: 'rgba(255,255,255,0.95)',
      borderColor: '#eee',
      textStyle: { color: '#333' }
    },
    legend: { bottom: 10, left: 'center' },
    series: [{
      type: 'pie',
      radius: ['45%', '70%'],
      center: ['50%', '45%'],
      avoidLabelOverlap: false,
      itemStyle: { borderRadius: 8, borderColor: '#fff', borderWidth: 3 },
      label: {
        show: true,
        position: 'center',
        formatter: () => '订单\n状态',
        fontSize: 14,
        fontWeight: 'bold',
        color: '#576574'
      },
      emphasis: {
        label: { show: true, fontSize: 16, fontWeight: 'bold' }
      },
      labelLine: { show: false },
      data: data.map(d => ({ ...d, itemStyle: { color: d.color } }))
    }]
  })
}

const fetchCategoryData = () => {
  // 分类销售占比测试数据
  const data = [
    { name: '零食', value: 45, color: colors.pink },
    { name: '饮料', value: 35, color: colors.cyan },
    { name: '日用', value: 20, color: colors.purple }
  ]
  
  categoryChartInstance.setOption({
    tooltip: { 
      trigger: 'item',
      formatter: '{b}: {c}% ({d}%)',
      backgroundColor: 'rgba(255,255,255,0.95)',
      borderColor: '#eee',
      textStyle: { color: '#333' }
    },
    legend: { bottom: 10, left: 'center' },
    series: [{
      type: 'pie',
      radius: ['0%', '70%'],
      center: ['50%', '45%'],
      roseType: 'area',
      itemStyle: { borderRadius: 8 },
      label: {
        show: true,
        formatter: '{b}\n{c}%'
      },
      data: data.map(d => ({ ...d, itemStyle: { color: d.color } }))
    }]
  })
}

const fetchHourlyData = () => {
  // 时段订单分布测试数据
  const hours = ['9:00', '10:00', '11:00', '12:00', '13:00', '14:00', '15:00', '16:00', '17:00', '18:00', '19:00', '20:00']
  const data = [3, 5, 8, 12, 6, 9, 11, 14, 10, 7, 4, 2]
  
  hourlyChartInstance.setOption({
    tooltip: { 
      trigger: 'axis',
      backgroundColor: 'rgba(255,255,255,0.95)',
      borderColor: '#eee',
      textStyle: { color: '#333' }
    },
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
      data: data.map((value, index) => ({
        value,
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: colors.orange },
            { offset: 1, color: colors.yellow }
          ])
        }
      })),
      type: 'bar',
      barWidth: 20,
      itemStyle: { borderRadius: [4, 4, 0, 0] }
    }]
  })
}

const fetchAlerts = async () => {
  try {
    const [lowRes, expRes] = await Promise.all([getLowStock(), getExpiring({ days: 30 })])
    lowStockList.value = lowRes.data || [
      { name: '农夫山泉NFC橙汁', stock: 8, safetyStock: 10 },
      { name: '伊利安慕希酸奶', stock: 3, safetyStock: 5 }
    ]
    expiringList.value = expRes.data || [
      { name: '农夫山泉NFC橙汁', expireDate: '2026-02-10', stock: 8 }
    ]
  } catch (e) {
    console.error(e)
  }
}

const fetchRecentOrders = async () => {
  try {
    const res = await getOrderList({ pageNum: 1, pageSize: 5 })
    recentOrders.value = res.data?.list || [
      { orderNo: 'LE20260128007', userPhone: '13900000008', totalPrice: 199.80, status: 'pending', verifyCode: '789012', createdAt: '2026-01-28 16:45:00' },
      { orderNo: 'LE20260128006', userPhone: '13900000007', totalPrice: 23.70, status: 'completed', verifyCode: '678901', createdAt: '2026-01-28 15:30:00' },
      { orderNo: 'LE20260128005', userPhone: '13900000006', totalPrice: 78.90, status: 'pending', verifyCode: '567890', createdAt: '2026-01-28 14:15:00' },
      { orderNo: 'LE20260128004', userPhone: '13900000005', totalPrice: 42.50, status: 'completed', verifyCode: '456789', createdAt: '2026-01-28 12:00:00' },
      { orderNo: 'LE20260128003', userPhone: '13900000004', totalPrice: 156.70, status: 'completed', verifyCode: '345678', createdAt: '2026-01-28 11:20:00' }
    ]
  } catch (e) {
    console.error(e)
  }
}

const generateTrendTestData = (days) => {
  const data = []
  const today = new Date()
  for (let i = days - 1; i >= 0; i--) {
    const date = new Date(today)
    date.setDate(date.getDate() - i)
    const dateStr = `${date.getMonth() + 1}-${date.getDate()}`
    const amount = Math.floor(Math.random() * 500) + 200
    const orders = Math.floor(amount / 80) + Math.floor(Math.random() * 3)
    data.push({ date: dateStr, amount, orders })
  }
  return data
}

const getStatusType = (status) => {
  const types = { pending: 'warning', completed: 'success', cancelled: 'info' }
  return types[status] || 'info'
}

const getStatusText = (status) => {
  const texts = { pending: '待取货', completed: '已完成', cancelled: '已取消' }
  return texts[status] || status
}

const handleVerify = async (order) => {
  try {
    await ElMessageBox.confirm(`确认核销订单 ${order.orderNo}？`, '核销确认')
    await verifyOrder({ verifyCode: order.verifyCode })
    ElMessage.success('核销成功')
    fetchRecentOrders()
    fetchDashboard()
  } catch (e) {
    if (e !== 'cancel') console.error(e)
  }
}
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

.page-card {
  .card-header {
    h3 {
      display: flex;
      align-items: center;
      gap: 8px;
    }
  }
}

.price {
  color: #FF6B6B;
  font-weight: 600;
}

.verify-code {
  font-family: 'Monaco', 'Consolas', monospace;
  font-weight: 600;
  color: #54A0FF;
  background: rgba(84, 160, 255, 0.1);
  padding: 2px 8px;
  border-radius: 4px;
}

.danger-text {
  color: #FF6B6B;
  font-weight: 600;
}
</style>
