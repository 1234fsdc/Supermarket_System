import { createRouter, createWebHistory } from 'vue-router'
import Layout from '@/layout/index.vue'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/',
    component: Layout,
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/index.vue'),
        meta: { title: '工作台', roles: ['admin', 'manager'] }
      }
    ]
  },
  {
    path: '/statistics',
    component: Layout,
    redirect: '/statistics/overview',
    children: [
      {
        path: 'overview',
        name: 'StatisticsOverview',
        component: () => import('@/views/statistics/overview.vue'),
        meta: { title: '数据统计', roles: ['admin', 'manager'] }
      }
    ]
  },
  {
    path: '/order',
    component: Layout,
    redirect: '/order/list',
    children: [
      {
        path: 'list',
        name: 'OrderList',
        component: () => import('@/views/order/list.vue'),
        meta: { title: '订单管理', roles: ['admin', 'manager', 'clerk'] }
      }
    ]
  },
  {
    path: '/product',
    component: Layout,
    redirect: '/product/list',
    children: [
      {
        path: 'list',
        name: 'ProductList',
        component: () => import('@/views/product/list.vue'),
        meta: { title: '商品管理', roles: ['admin', 'manager', 'clerk'] }
      }
    ]
  },
  {
    path: '/category',
    component: Layout,
    redirect: '/category/list',
    children: [
      {
        path: 'list',
        name: 'CategoryList',
        component: () => import('@/views/category/list.vue'),
        meta: { title: '分类管理', roles: ['admin', 'manager'] }
      }
    ]
  },
  {
    path: '/employee',
    component: Layout,
    redirect: '/employee/list',
    children: [
      {
        path: 'list',
        name: 'EmployeeList',
        component: () => import('@/views/employee/list.vue'),
        meta: { title: '员工管理', roles: ['admin'] }
      }
    ]
  },
  {
    path: '/shop',
    component: Layout,
    redirect: '/shop/status',
    children: [
      {
        path: 'status',
        name: 'ShopStatus',
        component: () => import('@/views/shop/index.vue'),
        meta: { title: '店铺状态', roles: ['admin', 'manager'] }
      }
    ]
  },
  {
    path: '/coupon',
    component: Layout,
    redirect: '/coupon/list',
    children: [
      {
        path: 'list',
        name: 'CouponList',
        component: () => import('@/views/coupon/list.vue'),
        meta: { title: '优惠券管理', roles: ['admin', 'manager'] }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

const roleHome = { admin: '/dashboard', manager: '/dashboard', clerk: '/order' }

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  const role = localStorage.getItem('role')

  if (to.path === '/login') {
    next()
    return
  }

  if (!token) {
    next('/login')
    return
  }

  const routeRoles = to.matched.some(r => r.meta.roles)
  if (routeRoles) {
    const allowed = to.matched.some(r => r.meta.roles && r.meta.roles.includes(role))
    if (!allowed) {
      next(roleHome[role] || '/order')
      return
    }
  }

  next()
})

export default router
