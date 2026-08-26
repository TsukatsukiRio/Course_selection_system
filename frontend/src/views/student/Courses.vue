<template>
  <el-card shadow="never">
    <template #header><b>课程检索</b></template>
    <el-form inline>
      <el-form-item label="课程名称">
        <el-input v-model="query.name" placeholder="模糊匹配" clearable style="width: 160px" />
      </el-form-item>
      <el-form-item label="课程编号">
        <el-input v-model="query.courseNo" placeholder="精确匹配" clearable style="width: 130px" />
      </el-form-item>
      <el-form-item label="院系">
        <el-input v-model="query.college" placeholder="如：计算机学院" clearable style="width: 150px" />
      </el-form-item>
      <el-form-item label="类型">
        <el-select v-model="query.courseType" placeholder="全部" clearable style="width: 120px">
          <el-option label="必修" value="REQUIRED" />
          <el-option label="选修" value="ELECTIVE" />
          <el-option label="通识" value="GENERAL" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="search">查询</el-button>
        <el-button @click="reset">重置</el-button>
      </el-form-item>
    </el-form>
    <el-table :data="list" size="small" v-loading="loading">
      <el-table-column prop="courseNo" label="编号" width="100" />
      <el-table-column prop="name" label="课程名称" min-width="150" />
      <el-table-column prop="teacherName" label="教师" width="100" />
      <el-table-column prop="credits" label="学分" width="70" />
      <el-table-column prop="classTime" label="上课时间" min-width="200" />
      <el-table-column prop="location" label="地点" width="120" />
      <el-table-column label="类型" width="80">
        <template #default="{ row }">{{ typeLabel(row.courseType) }}</template>
      </el-table-column>
      <el-table-column label="已选/容量" width="100">
        <template #default="{ row }">
          <el-tag :type="row.selectedCount >= row.capacity ? 'danger' : 'success'" size="small">
            {{ row.selectedCount }}/{{ row.capacity }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="170" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="showDetail(row)">详情</el-button>
          <el-button type="primary" size="small" @click="onSelect(row)">选课</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination style="margin-top: 12px" background layout="total, sizes, prev, pager, next"
                   :total="total" v-model:current-page="query.pageNum" v-model:page-size="query.pageSize"
                   :page-sizes="[10, 20, 50]" @current-change="load" @size-change="load" />
  </el-card>

  <el-dialog v-model="detailVisible" :title="detail && detail.name" width="560px">
    <template v-if="detail">
      <el-descriptions :column="2" border size="small">
        <el-descriptions-item label="课程编号">{{ detail.courseNo }}</el-descriptions-item>
        <el-descriptions-item label="课程类型">{{ typeLabel(detail.courseType) }}</el-descriptions-item>
        <el-descriptions-item label="授课教师">{{ detail.teacherName }}</el-descriptions-item>
        <el-descriptions-item label="学分">{{ detail.credits }}</el-descriptions-item>
        <el-descriptions-item label="上课时间">{{ detail.classTime }}</el-descriptions-item>
        <el-descriptions-item label="上课地点">{{ detail.location }}</el-descriptions-item>
        <el-descriptions-item label="先修课程">{{ detail.prerequisite || '无' }}</el-descriptions-item>
        <el-descriptions-item label="选课人数">{{ detail.selectedCount }}/{{ detail.capacity }}</el-descriptions-item>
      </el-descriptions>
      <p style="margin-top: 10px; color: #666">{{ detail.description }}</p>
      <template v-if="detail.teachingPlan && detail.teachingPlan.auditStatus === 1">
        <el-divider>教学计划（已审核）</el-divider>
        <p><b>教学大纲：</b>{{ detail.teachingPlan.outline || '暂无' }}</p>
        <p><b>教学进度：</b>{{ detail.teachingPlan.schedule || '暂无' }}</p>
        <p><b>参考教材：</b>{{ detail.teachingPlan.textbook || '暂无' }}</p>
      </template>
    </template>
  </el-dialog>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { apiCoursePage, apiCourseDetail, apiConflictCheck, apiSelect, apiOpenStatus } from '../../api'

const loading = ref(false)
const list = ref([])
const total = ref(0)
const detailVisible = ref(false)
const detail = ref(null)
const query = reactive({ name: '', courseNo: '', college: '', courseType: '', pageNum: 1, pageSize: 10 })
const openStatus = ref({ selectOpen: false })

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

function search() {
  query.pageNum = 1
  load()
}

function reset() {
  Object.assign(query, { name: '', courseNo: '', college: '', courseType: '', pageNum: 1 })
  load()
}

async function showDetail(row) {
  detail.value = await apiCourseDetail(row.id)
  detailVisible.value = true
}

async function onSelect(row) {
  if (!openStatus.value.selectOpen) {
    ElMessage.warning('当前不在选课开放时间段')
    return
  }
  const check = await apiConflictCheck(row.courseNo).catch(() => null)
  if (check && !check.ok) {
    ElMessage.error(check.message)
    return
  }
  try {
    await apiSelect({ courseNo: row.courseNo })
    ElMessage.success('选课成功')
    load()
  } catch (e) { /* 错误已在拦截器提示 */ }
}

onMounted(async () => {
  load()
  openStatus.value = await apiOpenStatus().catch(() => ({ selectOpen: false }))
})
</script>
