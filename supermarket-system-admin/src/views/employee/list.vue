<template>
  <div class="employee-container">
    <el-card>
      <div style="margin-bottom: 20px;">
        <el-button type="primary" @click="handleAdd">新增员工</el-button>
      </div>

      <el-form :inline="true" :model="queryForm" style="margin-bottom: 20px;">
        <el-form-item label="员工姓名">
          <el-input v-model="queryForm.name" placeholder="请输入员工姓名" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="fetchData">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="employeeList" style="width: 100%">
        <el-table-column prop="name" label="员工姓名" width="120" />
        <el-table-column prop="username" label="账号" width="140" />
        <el-table-column prop="phone" label="手机号" width="140" />
        <el-table-column prop="idNumber" label="身份证号" width="200" />
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
        <el-table-column prop="createTime" label="创建时间" width="180" />
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

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑员工' : '新增员工'" width="500px">
      <el-form ref="formRef" :model="employeeForm" :rules="rules" label-width="100px">
        <el-form-item label="员工姓名" prop="name">
          <el-input v-model="employeeForm.name" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="employeeForm.phone" />
        </el-form-item>
        <el-form-item label="性别" prop="sex">
          <el-radio-group v-model="employeeForm.sex">
            <el-radio label="男">男</el-radio>
            <el-radio label="女">女</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="身份证号" prop="idNumber">
          <el-input v-model="employeeForm.idNumber" />
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
import { employeePageQuery, addEmployee, updateEmployee, getEmployeeById, enableOrDisableEmployee, deleteEmployee } from '@/api/employee'

const queryForm = ref({
  name: '',
  page: 1,
  pageSize: 10
})

const total = ref(0)
const employeeList = ref([])

const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref(null)

const employeeForm = ref({
  id: undefined,
  name: '',
  phone: '',
  sex: '男',
  idNumber: '',
  status: 1
})

const rules = {
  name: [{ required: true, message: '请输入员工姓名', trigger: 'blur' }],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  idNumber: [
    { required: true, message: '请输入身份证号', trigger: 'blur' },
    { pattern: /^\d{17}[\dXx]$/, message: '身份证号格式不正确', trigger: 'blur' }
  ]
}

const fetchData = async () => {
  try {
    const res = await employeePageQuery(queryForm.value)
    employeeList.value = res.data.records || []
    total.value = res.data.total || 0
  } catch (error) {
    console.error('获取员工列表失败:', error)
  }
}

const handleReset = () => {
  queryForm.value = { name: '', page: 1, pageSize: 10 }
  fetchData()
}

const handleAdd = () => {
  isEdit.value = false
  employeeForm.value = { id: undefined, name: '', phone: '', sex: '男', idNumber: '', status: 1 }
  dialogVisible.value = true
}

const handleEdit = async (row) => {
  isEdit.value = true
  try {
    const res = await getEmployeeById(row.id)
    employeeForm.value = { ...res.data }
    dialogVisible.value = true
  } catch (error) {
    console.error('获取员工详情失败:', error)
  }
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确定要删除该员工吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteEmployee(row.id)
      ElMessage.success('删除成功')
      fetchData()
    } catch (error) {
      console.error('删除员工失败:', error)
    }
  }).catch(() => {})
}

const handleStatusChange = async (row) => {
  try {
    await enableOrDisableEmployee({ id: row.id, status: row.status })
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
      await updateEmployee(employeeForm.value)
      ElMessage.success('编辑成功')
    } else {
      employeeForm.value.username = employeeForm.value.phone
      employeeForm.value.password = '123456'
      await addEmployee(employeeForm.value)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    fetchData()
  } catch (error) {
    console.error('提交失败:', error)
  }
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped lang="scss">
.employee-container {
  width: 100%;
}
</style>
