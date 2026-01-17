import { createRouter, createWebHistory } from 'vue-router'
import { PORTAL_KEYS } from '../stores/user'

const routes = [
  // 用户端路由
  {
    path: '/',
    name: 'Home',
    component: () => import('../views/Home.vue'),
    meta: { title: '首页' }
  },
  {
    path: '/artists',
    name: 'Artists',
    component: () => import('../views/Artists.vue'),
    meta: { title: '化妆师' }
  },
  {
    path: '/artist/:id',
    name: 'ArtistDetail',
    component: () => import('../views/ArtistDetail.vue'),
    meta: { title: '化妆师详情' }
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('../views/Register.vue'),
    meta: { title: '注册' }
  },
  {
    path: '/my/appointments',
    name: 'MyAppointments',
    component: () => import('../views/MyAppointments.vue'),
    meta: { title: '我的预约', requiresAuth: true }
  },
  {
    path: '/my/favorites',
    name: 'MyFavorites',
    component: () => import('../views/MyFavorites.vue'),
    meta: { title: '我的收藏', requiresAuth: true }
  },
  {
    path: '/profile',
    name: 'Profile',
    component: () => import('../views/Profile.vue'),
    meta: { title: '个人中心', requiresAuth: true }
  },
  {
    path: '/appointment/:id',
    name: 'AppointmentDetail',
    component: () => import('../views/AppointmentDetail.vue'),
    meta: { title: '预约详情', requiresAuth: true }
  },
  {
    path: '/payment/:id',
    name: 'Payment',
    component: () => import('../views/Payment.vue'),
    meta: { title: '支付', requiresAuth: true }
  },

  // 化妆师端路由
  {
    path: '/artist/login',
    name: 'ArtistLogin',
    component: () => import('../views/artist/ArtistLogin.vue'),
    meta: { title: '化妆师登录', isArtistPortal: true }
  },
  {
    path: '/artist',
    component: () => import('../layouts/ArtistLayout.vue'),
    meta: { requiresArtist: true, isArtistPortal: true },
    children: [
      {
        path: '',
        name: 'ArtistDashboard',
        component: () => import('../views/artist/ArtistDashboard.vue'),
        meta: { title: '化妆师工作台', isArtistPortal: true }
      },
      {
        path: 'orders',
        name: 'ArtistOrders',
        component: () => import('../views/artist/MyOrders.vue'),
        meta: { title: '预约管理', isArtistPortal: true }
      },
      {
        path: 'services',
        name: 'ArtistServices',
        component: () => import('../views/artist/MyServices.vue'),
        meta: { title: '服务套餐', isArtistPortal: true }
      },
      {
        path: 'reviews',
        name: 'ArtistReviews',
        component: () => import('../views/artist/MyReviews.vue'),
        meta: { title: '客户评价', isArtistPortal: true }
      }
    ]
  },

  // 管理员端路由
  {
    path: '/admin/login',
    name: 'AdminLogin',
    component: () => import('../views/admin/AdminLogin.vue'),
    meta: { title: '管理员登录', isAdmin: true }
  },
  {
    path: '/admin',
    component: () => import('../layouts/AdminLayout.vue'),
    meta: { requiresAdmin: true, isAdmin: true },
    children: [
      {
        path: '',
        name: 'AdminDashboard',
        component: () => import('../views/admin/Dashboard.vue'),
        meta: { title: '仪表盘', isAdmin: true }
      },
      {
        path: 'users',
        name: 'AdminUsers',
        component: () => import('../views/admin/UserManagement.vue'),
        meta: { title: '用户管理', isAdmin: true }
      },
      {
        path: 'artists',
        name: 'AdminArtists',
        component: () => import('../views/admin/ArtistManagement.vue'),
        meta: { title: '化妆师管理', isAdmin: true }
      },
      {
        path: 'applications',
        name: 'AdminApplications',
        component: () => import('../views/admin/ApplicationReview.vue'),
        meta: { title: '入驻审核', isAdmin: true }
      },
      {
        path: 'appointments',
        name: 'AdminAppointments',
        component: () => import('../views/admin/AppointmentManagement.vue'),
        meta: { title: '预约管理', isAdmin: true }
      },
      {
        path: 'reviews',
        name: 'AdminReviews',
        component: () => import('../views/admin/ReviewManagement.vue'),
        meta: { title: '评价管理', isAdmin: true }
      },
      {
        path: 'services',
        name: 'AdminServices',
        component: () => import('../views/admin/ServiceManagement.vue'),
        meta: { title: '服务套餐管理', isAdmin: true }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior() {
    return { top: 0 }
  }
})

// 辅助函数：根据端口类型获取用户
function getUserByPortalType(portalType) {
  const key = PORTAL_KEYS[portalType]
  const stored = localStorage.getItem(key)
  return stored ? JSON.parse(stored) : null
}

// 路由守卫
router.beforeEach((to, from, next) => {
  // 设置页面标题
  let suffix = '安颜美妆'
  if (to.meta.isAdmin) suffix = '管理后台'
  else if (to.meta.isArtistPortal) suffix = '化妆师工作台'
  document.title = `${to.meta.title || ''} - ${suffix}`

  // 用户端需要登录的页面 - 检查 user 键
  if (to.meta.requiresAuth && !to.meta.requiresArtist && !to.meta.requiresAdmin) {
    const user = getUserByPortalType('user')
    if (!user) {
      next({ name: 'Login', query: { redirect: to.fullPath } })
      return
    }
  }

  // 化妆师端需要登录 - 检查 artist_user 键
  if (to.meta.requiresArtist) {
    const artistUser = getUserByPortalType('artist')
    if (!artistUser) {
      next({ name: 'ArtistLogin' })
      return
    }
    // 检查角色权限 (role: 0用户 1化妆师 2管理员)
    if (artistUser.role !== 1 && artistUser.role !== 2) {
      alert('此功能仅对化妆师开放')
      next({ name: 'ArtistLogin' })
      return
    }
  }

  // 管理员端需要登录 - 检查 admin_user 键
  if (to.meta.requiresAdmin) {
    const adminUser = getUserByPortalType('admin')
    if (!adminUser) {
      next({ name: 'AdminLogin' })
      return
    }
    // 检查管理员权限
    if (adminUser.role !== 2) {
      next({ name: 'AdminLogin' })
      return
    }
  }

  next()
})

export default router
