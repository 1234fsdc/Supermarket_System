<template>
  <div class="statistics-container">
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card>
          <div class="stat-item">
            <div class="stat-label">今日营业额</div>
            <div class="stat-value">¥{{ statsData.todayTurnover }}</div>
            <div class="stat-trend" :class="statsData.turnoverTrend >= 0 ? 'up' : 'down'">
              {{ statsData.turnoverTrend >= 0 ? '↑' : '↓' }} {{ Math.abs(statsData.turnoverTrend).toFixed(1) }}% 较昨日
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card>
          <div class="stat-item">
            <div class="stat-label">有效订单</div>
            <div class="stat-value">{{ statsData.validOrders }}</div>
            <div class="stat-trend" :class="statsData.orderTrend >= 0 ? 'up' : 'down'">
              {{ statsData.orderTrend >= 0 ? '↑' : '↓' }} {{ Math.abs(statsData.orderTrend).toFixed(1) }}% 较昨日
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card>
          <div class="stat-item">
            <div class="stat-label">订单完成率</div>
            <div class="stat-value">{{ statsData.completionRate }}%</div>
            <div class="stat-trend up">--</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card>
          <div class="stat-item">
            <div class="stat-label">平均客单价</div>
            <div class="stat-value">¥{{ statsData.avgOrderAmount }}</div>
            <div class="stat-trend" :class="statsData.amountTrend >= 0 ? 'up' : 'down'">
              {{ statsData.amountTrend >= 0 ? '↑' : '↓' }} {{ Math.abs(statsData.amountTrend).toFixed(1) }}% 较昨日
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>近 10 日营业额趋势</span>
          </template>
          <div ref="lineChartRef" style="height: 300px;"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>订单状态分布</span>
          </template>
          <div ref="pieChartRef" style="height: 300px;"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="24">
        <el-card>
          <template #header>
            <span>近 10 日订单数量趋势</span>
          </template>
          <div ref="barChartRef" style="height: 300px;"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import * as echarts from 'echarts'
import { orderConditionSearch, getOrderStatistics } from '@/api/order'

const lineChartRef = ref(null)
const pieChartRef = ref(null)
const barChartRef = ref(null)

const statsData = ref({
  todayTurnover: '0.00',
  validOrders: 0,
  completionRate: 0,
  avgOrderAmount: '0.00',
  turnoverTrend: 0,
  orderTrend: 0,
  amountTrend: 0
})

const orderStatistics = ref({
  toBeConfirmed: 0,
  confirmed: 0,
  deliveryInProgress: 0
})

const fetchStatsData = async () => {
  const today = new Date()
  const yesterday = new Date()
  yesterday.setDate(today.getDate() - 1)

  const todayStr = formatDate(today)
  const yesterdayStr = formatDate(yesterday)

  const todayBegin = `${todayStr} 00:00:00`
  const todayEnd = `${todayStr} 23:59:59`
  const yesterdayBegin = `${yesterdayStr} 00:00:00`
  const yesterdayEnd = `${yesterdayStr} 23:59:59`

  try {
    const [todayRes, yesterdayRes] = await Promise.all([
      orderConditionSearch({ page: 1, pageSize: 1000, beginTime: todayBegin, endTime: todayEnd }),
      orderConditionSearch({ page: 1, pageSize: 1000, beginTime: yesterdayBegin, endTime: yesterdayEnd })
    ])

    const todayOrders = todayRes.data.records || []
    const yesterdayOrders = yesterdayRes.data.records || []

    const todayTurnover = todayOrders.reduce((sum, o) => sum + (parseFloat(o.amount) || 0), 0)
    const yesterdayTurnover = yesterdayOrders.reduce((sum, o) => sum + (parseFloat(o.amount) || 0), 0)

    const todayValidOrders = todayOrders.filter(o => o.status === 5).length
    const todayCompletedOrders = todayOrders.filter(o => o.status === 5).length
    const todayAllOrders = todayOrders.length

    statsData.value.todayTurnover = todayTurnover.toFixed(2)
    statsData.value.validOrders = todayValidOrders
    statsData.value.completionRate = todayAllOrders > 0 ? ((todayCompletedOrders / todayAllOrders) * 100).toFixed(1) : 0
    statsData.value.avgOrderAmount = todayValidOrders > 0 ? (todayTurnover / todayValidOrders).toFixed(2) : '0.00'

    statsData.value.turnoverTrend = yesterdayTurnover > 0 ? ((todayTurnover - yesterdayTurnover) / yesterdayTurnover * 100) : 0
    statsData.value.orderTrend = yesterdayOrders.length > 0 ? ((todayOrders.length - yesterdayOrders.length) / yesterdayOrders.length * 100) : 0

    const todayAvg = todayValidOrders > 0 ? todayTurnover / todayValidOrders : 0
    const yesterdayValidOrders = yesterdayOrders.filter(o => o.status === 5).length
    const yesterdayAvg = yesterdayValidOrders > 0 ? yesterdayTurnover / yesterdayValidOrders : 0
    statsData.value.amountTrend = yesterdayAvg > 0 ? ((todayAvg - yesterdayAvg) / yesterdayAvg * 100) : 0
  } catch (error) {
    console.error('获取统计数据失败:', error)
  }
}

const fetchOrderStatistics = async () => {
  try {
    const res = await getOrderStatistics()
    orderStatistics.value = res.data
  } catch (error) {
    console.error('获取订单统计失败:', error)
  }
}

const initLineChart = async () => {
  if (!lineChartRef.value) return
  const chart = echarts.init(lineChartRef.value)

  const dates = []
  const turnoverData = []

  for (let i = 9; i >= 0; i--) {
    const date = new Date()
    date.setDate(date.getDate() - i)
    const dateStr = formatDate(date)
    dates.push(dateStr.slice(5))

    const beginTime = `${dateStr} 00:00:00`
    const endTime = `${dateStr} 23:59:59`

    try {
      const res = await orderConditionSearch({ page: 1, pageSize: 1000, beginTime, endTime })
      const records = res.data.records || []
      const turnover = records.reduce((sum, order) => sum + (parseFloat(order.amount) || 0), 0)
      turnoverData.push(turnover.toFixed(2))
    } catch {
      turnoverData.push(0)
    }
  }

  chart.setOption({
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: dates },
    yAxis: { type: 'value', name: '营业额(元)' },
    series: [{
      data: turnoverData,
      type: 'line',
      smooth: true,
      itemStyle: { color: '#409EFF' },
      areaStyle: { color: 'rgba(64, 158, 255, 0.1)' }
    }]
  })
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

const initBarChart = async () => {
  if (!barChartRef.value) return
  const chart = echarts.init(barChartRef.value)

  const dates = []
  const orderCounts = []

  for (let i = 9; i >= 0; i--) {
    const date = new Date()
    date.setDate(date.getDate() - i)
    const dateStr = formatDate(date)
    dates.push(dateStr.slice(5))

    const beginTime = `${dateStr} 00:00:00`
    const endTime = `${dateStr} 23:59:59`

    try {
      const res = await orderConditionSearch({ page: 1, pageSize: 1000, beginTime, endTime })
      orderCounts.push(res.data.total || 0)
    } catch {
      orderCounts.push(0)
    }
  }

  chart.setOption({
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: dates },
    yAxis: { type: 'value', name: '订单数' },
    series: [{
      data: orderCounts,
      type: 'bar',
      itemStyle: { color: '#f59e0b' }
    }]
  })
}

const formatDate = (date) => {
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}

onMounted(() => {
  fetchStatsData()
  fetchOrderStatistics()
  initLineChart()
  initPieChart()
  initBarChart()

  window.addEventListener('resize', () => {
    lineChartRef.value && echarts.getInstanceByDom(lineChartRef.value)?.resize()
    pieChartRef.value && echarts.getInstanceByDom(pieChartRef.value)?.resize()
    barChartRef.value && echarts.getInstanceByDom(barChartRef.value)?.resize()
  })
})
</script>

<style scoped lang="scss">
.statistics-container {
  width: 100%;
}

.stat-item {
  padding: 10px;
}

.stat-label {
  color: #909399;
  font-size: 14px;
  margin-bottom: 10px;
}

.stat-value {
  color: #303133;
  font-size: 28px;
  font-weight: 600;
  margin-bottom: 8px;
}

.stat-trend {
  font-size: 12px;
  &.up { color: #67C23A; }
  &.down { color: #F56C6C; }
}
</style>
