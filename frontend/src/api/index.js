import request from '../utils/request'

// ---------- 认证 ----------
export const apiLogin = data => request.post('/user/auth/login', data)
export const apiRegister = data => request.post('/user/auth/register', data)

// ---------- 个人 ----------
export const apiProfile = () => request.get('/user/profile')
export const apiUpdateProfile = data => request.put('/user/profile', data)
export const apiChangePassword = data => request.put('/user/profile/password', data)

// ---------- 公告 ----------
export const apiNotices = () => request.get('/user/notice/list')
export const apiNoticePage = params => request.get('/user/notice/admin/page', { params })
export const apiNoticeCreate = data => request.post('/user/notice/admin', data)
export const apiNoticeUpdate = (id, data) => request.put('/user/notice/admin/' + id, data)
export const apiNoticeDelete = id => request.delete('/user/notice/admin/' + id)

// ---------- 课程 ----------
export const apiCoursePage = params => request.get('/course/page', { params })
export const apiCourseDetail = id => request.get('/course/' + id)
export const apiCourseHot = limit => request.get('/course/hot', { params: { limit: limit || 10 } })
export const apiCourseCreate = data => request.post('/course/admin', data)
export const apiCourseUpdate = (id, data) => request.put('/course/admin/' + id, data)
export const apiCourseDelete = id => request.delete('/course/admin/' + id)
export const apiCourseStatus = (id, status) => request.put('/course/admin/' + id + '/status', null, { params: { status } })
export const apiTeacherCourses = () => request.get('/course/teacher/mine')
export const apiTeacherCourseUpdate = (id, data) => request.put('/course/teacher/' + id, data)
export const apiTeachingPlanSave = (data, isAdmin) =>
  request.post(isAdmin ? '/course/admin/teaching-plan' : '/course/teacher/teaching-plan', data)
export const apiTeachingPlanAudit = (planId, data) => request.put('/course/admin/teaching-plan/' + planId + '/audit', data)
export const apiGradeSave = data => request.post('/course/teacher/grade', data)
export const apiGrades = params => request.get('/course/teacher/grades', { params })

// ---------- 选课 ----------
export const apiSelect = data => request.post('/selection/select', data)
export const apiDrop = recordId => request.post('/selection/drop/' + recordId)
export const apiMySelections = params => request.get('/selection/my', { params })
export const apiSchedule = params => request.get('/selection/schedule', { params })
export const apiConflictCheck = courseNo => request.get('/selection/conflict-check', { params: { courseNo } })
export const apiOpenStatus = () => request.get('/selection/open-status')
export const apiSelectionConfig = () => request.get('/selection/admin/config')
export const apiSelectionConfigUpdate = data => request.put('/selection/admin/config', data)
export const apiSelectionRecords = params => request.get('/selection/admin/records', { params })
export const apiCourseStudents = courseNo => request.get('/selection/teacher/course/' + courseNo + '/students')

// ---------- 推荐 ----------
export const apiRecommend = () => request.get('/recommend/list')
export const apiRecommendHot = limit => request.get('/recommend/hot', { params: { limit: limit || 10 } })

// ---------- 统计 ----------
export const apiStatsOverview = params => request.get('/statistics/overview', { params })
export const apiStatsTrend = params => request.get('/statistics/trend', { params })
export const apiStatsTop = limit => request.get('/statistics/top', { params: { limit: limit || 10 } })
export const apiStatsCollege = () => request.get('/statistics/college')
export const apiStatsType = () => request.get('/statistics/type')
export const apiStatsHour = () => request.get('/statistics/hour')
export const statsExportUrl = '/api/statistics/export/top'

// ---------- 用户管理 ----------
export const apiUserPage = params => request.get('/user/admin/page', { params })
export const apiUserCreate = data => request.post('/user/admin', data)
export const apiUserUpdate = (id, data) => request.put('/user/admin/' + id, data)
export const apiUserResetPassword = (id, data) => request.put('/user/admin/' + id + '/reset-password', data)
export const apiUserImport = csv => request.post('/user/admin/import', { csv })
