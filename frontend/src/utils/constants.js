export const RISK_LEVEL_MAP = {
  NORMAL: { color: 'blue', label: '普通人员' },
  KEY: { color: 'red', label: '重点人员' },
  RISK: { color: 'gold', label: '风险人员' },
  ATTENTION: { color: 'green', label: '关注人员' }
}

export const TALK_TYPE_OPTIONS = ['日常沟通', '工作指导', '诫勉谈话', '提醒谈话', '廉政谈话']

export const VISIT_TYPE_OPTIONS = ['例行家访', '特殊家访', '慰问家访']

export function isBureauLeader(user) {
  const pos = user?.position || ''
  return pos.includes('局长')
}

export function isGlobalAdmin(user) {
  return user?.jobNo === 'admin' || user?.role === 'ADMIN_GLOBAL' || isBureauLeader(user)
}

export function isUnitHead(user) {
  const pos = user?.position || ''
  return (pos.includes('所长') || pos.includes('队长') || pos.includes('科长') || pos.includes('主任')) && !pos.startsWith('副')
}

export function isDepartmentHead(user) {
  return isUnitHead(user)
}

export function isAdmin(user) {
  return !isBureauLeader(user) && (user?.role === 'ADMIN_GLOBAL' || user?.role === 'ADMIN_UNIT' || user?.jobNo === 'admin')
}

// 权限感知函数 — 在角色之上叠加细粒度权限检查
export function canViewGlobalDashboard(user) {
  if (!user) return false
  if (user.jobNo === 'admin') return true
  if (user.role === 'ADMIN_GLOBAL') return true
  if (isBureauLeader(user)) return true
  const perms = user.permissions || []
  return perms.includes('GLOBAL_DASHBOARD')
}

export function canManagePersonnel(user) {
  if (!user) return false
  if (user.jobNo === 'admin') return true
  if (user.role === 'ADMIN_GLOBAL') return true
  const perms = user.permissions || []
  return perms.includes('PERSONNEL_MANAGE') || perms.includes('HIERARCHY_MANAGE')
}

export function canPublishActivity(user) {
  if (!user) return false
  if (user.jobNo === 'admin') return true
  if (user.role === 'ADMIN_GLOBAL') return true
  const perms = user.permissions || []
  return perms.includes('ACTIVITY_PUBLISH')
}

export function canManageHierarchy(user) {
  if (!user) return false
  if (user.jobNo === 'admin') return true
  if (user.role === 'ADMIN_GLOBAL') return true
  const perms = user.permissions || []
  return perms.includes('HIERARCHY_MANAGE')
}

// 活动任务页面可见性：管理角色可见，普通用户不可见
export function canViewActivityTasks(user) {
  if (!user) return false
  if (user.jobNo === 'admin' || user.role === 'ADMIN_GLOBAL' || user.role === 'ADMIN_UNIT') return true
  if (isBureauLeader(user) || isUnitHead(user)) return true
  const perms = user.permissions || []
  return perms.includes('ACTIVITY_PUBLISH')
}

// 是否有下级（控制组织架构图/列表的显示）
export function hasSubordinates(user) {
  if (!user) return false
  if (user.jobNo === 'admin') return true
  if (user.role === 'ADMIN_GLOBAL' || user.role === 'ADMIN_UNIT') return true
  if (isBureauLeader(user)) return true
  if (isUnitHead(user)) return true
  return false
}
