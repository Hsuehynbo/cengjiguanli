<template>
  <div class="talk-add-page">
    <a-card :bordered="false" class="talk-add-card">
      <template #title>
        <div class="header-content">
          <a-button type="link" @click="$router.back()"><left-outlined /> 返回</a-button>
          <span class="page-title">{{ isEditMode ? '编辑谈话记录' : '新增谈话记录' }}</span>
        </div>
      </template>

      <a-spin :spinning="loading">
        <a-form :model="form" :rules="rules" ref="formRef" layout="vertical" class="talk-form">
          <a-row :gutter="24">
            <a-col :span="12">
              <a-form-item label="谈话人" name="talkerName">
                <a-input v-model:value="form.talkerName" disabled />
              </a-form-item>
            </a-col>
            <a-col :span="12">
              <a-form-item label="被谈话人" name="targetName" :rules="[{ required: true, message: '请选择被谈话人' }]">
                <a-select
                  v-if="!hasTargetFromRoute"
                  v-model:value="form.targetJobNo"
                  show-search
                  placeholder="输入姓名搜索被谈话人"
                  :filter-option="false"
                  :loading="searchLoading"
                  @search="handleSearchUsers"
                  @change="handleTargetChange"
                  :not-found-content="searchLoading ? '搜索中...' : '未找到匹配人员'"
                >
                  <a-select-option v-for="u in searchResults" :key="u.jobNo" :value="u.jobNo">
                    {{ u.name }} ({{ u.jobNo }}) {{ u.deptName ? '- ' + u.deptName : '' }}
                  </a-select-option>
                </a-select>
                <a-input v-else v-model:value="form.targetName" disabled />
              </a-form-item>
            </a-col>
          </a-row>

          <a-row :gutter="24">
            <a-col :span="12">
              <a-form-item label="谈话时间" name="talkTime">
                <a-date-picker
                  v-model:value="form.talkTime"
                  show-time
                  style="width: 100%"
                  placeholder="请选择谈话时间"
                />
              </a-form-item>
            </a-col>
            <a-col :span="12">
              <a-form-item label="谈话类型" name="talkType">
                <a-select v-model:value="form.talkType" placeholder="请选择谈话类型">
                  <a-select-option v-for="opt in TALK_TYPE_OPTIONS" :key="opt" :value="opt">{{ opt }}</a-select-option>
                </a-select>
              </a-form-item>
            </a-col>
          </a-row>

          <a-form-item label="谈话地点" name="location">
            <a-input v-model:value="form.location" placeholder="请输入谈话地点" />
          </a-form-item>

          <a-form-item label="谈话内容" name="content">
            <a-textarea v-model:value="form.content" :rows="6" placeholder="请输入谈话详细内容" />
          </a-form-item>

          <a-form-item label="相关照片" name="photo">
            <a-upload
              v-model:file-list="fileList"
              name="photo"
              list-type="picture-card"
              :max-count="1"
              :before-upload="beforeUpload"
              @preview="handlePreview"
            >
              <div v-if="fileList.length < 1">
                <plus-outlined />
                <div style="margin-top: 8px">上传照片</div>
              </div>
            </a-upload>
            <a-modal :open="previewVisible" :title="previewTitle" :footer="null" @cancel="previewVisible = false" width="90%">
              <div style="display: flex; justify-content: center; align-items: center; min-height: 60vh;">
                <img alt="预览" style="max-width: 100%; max-height: 70vh; object-fit: contain;" :src="previewImage" />
              </div>
            </a-modal>
          </a-form-item>

          <div class="form-footer">
            <a-space>
              <a-button @click="$router.back()">返回</a-button>
              <a-button type="primary" :loading="submitting" @click="handleSubmit">{{ isEditMode ? '保存修改' : '提交记录' }}</a-button>
            </a-space>
          </div>
        </a-form>
      </a-spin>
    </a-card>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive, computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { LeftOutlined, PlusOutlined } from '@ant-design/icons-vue';
import axios, { getFileUrl } from '../utils/axios';
import { message } from 'ant-design-vue';
import dayjs from 'dayjs';
import { getCurrentUser } from '../utils/auth';
import { TALK_TYPE_OPTIONS } from '../utils/constants';

const route = useRoute();
const router = useRouter();
const formRef = ref(null);
const loading = ref(false);
const submitting = ref(false);
const recordId = ref(null);

const isEditMode = computed(() => !!recordId.value);

const form = reactive({
  talkerName: '',
  talkerJobNo: '',
  targetName: '',
  targetJobNo: '',
  talkTime: dayjs(),
  talkType: undefined,
  location: '',
  content: '',
});

const fileList = ref([]);
const previewVisible = ref(false);
const previewImage = ref('');
const previewTitle = ref('');

// 人员搜索相关
const hasTargetFromRoute = ref(false);
const searchResults = ref([]);
const searchLoading = ref(false);
let searchTimer = null;

const handleSearchUsers = (keyword) => {
  if (searchTimer) clearTimeout(searchTimer);
  if (!keyword || keyword.trim().length < 1) {
    searchResults.value = [];
    return;
  }
  searchLoading.value = true;
  searchTimer = setTimeout(async () => {
    try {
      const res = await axios.get(`/api/organization/search?keyword=${encodeURIComponent(keyword.trim())}`);
      searchResults.value = (res || []).slice(0, 20);
    } catch (e) {
      searchResults.value = [];
    } finally {
      searchLoading.value = false;
    }
  }, 300);
};

const handleTargetChange = (jobNo) => {
  const selected = searchResults.value.find(u => u.jobNo === jobNo);
  if (selected) {
    form.targetName = selected.name;
    form.targetJobNo = selected.jobNo;
  }
};

const rules = {
  talkTime: [{ required: true, message: '请选择谈话时间', trigger: 'change' }],
  talkType: [{ required: true, message: '请选择谈话类型', trigger: 'change' }],
  location: [{ required: true, message: '请输入谈话地点', trigger: 'blur' }],
  content: [{ required: true, message: '请输入谈话内容', trigger: 'blur' }],
};

const initInfo = async () => {
  loading.value = true;
  try {
    const id = route.query.id;
    if (id) {
      recordId.value = id;
      const res = await axios.get(`/api/talk-records/detail/${id}`);
      form.talkerName = res.talkerName || '';
      form.talkerJobNo = res.talkerJobNo || '';
      form.targetName = res.targetName || '';
      form.targetJobNo = res.targetJobNo || '';
      form.talkTime = dayjs(res.talkTime);
      form.talkType = res.talkType;
      form.location = res.location || '';
      form.content = res.content || '';
      if (res.photo) {
        fileList.value = [{
          uid: '-1',
          name: 'photo',
          status: 'done',
          url: getFileUrl(res.photo)
        }];
      }
    } else {
      const loginUser = getCurrentUser();
      if (loginUser) {
        form.talkerName = loginUser.name;
        form.talkerJobNo = loginUser.jobNo;
      }

      const targetJobNo = route.query.targetJobNo;
      if (targetJobNo) {
        hasTargetFromRoute.value = true;
        const res = await axios.get(`/api/organization/user/${targetJobNo}`);
        form.targetName = res.name;
        form.targetJobNo = res.jobNo;
      }
    }
  } catch (error) {
    message.error('初始化信息失败');
  } finally {
    loading.value = false;
  }
};

const beforeUpload = (file) => {
  const isJpgOrPng = file.type === 'image/jpeg' || file.type === 'image/png';
  if (!isJpgOrPng) {
    message.error('只能上传 JPG/PNG 格式的照片!');
    return false;
  }
  const isLt2M = file.size / 1024 / 1024 < 2;
  if (!isLt2M) {
    message.error('照片大小不能超过 2MB!');
    return false;
  }
  return false;
};

const handlePreview = async (file) => {
  if (!file.url && !file.preview) {
    file.preview = await getBase64(file.originFileObj);
  }
  previewImage.value = file.url || file.preview;
  previewVisible.value = true;
  previewTitle.value = file.name || file.url.substring(file.url.lastIndexOf('/') + 1);
};

const getBase64 = (file) => {
  return new Promise((resolve, reject) => {
    const reader = new FileReader();
    reader.readAsDataURL(file);
    reader.onload = () => resolve(reader.result);
    reader.onerror = (error) => reject(error);
  });
};

const handleSubmit = async () => {
  try {
    await formRef.value.validate();

    if (!form.talkerJobNo) {
      message.error('未获取到当前登录人信息，请重新登录');
      return;
    }
    if (!form.targetJobNo) {
      message.error('请选择被谈话人');
      return;
    }

    submitting.value = true;

    const formData = new FormData();
    formData.append('talkerJobNo', form.talkerJobNo);
    formData.append('targetJobNo', form.targetJobNo);
    formData.append('talkTime', form.talkTime.format('YYYY-MM-DD HH:mm:ss'));
    if (form.talkType) {
      formData.append('talkType', form.talkType);
    }
    formData.append('location', form.location);
    formData.append('content', form.content);

    if (fileList.value.length > 0 && fileList.value[0].originFileObj) {
      formData.append('photo', fileList.value[0].originFileObj);
    }

    if (isEditMode.value) {
      await axios.put(`/api/talk-records/${recordId.value}`, formData, {
        headers: { 'Content-Type': 'multipart/form-data' }
      });
      message.success('更新谈话记录成功');
    } else {
      await axios.post('/api/talk-records/add', formData, {
        headers: { 'Content-Type': 'multipart/form-data' }
      });
      message.success('新增谈话记录成功');
    }
    router.back();
  } catch (error) {
    if (error.errorFields) return;
    const errorMsg = error.response?.data?.message || error.response?.data || error.message || '提交失败，请重试';
    message.error(errorMsg);
  } finally {
    submitting.value = false;
  }
};

onMounted(() => {
  initInfo();
});
</script>

<style scoped>
.talk-add-page {
  padding: 24px;
  background: transparent;
  min-height: 100%;
}

.talk-add-card {
  max-width: 800px;
  margin: 0 auto;
  position: relative;
  background: rgba(0, 21, 41, 0.85);
  border: 1px solid rgba(0, 212, 255, 0.2);
  border-radius: 8px;
}

.talk-add-card::before {
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
  font-size: 18px;
  font-weight: bold;
  color: #fff;
}

.talk-form {
  padding: 20px 0;
}
</style>
