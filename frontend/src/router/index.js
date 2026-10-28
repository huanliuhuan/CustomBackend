import { createRouter, createWebHashHistory } from 'vue-router'
import { useAuth } from '../stores/auth'
import AdminLayout from '../layout/AdminLayout.vue'

const routes = [
  { path: '/login', component: () => import('../views/LoginView.vue'), meta: { public: true } },
  {
    path: '/',
    component: AdminLayout,
    redirect: '/dashboard',
    children: [
      { path: 'dashboard', component: () => import('../views/DashboardView.vue'), meta: { perm: 'dashboard:view', title: '工作台' } },
      { path: 'system/user', component: () => import('../views/system/UserView.vue'), meta: { perm: 'system:user:list', title: '用户管理' } },
      { path: 'system/role', component: () => import('../views/system/RoleView.vue'), meta: { perm: 'system:role:list', title: '角色管理' } },
      { path: 'system/menu', component: () => import('../views/system/MenuView.vue'), meta: { perm: 'system:menu:list', title: '菜单管理' } },
      { path: 'system/dept', component: () => import('../views/system/DeptView.vue'), meta: { perm: 'system:dept:list', title: '部门管理' } },
      { path: 'system/post', component: () => import('../views/system/PostView.vue'), meta: { perm: 'system:post:list', title: '岗位管理' } },
      { path: 'system/dict', component: () => import('../views/system/DictView.vue'), meta: { perm: 'system:dict:list', title: '字典管理' } },
      { path: 'system/config', component: () => import('../views/system/ConfigView.vue'), meta: { perm: 'system:config:list', title: '参数配置' } },
      { path: 'monitor/operlog', component: () => import('../views/monitor/OperLogView.vue'), meta: { perm: 'monitor:operlog:list', title: '操作日志' } },
      { path: 'monitor/loginlog', component: () => import('../views/monitor/LoginLogView.vue'), meta: { perm: 'monitor:loginlog:list', title: '登录日志' } },
      { path: 'tool/codegen', component: () => import('../views/tool/CodeGenView.vue'), meta: { perm: 'tool:codegen:list', title: '代码生成' } },
      { path: '403', component: () => import('../views/NotFound.vue'), meta: { forbidden: true } },
      { path: ':pathMatch(.*)*', component: () => import('../views/NotFound.vue'), meta: { public: true } }
    ]
  }
]

const router = createRouter({ history: createWebHashHistory(), routes })

router.beforeEach(async to => {
  const auth = useAuth()
  if (to.meta.public) return true
  if (!auth.token) return { path: '/login', query: { r: to.fullPath } }
  if (!auth.user) {
    try { await auth.fetchMe() } catch (e) { return { path: '/login' } }
  }
  if (to.meta.perm && !auth.can(to.meta.perm)) return '/403'
  return true
})

export default router
