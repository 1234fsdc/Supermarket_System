<template>
  <div class="apple-seckill">
    <div class="toolbar">
      <el-button type="primary" @click="handleAddActivity">+ 新增活动</el-button>
      <div class="toolbar-spacer" />
      <el-input v-model="searchForm.name" placeholder="搜索活动名称" clearable style="width: 200px;" @clear="handleSearch" @keyup.enter="handleSearch" />
      <el-select v-model="searchForm.status" placeholder="全部状态" clearable style="width: 140px;" @change="handleSearch">
        <el-option label="未开始" :value="0" />
        <el-option label="进行中" :value="1" />
        <el-option label="已结束" :value="2" />
        <el-option label="已停用" :value="3" />
      </el-select>
      <el-button type="primary" @click="handleSearch">查询</el-button>
      <el-button @click="handleReset">重置</el-button>
    </div>

    <div class="table-card">
      <el-table :data="tableData" v-loading="loading" row-key="id" style="width: 100%">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="name" label="活动名称" min-width="160" />
        <el-table-column label="活动时间" width="320">
          <template #default="{ row }">{{ formatDate(row.startTime) }} ~ {{ formatDate(row.endTime) }}</template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusTag(row.status)" size="small">{{ row.statusName }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="商品数" width="80" align="center">
          <template #default="{ row }">{{ row.productList?.length || 0 }}</template>
        </el-table-column>
        <el-table-column label="操作" width="240" fixed="right" align="center">
          <template #default="{ row }">
            <div class="action-cell">
              <el-button text size="small" type="primary" @click="handleManageProducts(row)">商品管理</el-button>
              <el-button text size="small" type="primary" @click="handleEditActivity(row)">编辑</el-button>
              <el-button text size="small" type="danger" @click="handleDeleteActivity(row)">删除</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="searchForm.page"
        v-model:page-size="searchForm.pageSize"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        @size-change="loadData"
        @current-change="loadData"
      />
    </div>

    <el-dialog v-model="activityDialogVisible" :title="activityDialogTitle" width="520px" destroy-on-close>
      <el-form ref="activityFormRef" :model="activityForm" :rules="activityRules" label-width="100px">
        <el-form-item label="活动名称" prop="name">
          <el-input v-model="activityForm.name" placeholder="请输入活动名称" />
        </el-form-item>
        <el-form-item label="开始时间" prop="startTime">
          <el-date-picker v-model="activityForm.startTime" type="datetime" placeholder="选择开始时间" value-format="YYYY-MM-DD HH:mm:ss" style="width: 100%" />
        </el-form-item>
        <el-form-item label="结束时间" prop="endTime">
          <el-date-picker v-model="activityForm.endTime" type="datetime" placeholder="选择结束时间" value-format="YYYY-MM-DD HH:mm:ss" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="activityDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitActivity" :loading="activitySubmitting">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="productDialogVisible" :title="`商品管理 - ${currentActivity?.name || ''}`" width="800px" destroy-on-close>
      <div class="product-toolbar">
        <el-button type="primary" size="small" @click="handleAddProduct">+ 添加商品</el-button>
        <span class="product-count">已关联 {{ productList.length }} 件商品</span>
      </div>
      <el-table :data="productList" v-loading="productLoading" style="width: 100%">
        <el-table-column label="图片" width="60">
          <template #default="{ row }">
            <el-avatar :src="row.productImage" :size="36" shape="square" />
          </template>
        </el-table-column>
        <el-table-column prop="productName" label="商品名称" min-width="140" />
        <el-table-column label="原价" width="90">
          <template #default="{ row }"><span class="original-price">¥{{ row.originalPrice }}</span></template>
        </el-table-column>
        <el-table-column label="秒杀价" width="100">
          <template #default="{ row }"><span class="seckill-price">¥{{ row.seckillPrice }}</span></template>
        </el-table-column>
        <el-table-column label="折扣" width="70">
          <template #default="{ row }">{{ row.discount != null ? (row.discount * 10).toFixed(1) + '折' : '-' }}</template>
        </el-table-column>
        <el-table-column prop="seckillStock" label="库存" width="80" align="center" />
        <el-table-column prop="soldCount" label="已售" width="60" align="center" />
        <el-table-column prop="limitPerUser" label="限购" width="60" align="center" />
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button text size="small" type="primary" @click="handleEditProduct(row)">编辑</el-button>
            <el-button text size="small" type="danger" @click="handleDeleteProduct(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-dialog v-model="productFormDialogVisible" :title="isEditProduct ? '编辑秒杀商品' : '添加秒杀商品'" width="480px" destroy-on-close append-to-body>
        <el-form ref="productFormRef" :model="productForm" :rules="productRules" label-width="100px">
          <el-form-item label="商品" prop="productId">
            <el-select v-model="productForm.productId" placeholder="搜索选择商品" filterable remote :remote-method="searchProducts" :loading="productSearchLoading" style="width: 100%">
              <el-option v-for="p in productOptions" :key="p.id" :label="`${p.name} (¥${p.price})`" :value="p.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="秒杀价格" prop="seckillPrice">
            <el-input-number v-model="productForm.seckillPrice" :min="0.01" :precision="2" :step="0.5" style="width: 200px" />
            <span class="form-tip">元</span>
          </el-form-item>
          <el-form-item label="秒杀库存" prop="seckillStock">
            <el-input-number v-model="productForm.seckillStock" :min="1" :step="10" style="width: 200px" />
            <span class="form-tip">件</span>
          </el-form-item>
          <el-form-item label="每人限购" prop="limitPerUser">
            <el-input-number v-model="productForm.limitPerUser" :min="1" :step="1" style="width: 200px" />
            <span class="form-tip">件</span>
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="productFormDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmitProduct" :loading="productSubmitting">确定</el-button>
        </template>
      </el-dialog>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getSeckillPage, createSeckillActivity, updateSeckillActivity, deleteSeckillActivity,
  addSeckillProduct, updateSeckillProduct, deleteSeckillProduct, getSeckillProductsByActivity
} from '@/api/seckill'
import { productPageQuery } from '@/api/product'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)

const searchForm = reactive({ page: 1, pageSize: 10, name: '', status: null })

const activityDialogVisible = ref(false)
const activityDialogTitle = ref('新增活动')
const activitySubmitting = ref(false)
const isEditActivity = ref(false)
const activityFormRef = ref(null)
const activityForm = reactive({ id: null, name: '', startTime: '', endTime: '' })
const activityRules = {
  name: [{ required: true, message: '请输入活动名称', trigger: 'blur' }],
  startTime: [{ required: true, message: '请选择开始时间', trigger: 'change' }],
  endTime: [{ required: true, message: '请选择结束时间', trigger: 'change' }]
}

const productDialogVisible = ref(false)
const productLoading = ref(false)
const currentActivity = ref(null)
const productList = ref([])
const productFormDialogVisible = ref(false)
const isEditProduct = ref(false)
const productSubmitting = ref(false)
const productFormRef = ref(null)
const productForm = reactive({ id: null, activityId: null, productId: null, seckillPrice: 0.01, seckillStock: 100, limitPerUser: 2 })
const productRules = {
  productId: [{ required: true, message: '请选择商品', trigger: 'change' }],
  seckillPrice: [{ required: true, message: '请输入秒杀价格', trigger: 'blur' }],
  seckillStock: [{ required: true, message: '请输入秒杀库存', trigger: 'blur' }],
  limitPerUser: [{ required: true, message: '请输入每人限购数量', trigger: 'blur' }]
}
const productOptions = ref([])
const productSearchLoading = ref(false)

function formatDate(date) {
  if (!date) return '-'
  return new Date(date).toLocaleDateString('zh-CN')
}

function statusTag(status) {
  const map = { 0: 'info', 1: 'success', 2: 'warning', 3: 'danger' }
  return map[status] || 'info'
}

async function loadData() {
  loading.value = true
  try {
    const res = await getSeckillPage({
      page: searchForm.page, pageSize: searchForm.pageSize,
      name: searchForm.name || undefined,
      status: searchForm.status !== null && searchForm.status !== '' ? searchForm.status : undefined
    })
    tableData.value = res.data.records || []
    total.value = res.data.total || 0
  } catch (err) { console.error('加载秒杀活动失败:', err) }
  finally { loading.value = false }
}

function handleSearch() { searchForm.page = 1; loadData() }
function handleReset() { searchForm.name = ''; searchForm.status = null; searchForm.page = 1; loadData() }

function resetActivityForm() { activityForm.id = null; activityForm.name = ''; activityForm.startTime = ''; activityForm.endTime = '' }

function handleAddActivity() { isEditActivity.value = false; activityDialogTitle.value = '新增活动'; resetActivityForm(); activityDialogVisible.value = true }
function handleEditActivity(row) { isEditActivity.value = true; activityDialogTitle.value = '编辑活动'; activityForm.id = row.id; activityForm.name = row.name; activityForm.startTime = row.startTime; activityForm.endTime = row.endTime; activityDialogVisible.value = true }

async function handleSubmitActivity() {
  const valid = await activityFormRef.value.validate().catch(() => false)
  if (!valid) return
  activitySubmitting.value = true
  try {
    if (isEditActivity.value) {
      await updateSeckillActivity({ id: activityForm.id, name: activityForm.name, startTime: activityForm.startTime, endTime: activityForm.endTime })
      ElMessage.success('更新成功')
    } else {
      await createSeckillActivity({ name: activityForm.name, startTime: activityForm.startTime, endTime: activityForm.endTime })
      ElMessage.success('创建成功')
    }
    activityDialogVisible.value = false; loadData()
  } catch (err) { console.error('提交活动失败:', err) }
  finally { activitySubmitting.value = false }
}

function handleDeleteActivity(row) {
  ElMessageBox.confirm(`确认删除活动「${row.name}」吗？`, '提示', { confirmButtonText: '确认', cancelButtonText: '取消', type: 'warning' })
    .then(async () => { await deleteSeckillActivity(row.id); ElMessage.success('删除成功'); loadData() }).catch(() => {})
}

async function handleManageProducts(row) { currentActivity.value = row; productDialogVisible.value = true; await loadProducts(row.id) }
async function loadProducts(activityId) { productLoading.value = true; try { const res = await getSeckillProductsByActivity(activityId); productList.value = res.data || [] } catch (err) { console.error('加载秒杀商品失败:', err) } finally { productLoading.value = false } }

function resetProductForm() { productForm.id = null; productForm.activityId = currentActivity.value?.id; productForm.productId = null; productForm.seckillPrice = 0.01; productForm.seckillStock = 100; productForm.limitPerUser = 2; productOptions.value = [] }
function handleAddProduct() { isEditProduct.value = false; resetProductForm(); productFormDialogVisible.value = true }
function handleEditProduct(row) { isEditProduct.value = true; productForm.id = row.id; productForm.activityId = row.activityId; productForm.productId = row.productId; productForm.seckillPrice = row.seckillPrice; productForm.seckillStock = row.seckillStock; productForm.limitPerUser = row.limitPerUser; productOptions.value = [{ id: row.productId, name: row.productName, price: row.originalPrice }]; productFormDialogVisible.value = true }

async function handleSubmitProduct() {
  const valid = await productFormRef.value.validate().catch(() => false)
  if (!valid) return
  productSubmitting.value = true
  try {
    if (isEditProduct.value) {
      await updateSeckillProduct({ id: productForm.id, activityId: productForm.activityId, productId: productForm.productId, seckillPrice: productForm.seckillPrice, seckillStock: productForm.seckillStock, limitPerUser: productForm.limitPerUser || 1 })
      ElMessage.success('更新成功')
    } else {
      await addSeckillProduct({ activityId: productForm.activityId, productId: productForm.productId, seckillPrice: productForm.seckillPrice, seckillStock: productForm.seckillStock, limitPerUser: productForm.limitPerUser || 1 })
      ElMessage.success('添加成功')
    }
    productFormDialogVisible.value = false; await loadProducts(currentActivity.value.id)
  } catch (err) { console.error('提交商品失败:', err) }
  finally { productSubmitting.value = false }
}

function handleDeleteProduct(row) {
  ElMessageBox.confirm(`确认从活动中移除商品「${row.productName}」吗？`, '提示', { confirmButtonText: '确认', cancelButtonText: '取消', type: 'warning' })
    .then(async () => { await deleteSeckillProduct(row.id); ElMessage.success('删除成功'); await loadProducts(currentActivity.value.id) }).catch(() => {})
}

async function searchProducts(query) {
  if (!query) return
  productSearchLoading.value = true
  try {
    const res = await productPageQuery({ name: query, page: 1, pageSize: 20 })
    productOptions.value = (res.data.records || []).map(p => ({ id: p.id, name: p.name, price: p.price || p.originalPrice }))
  } catch (err) { console.error('搜索商品失败:', err) }
  finally { productSearchLoading.value = false }
}

onMounted(() => { loadData() })
</script>

<style scoped lang="scss">
.apple-seckill {
  max-width: 1400px;
}

.toolbar {
  display: flex;
  gap: var(--space-sm);
  align-items: center;
  margin-bottom: var(--space-lg);
  background: var(--bg-card);
  border-radius: var(--radius-lg);
  padding: var(--space-md) var(--space-lg);
  box-shadow: var(--shadow-sm);
}

.toolbar-spacer { flex: 1; }

.table-card {
  background: var(--bg-card);
  border-radius: var(--radius-lg);
  padding: var(--space-md) 0;
  box-shadow: var(--shadow-sm);
}

.original-price { text-decoration: line-through; color: var(--text-tertiary); }
.seckill-price { color: var(--apple-red); font-weight: 600; }

.product-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--space-md);
}

.product-count {
  font-size: 13px;
  color: var(--text-secondary);
}

.form-tip {
  margin-left: 8px;
  color: var(--text-tertiary);
  font-size: 12px;
}

.action-cell {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
}
</style>
