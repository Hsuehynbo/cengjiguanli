<template>
  <div class="dashboard-container">
    <!-- 顶部信息栏 -->
    <div class="top-bar">
      <div class="title-section">
        <span class="main-title">云和县公安局层级管理系统</span>
<!--        <span class="sub-title">全局管理看板</span>-->
      </div>
      <div class="info-section">
        <ClockDisplay />
      </div>
    </div>

    <!-- 数据卡片区域 -->
    <a-skeleton :loading="loading" active :paragraph="{ rows: 2 }">
      <div class="stats-grid">

        <div class="stat-card tech-card">
          <div class="card-header">
            <div class="card-icon-wrap cyan"><Icon icon="ri:chat-3-fill" width="20" height="20" /></div>
            <span class="card-label">本月谈话完成率</span>
          </div>
          <div class="card-value"><NumberCounter :value="summary.talkRate" />%</div>
          <div class="card-trend">
            <Icon icon="ri:checkbox-circle-fill" width="14" height="14" style="color: #10b981" />
            <span>已完成</span>
          </div>
        </div>

        <div class="stat-card tech-card">
          <div class="card-header">
            <div class="card-icon-wrap cyan"><Icon icon="ri:home-heart-fill" width="20" height="20" /></div>
            <span class="card-label">本月家访数</span>
          </div>
          <div class="card-value"><NumberCounter :value="summary.homeVisitCount" /></div>
          <div class="card-trend">
            <Icon icon="ri:target-fill" width="14" height="14" style="color: var(--accent)" />
            <span>本月累计</span>
          </div>
        </div>

        <div class="stat-card tech-card key-card clickable-card" @click="openRiskUserModal('KEY')">
          <div class="card-header">
            <div class="card-icon-wrap red"><Icon icon="ri:error-warning-fill" width="20" height="20" /></div>
            <span class="card-label">重点人员</span>
          </div>
          <div class="card-value highlight-red"><NumberCounter :value="summary.keyCount" color="#ef4444" /></div>
          <div class="card-trend">
            <Icon icon="ri:alert-fill" width="14" height="14" style="color: #ef4444" />
            <span>需重点关注</span>
          </div>
          <div class="card-click-hint">点击查看详情</div>
        </div>

        <div class="stat-card tech-card warning-card clickable-card" @click="openRiskUserModal('RISK')">
          <div class="card-header">
            <div class="card-icon-wrap orange"><Icon icon="ri:shield-check-fill" width="20" height="20" /></div>
            <span class="card-label">风险人员</span>
          </div>
          <div class="card-value highlight-orange"><NumberCounter :value="summary.riskCount" color="#f59e0b" /></div>
          <div class="card-trend">
            <Icon icon="ri:alarm-warning-fill" width="14" height="14" style="color: #f59e0b" />
            <span>需加强管控</span>
          </div>
          <div class="card-click-hint">点击查看详情</div>
        </div>

        <div class="stat-card tech-card success-card clickable-card" @click="openRiskUserModal('ATTENTION')">
          <div class="card-header">
            <div class="card-icon-wrap green"><Icon icon="ri:eye-fill" width="20" height="20" /></div>
            <span class="card-label">关注人员</span>
          </div>
          <div class="card-value highlight-green"><NumberCounter :value="summary.attentionCount" color="#10b981" /></div>
          <div class="card-trend">
            <Icon icon="ri:eye-line" width="14" height="14" style="color: #10b981" />
            <span>持续观察中</span>
          </div>
          <div class="card-click-hint">点击查看详情</div>
        </div>

        <div class="stat-card tech-card clickable-card" @click="$router.push('/talk')">
          <div class="card-header">
            <div class="card-icon-wrap cyan"><Icon icon="ri:task-fill" width="20" height="20" /></div>
            <span class="card-label">待办任务</span>
          </div>
          <div class="card-value"><NumberCounter :value="summary.pendingTasks" /></div>
          <div class="card-trend">
            <Icon icon="ri:time-fill" width="14" height="14" style="color: var(--text-muted)" />
            <span>今日需处理</span>
          </div>
          <div class="card-click-hint">点击查看详情</div>
        </div>

        <div class="stat-card tech-card activity-card clickable-card" @click="$router.push('/activity-tasks')">
          <div class="card-header">
            <div class="card-icon-wrap purple"><Icon icon="ri:calendar-event-fill" width="20" height="20" /></div>
            <span class="card-label">活动任务</span>
          </div>
          <div class="card-value"><NumberCounter :value="summary.activeTasks" /></div>
          <div class="card-trend">
            <Icon icon="ri:pin-distance-fill" width="14" height="14" style="color: #7c3aed" />
            <span>进行中 {{ summary.activeTasks }} / 已结束 {{ summary.closedTasks }}</span>
          </div>
          <div class="card-click-hint">点击查看详情</div>
        </div>
      </div>
    </a-skeleton>

    <!-- 图表区域 -->
    <a-skeleton :loading="loading" active :paragraph="{ rows: 6 }">
      <div class="charts-grid">

        <!-- 折线图：谈话记录趋势 -->
        <div class="chart-card tech-card wide-card">
          <div class="chart-header">
            <span class="chart-title">近7天谈话记录趋势</span>
          </div>
          <div ref="lineChart" class="chart-container"></div>
        </div>

        <!-- 柱状图：各部门完成率对比 -->
        <div class="chart-card tech-card wide-card">
          <div class="chart-header">
            <span class="chart-title">各部门工作完成率对比</span>
          </div>
          <div ref="deptCompareChart" class="chart-container"></div>
        </div>
      </div>
    </a-skeleton>

    <!-- 底部数据表格 -->
    <a-skeleton :loading="loading" active :paragraph="{ rows: 4 }">
      <div class="table-card tech-card">
        <div class="table-header">
          <span class="table-title">部门工作推进一览</span>
        </div>
        <a-table
          :columns="columns"
          :data-source="departments"
          row-key="id"
          size="small"
          :pagination="false"
          class="custom-table"
        >
          <template #bodyCell="{ column, record }">
            <template v-if="column.key === 'talkRate'">
              <div class="progress-wrapper">
                <a-progress :percent="record.rate" :show-info="false" size="small" />
                <span class="progress-text">{{ record.rate }}%</span>
              </div>
            </template>
            <template v-else-if="column.key === 'homeVisitRate'">
              <div class="progress-wrapper">
                <a-progress :percent="record.homeVisitRate" :show-info="false" size="small" stroke-color="#722ed1" />
                <span class="progress-text">{{ record.homeVisitRate }}%</span>
              </div>
            </template>
            <template v-else-if="column.key === 'riskSummary'">
              <a-space>
                <a-tag color="red">重点 {{ record.keyCount }}</a-tag>
                <a-tag color="gold">风险 {{ record.riskCount }}</a-tag>
                <a-tag color="green">关注 {{ record.attentionCount }}</a-tag>
              </a-space>
            </template>
            <template v-else-if="column.key === 'action'">
              <a-button type="link" size="small" @click="goToDeptStats(record)">查看部门详情</a-button>
            </template>
          </template>
        </a-table>
      </div>
    </a-skeleton>

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
import { ref, reactive, onMounted, onUnmounted, nextTick } from 'vue';
import { gsap } from 'gsap';
import * as echarts from 'echarts';
import { useRouter } from 'vue-router';
import { message } from 'ant-design-vue';
import axios from '../utils/axios';
import NumberCounter from '../components/NumberCounter.vue';
import ClockDisplay from '../components/ClockDisplay.vue';
import { Icon } from '@iconify/vue';
import { RISK_LEVEL_MAP } from '../utils/constants';

const router = useRouter();
const loading = ref(true);

const summary = reactive({
  deptCount: 0,
  totalUsers: 0,
  talkRate: 0,
  homeVisitCount: 0,
  keyCount: 0,
  riskCount: 0,
  attentionCount: 0,
  pendingTasks: 0,
  activeTasks: 0,
  closedTasks: 0,
  totalTasks: 0
});

const departments = ref([]);
const allUsers = ref([]);

const lineChart = ref(null);
const deptCompareChart = ref(null);

let lineChartInstance = null;
let deptCompareChartInstance = null;

const columns = [
  { title: '部门', dataIndex: 'name', key: 'name', width: 180 },
  { title: '人数', dataIndex: 'total', key: 'total', width: 80, align: 'center' },
  { title: '谈话完成率', key: 'talkRate', width: 180 },
  { title: '家访完成率', key: 'homeVisitRate', width: 180 },
  { title: '风险分布', key: 'riskSummary' },
  { title: '操作', key: 'action', width: 120, align: 'center' }
];

const riskModal = reactive({
  visible: false,
  title: '',
  users: []
});

const riskTagMap = RISK_LEVEL_MAP;

const riskColumns = [
  { title: '姓名', dataIndex: 'name', key: 'name', width: 160 },
  { title: '警号', dataIndex: 'jobNo', key: 'jobNo', width: 120 },
  { title: '所属部门', dataIndex: 'deptName', key: 'deptName', width: 200 },
  { title: '职务', dataIndex: 'position', key: 'position', width: 160 },
  { title: '风险等级', key: 'riskLevel', align: 'center', width: 120 },
  { title: '操作', key: 'action', align: 'center', width: 100 }
];

const initCharts = () => {
  if (!lineChart.value || !deptCompareChart.value) {
    setTimeout(initCharts, 100);
    return;
  }

  // 折线图配置（空数据占位）
  lineChartInstance = echarts.init(lineChart.value);
  lineChartInstance.setOption({
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(30, 41, 59, 0.95)',
      borderColor: '#3b82f6',
      textStyle: { color: '#f1f5f9' }
    },
    legend: {
      data: ['谈话记录', '家访记录'],
      textStyle: { color: '#94a3b8' }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: [],
      axisLine: { lineStyle: { color: '#cbd5e1' } },
      axisLabel: { color: '#94a3b8' }
    },
    yAxis: {
      type: 'value',
      axisLine: { lineStyle: { color: '#cbd5e1' } },
      axisLabel: { color: '#94a3b8' },
      splitLine: { lineStyle: { color: '#e2e8f0' } }
    },
    series: [
      {
        name: '谈话记录',
        type: 'line',
        smooth: true,
        data: [],
        lineStyle: { color: '#2563eb', width: 3 },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(37, 99, 235, 0.3)' },
            { offset: 1, color: 'rgba(37, 99, 235, 0.05)' }
          ])
        },
        symbol: 'circle',
        symbolSize: 8,
        itemStyle: { color: '#2563eb' }
      },
      {
        name: '家访记录',
        type: 'line',
        smooth: true,
        data: [],
        lineStyle: { color: '#7c3aed', width: 3 },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(124, 58, 237, 0.3)' },
            { offset: 1, color: 'rgba(124, 58, 237, 0.05)' }
          ])
        },
        symbol: 'circle',
        symbolSize: 8,
        itemStyle: { color: '#7c3aed' }
      }
    ]
  });

  // 部门完成率对比柱状图
  deptCompareChartInstance = echarts.init(deptCompareChart.value);
  deptCompareChartInstance.setOption({
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(30, 41, 59, 0.95)',
      borderColor: '#3b82f6',
      textStyle: { color: '#f1f5f9' },
      formatter: (params) => {
        let tip = params[0].name + '<br/>';
        params.forEach(p => {
          tip += `${p.marker} ${p.seriesName}: ${p.value}%<br/>`;
        });
        return tip;
      }
    },
    legend: {
      data: ['谈话完成率', '家访完成率'],
      textStyle: { color: '#94a3b8' }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '8%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: [],
      axisLine: { lineStyle: { color: '#cbd5e1' } },
      axisLabel: { color: '#94a3b8', rotate: 30 }
    },
    yAxis: {
      type: 'value',
      max: 100,
      axisLine: { lineStyle: { color: '#cbd5e1' } },
      axisLabel: { color: '#94a3b8', formatter: '{value}%' },
      splitLine: { lineStyle: { color: '#e2e8f0' } }
    },
    series: [
      {
        name: '谈话完成率',
        type: 'bar',
        barWidth: '30%',
        data: [],
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#2563eb' },
            { offset: 1, color: '#1d4ed8' }
          ]),
          borderRadius: [4, 4, 0, 0]
        }
      },
      {
        name: '家访完成率',
        type: 'bar',
        barWidth: '30%',
        data: [],
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

const updateCharts = async () => {
  // 等待图表实例就绪
  if (!lineChartInstance && !deptCompareChartInstance) {
    setTimeout(() => updateCharts(), 100);
    return;
  }

  // 更新部门完成率对比图
  if (deptCompareChartInstance && departments.value.length) {
    deptCompareChartInstance.setOption({
      xAxis: { data: departments.value.map(d => d.name) },
      series: [
        { data: departments.value.map(d => d.rate || 0) },
        { data: departments.value.map(d => d.homeVisitRate || 0) }
      ]
    });
  }

  // 获取趋势数据并更新折线图
  try {
    const trendRes = await axios.get('/api/talk-records/trend', { params: { days: 7 } });
    const trend = trendRes.data || trendRes;
    if (lineChartInstance) {
      lineChartInstance.setOption({
        xAxis: { data: trend.dates || [] },
        series: [
          { data: trend.talks || [] },
          { data: trend.homeVisits || [] }
        ]
      });
    }
  } catch (e) {
    message.error('获取趋势数据失败');
  }

};

const fetchData = async () => {
  try {
    const res = await axios.get('/api/organization/dashboard');
    const body = res.data || res;
    Object.assign(summary, body.summary || {});
    departments.value = body.departments || [];
    allUsers.value = body.userList || [];
    await updateCharts();
  } catch (error) {
    message.error('获取全局数据失败');
  } finally {
    loading.value = false;
  }
};

const goToDeptStats = (record) => {
  router.push({
    name: 'OrganizationStats',
    params: { id: String(record.id) },
    query: { name: record.name }
  });
};

const openRiskUserModal = (riskLevel) => {
  const filteredUsers = allUsers.value.filter(user => user.riskLevel === riskLevel);
  riskModal.title = `${riskTagMap[riskLevel]?.label}列表`;
  riskModal.users = filteredUsers;
  riskModal.visible = true;
};

const goToUserDetail = (jobNo) => {
  riskModal.visible = false;
  router.push({
    name: 'UserDetail',
    query: { jobNo }
  });
};

const handleResize = () => {
  lineChartInstance?.resize();
  deptCompareChartInstance?.resize();
};

onMounted(() => {
  initCharts();
  fetchData();
  window.addEventListener('resize', handleResize);

  nextTick(() => {
    gsap.from('.stat-card', {
      opacity: 0,
      y: 30,
      duration: 0.5,
      stagger: 0.08,
      ease: 'power2.out',
      delay: 0.2
    });
    gsap.from('.chart-card', {
      opacity: 0,
      y: 40,
      duration: 0.6,
      stagger: 0.1,
      ease: 'power2.out',
      delay: 0.6
    });
  });
});

onUnmounted(() => {
  window.removeEventListener('resize', handleResize);
  lineChartInstance?.dispose();
  deptCompareChartInstance?.dispose();
});
</script>

<style scoped>
.dashboard-container {
  min-height: 100vh;
  background: transparent;
  padding: 20px;
}

/* 顶部信息栏 */
.top-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 24px;
  background: var(--bg-card);
  border-radius: var(--radius-lg);
  border: 1px solid var(--border-color);
  margin-bottom: 24px;
}

.title-section {
  display: flex;
  flex-direction: column;
}

.main-title {
  font-size: 24px;
  font-weight: 700;
  color: var(--accent);
}

.sub-title {
  font-size: 14px;
  color: var(--text-muted);
  margin-top: 4px;
}

/* 数据卡片网格 */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 14px;
  margin-bottom: 24px;
}

.stat-card {
  background: var(--bg-card);
  border-radius: var(--radius-lg);
  padding: 16px 20px;
  border: 1px solid var(--border-color);
  transition: all 0.35s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
  display: flex;
  align-items: center;
  gap: 16px;
}

.stat-card:hover {
  transform: translateY(-3px);
  border-color: var(--accent);
  box-shadow: 0 4px 24px var(--accent-shadow);
}

.card-header {
  display: flex;
  align-items: center;
  gap: 10px;
  flex: 1;
  min-width: 0;
}

.card-icon-wrap {
  width: 36px;
  height: 36px;
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.card-icon-wrap.cyan {
  background: var(--accent-light);
  color: var(--accent);
}

.card-icon-wrap.red {
  background: var(--danger-light);
  color: var(--danger);
}

.card-icon-wrap.orange {
  background: var(--warning-light);
  color: var(--warning);
}

.card-icon-wrap.green {
  background: var(--success-light);
  color: var(--success);
}

.card-icon-wrap.purple {
  background: var(--purple-light);
  color: var(--purple);
}

.card-label {
  font-size: 13px;
  color: var(--text-muted);
}

.card-value {
  font-size: 28px;
  font-weight: 700;
  color: var(--text-title);
  line-height: 1;
  flex-shrink: 0;
  font-family: var(--font-mono);
}

.card-trend {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: var(--text-muted);
  flex-shrink: 0;
  white-space: nowrap;
}

.key-card {
  border-color: var(--danger);
}

.warning-card {
  border-color: var(--warning);
}

.success-card {
  border-color: var(--success);
}

.activity-card {
  border-color: var(--purple);
}

.highlight-red { color: #ef4444; }
.highlight-orange { color: #f59e0b; }
.highlight-green { color: #10b981; }

.clickable-card {
  cursor: pointer;
}

.clickable-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 4px 24px var(--accent-shadow);
}

.card-click-hint {
  display: none;
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
  background: var(--bg-card);
  border-radius: var(--radius-lg);
  padding: 20px;
  border: 1px solid var(--border-color);
}

.chart-header {
  margin-bottom: 16px;
}

.chart-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-title);
}

.chart-container {
  height: 250px;
}

.wide-card {
  grid-column: span 2;
}

.wide-card .chart-container {
  height: 280px;
}

/* 表格卡片 */
.table-card {
  background: var(--bg-card);
  border-radius: var(--radius-lg);
  padding: 24px;
  border: 1px solid var(--border-color);
}

.table-header {
  margin-bottom: 16px;
}

.table-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-title);
}

.custom-table {
  background: transparent;
}






.progress-wrapper {
  display: flex;
  align-items: center;
  gap: 8px;
}

.progress-text {
  font-size: 12px;
  color: var(--text-muted);
  min-width: 40px;
}


/* 响应式设计 */
@media (max-width: 768px) {
  .top-bar {
    flex-direction: column;
    gap: 16px;
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
