<template>
  <div class="talk-record-container">
    <a-card :bordered="false" class="list-card">
      <template #title>
        <div class="header-toolbar">
          <span class="page-title">谈话记录中心</span>
          <div class="header-ops">
            <a-space>
              <a-input-search
                v-model:value="searchKeyword"
                placeholder="搜索姓名/内容"
                style="width: 250px"
                class="dark-search"
                @search="fetchTalkRecords"
              />
              <a-button v-if="hasSubordinates" @click="handleExport">
                <template #icon><DownloadOutlined /></template>
                导出Excel
              </a-button>
              <a-button v-if="hasSubordinates" type="primary" @click="goToAddTalk">
                <template #icon><PlusOutlined /></template>
                发起新谈话
              </a-button>
            </a-space>
          </div>
        </div>
      </template>

      <a-table
        :columns="columns"
        :data-source="talkRecords"
        :loading="loading"
        row-key="id"
        :pagination="{ pageSize: 10 }"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'talker'">
            {{ record.talkerName || record.talkerJobNo }}
          </template>
          <template v-if="column.key === 'target'">
            {{ record.targetName || record.targetJobNo }}
          </template>
          <template v-if="column.key === 'action'">
            <a-space>
              <a-button type="link" size="small" @click="viewDetail(record.id)">详情</a-button>
              <template v-if="currentUser?.jobNo === 'admin' || currentUser?.role === 'ADMIN_GLOBAL' || isBureauLeaderFn(currentUser) || record.talkerJobNo === currentUser?.jobNo">
                <a-button type="link" size="small" @click="goToEdit(record.id)">编辑</a-button>
                <a-popconfirm title="确认删除此谈话记录？" @confirm="handleDelete(record.id)" ok-text="确认" cancel-text="取消">
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
import { PlusOutlined, DownloadOutlined } from '@ant-design/icons-vue';
import { useRouter } from 'vue-router';
import { message } from 'ant-design-vue';
import axios from '../utils/axios';
import { getCurrentUser } from '../utils/auth';
import { hasSubordinates as hasSubordinatesFn, isBureauLeader as isBureauLeaderFn } from '../utils/constants';

const currentUser = getCurrentUser();
const hasSubordinates = hasSubordinatesFn(currentUser);

const router = useRouter();
const talkRecords = ref([]);
const loading = ref(false);
const searchKeyword = ref('');

const columns = [
  { title: '谈话时间', dataIndex: 'talkTime', key: 'talkTime', width: 180 },
  { title: '谈话人', key: 'talker', width: 120 },
  { title: '被谈话人', key: 'target', width: 120 },
  { title: '谈话类型', dataIndex: 'talkType', key: 'talkType', width: 120 },
  { title: '谈话地点', dataIndex: 'location', key: 'location', width: 150 },
  { title: '谈话内容', dataIndex: 'content', key: 'content', ellipsis: true },
  { title: '操作', key: 'action', fixed: 'right', width: 200 }
];

const fetchTalkRecords = async () => {
  loading.value = true;
  try {
    const params = {};
    if (searchKeyword.value) params.keyword = searchKeyword.value;
    const res = await axios.get('/api/talk-records/list', { params });
    talkRecords.value = res;
  } catch (error) {
    message.error('获取谈话记录失败，请重试');
  } finally {
    loading.value = false;
  }
};

const goToAddTalk = () => {
  router.push('/talk-add');
};

const viewDetail = (id) => {
  router.push(`/talk-detail/${id}`);
};

const goToEdit = (id) => {
  router.push(`/talk-detail/${id}?edit=1`);
};

const handleDelete = async (id) => {
  try {
    await axios.delete(`/api/talk-records/delete/${id}`);
    message.success('删除成功');
    fetchTalkRecords();
  } catch {
    message.error('删除失败');
  }
};

const handleExport = async () => {
  try {
    const response = await axios.get('/api/export/talk-records', { responseType: 'blob' });
    const url = window.URL.createObjectURL(new Blob([response]));
    const link = document.createElement('a');
    link.href = url;
    link.setAttribute('download', '谈话记录.xlsx');
    document.body.appendChild(link);
    link.click();
    link.remove();
    window.URL.revokeObjectURL(url);
    message.success('导出成功');
  } catch (error) {
    message.error('导出失败，请重试');
  }
};

onMounted(fetchTalkRecords);
</script>

<style scoped>
.talk-record-container {
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
  color: var(--text-title);
}
</style>
