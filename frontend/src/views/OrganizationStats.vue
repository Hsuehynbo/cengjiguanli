<template>
  <div class="org-stats-container">
    <!-- 顶部信息栏 -->
    <div class="top-bar">
      <div class="title-section">
        <span class="main-title">{{ deptName }}</span>
        <span class="sub-title">{{ pageTitle }}</span>
      </div>
      <div class="info-section">
        <ClockDisplay />
        <a-button type="primary" @click="$router.back()">
          <template #icon><LeftOutlined /></template>
          返回列表
        </a-button>
      </div>
    </div>

    <!-- 数据卡片区域 -->
    <div class="stats-grid">
      <div class="stat-card tech-card">
        <div class="card-header">
          <span class="card-icon">💬</span>
          <span class="card-label">本月谈话完成率</span>
        </div>
        <div class="card-value">{{ stats.rate }}%</div>
        <div class="card-trend">
          <span class="trend-icon">✅</span>
          <span>已完成 {{ stats.completed }} 人</span>
        </div>
      </div>

      <div class="stat-card tech-card">
        <div class="card-header">
          <span class="card-icon">🏠</span>
          <span class="card-label">本月家访完成率</span>
        </div>
        <div class="card-value">{{ stats.homeVisitRate }}%</div>
        <div class="card-trend">
          <span class="trend-icon">🎯</span>
          <span>已完成 {{ stats.homeVisitCompleted }} 人</span>
        </div>
      </div>

      <div class="stat-card tech-card key-card clickable-card" @click="openRiskUserModal('KEY')">
        <div class="card-header">
          <span class="card-icon">⚠️</span>
          <span class="card-label">重点人员</span>
        </div>
        <div class="card-value highlight-red">{{ stats.keyCount }}</div>
        <div class="card-trend">
          <span class="trend-icon">🔴</span>
          <span>需重点关注</span>
        </div>
        <div class="card-click-hint">点击查看详情</div>
      </div>

      <div class="stat-card tech-card warning-card clickable-card" @click="openRiskUserModal('RISK')">
        <div class="card-header">
          <span class="card-icon">⚠️</span>
          <span class="card-label">风险人员</span>
        </div>
        <div class="card-value highlight-orange">{{ stats.riskCount }}</div>
        <div class="card-trend">
          <span class="trend-icon">🟠</span>
          <span>需加强管控</span>
        </div>
        <div class="card-click-hint">点击查看详情</div>
      </div>

      <div class="stat-card tech-card success-card clickable-card" @click="openRiskUserModal('ATTENTION')">
        <div class="card-header">
          <span class="card-icon">👁️</span>
          <span class="card-label">关注人员</span>
        </div>
        <div class="card-value highlight-green">{{ stats.attentionCount }}</div>
        <div class="card-trend">
          <span class="trend-icon">🟢</span>
          <span>持续观察中</span>
        </div>
        <div class="card-click-hint">点击查看详情</div>
      </div>

      <div class="stat-card tech-card">
        <div class="card-header">
          <span class="card-icon">👤</span>
          <span class="card-label">普通人员</span>
        </div>
        <div class="card-value">{{ stats.normalCount }}</div>
        <div class="card-trend">
          <span class="trend-icon">📋</span>
          <span>正常状态</span>
        </div>
      </div>
    </div>

    <!-- 图表区域 -->
    <div class="charts-grid">
      <!-- 风险人员分布饼图 -->
      <div class="chart-card tech-card">
        <div class="chart-header">
          <span class="chart-title">风险人员分布</span>
        </div>
        <div ref="pieChartRef" class="chart-container"></div>
      </div>

      <!-- 工作推进柱状图 -->
      <div class="chart-card tech-card wide-card">
        <div class="chart-header">
          <span class="chart-title">工作推进情况</span>
        </div>
        <div ref="barChartRef" class="chart-container"></div>
      </div>
    </div>

    <!-- 人员完成情况列表 -->
    <div class="table-card tech-card">
      <div class="table-header">
        <span class="table-title">人员完成情况列表</span>
      </div>
      <a-table
        :columns="columns"
        :data-source="userList"
        row-key="jobNo"
        size="small"
        :pagination="false"
        :expand-icon="null"
        class="custom-table"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'riskLevel'">
            <a-tag :color="riskTagMap[record.riskLevel]?.color">
              {{ riskTagMap[record.riskLevel]?.label || '普通人员' }}
            </a-tag>
          </template>
          <template v-else-if="column.key === 'talkStatus'">
            <a-tag :color="record.completed ? 'green' : 'orange'">
              {{ record.completed ? '已完成' : '未完成' }}
            </a-tag>
          </template>
          <template v-else-if="column.key === 'homeVisitStatus'">
            <a-tag :color="record.homeVisitCompleted ? 'green' : 'orange'">
              {{ record.homeVisitCompleted ? '已完成' : '未完成' }}
            </a-tag>
          </template>
          <template v-else-if="column.key === 'action'">
            <a-button type="link" @click="goToUserDetail(record.jobNo)">查看详情</a-button>
          </template>
        </template>
      </a-table>
    </div>

    <!-- 风险人员列表模态框 -->
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
          <template v-if="column.key === 'riskLevel'">
            <a-tag :color="riskTagMap[record.riskLevel]?.color">
              {{ riskTagMap[record.riskLevel]?.label }}
            </a-tag>
          </template>
          <template v-else-if="column.key === 'action'">
            <a-button type="link" @click="goToUserDetail(record.jobNo)">查看详情</a-button>
          </template>
        </template>
      </a-table>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted, watch, computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { LeftOutlined } from '@ant-design/icons-vue';
import * as echarts from 'echarts';
import axios from '../utils/axios';
import { message } from 'ant-design-vue';
import { getCurrentUser } from '../utils/auth';
import { RISK_LEVEL_MAP } from '../utils/constants';
import ClockDisplay from '../components/ClockDisplay.vue';

const route = useRoute()
const router = useRouter()
const deptId = route.params.id
const deptName = ref(route.query.name || '部门')
const currentUser = getCurrentUser()

const isUnitHead = () => {
  const pos = currentUser.position || ''
  return (pos.includes('所长') || pos.includes('队长') || pos.includes('科长') || pos.includes('主任')) && !pos.startsWith('副')
}

const pageTitle = computed(() => deptName.value ? `${deptName.value} - 部门详情` : '部门详情');

const loading = ref(false);

const stats = ref({
  total: 0,
  completed: 0,
  pending: 0,
  rate: 0,
  homeVisitCompleted: 0,
  homeVisitPending: 0,
  homeVisitRate: 0,
  keyCount: 0,
  riskCount: 0,
  attentionCount: 0,
  normalCount: 0
});
const userList = ref([]);

const pieChartRef = ref(null);
const barChartRef = ref(null);
let pieChartInstance = null;
let barChartInstance = null;

const riskTagMap = RISK_LEVEL_MAP;

const riskModal = reactive({
  visible: false,
  title: '',
  users: []
});

const riskColumns = [
  { title: '姓名', dataIndex: 'name', key: 'name', width: 160 },
  { title: '警号', dataIndex: 'jobNo', key: 'jobNo', width: 120 },
  { title: '所属部门', dataIndex: 'deptName', key: 'deptName', width: 200 },
  { title: '职务', dataIndex: 'position', key: 'position', width: 160 },
  { title: '风险等级', key: 'riskLevel', align: 'center', width: 120 },
  { title: '操作', key: 'action', align: 'center', width: 100 }
];

const columns = [
  { title: '姓名', dataIndex: 'name', key: 'name', width: 120 },
  { title: '工号', dataIndex: 'jobNo', key: 'jobNo', width: 100 },
  { title: '职务', dataIndex: 'position', key: 'position', width: 120 },
  { title: '风险等级', key: 'riskLevel', align: 'center', width: 120 },
  { title: '本月谈话', key: 'talkStatus', align: 'center', width: 100 },
  { title: '半年家访', key: 'homeVisitStatus', align: 'center', width: 100 },
  { title: '操作', key: 'action', align: 'center', width: 100 }
];

const initCharts = () => {
  if (!pieChartRef.value || !barChartRef.value) {
    setTimeout(initCharts, 100);
    return;
  }

  // 饼图：风险人员分布
  pieChartInstance = echarts.init(pieChartRef.value);
  pieChartInstance.setOption({
    tooltip: {
      trigger: 'item',
      backgroundColor: 'rgba(0, 21, 41, 0.9)',
      borderColor: '#00d4ff',
      textStyle: { color: '#fff' }
    },
    legend: {
      bottom: 10,
      textStyle: { color: '#8892b0' }
    },
    color: ['#10b981', '#00d4ff', '#f59e0b', '#ef4444'],
    series: [{
      name: '风险分布',
      type: 'pie',
      radius: ['40%', '70%'],
      avoidLabelOverlap: false,
      itemStyle: {
        borderRadius: 10,
        borderColor: 'rgba(8, 22, 40, 0.8)',
        borderWidth: 2
      },
      label: {
        show: true,
        color: '#8892b0',
        fontSize: 12
      },
      emphasis: {
        label: {
          show: true,
          fontSize: 14,
          fontWeight: 'bold',
          color: '#fff'
        }
      },
      data: [
        { value: stats.value.normalCount, name: '普通人员' },
        { value: stats.value.attentionCount, name: '关注人员' },
        { value: stats.value.riskCount, name: '风险人员' },
        { value: stats.value.keyCount, name: '重点人员' }
      ]
    }]
  });

  // 柱状图：工作推进情况
  barChartInstance = echarts.init(barChartRef.value);
  barChartInstance.setOption({
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(0, 21, 41, 0.9)',
      borderColor: '#00d4ff',
      textStyle: { color: '#fff' }
    },
    legend: {
      data: ['谈话完成', '家访完成'],
      textStyle: { color: '#8892b0' }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: ['已完成', '未完成'],
      axisLine: { lineStyle: { color: '#495670' } },
      axisLabel: { color: '#8892b0' }
    },
    yAxis: {
      type: 'value',
      axisLine: { lineStyle: { color: '#495670' } },
      axisLabel: { color: '#8892b0' },
      splitLine: { lineStyle: { color: '#2a3f5f' } }
    },
    series: [
      {
        name: '谈话完成',
        type: 'bar',
        barWidth: '40%',
        data: [stats.value.completed, stats.value.total - stats.value.completed],
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#00d4ff' },
            { offset: 1, color: '#0099cc' }
          ]),
          borderRadius: [4, 4, 0, 0]
        }
      },
      {
        name: '家访完成',
        type: 'bar',
        barWidth: '40%',
        data: [stats.value.homeVisitCompleted, stats.value.total - stats.value.homeVisitCompleted],
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#7c3aed' },
            { offset: 1, color: '#5b21b6' }
          ]),
          borderRadius: [4, 4, 0, 0]
        }
      }
    ]
  });
};

const fetchStats = async () => {
  loading.value = true;
  try {
    const res = await axios.get(`/api/organization/stats/${deptId}`);
    stats.value = res.overview;
    userList.value = res.userList;

    // 数据更新后重新渲染图表
    setTimeout(() => {
      if (pieChartInstance) {
        pieChartInstance.setOption({
          series: [{
            data: [
              { value: stats.value.normalCount, name: '普通人员' },
              { value: stats.value.attentionCount, name: '关注人员' },
              { value: stats.value.riskCount, name: '风险人员' },
              { value: stats.value.keyCount, name: '重点人员' }
            ]
          }]
        });
      }
      if (barChartInstance) {
        barChartInstance.setOption({
          series: [
            { data: [stats.value.completed, stats.value.total - stats.value.completed] },
            { data: [stats.value.homeVisitCompleted, stats.value.total - stats.value.homeVisitCompleted] }
          ]
        });
      }
    }, 100);
  } catch (error) {
    message.error(error.response?.data?.message || '获取统计数据失败');
  } finally {
    loading.value = false;
  }
};

const goToUserDetail = (jobNo) => {
  riskModal.visible = false;
  router.push({
    name: 'UserDetail',
    query: { jobNo }
  });
};

const openRiskUserModal = (riskLevel) => {
  const filteredUsers = userList.value.filter(user => user.riskLevel === riskLevel);
  riskModal.title = `${riskTagMap[riskLevel]?.label}列表`;
  riskModal.users = filteredUsers;
  riskModal.visible = true;
};

const handleResize = () => {
  pieChartInstance?.resize();
  barChartInstance?.resize();
};

onMounted(() => {
  fetchStats();
  setTimeout(initCharts, 200);
  window.addEventListener('resize', handleResize);
});

onUnmounted(() => {
  window.removeEventListener('resize', handleResize);
  pieChartInstance?.dispose();
  barChartInstance?.dispose();
});
</script>

<style scoped>
.org-stats-container {
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
  align-items: center;
  gap: 24px;
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
  margin-bottom: 8px;
}

.card-trend {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: #64ffda;
}

.trend-icon {
  font-size: 12px;
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

/* 图表网格 */
.charts-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(350px, 1fr));
  gap: 20px;
  margin-bottom: 24px;
}

.chart-card {
  background: rgba(0, 21, 41, 0.8);
  border-radius: 12px;
  padding: 20px;
  border: 1px solid rgba(0, 212, 255, 0.2);
}

.chart-header {
  margin-bottom: 16px;
}

.chart-title {
  font-size: 14px;
  font-weight: 600;
  color: #fff;
}

.chart-container {
  height: 280px;
}

.wide-card {
  grid-column: span 2;
}

/* 表格卡片 */
.table-card {
  background: #081628;
  border-radius: 12px;
  padding: 24px;
  border: none;
}

.table-header {
  margin-bottom: 16px;
}

.table-title {
  font-size: 16px;
  font-weight: 600;
  color: #fff;
}

.custom-table {
  background: #081628;
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

  .charts-grid {
    grid-template-columns: 1fr;
  }

  .wide-card {
    grid-column: span 1;
  }

  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>