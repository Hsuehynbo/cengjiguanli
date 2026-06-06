<template>
  <div class="record-detail-container">
    <a-card :bordered="false" class="form-card">
      <template #title>
        <div class="header-toolbar">
          <span class="page-title">活动记录详情</span>
          <a-button @click="goBack">返回任务详情</a-button>
        </div>
      </template>

      <a-spin :spinning="loading">
        <div v-if="record">
          <a-descriptions :column="2" size="small" bordered class="task-info">
            <a-descriptions-item label="所属任务" :span="2">{{ record.taskTitle }}</a-descriptions-item>
            <a-descriptions-item label="提交单位">{{ record.deptName }}</a-descriptions-item>
            <a-descriptions-item label="提交人">{{ record.submittedByName }}（{{ record.submittedBy }}）</a-descriptions-item>
            <a-descriptions-item label="提交时间" :span="2">{{ formatTime(record.submitTime) }}</a-descriptions-item>
            <a-descriptions-item label="活动内容" :span="2">{{ record.content }}</a-descriptions-item>
            <a-descriptions-item label="备注" :span="2">{{ record.remark || '无' }}</a-descriptions-item>
          </a-descriptions>

          <!-- 活动照片 -->
          <div v-if="record.photos" style="margin-top: 24px">
            <div class="section-title">活动照片</div>
            <div style="display: flex; flex-wrap: wrap; gap: 12px">
              <img
                v-for="(photo, i) in photoList"
                :key="i"
                :src="photo"
                style="width: 150px; height: 150px; object-fit: cover; border-radius: 6px; cursor: pointer; border: 1px solid rgba(0,212,255,0.2)"
                @click="previewPhoto = photo"
              />
            </div>
          </div>

          <!-- 参与人员 -->
          <div v-if="record.participants && record.participants.length > 0" style="margin-top: 24px">
            <div class="section-title">参与人员（{{ record.participants.length }}人）</div>
            <div style="display: flex; flex-wrap: wrap; gap: 8px">
              <a-tag v-for="p in record.participants" :key="p.userJobNo" color="blue">
                {{ p.userName }}（{{ p.userJobNo }}）
              </a-tag>
            </div>
          </div>
        </div>
      </a-spin>
    </a-card>

    <!-- 照片预览 -->
    <a-modal :open="!!previewPhoto" :footer="null" width="80%" centered @cancel="previewPhoto = null">
      <img :src="previewPhoto" style="width: 100%" />
    </a-modal>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { message } from 'ant-design-vue';
import dayjs from 'dayjs';
import axios, { getFileUrl } from '../utils/axios';

const route = useRoute();
const router = useRouter();
const recordId = route.params.id;
const loading = ref(false);
const record = ref(null);
const previewPhoto = ref(null);

const photoList = computed(() => {
  if (!record.value?.photos) return [];
  return record.value.photos.split(',').filter(p => p.trim()).map(p => getFileUrl(p));
});

const formatTime = (t) => t ? dayjs(t).format('YYYY-MM-DD HH:mm') : '-';

const fetchRecord = async () => {
  loading.value = true;
  try {
    const res = await axios.get(`/api/activity-tasks/record-detail/${recordId}`);
    record.value = res;
  } catch (e) {
    message.error('获取记录详情失败');
  } finally {
    loading.value = false;
  }
};

const goBack = () => {
  if (record.value?.taskId) {
    router.push(`/activity-task-detail/${record.value.taskId}`);
  } else {
    router.push('/activity-tasks');
  }
};

onMounted(fetchRecord);
</script>

<style scoped>
.record-detail-container {
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
.task-info {
  margin-bottom: 16px;
}
.section-title {
  font-size: 15px;
  font-weight: 600;
  color: #ccd6f6;
  margin-bottom: 12px;
  border-left: 3px solid #00d4ff;
  padding-left: 8px;
}
</style>
