<template>
  <el-container class="layout">
    <el-aside width="210px" class="aside">
      <div class="logo">智能校园选课系统</div>
      <el-menu :default-active="$route.path" router background-color="#1e3c72"
               text-color="#d5deef" active-text-color="#ffd04b">
        <template v-if="userStore.role === 'STUDENT'">
          <el-menu-item index="/home"><el-icon><HomeFilled /></el-icon>首页推荐</el-menu-item>
          <el-menu-item index="/courses"><el-icon><Search /></el-icon>课程列表</el-menu-item>
          <el-menu-item index="/my-selections"><el-icon><List /></el-icon>我的选课</el-menu-item>
          <el-menu-item index="/schedule"><el-icon><Calendar /></el-icon>我的课表</el-menu-item>
        </template>
        <template v-if="userStore.role === 'TEACHER'">
          <el-menu-item index="/teacher/my-courses"><el-icon><Reading /></el-icon>我的课程</el-menu-item>
          <el-menu-item index="/teacher/students"><el-icon><User /></el-icon>学生与成绩</el-menu-item>
        </template>
        <template v-if="userStore.role === 'ADMIN'">
          <el-menu-item index="/admin/stats"><el-icon><DataAnalysis /></el-icon>数据统计</el-menu-item>
          <el-menu-item index="/admin/users"><el-icon><User /></el-icon>用户管理</el-menu-item>
          <el-menu-item index="/admin/courses"><el-icon><Reading /></el-icon>课程管理</el-menu-item>
          <el-menu-item index="/admin/records"><el-icon><List /></el-icon>选课记录</el-menu-item>
          <el-menu-item index="/admin/config"><el-icon><Setting /></el-icon>选课参数</el-menu-item>
          <el-menu-item index="/admin/notices"><el-icon><Bell /></el-icon>公告管理</el-menu-item>
        </template>
        <el-menu-item index="/profile"><el-icon><UserFilled /></el-icon>个人中心</el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="header">
        <span class="welcome">{{ roleLabel }}：{{ userStore.name }}</span>
        <el-dropdown @command="onCommand">
          <span class="user-link">{{ userStore.name }}<el-icon><ArrowDown /></el-icon></span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="profile">个人中心</el-dropdown-item>
              <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </el-header>
      <el-main class="main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import { HomeFilled, Search, List, Calendar, Reading, User, DataAnalysis, Setting, Bell, UserFilled, ArrowDown } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()

const roleLabel = computed(() => {
  return { STUDENT: '学生', TEACHER: '教师', ADMIN: '管理员' }[userStore.role] || '用户'
})

function onCommand(cmd) {
  if (cmd === 'logout') {
    userStore.logout()
    router.push('/login')
  } else if (cmd === 'profile') {
    router.push('/profile')
  }
}
</script>

<style scoped>
.layout { height: 100%; }
.aside { background: #1e3c72; }
.logo {
  height: 56px;
  line-height: 56px;
  text-align: center;
  color: #fff;
  font-weight: bold;
  font-size: 16px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.15);
}
.aside :deep(.el-menu) { border-right: none; }
.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
  border-bottom: 1px solid #eee;
}
.welcome { color: #666; font-size: 14px; }
.user-link { cursor: pointer; color: #1e3c72; display: flex; align-items: center; gap: 4px; }
.main { background: #f2f4f8; }
</style>
