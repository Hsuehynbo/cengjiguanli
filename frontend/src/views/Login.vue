<template>
  <div class="login-page">
    <div class="login-bg">
      <div class="particles">
        <div v-for="i in 20" :key="i" class="particle" :style="particleStyle(i)" />
      </div>
    </div>

    <div class="login-card">
      <div class="login-logo">
        <img src="/img/yhga.png" alt="logo" class="logo-img" />
      </div>
      <h2 class="login-title">云和县公安局层级管理系统</h2>
      <p class="login-subtitle">Enterprise Hierarchy Management System</p>

      <a-form
        :model="form"
        :rules="rules"
        ref="formRef"
        class="login-form"
        @finish="handleLogin"
      >
        <a-form-item name="jobNo">
          <a-input
            v-model:value="form.jobNo"
            placeholder="请输入工号"
            size="large"
            class="dark-input"
          >
            <template #prefix>
              <UserOutlined style="color: rgba(0,212,255,0.6)" />
            </template>
          </a-input>
        </a-form-item>

        <a-form-item name="password">
          <a-input-password
            v-model:value="form.password"
            placeholder="请输入密码"
            size="large"
            class="dark-input"
          >
            <template #prefix>
              <LockOutlined style="color: rgba(0,212,255,0.6)" />
            </template>
          </a-input-password>
        </a-form-item>

        <a-form-item>
          <a-button type="primary" html-type="submit" block size="large" class="login-btn" :loading="loading">
            登 录
          </a-button>
        </a-form-item>
      </a-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { UserOutlined, LockOutlined } from '@ant-design/icons-vue'
import axios from '../utils/axios'

const router = useRouter()
const formRef = ref(null)
const loading = ref(false)
const form = reactive({
  jobNo: '',
  password: ''
})

const rules = {
  jobNo: [
    { required: true, message: '请输入工号', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' }
  ]
}

const particleStyle = (i) => {
  const size = Math.random() * 3 + 1
  return {
    width: size + 'px',
    height: size + 'px',
    left: Math.random() * 100 + '%',
    top: Math.random() * 100 + '%',
    animationDelay: Math.random() * 5 + 's',
    animationDuration: (Math.random() * 4 + 3) + 's'
  }
}

const handleLogin = async () => {
  loading.value = true
  try {
    const response = await axios.post('/api/auth/login', form)
    if (response) {
      localStorage.setItem('token', response.token)
      localStorage.setItem('user', JSON.stringify(response.user))

      const user = response.user
      const position = user.position || ''
      const deptId = user.department?.id
      const deptName = user.department?.deptName || ''

      if (position.includes('局长')) {
        router.push('/global-dashboard')
      } else if ((position.includes('所长') || position.includes('队长') || position.includes('科长') || position.includes('主任')) && !position.startsWith('副')) {
        if (deptId) {
          router.push(`/organization-stats/${deptId}?name=${encodeURIComponent(deptName)}`)
        } else {
          router.push('/')
        }
      } else {
        router.push('/')
      }
    }
  } catch (error) {
    const errMsg = error.response?.data?.error || error.response?.data || error.message || '登录失败'
    message.error(typeof errMsg === 'string' ? errMsg : '登录失败，请检查工号和密码')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  position: relative;
  background: linear-gradient(135deg, #001529 0%, #0a192f 40%, #001529 100%);
  overflow: hidden;
}

.login-bg {
  position: absolute;
  inset: 0;
  pointer-events: none;
}

.particles {
  position: absolute;
  inset: 0;
}

.particle {
  position: absolute;
  background: #00d4ff;
  border-radius: 50%;
  animation: particleFloat linear infinite;
  opacity: 0;
}

@keyframes particleFloat {
  0%   { opacity: 0; transform: translateY(0); }
  10%  { opacity: 0.8; }
  90%  { opacity: 0.2; }
  100% { opacity: 0; transform: translateY(-60vh); }
}

.login-card {
  width: 420px;
  background: rgba(0, 21, 41, 0.9);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(0, 212, 255, 0.2);
  border-radius: 12px;
  padding: 40px 36px;
  position: relative;
  z-index: 1;
  box-shadow: 0 0 60px rgba(0, 212, 255, 0.08);
  animation: fadeInUp 0.6s ease;
}

.login-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 30px;
  right: 30px;
  height: 1px;
  background: linear-gradient(90deg, transparent, #00d4ff, #00ffff, transparent);
}

.login-logo {
  text-align: center;
  margin-bottom: 16px;
}
.logo-img {
  height: 48px;
  filter: brightness(1.2);
}

.login-title {
  text-align: center;
  font-size: 20px;
  font-weight: 600;
  margin: 0 0 4px;
  background: linear-gradient(90deg, #00d4ff, #00ffff);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.login-subtitle {
  text-align: center;
  font-size: 12px;
  color: #8892b0;
  margin: 0 0 28px;
  letter-spacing: 2px;
}

.login-form {
  margin-top: 8px;
}

:deep(.dark-input) {
  background: rgba(0, 212, 255, 0.06) !important;
  border-color: rgba(0, 212, 255, 0.2) !important;
  border-radius: 6px;
  height: 44px;
}
:deep(.dark-input input) {
  background: transparent !important;
  color: #ccd6f6 !important;
}
:deep(.dark-input .ant-input-prefix) {
  margin-right: 8px;
}

.login-btn {
  height: 46px;
  font-size: 16px;
  letter-spacing: 4px;
  border-radius: 6px;
  background: linear-gradient(90deg, #0066cc, #00d4ff);
  border: none;
  font-weight: 600;
  transition: all 0.3s;
}
.login-btn:hover {
  background: linear-gradient(90deg, #0077dd, #00eeee);
  box-shadow: 0 0 20px rgba(0, 212, 255, 0.4);
}

@keyframes fadeInUp {
  from { opacity: 0; transform: translateY(30px); }
  to   { opacity: 1; transform: translateY(0); }
}
</style>
