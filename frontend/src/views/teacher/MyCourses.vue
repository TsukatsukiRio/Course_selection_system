<template>
  <el-card shadow="never">
    <template #header><b>我的课程</b></template>
    <el-table :data="list" size="small" v-loading="loading">
      <el-table-column prop="courseNo" label="编号" width="100" />
      <el-table-column prop="name" label="课程名称" min-width="150" />
      <el-table-column prop="credits" label="学分" width="70" />
      <el-table-column prop="classTime" label="上课时间" min-width="200" />
      <el-table-column prop="location" label="地点" width="120" />
      <el-table-column label="已选/容量" width="100">
        <template #default="{ row }">{{ row.selectedCount }}/{{ row.capacity }}</template>
      </el-table-column>
      <el-table-column label="操作" width="260" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="openEdit(row)">编辑信息</el-button>
          <el-button size="small" type="primary" plain @click="openPlan(row)">教学计划</el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-card>

  <el-dialog v-model="editVisible" title="编辑课程信息" width="600px">
    <el-form :model="editForm" label-width="100px">
      <el-form-item label="课程名称"><el-input v-model="editForm.name" /></el-form-item>
      <el-form-item label="上课时间"><el-input v-model="editForm.classTime" /></el-form-item>
      <el-form-item label="上课地点"><el-input v-model="editForm.location" /></el-form-item>
      <el-form-item label="课程描述"><el-input v-model="editForm.description" type="textarea" :rows="2" /></el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="editVisible = false">取消</el-button>
      <el-button type="primary" @click="submitEdit">保存</el-button>
    </template>
  </el-dialog>

  <el-dialog v-model="planVisible" title="教学计划" width="600px">
    <el-alert v-if="plan && plan.auditStatus === 1" type="success" title="已通过审核" show-icon
              :description="plan.auditRemark" style="margin-bottom: 10px" :closable="false" />
    <el-alert v-if="plan && plan.auditStatus === 2" type="error" title="审核被驳回" show-icon
              :description="plan.auditRemark" style="margin-bottom: 10px" :closable="false" />
    <el-alert v-if="plan && plan.auditStatus === 0" type="warning" title="待管理员审核" show-icon
              style="margin-bottom: 10px" :closable="false" />
    <el-form :model="planForm" label-width="90px">
      <el-form-item label="教学大纲"><el-input v-model="planForm.outline" type="textarea" :rows="3" /></el-form-item>
      <el-form-item label="教学进度"><el-input v-model="planForm.schedule" type="textarea" :rows="3" /></el-form-item>
      <el-form-item label="参考教材"><el-input v-model="planForm.textbook" /></el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="planVisible = false">关闭</el-button>
      <el-button type="primary" @click="submitPlan">保存并提交审核</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { apiTeacherCourses, apiTeacherCourseUpdate, apiCourseDetail, apiTeachingPlanSave } from '../../api'

const loading = ref(false)
const list = ref([])
const editVisible = ref(false)
const editForm = reactive({})
const editId = ref(null)
const planVisible = ref(false)
const plan = ref(null)
const planCourseId = ref(null)
const planForm = reactive({ outline: '', schedule: '', textbook: '' })

async function load() {
  loading.value = true
  try {
    list.value = await apiTeacherCourses()
  } finally {
    loading.value = false
  }
}

function openEdit(row) {
  editId.value = row.id
  Object.assign(editForm, {
    courseNo: row.courseNo, name: row.name, teacherNo: row.teacherNo, teacherName: row.teacherName,
    credits: row.credits, capacity: row.capacity, courseType: row.courseType, college: row.college,
    classTime: row.classTime, location: row.location, prerequisite: row.prerequisite, description: row.description
  })
  editVisible.value = true
}

async function submitEdit() {
  await apiTeacherCourseUpdate(editId.value, editForm)
  ElMessage.success('保存成功')
  editVisible.value = false
  load()
}

async function openPlan(row) {
  planCourseId.value = row.id
  const detail = await apiCourseDetail(row.id)
  plan.value = detail.teachingPlan
  Object.assign(planForm, {
    outline: detail.teachingPlan ? detail.teachingPlan.outline : '',
    schedule: detail.teachingPlan ? detail.teachingPlan.schedule : '',
    textbook: detail.teachingPlan ? detail.teachingPlan.textbook : ''
  })
  planVisible.value = true
}

async function submitPlan() {
  await apiTeachingPlanSave({ courseId: planCourseId.value, ...planForm }, false)
  ElMessage.success('教学计划已提交')
  planVisible.value = false
}

onMounted(load)
</script>
