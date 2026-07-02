<template>
  <div class="profile-container">
    <a-row :gutter="16">
      <!-- 左侧个人卡片 -->
      <a-col :span="8">
        <a-card :bordered="false" class="info-card shadow-card">
          <div class="user-avatar-section">
            <a-avatar :size="100" :src="user?.avatar" class="avatar-border">
              <template #icon><UserOutlined /></template>
            </a-avatar>
            <h2 class="user-name">{{ user?.name }}</h2>
            <p class="user-job">{{ user?.position }}</p>
          </div>
          <a-divider />
          <a-descriptions column="1" size="small" class="desc-list">
            <a-descriptions-item label="工号">
              <span class="desc-val">{{ user?.jobNo }}</span>
            </a-descriptions-item>
            <a-descriptions-item label="所属单位">
              <span class="desc-val">{{ user?.department?.deptName || '未分配' }}</span>
            </a-descriptions-item>
            <a-descriptions-item label="重点关注">
              <a-badge :status="user?.isKeyPersonnel ? 'error' : 'success'" :text="user?.isKeyPersonnel ? '是' : '否'" />
            </a-descriptions-item>
            <a-descriptions-item label="入职时间">
              <span class="desc-val">{{ formatDate(user?.createTime) }}</span>
            </a-descriptions-item>
          </a-descriptions>
          <a-button type="primary" block style="margin-top: 16px" @click="showPwdModal = true">修改密码</a-button>
        </a-card>
      </a-col>

      <!-- 右侧工作面板 -->
      <a-col :span="16">
        <a-card :bordered="false" title="工作数据概览" class="stat-card shadow-card">
          <a-row :gutter="16">
            <a-col :span="8">
              <div class="stat-item">
                <a-statistic title="累计接受谈话" :value="stats.talkCount" value-style="color: #1890ff" />
                <message-outlined class="stat-icon" style="color: var(--accent-light)" />
              </div>
            </a-col>
            <a-col :span="8">
              <div class="stat-item">
                <a-statistic title="累计开展家访" :value="stats.visitCount" value-style="color: #52c41a" />
                <home-outlined class="stat-icon" style="color: rgba(82, 196, 26, 0.15)" />
              </div>
            </a-col>
            <a-col :span="8">
              <div class="stat-item">
                <a-statistic title="收到违规记录" :value="stats.violationCount" value-style="color: #cf1322" />
                <warning-outlined class="stat-icon" style="color: rgba(207, 19, 34, 0.15)" />
              </div>
            </a-col>
          </a-row>
        </a-card>

        <a-card :bordered="false" class="records-card shadow-card" style="margin-top: 16px">
          <a-tabs default-active-key="1" animated>
            <a-tab-pane key="1" tab="最近谈话记录">
              <a-list :data-source="recentTalks" :loading="loading" size="small">
                <template #renderItem="{ item }">
                  <a-list-item>
                    <a-list-item-meta>
                      <template #title>
                        <div class="list-title">
                          <span class="time">{{ item.talkTime }}</span>
                          <a-tag :color="getTalkTypeColor(item.talkType)">{{ item.talkType }}</a-tag>
                          <span class="location">@ {{ item.location }}</span>
                        </div>
                      </template>
                      <template #description>
                        <div class="list-desc">{{ item.content }}</div>
                      </template>
                    </a-list-item-meta>
                  </a-list-item>
                </template>
                <template v-if="recentTalks.length === 0" #emptyText>
                  <a-empty description="暂无谈话记录" />
                </template>
              </a-list>
            </a-tab-pane>
            <a-tab-pane key="2" tab="最近家访记录">
              <a-list :data-source="recentVisits" :loading="loading" size="small">
                <template #renderItem="{ item }">
                  <a-list-item>
                    <a-list-item-meta>
                      <template #title>
                        <div class="list-title">
                          <span class="time">{{ formatDate(item.visitTime) }}</span>
                          <a-tag color="cyan">{{ item.visitType }}</a-tag>
                          <span class="location">@ {{ item.location }}</span>
                        </div>
                      </template>
                      <template #description>
                        <div class="list-desc">{{ item.content }}</div>
                      </template>
                    </a-list-item-meta>
                  </a-list-item>
                </template>
                <template v-if="recentVisits.length === 0" #emptyText>
                  <a-empty description="暂无家访记录" />
                </template>
              </a-list>
            </a-tab-pane>
          </a-tabs>
        </a-card>
      </a-col>
    </a-row>

    <a-modal v-model:open="showPwdModal" title="修改密码" @ok="handleChangePassword" :confirm-loading="pwdLoading"
      ok-text="确认修改" cancel-text="取消">
      <a-form :model="pwdForm" layout="vertical">
        <a-form-item label="旧密码" required>
          <a-input-password v-model:value="pwdForm.oldPassword" placeholder="请输入旧密码" />
        </a-form-item>
        <a-form-item label="新密码" required>
          <a-input-password v-model:value="pwdForm.newPassword" placeholder="请输入新密码（不少于6位）" />
        </a-form-item>
        <a-form-item label="确认新密码" required>
          <a-input-password v-model:value="pwdForm.confirmPassword" placeholder="请再次输入新密码" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive } from 'vue';
import {
  UserOutlined,
  MessageOutlined,
  HomeOutlined,
  WarningOutlined
} from '@ant-design/icons-vue';
import axios from '../utils/axios';
import dayjs from 'dayjs';
import { getCurrentUser } from '../utils/auth';
import { message } from 'ant-design-vue';

const user = ref(null);
const loading = ref(false);
const stats = reactive({
  talkCount: 0,
  visitCount: 0,
  violationCount: 0
});
const recentTalks = ref([]);
const recentVisits = ref([]);
const showPwdModal = ref(false);
const pwdLoading = ref(false);
const pwdForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
});

const handleChangePassword = async () => {
  if (!pwdForm.oldPassword || !pwdForm.newPassword || !pwdForm.confirmPassword) {
    message.error('请填写所有密码字段');
    return;
  }
  if (pwdForm.newPassword.length < 6) {
    message.error('新密码长度不能少于6位');
    return;
  }
  if (pwdForm.newPassword !== pwdForm.confirmPassword) {
    message.error('两次输入的新密码不一致');
    return;
  }
  pwdLoading.value = true;
  try {
    const res = await axios.post('/api/auth/change-password', {
      oldPassword: pwdForm.oldPassword,
      newPassword: pwdForm.newPassword
    });
    message.success(res.message || '密码修改成功');
    showPwdModal.value = false;
    pwdForm.oldPassword = '';
    pwdForm.newPassword = '';
    pwdForm.confirmPassword = '';
  } catch (error) {
    // interceptor handles error display
  } finally {
    pwdLoading.value = false;
  }
};

const fetchData = async () => {
  loading.value = true;
  try {
    const localUser = getCurrentUser();
    const jobNo = localUser.jobNo;
    if (!jobNo) return;

    const res = await axios.get(`/api/organization/user/${jobNo}`);
    user.value = res;

    // 并行获取各类数据
    const [talks, visits, violations] = await Promise.all([
      axios.get(`/api/talk-records/list?targetJobNo=${jobNo}`),
      axios.get(`/api/home-visits/list?targetJobNo=${jobNo}`),
      axios.get(`/api/users/${jobNo}/violation-records`)
    ]);

    recentTalks.value = talks.slice(0, 5);
    stats.talkCount = talks.length;

    recentVisits.value = visits.slice(0, 5);
    stats.visitCount = visits.length;

    stats.violationCount = violations.length;

  } catch (error) {
    // ignore
  } finally {
    loading.value = false;
  }
};

const getTalkTypeColor = (type) => {
  const map = {
    '日常沟通': 'blue',
    '工作指导': 'green',
    '诫勉谈话': 'red',
    '提醒谈话': 'orange',
    '廉政谈话': 'purple'
  };
  return map[type] || 'default';
};

const formatDate = (date) => {
  if (!date) return '-';
  return dayjs(date).format('YYYY-MM-DD');
};

onMounted(fetchData);
</script>

<style scoped>
.profile-container {
  padding: 24px;
  background-color: transparent;
  min-height: calc(100vh - 64px);
}

.shadow-card {
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-lg);
}

.info-card {
  text-align: center;
  padding: 20px 0;
}

.avatar-border {
  border: 4px solid var(--accent-light);
}

.user-name {
  margin-top: 16px;
  font-size: 22px;
  font-weight: 600;
  color: var(--text-title);
}

.user-job {
  color: var(--text-secondary);
  font-size: 14px;
  margin-bottom: 12px;
}

.role-tag {
  padding: 0 12px;
}

.desc-list {
  text-align: left;
  padding: 0 20px;
}

/* Description labels and values */

.desc-val {
  color: var(--text-primary);
  font-weight: 500;
}


.stat-item {
  position: relative;
  background: var(--bg-card);
  padding: 20px;
  border-radius: var(--radius-md);
  overflow: hidden;
  border: 1px solid var(--border-color);
}


.stat-icon {
  position: absolute;
  right: -10px;
  bottom: -10px;
  font-size: 64px;
  opacity: 0.5;
}

.list-title {
  display: flex;
  align-items: center;
  gap: 12px;
}

.list-title .time {
  font-weight: 600;
  color: var(--text-primary);
}

.list-title .location {
  color: var(--text-muted);
  font-size: 12px;
}

.list-desc {
  margin-top: 8px;
  color: var(--text-secondary);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

</style>
