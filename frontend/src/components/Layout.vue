<template>
  <div class="layout">
    <div class="sidebar" :class="{ collapsed }">
      <div class="logo">
        <h1 v-show="!collapsed">云和县公安局层级管理</h1>
      </div>

      <div class="menu">
        <div v-if="canViewUnitDashboard" class="menu-item" :class="{ active: isOrgStatsActive }" @click="goToUnitDashboard" :title="collapsed ? '部门详情' : ''">
          <Icon icon="ri:bar-chart-2-fill" class="menu-icon" width="18" height="18" />
          <span class="menu-text">部门详情</span>
        </div>
        <div v-if="canViewGlobalDashboard" class="menu-item" :class="{ active: selectedKey === '/global-dashboard' }" @click="goToRoute('/global-dashboard')" :title="collapsed ? '全局总览' : ''">
          <Icon icon="ri:dashboard-3-fill" class="menu-icon" width="18" height="18" />
          <span class="menu-text">全局总览</span>
        </div>
        <div v-if="hasSubordinates" class="menu-item" :class="{ active: selectedKey === '/' }" @click="goToRoute('/')" :title="collapsed ? '组织架构图' : ''">
          <Icon icon="ri:group-fill" class="menu-icon" width="18" height="18" />
          <span class="menu-text">组织架构图</span>
        </div>
        <div v-if="hasSubordinates" class="menu-item" :class="{ active: selectedKey === '/organization-list' }" @click="goToRoute('/organization-list')" :title="collapsed ? '组织架构列表' : ''">
          <Icon icon="ri:list-check-2" class="menu-icon" width="18" height="18" />
          <span class="menu-text">组织架构列表</span>
        </div>
        <div class="menu-item" :class="{ active: selectedKey === '/talk' }" @click="goToRoute('/talk')" :title="collapsed ? '谈话记录' : ''">
          <Icon icon="ri:chat-3-fill" class="menu-icon" width="18" height="18" />
          <span class="menu-text">谈话记录</span>
        </div>
        <div class="menu-item" :class="{ active: selectedKey === '/home-visit-list' }" @click="goToRoute('/home-visit-list')" :title="collapsed ? '家访记录' : ''">
          <Icon icon="ri:home-heart-fill" class="menu-icon" width="18" height="18" />
          <span class="menu-text">家访记录</span>
        </div>
        <div v-if="canViewActivityTasks" class="menu-item" :class="{ active: selectedKey === '/activity-tasks' }" @click="goToRoute('/activity-tasks')" :title="collapsed ? '活动任务' : ''">
          <Icon icon="ri:calendar-schedule-fill" class="menu-icon" width="18" height="18" />
          <span class="menu-text">活动任务</span>
        </div>
        <div class="menu-item" :class="{ active: selectedKey === '/profile' }" @click="goToRoute('/profile')" :title="collapsed ? '个人信息' : ''">
          <Icon icon="ri:user-3-fill" class="menu-icon" width="18" height="18" />
          <span class="menu-text">个人信息</span>
        </div>
        <div v-if="canManagePersonnel" class="menu-item" :class="{ active: selectedKey === '/admin-mgmt' }" @click="goToRoute('/admin-mgmt')" :title="collapsed ? '人事调动与管控' : ''">
          <Icon icon="ri:settings-3-fill" class="menu-icon" width="18" height="18" />
          <span class="menu-text">人事调动与管控</span>
        </div>
      </div>
      <div class="sidebar-toggle" @click="collapsed = !collapsed">
        <Icon icon="ri:menu-fold-fill" v-if="!collapsed" width="16" height="16" />
        <Icon icon="ri:menu-unfold-fill" v-else width="16" height="16" />
        <span v-show="!collapsed" class="toggle-text">收起菜单</span>
      </div>
      <div class="logout">
        <a-button type="primary" danger ghost block @click="handleLogout" :title="collapsed ? '退出登录' : ''">
          <Icon icon="ri:logout-box-r-fill" width="16" height="16" />
          <span v-show="!collapsed" style="margin-left: 6px">退出登录</span>
        </a-button>
      </div>
    </div>
    <div class="content">
      <div class="header">
        <div class="header-left">
          <Icon icon="ri:menu-fold-fill" v-if="!collapsed" class="collapse-btn" @click="collapsed = true" width="18" height="18" />
          <Icon icon="ri:menu-unfold-fill" v-else class="collapse-btn" @click="collapsed = false" width="18" height="18" />
          <h2>{{ pageTitle }}</h2>
        </div>
        <div class="header-right">
          <a-input-search
            v-model:value="globalSearchKey"
            placeholder="搜索人员..."
            style="width: 260px; margin-right: 16px"
            allow-clear
            @search="handleGlobalSearch"
            @pressEnter="handleGlobalSearch"
          />
          <a-popover placement="bottomRight" trigger="click" v-model:open="notifVisible">
            <template #content>
              <div class="notif-panel">
                <div class="notif-header">
                  <span>消息通知</span>
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
              <Icon icon="ri:notification-3-fill" class="notif-bell" width="20" height="20" />
            </a-badge>
          </a-popover>
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
        <div v-if="searchResults.length === 0 && !searchLoading" style="text-align: center; color: #999; padding: 40px 0">
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
import { ref, watch, onMounted, onUnmounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { Icon } from '@iconify/vue'
import { getCurrentUser, clearAuth, hasPermission } from '../utils/auth'
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

const isOrgStatsActive = computed(() => {
  return route.path.startsWith('/organization-stats')
})

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
  } catch (e) {
    // ignore
  }
}

const handleReadNotif = async (n) => {
  if (!n.isRead) {
    try {
      await axios.put(`/api/notifications/${n.id}/read`)
      n.isRead = true
      unreadCount.value = Math.max(0, unreadCount.value - 1)
    } catch (e) {
      // ignore
    }
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
  } catch (e) {
    // ignore
  }
}

// 全局搜索
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
      type: 'user',
      id: u.jobNo,
      title: u.name,
      desc: `${u.deptName || ''} · ${u.position || ''} · ${u.jobNo}`,
      jobNo: u.jobNo
    }))
  } catch (e) {
    // ignore
  } finally {
    searchLoading.value = false
  }
}

const goToSearchResult = (item) => {
  searchDrawerVisible.value = false
  if (item.type === 'user') {
    router.push(`/user-detail?jobNo=${item.jobNo}`)
  }
}

const goToRoute = (path) => {
  if (route.path === path) {
    return
  }
  router.push(path).catch(err => {
  })
}

const goToUnitDashboard = () => {
  const u = user.value
  const deptId = u.department?.id
  const deptName = u.department?.deptName || ''
  if (deptId) {
    const path = `/organization-stats/${deptId}?name=${encodeURIComponent(deptName)}`
    if (route.path.startsWith('/organization-stats')) {
      return
    }
    router.push(path).catch(err => {
      })
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

watch(
  () => route.path,
  () => {
    updatePageInfo()
  }
)

let notifTimer = null

onMounted(() => {
  updatePageInfo()
  fetchNotifications()
  notifTimer = setInterval(fetchNotifications, 60000)
})

onUnmounted(() => {
  if (notifTimer) {
    clearInterval(notifTimer)
    notifTimer = null
  }
})
</script>

<style scoped>
.layout {
  display: flex;
  min-height: 100vh;
  background: linear-gradient(135deg, #001529 0%, #0a1628 50%, #001529 100%);
  width: 100%;
}

.sidebar {
  width: 250px;
  flex: 0 0 250px;
  background: rgba(0, 21, 41, 0.85);
  backdrop-filter: blur(16px);
  -webkit-backdrop-filter: blur(16px);
  color: #fff;
  display: flex;
  flex-direction: column;
  transition: width 0.3s, flex 0.3s;
  overflow: hidden;
  position: relative;
  z-index: 100;
}

.sidebar.collapsed {
  width: 60px;
  flex: 0 0 60px;
}

.logo {
  padding: 24px 20px;
  border-bottom: 1px solid rgba(0, 212, 255, 0.08);
  white-space: nowrap;
  position: relative;
}

.logo::after {
  content: '';
  position: absolute;
  bottom: -1px;
  left: 20px;
  right: 20px;
  height: 1px;
  background: linear-gradient(90deg, transparent, rgba(0, 212, 255, 0.2), transparent);
}

.logo h1 {
  font-size: 16px;
  margin: 0;
  font-weight: 600;
  background: linear-gradient(90deg, #00d4ff, #00ffff);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.logo-icon {
  font-size: 16px;
  font-weight: 700;
  background: linear-gradient(90deg, #00d4ff, #00ffff);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  display: none;
}

.sidebar.collapsed .logo-icon {
  display: inline;
}

.sidebar.collapsed .logo h1 {
  display: none;
}

.menu {
  flex: 1;
  padding: 16px 0;
  overflow-x: hidden;
}

.menu-item {
  display: flex;
  align-items: center;
  padding: 13px 20px;
  margin: 2px 8px;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
  white-space: nowrap;
  position: relative;
}

.menu-item:hover {
  background: rgba(0, 212, 255, 0.06);
}

.menu-item.active {
  background: rgba(0, 212, 255, 0.1);
  color: #00d4ff;
  box-shadow: inset 3px 0 0 #00d4ff;
}

.menu-item.active .menu-icon {
  color: #00d4ff;
  filter: drop-shadow(0 0 4px rgba(0, 212, 255, 0.4));
}

.menu-icon {
  margin-right: 12px;
  flex-shrink: 0;
}

.sidebar.collapsed .menu-icon {
  margin-right: 0;
}

.sidebar.collapsed .menu-item {
  justify-content: center;
  padding: 13px 0;
  margin: 2px 6px;
}

.sidebar.collapsed .menu-item.active {
  box-shadow: inset 3px 0 0 #00d4ff;
}

.menu-text {
  display: inline;
}

.sidebar.collapsed .menu-text {
  display: none;
}

.logout {
  padding: 20px;
  border-top: 1px solid rgba(0, 212, 255, 0.08);
}

.sidebar.collapsed .logout {
  padding: 20px 10px;
}

.sidebar.collapsed .logout span {
  display: none;
}

.sidebar-toggle {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 12px 20px;
  cursor: pointer;
  border-top: 1px solid rgba(0, 212, 255, 0.08);
  color: #8892b0;
  transition: all 0.3s;
  gap: 8px;
}

.sidebar-toggle:hover {
  color: #00d4ff;
  background: rgba(0, 212, 255, 0.08);
}

.sidebar.collapsed .sidebar-toggle {
  padding: 12px 0;
  justify-content: center;
}

.sidebar.collapsed .toggle-text {
  display: none;
}

.content {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
}

.header {
  padding: 0 28px;
  height: 64px;
  line-height: 64px;
  background: rgba(0, 21, 41, 0.7);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border-bottom: 1px solid rgba(0, 212, 255, 0.08);
  display: flex;
  justify-content: space-between;
  align-items: center;
  position: relative;
}

.header::after {
  content: '';
  position: absolute;
  bottom: -1px;
  left: 0;
  right: 0;
  height: 1px;
  background: linear-gradient(90deg, transparent, rgba(0, 212, 255, 0.25), transparent);
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
  background: linear-gradient(90deg, #ccd6f6, #e6edf8);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.collapse-btn {
  color: #ccd6f6;
  cursor: pointer;
  transition: color 0.3s;
}

.collapse-btn:hover {
  color: #00d4ff;
}

.header-right {
  display: flex;
  align-items: center;
}

.header-right :deep(.ant-input-search) {
  transition: all 0.3s;
}

.header-right :deep(.ant-input-search:focus-within) {
  box-shadow: 0 0 16px rgba(0, 212, 255, 0.15);
  border-radius: 6px;
}

.notif-bell {
  color: #ccd6f6;
  cursor: pointer;
  transition: color 0.3s;
}

.notif-bell:hover {
  color: #00d4ff;
}

.notif-panel {
  width: 320px;
  max-height: 400px;
}

.notif-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 8px;
  border-bottom: 1px solid #f0f0f0;
  margin-bottom: 8px;
}

.notif-list {
  max-height: 300px;
  overflow-y: auto;
}

.notif-empty {
  text-align: center;
  color: #999;
  padding: 20px 0;
}

.notif-item {
  padding: 8px;
  cursor: pointer;
  border-radius: 4px;
  transition: background 0.2s;
}

.notif-item:hover {
  background: #f5f5f5;
}

.notif-item.unread {
  background: #e6f7ff;
}

.notif-title {
  font-weight: 500;
  margin-bottom: 4px;
}

.notif-content {
  font-size: 12px;
  color: #666;
  margin-bottom: 4px;
}

.notif-time {
  font-size: 11px;
  color: #999;
}

.search-result-item {
  padding: 12px 16px;
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;
  transition: background 0.2s;
}

.search-result-item:hover {
  background: #e6f7ff;
}

.search-result-title {
  font-size: 14px;
  font-weight: 500;
  color: #333;
}

.search-result-desc {
  font-size: 12px;
  color: #999;
  margin-top: 4px;
}

.main {
  flex: 1;
  min-width: 0;
  padding: 24px;
  overflow: auto;
  background: transparent;
  position: relative;
}

.main::before {
  content: '';
  position: fixed;
  inset: 0;
  background-image:
    linear-gradient(rgba(0, 212, 255, 0.045) 1px, transparent 1px),
    linear-gradient(90deg, rgba(0, 212, 255, 0.045) 1px, transparent 1px);
  background-size: 60px 60px;
  pointer-events: none;
  z-index: 0;
}

.main > * {
  position: relative;
  z-index: 1;
}
</style>
