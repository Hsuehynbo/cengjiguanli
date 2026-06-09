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
import { gsap } from 'gsap'

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

const themeConfig = {
  token: {
    colorBgContainer: 'rgba(0, 21, 41, 0.85)',
    colorBgElevated: 'rgba(0, 21, 41, 0.95)',
    colorBgBase: '#001529',
    colorText: '#ccd6f6',
    colorTextSecondary: '#8892b0',
    colorTextTertiary: '#5a6a8a',
    colorTextQuaternary: '#3a4a6a',
    colorBorder: 'rgba(0, 212, 255, 0.2)',
    colorBorderSecondary: 'rgba(0, 212, 255, 0.1)',
    colorPrimary: '#00d4ff',
    controlOutline: 'rgba(0, 212, 255, 0.2)',
    colorLink: '#00d4ff',
    colorLinkHover: '#00ffff',
    colorItemBgActive: 'rgba(0, 212, 255, 0.15)',
    colorItemBgHover: 'rgba(0, 212, 255, 0.08)',
    borderRadius: 6,
    wireframe: false,
  },
  components: {
    Table: {
      headerBg: 'rgba(0, 21, 41, 0.6)',
      headerColor: '#ccd6f6',
      rowHoverBg: 'rgba(0, 212, 255, 0.08)',
      borderColor: 'rgba(0, 212, 255, 0.1)',
      cellPaddingBlock: 12,
    },
    Input: {
      colorBgContainer: 'rgba(0, 21, 41, 0.6)',
      colorBorder: 'rgba(0, 212, 255, 0.2)',
      colorText: '#ccd6f6',
      colorTextPlaceholder: '#5a6a8a',
      activeBorderColor: '#00d4ff',
      hoverBorderColor: 'rgba(0, 212, 255, 0.35)',
    },
    Select: {
      colorBgContainer: 'rgba(0, 21, 41, 0.6)',
      colorBorder: 'rgba(0, 212, 255, 0.2)',
      colorText: '#ccd6f6',
      colorTextPlaceholder: '#5a6a8a',
      optionSelectedBg: 'rgba(0, 212, 255, 0.15)',
      optionActiveBg: 'rgba(0, 212, 255, 0.08)',
      colorBgElevated: 'rgba(0, 21, 41, 0.95)',
    },
    Modal: {
      contentBg: 'rgba(0, 21, 41, 0.95)',
      headerBg: 'rgba(0, 21, 41, 0.95)',
      titleColor: '#fff',
    },
    Tabs: {
      itemColor: '#8892b0',
      itemActiveColor: '#00d4ff',
      itemHoverColor: '#00d4ff',
      inkBarColor: '#00d4ff',
    },
    Card: {
      colorBgContainer: 'rgba(0, 21, 41, 0.85)',
      colorText: '#ccd6f6',
      colorTextHeading: '#fff',
    },
    Alert: {
      colorInfoBg: 'rgba(0, 21, 41, 0.6)',
      colorInfoBorder: 'rgba(0, 212, 255, 0.2)',
      colorText: '#ccd6f6',
      colorTextDescription: '#8892b0',
    },
    Tree: {
      colorBgContainer: 'transparent',
      colorText: '#ccd6f6',
      titleColor: '#ccd6f6',
    },
    DatePicker: {
      colorBgContainer: 'rgba(0, 21, 41, 0.6)',
      colorBorder: 'rgba(0, 212, 255, 0.2)',
      colorText: '#ccd6f6',
      colorTextPlaceholder: '#5a6a8a',
      colorBgElevated: 'rgba(0, 21, 41, 0.95)',
    },
    Form: {
      labelColor: '#ccd6f6',
    },
    Pagination: {
      colorBgContainer: 'rgba(0, 21, 41, 0.85)',
      colorBorder: 'rgba(0, 212, 255, 0.2)',
      colorText: '#ccd6f6',
      itemActiveBg: 'rgba(0, 212, 255, 0.15)',
    },
    Menu: {
      colorBgContainer: 'rgba(0, 21, 41, 0.85)',
      colorItemText: '#ccd6f6',
      colorItemTextHover: '#00d4ff',
      colorItemBgHover: 'rgba(0, 212, 255, 0.08)',
      colorItemBgSelected: 'rgba(0, 212, 255, 0.15)',
      colorItemTextSelected: '#00d4ff',
    },
    Dropdown: {
      colorBgElevated: 'rgba(0, 21, 41, 0.95)',
    },
    Popover: {
      colorBgElevated: 'rgba(0, 21, 41, 0.95)',
    },
    Tooltip: {
      colorBgDefault: 'rgba(0, 21, 41, 0.95)',
    },
    Message: {
      contentBg: 'rgba(0, 21, 41, 0.95)',
    },
    Notification: {
      colorBgElevated: 'rgba(0, 21, 41, 0.95)',
    },
  }
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
  background-color: #001529;
}
</style>
