<template>
  <a-config-provider :theme="themeConfig">
    <div class="app">
      <router-view v-slot="{ Component, route }">
        <Transition
          :css="false"
          @before-enter="onBeforeEnter"
          @enter="onEnter"
          @leave="onLeave"
        >
          <component :is="Component" :key="route.path" />
        </Transition>
      </router-view>
    </div>
  </a-config-provider>
</template>

<script setup>
import { ref, computed, watchEffect, onMounted } from 'vue'
import { gsap } from 'gsap'

// ========== 主题系统 ==========
const themeMode = ref('light') // 'light' | 'dark'

function getInitialTheme() {
  const saved = localStorage.getItem('theme-mode')
  if (saved === 'light' || saved === 'dark') return saved
  // 自动模式：6:00-18:00 浅色，其余深色
  const hour = new Date().getHours()
  return (hour >= 6 && hour < 18) ? 'light' : 'dark'
}

function applyTheme(mode) {
  themeMode.value = mode
  document.documentElement.setAttribute('data-theme', mode)
  localStorage.setItem('theme-mode', mode)
}

function toggleTheme() {
  applyTheme(themeMode.value === 'light' ? 'dark' : 'light')
}

// 暴露给子组件使用
defineExpose({ themeMode, toggleTheme })

onMounted(() => {
  applyTheme(getInitialTheme())
})

// ========== Ant Design 主题 Token ==========
const themeConfig = computed(() => {
  const isDark = themeMode.value === 'dark'
  return {
    token: {
      colorBgContainer: isDark ? '#1e293b' : '#ffffff',
      colorBgElevated: isDark ? '#1e293b' : '#ffffff',
      colorBgBase: isDark ? '#0f172a' : '#f5f7fa',
      colorText: isDark ? '#e2e8f0' : '#1e293b',
      colorTextSecondary: isDark ? '#94a3b8' : '#475569',
      colorTextTertiary: isDark ? '#64748b' : '#94a3b8',
      colorTextQuaternary: isDark ? '#475569' : '#cbd5e1',
      colorBorder: isDark ? '#334155' : '#e2e8f0',
      colorBorderSecondary: isDark ? '#1e293b' : '#f1f5f9',
      colorPrimary: isDark ? '#3b82f6' : '#2563eb',
      controlOutline: isDark ? 'rgba(59,130,246,0.2)' : 'rgba(37,99,235,0.15)',
      colorLink: isDark ? '#3b82f6' : '#2563eb',
      colorLinkHover: isDark ? '#60a5fa' : '#1d4ed8',
      colorItemBgActive: isDark ? 'rgba(59,130,246,0.15)' : 'rgba(37,99,235,0.08)',
      colorItemBgHover: isDark ? 'rgba(59,130,246,0.08)' : 'rgba(37,99,235,0.04)',
      borderRadius: 8,
      wireframe: false,
    },
    components: {
      Table: {
        headerBg: isDark ? '#1e293b' : '#f8fafc',
        headerColor: isDark ? '#94a3b8' : '#475569',
        rowHoverBg: isDark ? 'rgba(59,130,246,0.08)' : '#f8fafc',
        borderColor: isDark ? '#334155' : '#f1f5f9',
        cellPaddingBlock: 12,
      },
      Input: {
        colorBgContainer: isDark ? '#0f172a' : '#f8fafc',
        colorBorder: isDark ? '#334155' : '#e2e8f0',
        colorText: isDark ? '#e2e8f0' : '#1e293b',
        colorTextPlaceholder: isDark ? '#64748b' : '#94a3b8',
        activeBorderColor: isDark ? '#3b82f6' : '#2563eb',
        hoverBorderColor: isDark ? '#475569' : '#cbd5e1',
      },
      Select: {
        colorBgContainer: isDark ? '#0f172a' : '#f8fafc',
        colorBorder: isDark ? '#334155' : '#e2e8f0',
        colorText: isDark ? '#e2e8f0' : '#1e293b',
        colorTextPlaceholder: isDark ? '#64748b' : '#94a3b8',
        optionSelectedBg: isDark ? 'rgba(59,130,246,0.15)' : 'rgba(37,99,235,0.08)',
        optionActiveBg: isDark ? 'rgba(59,130,246,0.08)' : 'rgba(37,99,235,0.04)',
        colorBgElevated: isDark ? '#1e293b' : '#ffffff',
      },
      Modal: {
        contentBg: isDark ? '#1e293b' : '#ffffff',
        headerBg: isDark ? '#1e293b' : '#ffffff',
        titleColor: isDark ? '#f1f5f9' : '#0f172a',
      },
      Tabs: {
        itemColor: isDark ? '#64748b' : '#94a3b8',
        itemActiveColor: isDark ? '#3b82f6' : '#2563eb',
        itemHoverColor: isDark ? '#3b82f6' : '#2563eb',
        inkBarColor: isDark ? '#3b82f6' : '#2563eb',
      },
      Card: {
        colorBgContainer: isDark ? '#1e293b' : '#ffffff',
        colorText: isDark ? '#e2e8f0' : '#1e293b',
        colorTextHeading: isDark ? '#f1f5f9' : '#0f172a',
      },
      Alert: {
        colorInfoBg: isDark ? 'rgba(59,130,246,0.1)' : 'rgba(37,99,235,0.05)',
        colorInfoBorder: isDark ? 'rgba(59,130,246,0.3)' : 'rgba(37,99,235,0.2)',
        colorText: isDark ? '#e2e8f0' : '#1e293b',
        colorTextDescription: isDark ? '#94a3b8' : '#475569',
      },
      Tree: {
        colorBgContainer: 'transparent',
        colorText: isDark ? '#e2e8f0' : '#1e293b',
        titleColor: isDark ? '#e2e8f0' : '#1e293b',
      },
      DatePicker: {
        colorBgContainer: isDark ? '#0f172a' : '#f8fafc',
        colorBorder: isDark ? '#334155' : '#e2e8f0',
        colorText: isDark ? '#e2e8f0' : '#1e293b',
        colorTextPlaceholder: isDark ? '#64748b' : '#94a3b8',
        colorBgElevated: isDark ? '#1e293b' : '#ffffff',
      },
      Form: {
        labelColor: isDark ? '#e2e8f0' : '#1e293b',
      },
      Pagination: {
        colorBgContainer: isDark ? '#1e293b' : '#ffffff',
        colorBorder: isDark ? '#334155' : '#e2e8f0',
        colorText: isDark ? '#e2e8f0' : '#1e293b',
        itemActiveBg: isDark ? 'rgba(59,130,246,0.15)' : 'rgba(37,99,235,0.08)',
      },
      Menu: {
        colorBgContainer: isDark ? '#1e293b' : '#ffffff',
        colorItemText: isDark ? '#e2e8f0' : '#475569',
        colorItemTextHover: isDark ? '#3b82f6' : '#2563eb',
        colorItemBgHover: isDark ? 'rgba(59,130,246,0.08)' : 'rgba(37,99,235,0.04)',
        colorItemBgSelected: isDark ? 'rgba(59,130,246,0.15)' : 'rgba(37,99,235,0.08)',
        colorItemTextSelected: isDark ? '#3b82f6' : '#2563eb',
      },
      Dropdown: {
        colorBgElevated: isDark ? '#1e293b' : '#ffffff',
      },
      Popover: {
        colorBgElevated: isDark ? '#1e293b' : '#ffffff',
      },
      Tooltip: {
        colorBgDefault: isDark ? '#1e293b' : '#0f172a',
      },
      Message: {
        contentBg: isDark ? '#1e293b' : '#ffffff',
      },
      Notification: {
        colorBgElevated: isDark ? '#1e293b' : '#ffffff',
      },
    }
  }
})

// ========== GSAP 页面过渡 ==========
function onBeforeEnter(el) {
  gsap.set(el, { opacity: 0, y: 20 })
}

function onEnter(el, done) {
  gsap.to(el, {
    opacity: 1,
    y: 0,
    duration: 0.4,
    ease: 'power2.out',
    onComplete: done
  })
}

function onLeave(el, done) {
  gsap.to(el, {
    opacity: 0,
    y: -10,
    duration: 0.25,
    ease: 'power2.in',
    onComplete: done
  })
}
</script>

<style>
.modal-table {
  width: 100% !important;
}
.modal-table :deep(.ant-table-wrapper) {
  width: 100% !important;
}
.modal-table :deep(.ant-table) {
  width: 100% !important;
  margin: 0 !important;
  padding: 0 !important;
}
.modal-table :deep(.ant-table-content) {
  width: 100% !important;
}
.modal-table :deep(.ant-table-container) {
  width: 100% !important;
  overflow: hidden !important;
}
.modal-table :deep(.ant-table-body) {
  width: 100% !important;
  overflow: hidden !important;
}
.modal-table :deep(.ant-table-thead) {
  width: 100% !important;
}
.modal-table :deep(.ant-table-thead > tr) {
  width: 100% !important;
}
.modal-table :deep(.ant-table-tbody) {
  width: 100% !important;
}
.modal-table :deep(.ant-table-tbody > tr) {
  width: 100% !important;
}
.modal-table :deep(.ant-table-selection-column) {
  display: none !important;
  width: 0 !important;
  min-width: 0 !important;
  padding: 0 !important;
}
.modal-table :deep(.ant-table-row-expand-icon-cell) {
  display: none !important;
  width: 0 !important;
  min-width: 0 !important;
  padding: 0 !important;
}
.modal-table :deep(.ant-checkbox-wrapper),
.modal-table :deep(.ant-checkbox) {
  display: none !important;
}
.modal-table :deep(.ant-table-thead > tr > th:first-child),
.modal-table :deep(.ant-table-tbody > tr > td:first-child) {
  display: none !important;
  width: 0 !important;
  min-width: 0 !important;
  padding: 0 !important;
}
</style>

<style scoped>
.app {
  min-height: 100vh;
  background-color: var(--bg-page);
}
</style>
