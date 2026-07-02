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
import { ref, reactive, onMounted, nextTick } from 'vue';
import { gsap } from 'gsap';
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

const fetchData = async () => {
  try {
    const res = await axios.get('/api/organization/dashboard');
    const body = res.data || res;
    Object.assign(summary, body.summary || {});
    departments.value = body.departments || [];
    allUsers.value = body.userList || [];
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

onMounted(() => {
  fetchData();

  nextTick(() => {
    gsap.from('.stat-card', {
      opacity: 0,
      y: 30,
      duration: 0.5,
      stagger: 0.08,
      ease: 'power2.out',
      delay: 0.2
    });
  });
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

  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
