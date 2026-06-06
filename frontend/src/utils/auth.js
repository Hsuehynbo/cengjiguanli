export function getCurrentUser() {
  try {
    return JSON.parse(localStorage.getItem('user') || '{}')
  } catch {
    return {}
  }
}

export function getToken() {
  return localStorage.getItem('token')
}

export function setAuth(token, user) {
  localStorage.setItem('token', token)
  localStorage.setItem('user', JSON.stringify(user))
}

export function clearAuth() {
  localStorage.removeItem('token')
  localStorage.removeItem('user')
}

export function hasPermission(permissionCode) {
  const user = getCurrentUser()
  if (!user) return false
  if (user.jobNo === 'admin') return true
  const permissions = user.permissions || []
  return permissions.includes(permissionCode)
}

export function hasAnyPermission(...codes) {
  return codes.some(code => hasPermission(code))
}

export function getUserPermissions() {
  const user = getCurrentUser()
  return user?.permissions || []
}
