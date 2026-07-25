import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/auth/Login.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/auth/Register.vue'),
    meta: { title: '注册' }
  },
  {
    path: '/',
    component: () => import('@/layouts/BasicLayout.vue'),
    children: [
      {
        path: '',
        name: 'Home',
        component: () => import('@/views/Home.vue'),
        meta: { title: '首页' }
      },
      // 论坛
      {
        path: 'forum/study',
        name: 'StudyForum',
        component: () => import('@/views/forum/StudyForum.vue'),
        meta: { title: '学习论坛' }
      },
      {
        path: 'forum/game',
        name: 'GameForum',
        component: () => import('@/views/forum/GameForum.vue'),
        meta: { title: '游戏社区' }
      },
      {
        path: 'forum/post/:id',
        name: 'PostDetail',
        component: () => import('@/views/forum/PostDetail.vue'),
        meta: { title: '帖子详情' }
      },
      {
        path: 'forum/post/edit',
        name: 'PostEdit',
        component: () => import('@/views/forum/PostEdit.vue'),
        meta: { title: '发布帖子', requireAuth: true }
      },
      // 表白墙
      {
        path: 'confession',
        name: 'ConfessionWall',
        component: () => import('@/views/confession/ConfessionWall.vue'),
        meta: { title: '表白墙' }
      },
      // 二手交易
      {
        path: 'secondhand',
        name: 'SecondhandMarket',
        component: () => import('@/views/secondhand/Market.vue'),
        meta: { title: '二手市场' }
      },
      {
        path: 'secondhand/:id',
        name: 'ProductDetail',
        component: () => import('@/views/secondhand/ProductDetail.vue'),
        meta: { title: '商品详情' }
      },
      {
        path: 'secondhand/publish',
        name: 'PublishProduct',
        component: () => import('@/views/secondhand/PublishProduct.vue'),
        meta: { title: '发布商品', requireAuth: true }
      },
      // 代课服务
      {
        path: 'course',
        name: 'CourseService',
        component: () => import('@/views/course/CourseService.vue'),
        meta: { title: '代课服务' }
      },
      {
        path: 'course/:id',
        name: 'OrderDetail',
        component: () => import('@/views/course/OrderDetail.vue'),
        meta: { title: '订单详情' }
      },
      {
        path: 'course/publish',
        name: 'PublishOrder',
        component: () => import('@/views/course/PublishOrder.vue'),
        meta: { title: '发布代课', requireAuth: true }
      },
      // AI 助手
      {
        path: 'ai',
        name: 'AIAssistant',
        component: () => import('@/views/ai/AIAssistant.vue'),
        meta: { title: 'AI智能助手', requireAuth: true }
      },
      // 消息
      {
        path: 'message',
        name: 'Message',
        component: () => import('@/views/message/Message.vue'),
        meta: { title: '消息', requireAuth: true }
      },
      // 个人中心
      {
        path: 'user/:id',
        name: 'UserProfile',
        component: () => import('@/views/user/UserProfile.vue'),
        meta: { title: '个人主页' }
      },
      {
        path: 'profile/edit',
        name: 'EditProfile',
        component: () => import('@/views/user/EditProfile.vue'),
        meta: { title: '编辑资料', requireAuth: true }
      }
    ]
  },
  // 后台管理(独立布局)
  {
    path: '/admin',
    component: () => import('@/layouts/AdminLayout.vue'),
    meta: { requireAuth: true, requireAdmin: true },
    children: [
      {
        path: '',
        name: 'AdminDashboard',
        component: () => import('@/views/admin/Dashboard.vue'),
        meta: { title: '管理控制台' }
      },
      {
        path: 'users',
        name: 'AdminUsers',
        component: () => import('@/views/admin/UserManage.vue'),
        meta: { title: '用户管理' }
      },
      {
        path: 'review',
        name: 'AdminReview',
        component: () => import('@/views/admin/ContentReview.vue'),
        meta: { title: '内容审核' }
      },
      {
        path: 'reports',
        name: 'AdminReports',
        component: () => import('@/views/admin/ReportManage.vue'),
        meta: { title: '举报处理' }
      },
      {
        path: 'notice',
        name: 'AdminNotice',
        component: () => import('@/views/admin/NoticeManage.vue'),
        meta: { title: '公告管理' }
      },
      {
        path: 'logs',
        name: 'AdminLogs',
        component: () => import('@/views/admin/OperationLog.vue'),
        meta: { title: '操作日志' }
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/NotFound.vue'),
    meta: { title: '页面不存在' }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior() {
    return { top: 0 }
  }
})

// 全局前置守卫
router.beforeEach((to, from, next) => {
  document.title = to.meta.title
    ? `${to.meta.title} - 智能校园综合服务平台`
    : '智能校园综合服务平台'

  const userStore = useUserStore()

  if (to.meta.requireAuth && !userStore.isLogin) {
    next({ path: '/login', query: { redirect: to.fullPath } })
    return
  }
  if (to.meta.requireAdmin && !userStore.isAdmin) {
    next('/')
    return
  }
  next()
})

export default router
