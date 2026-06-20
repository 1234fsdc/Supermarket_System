<template>
  <div class="coupon-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>优惠券管理</span>
          <el-button type="primary" @click="handleAdd">+ 新增优惠券</el-button>
        </div>
      </template>

      <!-- 搜索栏 -->
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="优惠券名称">
          <el-input v-model="searchForm.name" placeholder="请输入名称" clearable />
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="searchForm.type" placeholder="全部类型" clearable>
            <el-option label="满减券" :value="1" />
            <el-option label="折扣券" :value="2" />
            <el-option label="新人券" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部状态" clearable>
            <el-option label="启用" :value="1" />
            <el-option label="停用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 数据表格 -->
      <el-table :data="tableData" v-loading="loading" style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="优惠券名称" width="180" />
        <el-table-column label="类型" width="100">
          <template #default="{ row }">
            <el-tag :type="getTypeTag(row.type)">{{ row.typeName }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="优惠内容" width="150">
          <template #default="{ row }">
            <span v-if="row.discountType === 1">满{{ row.minSpend }}减{{ row.discountValue }}</span>
            <span v-else>{{ (row.discountValue * 10).toFixed(1) }}折</span>
          </template>
        </el-table-column>
        <el-table-column label="发放进度" width="150">
          <template #default="{ row }">
            <el-progress
              :percentage="getProgress(row)"
              :status="getProgressStatus(row)"
            />
            <span class="progress-text">{{ row.totalCount - row.remainCount }}/{{ row.totalCount }}</span>
          </template>
        </el-table-column>
        <el-table-column label="有效期" width="200">
          <template #default="{ row }">
            <div v-if="row.validDays">领取后{{ row.validDays }}天有效</div>
            <div v-else>
              <div>{{ formatDate(row.startTime) }}</div>
              <div>至 {{ formatDate(row.endTime) }}</div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-switch
              v-model="row.status"
              :active-value="1"
              :inactive-value="0"
              @change="(val) => handleStatusChange(row, val)"
            />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <el-pagination
        v-model:current-page="searchForm.page"
        v-model:page-size="searchForm.pageSize"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        :total="total"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        class="pagination"
      />
    </el-card>

    <!-- 新增/编辑弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      destroy-on-close
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="120px"
      >
        <el-form-item label="优惠券名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入优惠券名称" />
        </el-form-item>
        <el-form-item label="优惠券类型" prop="type">
          <el-radio-group v-model="form.type">
            <el-radio :label="1">满减券</el-radio>
            <el-radio :label="2">折扣券</el-radio>
            <el-radio :label="3">新人券</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="优惠方式" prop="discountType">
          <el-radio-group v-model="form.discountType">
            <el-radio :label="1">固定金额</el-radio>
            <el-radio :label="2">百分比折扣</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="优惠值" prop="discountValue">
          <el-input-number v-model="form.discountValue" :min="0.01" :precision="2" />
          <span class="form-tip">{{ form.discountType === 1 ? '元（减免金额）' : '（0.85表示85折）' }}</span>
        </el-form-item>
        <el-form-item label="最低消费" prop="minSpend">
          <el-input-number v-model="form.minSpend" :min="0" :precision="2" />
          <span class="form-tip">元</span>
        </el-form-item>
        <el-form-item label="最大优惠" v-if="form.discountType === 2">
          <el-input-number v-model="form.maxDiscount" :min="0" :precision="2" />
          <span class="form-tip">元（折扣券用，可选）</span>
        </el-form-item>
        <el-form-item label="发放总量" prop="totalCount">
          <el-input-number v-model="form.totalCount" :min="0" />
          <span class="form-tip">0表示不限量</span>
        </el-form-item>
        <el-form-item label="每人限领" prop="limitPerUser">
          <el-input-number v-model="form.limitPerUser" :min="1" />
          <span class="form-tip">张</span>
        </el-form-item>
        <el-form-item label="有效期类型">
          <el-radio-group v-model="validType">
            <el-radio label="days">领取后有效天数</el-radio>
            <el-radio label="range">固定时间范围</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="有效天数" v-if="validType === 'days'">
          <el-input-number v-model="form.validDays" :min="1" />
          <span class="form-tip">天</span>
        </el-form-item>
        <el-form-item label="有效期" v-if="validType === 'range'">
          <el-date-picker
            v-model="dateRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            value-format="YYYY-MM-DD HH:mm:ss"
          />
        </el-form-item>
        <el-form-item label="适用范围" prop="scopeType">
          <el-radio-group v-model="form.scopeType">
            <el-radio :label="1">全场通用</el-radio>
            <el-radio :label="2">指定分类</el-radio>
            <el-radio :label="3">指定商品</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getCouponTemplatePage,
  createCouponTemplate,
  updateCouponTemplate,
  updateCouponTemplateStatus,
  deleteCouponTemplate
} from '@/api/coupon'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const dialogTitle = ref('新增优惠券')
const formRef = ref(null)
const validType = ref('days')
const dateRange = ref([])

const searchForm = reactive({
  page: 1,
  pageSize: 10,
  name: '',
  type: null,
  status: null
})

const form = reactive({
  id: null,
  name: '',
  type: 1,
  discountType: 1,
  discountValue: 1,
  minSpend: 0,
  maxDiscount: null,
  totalCount: 100,
  limitPerUser: 1,
  validDays: 7,
  startTime: null,
  endTime: null,
  scopeType: 1,
  scopeIds: null
})

const rules = {
  name: [{ required: true, message: '请输入优惠券名称', trigger: 'blur' }],
  type: [{ required: true, message: '请选择优惠券类型', trigger: 'change' }],
  discountType: [{ required: true, message: '请选择优惠方式', trigger: 'change' }],
  discountValue: [{ required: true, message: '请输入优惠值', trigger: 'blur' }],
  minSpend: [{ required: true, message: '请输入最低消费金额', trigger: 'blur' }],
  totalCount: [{ required: true, message: '请输入发放总量', trigger: 'blur' }],
  limitPerUser: [{ required: true, message: '请输入每人限领数量', trigger: 'blur' }],
  scopeType: [{ required: true, message: '请选择适用范围', trigger: 'change' }]
}

const getTypeTag = (type) => {
  const map = { 1: 'success', 2: 'warning', 3: 'danger' }
  return map[type] || 'info'
}

const getProgress = (row) => {
  if (row.totalCount === 0) return 0
  return Math.round(((row.totalCount - row.remainCount) / row.totalCount) * 100)
}

const getProgressStatus = (row) => {
  if (row.remainCount === 0) return 'exception'
  if (row.remainCount / row.totalCount < 0.1) return 'warning'
  return ''
}

const formatDate = (date) => {
  if (!date) return '-'
  return new Date(date).toLocaleString()
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await getCouponTemplatePage(searchForm)
    tableData.value = res.data.records
    total.value = res.data.total
  } catch (error) {
    console.error('加载数据失败:', error)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  searchForm.page = 1
  loadData()
}

const handleReset = () => {
  searchForm.name = ''
  searchForm.type = null
  searchForm.status = null
  searchForm.page = 1
  loadData()
}

const handleSizeChange = (val) => {
  searchForm.pageSize = val
  loadData()
}

const handleCurrentChange = (val) => {
  searchForm.page = val
  loadData()
}

const resetForm = () => {
  form.id = null
  form.name = ''
  form.type = 1
  form.discountType = 1
  form.discountValue = 1
  form.minSpend = 0
  form.maxDiscount = null
  form.totalCount = 100
  form.limitPerUser = 1
  form.validDays = 7
  form.startTime = null
  form.endTime = null
  form.scopeType = 1
  form.scopeIds = null
  validType.value = 'days'
  dateRange.value = []
}

const handleAdd = () => {
  resetForm()
  dialogTitle.value = '新增优惠券'
  dialogVisible.value = true
}

const handleEdit = (row) => {
  resetForm()
  dialogTitle.value = '编辑优惠券'
  Object.assign(form, row)
  if (row.validDays) {
    validType.value = 'days'
  } else {
    validType.value = 'range'
    dateRange.value = [row.startTime, row.endTime]
  }
  dialogVisible.value = true
}

const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  // 处理时间
  if (validType.value === 'range' && dateRange.value.length === 2) {
    form.startTime = dateRange.value[0]
    form.endTime = dateRange.value[1]
    form.validDays = null
  } else {
    form.startTime = null
    form.endTime = null
  }

  try {
    if (form.id) {
      await updateCouponTemplate(form)
      ElMessage.success('更新成功')
    } else {
      await createCouponTemplate(form)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    loadData()
  } catch (error) {
    console.error('提交失败:', error)
  }
}

const handleStatusChange = async (row, status) => {
  try {
    await updateCouponTemplateStatus(row.id, status)
    ElMessage.success('状态更新成功')
  } catch (error) {
    row.status = status === 1 ? 0 : 1
    console.error('状态更新失败:', error)
  }
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确认删除该优惠券模板吗？', '提示', {
    confirmButtonText: '确认',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    await deleteCouponTemplate(row.id)
    ElMessage.success('删除成功')
    loadData()
  }).catch(() => {})
}

onMounted(() => {
  loadData()
})
</script>

<style scoped lang="scss">
.coupon-container {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.search-form {
  margin-bottom: 20px;
}

.pagination {
  margin-top: 20px;
  justify-content: flex-end;
}

.progress-text {
  font-size: 12px;
  color: #909399;
}

.form-tip {
  margin-left: 8px;
  color: #909399;
  font-size: 12px;
}
</style>
