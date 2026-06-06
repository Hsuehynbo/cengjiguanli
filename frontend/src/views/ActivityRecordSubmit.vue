<template>
  <div class="record-submit-container">
    <a-card :bordered="false" class="form-card">
      <template #title>
        <div class="header-toolbar">
          <span class="page-title">{{ existingRecord ? '修改活动记录' : '填写活动记录' }}</span>
          <a-button @click="goBack">返回</a-button>
        </div>
      </template>

      <div v-if="taskInfo" class="task-info">
        <a-descriptions :column="2" size="small" bordered>
          <a-descriptions-item label="活动名称">{{ taskInfo.title }}</a-descriptions-item>
          <a-descriptions-item label="活动类型">{{ typeMap[taskInfo.taskType] || taskInfo.taskType }}</a-descriptions-item>
          <a-descriptions-item label="截止时间" :span="2">{{ taskInfo.deadline }}</a-descriptions-item>
          <a-descriptions-item label="活动要求" :span="2">{{ taskInfo.description || '无' }}</a-descriptions-item>
        </a-descriptions>
      </div>

      <a-divider />

      <a-form :model="form" :label-col="{ span: 3 }" :wrapper-col="{ span: 18 }" @finish="handleSubmit">
        <a-form-item label="活动内容" required>
          <a-textarea v-model:value="form.content" :rows="6" placeholder="描述活动开展情况" />
        </a-form-item>

        <a-form-item label="活动照片">
          <div class="photo-upload-area">
            <div v-for="(photo, index) in photoList" :key="index" class="photo-item">
              <img :src="getFileUrl(photo)" alt="照片" />
              <div class="photo-remove" @click="removePhoto(index)">
                <DeleteOutlined />
              </div>
            </div>
            <div v-if="photoList.length < 9" class="photo-upload-btn" @click="triggerUpload">
              <PlusOutlined />
              <span>上传</span>
            </div>
            <input ref="fileInput" type="file" accept="image/*" multiple style="display: none" @change="handleFileChange" />
          </div>
          <div class="photo-hint">最多9张照片</div>
        </a-form-item>

        <a-form-item label="参与人员" required>
          <div class="participants-area">
            <div class="participants-header">
              <a-checkbox v-model:checked="selectAll" @change="handleSelectAll">
                全选本单位人员
              </a-checkbox>
              <span class="selected-count">已选 {{ selectedJobNos.length }} / {{ deptUsers.length }} 人</span>
            </div>
            <a-divider style="margin: 8px 0" />
            <a-checkbox-group v-model:value="selectedJobNos" style="display: flex; flex-wrap: wrap; gap: 8px">
              <a-checkbox v-for="u in deptUsers" :key="u.jobNo" :value="u.jobNo">
                {{ u.name }}（{{ u.jobNo }}）
              </a-checkbox>
            </a-checkbox-group>
          </div>
        </a-form-item>

        <a-form-item label="备注">
          <a-input v-model:value="form.remark" placeholder="可选备注信息" />
        </a-form-item>

        <a-form-item :wrapper-col="{ offset: 3, span: 18 }">
          <a-space>
            <a-button type="primary" html-type="submit" :loading="submitting">
              {{ existingRecord ? '保存修改' : '提交' }}
            </a-button>
            <a-button @click="goBack">取消</a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </a-card>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { message } from 'ant-design-vue';
import { PlusOutlined, DeleteOutlined } from '@ant-design/icons-vue';
import axios, { getFileUrl } from '../utils/axios';

const route = useRoute();
const router = useRouter();
const taskId = route.params.taskId;
const submitting = ref(false);
const taskInfo = ref(null);
const deptUsers = ref([]);
const existingRecord = ref(null);
const photoList = ref([]);
const selectedJobNos = ref([]);
const selectAll = ref(false);
const fileInput = ref(null);

const form = ref({
  content: '',
  remark: ''
});

const typeMap = { LEARNING: '学习', MEETING: '会议', DRILL: '演练', TRAINING: '培训', OTHER: '其他' };

const fetchTaskInfo = async () => {
  try {
    const res = await axios.get(`/api/activity-tasks/${taskId}`);
    taskInfo.value = res;
  } catch (e) {
    message.error('获取任务信息失败');
  }
};

const fetchDeptUsers = async () => {
  try {
    const res = await axios.get('/api/users/list');
    deptUsers.value = res;
  } catch (e) {
    message.error('获取人员列表失败');
  }
};

const fetchExistingRecord = async () => {
  try {
    const res = await axios.get(`/api/activity-tasks/my-record/${taskId}`);
    if (res.exists === false) {
      existingRecord.value = null;
      return;
    }
    existingRecord.value = res;
    form.value.content = res.content || '';
    form.value.remark = res.remark || '';

    if (res.photos) {
      photoList.value = res.photos.split(',').filter(p => p.trim());
    }

    if (res.participants && res.participants.length > 0) {
      selectedJobNos.value = res.participants.map(p => p.userJobNo);
    }
  } catch (e) {
    // no existing record
  }
};

const handleSelectAll = (e) => {
  if (e.target.checked) {
    selectedJobNos.value = deptUsers.value.map(u => u.jobNo);
  } else {
    selectedJobNos.value = [];
  }
};

const triggerUpload = () => {
  fileInput.value.click();
};

const handleFileChange = async (e) => {
  const files = Array.from(e.target.files);
  const remaining = 9 - photoList.value.length;
  const toUpload = files.slice(0, remaining);

  for (const file of toUpload) {
    const formData = new FormData();
    formData.append('file', file);
    try {
      const res = await axios.post('/api/files/upload', formData, {
        headers: { 'Content-Type': 'multipart/form-data' }
      });
      photoList.value.push(res.url || res);
    } catch (err) {
      message.error('照片上传失败');
    }
  }
  e.target.value = '';
};

const removePhoto = (index) => {
  photoList.value.splice(index, 1);
};

const handleSubmit = async () => {
  if (!form.value.content.trim()) { message.warning('请输入活动内容'); return; }
  if (selectedJobNos.value.length === 0) { message.warning('请至少选择一名参与人员'); return; }

  submitting.value = true;
  try {
    const payload = {
      taskId: parseInt(taskId),
      content: form.value.content,
      photos: photoList.value.join(','),
      remark: form.value.remark,
      participants: selectedJobNos.value
    };
    const res = await axios.post('/api/activity-tasks/submit-record', payload);
    message.success(res.message);
    router.push('/activity-tasks');
  } catch (e) {
    message.error(e.response?.data?.error || '提交失败');
  } finally {
    submitting.value = false;
  }
};

const goBack = () => router.push('/activity-tasks');

onMounted(() => {
  fetchTaskInfo();
  fetchDeptUsers();
  fetchExistingRecord();
});
</script>

<style scoped>
.record-submit-container {
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
.participants-area {
  border: 1px solid #d9d9d9;
  border-radius: 6px;
  padding: 12px;
}
.participants-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.selected-count {
  font-size: 13px;
  color: #999;
}
.photo-upload-area {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
.photo-item {
  width: 100px;
  height: 100px;
  border-radius: 6px;
  overflow: hidden;
  position: relative;
  border: 1px solid #d9d9d9;
}
.photo-item img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.photo-remove {
  position: absolute;
  top: 2px;
  right: 2px;
  width: 20px;
  height: 20px;
  background: rgba(0, 0, 0, 0.5);
  color: #fff;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  font-size: 12px;
}
.photo-upload-btn {
  width: 100px;
  height: 100px;
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  color: #999;
  transition: border-color 0.3s;
}
.photo-upload-btn:hover {
  border-color: #1890ff;
  color: #1890ff;
}
.photo-hint {
  font-size: 12px;
  color: #999;
  margin-top: 4px;
}
</style>
