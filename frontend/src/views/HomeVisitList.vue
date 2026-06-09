<template>
  <div class="home-visit-container">
    <a-card :bordered="false" class="list-card">
      <template #title>
        <div class="header-toolbar">
          <span class="page-title">家访记录</span>
          <div class="header-ops">
            <a-space>
              <a-select v-model:value="filterVisitType" placeholder="家访类型" allow-clear style="width: 140px">
                <a-select-option v-for="opt in VISIT_TYPE_OPTIONS" :key="opt.value" :value="opt.value">{{ opt.label }}</a-select-option>
              </a-select>
              <a-range-picker v-model:value="dateRange" format="YYYY-MM-DD" style="width: 240px" />
              <a-button type="primary" @click="fetchData">
                <template #icon><SearchOutlined /></template>
                查询
              </a-button>
              <a-button v-if="hasSubordinates" @click="handleExport">
                <template #icon><DownloadOutlined /></template>
                导出Excel
              </a-button>
              <a-button v-if="hasSubordinates" type="primary" @click="goToAdd">
                <template #icon><PlusOutlined /></template>
                新增家访
              </a-button>
            </a-space>
          </div>
        </div>
      </template>

      <a-table
        :columns="columns"
        :data-source="visits"
        :loading="loading"
        row-key="id"
        :pagination="{ pageSize: 10 }"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'operator'">
            {{ record.operatorName || record.operatorJobNo }}
          </template>
          <template v-if="column.key === 'target'">
            {{ record.targetName || record.targetJobNo }}
          </template>
          <template v-if="column.key === 'visitTime'">
            {{ formatDate(record.visitTime) }}
          </template>
          <template v-if="column.key === 'action'">
            <a-space>
              <a-button type="link" size="small" @click="goToDetail(record)">查看</a-button>
              <template v-if="currentUser?.jobNo === 'admin' || currentUser?.role === 'ADMIN_GLOBAL' || isBureauLeaderFn(currentUser) || record.operatorJobNo === currentUser?.jobNo">
                <a-button type="link" size="small" @click="goToEdit(record)">编辑</a-button>
                <a-popconfirm title="确认删除？" @confirm="handleDelete(record.id)">
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
import { ref, onMounted } from 'vue';
import { PlusOutlined, SearchOutlined, DownloadOutlined } from '@ant-design/icons-vue';
import { useRouter } from 'vue-router';
import { message } from 'ant-design-vue';
import dayjs from 'dayjs';
import axios from '../utils/axios';
import { getCurrentUser } from '../utils/auth';
import { VISIT_TYPE_OPTIONS, hasSubordinates as hasSubordinatesFn, isBureauLeader as isBureauLeaderFn } from '../utils/constants';

const currentUser = getCurrentUser();
const hasSubordinates = hasSubordinatesFn(currentUser);

const router = useRouter();
const visits = ref([]);
const loading = ref(false);
const filterVisitType = ref(undefined);
const dateRange = ref(null);

const columns = [
  { title: '家访时间', key: 'visitTime', width: 170 },
  { title: '家访人', key: 'operator', width: 120 },
  { title: '被家访人', key: 'target', width: 120 },
  { title: '家访类型', dataIndex: 'visitType', key: 'visitType', width: 120 },
  { title: '家访地点', dataIndex: 'location', key: 'location', width: 150 },
  { title: '家访内容', dataIndex: 'content', key: 'content', ellipsis: true },
  { title: '操作', key: 'action', fixed: 'right', width: 200 }
];

const formatDate = (d) => d ? dayjs(d).format('YYYY-MM-DD HH:mm') : '-';

const fetchData = async () => {
  loading.value = true;
  try {
    const params = {};
    if (filterVisitType.value) params.visitType = filterVisitType.value;
    if (dateRange.value && dateRange.value[0]) params.startDate = dateRange.value[0].format('YYYY-MM-DD');
    if (dateRange.value && dateRange.value[1]) params.endDate = dateRange.value[1].format('YYYY-MM-DD');
    const res = await axios.get('/api/home-visits/list', { params });
    visits.value = res;
  } catch (error) {
    message.error('获取家访记录失败');
  } finally {
    loading.value = false;
  }
};

const goToAdd = () => router.push('/home-visit-add');

const goToDetail = (record) => {
  router.push(`/home-visit-add?id=${record.id}`);
};

const goToEdit = (record) => {
  router.push(`/home-visit-add?id=${record.id}`);
};

const handleDelete = async (id) => {
  try {
    await axios.delete(`/api/home-visits/delete/${id}`);
    message.success('删除成功');
    fetchData();
  } catch {
    message.error('删除失败');
  }
};

const handleExport = async () => {
  try {
    const params = {};
    if (filterVisitType.value) params.visitType = filterVisitType.value;
    if (dateRange.value && dateRange.value[0]) params.startDate = dateRange.value[0].format('YYYY-MM-DD');
    if (dateRange.value && dateRange.value[1]) params.endDate = dateRange.value[1].format('YYYY-MM-DD');
    const response = await axios.get('/api/export/home-visits', { params, responseType: 'blob' });
    const url = window.URL.createObjectURL(new Blob([response]));
    const link = document.createElement('a');
    link.href = url;
    link.setAttribute('download', '家访记录.xlsx');
    document.body.appendChild(link);
    link.click();
    link.remove();
    window.URL.revokeObjectURL(url);
    message.success('导出成功');
  } catch (error) {
    message.error('导出失败，请重试');
  }
};

onMounted(fetchData);
</script>

<style scoped>
.home-visit-container {
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
  font-weight: 600;
  background: linear-gradient(90deg, #ccd6f6, #e6edf8);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}
</style>
