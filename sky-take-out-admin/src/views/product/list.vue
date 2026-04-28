<template>
  <div class="product-container">
    <el-card>
      <div style="margin-bottom: 20px;">
        <el-button type="primary" @click="handleAdd">新增商品</el-button>
      </div>

      <el-form :inline="true" :model="queryForm" style="margin-bottom: 20px;">
        <el-form-item label="商品名称">
          <el-input v-model="queryForm.name" placeholder="请输入商品名称" />
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="queryForm.categoryId" placeholder="请选择分类" clearable>
            <el-option 
              v-for="item in categoryOptions" 
              :key="item.id" 
              :label="item.name" 
              :value="item.id" 
            />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryForm.status" placeholder="请选择" clearable>
            <el-option label="上架" :value="1" />
            <el-option label="下架" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="fetchData">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="productList" style="width: 100%">
        <el-table-column prop="name" label="商品名称" width="200" />
        <el-table-column prop="categoryName" label="分类" width="120" />
        <el-table-column prop="price" label="价格" width="100">
          <template #default="{ row }">¥{{ row.price }}</template>
        </el-table-column>
        <el-table-column prop="image" label="图片" width="100">
          <template #default="{ row }">
            <el-image 
              v-if="row.image"
              style="width: 50px; height: 50px"
              :src="row.image" 
              fit="cover"
              :preview-src-list="[row.image]"
            />
            <span v-else>无</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-switch
              v-model="row.status"
              :active-value="1"
              :inactive-value="0"
              @change="handleStatusChange(row)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" show-overflow-tooltip />
        <el-table-column prop="updateTime" label="更新时间" width="180" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" size="small" @click="handleDelete(row)">删除</el-button>
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

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑商品' : '新增商品'" width="600px">
      <el-form ref="formRef" :model="productForm" :rules="rules" label-width="100px">
        <el-form-item label="商品名称" prop="name">
          <el-input v-model="productForm.name" />
        </el-form-item>
        <el-form-item label="价格" prop="price">
          <el-input-number v-model="productForm.price" :precision="2" :min="0" />
        </el-form-item>
        <el-form-item label="分类" prop="categoryId">
          <el-select v-model="productForm.categoryId" style="width: 100%;">
            <el-option 
              v-for="item in categoryOptions" 
              :key="item.id" 
              :label="item.name" 
              :value="item.id" 
            />
          </el-select>
        </el-form-item>
        <el-form-item label="图片" prop="image">
          <el-input v-model="productForm.image" placeholder="请输入图片URL" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="productForm.description" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="单位" prop="unit">
          <el-input v-model="productForm.unit" placeholder="如：份、杯、碗" />
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
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { productPageQuery, addProduct, updateProduct, getProductById, deleteProducts, enableOrDisableProduct } from '@/api/product'
import { getCategoryList } from '@/api/category'

const queryForm = ref({
  name: '',
  categoryId: '',
  status: '',
  page: 1,
  pageSize: 10
})

const total = ref(0)
const productList = ref([])
const categoryOptions = ref([])

const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref(null)

const productForm = ref({
  id: undefined,
  name: '',
  categoryId: '',
  price: 0,
  image: '',
  description: '',
  status: 1,
  unit: ''
})

const rules = {
  name: [{ required: true, message: '请输入商品名称', trigger: 'blur' }],
  price: [{ required: true, message: '请输入价格', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }]
}

const fetchCategoryList = async () => {
  try {
    const res = await getCategoryList()
    categoryOptions.value = res.data || []
  } catch (error) {
    console.error('获取分类列表失败:', error)
  }
}

const fetchData = async () => {
  try {
    const res = await productPageQuery(queryForm.value)
    productList.value = res.data.records || []
    total.value = res.data.total || 0
  } catch (error) {
    console.error('获取商品列表失败:', error)
  }
}

const handleReset = () => {
  queryForm.value = { name: '', categoryId: '', status: '', page: 1, pageSize: 10 }
  fetchData()
}

const handleAdd = () => {
  isEdit.value = false
  productForm.value = { id: undefined, name: '', categoryId: '', price: 0, image: '', description: '', status: 1, unit: '' }
  dialogVisible.value = true
}

const handleEdit = async (row) => {
  isEdit.value = true
  try {
    const res = await getProductById(row.id)
    productForm.value = { ...res.data }
    dialogVisible.value = true
  } catch (error) {
    console.error('获取商品详情失败:', error)
  }
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确定要删除该商品吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteProducts([row.id])
      ElMessage.success('删除成功')
      fetchData()
    } catch (error) {
      console.error('删除商品失败:', error)
    }
  }).catch(() => {})
}

const handleStatusChange = async (row) => {
  try {
    await enableOrDisableProduct({ id: row.id, status: row.status })
    ElMessage.success('状态更新成功')
  } catch (error) {
    row.status = row.status === 1 ? 0 : 1
    console.error('更新状态失败:', error)
  }
}

const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  try {
    if (isEdit.value) {
      await updateProduct(productForm.value)
      ElMessage.success('编辑成功')
    } else {
      await addProduct(productForm.value)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    fetchData()
  } catch (error) {
    console.error('提交失败:', error)
  }
}

onMounted(() => {
  fetchCategoryList()
  fetchData()
})
</script>

<style scoped lang="scss">
.product-container {
  width: 100%;
}
</style>
