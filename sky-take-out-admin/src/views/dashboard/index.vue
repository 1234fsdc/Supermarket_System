<template>
  <div class="dashboard-container">
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <el-icon class="stat-icon" color="#409EFF" :size="40"><Money /></el-icon>
            <div class="stat-info">
              <div class="stat-title">今日营业额</div>
              <div class="stat-value">¥{{ dashboardData.todayTurnover || '0.00' }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <el-icon class="stat-icon" color="#67C23A" :size="40"><List /></el-icon>
            <div class="stat-info">
              <div class="stat-title">今日订单数</div>
              <div class="stat-value">{{ dashboardData.todayOrderCount || 0 }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <el-icon class="stat-icon" color="#E6A23C" :size="40"><Goods /></el-icon>
            <div class="stat-info">
              <div class="stat-title">商品总数</div>
              <div class="stat-value">{{ dashboardData.totalProducts || 0 }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <el-icon class="stat-icon" color="#F56C6C" :size="40"><UserFilled /></el-icon>
            <div class="stat-info">
              <div class="stat-title">员工总数</div>
              <div class="stat-value">{{ dashboardData.totalEmployees || 0 }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>订单状态统计</span>
            </div>
          </template>
          <div ref="pieChartRef" style="height: 300px;"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>最新订单</span>
            </div>
          </template>
          <el-table :data="recentOrders" style="width: 100%">
            <el-table-column prop="number" label="订单号" width="180" />
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="getStatusType(row.status)">
                  {{ getStatusText(row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="amount" label="金额" width="100">
              <template #default="{ row }">¥{{ row.amount }}</template>
            </el-table-column>
            <el-table-column prop="orderTime" label="下单时间" />
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="24">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>近 7 日订单趋势</span>
            </div>
          </template>
          <div ref="lineChartRef" style="height: 300px;"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Money, List, Goods, UserFilled } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import { orderConditionSearch, getOrderStatistics } from '@/api/order'
import { employeePageQuery } from '@/api/employee'
import { productPageQuery } from '@/api/product'

const pieChartRef = ref(null)
const lineChartRef = ref(null)

const dashboardData = ref({
  todayTurnover: 0,
  todayOrderCount: 0,
  totalProducts: 0,
  totalEmployees: 0
})

const orderStatistics = ref({
  toBeConfirmed: 0,
  confirmed: 0,
  deliveryInProgress: 0
})

const recentOrders = ref([])

const getStatusType = (status) => {
  const types = { 1: 'info', 2: 'warning', 3: '', 4: 'primary', 5: 'success', 6: 'danger' }
  return types[status] || 'info'
}

const getStatusText = (status) => {
  const texts = { 1: '待付款', 2: '待接单', 3: '已接单', 4: '派送中', 5: '已完成', 6: '已取消' }
  return texts[status] || '未知'
}

const fetchDashboardData = async () => {
  try {
    const today = new Date()
    const todayStr = `${today.getFullYear()}-${String(today.getMonth() + 1).padStart(2, '0')}-${String(today.getDate()).padStart(2, '0')}`
    const beginTime = `${todayStr} 00:00:00`
    const endTime = `${todayStr} 23:59:59`

    const orderRes = await orderConditionSearch({ page: 1, pageSize: 100, beginTime, endTime })
    const todayOrders = orderRes.data.records || []
    dashboardData.value.todayOrderCount = todayOrders.length

    const turnover = todayOrders.reduce((sum, order) => {
      return sum + (parseFloat(order.amount) || 0)
    }, 0)
    dashboardData.value.todayTurnover = turnover.toFixed(2)
  } catch (error) {
    console.error('获取今日订单数据失败:', error)
  }

  try {
    const productRes = await productPageQuery({ page: 1, pageSize: 1 })
    dashboardData.value.totalProducts = productRes.data.total || 0
  } catch (error) {
    console.error('获取商品总数失败:', error)
  }

  try {
    const employeeRes = await employeePageQuery({ page: 1, pageSize: 1 })
    dashboardData.value.totalEmployees = employeeRes.data.total || 0
  } catch (error) {
    console.error('获取员工总数失败:', error)
  }
}

const fetchOrderStatistics = async () => {
  try {
    const res = await getOrderStatistics()
    orderStatistics.value = res.data
    initPieChart()
  } catch (error) {
    console.error('获取订单统计失败:', error)
  }
}

const fetchRecentOrders = async () => {
  try {
    const res = await orderConditionSearch({ page: 1, pageSize: 5 })
    recentOrders.value = res.data.records || []
  } catch (error) {
    console.error('获取最新订单失败:', error)
  }
}

const initPieChart = () => {
  if (!pieChartRef.value) return
  const chart = echarts.init(pieChartRef.value)
  chart.setOption({
    tooltip: { trigger: 'item' },
    legend: { orient: 'vertical', left: 'left' },
    series: [{
      type: 'pie',
      radius: '60%',
      data: [
        { value: orderStatistics.value.toBeConfirmed || 0, name: '待接单' },
        { value: orderStatistics.value.confirmed || 0, name: '待派送' },
        { value: orderStatistics.value.deliveryInProgress || 0, name: '派送中' }
      ],
      emphasis: { itemStyle: { shadowBlur: 10, shadowOffsetX: 0, shadowColor: 'rgba(0, 0, 0, 0.5)' } }
    }]
  })
}

const initLineChart = async () => {
  if (!lineChartRef.value) return
  const chart = echarts.init(lineChartRef.value)

  const dates = []
  const orderCounts = []
  const turnoverData = []

  for (let i = 6; i >= 0; i--) {
    const date = new Date()
    date.setDate(date.getDate() - i)
    const dateStr = `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
    dates.push(dateStr.slice(5))

    const beginTime = `${dateStr} 00:00:00`
    const endTime = `${dateStr} 23:59:59`

    try {
      const res = await orderConditionSearch({ page: 1, pageSize: 100, beginTime, endTime })
      const records = res.data.records || []
      orderCounts.push(records.length)
      const turnover = records.reduce((sum, order) => sum + (parseFloat(order.amount) || 0), 0)
      turnoverData.push(turnover.toFixed(2))
    } catch {
      orderCounts.push(0)
      turnoverData.push(0)
    }
  }

  chart.setOption({
    tooltip: { trigger: 'axis' },
    legend: { data: ['订单数', '营业额'] },
    xAxis: { type: 'category', data: dates },
    yAxis: [
      { type: 'value', name: '订单数' },
      { type: 'value', name: '营业额(元)' }
    ],
    series: [
      {
        name: '订单数',
        data: orderCounts,
        type: 'line',
        smooth: true,
        itemStyle: { color: '#409EFF' }
      },
      {
        name: '营业额',
        data: turnoverData,
        type: 'line',
        smooth: true,
        yAxisIndex: 1,
        itemStyle: { color: '#f59e0b' }
      }
    ]
  })
}

onMounted(() => {
  fetchDashboardData()
  fetchOrderStatistics()
  fetchRecentOrders()
  initLineChart()

  window.addEventListener('resize', () => {
    pieChartRef.value && echarts.getInstanceByDom(pieChartRef.value)?.resize()
    lineChartRef.value && echarts.getInstanceByDom(lineChartRef.value)?.resize()
  })
})
</script>

<style scoped lang="scss">
.dashboard-container {
  width: 100%;
}

.stat-card {
  :deep(.el-card__body) {
    padding: 10px;
  }
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 20px;
}

.stat-info {
  flex: 1;
}

.stat-title {
  color: #909399;
  font-size: 14px;
  margin-bottom: 8px;
}

.stat-value {
  color: #303133;
  font-size: 24px;
  font-weight: 600;
}

.card-header {
  font-weight: 500;
  font-size: 16px;
}
</style>
