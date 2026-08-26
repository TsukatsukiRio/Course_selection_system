<template>
  <el-row :gutter="16">
    <el-col :span="24">
      <el-card shadow="never">
        <template #header>
          <b>学生名单与成绩录入</b>
          <el-select v-model="courseNo" placeholder="选择课程" style="width: 260px; margin-left: 12px"
                     @change="loadStudents">
            <el-option v-for="c in courses" :key="c.courseNo" :label="c.courseNo + ' ' + c.name" :value="c.courseNo" />
          </el-select>
        </template>
        <el-table :data="students" size="small" v-loading="loading" style="margin-bottom: 16px">
          <el-table-column prop="studentNo" label="学号" width="130" />
          <el-table-column prop="name" label="姓名" width="110" />
          <el-table-column prop="selectTime" label="选课时间" width="180">
            <template #default="{ row }">{{ formatTime(row.selectTime) }}</template>
          </el-table-column>
          <el-table-column label="成绩">
            <template #default="{ row }">
              <el-tag v-if="gradeMap[row.studentNo] != null" type="success" size="small">
                {{ gradeMap[row.studentNo] }}
              </el-tag>
              <el-tag v-else type="info" size="small">未录入</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="150">
            <template #default="{ row }">
              <el-button size="small" type="primary" @click="openGrade(row)">录入成绩</el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-empty v-if="!students.length" description="请先选择课程" />
      </el-card>
    </el-col>
  </el-row>

  <el-dialog v-model="gradeVisible" title="录入成绩" width="380px">
    <el-form label-width="90px">
      <el-form-item label="学生">
        <el-input :model-value="currentStudent && (currentStudent.studentNo + ' ' + currentStudent.name)" disabled />
      </el-form-item>
      <el-form-item label="成绩">
        <el-input-number v-model="score" :min="0" :max="100" :precision="1" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="gradeVisible = false">取消</el-button>
      <el-button type="primary" @click="submitGrade">保存</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { apiTeacherCourses, apiCourseStudents, apiGrades, apiGradeSave } from '../../api'

const courses = ref([])
const courseNo = ref('')
const students = ref([])
const gradeMap = ref({})
const loading = ref(false)
const gradeVisible = ref(false)
const currentStudent = ref(null)
const score = ref(80)

function formatTime(t) {
  return t ? String(t).replace('T', ' ').slice(0, 19) : '-'
}

async function loadStudents() {
  if (!courseNo.value) return
  loading.value = true
  try {
    const [studentList, gradeList] = await Promise.all([
      apiCourseStudents(courseNo.value),
      apiGrades({ courseNo: courseNo.value })
    ])
    students.value = studentList || []
    const map = {}
    ;(gradeList || []).forEach(g => { map[g.studentNo] = g.score })
    gradeMap.value = map
  } finally {
    loading.value = false
  }
}

function openGrade(row) {
  currentStudent.value = row
  score.value = gradeMap.value[row.studentNo] != null ? Number(gradeMap.value[row.studentNo]) : 80
  gradeVisible.value = true
}

async function submitGrade() {
  await apiGradeSave({ studentNo: currentStudent.value.studentNo, courseNo: courseNo.value, score: score.value })
  ElMessage.success('成绩已保存')
  gradeVisible.value = false
  loadStudents()
}

onMounted(async () => {
  courses.value = await apiTeacherCourses()
})
</script>
