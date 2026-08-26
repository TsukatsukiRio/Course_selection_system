<template>
  <div>
    <el-row :gutter="16" style="margin-bottom: 16px">
      <el-col :span="6" v-for="card in cards" :key="card.label">
        <el-card shadow="never">
          <div class="stat-label">{{ card.label }}</div>
          <div class="stat-value">{{ card.value }}</div>
        </el-card>
      </el-col>
    </el-row>
    <el-row :gutter="16">
      <el-col :span="14">
        <el-card shadow="never">
          <template #header><b>选课人数趋势</b></template>
          <div ref="trendRef" style="height: 300px"></div>
        </el-card>
      </el-col>
      <el-col :span="10">
        <el-card shadow="never">
          <template #header><b>课程热度排行 Top10</b></template>
          <div ref="topRef" style="height: 300px"></div>
        </el-card>
      </el-col>
    </el-row>
    <el-row :gutter="16" style="margin-top: 16px">
      <el-col :span="12">
        <el-card shadow="never">
          <template #header><b>各院系选课率对比</b></template>
          <div ref="collegeRef" style="height: 300px"></div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="never">
          <template #header><b>课程类型分布</b></template>
          <div ref="typeRef" style="height: 300px"></div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="never">
          <template #header>
            <b>选课时段分布</b>
            <el-button size="small" style="float: right" @click="exportCsv">导出CSV</el-button>
          </template>
          <div ref="hourRef" style="height: 300px"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import * as echarts from 'echarts'
import { apiStatsOverview, apiStatsTrend, apiStatsTop, apiStatsCollege, apiStatsType, apiStatsHour } from '../../api'

const cards = ref([
  { label: '学生总数', value: '-' },
  { label: '课程总数', value: '-' },
  { label: '选课人次', value: '-' },
  { label: '人均选课', value: '-' }
])
const trendRef = ref(null)
const topRef = ref(null)
const collegeRef = ref(null)
const typeRef = ref(null)
const hourRef = ref(null)

const typeNames = { REQUIRED: '必修', ELECTIVE: '选修', GENERAL: '通识' }

async function load() {
  const [overview, trend, top, college, type, hour] = await Promise.all([
    apiStatsOverview({}).catch(() => null),
    apiStatsTrend({}).catch(() => []),
    apiStatsTop(10).catch(() => []),
    apiStatsCollege().catch(() => []),
    apiStatsType().catch(() => []),
    apiStatsHour().catch(() => [])
  ])
  if (overview) {
    cards.value = [
      { label: '学生总数', value: overview.studentCount },
      { label: '课程总数', value: overview.courseCount },
      { label: '选课人次', value: overview.selectionCount },
      { label: '人均选课', value: overview.avgSelectionPerStudent }
    ]
  }
  renderTrend(trend || [])
  renderTop(top || [])
  renderCollege(college || [])
  renderType(type || [])
  renderHour(hour || [])
}

function renderTrend(data) {
  const chart = echarts.init(trendRef.value)
  chart.setOption({
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: data.map(d => d.date) },
    yAxis: { type: 'value' },
    series: [{ type: 'line', smooth: true, areaStyle: {}, data: data.map(d => d.count) }]
  })
}

function renderTop(data) {
  const chart = echarts.init(topRef.value)
  chart.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: 130, right: 20 },
    xAxis: { type: 'value' },
    yAxis: { type: 'category', data: data.map(d => d.name).reverse() },
    series: [{ type: 'bar', data: data.map(d => d.count).reverse(), itemStyle: { color: '#409eff' } }]
  })
}

function renderCollege(data) {
  const chart = echarts.init(collegeRef.value)
  chart.setOption({
    tooltip: { trigger: 'axis' },
    legend: { data: ['学生数', '已选课人数'] },
    xAxis: { type: 'category', data: data.map(d => d.college) },
    yAxis: { type: 'value' },
    series: [
      { name: '学生数', type: 'bar', data: data.map(d => d.studentCount) },
      { name: '已选课人数', type: 'bar', data: data.map(d => d.selectedCount) }
    ]
  })
}

function renderType(data) {
  const chart = echarts.init(typeRef.value)
  chart.setOption({
    tooltip: { trigger: 'item' },
    series: [{
      type: 'pie',
      radius: ['40%', '70%'],
      data: data.map(d => ({ name: typeNames[d.courseType] || d.courseType, value: d.count }))
    }]
  })
}

function renderHour(data) {
  const chart = echarts.init(hourRef.value)
  chart.setOption({
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: data.map(d => d.hour + '时') },
    yAxis: { type: 'value' },
    series: [{ type: 'bar', data: data.map(d => d.count), itemStyle: { color: '#e6a23c' } }]
  })
}

function exportCsv() {
  const token = localStorage.getItem('token')
  fetch('/api/statistics/export/top', {
    headers: { Authorization: 'Bearer ' + token }
  }).then(res => res.blob()).then(blob => {
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = 'course-top.csv'
    a.click()
    URL.revokeObjectURL(url)
  })
}

onMounted(load)
</script>

<style scoped>
.stat-label { color: #999; font-size: 13px; }
.stat-value { font-size: 28px; font-weight: bold; color: #1e3c72; margin-top: 6px; }
</style>
