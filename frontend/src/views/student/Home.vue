<template>
  <div>
    <el-alert v-for="n in notices" :key="n.id" :title="n.title" type="info" show-icon
              :description="n.content" style="margin-bottom: 8px" :closable="false" />
    <el-card shadow="never" style="margin-bottom: 16px">
      <template #header><b>AI 个性化推荐</b><span class="tip">基于你的成绩、兴趣与同学选课行为</span></template>
      <el-row :gutter="16">
        <el-col v-for="item in recommendList" :key="item.courseNo" :span="8" style="margin-bottom: 12px">
          <el-card shadow="hover">
            <div class="course-name">{{ item.name }} <el-tag size="small">{{ typeLabel(item.courseType) }}</el-tag></div>
            <div class="meta">{{ item.courseNo }} · {{ item.credits }}学分 · {{ item.teacherName }}</div>
            <div class="meta">时间：{{ item.classTime }} · {{ item.location }}</div>
            <div class="meta">已选 {{ item.selectedCount }}/{{ item.capacity }}</div>
            <div class="reason"><el-tag type="warning" size="small" effect="plain">{{ item.reason }}</el-tag></div>
            <el-button type="primary" size="small" style="margin-top: 8px"
                       @click="quickSelect(item.courseNo)">立即选课</el-button>
          </el-card>
        </el-col>
        <el-empty v-if="!recommendList.length" description="暂无推荐课程" />
      </el-row>
    </el-card>
    <el-card shadow="never">
      <template #header><b>热门课程</b></template>
      <el-table :data="hotList" size="small">
        <el-table-column prop="courseNo" label="编号" width="100" />
        <el-table-column prop="name" label="课程名称" />
        <el-table-column prop="teacherName" label="教师" width="120" />
        <el-table-column prop="classTime" label="上课时间" />
        <el-table-column label="已选/容量" width="110">
          <template #default="{ row }">{{ row.selectedCount }}/{{ row.capacity }}</template>
        </el-table-column>
        <el-table-column label="操作" width="100">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="quickSelect(row.courseNo)">选课</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { apiRecommend, apiRecommendHot, apiNotices, apiSelect } from '../../api'

const recommendList = ref([])
const hotList = ref([])
const notices = ref([])

const typeLabels = { REQUIRED: '必修', ELECTIVE: '选修', GENERAL: '通识' }
const typeLabel = t => typeLabels[t] || t

async function load() {
  const [recommend, hot, noticeList] = await Promise.all([
    apiRecommend().catch(() => []),
    apiRecommendHot(10).catch(() => []),
    apiNotices().catch(() => [])
  ])
  recommendList.value = recommend || []
  hotList.value = hot || []
  notices.value = noticeList || []
}

async function quickSelect(courseNo) {
  try {
    await apiSelect({ courseNo })
    ElMessage.success('选课成功')
    load()
  } catch (e) { /* 错误已在拦截器提示 */ }
}

onMounted(load)
</script>

<style scoped>
.tip { color: #999; font-size: 12px; margin-left: 8px; }
.course-name { font-weight: bold; font-size: 15px; margin-bottom: 4px; }
.meta { color: #666; font-size: 13px; line-height: 1.7; }
.reason { margin-top: 6px; }
</style>
