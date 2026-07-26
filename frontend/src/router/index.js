import { createRouter, createWebHashHistory } from 'vue-router'
import { getCurrentUser } from '../utils/auth'
import { canViewGlobalDashboard, canManagePersonnel, canPublishActivity, canViewActivityTasks, isDepartmentHead } from '../utils/constants'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue')
  },
  {

    path: '/',
    name: 'Layout',
    component: () => import('../components/Layout.vue'),
    children: [
      {
        path: 'global-dashboard',
        name: 'GlobalDashboard',
        component: () => import('../views/GlobalDashboard.vue'),
        meta: {
          title: '全局总览'
        }
      },
       {
        path: '',
        name: 'OrganizationChart',
        component: () => import('../views/OrganizationChart.vue'),
        meta: {
          title: '组织架构图'
        }
      },
      {
        path: 'organization-list',
        name: 'OrganizationList',
        component: () => import('../views/OrganizationList.vue'),
        meta: {
          title: '组织架构列表'
        }
      },
      {
        path: 'organization-stats/:id',
        name: 'OrganizationStats',
        component: () => import('../views/OrganizationStats.vue'),
        meta: {
          title: '部门谈话统计'
        }
      },
      {
        path: 'talk',
        name: 'TalkRecord',
        component: () => import('../views/TalkRecord.vue'),
        meta: {
          title: '谈话记录'
        }
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('../views/Profile.vue'),
        meta: {
          title: '个人信息'
        }
      },
      {
        path: 'user-detail',
        name: 'UserDetail',
        component: () => import('../views/UserDetail.vue'),
        meta: {
          title: '人员详细信息'
        }
      },
      {
        path: 'talk-add',
        name: 'TalkAdd',
        component: () => import('../views/TalkAdd.vue'),
        meta: {
          title: '新增谈话'
        }
      },
      {
        path: 'talk-detail/:id',
        name: 'TalkDetail',
        component: () => import('../views/TalkDetail.vue'),
        meta: {
          title: '谈话详情'
        }
      },
      {
        path: 'home-visit-add',
        name: 'HomeVisitAdd',
        component: () => import('../views/HomeVisitAdd.vue'),
        meta: {
          title: '新增家访'
        }
      },
      {
        path: 'home-visit-list',
        name: 'HomeVisitList',
        component: () => import('../views/HomeVisitList.vue'),
        meta: {
          title: '家访记录'
        }
      },
      {
        path: 'admin-mgmt',
        name: 'AdminManagement',
        component: () => import('../views/AdminManagement.vue'),
        meta: {
          title: '人事调动与管控'
        }
      },
      {
        path: 'activity-tasks',
        name: 'ActivityTaskList',
        component: () => import('../views/ActivityTaskList.vue'),
        meta: {
          title: '活动任务'
        }
      },
      {
        path: 'activity-task-create',
        name: 'ActivityTaskCreate',
        component: () => import('../views/ActivityTaskCreate.vue'),
        meta: {
          title: '创建活动任务'
        }
      },
      {
        path: 'activity-record-submit/:taskId',
        name: 'ActivityRecordSubmit',
        component: () => import('../views/ActivityRecordSubmit.vue'),
        meta: {
          title: '填写活动记录'
        }
      },
      {
        path: 'activity-task-detail/:id',
        name: 'ActivityTaskDetail',
        component: () => import('../views/ActivityTaskDetail.vue'),
        meta: {
          title: '任务详情'
        }
      },
      {
        path: 'activity-record-detail/:id',
        name: 'ActivityRecordDetail',
        component: () => import('../views/ActivityRecordDetail.vue'),
        meta: {
          title: '活动记录详情'
        }
      },
      {
        path: 'ai-center',
        name: 'AICenter',
        component: () => import('../views/AICenter.vue'),
        meta: { title: 'AI预警中心' }
      },
      {
        path: 'data-fusion',
        name: 'DataFusion',
        component: () => import('../views/DataFusion.vue'),
        meta: { title: '数据融合中心' }
      },
      {
        path: 'risk-assessment',
        name: 'RiskAssessment',
        component: () => import('../views/RiskAssessment.vue'),
        meta: { title: '风险评估详情' }
      },
      {
        path: 'ai-agents',
        name: 'AIAgents',
        component: () => import('../views/AIAgents.vue'),
        meta: { title: '智能体应用' }
      },
      {
        path: 'major-event-report',
        name: 'MajorEventReport',
        component: () => import('../views/MajorEventReport.vue'),
        meta: { title: '重大事项申报' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHashHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  if (to.path === '/login') {
    next()
  } else {
    if (token) {
      // 权限路由守卫
      const user = getCurrentUser()
      if (user) {
        if (to.path === '/global-dashboard' && !canViewGlobalDashboard(user)) {
          next('/')
          return
        }
        if (to.path === '/admin-mgmt' && !canManagePersonnel(user)) {
          next('/')
          return
        }
        // 统计报表已合并到管理看板，根据角色重定向
        if (to.path === '/activity-task-create' && !canPublishActivity(user)) {
          next('/activity-tasks')
          return
        }
        if ((to.path === '/activity-tasks' || to.path.startsWith('/activity-task') || to.path.startsWith('/activity-record')) && !canViewActivityTasks(user)) {
          next('/')
          return
        }
      }

      // 如果访问的是根路径，且不是从应用内导航过来的（刷新页面或直接访问），根据角色重定向
      if (to.path === '/' && to.name === 'OrganizationChart' && (from.path === '/login' || from.path === '/')) {
        const user = getCurrentUser()
        if (user) {
          // 系统管理员、单位领导 → 全局管理看板
          if (canViewGlobalDashboard(user)) {
            next('/global-dashboard')
            return
          }
          // 部门领导 → 部门管理看板
          if (isDepartmentHead(user)) {
            const deptId = user.department?.id
            const deptName = user.department?.deptName || ''
            if (deptId) {
              next({ path: `/organization-stats/${deptId}`, query: { name: deptName } })
              return
            }
          }
        }
      }
      next()
    } else {
      next('/login')
    }
  }
})

export default router
