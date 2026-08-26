<template>
  <el-card shadow="never">
    <template #header>
      <b>用户管理</b>
      <el-button type="primary" size="small" style="margin-left: 12px" @click="openCreate">新增教师/管理员</el-button>
      <el-button size="small" @click="openImport">批量导入学生(CSV)</el-button>
    </template>
    <el-form inline>
      <el-form-item label="关键词">
        <el-input v-model="query.keyword" placeholder="学号/姓名" clearable style="width: 150px" />
      </el-form-item>
      <el-form-item label="角色">
        <el-select v-model="query.role" placeholder="全部" clearable style="width: 130px">
          <el-option label="学生" value="STUDENT" />
          <el-option label="教师" value="TEACHER" />
          <el-option label="管理员" value="ADMIN" />
        </el-select>
      </el-form-item>
      <el-form-item label="院系">
        <el-input v-model="query.college" clearable style="width: 150px" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="search">查询</el-button>
      </el-form-item>
    </el-form>
    <el-table :data="list" size="small" v-loading="loading">
      <el-table-column prop="username" label="学号/工号" width="120" />
      <el-table-column prop="name" label="姓名" width="100" />
      <el-table-column label="角色" width="90">
        <template #default="{ row }">{{ roleLabel(row.role) }}</template>
      </el-table-column>
      <el-table-column prop="college" label="院系" />
      <el-table-column prop="major" label="专业" />
      <el-table-column prop="email" label="邮箱" min-width="180" />
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
            {{ row.status === 1 ? '正常' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="260" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="openEdit(row)">编辑</el-button>
          <el-button size="small" :type="row.status === 1 ? 'warning' : 'success'"
                     @click="toggleStatus(row)">{{ row.status === 1 ? '禁用' : '启用' }}</el-button>
          <el-button size="small" type="danger" plain @click="resetPwd(row)">重置密码</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination style="margin-top: 12px" background layout="total, prev, pager, next"
                   :total="total" v-model:current-page="query.pageNum" v-model:page-size="query.pageSize"
                   @current-change="load" />
  </el-card>

  <el-dialog v-model="createVisible" title="新增账号" width="480px">
    <el-form :model="createForm" label-width="90px">
      <el-form-item label="工号"><el-input v-model="createForm.username" placeholder="6-12位数字" /></el-form-item>
      <el-form-item label="姓名"><el-input v-model="createForm.name" /></el-form-item>
      <el-form-item label="角色">
        <el-select v-model="createForm.role" style="width: 100%">
          <el-option label="教师" value="TEACHER" />
          <el-option label="管理员" value="ADMIN" />
        </el-select>
      </el-form-item>
      <el-form-item label="院系"><el-input v-model="createForm.college" /></el-form-item>
      <el-form-item label="邮箱"><el-input v-model="createForm.email" /></el-form-item>
      <el-form-item label="初始密码"><el-input v-model="createForm.password" placeholder="8-20位含大小写字母数字" /></el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="createVisible = false">取消</el-button>
      <el-button type="primary" @click="submitCreate">确定</el-button>
    </template>
  </el-dialog>

  <el-dialog v-model="editVisible" title="编辑用户" width="480px">
    <el-form :model="editForm" label-width="90px">
      <el-form-item label="姓名"><el-input v-model="editForm.name" /></el-form-item>
      <el-form-item label="院系"><el-input v-model="editForm.college" /></el-form-item>
      <el-form-item label="专业"><el-input v-model="editForm.major" /></el-form-item>
      <el-form-item label="邮箱"><el-input v-model="editForm.email" /></el-form-item>
      <el-form-item label="手机"><el-input v-model="editForm.phone" /></el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="editVisible = false">取消</el-button>
      <el-button type="primary" @click="submitEdit">保存</el-button>
    </template>
  </el-dialog>

  <el-dialog v-model="importVisible" title="批量导入学生" width="560px">
    <p style="color: #999; font-size: 13px; margin-bottom: 8px">
      CSV 格式（首行表头）：学号,姓名,院系,专业,邮箱。默认初始密码 Pass1234。
    </p>
    <el-input v-model="importCsv" type="textarea" :rows="10" placeholder="20210010,张三,计算机学院,软件工程,zhangsan@stu.edu.cn" />
    <template #footer>
      <el-button @click="importVisible = false">取消</el-button>
      <el-button type="primary" @click="submitImport">导入</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { apiUserPage, apiUserCreate, apiUserUpdate, apiUserResetPassword, apiUserImport } from '../../api'

const loading = ref(false)
const list = ref([])
const total = ref(0)
const query = reactive({ keyword: '', role: '', college: '', pageNum: 1, pageSize: 10 })
const createVisible = ref(false)
const editVisible = ref(false)
const importVisible = ref(false)
const importCsv = ref('')
const createForm = reactive({ username: '', name: '', role: 'TEACHER', college: '', email: '', password: 'Pass1234' })
const editForm = reactive({})
const editId = ref(null)

const roleLabel = r => ({ STUDENT: '学生', TEACHER: '教师', ADMIN: '管理员' }[r] || r)

async function load() {
  loading.value = true
  try {
    const data = await apiUserPage(query)
    list.value = data.records
    total.value = data.total
  } finally {
    loading.value = false
  }
}

function search() {
  query.pageNum = 1
  load()
}

function openCreate() {
  Object.assign(createForm, { username: '', name: '', role: 'TEACHER', college: '', email: '', password: 'Pass1234' })
  createVisible.value = true
}

async function submitCreate() {
  await apiUserCreate(createForm)
  ElMessage.success('创建成功')
  createVisible.value = false
  load()
}

function openEdit(row) {
  editId.value = row.id
  Object.assign(editForm, {
    name: row.name, college: row.college, major: row.major,
    email: row.email, phone: row.phone
  })
  editVisible.value = true
}

async function submitEdit() {
  await apiUserUpdate(editId.value, editForm)
  ElMessage.success('保存成功')
  editVisible.value = false
  load()
}

async function toggleStatus(row) {
  await apiUserUpdate(row.id, {
    name: row.name, college: row.college, major: row.major,
    email: row.email, phone: row.phone,
    status: row.status === 1 ? 0 : 1
  })
  ElMessage.success('操作成功')
  load()
}

async function resetPwd(row) {
  const { value } = await ElMessageBox.prompt('请输入新密码（8-20位含大小写字母数字）', '重置密码', {
    inputType: 'password', confirmButtonText: '确定', cancelButtonText: '取消'
  }).catch(() => ({}))
  if (!value) return
  await apiUserResetPassword(row.id, { newPassword: value })
  ElMessage.success('密码已重置')
}

function openImport() {
  importCsv.value = ''
  importVisible.value = true
}

async function submitImport() {
  const data = await apiUserImport(importCsv.value)
  ElMessage.success('成功导入 ' + data.imported + ' 名学生')
  importVisible.value = false
  load()
}

onMounted(load)
</script>
