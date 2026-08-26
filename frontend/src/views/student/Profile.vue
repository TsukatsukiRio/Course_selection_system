<template>
  <el-row :gutter="16">
    <el-col :span="12">
      <el-card shadow="never">
        <template #header><b>个人信息</b></template>
        <el-form :model="form" label-width="80px">
          <el-form-item label="账号"><el-input v-model="form.username" disabled /></el-form-item>
          <el-form-item label="姓名"><el-input v-model="form.name" /></el-form-item>
          <el-form-item label="院系"><el-input v-model="form.college" /></el-form-item>
          <el-form-item label="专业"><el-input v-model="form.major" /></el-form-item>
          <el-form-item label="邮箱"><el-input v-model="form.email" /></el-form-item>
          <el-form-item label="手机"><el-input v-model="form.phone" /></el-form-item>
          <el-form-item label="兴趣"><el-input v-model="form.interestTags" placeholder="逗号分隔" /></el-form-item>
          <el-button type="primary" @click="saveProfile">保存</el-button>
        </el-form>
      </el-card>
    </el-col>
    <el-col :span="12">
      <el-card shadow="never">
        <template #header><b>修改密码</b></template>
        <el-form :model="pwdForm" label-width="80px">
          <el-form-item label="原密码">
            <el-input v-model="pwdForm.oldPassword" type="password" show-password />
          </el-form-item>
          <el-form-item label="新密码">
            <el-input v-model="pwdForm.newPassword" type="password" show-password
                      placeholder="8-20位，含大小写字母和数字" />
          </el-form-item>
          <el-button type="primary" @click="savePassword">修改密码</el-button>
        </el-form>
      </el-card>
    </el-col>
  </el-row>
</template>

<script setup>
import { onMounted, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { apiProfile, apiUpdateProfile, apiChangePassword } from '../../api'

const form = reactive({})
const pwdForm = reactive({ oldPassword: '', newPassword: '' })

onMounted(async () => {
  Object.assign(form, await apiProfile())
})

async function saveProfile() {
  await apiUpdateProfile(form)
  ElMessage.success('保存成功')
}

async function savePassword() {
  await apiChangePassword(pwdForm)
  ElMessage.success('密码修改成功')
  pwdForm.oldPassword = ''
  pwdForm.newPassword = ''
}
</script>
