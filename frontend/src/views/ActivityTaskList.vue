<template>
  <div class="activity-task-container">
    <a-card :bordered="false" class="list-card">
      <template #title>
        <div class="header-toolbar">
          <span class="page-title">活动任务</span>
          <div class="header-ops">
            <a-button v-if="canPublish" type="primary" @click="goToCreate">
              <template #icon><PlusOutlined /></template>
              创建任务
            </a-button>
          </div>
        </div>
      </template>

      <a-table
        :columns="columns"
        :data-source="tasks"
        :loading="loading"
        row-key="id"
        :pagination="{ pageSize: 10 }"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'taskType'">
            <a-tag>{{ typeMap[record.taskType] || record.taskType }}</a-tag>
          </template>
          <template v-if="column.key === 'progress'">
            <a-progress :percent="record.totalTargets ? Math.round(record.submittedCount / record.totalTargets * 100) : 0" size="small" style="width: 120px" />
            <span class="progress-text">{{ record.submittedCount }}/{{ record.totalTargets }}</span>
          </template>
          <template v-if="column.key === 'status'">
            <a-tag v-if="record.status === 'CLOSED'" color="default">已结束</a-tag>
            <a-tag v-else-if="record.overdue" color="red">逾期未完成</a-tag>
            <a-tag v-else color="green">进行中</a-tag>
          </template>
          <template v-if="column.key === 'myStatus'">
            <template v-if="record.myStatus === 'SUBMITTED'">
              <a-tag color="green">已提交</a-tag>
              <a-tag v-if="record.participated" color="blue" style="margin-left: 4px">已参与</a-tag>
              <a-tag v-else color="default" style="margin-left: 4px">未参与</a-tag>
            </template>
            <template v-else-if="record.myStatus === 'PENDING'">
              <a-tag color="orange">待填写</a-tag>
            </template>
            <template v-else>-</template>
          </template>
          <template v-if="column.key === 'deadline'">
            {{ formatTime(record.deadline) }}
          </template>
          <template v-if="column.key === 'action'">
            <a-space>
              <a-button type="link" size="small" @click="goToDetail(record)">详情</a-button>
              <template v-if="record.myStatus === 'PENDING' && record.status !== 'CLOSED' && !record.overdue">
                <a-button type="link" size="small" @click="goToSubmit(record)">去填写</a-button>
              </template>
              <template v-if="record.myStatus === 'SUBMITTED' && record.status !== 'CLOSED' && !record.overdue">
                <a-button type="link" size="small" @click="goToSubmit(record)">修改</a-button>
              </template>
              <template v-if="canPublish && record.status === 'ACTIVE'">
                <a-popconfirm title="确认结束此任务？" @confirm="handleClose(record.id)">
                  <a-button type="link" danger size="small">结束</a-button>
                </a-popconfirm>
              </template>
              <template v-if="canPublish">
                <a-button type="link" size="small" @click="handleExport(record)">导出</a-button>
                <a-popconfirm title="确认删除此任务？所有相关记录也会被删除。" @confirm="handleDelete(record.id)">
                  <a-button type="link" danger size="small">删除</a-button>
                </a-popconfirm>
              </template>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import { PlusOutlined } from '@ant-design/icons-vue';
import { useRouter } from 'vue-router';
import { message } from 'ant-design-vue';
import dayjs from 'dayjs';
import axios from '../utils/axios';
import { getCurrentUser } from '../utils/auth';
import { canPublishActivity as canPublishActivityFn } from '../utils/constants';

const router = useRouter();
const tasks = ref([]);
const loading = ref(false);
const user = getCurrentUser();
const canPublish = computed(() => canPublishActivityFn(user));

const typeMap = { LEARNING: '学习', MEETING: '会议', DRILL: '演练', TRAINING: '培训', OTHER: '其他' };

const columns = computed(() => {
  const base = [
    { title: '活动名称', dataIndex: 'title', key: 'title', width: 200, ellipsis: true },
    { title: '类型', key: 'taskType', width: 80 },
    { title: '截止时间', key: 'deadline', width: 160 },
    { title: '状态', key: 'status', width: 110 },
  ];
  if (canPublish.value) {
    base.push({ title: '完成进度', key: 'progress', width: 200 });
  } else {
    base.push({ title: '我的状态', key: 'myStatus', width: 100 });
  }
  base.push({ title: '操作', key: 'action', fixed: 'right', width: 200 });
  return base;
});

const formatTime = (t) => t ? dayjs(t).format('YYYY-MM-DD HH:mm') : '-';

const fetchTasks = async () => {
  loading.value = true;
  try {
    const res = await axios.get('/api/activity-tasks/list');
    tasks.value = res;
  } catch (e) {
    message.error('获取任务列表失败');
  } finally {
    loading.value = false;
  }
};

const goToCreate = () => router.push('/activity-task-create');
const goToDetail = (record) => router.push(`/activity-task-detail/${record.id}`);
const goToSubmit = (record) => router.push(`/activity-record-submit/${record.id}`);

const handleClose = async (id) => {
  try {
    await axios.put(`/api/activity-tasks/${id}/close`);
    message.success('任务已结束');
    fetchTasks();
  } catch (e) {
    message.error('操作失败');
  }
};

const handleDelete = async (id) => {
  try {
    await axios.delete(`/api/activity-tasks/${id}`);
    message.success('任务已删除');
    fetchTasks();
  } catch (e) {
    message.error('删除失败');
  }
};

const handleExport = async (record) => {
  try {
    const response = await axios.get(`/api/activity-tasks/${record.id}/export`, { responseType: 'blob' });
    const url = window.URL.createObjectURL(new Blob([response]));
    const link = document.createElement('a');
    link.href = url;
    link.setAttribute('download', `参与人员_${record.title}.xlsx`);
    document.body.appendChild(link);
    link.click();
    link.remove();
    window.URL.revokeObjectURL(url);
    message.success('导出成功');
  } catch (e) {
    message.error('导出失败');
  }
};

onMounted(fetchTasks);
</script>

<style scoped>
.activity-task-container {
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
  color: #fff;
}
.progress-text {
  font-size: 12px;
  color: #999;
  margin-left: 8px;
}
</style>
