<template>
  <div class="category-page">
    <div class="page-card">
      <div class="card-header">
        <h3>分类管理</h3>
        <el-button type="primary" @click="handleAdd(0)">
          <el-icon><Plus /></el-icon> 新增一级分类
        </el-button>
      </div>
      <div class="card-body">
        <el-table :data="categories" row-key="id" border default-expand-all>
          <el-table-column prop="name" label="分类名称" />
          <el-table-column prop="sort" label="排序" width="100" />
          <el-table-column label="操作" width="250">
            <template #default="{ row }">
              <el-button v-if="row.parentId === 0" type="primary" text size="small" @click="handleAdd(row.id)">
                添加子分类
              </el-button>
              <el-button type="primary" text size="small" @click="handleEdit(row)">编辑</el-button>
              <el-button type="danger" text size="small" @click="handleDelete(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>
    
    <el-dialog v-model="dialogVisible" :title="editForm.id ? '编辑分类' : '新增分类'" width="400px">
      <el-form :model="editForm" :rules="rules" ref="formRef" label-width="80px">
        <el-form-item label="分类名称" prop="name">
          <el-input v-model="editForm.name" placeholder="请输入分类名称" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="editForm.sort" :min="0" />
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
import { getCategoryList, addCategory, updateCategory, deleteCategory } from '@/api'

const categories = ref([])
const dialogVisible = ref(false)
const saving = ref(false)
const formRef = ref()

const editForm = reactive({
  id: null,
  parentId: 0,
  name: '',
  sort: 0
})

const rules = {
  name: [{ required: true, message: '请输入分类名称', trigger: 'blur' }]
}

onMounted(() => fetchCategories())

const fetchCategories = async () => {
  try {
    const res = await getCategoryList()
    categories.value = res.data || []
  } catch (e) {
    console.error(e)
  }
}

const handleAdd = (parentId) => {
  editForm.id = null
  editForm.parentId = parentId
  editForm.name = ''
  editForm.sort = 0
  dialogVisible.value = true
}

const handleEdit = (row) => {
  editForm.id = row.id
  editForm.parentId = row.parentId
  editForm.name = row.name
  editForm.sort = row.sort
  dialogVisible.value = true
}

const handleSave = async () => {
  await formRef.value.validate()
  saving.value = true
  try {
    if (editForm.id) {
      await updateCategory(editForm)
    } else {
      await addCategory(editForm)
    }
    ElMessage.success('保存成功')
    dialogVisible.value = false
    fetchCategories()
  } catch (e) {
    console.error(e)
  } finally {
    saving.value = false
  }
}

const handleDelete = async (row) => {
  await ElMessageBox.confirm('确定删除该分类吗？', '提示', { type: 'warning' })
  try {
    await deleteCategory(row.id)
    ElMessage.success('删除成功')
    fetchCategories()
  } catch (e) {
    console.error(e)
  }
}
</script>



