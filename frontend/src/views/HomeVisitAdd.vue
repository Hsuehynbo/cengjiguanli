<template>
  <div class="home-visit-add-container">
    <a-card :bordered="false" class="home-visit-add-card">
      <template #title>
        <div class="header-content">
          <a-button type="link" @click="$router.back()"><left-outlined /> 返回</a-button>
          <span class="page-title">{{ isEditMode ? '编辑家访记录' : '新增家访记录' }}</span>
        </div>
      </template>

      <a-spin :spinning="loading">
        <a-form :model="form" :rules="rules" ref="formRef" layout="vertical" class="home-visit-form">
          <a-row :gutter="24">
            <a-col :span="12">
              <a-form-item label="家访人" name="operatorName">
                <a-input v-model:value="form.operatorName" disabled />
              </a-form-item>
            </a-col>
            <a-col :span="12">
              <a-form-item label="被家访人" name="targetName" :rules="[{ required: true, message: '请选择被家访人' }]">
                <a-select
                  v-if="!hasTargetFromRoute"
                  v-model:value="form.targetJobNo"
                  show-search
                  placeholder="输入姓名搜索被家访人"
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
              <a-form-item label="家访时间" name="visitTime">
                <a-date-picker
                  v-model:value="form.visitTime"
                  show-time
                  style="width: 100%"
                  placeholder="请选择家访时间"
                />
              </a-form-item>
            </a-col>
            <a-col :span="12">
              <a-form-item label="家访类型" name="visitType">
                <a-select v-model:value="form.visitType" placeholder="请选择家访类型">
                  <a-select-option value="例行家访">例行家访</a-select-option>
                  <a-select-option value="特殊家访">特殊家访</a-select-option>
                  <a-select-option value="慰问家访">慰问家访</a-select-option>
                </a-select>
              </a-form-item>
            </a-col>
          </a-row>

          <a-form-item label="家访内容" name="content">
            <a-textarea v-model:value="form.content" :rows="6" placeholder="请输入家访详细内容" />
          </a-form-item>

          <a-form-item label="现场照片" name="photo">
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

const route = useRoute();
const router = useRouter();
const formRef = ref(null);
const loading = ref(false);
const submitting = ref(false);
const recordId = ref(null);

const isEditMode = computed(() => !!recordId.value);

const form = reactive({
  operatorName: '',
  operatorJobNo: '',
  targetName: '',
  targetJobNo: '',
  visitTime: dayjs(),
  visitType: '例行家访',
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
  visitTime: [{ required: true, message: '请选择家访时间', trigger: 'change' }],
  visitType: [{ required: true, message: '请选择家访类型', trigger: 'change' }],
  location: [{ required: true, message: '请输入家访地点', trigger: 'blur' }],
  content: [{ required: true, message: '请输入家访内容', trigger: 'blur' }],
};

const initInfo = async () => {
  loading.value = true;
  try {
    const id = route.query.id;
    if (id) {
      recordId.value = id;
      const res = await axios.get(`/api/home-visits/${id}`);
      form.operatorName = res.operatorName || '';
      form.operatorJobNo = res.operatorJobNo || '';
      form.targetName = res.targetName || '';
      form.targetJobNo = res.targetJobNo || '';
      form.visitTime = dayjs(res.visitTime);
      form.visitType = res.visitType || '例行家访';
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
        form.operatorName = loginUser.name;
        form.operatorJobNo = loginUser.jobNo;
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
    if (!form.operatorJobNo) {
      message.error('未获取到当前登录人信息，请重新登录');
      return;
    }
    if (!form.targetJobNo) {
      message.error('请选择被家访人');
      return;
    }
    submitting.value = true;

    const formData = new FormData();
    formData.append('operatorJobNo', form.operatorJobNo);
    formData.append('targetJobNo', form.targetJobNo);
    formData.append('visitTime', form.visitTime.format('YYYY-MM-DD HH:mm:ss'));
    formData.append('visitType', form.visitType);
    formData.append('location', form.location);
    formData.append('content', form.content);

    if (fileList.value.length > 0) {
      formData.append('photo', fileList.value[0].originFileObj);
    }

    if (isEditMode.value) {
      await axios.put(`/api/home-visits/${recordId.value}`, formData, {
        headers: { 'Content-Type': 'multipart/form-data' }
      });
      message.success('更新家访记录成功');
    } else {
      await axios.post('/api/home-visits', formData, {
        headers: { 'Content-Type': 'multipart/form-data' }
      });
      message.success('家访记录提交成功');
    }
    router.back();
  } catch (error) {
    if (error.response?.data) {
      message.error(error.response.data);
    } else {
      message.error('提交失败，请重试');
    }
  } finally {
    submitting.value = false;
  }
};

onMounted(initInfo);
</script>

<style scoped>
.home-visit-add-container {
  padding: 24px;
  background-color: transparent;
  min-height: 100vh;
}

.home-visit-add-card {
  max-width: 900px;
  margin: 0 auto;
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
  font-size: 20px;
  font-weight: bold;
  color: #fff;
}

.home-visit-form {
  padding: 10px 20px;
}


.form-footer {
  margin-top: 32px;
  text-align: center;
  border-top: 1px solid rgba(0, 212, 255, 0.15);
  padding-top: 24px;
}

</style>
