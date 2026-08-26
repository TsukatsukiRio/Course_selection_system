<template>
  <el-card shadow="never">
    <template #header><b>选课记录</b></template>
    <el-form inline>
      <el-form-item label="学号">
        <el-input v-model="query.studentNo" clearable style="width: 140px" />
      </el-form-item>
      <el-form-item label="课程编号">
        <el-input v-model="query.courseNo" clearable style="width: 140px" />
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="query.status" placeholder="全部" clearable style="width: 120px">
          <el-option label="已选" value="SELECTED" />
          <el-option label="已退" value="DROPPED" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="search">查询</el-button>
      </el-form-item>
    </el-form>
    <el-table :data="list" size="small" v-loading="loading">
      <el-table-column prop="studentNo" label="学号" width="120" />
      <el-table-column prop="courseNo" label="课程编号" width="110" />
      <el-table-column prop="course.name" label="课程名称" />
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
      <el-table-column prop="dropTime" label="退课时间" width="170">
        <template #default="{ row }">{{ formatTime(row.dropTime) }}</template>
      </el-table-column>
    </el-table>
    <el-pagination style="margin-top: 12px" background layout="total, prev, pager, next"
                   :total="total" v-model:current-page="query.pageNum" @current-change="load" />
  </el-card>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { apiSelectionRecords } from '../../api'

const loading = ref(false)
const list = ref([])
const total = ref(0)
const query = reactive({ studentNo: '', courseNo: '', status: '', pageNum: 1, pageSize: 10 })

function formatTime(t) {
  return t ? String(t).replace('T', ' ').slice(0, 19) : '-'
}

async function load() {
  loading.value = true
  try {
    const data = await apiSelectionRecords(query)
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

onMounted(load)
</script>
