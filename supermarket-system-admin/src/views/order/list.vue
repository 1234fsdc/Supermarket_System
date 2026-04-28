<template>
  <div class="order-container">
    <el-card>
      <el-form :inline="true" :model="queryForm" style="margin-bottom: 20px;">
        <el-form-item label="订单号">
          <el-input v-model="queryForm.number" placeholder="请输入订单号" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryForm.status" placeholder="请选择" clearable>
            <el-option label="待接单" :value="2" />
            <el-option label="已接单" :value="3" />
            <el-option label="派送中" :value="4" />
            <el-option label="已完成" :value="5" />
            <el-option label="已取消" :value="6" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="fetchData">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <el-row :gutter="20" style="margin-bottom: 20px;">
        <el-col :span="4">
          <el-card shadow="hover">
            <div style="text-align: center;">
              <div style="font-size: 24px; font-weight: bold; color: #409EFF;">{{ statistics.toBeConfirmed || 0 }}</div>
              <div style="color: #666; margin-top: 5px;">待接单</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="4">
          <el-card shadow="hover">
            <div style="text-align: center;">
              <div style="font-size: 24px; font-weight: bold; color: #67C23A;">{{ statistics.confirmed || 0 }}</div>
              <div style="color: #666; margin-top: 5px;">已接单</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="4">
          <el-card shadow="hover">
            <div style="text-align: center;">
              <div style="font-size: 24px; font-weight: bold; color: #E6A23C;">{{ statistics.deliveryInProgress || 0 }}</div>
              <div style="color: #666; margin-top: 5px;">派送中</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="4">
          <el-card shadow="hover">
            <div style="text-align: center;">
              <div style="font-size: 24px; font-weight: bold; color: #909399;">{{ statistics.completed || 0 }}</div>
              <div style="color: #666; margin-top: 5px;">已完成</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="4">
          <el-card shadow="hover">
            <div style="text-align: center;">
              <div style="font-size: 24px; font-weight: bold; color: #F56C6C;">{{ statistics.cancelled || 0 }}</div>
              <div style="color: #666; margin-top: 5px;">已取消</div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <el-table :data="orderList" style="width: 100%">
        <el-table-column prop="number" label="订单号" width="180" />
        <el-table-column prop="userName" label="用户名" width="120" />
        <el-table-column prop="phone" label="联系电话" width="140" />
        <el-table-column prop="address" label="配送地址" show-overflow-tooltip />
        <el-table-column prop="amount" label="金额" width="100">
          <template #default="{ row }">¥{{ row.amount }}</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="orderTime" label="下单时间" width="180" />
        <el-table-column label="操作" width="300" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleDetail(row)">详情</el-button>
            <el-button v-if="row.status === 2" type="success" size="small" @click="handleAccept(row)">接单</el-button>
            <el-button v-if="row.status === 2" type="danger" size="small" @click="handleReject(row)">拒单</el-button>
            <el-button v-if="row.status === 3" type="warning" size="small" @click="handleDelivery(row)">派送</el-button>
            <el-button v-if="row.status === 4" type="info" size="small" @click="handleComplete(row)">完成</el-button>
            <el-button v-if="row.status === 2 || row.status === 3" type="danger" size="small" @click="handleCancel(row)">取消</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="queryForm.page"
        v-model:page-size="queryForm.pageSize"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        style="margin-top: 20px; justify-content: flex-end;"
        @size-change="fetchData"
        @current-change="fetchData"
      />
    </el-card>

    <el-dialog v-model="detailVisible" title="订单详情" width="700px">
      <div v-if="currentOrder">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="订单号">{{ currentOrder.number }}</el-descriptions-item>
          <el-descriptions-item label="状态">{{ getStatusText(currentOrder.status) }}</el-descriptions-item>
          <el-descriptions-item label="用户名">{{ currentOrder.userName }}</el-descriptions-item>
          <el-descriptions-item label="联系电话">{{ currentOrder.phone }}</el-descriptions-item>
          <el-descriptions-item label="金额">¥{{ currentOrder.amount }}</el-descriptions-item>
          <el-descriptions-item label="支付方式">{{ currentOrder.payMethod === 1 ? '微信' : '支付宝' }}</el-descriptions-item>
          <el-descriptions-item label="配送地址" :span="2">{{ currentOrder.address }}</el-descriptions-item>
          <el-descriptions-item label="下单时间" :span="2">{{ currentOrder.orderTime }}</el-descriptions-item>
          <el-descriptions-item label="备注" :span="2">{{ currentOrder.remark || '无' }}</el-descriptions-item>
        </el-descriptions>

        <div v-if="currentOrder.orderDetailList && currentOrder.orderDetailList.length > 0" style="margin-top: 20px;">
          <h4>订单商品</h4>
          <el-table :data="currentOrder.orderDetailList" border style="margin-top: 10px;">
            <el-table-column prop="name" label="商品名称" />
            <el-table-column prop="number" label="数量" width="100" />
            <el-table-column prop="amount" label="金额" width="100">
              <template #default="{ row }">¥{{ row.amount }}</template>
            </el-table-column>
          </el-table>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { orderConditionSearch, getOrderDetails, confirmOrder, completeOrder, deliveryOrder, rejectionOrder, cancelOrder, getOrderStatistics } from '@/api/order'

const queryForm = ref({
  number: '',
  status: '',
  page: 1,
  pageSize: 10
})

const total = ref(0)
const orderList = ref([])
const statistics = ref({})

const detailVisible = ref(false)
const currentOrder = ref(null)

const getStatusType = (status) => {
  const types = { 1: 'info', 2: 'warning', 3: '', 4: 'primary', 5: 'success', 6: 'danger' }
  return types[status] || 'info'
}

const getStatusText = (status) => {
  const texts = { 1: '待付款', 2: '待接单', 3: '已接单', 4: '派送中', 5: '已完成', 6: '已取消' }
  return texts[status] || '未知'
}

const fetchData = async () => {
  try {
    const res = await orderConditionSearch(queryForm.value)
    orderList.value = res.data.records || []
    total.value = res.data.total || 0
  } catch (error) {
    console.error('获取订单列表失败:', error)
  }
}

const fetchStatistics = async () => {
  try {
    const res = await getOrderStatistics()
    statistics.value = res.data || {}
  } catch (error) {
    console.error('获取订单统计失败:', error)
  }
}

const handleReject = async (row) => {
  try {
    const { value } = await ElMessageBox.prompt('请输入拒单原因', '拒单', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputValidator: (value) => {
        if (!value || value.trim() === '') {
          return '请输入拒单原因'
        }
        return true
      }
    })
    await rejectionOrder({ id: row.id, rejectionReason: value })
    ElMessage.success('拒单成功')
    fetchData()
    fetchStatistics()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('拒单失败:', error)
    }
  }
}

const handleCancel = async (row) => {
  try {
    const { value } = await ElMessageBox.prompt('请输入取消原因', '取消订单', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputValidator: (value) => {
        if (!value || value.trim() === '') {
          return '请输入取消原因'
        }
        return true
      }
    })
    await cancelOrder({ id: row.id, cancelReason: value })
    ElMessage.success('取消订单成功')
    fetchData()
    fetchStatistics()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('取消订单失败:', error)
    }
  }
}

const handleReset = () => {
  queryForm.value = { number: '', status: '', page: 1, pageSize: 10 }
  fetchData()
}

const handleDetail = async (row) => {
  try {
    const res = await getOrderDetails(row.id)
    currentOrder.value = res.data
    detailVisible.value = true
  } catch (error) {
    console.error('获取订单详情失败:', error)
  }
}

const handleAccept = async (row) => {
  try {
    await confirmOrder({ id: row.id })
    ElMessage.success('接单成功')
    fetchData()
    fetchStatistics()
  } catch (error) {
    console.error('接单失败:', error)
  }
}

const handleDelivery = async (row) => {
  try {
    await deliveryOrder(row.id)
    ElMessage.success('已派送')
    fetchData()
    fetchStatistics()
  } catch (error) {
    console.error('派送失败:', error)
  }
}

const handleComplete = async (row) => {
  try {
    await completeOrder(row.id)
    ElMessage.success('订单已完成')
    fetchData()
    fetchStatistics()
  } catch (error) {
    console.error('完成订单失败:', error)
  }
}

onMounted(() => {
  fetchData()
  fetchStatistics()
})
</script>

<style scoped lang="scss">
.order-container {
  width: 100%;
}
</style>
