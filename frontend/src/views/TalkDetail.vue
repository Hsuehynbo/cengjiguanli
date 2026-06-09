<template>
  <div class="talk-detail-page">
    <a-card :bordered="false" class="talk-detail-card">
      <template #title>
        <div class="header-content">
          <a-button type="link" @click="$router.back()"><left-outlined /> 返回</a-button>
          <span class="page-title">谈话详情记录</span>
          <a-button v-if="canEdit(record)" type="primary" size="small" style="margin-left: auto" @click="goToEdit">编辑</a-button>
        </div>
      </template>

      <a-spin :spinning="loading">
        <div v-if="record" class="detail-full-content">
          <div class="info-grid">
            <div class="info-item">
              <span class="info-label">谈话人：</span>
              <span class="info-value">{{ talkerName }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">被谈话人：</span>
              <span class="info-value">{{ targetName }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">谈话时间：</span>
              <span class="info-value">{{ record.talkTime }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">谈话类型：</span>
              <a-tag color="blue">{{ record.talkType }}</a-tag>
            </div>
            <div class="info-item full-width">
              <span class="info-label">谈话地点：</span>
              <span class="info-value">{{ record.location }}</span>
            </div>
          </div>

          <a-divider />

          <div class="content-section">
            <div class="section-title">谈话内容详情</div>
            <div class="content-box">{{ record.content }}</div>
          </div>

          <a-divider v-if="record.photo" />

          <div v-if="record.photo" class="photo-section">
            <div class="section-title">现场照片记录</div>
            <div class="photo-box">
              <a-image :src="getPhotoUrl(record.photo)" :alt="record.talkType" class="detail-photo" />
            </div>
          </div>
        </div>

        <a-empty v-else-if="!loading && !record" description="未找到谈话记录详情" />
      </a-spin>
    </a-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { LeftOutlined } from '@ant-design/icons-vue';
import axios from '../utils/axios';
import { message } from 'ant-design-vue';
import { getCurrentUser } from '../utils/auth';
import { getFileUrl } from '../utils/axios';
import { isBureauLeader as isBureauLeaderFn } from '../utils/constants';

const currentUser = getCurrentUser();
const canEdit = (record) => currentUser?.jobNo === 'admin' || currentUser?.role === 'ADMIN_GLOBAL' || isBureauLeaderFn(currentUser) || record?.talkerJobNo === currentUser?.jobNo;

const route = useRoute();
const router = useRouter();
const loading = ref(false);
const record = ref(null);
const talkerName = ref('');
const targetName = ref('');

const fetchDetail = async () => {
  const id = route.params.id;
  if (!id) return;

  loading.value = true;
  try {
    const res = await axios.get(`/api/talk-records/detail/${id}`);
    record.value = res;
    talkerName.value = res.talkerName || '';
    targetName.value = res.targetName || '';
  } catch (error) {
    message.error('加载详情失败');
  } finally {
    loading.value = false;
  }
};

const goToEdit = () => {
  router.push(`/talk-add?id=${record.value.id}`);
};

const getPhotoUrl = (photo) => {
  if (!photo) return '';
  return getFileUrl(photo);
};

onMounted(() => {
  fetchDetail();
});
</script>

<style scoped>
.talk-detail-page {
  padding: 24px;
  background: transparent;
  min-height: 100%;
}

.talk-detail-card {
  max-width: 1000px;
  margin: 0 auto;
  position: relative;
  background: rgba(0, 21, 41, 0.85);
  border: 1px solid rgba(0, 212, 255, 0.2);
  border-radius: 8px;
}

.talk-detail-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 20px;
  right: 20px;
  height: 1px;
  background: linear-gradient(90deg, #00d4ff, #00ffff);
}

.header-content {
  display: flex;
  align-items: center;
}

.page-title {
  margin-left: 16px;
  font-size: 20px;
  font-weight: bold;
  color: #fff;
}

.detail-full-content {
  padding: 10px 20px;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 24px;
  margin-bottom: 8px;
}

.info-item {
  display: flex;
  align-items: center;
}

.info-item.full-width {
  grid-column: span 2;
}

.info-label {
  color: #8892b0;
  font-size: 14px;
  width: 90px;
  flex-shrink: 0;
}

.info-value {
  color: #ccd6f6;
  font-size: 16px;
  font-weight: 500;
}

.section-title {
  font-size: 17px;
  font-weight: 600;
  margin-bottom: 16px;
  padding-left: 10px;
  border-left: 4px solid #00d4ff;
  color: #fff;
}

.content-box {
  background-color: rgba(0, 21, 41, 0.6);
  padding: 24px;
  border-radius: 8px;
  border: 1px solid rgba(0, 212, 255, 0.1);
  font-size: 15px;
  line-height: 1.8;
  color: #ccd6f6;
  white-space: pre-wrap;
  min-height: 200px;
}

.photo-box {
  margin-top: 16px;
  text-align: center;
}

.detail-photo {
  max-width: 100%;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.3);
  transition: transform 0.3s;
}

.detail-photo:hover {
  transform: scale(1.02);
}

</style>
