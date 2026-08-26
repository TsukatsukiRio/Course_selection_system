<template>
  <div class="login-wrap">
    <div class="login-card">
      <h2>智能校园微服务课程选退课系统</h2>
      <p class="sub">Smart Campus Course Selection System</p>
      <el-tabs v-model="activeTab">
        <el-tab-pane label="登录" name="login">
          <el-form :model="loginForm" label-width="70px">
            <el-form-item label="账号">
              <el-input v-model="loginForm.username" placeholder="学号 / 工号" />
            </el-form-item>
            <el-form-item label="密码">
              <el-input v-model="loginForm.password" type="password" show-password
                        placeholder="密码" @keyup.enter="onLogin" />
            </el-form-item>
            <el-button type="primary" style="width: 100%" :loading="loading" @click="onLogin">登 录</el-button>
          </el-form>
        </el-tab-pane>
        <el-tab-pane label="学生注册" name="register">
          <el-form :model="registerForm" label-width="80px">
            <el-form-item label="学号">
              <el-input v-model="registerForm.username" placeholder="6-12位数字" />
            </el-form-item>
            <el-form-item label="姓名">
              <el-input v-model="registerForm.name" placeholder="2-20个中文字符" />
            </el-form-item>
            <el-form-item label="密码">
              <el-input v-model="registerForm.password" type="password" show-password
                        placeholder="8-20位，含大小写字母和数字" />
            </el-form-item>
            <el-form-item label="院系">
              <el-input v-model="registerForm.college" placeholder="如：计算机学院" />
            </el-form-item>
            <el-form-item label="专业">
              <el-input v-model="registerForm.major" placeholder="如：软件工程" />
            </el-form-item>
            <el-form-item label="邮箱">
              <el-input v-model="registerForm.email" placeholder="选填" />
            </el-form-item>
            <el-form-item label="兴趣">
              <el-input v-model="registerForm.interestTags" placeholder="逗号分隔，如：编程,人工智能" />
            </el-form-item>
            <el-button type="success" style="width: 100%" :loading="loading" @click="onRegister">注 册</el-button>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { apiLogin, apiRegister } from '../api'
import { useUserStore } from '../stores/user'

const router = useRouter()
const userStore = useUserStore()
const activeTab = ref('login')
const loading = ref(false)

const loginForm = reactive({ username: '', password: '' })
const registerForm = reactive({
  username: '', name: '', password: '', college: '', major: '', email: '', interestTags: ''
})

async function onLogin() {
  if (!loginForm.username || !loginForm.password) {
    ElMessage.warning('请输入账号和密码')
    return
  }
  loading.value = true
  try {
    const data = await apiLogin(loginForm)
    userStore.setLogin(data.token, data.user)
    ElMessage.success('登录成功，欢迎 ' + data.user.name)
    const homePath = { STUDENT: '/home', TEACHER: '/teacher/my-courses', ADMIN: '/admin/stats' }[data.user.role] || '/home'
    router.push(homePath)
  } catch (e) { /* 错误已在拦截器提示 */ } finally {
    loading.value = false
  }
}

async function onRegister() {
  loading.value = true
  try {
    await apiRegister(registerForm)
    ElMessage.success('注册成功，请登录')
    loginForm.username = registerForm.username
    activeTab.value = 'login'
  } catch (e) { /* 错误已在拦截器提示 */ } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-wrap {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #1e3c72 0%, #2a5298 100%);
}
.login-card {
  width: 440px;
  background: #fff;
  border-radius: 8px;
  padding: 28px 32px;
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.2);
}
.login-card h2 { font-size: 20px; text-align: center; color: #1e3c72; }
.login-card .sub { text-align: center; color: #999; font-size: 12px; margin: 4px 0 16px; }
</style>
