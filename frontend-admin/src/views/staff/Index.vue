<template>
  <div class="staff-page">
    <div class="page-card">
      <div class="card-header">
        <h3>店员管理</h3>
        <el-button type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon> 新增店员
        </el-button>
      </div>
      <div class="card-body">
        <el-table :data="staffList" border>
          <el-table-column prop="name" label="姓名" />
          <el-table-column prop="phone" label="手机号" />
          <el-table-column prop="role" label="角色" width="100">
            <template #default="{ row }">
              <el-tag :type="row.role === 'manager' ? 'primary' : 'info'">
                {{ row.role === 'manager' ? '店长' : '店员' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createdAt" label="创建时间" width="170">
            <template #default="{ row }">{{ formatTime(row.createdAt) }}</template>
          </el-table-column>
          <el-table-column label="操作" width="200">
            <template #default="{ row }">
              <el-button type="primary" text size="small" @click="handleEdit(row)">编辑</el-button>
              <el-button type="warning" text size="small" @click="handleResetPassword(row)">重置密码</el-button>
              <el-button type="danger" text size="small" @click="handleDelete(row)" :disabled="row.role === 'manager'">
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>
    
    <el-dialog v-model="dialogVisible" :title="editForm.id ? '编辑店员' : '新增店员'" width="450px">
      <el-form :model="editForm" :rules="rules" ref="formRef" label-width="80px">
        <el-form-item label="姓名" prop="name">
          <el-input v-model="editForm.name" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="editForm.phone" placeholder="请输入手机号" :disabled="!!editForm.id" />
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select v-model="editForm.role" placeholder="请选择角色">
            <el-option label="店长" value="manager" />
            <el-option label="店员" value="clerk" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="!editForm.id" label="初始密码" prop="password">
          <el-input v-model="editForm.password" type="password" placeholder="请输入初始密码" show-password />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave" :loading="saving">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import dayjs from 'dayjs'
import { getStaffList, addStaff, updateStaff, resetPassword, deleteStaff } from '@/api'

const staffList = ref([])
const dialogVisible = ref(false)
const saving = ref(false)
const formRef = ref()

const editForm = reactive({
  id: null,
  name: '',
  phone: '',
  role: 'clerk',
  password: ''
})

const rules = {
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  role: [{ required: true, message: '请选择角色', trigger: 'change' }],
  password: [{ required: true, message: '请输入初始密码', trigger: 'blur' }]
}

const formatTime = (time) => dayjs(time).format('YYYY-MM-DD HH:mm')

onMounted(() => fetchStaff())

const fetchStaff = async () => {
  try {
    const res = await getStaffList()
    staffList.value = res.data || []
  } catch (e) {
    console.error(e)
  }
}

const handleAdd = () => {
  editForm.id = null
  editForm.name = ''
  editForm.phone = ''
  editForm.role = 'clerk'
  editForm.password = ''
  dialogVisible.value = true
}

const handleEdit = (row) => {
  editForm.id = row.id
  editForm.name = row.name
  editForm.phone = row.phone
  editForm.role = row.role
  dialogVisible.value = true
}

const handleSave = async () => {
  await formRef.value.validate()
  saving.value = true
  try {
    if (editForm.id) {
      await updateStaff({ id: editForm.id, name: editForm.name, phone: editForm.phone, role: editForm.role })
    } else {
      await addStaff(editForm)
    }
    ElMessage.success('保存成功')
    dialogVisible.value = false
    fetchStaff()
  } catch (e) {
    console.error(e)
  } finally {
    saving.value = false
  }
}

const handleResetPassword = async (row) => {
  const { value: password } = await ElMessageBox.prompt('请输入新密码', '重置密码', {
    inputPattern: /^.{6,}$/,
    inputErrorMessage: '密码至少6位'
  })
  try {
    await resetPassword(row.id, password)
    ElMessage.success('密码重置成功')
  } catch (e) {
    console.error(e)
  }
}

const handleDelete = async (row) => {
  await ElMessageBox.confirm('确定删除该店员吗？', '提示', { type: 'warning' })
  try {
    await deleteStaff(row.id)
    ElMessage.success('删除成功')
    fetchStaff()
  } catch (e) {
    console.error(e)
  }
}
</script>



