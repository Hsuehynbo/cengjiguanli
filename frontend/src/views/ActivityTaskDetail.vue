<template>
  <div class="task-detail-container">
    <a-card :bordered="false" class="form-card">
      <template #title>
        <div class="header-toolbar">
          <span class="page-title">任务详情</span>
          <a-button @click="goBack">返回列表</a-button>
        </div>
      </template>

      <a-spin :spinning="loading">
        <!-- 任务信息 -->
        <a-descriptions :column="2" size="small" bordered class="task-info">
          <a-descriptions-item label="活动名称" :span="2">{{ detail.title }}</a-descriptions-item>
          <a-descriptions-item label="活动类型">{{ typeMap[detail.taskType] || detail.taskType }}</a-descriptions-item>
          <a-descriptions-item label="创建人">{{ detail.createdByName }}</a-descriptions-item>
          <a-descriptions-item label="截止时间">{{ formatTime(detail.deadline) }}</a-descriptions-item>
          <a-descriptions-item label="状态">
            <a-tag v-if="detail.status === 'CLOSED'" color="default">已结束</a-tag>
            <a-tag v-else color="green">进行中</a-tag>
          </a-descriptions-item>
          <a-descriptions-item label="活动要求" :span="2">{{ detail.description || '无' }}</a-descriptions-item>
        </a-descriptions>

        <!-- 统计卡片 -->
        <div class="stats-row">
          <div class="stat-card">
            <div class="stat-value">{{ detail.totalTargets || 0 }}</div>
            <div class="stat-label">下发单位</div>
          </div>
          <div class="stat-card">
            <div class="stat-value success">{{ detail.submittedCount || 0 }}</div>
            <div class="stat-label">已提交</div>
          </div>
          <div class="stat-card">
            <div class="stat-value warning">{{ detail.pendingCount || 0 }}</div>
            <div class="stat-label">未提交</div>
          </div>
          <div class="stat-card">
            <div class="stat-value primary">{{ detail.totalParticipants || 0 }}</div>
            <div class="stat-label">总参与人数</div>
          </div>
        </div>

        <!-- 单位完成情况表格 -->
        <a-divider>各单位完成情况</a-divider>

        <a-table
          :columns="targetColumns"
          :data-source="detail.targets || []"
          row-key="deptId"
          :pagination="false"
          size="small"
        >
          <template #bodyCell="{ column, record }">
            <template v-if="column.key === 'status'">
              <a-tag v-if="record.status === 'SUBMITTED'" color="green">已提交</a-tag>
              <a-tag v-else color="orange">未提交</a-tag>
            </template>
            <template v-if="column.key === 'submitTime'">
              {{ formatTime(record.submitTime) || '-' }}
            </template>
            <template v-if="column.key === 'participantCount'">
              {{ record.participantCount || '-' }}
            </template>
            <template v-if="column.key === 'action'">
              <a-button v-if="record.recordId" type="link" size="small" @click="viewRecord(record)">查看记录</a-button>
              <span v-else style="color: #999">-</span>
            </template>
          </template>
        </a-table>

        <!-- 导出按钮 -->
        <div v-if="isGlobalAdmin" style="margin-top: 16px; text-align: right">
          <a-button @click="handleExport">
            <template #icon><DownloadOutlined /></template>
            导出参与人员
          </a-button>
        </div>
      </a-spin>
    </a-card>

  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { message } from 'ant-design-vue';
import { DownloadOutlined } from '@ant-design/icons-vue';
import dayjs from 'dayjs';
import axios from '../utils/axios';
import { getCurrentUser } from '../utils/auth';
import { isGlobalAdmin as isGlobalAdminFn } from '../utils/constants';

const route = useRoute();
const router = useRouter();
const taskId = route.params.id;
const loading = ref(false);
const detail = ref({});

const user = getCurrentUser();
const isGlobalAdmin = computed(() => isGlobalAdminFn(user));

const typeMap = { LEARNING: '学习', MEETING: '会议', DRILL: '演练', TRAINING: '培训', OTHER: '其他' };

const targetColumns = [
  { title: '单位', dataIndex: 'deptName', key: 'deptName' },
  { title: '状态', key: 'status', width: 100 },
  { title: '提交时间', key: 'submitTime', width: 170 },
  { title: '参与人数', key: 'participantCount', width: 100 },
  { title: '操作', key: 'action', width: 120 }
];

const formatTime = (t) => t ? dayjs(t).format('YYYY-MM-DD HH:mm') : '-';

const fetchDetail = async () => {
  loading.value = true;
  try {
    const res = await axios.get(`/api/activity-tasks/${taskId}`);
    detail.value = res;
  } catch (e) {
    message.error('获取详情失败');
  } finally {
    loading.value = false;
  }
};

const viewRecord = (record) => {
  router.push(`/activity-record-detail/${record.recordId}`);
};

const handleExport = async () => {
  try {
    const response = await axios.get(`/api/activity-tasks/${taskId}/export`, { responseType: 'blob' });
    const url = window.URL.createObjectURL(new Blob([response]));
    const link = document.createElement('a');
    link.href = url;
    link.setAttribute('download', `参与人员_${detail.value.title}.xlsx`);
    document.body.appendChild(link);
    link.click();
    link.remove();
    window.URL.revokeObjectURL(url);
    message.success('导出成功');
  } catch (e) {
    message.error('导出失败');
  }
};

const goBack = () => router.push('/activity-tasks');

onMounted(fetchDetail);
</script>

<style scoped>
.task-detail-container {
  padding: 24px;
  background: transparent;
}
.header-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.page-title {
  font-size: 18px;
  font-weight: bold;
  color: var(--text-title);
}
.task-info {
  margin-bottom: 24px;
}
.stats-row {
  display: flex;
  gap: 16px;
  margin: 24px 0;
}
.stat-card {
  flex: 1;
  text-align: center;
  padding: 16px;
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: 12px;
}
.stat-value {
  font-size: 28px;
  font-weight: 600;
  font-family: var(--font-mono);
}
.stat-value.success { color: #10b981; }
.stat-value.warning { color: #f59e0b; }
.stat-value.primary { color: var(--accent); }
.stat-label {
  font-size: 13px;
  color: var(--text-muted);
  margin-top: 4px;
}
</style>
