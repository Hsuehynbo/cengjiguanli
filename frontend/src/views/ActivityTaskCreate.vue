<template>
  <div class="activity-create-container">
    <a-card :bordered="false" class="form-card">
      <template #title>
        <div class="header-toolbar">
          <span class="page-title">创建活动任务</span>
          <a-button @click="goBack">返回列表</a-button>
        </div>
      </template>

      <a-form :model="form" :label-col="{ span: 4 }" :wrapper-col="{ span: 16 }" @finish="handleSubmit">
        <a-form-item label="活动名称" required>
          <a-input v-model:value="form.title" placeholder="如：安全生产月学习" />
        </a-form-item>

        <a-form-item label="活动类型" required>
          <a-select v-model:value="form.taskType" placeholder="选择类型">
            <a-select-option value="LEARNING">学习</a-select-option>
            <a-select-option value="MEETING">会议</a-select-option>
            <a-select-option value="DRILL">演练</a-select-option>
            <a-select-option value="TRAINING">培训</a-select-option>
            <a-select-option value="OTHER">其他</a-select-option>
          </a-select>
        </a-form-item>

        <a-form-item label="活动要求">
          <a-textarea v-model:value="form.description" :rows="4" placeholder="填写活动要求和说明" />
        </a-form-item>

        <a-form-item label="截止时间" required>
          <a-date-picker v-model:value="form.deadline" show-time format="YYYY-MM-DD HH:mm:ss" style="width: 100%" placeholder="选择截止时间" />
        </a-form-item>

        <a-form-item label="下发范围" required>
          <template v-if="isGlobalAdmin">
            <a-select
              v-model:value="selectedDeptIds"
              mode="multiple"
              placeholder="选择下发单位（可多选）"
              :options="deptOptions"
              :max-tag-count="3"
              show-search
              :filter-option="filterDept"
              style="width: 100%"
            >
              <template #dropdownRender="{ menuNode }">
                <div style="padding: 4px 8px; cursor: pointer; border-bottom: 1px solid #f0f0f0" @mousedown.prevent>
                  <a @mousedown.prevent="handleSelectAll" style="user-select: none">全选 / 取消全选</a>
                </div>
                <component :is="menuNode" />
              </template>
            </a-select>
          </template>
          <template v-else>
            <a-input :value="currentUser.department?.deptName || '本单位'" disabled />
            <div style="color: #999; font-size: 12px; margin-top: 4px">活动将下发至您所在的单位</div>
          </template>
        </a-form-item>

        <a-form-item :wrapper-col="{ offset: 4, span: 16 }">
          <a-button type="primary" html-type="submit" :loading="submitting">创建并下发</a-button>
        </a-form-item>
      </a-form>
    </a-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { message } from 'ant-design-vue';
import dayjs from 'dayjs';
import axios from '../utils/axios';
import { getCurrentUser } from '../utils/auth';
import { isGlobalAdmin as isGlobalAdminFn } from '../utils/constants';

const router = useRouter();
const submitting = ref(false);
const departments = ref([]);
const selectedDeptIds = ref([]);
const currentUser = getCurrentUser();
const isGlobalAdmin = computed(() => isGlobalAdminFn(currentUser));

const form = ref({
  title: '',
  taskType: undefined,
  description: '',
  deadline: null
});

const deptOptions = computed(() =>
  departments.value.map(d => ({ label: d.name, value: d.id }))
);

const filterDept = (input, option) => {
  return option.label.toLowerCase().includes(input.toLowerCase());
};

const handleSelectAll = () => {
  if (selectedDeptIds.value.length === departments.value.length) {
    selectedDeptIds.value = [];
  } else {
    selectedDeptIds.value = departments.value.map(d => d.id);
  }
};

const fetchDepartments = async () => {
  if (!isGlobalAdmin.value) return;
  try {
    const res = await axios.get('/api/organization/departments');
    departments.value = res;
    selectedDeptIds.value = res.map(d => d.id);
  } catch (e) {
    message.error('获取部门列表失败');
  }
};

const handleSubmit = async () => {
  if (!form.value.title) { message.warning('请输入活动名称'); return; }
  if (!form.value.taskType) { message.warning('请选择活动类型'); return; }
  if (!form.value.deadline) { message.warning('请选择截止时间'); return; }
  if (isGlobalAdmin.value && selectedDeptIds.value.length === 0) { message.warning('请选择下发单位'); return; }

  submitting.value = true;
  try {
    const payload = {
      title: form.value.title,
      description: form.value.description,
      taskType: form.value.taskType,
      deadline: form.value.deadline.format('YYYY-MM-DD HH:mm:ss')
    };
    if (isGlobalAdmin.value) {
      const isAll = selectedDeptIds.value.length === departments.value.length;
      payload.allDepts = isAll;
      payload.deptIds = isAll ? [] : selectedDeptIds.value;
    }
    const res = await axios.post('/api/activity-tasks/create', payload);
    message.success(`任务创建成功，已下发${res.targetCount}个单位`);
    router.push('/activity-tasks');
  } catch (e) {
    message.error(e.response?.data?.error || '创建失败');
  } finally {
    submitting.value = false;
  }
};

const goBack = () => router.push('/activity-tasks');

onMounted(fetchDepartments);
</script>

<style scoped>
.activity-create-container {
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
</style>
