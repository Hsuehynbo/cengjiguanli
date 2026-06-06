<template>
  <div class="user-detail-container">
    <a-card :bordered="false" class="detail-card">
      <template #title>
        <div class="header-content">
          <a-button type="link" @click="$router.back()"><left-outlined /> 返回列表</a-button>
          <span class="page-title">人员详细信息</span>
        </div>
      </template>

      <a-spin :spinning="loading">
        <div v-if="user" class="detail-content">
          <!-- 基础信息 -->
          <div class="info-section">
            <div class="section-title">基础资料</div>
            <a-descriptions bordered column="2">
              <a-descriptions-item label="姓名">{{ user.name }}</a-descriptions-item>
              <a-descriptions-item label="工号">{{ user.jobNo }}</a-descriptions-item>
              <a-descriptions-item label="所属部门">{{ user.department?.deptName }}</a-descriptions-item>
              <a-descriptions-item label="当前职务">{{ user.position }}</a-descriptions-item>
              <a-descriptions-item label="风险等级">
                <a-tag :color="riskTagMap[user.riskLevel]?.color || 'blue'">
                  {{ riskTagMap[user.riskLevel]?.label || '普通人员' }}
                </a-tag>
              </a-descriptions-item>
            </a-descriptions>
          </div>

          <a-divider />

          <!-- 谈话记录 -->
          <div class="records-section">
            <div class="section-header">
              <div class="section-title">谈话历史记录</div>
              <a-button v-if="hasSubordinates" type="primary" @click="goToAddTalk">
                <template #icon><plus-outlined /></template>
                新增谈话
              </a-button>
            </div>

            <a-table
              :columns="columns"
              :data-source="talkRecords"
              row-key="id"
              :pagination="{ pageSize: 5 }"
            >
              <template #bodyCell="{ column, record }">
                <template v-if="column.key === 'action'">
                  <a-space>
                    <a-button type="link" @click="viewDetail(record)">查看详情</a-button>
                    <template v-if="isRecordOwner(record.talkerJobNo)">
                      <a-button type="link" @click="editRecord(record)">编辑</a-button>
                      <a-popconfirm
                        title="确定要删除这条谈话记录吗？"
                        @confirm="deleteRecord(record.id)"
                      >
                        <a-button type="link" danger>删除</a-button>
                      </a-popconfirm>
                    </template>
                  </a-space>
                </template>
              </template>
            </a-table>
          </div>

          <!-- 家访记录 -->
          <div class="records-section">
            <div class="section-header">
              <div class="section-title">家访历史记录</div>
              <a-button v-if="hasSubordinates" type="primary" @click="goToAddHomeVisit">
                <template #icon><plus-outlined /></template>
                新增家访
              </a-button>
            </div>

            <a-table
                :columns="homeVisitColumns"
                :data-source="homeVisitRecords"
                row-key="id"
                :pagination="{ pageSize: 5 }"
            >
              <template #bodyCell="{ column, record }">
                <template v-if="column.key === 'action'">
                  <a-space>
                    <a-button type="link" @click="viewHomeVisitDetail(record)">查看详情</a-button>
                    <template v-if="isRecordOwner(record.operatorJobNo)">
                      <a-button type="link" @click="editHomeVisitRecord(record)">编辑</a-button>
                      <a-popconfirm
                          title="确定要删除这条家访记录吗？"
                          @confirm="deleteHomeVisitRecord(record.id)"
                      >
                        <a-button type="link" danger>删除</a-button>
                      </a-popconfirm>
                    </template>
                  </a-space>
                </template>
              </template>
            </a-table>
          </div>

          <!-- 层级管理历史记录 -->
          <div class="records-section">
            <div class="section-title">层级管理历史记录</div>
            <a-table
                :columns="hierarchyColumns"
                :data-source="hierarchyHistory"
                row-key="id"
                size="small"
                :pagination="{ pageSize: 5 }"
            >
              <template #bodyCell="{ column, record }">
                <template v-if="column.key === 'period'">
                  {{ dayjs(record.startDate).format('YYYY.MM') }} -
                  {{ record.endDate ? dayjs(record.endDate).format('YYYY.MM') : '至今' }}
                </template>
                <template v-else-if="column.key === 'manager'">
                  {{ record.managerName || '' }} {{ record.managerJobNo ? `(${record.managerJobNo})` : '' }}
                </template>
              </template>
            </a-table>
          </div>

          <!-- 违规记录 -->
          <div class="records-section">
            <div class="section-title">违规记录</div>
            <a-table
                :columns="violationColumns"
                :data-source="violationRecords"
                row-key="id"
                size="small"
                :pagination="{ pageSize: 5 }"
            >
              <template #bodyCell="{ column, record }">
                <template v-if="column.key === 'violationTime'">
                  {{ dayjs(record.violationTime).format('YYYY年MM月') }}
                </template>
              </template>
            </a-table>
          </div>

        </div>
        <a-empty v-else description="未找到该人员信息" />
      </a-spin>
    </a-card>


    <!-- 谈话详情弹窗 -->
    <a-modal
      v-model:open="detailVisible"
      title="谈话详细内容"
      footer=""
      width="650px"
    >
      <div v-if="selectedRecord" class="record-detail-view">
        <a-descriptions bordered column="1" size="small">
          <a-descriptions-item label="谈话时间">{{ selectedRecord.talkTime }}</a-descriptions-item>
          <a-descriptions-item label="谈话类型">{{ selectedRecord.talkType }}</a-descriptions-item>
          <a-descriptions-item label="谈话地点">{{ selectedRecord.location }}</a-descriptions-item>
          <a-descriptions-item label="谈话内容">
            <div class="pre-content">{{ selectedRecord.content }}</div>
          </a-descriptions-item>
          <a-descriptions-item label="现场照片" v-if="selectedRecord.photo">
            <img :src="getFileUrl(selectedRecord.photo)" style="max-width: 100%; border-radius: 4px;" alt="现场照片" />
          </a-descriptions-item>
        </a-descriptions>
      </div>
    </a-modal>

    <!-- 家访详情弹窗 -->
    <a-modal
        v-model:open="homeVisitDetailVisible"
        title="家访详细内容"
        footer=""
        width="650px"
    >
      <div v-if="selectedHomeVisit" class="record-detail-view">
        <a-descriptions bordered column="1" size="small">
          <a-descriptions-item label="家访时间">{{ dayjs(selectedHomeVisit.visitTime).format('YYYY-MM-DD HH:mm:ss') }}</a-descriptions-item>
          <a-descriptions-item label="家访类型">{{ selectedHomeVisit.visitType }}</a-descriptions-item>
          <a-descriptions-item label="家访地点">{{ selectedHomeVisit.location }}</a-descriptions-item>
          <a-descriptions-item label="家访内容">
            <div class="pre-content">{{ selectedHomeVisit.content }}</div>
          </a-descriptions-item>
        </a-descriptions>
      </div>
    </a-modal>

    <!-- 谈话编辑弹窗 -->
    <a-modal
      v-model:open="editModal.visible"
      title="修改谈话记录"
      @ok="handleEditSubmit"
      :confirmLoading="editModal.submitting"
      width="600px"
      destroyOnClose
    >
      <a-form :model="editForm" :rules="editRules" ref="editFormRef" layout="vertical">
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="谈话时间" name="talkTime">
              <a-date-picker v-model:value="editForm.talkTime" show-time style="width: 100%" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="谈话类型" name="talkType">
              <a-select v-model:value="editForm.talkType">
                <a-select-option v-for="opt in TALK_TYPE_OPTIONS" :key="opt" :value="opt">{{ opt }}</a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
        </a-row>
        <a-form-item label="谈话地点" name="location">
          <a-input v-model:value="editForm.location" />
        </a-form-item>
        <a-form-item label="谈话内容" name="content">
          <a-textarea v-model:value="editForm.content" :rows="4" />
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- 家访新增/编辑弹窗 -->
    <a-modal
        v-model:open="homeVisitModal.visible"
        :title="homeVisitModal.id ? '修改家访记录' : '新增家访记录'"
        @ok="handleHomeVisitSubmit"
        :confirmLoading="homeVisitModal.submitting"
        width="600px"
        destroyOnClose
    >
      <a-form :model="homeVisitForm" :rules="homeVisitRules" ref="homeVisitFormRef" layout="vertical">
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="家访时间" name="visitTime">
              <a-date-picker v-model:value="homeVisitForm.visitTime" show-time style="width: 100%" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="家访类型" name="visitType">
              <a-select v-model:value="homeVisitForm.visitType">
                <a-select-option value="例行家访">例行家访</a-select-option>
                <a-select-option value="特殊家访">特殊家访</a-select-option>
                <a-select-option value="慰问家访">慰问家访</a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
        </a-row>
        <a-form-item label="家访地点" name="location">
          <a-input v-model:value="homeVisitForm.location" />
        </a-form-item>
        <a-form-item label="家访内容" name="content">
          <a-textarea v-model:value="homeVisitForm.content" :rows="4" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive, watch, computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { LeftOutlined, PlusOutlined } from '@ant-design/icons-vue';
import axios from '../utils/axios';
import { message } from 'ant-design-vue';
import dayjs from 'dayjs';
import { getCurrentUser } from '../utils/auth';
import { getFileUrl } from '../utils/axios';
import { RISK_LEVEL_MAP, TALK_TYPE_OPTIONS, hasSubordinates as hasSubordinatesFn, isBureauLeader as isBureauLeaderFn } from '../utils/constants';

const currentUser = getCurrentUser();
const hasSubordinates = hasSubordinatesFn(currentUser);
const isRecordOwner = (jobNo) => currentUser?.jobNo === 'admin' || currentUser?.role === 'ADMIN_GLOBAL' || isBureauLeaderFn(currentUser) || jobNo === currentUser?.jobNo;

const route = useRoute();
const router = useRouter();
const user = ref(null);
const loading = ref(false);
const talkRecords = ref([]);
const homeVisitRecords = ref([]);
const hierarchyHistory = ref([]);
const violationRecords = ref([]);

const detailVisible = ref(false);
const selectedRecord = ref(null);

const homeVisitDetailVisible = ref(false);
const selectedHomeVisit = ref(null);

const editFormRef = ref(null);
const editModal = reactive({
  visible: false,
  submitting: false,
  id: null
});

const homeVisitFormRef = ref(null);
const homeVisitModal = reactive({
  visible: false,
  submitting: false,
  id: null
});

const editForm = reactive({
  talkTime: null,
  talkType: '',
  location: '',
  content: ''
});

const homeVisitForm = reactive({
  visitTime: null,
  visitType: '',
  location: '',
  content: ''
});

const editRules = {
  talkTime: [{ required: true, message: '请选择时间' }],
  talkType: [{ required: true, message: '请选择类型' }],
  location: [{ required: true, message: '请输入地点' }],
  content: [{ required: true, message: '请输入内容' }]
};

const homeVisitRules = {
  visitTime: [{ required: true, message: '请选择时间' }],
  visitType: [{ required: true, message: '请选择类型' }],
  location: [{ required: true, message: '请输入地点' }],
  content: [{ required: true, message: '请输入内容' }]
};

const formatDate = (dateStr) => {
  if (!dateStr) return '';
  try {
    const date = dayjs(dateStr);
    if (date.isValid()) {
      return date.format('YYYY-MM-DD HH:mm:ss');
    }
    return dateStr;
  } catch (e) {
    return dateStr;
  }
};

const columns = [
  { title: '谈话时间', dataIndex: 'talkTime', key: 'talkTime', customRender: (text) => formatDate(text) },
  { title: '类型', dataIndex: 'talkType', key: 'talkType' },
  { title: '地点', dataIndex: 'location', key: 'location' },
  { title: '操作', key: 'action', align: 'center' }
];

const homeVisitColumns = [
  {
    title: '家访时间',
    dataIndex: 'visitTime',
    key: 'visitTime',
    customRender: ({ text }) => text ? dayjs(text).format('YYYY-MM-DD HH:mm:ss') : ''
  },
  { title: '类型', dataIndex: 'visitType', key: 'visitType' },
  { title: '地点', dataIndex: 'location', key: 'location' },
  { title: '操作', key: 'action', align: 'center' }
];

const hierarchyColumns = [
  { title: '时间段', key: 'period', width: 200 },
  { title: '层级单位', dataIndex: 'unitName', key: 'unitName' },
  { title: '管理人', key: 'manager' }
];

const violationColumns = [
  { title: '违规时间', key: 'violationTime', width: 120 },
  { title: '违规事由', dataIndex: 'reason', key: 'reason' },
  { title: '处理结果', dataIndex: 'punishment', key: 'punishment' }
];

const riskTagMap = RISK_LEVEL_MAP;

const fetchUserInfo = async (jobNo) => {
  loading.value = true;
  try {
    const res = await axios.get(`/api/organization/user/${jobNo}`);
    user.value = res;
    fetchTalkRecords(jobNo);
    fetchHomeVisits(jobNo);
    fetchExtraInfo(jobNo);
  } catch (error) {
    message.error('获取用户信息失败');
  } finally {
    loading.value = false;
  }
};

const fetchExtraInfo = async (jobNo) => {
  try {
    const [hierarchy, violations] = await Promise.all([
      axios.get(`/api/users/${jobNo}/hierarchy-history`),
      axios.get(`/api/users/${jobNo}/violation-records`)
    ]);
    hierarchyHistory.value = hierarchy;
    violationRecords.value = violations;
  } catch (e) {
    // ignore
  }
};

const fetchTalkRecords = async (jobNo) => {
  try {
    const res = await axios.get(`/api/talk-records/list?targetJobNo=${jobNo}`);
    talkRecords.value = res;
  } catch (error) {
    // ignore
  }
};

const fetchHomeVisits = async (jobNo) => {
  try {
    const res = await axios.get(`/api/home-visits/list?targetJobNo=${jobNo}`);
    homeVisitRecords.value = res;
  } catch (error) {
    // ignore
  }
};

const goToAddTalk = () => {
  router.push({
    name: 'TalkAdd',
    query: { targetJobNo: user.value.jobNo }
  });
};

const goToAddHomeVisit = () => {
  router.push({
    name: 'HomeVisitAdd',
    query: { targetJobNo: user.value.jobNo }
  });
};

const viewDetail = (record) => {
  router.push({
    name: 'TalkDetail',
    query: { id: record.id }
  });
};

const viewHomeVisitDetail = (record) => {
  router.push({
    name: 'HomeVisitAdd',
    query: { id: record.id }
  });
};

const editRecord = (record) => {
  editModal.id = record.id;
  Object.assign(editForm, {
    talkTime: dayjs(record.talkTime),
    talkType: record.talkType,
    location: record.location,
    content: record.content
  });
  editModal.visible = true;
};

const editHomeVisitRecord = (record) => {
  homeVisitModal.id = record.id;
  Object.assign(homeVisitForm, {
    visitTime: dayjs(record.visitTime),
    visitType: record.visitType,
    location: record.location,
    content: record.content
  });
  homeVisitModal.visible = true;
};

const handleEditSubmit = async () => {
  try {
    await editFormRef.value.validate();
    editModal.submitting = true;
    const payload = {
      ...editForm,
      talkTime: editForm.talkTime.format('YYYY-MM-DD HH:mm:ss')
    };
    await axios.put(`/api/talk-records/${editModal.id}`, payload);
    message.success('更新成功');
    editModal.visible = false;
    fetchTalkRecords(user.value.jobNo);
  } catch (e) {
    message.error('更新失败');
  } finally {
    editModal.submitting = false;
  }
};

const handleHomeVisitSubmit = async () => {
  try {
    await homeVisitFormRef.value.validate();
    homeVisitModal.submitting = true;
    const payload = {
      ...homeVisitForm,
      operatorJobNo: getCurrentUser()?.jobNo,
      targetJobNo: user.value.jobNo,
      visitTime: homeVisitForm.visitTime.format('YYYY-MM-DD HH:mm:ss')
    };

    if (homeVisitModal.id) {
      await axios.put(`/api/home-visits/${homeVisitModal.id}`, payload);
      message.success('更新成功');
    } else {
      await axios.post('/api/home-visits', payload);
      message.success('新增成功');
    }

    homeVisitModal.visible = false;
    fetchHomeVisits(user.value.jobNo);
  } catch (e) {
    message.error('操作失败');
  } finally {
    homeVisitModal.submitting = false;
  }
};

const deleteRecord = async (id) => {
  try {
    await axios.delete(`/api/talk-records/delete/${id}`);
    message.success('删除成功');
    fetchTalkRecords(user.value.jobNo);
  } catch (error) {
    message.error('删除失败');
  }
};

const deleteHomeVisitRecord = async (id) => {
  try {
    await axios.delete(`/api/home-visits/delete/${id}`);
    message.success('删除成功');
    fetchHomeVisits(user.value.jobNo);
  } catch (error) {
    message.error('删除失败');
  }
};

onMounted(() => {
  const jobNo = route.query.jobNo;
  if (jobNo) {
    fetchUserInfo(jobNo);
  }
});

watch(() => route.query.jobNo, (newJobNo) => {
  if (newJobNo) {
    fetchUserInfo(newJobNo);
  }
});
</script>

<style scoped>
.user-detail-container {
  padding: 24px;
  background-color: transparent;
  min-height: 100%;
}

.detail-card {
  background: rgba(0, 21, 41, 0.85);
  border: 1px solid rgba(0, 212, 255, 0.2);
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
}


.header-content {
  display: flex;
  align-items: center;
}

.page-title {
  margin-left: 16px;
  font-size: 18px;
  font-weight: bold;
  color: #fff;
}

.section-title {
  font-size: 16px;
  font-weight: bold;
  margin-bottom: 16px;
  padding-left: 8px;
  border-left: 4px solid #00d4ff;
  color: #fff;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.pre-content {
  white-space: pre-wrap;
  word-break: break-all;
  color: #ccd6f6;
}






</style>
