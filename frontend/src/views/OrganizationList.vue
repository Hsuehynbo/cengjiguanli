<template>
  <div class="org-list-container">
    <!-- 顶部信息栏 -->
    <div class="top-bar">
      <div class="title-section">
        <span class="main-title">组织架构列表</span>
        <span class="sub-title">部门人员管理</span>
      </div>
      <div class="info-section">
        <ClockDisplay />
      </div>
    </div>

    <!-- 主体：左右分栏 -->
    <div class="main-content">
      <!-- 左面板：部门列表 -->
      <div class="left-panel">
        <div class="panel-header">
          <span class="panel-title">部门列表</span>
          <span class="panel-subtitle">{{ departments.length }} 个部门</span>
        </div>
        <div class="dept-search">
          <a-input
            v-model:value="searchDept"
            placeholder="搜索部门..."
            allow-clear
            size="small"
          />
        </div>
        <div class="dept-list">
          <div
            v-for="dept in filteredDepartments"
            :key="dept.id"
            class="dept-item"
            :class="{ active: dept.id === selectedDeptId }"
            @click="selectDepartment(dept)"
          >
            <span class="dept-icon">🏢</span>
            <div class="dept-info">
              <span class="dept-name">{{ dept.name }}</span>
              <span class="dept-count">{{ dept.userCount }} 人</span>
            </div>
            <span class="dept-arrow">›</span>
          </div>
          <a-empty v-if="filteredDepartments.length === 0 && !departmentsLoading" description="无匹配部门" />
        </div>
      </div>

      <!-- 右面板：人员卡片 -->
      <div class="right-panel">
        <div class="panel-header">
          <div class="panel-title-row">
            <span v-if="selectedDeptName" class="panel-title">{{ selectedDeptName }}</span>
            <span v-else class="panel-title">人员信息</span>
            <span v-if="users.length" class="panel-subtitle">共 {{ filteredUsers.length }} 人</span>
          </div>
          <a-input
            v-if="selectedDeptId"
            v-model:value="searchUser"
            placeholder="搜索姓名/工号/职位..."
            allow-clear
            class="user-search"
          />
        </div>

        <div class="user-content">
          <a-spin :spinning="usersLoading">
            <!-- 未选部门 -->
            <div v-if="!selectedDeptId" class="empty-hint">
              <span class="empty-icon">👈</span>
              <span>请从左侧列表中选择一个部门查看人员信息</span>
            </div>

            <!-- 部门无人员 -->
            <div v-else-if="!usersLoading && users.length === 0" class="empty-hint">
              <span class="empty-icon">📭</span>
              <span>该部门暂无人员</span>
            </div>

            <!-- 搜索结果为空 -->
            <div v-else-if="!usersLoading && users.length > 0 && filteredUsers.length === 0" class="empty-hint">
              <span class="empty-icon">🔍</span>
              <span>无匹配人员</span>
            </div>

            <!-- 人员卡片网格 -->
            <div v-else class="user-card-grid">
              <div
                v-for="user in filteredUsers"
                :key="user.jobNo"
                class="user-card"
                :class="{ 'key-personnel-card': user.isKeyPersonnel }"
                @click="goToUserDetail(user.jobNo)"
              >
                <div class="card-accent"></div>
                <div class="card-body">
                  <div class="card-avatar">
                    <img v-if="user.avatar" :src="user.avatar" alt="" />
                    <span v-else class="avatar-placeholder">{{ (user.name || '?').charAt(0) }}</span>
                  </div>
                  <div class="card-info">
                    <div class="card-name">{{ user.name }}</div>
                    <div class="card-jobno">{{ user.jobNo }}</div>
                    <div class="card-position">{{ user.position }}</div>
                    <div class="card-phone" v-if="user.phone">{{ user.phone }}</div>
                  </div>
                </div>
                <div class="card-tags">
                  <a-tag v-if="user.isKeyPersonnel" color="#ef4444">重点人员</a-tag>
                  <a-tag v-else :color="riskColorMap[user.riskLevel] || riskColorMap.NORMAL">
                    {{ riskLabelMap[user.riskLevel] || riskLabelMap.NORMAL }}
                  </a-tag>
                </div>
              </div>
            </div>
          </a-spin>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import axios from '../utils/axios'
import { message } from 'ant-design-vue'
import ClockDisplay from '../components/ClockDisplay.vue'
import { RISK_LEVEL_MAP } from '../utils/constants'

const router = useRouter()

// 部门列表
const departments = ref([])
const departmentsLoading = ref(false)
const selectedDeptId = ref(null)
const selectedDeptName = ref('')
const searchDept = ref('')

const filteredDepartments = computed(() => {
  if (!searchDept.value) return departments.value
  const kw = searchDept.value.toLowerCase()
  return departments.value.filter(d => d.name.toLowerCase().includes(kw))
})

const fetchDepartments = async () => {
  departmentsLoading.value = true
  try {
    const res = await axios.get('/api/organization/departments')
    departments.value = res || []
  } catch (error) {
    // ignore
  } finally {
    departmentsLoading.value = false
  }
}

const selectDepartment = (dept) => {
  selectedDeptId.value = dept.id
  selectedDeptName.value = dept.name
  searchUser.value = ''
  fetchUsers(dept.id)
}

// 人员列表
const users = ref([])
const usersLoading = ref(false)
const searchUser = ref('')

const filteredUsers = computed(() => {
  if (!searchUser.value) return users.value
  const kw = searchUser.value.toLowerCase()
  return users.value.filter(u =>
    (u.name || '').toLowerCase().includes(kw) ||
    (u.jobNo || '').toLowerCase().includes(kw) ||
    (u.position || '').toLowerCase().includes(kw)
  )
})

const fetchUsers = async (deptId) => {
  usersLoading.value = true
  users.value = []
  try {
    const res = await axios.get(`/api/organization/children/dept_${deptId}`)
    users.value = res || []
  } catch (error) {
    message.error('获取部门人员失败')
  } finally {
    usersLoading.value = false
  }
}

const goToUserDetail = (jobNo) => {
  router.push({ name: 'UserDetail', query: { jobNo } })
}

// 风险等级映射
const riskColorMap = {
  NORMAL: RISK_LEVEL_MAP.NORMAL.color,
  KEY: RISK_LEVEL_MAP.KEY.color,
  RISK: RISK_LEVEL_MAP.RISK.color,
  ATTENTION: RISK_LEVEL_MAP.ATTENTION.color
}
const riskLabelMap = {
  NORMAL: RISK_LEVEL_MAP.NORMAL.label,
  KEY: RISK_LEVEL_MAP.KEY.label,
  RISK: RISK_LEVEL_MAP.RISK.label,
  ATTENTION: RISK_LEVEL_MAP.ATTENTION.label
}

onMounted(() => {
  fetchDepartments()
})

onUnmounted(() => {
})
</script>

<style scoped>
.org-list-container {
  min-height: 100vh;
  background: var(--bg-page);
  padding: 20px;
  /* font-family inherited from global */
}

/* 顶部信息栏 */
.top-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 24px;
  background: var(--bg-card);
  border-radius: 12px;
  border: 1px solid var(--border-color);
  margin-bottom: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.title-section {
  display: flex;
  flex-direction: column;
}

.main-title {
  font-size: 24px;
  font-weight: 700;
  color: var(--text-title);
}

.sub-title {
  font-size: 14px;
  color: var(--text-muted);
  margin-top: 4px;
}

.info-section {
  display: flex;
  gap: 32px;
}

.time-display {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
}

.time-display .date {
  font-size: 12px;
  color: var(--text-muted);
}

.time-display .time {
  font-size: 20px;
  font-weight: 600;
  color: var(--accent);
  font-family: var(--font-mono);
}

/* 主体左右分栏 */
.main-content {
  display: flex;
  gap: 20px;
  height: calc(100vh - 180px);
}

/* 左面板 */
.left-panel {
  width: 280px;
  min-width: 280px;
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.left-panel .panel-header {
  padding: 16px 20px 12px;
  border-bottom: 1px solid var(--border-light);
  display: flex;
  justify-content: space-between;
  align-items: baseline;
}

.dept-search {
  padding: 12px 16px;
  border-bottom: 1px solid var(--border-light);
}


.dept-list {
  flex: 1;
  overflow-y: auto;
  padding: 4px 0;
}

.dept-list::-webkit-scrollbar {
  width: 6px;
}
.dept-list::-webkit-scrollbar-thumb {
  background: var(--border-color);
  border-radius: 3px;
}
.dept-list::-webkit-scrollbar-track {
  background: transparent;
}

.dept-item {
  display: flex;
  align-items: center;
  padding: 10px 16px;
  cursor: pointer;
  transition: all 0.2s;
  border-left: 3px solid transparent;
  margin: 2px 0;
}

.dept-item:hover {
  background: var(--accent-light);
}

.dept-item.active {
  background: var(--accent-light);
  border-left-color: var(--accent);
}

.dept-item .dept-icon {
  font-size: 18px;
  margin-right: 10px;
}

.dept-item .dept-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.dept-item .dept-name {
  color: var(--text-primary);
  font-size: 14px;
  font-weight: 500;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.dept-item .dept-count {
  color: var(--text-muted);
  font-size: 12px;
}

.dept-item .dept-arrow {
  color: var(--text-muted);
  font-size: 18px;
  transition: color 0.2s;
}

.dept-item.active .dept-arrow {
  color: var(--accent);
}

/* 右面板 */
.right-panel {
  flex: 1;
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.right-panel .panel-header {
  padding: 16px 24px 12px;
  border-bottom: 1px solid var(--border-light);
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
}

.panel-title-row {
  display: flex;
  align-items: baseline;
  gap: 12px;
}

.panel-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-title);
}

.panel-subtitle {
  font-size: 13px;
  color: var(--text-muted);
}

.user-search {
  width: 220px;
}

.user-content {
  flex: 1;
  overflow-y: auto;
  padding: 20px 24px;
}

.user-content::-webkit-scrollbar {
  width: 6px;
}
.user-content::-webkit-scrollbar-thumb {
  background: var(--border-color);
  border-radius: 3px;
}
.user-content::-webkit-scrollbar-track {
  background: transparent;
}

/* 空状态 */
.empty-hint {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  padding: 80px 0;
  color: var(--text-muted);
  font-size: 15px;
}

.empty-hint .empty-icon {
  font-size: 40px;
}

/* 人员卡片网格 */
.user-card-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 16px;
  align-content: start;
}

/* 单张人员卡片 */
.user-card {
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.25s;
  position: relative;
  overflow: hidden;
  padding: 20px;
}

.user-card .card-accent {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 2px;
  background: linear-gradient(90deg, var(--accent), transparent);
}

.user-card:hover {
  transform: translateY(-4px);
  border-color: var(--accent);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
}

.user-card:hover .card-accent {
  height: 3px;
  background: linear-gradient(90deg, var(--accent), var(--accent-light), transparent);
}

.user-card.key-personnel-card {
  border-color: rgba(239, 68, 68, 0.3);
}
.user-card.key-personnel-card .card-accent {
  background: linear-gradient(90deg, #ef4444, transparent);
}

.card-body {
  display: flex;
  align-items: center;
  gap: 14px;
  margin-bottom: 12px;
}

.card-avatar {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  overflow: hidden;
  flex-shrink: 0;
  border: 2px solid var(--border-color);
}

.card-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.card-avatar .avatar-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--accent-light);
  color: var(--accent);
  font-size: 20px;
  font-weight: 700;
}

.card-info {
  flex: 1;
  min-width: 0;
}

.card-name {
  color: var(--text-title);
  font-size: 16px;
  font-weight: 600;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.card-jobno {
  color: var(--text-secondary);
  font-size: 12px;
  margin-top: 2px;
}

.card-position {
  color: var(--text-secondary);
  font-size: 13px;
  margin-top: 2px;
}

.card-phone {
  color: var(--text-muted);
  font-size: 12px;
  margin-top: 2px;
}

.card-tags {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}

/* 响应式 */
@media (max-width: 768px) {
  .main-content {
    flex-direction: column;
    height: auto;
  }

  .left-panel {
    width: 100%;
    min-width: unset;
    max-height: 300px;
  }

  .right-panel {
    min-height: 400px;
  }

  .user-card-grid {
    grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  }

  .top-bar {
    flex-direction: column;
    gap: 16px;
  }

  .time-display {
    align-items: center;
  }
}
</style>
