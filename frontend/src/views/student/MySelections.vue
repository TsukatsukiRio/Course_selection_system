<template>
  <el-card shadow="never">
    <template #header>
      <b>我的选课</b>
      <el-switch v-model="showHistory" active-text="含历史记录" style="margin-left: 16px"
                 @change="load" />
    </template>
    <el-table :data="list" size="small" v-loading="loading">
      <el-table-column prop="course.courseNo" label="编号" width="100" />
      <el-table-column prop="course.name" label="课程名称" />
      <el-table-column prop="course.teacherName" label="教师" width="110" />
      <el-table-column prop="course.credits" label="学分" width="70" />
      <el-table-column prop="course.classTime" label="上课时间" min-width="200" />
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="row.status === 'SELECTED' ? 'success' : 'info'" size="small">
            {{ row.status === 'SELECTED' ? '已选' : '已退' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="selectTime" label="选课时间" width="170">
        <template #default="{ row }">{{ formatTime(row.selectTime) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="110">
        <template #default="{ row }">
          <el-button v-if="row.status === 'SELECTED'" type="danger" size="small"
                     @click="onDrop(row)">退课</el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-card>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { apiMySelections, apiDrop } from '../../api'

const list = ref([])
const loading = ref(false)
const showHistory = ref(false)

async function load() {
  loading.value = true
  try {
    list.value = await apiMySelections({ onlyActive: !showHistory.value })
  } finally {
    loading.value = false
  }
}

function formatTime(t) {
  return t ? String(t).replace('T', ' ').slice(0, 19) : ''
}

async function onDrop(row) {
  try {
    await ElMessageBox.confirm('确定退选《' + row.course.name + '》吗？', '退课确认', { type: 'warning' })
  } catch (e) {
    return
  }
  try {
    await apiDrop(row.id)
    ElMessage.success('退课成功')
    load()
  } catch (e) { /* 错误已在拦截器提示 */ }
}

onMounted(load)
</script>
