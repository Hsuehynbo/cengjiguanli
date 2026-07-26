<template>
  <div class="layout">
    <!-- 侧边栏 -->
    <div class="sidebar" :class="{ collapsed }">
      <div class="logo">
        <div class="logo-icon-box">
          <SafetyCertificateOutlined style="font-size: 22px" />
        </div>
        <h1 v-show="!collapsed">层级管理系统</h1>
      </div>

      <div class="menu">
        <div class="menu-divider" v-show="!collapsed"></div>
        <div class="menu-group-label" v-show="!collapsed">AI 智能预警</div>
        <div class="menu-item" :class="{ active: selectedKey === '/ai-center' }" @click="goToRoute('/ai-center')" :title="collapsed ? 'AI预警中心' : ''">
          <AlertOutlined class="menu-icon" style="font-size: 18px" />
          <span class="menu-text">AI预警中心</span>
        </div>
        <div class="menu-item" :class="{ active: selectedKey === '/data-fusion' }" @click="goToRoute('/data-fusion')" :title="collapsed ? '数据融合中心' : ''">
          <FundOutlined class="menu-icon" style="font-size: 18px" />
          <span class="menu-text">数据融合中心</span>
        </div>
        <div class="menu-item" :class="{ active: selectedKey === '/risk-assessment' }" @click="goToRoute('/risk-assessment')" :title="collapsed ? '风险评估详情' : ''">
          <DashboardOutlined class="menu-icon" style="font-size: 18px" />
          <span class="menu-text">风险评估详情</span>
        </div>
        <div class="menu-item" :class="{ active: selectedKey === '/ai-agents' }" @click="goToRoute('/ai-agents')" :title="collapsed ? '智能体应用' : ''">
          <RobotOutlined class="menu-icon" style="font-size: 18px" />
          <span class="menu-text">智能体应用</span>
        </div>

        <div class="menu-divider" v-show="!collapsed"></div>
        <div v-if="canViewUnitDashboard" class="menu-item" :class="{ active: isOrgStatsActive }" @click="goToUnitDashboard" :title="collapsed ? '部门详情' : ''">
          <BarChartOutlined class="menu-icon" style="font-size: 18px" />
          <span class="menu-text">部门详情</span>
        </div>
        <div v-if="canViewGlobalDashboard" class="menu-item" :class="{ active: selectedKey === '/global-dashboard' }" @click="goToRoute('/global-dashboard')" :title="collapsed ? '全局总览' : ''">
          <DashboardOutlined class="menu-icon" style="font-size: 18px" />
          <span class="menu-text">全局总览</span>
        </div>
        <div v-if="hasSubordinates" class="menu-item" :class="{ active: selectedKey === '/' }" @click="goToRoute('/')" :title="collapsed ? '组织架构图' : ''">
          <TeamOutlined class="menu-icon" style="font-size: 18px" />
          <span class="menu-text">组织架构图</span>
        </div>
        <div v-if="hasSubordinates" class="menu-item" :class="{ active: selectedKey === '/organization-list' }" @click="goToRoute('/organization-list')" :title="collapsed ? '组织架构列表' : ''">
          <UnorderedListOutlined class="menu-icon" style="font-size: 18px" />
          <span class="menu-text">组织架构列表</span>
        </div>

        <div class="menu-divider" v-show="!collapsed"></div>

        <div class="menu-item" :class="{ active: selectedKey === '/talk' }" @click="goToRoute('/talk')" :title="collapsed ? '谈话记录' : ''">
          <MessageOutlined class="menu-icon" style="font-size: 18px" />
          <span class="menu-text">谈话记录</span>
        </div>
        <div class="menu-item" :class="{ active: selectedKey === '/home-visit-list' }" @click="goToRoute('/home-visit-list')" :title="collapsed ? '家访记录' : ''">
          <HomeOutlined class="menu-icon" style="font-size: 18px" />
          <span class="menu-text">家访记录</span>
        </div>
        <div v-if="canViewActivityTasks" class="menu-item" :class="{ active: selectedKey === '/activity-tasks' }" @click="goToRoute('/activity-tasks')" :title="collapsed ? '活动任务' : ''">
          <CalendarOutlined class="menu-icon" style="font-size: 18px" />
          <span class="menu-text">活动任务</span>
        </div>

        <div class="menu-divider" v-show="!collapsed"></div>

        <div class="menu-item" :class="{ active: selectedKey === '/profile' }" @click="goToRoute('/profile')" :title="collapsed ? '个人信息' : ''">
          <UserOutlined class="menu-icon" style="font-size: 18px" />
          <span class="menu-text">个人信息</span>
        </div>
        <div class="menu-item" :class="{ active: selectedKey === '/major-event-report' }" @click="goToRoute('/major-event-report')" :title="collapsed ? '重大事项申报' : ''">
          <FileTextOutlined class="menu-icon" style="font-size: 18px" />
          <span class="menu-text">重大事项申报</span>
        </div>
        <div v-if="canManagePersonnel" class="menu-item" :class="{ active: selectedKey === '/admin-mgmt' }" @click="goToRoute('/admin-mgmt')" :title="collapsed ? '人事调动与管控' : ''">
          <SettingOutlined class="menu-icon" style="font-size: 18px" />
          <span class="menu-text">人事调动与管控</span>
        </div>
      </div>

      <div class="sidebar-footer">
        <div class="sidebar-toggle" @click="collapsed = !collapsed" :title="collapsed ? '展开菜单' : '收起菜单'">
          <MenuUnfoldOutlined v-if="collapsed" style="font-size: 16px" />
          <MenuFoldOutlined v-else style="font-size: 16px" />
          <span v-show="!collapsed" class="toggle-text">收起</span>
        </div>
      </div>
    </div>

    <!-- 主内容区 -->
    <div class="content">
      <div class="header">
        <div class="header-left">
          <h2>{{ pageTitle }}</h2>
        </div>
        <div class="header-right">
          <a-input-search
            v-model:value="globalSearchKey"
            placeholder="搜索人员..."
            style="width: 220px"
            allow-clear
            @search="handleGlobalSearch"
            @pressEnter="handleGlobalSearch"
          />
          <div class="header-divider"></div>
          <!-- 主题切换 -->
          <div class="theme-toggle" @click="toggleTheme" :title="isDark ? '切换浅色模式' : '切换深色模式'">
            <BulbOutlined style="font-size: 18px" />
          </div>
          <!-- 通知 -->
          <a-popover placement="bottomRight" trigger="click" v-model:open="notifVisible">
            <template #content>
              <div class="notif-panel">
                <div class="notif-header">
                  <span style="font-weight: 600">消息通知</span>
                  <a-button type="link" size="small" @click="handleMarkAllRead">全部已读</a-button>
                </div>
                <div class="notif-list">
                  <div v-if="notifications.length === 0" class="notif-empty">暂无通知</div>
                  <div v-for="n in notifications" :key="n.id" class="notif-item" :class="{ unread: !n.isRead }" @click="handleReadNotif(n)">
                    <div class="notif-title">{{ n.title }}</div>
                    <div class="notif-content">{{ n.content }}</div>
                    <div class="notif-time">{{ n.createTime }}</div>
                  </div>
                </div>
              </div>
            </template>
            <a-badge :count="unreadCount" :offset="[-2, 2]">
              <div class="notif-bell">
                <BellOutlined style="font-size: 18px" />
              </div>
            </a-badge>
          </a-popover>
          <div class="header-divider"></div>
          <!-- 用户信息 -->
          <div class="user-info">
            <div class="user-avatar">{{ user?.name?.charAt(0) || 'U' }}</div>
            <span class="user-name">{{ user?.name || '用户' }}</span>
          </div>
          <div class="header-divider"></div>
          <!-- 退出登录 -->
          <div class="header-logout" @click="handleLogout" title="退出登录">
            <LogoutOutlined style="font-size: 16px" />
          </div>
        </div>
      </div>

      <div class="main">
        <router-view :key="$route.fullPath" />
      </div>
    </div>

    <!-- 全局搜索抽屉 -->
    <a-drawer
      v-model:open="searchDrawerVisible"
      title="搜索结果"
      placement="right"
      width="400"
    >
      <a-spin :spinning="searchLoading">
        <div v-if="searchResults.length === 0 && !searchLoading" style="text-align: center; color: var(--text-muted); padding: 40px 0">
          {{ globalSearchKey ? '未找到匹配结果' : '请输入搜索关键词' }}
        </div>
        <div v-for="item in searchResults" :key="item.id" class="search-result-item" @click="goToSearchResult(item)">
          <div class="search-result-title">{{ item.title }}</div>
          <div class="search-result-desc">{{ item.desc }}</div>
        </div>
      </a-spin>
    </a-drawer>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import {
  SafetyCertificateOutlined,
  BarChartOutlined,
  DashboardOutlined,
  TeamOutlined,
  UnorderedListOutlined,
  MessageOutlined,
  HomeOutlined,
  CalendarOutlined,
  UserOutlined,
  SettingOutlined,
  MenuUnfoldOutlined,
  MenuFoldOutlined,
  LogoutOutlined,
  BellOutlined,
  BulbOutlined,
  RobotOutlined,
  AlertOutlined,
  ApiOutlined,
  FundOutlined,
  FileTextOutlined
} from '@ant-design/icons-vue'
import { getCurrentUser, clearAuth } from '../utils/auth'
import { isBureauLeader as isBureauLeaderFn, isGlobalAdmin as isGlobalAdminFn, isAdmin as isAdminFn, isUnitHead as isUnitHeadFn, isDepartmentHead as isDepartmentHeadFn, canViewGlobalDashboard as canViewGlobalDashboardFn, canManagePersonnel as canManagePersonnelFn, hasSubordinates as hasSubordinatesFn, canViewActivityTasks as canViewActivityTasksFn } from '../utils/constants'
import axios from '../utils/axios'

const router = useRouter()
const route = useRoute()
const selectedKey = ref('/')
const pageTitle = ref('组织架构')
const collapsed = ref(localStorage.getItem('sidebar_collapsed') === 'true')

watch(collapsed, (val) => {
  localStorage.setItem('sidebar_collapsed', val)
})

// ========== 主题 ==========
const isDark = ref(document.documentElement.getAttribute('data-theme') === 'dark')

const toggleTheme = () => {
  isDark.value = !isDark.value
  const mode = isDark.value ? 'dark' : 'light'
  document.documentElement.setAttribute('data-theme', mode)
  localStorage.setItem('theme-mode', mode)
}

// 监听主题变化
const themeObserver = new MutationObserver(() => {
  isDark.value = document.documentElement.getAttribute('data-theme') === 'dark'
})
themeObserver.observe(document.documentElement, { attributes: true, attributeFilter: ['data-theme'] })

onUnmounted(() => themeObserver.disconnect())

// ========== 用户权限 ==========
const user = ref(getCurrentUser())
const isBureauLeader = computed(() => isBureauLeaderFn(user.value))
const isAdmin = computed(() => isAdminFn(user.value))
const isGlobalAdmin = computed(() => isGlobalAdminFn(user.value))
const isUnitHead = computed(() => isUnitHeadFn(user.value))
const isDepartmentHead = computed(() => isDepartmentHeadFn(user.value))
const isAdminUnit = computed(() => user.value?.role === 'ADMIN_UNIT')
const canViewGlobalDashboard = computed(() => canViewGlobalDashboardFn(user.value))
const canViewUnitDashboard = computed(() => isDepartmentHead.value && !canViewGlobalDashboard.value)
const canManagePersonnel = computed(() => canManagePersonnelFn(user.value))
const hasSubordinates = computed(() => hasSubordinatesFn(user.value))
const canViewActivityTasks = computed(() => canViewActivityTasksFn(user.value))

const isOrgStatsActive = computed(() => route.path.startsWith('/organization-stats'))

// ========== 通知 ==========
const notifications = ref([])
const unreadCount = ref(0)
const notifVisible = ref(false)

const fetchNotifications = async () => {
  try {
    const [countRes, listRes] = await Promise.all([
      axios.get('/api/notifications/unread-count'),
      axios.get('/api/notifications/unread')
    ])
    unreadCount.value = countRes.count || 0
    notifications.value = listRes || []
  } catch (e) { /* ignore */ }
}

const handleReadNotif = async (n) => {
  if (!n.isRead) {
    try {
      await axios.put(`/api/notifications/${n.id}/read`)
      n.isRead = true
      unreadCount.value = Math.max(0, unreadCount.value - 1)
    } catch (e) { /* ignore */ }
  }
  if (n.relatedId) {
    notifVisible.value = false
    router.push(`/user-detail?jobNo=${n.relatedId}`)
  }
}

const handleMarkAllRead = async () => {
  try {
    await axios.put('/api/notifications/read-all')
    notifications.value.forEach(n => n.isRead = true)
    unreadCount.value = 0
  } catch (e) { /* ignore */ }
}

// ========== 搜索 ==========
const globalSearchKey = ref('')
const searchResults = ref([])
const searchDrawerVisible = ref(false)
const searchLoading = ref(false)

const handleGlobalSearch = async () => {
  const keyword = globalSearchKey.value?.trim()
  if (!keyword) return
  searchLoading.value = true
  searchDrawerVisible.value = true
  searchResults.value = []
  try {
    const users = await axios.get('/api/organization/search', { params: { keyword } })
    searchResults.value = users.slice(0, 20).map(u => ({
      type: 'user', id: u.jobNo, title: u.name,
      desc: `${u.deptName || ''} · ${u.position || ''} · ${u.jobNo}`,
      jobNo: u.jobNo
    }))
  } catch (e) { /* ignore */ }
  finally { searchLoading.value = false }
}

const goToSearchResult = (item) => {
  searchDrawerVisible.value = false
  if (item.type === 'user') router.push(`/user-detail?jobNo=${item.jobNo}`)
}

// ========== 导航 ==========
const goToRoute = (path) => {
  if (route.path !== path) router.push(path).catch(() => {})
}

const goToUnitDashboard = () => {
  const u = user.value
  const deptId = u.department?.id
  const deptName = u.department?.deptName || ''
  if (deptId && !route.path.startsWith('/organization-stats')) {
    router.push(`/organization-stats/${deptId}?name=${encodeURIComponent(deptName)}`).catch(() => {})
  }
}

const handleLogout = () => {
  clearAuth()
  router.push('/login')
}

const updatePageInfo = () => {
  user.value = getCurrentUser()
  selectedKey.value = route.path
  pageTitle.value = route.meta.title || '组织架构'
}

watch(() => route.path, updatePageInfo)

let notifTimer = null
onMounted(() => {
  updatePageInfo()
  fetchNotifications()
  notifTimer = setInterval(fetchNotifications, 60000)
})
onUnmounted(() => {
  if (notifTimer) clearInterval(notifTimer)
})
</script>

<style scoped>
.layout {
  display: flex;
  min-height: 100vh;
  background: var(--bg-page);
  width: 100%;
}

/* ========== 侧边栏 ========== */
.sidebar {
  width: var(--sidebar-width);
  flex: 0 0 var(--sidebar-width);
  background: var(--bg-sidebar);
  border-right: 1px solid var(--border-color);
  display: flex;
  flex-direction: column;
  transition: width 0.3s ease, flex 0.3s ease;
  overflow: hidden;
  position: relative;
  z-index: 100;
}
.sidebar.collapsed {
  width: var(--sidebar-collapsed);
  flex: 0 0 var(--sidebar-collapsed);
}

.logo {
  padding: 20px 16px;
  display: flex;
  align-items: center;
  gap: 10px;
  border-bottom: 1px solid var(--border-light);
  min-height: 64px;
}
.logo-icon-box {
  width: 34px;
  height: 34px;
  border-radius: 10px;
  background: var(--accent);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.logo h1 {
  font-size: 15px;
  margin: 0;
  font-weight: 700;
  color: var(--text-title);
  white-space: nowrap;
}
.sidebar.collapsed .logo {
  justify-content: center;
  padding: 20px 0;
}
.sidebar.collapsed .logo h1 {
  display: none;
}

.menu {
  flex: 1;
  padding: 12px 8px;
  overflow-x: hidden;
  overflow-y: auto;
}

.menu-divider {
  height: 1px;
  background: var(--border-light);
  margin: 8px 12px;
}
.menu-group-label {
  font-size: 11px;
  font-weight: 600;
  color: var(--text-muted);
  padding: 4px 14px 2px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.menu-item {
  display: flex;
  align-items: center;
  padding: 10px 14px;
  margin: 2px 0;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s ease;
  white-space: nowrap;
  color: var(--text-secondary);
  font-size: 14px;
}
.menu-item:hover {
  background: var(--accent-light);
  color: var(--accent);
}
.menu-item.active {
  background: var(--accent-light);
  color: var(--accent);
  font-weight: 600;
}
.menu-item.active .menu-icon {
  color: var(--accent);
}

.menu-icon {
  margin-right: 10px;
  flex-shrink: 0;
  color: var(--text-muted);
}
.menu-item:hover .menu-icon,
.menu-item.active .menu-icon {
  color: var(--accent);
}

.sidebar.collapsed .menu-item {
  justify-content: center;
  padding: 10px 0;
}
.sidebar.collapsed .menu-icon {
  margin-right: 0;
}
.sidebar.collapsed .menu-text {
  display: none;
}
.sidebar.collapsed .menu-divider {
  margin: 8px 6px;
}

.sidebar-footer {
  border-top: 1px solid var(--border-light);
}

.sidebar-toggle {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 10px 14px;
  cursor: pointer;
  color: var(--text-muted);
  transition: all 0.2s;
  gap: 8px;
  font-size: 13px;
}
.sidebar-toggle:hover {
  color: var(--accent);
  background: var(--accent-light);
}
.sidebar.collapsed .sidebar-toggle {
  padding: 10px 0;
  justify-content: center;
}
.sidebar.collapsed .toggle-text {
  display: none;
}

/* ========== 内容区 ========== */
.content {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  background: var(--bg-page);
}

.header {
  padding: 0 24px;
  height: var(--header-height);
  background: var(--bg-header);
  border-bottom: 1px solid var(--border-color);
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-shrink: 0;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.header h2 {
  font-size: 17px;
  margin: 0;
  font-weight: 600;
  color: var(--text-title);
}

.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.header-divider {
  width: 1px;
  height: 20px;
  background: var(--border-color);
}

.theme-toggle {
  width: 36px;
  height: 36px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  color: var(--text-secondary);
  transition: all 0.2s;
}
.theme-toggle:hover {
  background: var(--accent-light);
  color: var(--accent);
}

.notif-bell {
  width: 36px;
  height: 36px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  color: var(--text-secondary);
  transition: all 0.2s;
}
.notif-bell:hover {
  background: var(--accent-light);
  color: var(--accent);
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: default;
}
.user-avatar {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  background: var(--accent);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: 600;
}
.user-name {
  font-size: 14px;
  color: var(--text-primary);
  font-weight: 500;
}

.header-logout {
  width: 36px;
  height: 36px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  color: var(--text-secondary);
  transition: all 0.2s;
}
.header-logout:hover {
  background: var(--danger-light);
  color: var(--danger);
}

.main {
  flex: 1;
  min-width: 0;
  padding: 24px;
  overflow: auto;
}

/* ========== 通知面板 ========== */
.notif-panel { width: 320px; max-height: 400px; }
.notif-header { display: flex; justify-content: space-between; align-items: center; padding-bottom: 8px; border-bottom: 1px solid var(--border-light); margin-bottom: 8px; }
.notif-list { max-height: 300px; overflow-y: auto; }
.notif-empty { text-align: center; color: var(--text-muted); padding: 20px 0; }
.notif-item { padding: 10px 8px; cursor: pointer; border-radius: 6px; transition: background 0.2s; }
.notif-item:hover { background: var(--bg-table-row-hover); }
.notif-item.unread { background: var(--accent-light); }
.notif-title { font-weight: 500; margin-bottom: 4px; font-size: 13px; color: var(--text-primary); }
.notif-content { font-size: 12px; color: var(--text-muted); margin-bottom: 4px; }
.notif-time { font-size: 11px; color: var(--text-muted); }

/* ========== 搜索结果 ========== */
.search-result-item { padding: 12px 16px; border-bottom: 1px solid var(--border-light); cursor: pointer; transition: background 0.2s; }
.search-result-item:hover { background: var(--accent-light); }
.search-result-title { font-size: 14px; font-weight: 500; color: var(--text-primary); }
.search-result-desc { font-size: 12px; color: var(--text-muted); margin-top: 4px; }
</style>
