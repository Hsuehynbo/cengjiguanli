<template>
  <div class="org-chart-container">
    <!-- 顶部信息栏 -->
    <div class="top-bar">
      <div class="title-section">
        <span class="main-title">组织架构图</span>
        <span class="sub-title">{{ scopeText }}</span>
      </div>
      <div class="info-section">
        <ClockDisplay />
      </div>
    </div>

    <!-- 数据卡片区域 -->
    <div class="stats-grid">
      <div class="stat-card tech-card">
        <div class="card-header">
          <span class="card-icon">👮</span>
          <span class="card-label">总人数</span>
        </div>
        <div class="card-value">{{ stats.totalUsers }}</div>
      </div>

      <div class="stat-card tech-card key-card clickable-card" @click="openRiskUserModal('KEY')">
        <div class="card-header">
          <span class="card-icon">⚠️</span>
          <span class="card-label">重点人员</span>
        </div>
        <div class="card-value highlight-red">{{ stats.keyUsers }}</div>
        <div class="card-click-hint">点击查看详情</div>
      </div>

      <div class="stat-card tech-card warning-card clickable-card" @click="openRiskUserModal('RISK')">
        <div class="card-header">
          <span class="card-icon">⚠️</span>
          <span class="card-label">风险人员</span>
        </div>
        <div class="card-value highlight-orange">{{ stats.riskUsers }}</div>
        <div class="card-click-hint">点击查看详情</div>
      </div>

      <div class="stat-card tech-card success-card clickable-card" @click="openRiskUserModal('ATTENTION')">
        <div class="card-header">
          <span class="card-icon">👁️</span>
          <span class="card-label">关注人员</span>
        </div>
        <div class="card-value highlight-green">{{ stats.attentionUsers }}</div>
        <div class="card-click-hint">点击查看详情</div>
      </div>
    </div>

    <!-- 图表区域 -->
    <div class="chart-card tech-card">
      <div class="chart-header">
        <span class="chart-title">组织架构图</span>
        <div class="chart-actions">
          <a-input-search
            v-model:value="searchKeyword"
            allow-clear
            placeholder="输入姓名或警号定位人员"
            class="toolbar-input"
            @search="handleSearch"
            @change="handleSearchChange"
          />
          <a-select
            v-if="canSwitchDepartment"
            v-model:value="selectedDeptId"
            allow-clear
            placeholder="查看指定部门"
            class="toolbar-select"
            @change="handleDepartmentChange"
          >
            <a-select-option :value="null">全局架构</a-select-option>
            <a-select-option v-for="dept in departmentOptions" :key="dept.id" :value="dept.id">
              {{ dept.name }}
            </a-select-option>
          </a-select>
          <a-button @click="handleFitView">自适应</a-button>
          <a-button @click="handleResetView">重置高亮</a-button>
          <a-button type="primary" @click="fetchData">刷新数据</a-button>
        </div>
      </div>

      <div class="chart-toolbar">
        <div class="legend-list">
          <span class="legend-item"><span class="legend-dot normal"></span>普通人员</span>
          <span class="legend-item"><span class="legend-dot key"></span>重点人员</span>
          <span class="legend-item"><span class="legend-dot risk"></span>风险人员</span>
          <span class="legend-item"><span class="legend-dot attention"></span>关注人员</span>
        </div>
        <div class="toolbar-tip">支持滚动条拖动、鼠标滚轮缩放、右下角缩略图快速定位</div>
      </div>

      <div class="chart-wrapper">
        <a-spin :spinning="loading" class="chart-spin">
          <div ref="scrollAreaRef" class="canvas-scroll-area">
            <div
              ref="graphStageRef"
              class="canvas-stage"
              :style="{ width: `${canvasSize.width}px`, height: `${canvasSize.height}px` }"
            >
              <div ref="containerRef" class="g6-container"></div>
            </div>
          </div>
        </a-spin>

        <div v-if="hoverNode" class="node-tooltip" :style="tooltipStyle">
          <div class="tooltip-header">{{ hoverNode.name }}</div>
          <div class="tooltip-body">
            <p v-if="hoverNode.nodeType === 'user'">职务：{{ hoverNode.position || '未设置' }}</p>
            <p v-if="hoverNode.jobNo">警号：{{ hoverNode.jobNo }}</p>
            <p v-if="hoverNode.nodeType === 'user'">风险等级：{{ riskLevelMap[hoverNode.riskLevel || 'NORMAL'] }}</p>
            <p v-if="hoverNode.nodeType === 'department'">部门人数：{{ hoverNode.userCount }} 人</p>
            <p v-if="hoverNode.nodeType === 'company'">部门总数：{{ hoverNode.deptCount }} 个</p>
          </div>
        </div>
      </div>
    </div>

    <a-modal
      v-model:open="riskModal.visible"
      :title="riskModal.title"
      :footer="null"
      width="900px"
    >
      <a-table
        :columns="riskColumns"
        :data-source="riskModal.users"
        row-key="jobNo"
        size="small"
        :pagination="{ pageSize: 8 }"
        :row-selection="false"
        :defaultExpandAllRows="false"
        class="modal-table"
        :showHeader="true"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'action'">
            <a-button type="link" @click="goToUserDetail(record.jobNo)">查看详情</a-button>
          </template>
        </template>
      </a-table>
    </a-modal>
  </div>
</template>

<script setup>
import { computed, nextTick, onMounted, onUnmounted, reactive, ref } from 'vue';
import G6, { TreeGraph } from '@antv/g6';
import axios from '../utils/axios';
import { message } from 'ant-design-vue';
import { useRouter } from 'vue-router';
import {
  AlertOutlined,
  EyeOutlined,
  TeamOutlined,
  WarningOutlined
} from '@ant-design/icons-vue';
import { getCurrentUser } from '../utils/auth';
import { RISK_LEVEL_MAP } from '../utils/constants';
import ClockDisplay from '../components/ClockDisplay.vue';

const router = useRouter();
const currentUser = ref(getCurrentUser());
const containerRef = ref(null);
const scrollAreaRef = ref(null);
const graphStageRef = ref(null);
const loading = ref(false);
const searchKeyword = ref('');
const selectedDeptId = ref(null);
const rawTreeData = ref(null);
const departmentOptions = ref([]);
const hoverNode = ref(null);
const highlightedNodeId = ref('');
const tooltipPos = reactive({ x: 0, y: 0 });
const canvasSize = reactive({ width: 1200, height: 620 });

const stats = reactive({
  totalUsers: 0,
  keyUsers: 0,
  riskUsers: 0,
  attentionUsers: 0
});

const riskModal = reactive({
  visible: false,
  title: '',
  users: []
});

const riskLevelMap = {
  NORMAL: RISK_LEVEL_MAP.NORMAL.label,
  KEY: RISK_LEVEL_MAP.KEY.label,
  RISK: RISK_LEVEL_MAP.RISK.label,
  ATTENTION: RISK_LEVEL_MAP.ATTENTION.label
};

const riskColumns = [
  { title: '姓名', dataIndex: 'name', key: 'name', width: 160 },
  { title: '警号', dataIndex: 'jobNo', key: 'jobNo', width: 120 },
  { title: '所属部门', dataIndex: 'deptName', key: 'deptName', width: 200 },
  { title: '职务', dataIndex: 'position', key: 'position', width: 160 },
  { title: '操作', key: 'action', width: 100, align: 'center' }
];

const NODE_COLORS = {
  company: { fill: '#165dff', stroke: '#0e42b3', text: '#ffffff' },
  department: { fill: '#165dff', stroke: '#0e42b3', text: '#ffffff' },
  NORMAL: { fill: '#ffffff', stroke: '#1677ff', text: '#1f1f1f' },
  KEY: { fill: '#fff1f0', stroke: '#cf1322', text: '#5c0011' },
  RISK: { fill: '#fffbe6', stroke: '#d48806', text: '#614700' },
  ATTENTION: { fill: '#f6ffed', stroke: '#389e0d', text: '#135200' }
};

const canSwitchDepartment = computed(() => {
  const user = currentUser.value;
  const position = user.position || '';
  return user.jobNo === 'admin' || user.role === 'ADMIN_GLOBAL' || position.includes('局长');
});

const scopeText = computed(() => {
  if (canSwitchDepartment.value) {
    const selectedDept = departmentOptions.value.find(item => item.id === selectedDeptId.value);
    return selectedDept ? `${selectedDept.name}视图` : '全局架构';
  }
  return `${currentUser.value.department?.deptName || '本部门'}视图`;
});

const tooltipStyle = computed(() => ({
  left: `${tooltipPos.x + 12}px`,
  top: `${tooltipPos.y + 12}px`
}));

let graph = null;
let minimap = null;
let resizeTimer = null;
let lastWrapperWidth = 0;

const fetchDepartmentOptions = async () => {
  try {
    departmentOptions.value = await axios.get('/api/organization/departments');
  } catch (error) {
    // ignore
  }
};

const fetchData = async () => {
  loading.value = true;
  try {
    const params = {};
    if (selectedDeptId.value) {
      params.deptId = selectedDeptId.value;
    }
    const data = await axios.get('/api/organization/tree', { params });
    rawTreeData.value = data;
    updateStats(data);
    await nextTick();
    renderGraph(data);
    if (searchKeyword.value) {
      handleSearch(searchKeyword.value);
    }
  } catch (error) {
    message.error(error.response?.data?.message || '获取组织架构失败');
  } finally {
    loading.value = false;
  }
};

const collectMetrics = (node, context = {}) => {
  if (!node) {
    return [];
  }
  const currentDeptName = node.type === 'department' ? node.name : context.deptName;
  const items = [];
  if (node.type === 'user') {
    items.push({
      name: node.name,
      jobNo: node.jobNo,
      position: node.position,
      deptName: node.deptName || currentDeptName || currentUser.value.department?.deptName || '未分配',
      riskLevel: node.riskLevel || (node.isKeyPersonnel ? 'KEY' : 'NORMAL')
    });
  }
  (node.children || []).forEach(child => {
    items.push(...collectMetrics(child, { deptName: currentDeptName }));
  });
  return items;
};

const updateStats = (treeData) => {
  const users = collectMetrics(treeData);
  stats.totalUsers = users.length;
  stats.keyUsers = users.filter(item => item.riskLevel === 'KEY').length;
  stats.riskUsers = users.filter(item => item.riskLevel === 'RISK').length;
  stats.attentionUsers = users.filter(item => item.riskLevel === 'ATTENTION').length;
};

const openRiskUserModal = (riskLevel) => {
  if (!rawTreeData.value) {
    return;
  }
  const users = collectMetrics(rawTreeData.value).filter(item => item.riskLevel === riskLevel);
  riskModal.title = `${riskLevelMap[riskLevel]}列表`;
  riskModal.users = users;
  riskModal.visible = true;
};

const getNodeColor = (node) => {
  if (node.type === 'company') {
    return NODE_COLORS.company;
  }
  if (node.type === 'department') {
    return NODE_COLORS.department;
  }
  return NODE_COLORS[node.riskLevel || (node.isKeyPersonnel ? 'KEY' : 'NORMAL')] || NODE_COLORS.NORMAL;
};

const getTreeDepth = (node) => {
  if (!node?.children?.length) {
    return 1;
  }
  return 1 + Math.max(...node.children.map(getTreeDepth));
};

const getLeafCount = (node) => {
  if (!node?.children?.length) {
    return 1;
  }
  return node.children.reduce((total, child) => total + getLeafCount(child), 0);
};

const updateCanvasSize = (data) => {
  const wrapperWidth = Math.max((scrollAreaRef.value?.clientWidth || 1200) - 16, 760);
  const depth = getTreeDepth(data);
  const leafCount = getLeafCount(data);
  const preferredHeight = Math.max(460, Math.min(depth * 128 + 180, 860));
  const compactWidth = leafCount <= 3 ? wrapperWidth : Math.min(wrapperWidth + 120, Math.max(wrapperWidth, leafCount * 160));
  lastWrapperWidth = wrapperWidth;
  canvasSize.width = compactWidth;
  canvasSize.height = preferredHeight;
};

const resetScrollPosition = () => {
  if (!scrollAreaRef.value) {
    return;
  }
  scrollAreaRef.value.scrollTo({ left: 0, top: 0, behavior: 'auto' });
};

const transformData = (node) => {
  const color = getNodeColor(node);
  const isUser = node.type === 'user';
  const label = isUser
    ? `${node.name}\n${node.position || ''}`
    : `${node.name}${node.type === 'department' ? `\n${node.userCount || 0}人` : ''}`;

  return {
    ...node,
    id: node.id,
    nodeType: node.type,
    label,
    type: 'rect',
    style: {
      fill: color.fill,
      stroke: color.stroke,
      radius: 8,
      lineWidth: 2,
      cursor: 'pointer'
    },
    labelCfg: {
      style: {
        fill: color.text,
        fontSize: 13,
        lineHeight: 18,
        fontWeight: isUser ? 500 : 600
      }
    },
    children: (node.children || []).map(transformData)
  };
};

const destroyGraph = () => {
  if (graph) {
    graph.destroy();
    graph = null;
  }
  minimap = null;
};

const bindGraphEvents = () => {
  graph.on('node:mouseenter', evt => {
    const { item } = evt;
    graph.setItemState(item, 'hover', true);
    hoverNode.value = item.getModel();
    tooltipPos.x = evt.canvasX;
    tooltipPos.y = evt.canvasY;
  });

  graph.on('node:mousemove', evt => {
    tooltipPos.x = evt.canvasX;
    tooltipPos.y = evt.canvasY;
  });

  graph.on('node:mouseleave', evt => {
    graph.setItemState(evt.item, 'hover', false);
    hoverNode.value = null;
  });

  graph.on('node:click', evt => {
    const model = evt.item.getModel();
    if (model.nodeType === 'user') {
      goToUserDetail(model.jobNo);
      return;
    }
    if (model.nodeType === 'department') {
      router.push({
        name: 'OrganizationStats',
        params: { id: String(model.deptId || model.id.replace('dept_', '')) },
        query: { name: model.name }
      });
      return;
    }
    if (model.nodeType === 'company' && canSwitchDepartment.value) {
      router.push({ name: 'GlobalDashboard' });
    }
  });
};

const renderGraph = (data) => {
  if (!containerRef.value || !data) {
    return;
  }

  updateCanvasSize(data);
  destroyGraph();

  minimap = new G6.Minimap({
    size: [180, 120],
    className: 'org-chart-minimap'
  });

  graph = new TreeGraph({
    container: containerRef.value,
    width: canvasSize.width,
    height: canvasSize.height,
    plugins: [minimap],
    modes: {
      default: ['drag-canvas', 'zoom-canvas']
    },
    defaultNode: {
      type: 'rect',
      size: [176, 68],
      anchorPoints: [
        [0.5, 0],
        [0.5, 1]
      ]
    },
    nodeStateStyles: {
      hover: {
        shadowColor: '#91caff',
        shadowBlur: 16
      },
      highlight: {
        lineWidth: 4,
        shadowColor: '#faad14',
        shadowBlur: 18
      }
    },
    defaultEdge: {
      type: 'polyline',
      sourceAnchor: 1,
      targetAnchor: 0,
      style: {
        stroke: '#b7c4d6',
        lineWidth: 1.4,
        radius: 8,
        offset: 24,
        endArrow: {
          path: G6.Arrow.triangle(8, 10, 5),
          fill: '#b7c4d6'
        }
      }
    },
    layout: {
      type: 'compactBox',
      direction: 'TB',
      getId: d => d.id,
      getHeight: () => 68,
      getWidth: () => 176,
      getVGap: () => 54,
      getHGap: () => 36
    }
  });

  graph.data(transformData(data));
  graph.render();
  resetScrollPosition();
  graph.fitView(24);
  if (graph.getZoom() > 1.5) {
    graph.zoomTo(1.5);
  }
  graph.fitCenter();
  bindGraphEvents();
};

const clearHighlight = () => {
  highlightedNodeId.value = '';
  if (!graph) {
    return;
  }
  graph.getNodes().forEach(node => {
    graph.clearItemStates(node, ['highlight']);
  });
};

const matchKeyword = (source, keyword) => source?.toLowerCase().includes(keyword);

const findGraphUserItem = keyword => {
  if (!graph) {
    return null;
  }
  return graph.getNodes().find(node => {
    const model = node.getModel();
    return model.nodeType === 'user' && (
      matchKeyword(model.name, keyword) ||
      matchKeyword(model.jobNo, keyword)
    );
  }) || null;
};

const focusUserItem = targetItem => {
  highlightedNodeId.value = targetItem.getID();
  graph.setItemState(targetItem, 'highlight', true);
  graph.focusItem(targetItem);
  graph.zoomTo(Math.max(graph.getZoom(), 1));
};

const locateUserAcrossVisibleScope = async keyword => {
  if (!canSwitchDepartment.value || !selectedDeptId.value) {
    return null;
  }

  const candidates = await axios.get('/api/organization/search', {
    params: { keyword }
  });
  const targetUser = Array.isArray(candidates) ? candidates[0] : null;
  if (!targetUser?.jobNo) {
    return null;
  }

  const detail = await axios.get(`/api/organization/user/${targetUser.jobNo}`);
  const targetDeptId = detail.department?.id ?? null;
  if (!targetDeptId || targetDeptId === selectedDeptId.value) {
    return null;
  }

  selectedDeptId.value = targetDeptId;
  await fetchData();
  return findGraphUserItem(keyword);
};

const handleSearch = async (value) => {
  const keyword = (value ?? searchKeyword.value).trim();
  if (!keyword) {
    clearHighlight();
    return;
  }
  if (!graph) {
    return;
  }
  clearHighlight();
  const normalizedKeyword = keyword.toLowerCase();
  let targetItem = findGraphUserItem(normalizedKeyword);

  if (!targetItem) {
    try {
      targetItem = await locateUserAcrossVisibleScope(keyword);
    } catch (error) {
      // ignore
    }
  }

  if (!targetItem) {
    message.warning(canSwitchDepartment.value && selectedDeptId.value
      ? '当前部门未找到匹配人员'
      : '当前视图下未找到匹配人员');
    return;
  }

  focusUserItem(targetItem);
};

const handleSearchChange = (event) => {
  if (!event?.target?.value) {
    clearHighlight();
  }
};

const handleDepartmentChange = async value => {
  selectedDeptId.value = value ?? null;
  searchKeyword.value = '';
  clearHighlight();
  await fetchData();
};

const handleFitView = () => {
  graph?.fitView(24);
  if (graph?.getZoom() > 1.5) {
    graph.zoomTo(1.5);
  }
  graph?.fitCenter();
};

const handleResetView = () => {
  searchKeyword.value = '';
  clearHighlight();
  graph?.fitView(24);
  graph?.fitCenter();
};

const goToUserDetail = (jobNo) => {
  riskModal.visible = false;
  router.push({
    name: 'UserDetail',
    query: { jobNo }
  });
};

const handleResize = () => {
  if (!rawTreeData.value) {
    return;
  }
  const wrapperWidth = Math.max((scrollAreaRef.value?.clientWidth || 1200) - 16, 760);
  if (Math.abs(wrapperWidth - lastWrapperWidth) < 4) {
    return;
  }
  renderGraph(rawTreeData.value);
  if (searchKeyword.value) {
    handleSearch(searchKeyword.value);
  }
};

const scheduleResize = () => {
  if (resizeTimer) {
    window.clearTimeout(resizeTimer);
  }
  resizeTimer = window.setTimeout(() => {
    handleResize();
  }, 120);
};

onMounted(async () => {
  if (canSwitchDepartment.value) {
    await fetchDepartmentOptions();
  } else if (currentUser.value.department?.id) {
    selectedDeptId.value = currentUser.value.department.id;
  }
  await fetchData();
  window.addEventListener('resize', scheduleResize);
});

onUnmounted(() => {
  window.removeEventListener('resize', scheduleResize);
  if (resizeTimer) {
    window.clearTimeout(resizeTimer);
  }
  destroyGraph();
});
</script>

<style scoped>
.org-chart-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #001529 0%, #0a1628 50%, #001529 100%);
  padding: 20px;
  /* font-family inherited from global */
}

/* 顶部信息栏 */
.top-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 24px;
  background: linear-gradient(90deg, rgba(0, 212, 255, 0.1), transparent);
  border-radius: 12px;
  border: 1px solid rgba(0, 212, 255, 0.3);
  margin-bottom: 24px;
  box-shadow: 0 0 30px rgba(0, 212, 255, 0.1);
}

.title-section {
  display: flex;
  flex-direction: column;
}

.main-title {
  font-size: 24px;
  font-weight: 700;
  background: linear-gradient(90deg, #00d4ff, #00ffff);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.sub-title {
  font-size: 14px;
  color: #8892b0;
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
  color: #8892b0;
}

.time-display .time {
  font-size: 20px;
  font-weight: 600;
  color: #00d4ff;
  font-family: var(--font-mono);
}

/* 数据卡片网格 */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
  margin-bottom: 24px;
}

.stat-card {
  background: rgba(0, 21, 41, 0.8);
  border-radius: 12px;
  padding: 20px;
  border: 1px solid rgba(0, 212, 255, 0.2);
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
}

.stat-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 2px;
  background: linear-gradient(90deg, transparent, #00d4ff, transparent);
}

.stat-card:hover {
  transform: translateY(-4px);
  border-color: rgba(0, 212, 255, 0.5);
  box-shadow: 0 8px 30px rgba(0, 212, 255, 0.2);
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
}

.card-icon {
  font-size: 20px;
}

.card-label {
  font-size: 13px;
  color: #8892b0;
}

.card-value {
  font-size: 36px;
  font-weight: 700;
  color: #fff;
}

.key-card {
  border-color: rgba(239, 68, 68, 0.4);
}

.key-card::before {
  background: linear-gradient(90deg, transparent, #ef4444, transparent);
}

.warning-card {
  border-color: rgba(245, 158, 11, 0.4);
}

.warning-card::before {
  background: linear-gradient(90deg, transparent, #f59e0b, transparent);
}

.success-card {
  border-color: rgba(16, 185, 129, 0.4);
}

.success-card::before {
  background: linear-gradient(90deg, transparent, #10b981, transparent);
}

.highlight-red { color: #ef4444; }
.highlight-orange { color: #f59e0b; }
.highlight-green { color: #10b981; }

.clickable-card {
  cursor: pointer;
}

.clickable-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 40px rgba(0, 212, 255, 0.3);
}

.card-click-hint {
  font-size: 11px;
  color: #64ffda;
  opacity: 0.7;
  margin-top: 4px;
}

/* 图表卡片 */
.chart-card {
  background: rgba(0, 21, 41, 0.8);
  border-radius: 12px;
  padding: 20px;
  border: 1px solid rgba(0, 212, 255, 0.2);
}

.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  flex-wrap: wrap;
  gap: 12px;
}

.chart-title {
  font-size: 16px;
  font-weight: 600;
  color: #fff;
}

.chart-actions {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.toolbar-input {
  width: 260px;
  background: rgba(0, 21, 41, 0.8);
  border-color: rgba(0, 212, 255, 0.3);
}

.toolbar-select {
  width: 220px;
  background: rgba(0, 21, 41, 0.8);
  border-color: rgba(0, 212, 255, 0.3);
}

.chart-toolbar {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
  padding-bottom: 12px;
  border-bottom: 1px solid rgba(73, 86, 112, 0.3);
  margin-bottom: 16px;
}

.legend-list {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
}

.legend-item {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  color: #8892b0;
}

.legend-dot {
  width: 14px;
  height: 14px;
  border-radius: 50%;
  border: 2px solid transparent;
  background: #fff;
}

.legend-dot.normal {
  border-color: #00d4ff;
}

.legend-dot.key {
  background: #fff1f0;
  border-color: #ef4444;
}

.legend-dot.risk {
  background: #fffbe6;
  border-color: #f59e0b;
}

.legend-dot.attention {
  background: #f6ffed;
  border-color: #10b981;
}

.toolbar-tip {
  color: #8892b0;
  font-size: 12px;
}

.chart-wrapper {
  position: relative;
  border: 1px solid rgba(0, 212, 255, 0.2);
  border-radius: 12px;
  background: rgba(0, 21, 41, 0.6);
  overflow: hidden;
}

.chart-spin {
  width: 100%;
}

.canvas-scroll-area {
  height: calc(100vh - 400px);
  min-height: 460px;
  max-height: 720px;
  overflow: auto;
  padding: 12px;
}

.canvas-scroll-area::-webkit-scrollbar {
  width: 8px;
  height: 8px;
}

.canvas-scroll-area::-webkit-scrollbar-thumb {
  background: rgba(0, 212, 255, 0.4);
  border-radius: 4px;
}

.canvas-scroll-area::-webkit-scrollbar-track {
  background: rgba(0, 21, 41, 0.6);
  border-radius: 4px;
}

.canvas-stage {
  position: relative;
  min-width: 100%;
}

.g6-container {
  width: 100%;
  height: 100%;
}

.node-tooltip {
  position: absolute;
  z-index: 12;
  min-width: 180px;
  background: rgba(0, 21, 41, 0.96);
  border: 1px solid rgba(0, 212, 255, 0.3);
  border-radius: 10px;
  padding: 12px;
  box-shadow: 0 10px 24px rgba(0, 212, 255, 0.2);
  pointer-events: none;
}

.tooltip-header {
  font-weight: 600;
  margin-bottom: 8px;
  color: #00d4ff;
}

.tooltip-body p {
  margin: 4px 0;
  color: #ccd6f6;
  font-size: 12px;
}

:deep(.org-chart-minimap) {
  position: absolute;
  right: 16px;
  bottom: 16px;
  border: 1px solid rgba(0, 212, 255, 0.3);
  border-radius: 8px;
  overflow: hidden;
  background: rgba(0, 21, 41, 0.9);
  box-shadow: 0 8px 20px rgba(0, 212, 255, 0.2);
}


/* 响应式设计 */
@media (max-width: 768px) {
  .top-bar {
    flex-direction: column;
    gap: 16px;
  }

  .info-section {
    flex-direction: column;
    align-items: center;
    gap: 16px;
  }

  .time-display {
    align-items: center;
  }

  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .canvas-scroll-area {
    height: calc(100vh - 480px);
  }
}
</style>
