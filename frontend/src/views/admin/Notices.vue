<template>
  <el-card shadow="never">
    <template #header>
      <b>公告管理</b>
      <el-button type="primary" size="small" style="margin-left: 12px" @click="openCreate">发布公告</el-button>
    </template>
    <el-table :data="list" size="small" v-loading="loading">
      <el-table-column prop="title" label="标题" min-width="220" />
      <el-table-column prop="publisher" label="发布人" width="110" />
      <el-table-column prop="publishTime" label="发布时间" width="170">
        <template #default="{ row }">{{ formatTime(row.publishTime) }}</template>
      </el-table-column>
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
            {{ row.status === 1 ? '发布中' : '已下架' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="180">
        <template #default="{ row }">
          <el-button size="small" @click="openEdit(row)">编辑</el-button>
          <el-button size="small" type="danger" plain @click="remove(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-card>

  <el-dialog v-model="dialogVisible" :title="editingId ? '编辑公告' : '发布公告'" width="560px">
    <el-form :model="form" label-width="70px">
      <el-form-item label="标题"><el-input v-model="form.title" /></el-form-item>
      <el-form-item label="内容"><el-input v-model="form.content" type="textarea" :rows="5" /></el-form-item>
      <el-form-item label="状态">
        <el-radio-group v-model="form.status">
          <el-radio :label="1">发布</el-radio>
          <el-radio :label="0">下架</el-radio>
        </el-radio-group>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button type="primary" @click="submit">保存</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { apiNoticePage, apiNoticeCreate, apiNoticeUpdate, apiNoticeDelete } from '../../api'

const loading = ref(false)
const list = ref([])
const dialogVisible = ref(false)
const editingId = ref(null)
const form = reactive({ title: '', content: '', status: 1 })

function formatTime(t) {
  return t ? String(t).replace('T', ' ').slice(0, 19) : '-'
}

async function load() {
  loading.value = true
  try {
    const data = await apiNoticePage({ pageNum: 1, pageSize: 50 })
    list.value = data.records
  } finally {
    loading.value = false
  }
}

function openCreate() {
  editingId.value = null
  Object.assign(form, { title: '', content: '', status: 1 })
  dialogVisible.value = true
}

function openEdit(row) {
  editingId.value = row.id
  Object.assign(form, { title: row.title, content: row.content, status: row.status })
  dialogVisible.value = true
}

async function submit() {
  if (editingId.value) {
    await apiNoticeUpdate(editingId.value, form)
  } else {
    await apiNoticeCreate(form)
  }
  ElMessage.success('保存成功')
  dialogVisible.value = false
  load()
}

async function remove(row) {
  try {
    await ElMessageBox.confirm('确定删除该公告吗？', '删除确认', { type: 'warning' })
  } catch (e) {
    return
  }
  await apiNoticeDelete(row.id)
  ElMessage.success('删除成功')
  load()
}

onMounted(load)
</script>
