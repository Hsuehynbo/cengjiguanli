<template>
  <div class="talk-detail-page">
    <a-card :bordered="false" class="talk-detail-card">
      <template #title>
        <div class="header-content">
          <a-button type="link" @click="$router.back()"><left-outlined /> 返回</a-button>
          <span class="page-title">{{ editMode ? '编辑谈话记录' : '谈话详情记录' }}</span>
          <a-button v-if="!editMode && canEdit(record)" type="primary" size="small" style="margin-left: auto" @click="enterEdit">编辑</a-button>
        </div>
      </template>

      <a-spin :spinning="loading">
        <!-- 查看模式 -->
        <div v-if="record && !editMode" class="detail-full-content">
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

        <!-- 编辑模式 -->
        <div v-if="record && editMode" class="detail-full-content">
          <a-form :model="editForm" layout="vertical">
            <a-row :gutter="24">
              <a-col :span="12">
                <a-form-item label="谈话类型">
                  <a-select v-model:value="editForm.talkType">
                    <a-select-option v-for="opt in TALK_TYPE_OPTIONS" :key="opt" :value="opt">{{ opt }}</a-select-option>
                  </a-select>
                </a-form-item>
              </a-col>
              <a-col :span="12">
                <a-form-item label="谈话地点">
                  <a-input v-model:value="editForm.location" />
                </a-form-item>
              </a-col>
            </a-row>
            <a-form-item label="谈话内容">
              <a-textarea v-model:value="editForm.content" :rows="6" />
            </a-form-item>
          </a-form>
          <div style="text-align: right; margin-top: 16px">
            <a-space>
              <a-button @click="editMode = false">取消</a-button>
              <a-button type="primary" :loading="saving" @click="handleSave">保存</a-button>
            </a-space>
          </div>
        </div>

        <a-empty v-else-if="!loading && !record" description="未找到谈话记录详情" />
      </a-spin>
    </a-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { LeftOutlined } from '@ant-design/icons-vue';
import axios from '../utils/axios';
import { message } from 'ant-design-vue';
import { getCurrentUser } from '../utils/auth';
import { TALK_TYPE_OPTIONS, isBureauLeader as isBureauLeaderFn } from '../utils/constants';

const currentUser = getCurrentUser();
const canEdit = (record) => currentUser?.jobNo === 'admin' || currentUser?.role === 'ADMIN_GLOBAL' || isBureauLeaderFn(currentUser) || record?.talkerJobNo === currentUser?.jobNo;

const route = useRoute();
const router = useRouter();
const loading = ref(false);
const saving = ref(false);
const record = ref(null);
const talkerName = ref('');
const targetName = ref('');
const editMode = ref(false);
const editForm = reactive({
  talkType: '',
  location: '',
  content: ''
});

const fetchDetail = async () => {
  const id = route.params.id;
  if (!id) return;

  loading.value = true;
  try {
    const res = await axios.get(`/api/talk-records/detail/${id}`);
    record.value = res;
    talkerName.value = res.talkerName || '';
    targetName.value = res.targetName || '';

    if (route.query.edit === '1') {
      enterEdit();
    }
  } catch (error) {
    message.error('加载详情失败');
  } finally {
    loading.value = false;
  }
};

const enterEdit = () => {
  editForm.talkType = record.value.talkType;
  editForm.location = record.value.location;
  editForm.content = record.value.content;
  editMode.value = true;
};

const handleSave = async () => {
  if (!editForm.content) {
    message.warning('谈话内容不能为空');
    return;
  }
  saving.value = true;
  try {
    await axios.put(`/api/talk-records/${record.value.id}`, {
      talkerJobNo: record.value.talkerJobNo,
      targetJobNo: record.value.targetJobNo,
      talkTime: record.value.talkTime,
      talkType: editForm.talkType,
      location: editForm.location,
      content: editForm.content
    });
    message.success('保存成功');
    editMode.value = false;
    fetchDetail();
  } catch {
    message.error('保存失败');
  } finally {
    saving.value = false;
  }
};

const getPhotoUrl = (photo) => {
  if (!photo) return '';
  return photo;
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
