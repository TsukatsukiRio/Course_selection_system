<template>
  <el-card shadow="never">
    <template #header><b>选课参数配置</b></template>
    <el-form label-width="180px" style="max-width: 560px">
      <el-form-item label="选课开始时间">
        <el-input v-model="form.select_start" placeholder="2026-08-01 00:00:00" />
      </el-form-item>
      <el-form-item label="选课截止时间">
        <el-input v-model="form.select_end" placeholder="2026-09-30 23:59:59" />
      </el-form-item>
      <el-form-item label="退课截止时间">
        <el-input v-model="form.drop_end" placeholder="2026-09-15 23:59:59" />
      </el-form-item>
      <el-form-item label="每人选课上限（门）">
        <el-input-number v-model="form.max_course_count" :min="1" :max="20" />
      </el-form-item>
      <el-form-item label="学期学分上限">
        <el-input-number v-model="form.max_credits" :min="1" :max="100" />
      </el-form-item>
      <el-form-item label="允许退必修课">
        <el-switch v-model="form.allow_drop_required" active-value="true" inactive-value="false" />
      </el-form-item>
      <el-button type="primary" @click="save">保存配置</el-button>
    </el-form>
  </el-card>
</template>

<script setup>
import { onMounted, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { apiSelectionConfig, apiSelectionConfigUpdate } from '../../api'

const form = reactive({})

onMounted(async () => {
  Object.assign(form, await apiSelectionConfig())
})

async function save() {
  await apiSelectionConfigUpdate(form)
  ElMessage.success('配置已保存')
}
</script>
