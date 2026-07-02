<template>
  <div class="login-page">
    <!-- 星空背景 -->
    <div class="stars">
      <div v-for="s in stars" :key="s.id" class="star" :style="s.style" />
    </div>

    <!-- 流光粒子 -->
    <div class="particles">
      <div v-for="p in particles" :key="p.id" class="particle" :style="p.style" />
    </div>

    <!-- 光晕 -->
    <div class="glow-orb orb1"></div>
    <div class="glow-orb orb2"></div>
    <div class="glow-orb orb3"></div>

    <!-- 网格 -->
    <div class="grid-lines"></div>

    <!-- 扫描线 -->
    <div class="scan-line"></div>

    <!-- 登录卡片 -->
    <div class="login-card">
      <div class="corner tl"></div>
      <div class="corner tr"></div>
      <div class="corner bl"></div>
      <div class="corner br"></div>

      <!-- Logo -->
      <div class="logo-section">
        <div class="logo-img-wrapper">
          <img src="/img/yhga.png" alt="云和县公安局" class="logo-img" />
        </div>
        <h1 class="login-title">云和县公安局层级管理系统</h1>
        <p class="login-subtitle">Hierarchy Management System</p>
      </div>

      <!-- 表单 -->
      <a-form
        :model="form"
        :rules="rules"
        ref="formRef"
        class="login-form"
        @finish="handleLogin"
      >
        <div class="form-group">
          <label class="form-label">工 号</label>
          <a-form-item name="jobNo" :no-style="true">
            <div class="input-wrapper">
              <span class="input-icon"><UserOutlined /></span>
              <a-input
                v-model:value="form.jobNo"
                placeholder="请输入工号"
                size="large"
                class="dark-input"
                @focus="jobNoFocused = true"
                @blur="jobNoFocused = false"
              />
              <div class="input-line" :class="{ active: jobNoFocused }"></div>
            </div>
          </a-form-item>
        </div>

        <div class="form-group">
          <label class="form-label">密 码</label>
          <a-form-item name="password" :no-style="true">
            <div class="input-wrapper">
              <span class="input-icon"><LockOutlined /></span>
              <a-input-password
                v-model:value="form.password"
                placeholder="请输入密码"
                size="large"
                class="dark-input"
                @focus="pwdFocused = true"
                @blur="pwdFocused = false"
              />
              <div class="input-line" :class="{ active: pwdFocused }"></div>
            </div>
          </a-form-item>
        </div>

        <a-form-item :no-style="true">
          <a-button
            type="primary"
            html-type="submit"
            block
            size="large"
            class="login-btn"
            :loading="loading"
          >
            <span class="btn-shine"></span>
            {{ loading ? '登录中...' : '登 录' }}
          </a-button>
        </a-form-item>
      </a-form>

      <div class="footer-text">云和县公安局 · 层级管理系统</div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { UserOutlined, LockOutlined } from '@ant-design/icons-vue'
import axios from '../utils/axios'

const router = useRouter()
const formRef = ref(null)
const loading = ref(false)
const jobNoFocused = ref(false)
const pwdFocused = ref(false)

const form = reactive({
  jobNo: '',
  password: ''
})

const rules = {
  jobNo: [{ required: true, message: '请输入工号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

// 生成星空
const stars = computed(() => {
  const arr = []
  for (let i = 0; i < 120; i++) {
    const size = (1 + Math.random() * 2) + 'px'
    arr.push({
      id: i,
      style: {
        left: Math.random() * 100 + '%',
        top: Math.random() * 100 + '%',
        width: size,
        height: size,
        '--dur': (2 + Math.random() * 4) + 's',
        '--max-opacity': (0.3 + Math.random() * 0.7),
        animationDelay: Math.random() * 5 + 's'
      }
    })
  }
  return arr
})

// 生成流光粒子
const particles = computed(() => {
  const arr = []
  for (let i = 0; i < 25; i++) {
    const size = (3 + Math.random() * 5) + 'px'
    arr.push({
      id: i,
      style: {
        left: Math.random() * 100 + '%',
        '--size': size,
        '--duration': (6 + Math.random() * 8) + 's',
        '--delay': Math.random() * 10 + 's',
        '--drift': (Math.random() * 100 - 50) + 'px'
      }
    })
  }
  return arr
})

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
  background: radial-gradient(ellipse at 20% 50%, #0a1f3d 0%, #000d1a 50%, #000510 100%);
  overflow: hidden;
  font-family: 'Segoe UI', -apple-system, sans-serif;
}

/* ===== 星空 ===== */
.stars { position: absolute; inset: 0; }
.star {
  position: absolute;
  background: #fff;
  border-radius: 50%;
  animation: twinkle var(--dur) ease-in-out infinite;
  opacity: 0;
}
@keyframes twinkle {
  0%, 100% { opacity: 0; }
  50% { opacity: var(--max-opacity); }
}

/* ===== 流光粒子 ===== */
.particles { position: absolute; inset: 0; pointer-events: none; }
.particle {
  position: absolute;
  width: var(--size);
  height: var(--size);
  background: radial-gradient(circle, #2563eb, transparent);
  border-radius: 50%;
  animation: floatUp var(--duration) linear infinite;
  animation-delay: var(--delay);
  opacity: 0;
  bottom: -20px;
}
@keyframes floatUp {
  0%   { opacity: 0; transform: translateY(0) translateX(0) scale(0.5); }
  15%  { opacity: 0.8; }
  85%  { opacity: 0.3; }
  100% { opacity: 0; transform: translateY(-100vh) translateX(var(--drift)) scale(1.2); }
}

/* ===== 光晕 ===== */
.glow-orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  animation: orbPulse 8s ease-in-out infinite;
}
.glow-orb.orb1 { width: 400px; height: 400px; background: rgba(37, 99, 235, 0.12); top: -100px; left: -100px; }
.glow-orb.orb2 { width: 300px; height: 300px; background: rgba(59, 130, 246, 0.08); bottom: -50px; right: -50px; animation-delay: 4s; }
.glow-orb.orb3 { width: 200px; height: 200px; background: rgba(16, 185, 129, 0.06); top: 50%; left: 60%; animation-delay: 2s; }
@keyframes orbPulse {
  0%, 100% { transform: scale(1); opacity: 0.6; }
  50% { transform: scale(1.3); opacity: 1; }
}

/* ===== 网格 ===== */
.grid-lines {
  position: absolute;
  inset: 0;
  background-image:
    linear-gradient(rgba(37, 99, 235, 0.03) 1px, transparent 1px),
    linear-gradient(90deg, rgba(37, 99, 235, 0.03) 1px, transparent 1px);
  background-size: 60px 60px;
  animation: gridShift 20s linear infinite;
}
@keyframes gridShift {
  0% { transform: translate(0, 0); }
  100% { transform: translate(60px, 60px); }
}

/* ===== 扫描线 ===== */
.scan-line {
  position: absolute;
  top: 0; left: 0; right: 0;
  height: 1px;
  background: linear-gradient(90deg, transparent, rgba(37, 99, 235, 0.3), transparent);
  animation: scanDown 6s linear infinite;
  pointer-events: none;
  z-index: 5;
}
@keyframes scanDown {
  0% { top: 0; opacity: 0; }
  10% { opacity: 1; }
  90% { opacity: 1; }
  100% { top: 100%; opacity: 0; }
}

/* ===== 登录卡片 ===== */
.login-card {
  width: 440px;
  background: rgba(0, 15, 35, 0.7);
  backdrop-filter: blur(24px);
  -webkit-backdrop-filter: blur(24px);
  border: 1px solid rgba(37, 99, 235, 0.15);
  border-radius: 16px;
  padding: 44px 40px 36px;
  position: relative;
  z-index: 10;
  animation: cardAppear 0.8s cubic-bezier(0.16, 1, 0.3, 1) forwards;
  box-shadow:
    0 0 80px rgba(37, 99, 235, 0.06),
    0 20px 60px rgba(0, 0, 0, 0.4),
    inset 0 1px 0 rgba(255, 255, 255, 0.05);
}
@keyframes cardAppear {
  from { opacity: 0; transform: translateY(40px) scale(0.96); }
  to { opacity: 1; transform: translateY(0) scale(1); }
}

/* 顶部光带 */
.login-card::before {
  content: '';
  position: absolute;
  top: 0; left: 40px; right: 40px;
  height: 1px;
  background: linear-gradient(90deg, transparent, #2563eb, #3b82f6, #2563eb, transparent);
  animation: topGlow 3s ease-in-out infinite;
}
@keyframes topGlow {
  0%, 100% { opacity: 0.5; }
  50% { opacity: 1; }
}

/* 角落装饰 */
.corner {
  position: absolute;
  width: 20px;
  height: 20px;
  border-color: rgba(37, 99, 235, 0.4);
  border-style: solid;
  border-width: 0;
}
.corner.tl { top: -1px; left: -1px; border-top-width: 2px; border-left-width: 2px; border-top-left-radius: 16px; }
.corner.tr { top: -1px; right: -1px; border-top-width: 2px; border-right-width: 2px; border-top-right-radius: 16px; }
.corner.bl { bottom: -1px; left: -1px; border-bottom-width: 2px; border-left-width: 2px; border-bottom-left-radius: 16px; }
.corner.br { bottom: -1px; right: -1px; border-bottom-width: 2px; border-right-width: 2px; border-bottom-right-radius: 16px; }

/* ===== Logo ===== */
.logo-section { text-align: center; margin-bottom: 28px; }
.logo-img-wrapper { text-align: center; margin-bottom: 16px; }
.logo-img {
  height: 56px;
  filter: brightness(1.2) drop-shadow(0 0 12px rgba(37, 99, 235, 0.3));
  animation: logoPulse 3s ease-in-out infinite;
}
@keyframes logoPulse {
  0%, 100% { filter: brightness(1.2) drop-shadow(0 0 12px rgba(37, 99, 235, 0.3)); }
  50% { filter: brightness(1.4) drop-shadow(0 0 20px rgba(37, 99, 235, 0.5)); }
}

.login-title {
  font-size: 22px;
  font-weight: 700;
  margin: 0 0 6px;
  background: linear-gradient(90deg, #2563eb, #3b82f6, #2563eb);
  background-size: 200% 100%;
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  animation: textShimmer 4s linear infinite;
  letter-spacing: 2px;
}
@keyframes textShimmer {
  0% { background-position: 200% 0; }
  100% { background-position: -200% 0; }
}

.login-subtitle {
  font-size: 11px;
  color: #4a6a8a;
  letter-spacing: 3px;
  text-transform: uppercase;
  margin: 0;
}

/* ===== 表单 ===== */
.login-form { margin-top: 8px; }
.form-group { margin-bottom: 20px; }
.form-label {
  display: block;
  font-size: 12px;
  color: #5a7a9a;
  margin-bottom: 8px;
  letter-spacing: 1px;
}
.input-wrapper { position: relative; }
.input-icon {
  position: absolute;
  left: 14px;
  top: 50%;
  transform: translateY(-50%);
  font-size: 16px;
  color: rgba(37, 99, 235, 0.4);
  transition: color 0.3s;
  z-index: 1;
  pointer-events: none;
}
.input-line {
  position: absolute;
  bottom: 0; left: 10%; right: 10%;
  height: 2px;
  background: linear-gradient(90deg, transparent, #2563eb, transparent);
  transform: scaleX(0);
  transition: transform 0.4s cubic-bezier(0.16, 1, 0.3, 1);
  border-radius: 1px;
}
.input-line.active { transform: scaleX(1); }

:deep(.dark-input) {
  background: rgba(37, 99, 235, 0.04) !important;
  border: 1px solid rgba(37, 99, 235, 0.12) !important;
  border-radius: 8px !important;
  height: 48px !important;
  padding-left: 42px !important;
  color: #ccd6f6 !important;
  transition: all 0.3s !important;
}
:deep(.dark-input:focus),
:deep(.dark-input:hover) {
  border-color: rgba(37, 99, 235, 0.5) !important;
  background: rgba(37, 99, 235, 0.08) !important;
  box-shadow: 0 0 0 3px rgba(37, 99, 235, 0.08), 0 0 20px rgba(37, 99, 235, 0.1) !important;
}
:deep(.dark-input input) {
  background: transparent !important;
  color: #ccd6f6 !important;
}
:deep(.dark-input input::placeholder) {
  color: #3a5a7a !important;
}
:deep(.dark-input .ant-input-prefix) {
  display: none !important;
}

/* ===== 登录按钮 ===== */
.login-btn {
  height: 50px !important;
  font-size: 16px !important;
  letter-spacing: 6px !important;
  border-radius: 8px !important;
  border: none !important;
  font-weight: 600 !important;
  position: relative;
  overflow: hidden;
  background: linear-gradient(135deg, #1d4ed8, #2563eb, #3b82f6) !important;
  background-size: 200% 200% !important;
  box-shadow: 0 4px 20px rgba(37, 99, 235, 0.3) !important;
  transition: all 0.4s !important;
}
.login-btn:hover {
  background-position: 100% 100% !important;
  box-shadow: 0 4px 30px rgba(59, 130, 246, 0.5) !important;
  transform: translateY(-1px);
}
.login-btn:active {
  transform: translateY(0) !important;
}

.btn-shine {
  position: absolute;
  top: 0; left: -100%;
  width: 100%; height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255,255,255,0.15), transparent);
  animation: btnShine 3s ease-in-out infinite;
}
@keyframes btnShine {
  0% { left: -100%; }
  50%, 100% { left: 100%; }
}

/* ===== 底部 ===== */
.footer-text {
  text-align: center;
  margin-top: 24px;
  font-size: 11px;
  color: #3a5a7a;
  letter-spacing: 1px;
}

/* ===== 响应式 ===== */
@media (max-width: 480px) {
  .login-card {
    width: calc(100% - 32px);
    padding: 32px 24px;
    margin: 16px;
  }
}
</style>
