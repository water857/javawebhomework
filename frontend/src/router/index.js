import { createRouter, createWebHistory } from 'vue-router'

// 路由配置
const routes = [
  {
    path: '/',
    redirect: '/login'
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue')
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('../views/Register.vue')
  },
  {
    path: '/resident',
    name: 'Resident',
    component: () => import('../views/Resident.vue'),
    meta: { requiresAuth: true, role: 'resident' }
  },
  {
    path: '/resident/profile',
    name: 'ResidentProfile',
    component: () => import('../views/ResidentProfile.vue'),
    meta: { requiresAuth: true, role: 'resident' }
  },
  {
    path: '/admin',
    name: 'Admin',
    component: () => import('../views/Admin.vue'),
    meta: { requiresAuth: true, role: 'property_admin' }
  },
  { path: '/admin/profile', name: 'AdminProfile', component: () => import('../views/AdminProfile.vue'), meta: { requiresAuth: true, role: 'property_admin' } },
  { path: '/admin/resident-management', name: 'ResidentManagement', component: () => import('../views/ResidentManagement.vue'), meta: { requiresAuth: true, role: 'property_admin' } },
  { path: '/admin/provider-management', name: 'ProviderManagement', component: () => import('../views/ProviderManagement.vue'), meta: { requiresAuth: true, role: 'property_admin' } },
  { path: '/resident/repairs', name: 'ResidentRepairs', component: () => import('../views/ResidentRepairs.vue'), meta: { requiresAuth: true, role: 'resident' } },
  { path: '/resident/community', name: 'ResidentCommunity', component: () => import('../views/ResidentCommunity.vue'), meta: { requiresAuth: true, role: 'resident' } },
  { path: '/resident/announcements', name: 'ResidentAnnouncements', component: () => import('../views/ResidentAnnouncements.vue'), meta: { requiresAuth: true, role: 'resident' } },
  { path: '/resident/activities', name: 'ResidentActivities', component: () => import('../views/ResidentActivities.vue'), meta: { requiresAuth: true, role: 'resident' } },
  { path: '/resident/activities/:id', name: 'ResidentActivityDetail', component: () => import('../views/ResidentActivityDetail.vue'), meta: { requiresAuth: true, role: 'resident' } },
  { path: '/resident/activities/publish', name: 'ResidentPublishActivity', component: () => import('../views/ResidentPublishActivity.vue'), meta: { requiresAuth: true, role: 'resident' } },
  { path: '/resident/activities/edit/:id', name: 'ResidentEditActivity', component: () => import('../views/ResidentEditActivity.vue'), meta: { requiresAuth: true, role: 'resident' } },
  { path: '/resident/messages', name: 'ResidentMessages', component: () => import('../views/ResidentMessages.vue'), meta: { requiresAuth: true, role: 'resident' } },
  { path: '/resident/messages/:userId', name: 'ResidentMessageChat', component: () => import('../views/ResidentMessageChat.vue'), meta: { requiresAuth: true, role: 'resident' } },
  { path: '/resident/parking', name: 'ResidentParking', component: () => import('../views/ResidentParking.vue'), meta: { requiresAuth: true, role: 'resident' } },
  { path: '/resident/parking-admin', name: 'ResidentParkingAdmin', component: () => import('../views/ResidentParkingAdmin.vue'), meta: { requiresAuth: true, role: 'property_admin' } },
  { path: '/resident/visitor', name: 'ResidentVisitor', component: () => import('../views/ResidentVisitor.vue'), meta: { requiresAuth: true, role: 'resident' } },
  { path: '/resident/visitor-admin', name: 'ResidentVisitorAdmin', component: () => import('../views/ResidentVisitorAdmin.vue'), meta: { requiresAuth: true, role: 'property_admin' } },
  { path: '/resident/second-hand', name: 'ResidentSecondHand', component: () => import('../views/ResidentSecondHand.vue'), meta: { requiresAuth: true, role: 'resident' } },
  { path: '/resident/skill-share', name: 'ResidentSkillShare', component: () => import('../views/ResidentSkillShare.vue'), meta: { requiresAuth: true, role: 'resident' } },
  { path: '/resident/lost-found', name: 'ResidentLostFound', component: () => import('../views/ResidentLostFound.vue'), meta: { requiresAuth: true, role: 'resident' } },
  { path: '/admin/activities', name: 'AdminActivities', component: () => import('../views/AdminActivities.vue'), meta: { requiresAuth: true, role: 'property_admin' } },
  { path: '/admin/activities/:id', name: 'AdminActivityDetail', component: () => import('../views/AdminActivityDetail.vue'), meta: { requiresAuth: true, role: 'property_admin' } },
  { path: '/admin/activities/publish', name: 'AdminPublishActivity', component: () => import('../views/AdminPublishActivity.vue'), meta: { requiresAuth: true, role: 'property_admin' } },
  { path: '/admin/activities/edit/:id', name: 'AdminEditActivity', component: () => import('../views/AdminEditActivity.vue'), meta: { requiresAuth: true, role: 'property_admin' } },
  { path: '/admin/repair-management', name: 'AdminRepairManagement', component: () => import('../views/AdminRepairManagement.vue'), meta: { requiresAuth: true, role: 'property_admin' } },
  { path: '/admin/announcements', name: 'AdminAnnouncements', component: () => import('../views/AdminAnnouncements.vue'), meta: { requiresAuth: true, role: 'property_admin' } },
  { path: '/admin/announcements/publish', name: 'AdminPublishAnnouncement', component: () => import('../views/AdminPublishAnnouncement.vue'), meta: { requiresAuth: true, role: 'property_admin' } },
  { path: '/admin/announcements/:id', name: 'AdminAnnouncementDetail', component: () => import('../views/AdminAnnouncementDetail.vue'), meta: { requiresAuth: true, role: 'property_admin' } },
  { path: '/admin/announcements/edit/:id', name: 'AdminEditAnnouncement', component: () => import('../views/AdminEditAnnouncement.vue'), meta: { requiresAuth: true, role: 'property_admin' } },
  // 物业费相关路由
  { path: '/resident/property-fee', name: 'ResidentPropertyFee', component: () => import('../views/ResidentPropertyFee.vue'), meta: { requiresAuth: true, role: 'resident' } },
  { path: '/resident/property-fee/payment-history', name: 'ResidentPaymentHistory', component: () => import('../views/ResidentPaymentHistory.vue'), meta: { requiresAuth: true, role: 'resident' } },
  { path: '/admin/property-fee-management', name: 'AdminPropertyFeeManagement', component: () => import('../views/AdminPropertyFeeManagement.vue'), meta: { requiresAuth: true, role: 'property_admin' } },
  { path: '/admin/property-fee-statistics', name: 'AdminPropertyFeeStatistics', component: () => import('../views/AdminPropertyFeeStatistics.vue'), meta: { requiresAuth: true, role: 'property_admin' } },
  { path: '/admin/property-fee-reconciliation', name: 'AdminPropertyFeeReconciliation', component: () => import('../views/AdminPropertyFeeReconciliation.vue'), meta: { requiresAuth: true, role: 'property_admin' } },
  {
    path: '/provider',
    name: 'Provider',
    component: () => import('../views/Provider.vue'),
    meta: { requiresAuth: true, role: 'service_provider' }
  },
  {
    path: '/provider/profile',
    name: 'ProviderProfile',
    component: () => import('../views/ProviderProfile.vue'),
    meta: { requiresAuth: true, role: 'service_provider' }
  },
  { path: '/provider/service-management', name: 'ProviderServiceManagement', component: () => import('../views/ProviderServiceManagement.vue'), meta: { requiresAuth: true, role: 'service_provider' } },
  { path: '/provider/activities', name: 'ProviderActivities', component: () => import('../views/ProviderActivities.vue'), meta: { requiresAuth: true, role: 'service_provider' } },
  { path: '/provider/activities/:id', name: 'ProviderActivityDetail', component: () => import('../views/ProviderActivityDetail.vue'), meta: { requiresAuth: true, role: 'service_provider' } },
  { path: '/provider/activities/publish', name: 'ProviderPublishActivity', component: () => import('../views/ProviderPublishActivity.vue'), meta: { requiresAuth: true, role: 'service_provider' } },
  { path: '/provider/activities/edit/:id', name: 'ProviderEditActivity', component: () => import('../views/ProviderEditActivity.vue'), meta: { requiresAuth: true, role: 'service_provider' } },
  {
    path: '/notifications',
    name: 'Notifications',
    component: () => import('../views/Notifications.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/announcements',
    name: 'Announcements',
    component: () => import('../views/ResidentAnnouncements.vue'),
    meta: { requiresAuth: true }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  // 检查是否需要身份验证
  if (to.matched.some(record => record.meta.requiresAuth)) {
    // 安全获取本地存储中的token和role
    let token = ''
    let role = ''
    if (typeof localStorage !== 'undefined') {
      token = localStorage.getItem('token')
      role = localStorage.getItem('role')
    }
    
    if (!token) {
      // 没有token，跳转到登录页
      next({ name: 'Login' })
    } else if (to.meta.role && to.meta.role !== role) {
      // 角色不匹配，跳转到对应角色的首页
      if (role === 'resident') {
        next({ name: 'Resident' })
      } else if (role === 'property_admin') {
        next({ name: 'Admin' })
      } else if (role === 'service_provider') {
        next({ name: 'Provider' })
      } else {
        next({ name: 'Login' })
      }
    } else {
      // 有token且角色匹配，允许访问
      next()
    }
  } else {
    // 不需要身份验证的页面，直接访问
    next()
  }
})

export default router
