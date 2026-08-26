<template>
  <el-card shadow="never">
    <template #header><b>我的课表（周视图）</b></template>
    <div class="grid-wrap">
      <table class="schedule-table">
        <thead>
          <tr>
            <th>节次</th>
            <th v-for="d in days" :key="d">{{ d }}</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="p in 12" :key="p">
            <td class="period">{{ p }}</td>
            <td v-for="d in 7" :key="d" class="cell">
              <template v-for="item in cellItems(d, p)" :key="item.recordId">
                <div class="lesson" :class="'color-' + (hash(item.courseNo) % 5)">
                  <div class="lesson-name">{{ item.name }}</div>
                  <div class="lesson-loc">{{ item.location }}</div>
                </div>
              </template>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </el-card>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { apiSchedule } from '../../api'

const days = ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
const items = ref([])

function cellItems(day, period) {
  return items.value.filter(item =>
    (item.segments || []).some(s => s.day === day && s.startPeriod <= period && period <= s.endPeriod)
  )
}

function hash(str) {
  let h = 0
  for (let i = 0; i < str.length; i++) h = (h * 31 + str.charCodeAt(i)) % 997
  return h
}

onMounted(async () => {
  items.value = await apiSchedule({})
})
</script>

<style scoped>
.grid-wrap { overflow-x: auto; }
.schedule-table { border-collapse: collapse; width: 100%; min-width: 900px; }
.schedule-table th, .schedule-table td { border: 1px solid #e5e7eb; text-align: center; }
.schedule-table th { background: #1e3c72; color: #fff; padding: 8px 0; font-size: 14px; }
.period { background: #f8fafc; width: 60px; color: #666; }
.cell { height: 62px; vertical-align: top; padding: 3px; }
.lesson { border-radius: 4px; padding: 4px; font-size: 12px; color: #fff; }
.lesson-name { font-weight: bold; }
.lesson-loc { opacity: 0.9; }
.color-0 { background: #409eff; }
.color-1 { background: #67c23a; }
.color-2 { background: #e6a23c; }
.color-3 { background: #f56c6c; }
.color-4 { background: #909399; }
</style>
