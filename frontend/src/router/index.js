import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  { path: '/login', name: 'login', component: () => import('../views/Login.vue') },
  {
    path: '/',
    component: () => import('../views/Layout.vue'),
    redirect: '/home',
    children: [
      { path: 'home', name: 'home', component: () => import('../views/student/Home.vue'), meta: { roles: ['STUDENT'] } },
      { path: 'courses', name: 'courses', component: () => import('../views/student/Courses.vue'), meta: { roles: ['STUDENT'] } },
      { path: 'my-selections', name: 'mySelections', component: () => import('../views/student/MySelections.vue'), meta: { roles: ['STUDENT'] } },
      { path: 'schedule', name: 'schedule', component: () => import('../views/student/Schedule.vue'), meta: { roles: ['STUDENT'] } },
      { path: 'profile', name: 'profile', component: () => import('../views/student/Profile.vue'), meta: { roles: ['STUDENT', 'TEACHER', 'ADMIN'] } },
      { path: 'admin/users', name: 'adminUsers', component: () => import('../views/admin/Users.vue'), meta: { roles: ['ADMIN'] } },
      { path: 'admin/courses', name: 'adminCourses', component: () => import('../views/admin/Courses.vue'), meta: { roles: ['ADMIN'] } },
      { path: 'admin/config', name: 'adminConfig', component: () => import('../views/admin/Config.vue'), meta: { roles: ['ADMIN'] } },
      { path: 'admin/stats', name: 'adminStats', component: () => import('../views/admin/Stats.vue'), meta: { roles: ['ADMIN'] } },
      { path: 'admin/notices', name: 'adminNotices', component: () => import('../views/admin/Notices.vue'), meta: { roles: ['ADMIN'] } },
      { path: 'admin/records', name: 'adminRecords', component: () => import('../views/admin/Records.vue'), meta: { roles: ['ADMIN'] } },
      { path: 'teacher/my-courses', name: 'teacherMyCourses', component: () => import('../views/teacher/MyCourses.vue'), meta: { roles: ['TEACHER'] } },
      { path: 'teacher/students', name: 'teacherStudents', component: () => import('../views/teacher/StudentsGrades.vue'), meta: { roles: ['TEACHER'] } }
    ]
  },
  { path: '/:pathMatch(.*)*', redirect: '/home' }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

function homeOf(role) {
  if (role === 'TEACHER') return '/teacher/my-courses'
  if (role === 'ADMIN') return '/admin/stats'
  return '/home'
}

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  const user = JSON.parse(localStorage.getItem('user') || 'null')
  if (to.path === '/login') {
    if (token && user) next(homeOf(user.role))
    else next()
    return
  }
  if (!token || !user) {
    next('/login')
    return
  }
  const roles = to.meta.roles
  if (roles && !roles.includes(user.role)) {
    next(homeOf(user.role))
    return
  }
  next()
})

export default router
