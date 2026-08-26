<template>
  <el-card shadow="never">
    <template #header>
      <b>课程管理</b>
      <el-button type="primary" size="small" style="margin-left: 12px" @click="openCreate">新增课程</el-button>
    </template>
    <el-table :data="list" size="small" v-loading="loading">
      <el-table-column prop="courseNo" label="编号" width="100" />
      <el-table-column prop="name" label="课程名称" min-width="150" />
      <el-table-column prop="teacherName" label="教师" width="100" />
      <el-table-column prop="credits" label="学分" width="70" />
      <el-table-column prop="classTime" label="上课时间" min-width="200" />
      <el-table-column prop="location" label="地点" width="120" />
      <el-table-column prop="college" label="院系" width="120" />
      <el-table-column label="类型" width="80">
        <template #default="{ row }">{{ typeLabel(row.courseType) }}</template>
      </el-table-column>
      <el-table-column label="已选/容量" width="100">
        <template #default="{ row }">{{ row.selectedCount }}/{{ row.capacity }}</template>
      </el-table-column>
      <el-table-column label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
            {{ row.status === 1 ? '开放' : '停开' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="220" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="openEdit(row)">编辑</el-button>
          <el-button size="small" :type="row.status === 1 ? 'warning' : 'success'"
                     @click="toggleStatus(row)">{{ row.status === 1 ? '停开' : '开放' }}</el-button>
          <el-button size="small" type="danger" plain @click="remove(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination style="margin-top: 12px" background layout="total, prev, pager, next"
                   :total="total" v-model:current-page="query.pageNum" @current-change="load" />
  </el-card>

  <el-dialog v-model="dialogVisible" :title="editingId ? '编辑课程' : '新增课程'" width="640px">
    <el-form :model="form" label-width="100px">
      <el-row :gutter="12">
        <el-col :span="12"><el-form-item label="课程编号"><el-input v-model="form.courseNo" placeholder="如 CS101" /></el-form-item></el-col>
        <el-col :span="12"><el-form-item label="课程名称"><el-input v-model="form.name" /></el-form-item></el-col>
        <el-col :span="12"><el-form-item label="教师工号"><el-input v-model="form.teacherNo" /></el-form-item></el-col>
        <el-col :span="12"><el-form-item label="教师姓名"><el-input v-model="form.teacherName" /></el-form-item></el-col>
        <el-col :span="12"><el-form-item label="学分"><el-input-number v-model="form.credits" :min="1" :max="6" /></el-form-item></el-col>
        <el-col :span="12"><el-form-item label="容量"><el-input-number v-model="form.capacity" :min="1" /></el-form-item></el-col>
        <el-col :span="12">
          <el-form-item label="课程类型">
            <el-select v-model="form.courseType" style="width: 100%">
              <el-option label="必修" value="REQUIRED" />
              <el-option label="选修" value="ELECTIVE" />
              <el-option label="通识" value="GENERAL" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12"><el-form-item label="所属院系"><el-input v-model="form.college" /></el-form-item></el-col>
        <el-col :span="24"><el-form-item label="上课时间"><el-input v-model="form.classTime" placeholder="如：周一 1-2节, 周三 3-4节" /></el-form-item></el-col>
        <el-col :span="12"><el-form-item label="上课地点"><el-input v-model="form.location" /></el-form-item></el-col>
        <el-col :span="12"><el-form-item label="先修课程"><el-input v-model="form.prerequisite" placeholder="编号逗号分隔" /></el-form-item></el-col>
        <el-col :span="24"><el-form-item label="课程描述"><el-input v-model="form.description" type="textarea" :rows="2" /></el-form-item></el-col>
      </el-row>
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
import { apiCoursePage, apiCourseCreate, apiCourseUpdate, apiCourseDelete, apiCourseStatus } from '../../api'

const loading = ref(false)
const list = ref([])
const total = ref(0)
const query = reactive({ pageNum: 1, pageSize: 10 })
const dialogVisible = ref(false)
const editingId = ref(null)
const form = reactive({})

const typeLabels = { REQUIRED: '必修', ELECTIVE: '选修', GENERAL: '通识' }
const typeLabel = t => typeLabels[t] || t

async function load() {
  loading.value = true
  try {
    const data = await apiCoursePage(query)
    list.value = data.records
    total.value = data.total
  } finally {
    loading.value = false
  }
}

function openCreate() {
  editingId.value = null
  Object.assign(form, {
    courseNo: '', name: '', teacherNo: '', teacherName: '', credits: 2, capacity: 40,
    courseType: 'ELECTIVE', college: '', classTime: '', location: '', prerequisite: '', description: ''
  })
  dialogVisible.value = true
}

function openEdit(row) {
  editingId.value = row.id
  Object.assign(form, {
    courseNo: row.courseNo, name: row.name, teacherNo: row.teacherNo, teacherName: row.teacherName,
    credits: row.credits, capacity: row.capacity, courseType: row.courseType, college: row.college,
    classTime: row.classTime, location: row.location, prerequisite: row.prerequisite, description: row.description
  })
  dialogVisible.value = true
}

async function submit() {
  if (editingId.value) {
    await apiCourseUpdate(editingId.value, form)
  } else {
    await apiCourseCreate(form)
  }
  ElMessage.success('保存成功')
  dialogVisible.value = false
  load()
}

async function toggleStatus(row) {
  await apiCourseStatus(row.id, row.status === 1 ? 0 : 1)
  ElMessage.success('操作成功')
  load()
}

async function remove(row) {
  try {
    await ElMessageBox.confirm('确定删除课程《' + row.name + '》吗？', '删除确认', { type: 'warning' })
  } catch (e) {
    return
  }
  await apiCourseDelete(row.id)
  ElMessage.success('删除成功')
  load()
}

onMounted(load)
</script>
