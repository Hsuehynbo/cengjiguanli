<template>
  <div class="admin-management-container">
    <a-row :gutter="16" class="full-height">
      <!-- 左侧组织架构树 -->
      <a-col :span="8" class="full-height">
        <a-card :bordered="false" class="tree-card">
          <!-- 顶部操作栏 -->
          <div class="header-toolbar">
            <div class="toolbar-row">
              <span class="page-title">组织架构</span>
              <a-space>
                <a-button @click="showImportModal">
                  <template #icon><UploadOutlined /></template>
                  批量导入
                </a-button>
                <a-button type="primary" @click="showCreateUser">
                  <template #icon><PlusOutlined /></template>
                  新增人员
                </a-button>
              </a-space>
            </div>
            <div class="toolbar-row">
              <a-input-search
                v-model:value="searchKey"
                placeholder="搜索部门/姓名/工号"
                style="flex: 1"
                allow-clear
                @search="onSearch"
                @change="onSearchChange"
              />
              <a-button style="margin-left: 8px" @click="expandAll">全部展开</a-button>
              <a-button style="margin-left: 8px" @click="collapseAll">全部折叠</a-button>
            </div>
          </div>

          <div class="tree-wrapper">
            <a-tree
              v-if="treeData.length > 0"
              block-node
              :tree-data="treeData"
              :expanded-keys="expandedKeys"
              :auto-expand-parent="autoExpandParent"
              @expand="onExpand"
              @select="onSelect"
              class="custom-tree"
            >
              <template #title="{ dataRef }">
                <div
                  class="node-title-wrapper"
                  :class="{
                    'user-node-hover': dataRef.type === 'user',
                    'dept-node-hover': dataRef.type === 'department'
                  }"
                >
                  <!-- 根节点（公司） -->
                  <template v-if="dataRef.type === 'company'">
                    <span class="node-icon">🏢</span>
                    <span class="company-title" v-html="highlightText(dataRef.name)"></span>
                    <span class="node-count">（共{{ dataRef.deptCount }}个部门）</span>
                  </template>

                  <!-- 部门节点 -->
                  <template v-else-if="dataRef.type === 'department'">
                    <span class="node-icon">🏢</span>
                    <span class="dept-title" v-html="highlightText(dataRef.name)"></span>
                    <span class="node-count">（{{ dataRef.userCount }}人）</span>
                  </template>

                  <!-- 人员节点 -->
                  <template v-else-if="dataRef.type === 'user'">
                    <div class="user-node-content" :class="{ 'key-personnel': dataRef.isKeyPersonnel }">
                      <span class="node-icon">👤</span>
                      <span class="user-name">
                        <span v-html="highlightText(dataRef.name)"></span>
                        <span class="user-position-label"> · {{ dataRef.position }}</span>
                      </span>
                      <span class="user-job-no" v-html="highlightText(`(${dataRef.jobNo})`)"></span>
                    </div>
                  </template>
                </div>
              </template>
            </a-tree>
            <div v-else-if="!loading" class="empty-container">
              <a-empty description="暂无组织架构数据" />
            </div>
          </div>
        </a-card>
      </a-col>

        <!-- 右侧管理面板 -->
      <a-col :span="16" class="full-height">
        <a-card :bordered="false" class="detail-card">
          <template #title>
            <div class="header-content">
              <span v-if="selectedUser">{{ selectedUser.name }} - 管理面板</span>
              <span v-else>请选择人员进行管理</span>
            </div>
          </template>

          <div v-if="selectedUser" class="admin-content">
            <div style="margin-bottom: 16px; text-align: right">
              <a-popconfirm title="确认删除该用户？删除后不可恢复。" @confirm="handleDeleteUser" ok-text="确认" cancel-text="取消">
                <a-button type="primary" danger>删除该用户</a-button>
              </a-popconfirm>
            </div>
            <a-tabs v-model:activeKey="activeTab">
              <!-- 基本信息管理 -->
              <a-tab-pane key="basic" tab="基本信息">
                <a-form :model="editForm" layout="vertical" class="mgmt-form">
                  <a-row :gutter="24">
                    <a-col :span="12">
                      <a-form-item label="姓名">
                        <a-input v-model:value="editForm.name" />
                      </a-form-item>
                    </a-col>
                    <a-col :span="12">
                      <a-form-item label="职务">
                        <a-input v-model:value="editForm.position" />
                      </a-form-item>
                    </a-col>
                  </a-row>
                  <a-row :gutter="24">
                    <a-col :span="12">
                      <a-form-item label="角色">
                        <a-select v-model:value="editForm.role" :disabled="!isGlobalManager()">
                          <a-select-option value="USER">普通用户</a-select-option>
                          <a-select-option v-if="isGlobalManager() || editForm.role === 'ADMIN_UNIT'" value="ADMIN_UNIT">
                            单位管理员
                          </a-select-option>
                          <a-select-option v-if="isGlobalManager()" value="ADMIN_GLOBAL">全局管理员</a-select-option>
                        </a-select>
                      </a-form-item>
                    </a-col>
                    <a-col :span="12">
                      <a-form-item label="风险等级">
                        <a-select v-model:value="editForm.riskLevel">
                          <a-select-option value="NORMAL">普通人员</a-select-option>
                          <a-select-option value="KEY">重点人员</a-select-option>
                          <a-select-option value="RISK">风险人员</a-select-option>
                          <a-select-option value="ATTENTION">关注人员</a-select-option>
                        </a-select>
                      </a-form-item>
                    </a-col>
                  </a-row>
                  <a-alert
                    message="单位管理员设置说明"
                    description="将角色设置为'单位管理员'后，该人员可进入管理页面并管理本部门人员；全局管理员仅允许全局管理员账号设置。"
                    type="info"
                    show-icon
                    style="margin-bottom: 16px"
                  />
                  <a-button type="primary" @click="saveBasicInfo" :loading="saving">保存基本信息</a-button>
                </a-form>
              </a-tab-pane>

              <!-- 人员调动管理 (仅限全局管理员或单位管理员在单位内) -->
              <a-tab-pane key="transfer" tab="人员调动/层级管理">
                <div class="transfer-section">
                  <div class="section-title">部门调动</div>
                  <a-alert message="调动部门后，该人员的原有上级关系将自动清空，请重新分配。" type="info" show-icon style="margin-bottom: 16px" />
                  <a-form layout="inline">
                    <a-form-item label="目标部门">
                      <a-select
                        v-model:value="transferDeptId"
                        style="width: 250px"
                        placeholder="请选择部门"
                        show-search
                        option-filter-prop="label"
                      >
                        <a-select-option v-for="dept in departments" :key="dept.id" :value="dept.id" :label="dept.deptName">
                          {{ dept.deptName }}
                        </a-select-option>
                      </a-select>
                    </a-form-item>
                    <a-form-item>
                      <a-button type="primary" danger @click="handleTransfer" :loading="transferring">执行调动</a-button>
                    </a-form-item>
                  </a-form>

                  <a-divider />

                  <div class="section-title">分配直接上级</div>
                  <a-form layout="inline">
                    <a-form-item label="直接上级">
                      <a-select
                        v-model:value="newSuperiorJobNo"
                        style="width: 250px"
                        placeholder="请选择上级人员"
                        show-search
                        option-filter-prop="label"
                        allow-clear
                      >
                        <a-select-option v-for="u in sameDeptUsers" :key="u.jobNo" :value="u.jobNo" :label="u.name">
                          {{ u.name }} ({{ u.position }})
                        </a-select-option>
                      </a-select>
                    </a-form-item>
                    <a-form-item>
                      <a-button type="primary" @click="handleAssignSuperior" :loading="assigning">确认分配</a-button>
                    </a-form-item>
                  </a-form>
                </div>
              </a-tab-pane>

              <!-- 违规记录管理 (仅单位管理员或全局管理员) -->
              <a-tab-pane key="violation" tab="违规记录管理">
                <div class="violation-section">
                  <div class="section-header">
                    <div class="section-title">违规记录列表</div>
                    <a-button type="primary" danger @click="showAddViolation">
                      <template #icon><plus-outlined /></template>
                      新增违规记录
                    </a-button>
                  </div>
                  <a-table :columns="violationColumns" :data-source="violationRecords" row-key="id" size="small">
                    <template #bodyCell="{ column, record }">
                      <template v-if="column.key === 'violationTime'">
                        {{ dayjs(record.violationTime).format('YYYY-MM-DD') }}
                      </template>
                    </template>
                  </a-table>
                </div>
              </a-tab-pane>

              <a-tab-pane key="history" tab="层级历史记录">
                <div class="history-section">
                  <div class="section-header">
                    <div class="section-title">历史变动记录</div>
                    <a-button @click="fetchHierarchyHistory">刷新记录</a-button>
                  </div>
                  <a-table :columns="historyColumns" :data-source="historyRecords" row-key="id" size="small">
                    <template #bodyCell="{ column, record }">
                      <template v-if="column.key === 'period'">
                        {{ dayjs(record.startDate).format('YYYY-MM-DD HH:mm:ss') }} -
                        {{ record.endDate ? dayjs(record.endDate).format('YYYY-MM-DD HH:mm:ss') : '至今' }}
                      </template>
                      <template v-else-if="column.key === 'manager'">
                        {{ record.managerName || '' }} {{ record.managerJobNo ? `(${record.managerJobNo})` : '' }}
                      </template>
                    </template>
                  </a-table>
                </div>
              </a-tab-pane>

              <!-- 权限管理 (仅限全局管理员) -->
              <a-tab-pane v-if="isGlobalManager()" key="permissions" tab="权限管理">
                <div class="permission-section">
                  <div class="section-header">
                    <span class="section-title">功能权限分配</span>
                    <a-button type="primary" size="small" @click="savePermissions" :loading="permSaving">保存权限</a-button>
                  </div>
                  <a-spin :spinning="permLoading">
                    <a-checkbox-group v-model:value="selectedPermissions" style="width: 100%">
                      <a-row :gutter="[16, 16]">
                        <a-col :span="12" v-for="perm in allPermissions" :key="perm.code">
                          <a-checkbox :value="perm.code">
                            <div>
                              <div style="font-weight: 500">{{ perm.name }}</div>
                              <div style="font-size: 12px; color: #999">{{ perm.description }}</div>
                            </div>
                          </a-checkbox>
                        </a-col>
                      </a-row>
                    </a-checkbox-group>
                  </a-spin>
                </div>
              </a-tab-pane>
            </a-tabs>
          </div>
          <a-empty v-else description="请从左侧选择一个人员" />
        </a-card>
      </a-col>
    </a-row>

    <!-- 新增人员弹窗 -->
    <a-modal
      v-model:open="createUserModal.visible"
      title="新增人员"
      @ok="handleCreateUser"
      :confirmLoading="createUserModal.submitting"
    >
      <a-form :model="createUserForm" layout="vertical">
        <a-form-item label="工号" required>
          <a-input v-model:value="createUserForm.jobNo" placeholder="请输入工号" />
        </a-form-item>
        <a-form-item label="姓名" required>
          <a-input v-model:value="createUserForm.name" placeholder="请输入姓名" />
        </a-form-item>
        <a-form-item label="职位" required>
          <a-input v-model:value="createUserForm.position" placeholder="请输入职位" />
        </a-form-item>
        <a-form-item label="所属部门" required>
          <a-select v-model:value="createUserForm.deptId" placeholder="请选择部门" show-search option-filter-prop="label">
            <a-select-option v-for="dept in departments" :key="dept.id" :value="dept.id" :label="dept.deptName">
              {{ dept.deptName }}
            </a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="初始密码" required>
          <a-input-password v-model:value="createUserForm.password" placeholder="请输入初始密码（不少于6位）" />
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- 新增违规记录弹窗 -->
    <a-modal
      v-model:open="violationModal.visible"
      title="新增违规记录"
      @ok="handleAddViolation"
      :confirmLoading="violationModal.submitting"
    >
      <a-form :model="violationForm" layout="vertical">
        <a-form-item label="违规时间" required>
          <a-date-picker v-model:value="violationForm.violationTime" style="width: 100%" placeholder="请选择违规时间" />
        </a-form-item>
        <a-form-item label="违规事由" required>
          <a-textarea v-model:value="violationForm.reason" :rows="4" placeholder="请输入具体违规事由" />
        </a-form-item>
        <a-form-item label="处理结果" required>
          <a-input v-model:value="violationForm.punishment" placeholder="请输入受到的处罚或处理结果" />
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- 批量导入弹窗 -->
    <a-modal
      v-model:open="importModal.visible"
      title="批量导入人员"
      :footer="null"
      width="600px"
    >
      <div class="import-content">
        <a-alert
          message="Excel格式说明"
          type="info"
          show-icon
          style="margin-bottom: 16px"
        >
          <template #description>
            <p>请按以下列顺序准备Excel文件（第1行为表头，数据从第2行开始）：</p>
            <p><strong>警号 | 姓名 | 部门名称 | 职务 | 手机号 | 上级警号</strong></p>
            <p>其中警号和姓名为必填，部门名称需与系统中已有部门完全一致。</p>
            <p>默认密码为警号，用户登录后可自行修改。</p>
          </template>
        </a-alert>

        <a-upload
          :file-list="importModal.fileList"
          :before-upload="beforeImportUpload"
          :remove="handleImportRemove"
          accept=".xlsx,.xls"
          :max-count="1"
        >
          <a-button>
            <template #icon><UploadOutlined /></template>
            选择Excel文件
          </a-button>
        </a-upload>

        <div v-if="importModal.result" style="margin-top: 16px">
          <a-alert
            :type="importModal.result.errors?.length ? 'warning' : 'success'"
            show-icon
          >
            <template #description>
              <p>成功导入 <strong>{{ importModal.result.success }}</strong> 人</p>
              <p v-if="importModal.result.skipped > 0">跳过 <strong>{{ importModal.result.skipped }}</strong> 条</p>
              <div v-if="importModal.result.errors?.length" style="margin-top: 8px">
                <p style="color: #faad14; margin-bottom: 4px">详细信息：</p>
                <p v-for="(err, idx) in importModal.result.errors" :key="idx" style="font-size: 12px; color: #999">{{ err }}</p>
              </div>
            </template>
          </a-alert>
        </div>

        <div style="margin-top: 16px; text-align: right">
          <a-space>
            <a-button @click="importModal.visible = false">关闭</a-button>
            <a-button type="primary" :loading="importModal.uploading" :disabled="importModal.fileList.length === 0" @click="handleImportSubmit">
              开始导入
            </a-button>
          </a-space>
        </div>
      </div>
    </a-modal>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref, watch } from 'vue';
import { PlusOutlined, UploadOutlined } from '@ant-design/icons-vue';
import axios from '../utils/axios';
import { message } from 'ant-design-vue';
import dayjs from 'dayjs';
import { getCurrentUser } from '../utils/auth';

const searchKey = ref('');
const treeData = ref([]);
const selectedUser = ref(null);
const activeTab = ref('basic');
const loading = ref(false);
const saving = ref(false);
const transferring = ref(false);
const assigning = ref(false);

const departments = ref([]);
const sameDeptUsers = ref([]);
const transferDeptId = ref(undefined);
const newSuperiorJobNo = ref(undefined);

const violationRecords = ref([]);
const historyRecords = ref([]);
const violationModal = reactive({
  visible: false,
  submitting: false
});
const violationForm = reactive({
  violationTime: dayjs(),
  reason: '',
  punishment: ''
});

const createUserModal = reactive({
  visible: false,
  submitting: false
});
const createUserForm = reactive({
  jobNo: '',
  name: '',
  position: '',
  deptId: undefined,
  password: ''
});

const editForm = reactive({
  name: '',
  position: '',
  role: '',
  riskLevel: 'NORMAL'
});

const violationColumns = [
  { title: '时间', dataIndex: 'violationTime', key: 'violationTime', width: 120 },
  { title: '违规事由', dataIndex: 'reason', key: 'reason' },
  { title: '处理结果', dataIndex: 'punishment', key: 'punishment', width: 150 },
];

const historyColumns = [
  { title: '任职时间段', key: 'period', width: 280 },
  { title: '所在部门', dataIndex: 'unitName', key: 'unitName' },
  { title: '直接上级', key: 'manager', width: 180 }
];

const currentUser = getCurrentUser();
const fullTreeData = ref([]);
const allKeys = ref([]);
const expandedKeys = ref([]);
const autoExpandParent = ref(true);

const isGlobalManager = () => currentUser.jobNo === 'admin' || currentUser.role === 'ADMIN_GLOBAL';

const getErrorMessage = (error, fallback = '服务器错误') => {
  const data = error?.response?.data;
  if (typeof data === 'string' && data.trim()) {
    return data;
  }
  if (data?.message) {
    return data.message;
  }
  return fallback;
};

const findNodeByKey = (nodes, key) => {
  for (const node of nodes) {
    if (node.key === key || node.id === key) {
      return node;
    }
    if (node.children?.length) {
      const found = findNodeByKey(node.children, key);
      if (found) return found;
    }
  }
  return null;
};

const fetchTree = async () => {
  loading.value = true;
  try {
    const res = await axios.get('/api/organization/tree');
    // 转换为 a-tree 格式
    const transform = (node) => {
      const isUser = node.type === 'user';
      // 这里的 node.deptId 是后端新加的
      const nodeDeptId = node.deptId || (node.id.startsWith('dept_') ? parseInt(node.id.replace('dept_', '')) : null);

      // 单位管理员只能选择自己部门的人
      const selectable = isGlobalManager()
        ? isUser
        : (isUser && nodeDeptId === currentUser.department?.id);

      const result = {
        key: node.id,
        title: node.name,
        name: node.name,
        type: node.type,
        ...node,
        isLeaf: !node.children || node.children.length === 0,
        selectable: selectable
      };
      if (node.children && node.children.length > 0) {
        result.children = node.children.map(transform);
      }
      return result;
    };

    const root = transform(res);
    const transformed = [root];
    treeData.value = transformed;
    fullTreeData.value = transformed;

    // 收集所有key用于全部展开
    const collectKeys = (nodes) => {
      nodes.forEach(n => {
        allKeys.value.push(n.key);
        if (n.children) collectKeys(n.children);
      });
    };
    allKeys.value = [];
    collectKeys(treeData.value);

    // 恢复之前的展开状态或默认展开根节点
    const savedKeys = localStorage.getItem('admin_org_tree_expanded_keys');
    if (savedKeys) {
      try {
        expandedKeys.value = JSON.parse(savedKeys);
      } catch (e) {
        expandedKeys.value = [root.key];
      }
    } else {
      expandedKeys.value = [root.key];
    }
  } catch (error) {
    message.error('获取组织架构失败');
  } finally {
    loading.value = false;
  }
};

const onSearch = () => {
  if (!searchKey.value) {
    treeData.value = fullTreeData.value;
    expandedKeys.value = treeData.value.length > 0 ? [treeData.value[0].key] : [];
    return;
  }

  const matchedKeys = [];
  const findKeys = (nodes) => {
    nodes.forEach(node => {
      const match = node.name.includes(searchKey.value) ||
                   (node.jobNo && node.jobNo.includes(searchKey.value));
      if (match) {
        // 匹配到了，需要展开其父级
        matchedKeys.push(node.key);
      }
      if (node.children) {
        findKeys(node.children);
      }
    });
  };

  findKeys(fullTreeData.value);

  // 获取所有需要展开的父级key
  const expandSet = new Set();
  const getParentKeys = (key, nodes, parents = []) => {
    for (let node of nodes) {
      if (node.key === key) return parents;
      if (node.children) {
        const res = getParentKeys(key, node.children, [...parents, node.key]);
        if (res) return res;
      }
    }
    return null;
  };

  matchedKeys.forEach(key => {
    const parents = getParentKeys(key, fullTreeData.value);
    if (parents) parents.forEach(pk => expandSet.add(pk));
  });

  expandedKeys.value = Array.from(expandSet);
  autoExpandParent.value = true;

  // 过滤树数据
  const filterTree = (nodes) => {
    return nodes.reduce((acc, node) => {
      const match = node.name.includes(searchKey.value) ||
                   (node.jobNo && node.jobNo.includes(searchKey.value));
      const children = node.children ? filterTree(node.children) : [];
      if (match || children.length > 0) {
        acc.push({ ...node, children: children.length > 0 ? children : undefined });
      }
      return acc;
    }, []);
  };

  treeData.value = filterTree(fullTreeData.value);
};

const fetchDepts = async () => {
  try {
    const res = await axios.get('/api/organization/departments');
    departments.value = res.map(d => ({ id: d.id, deptName: d.name }));
  } catch (error) {
    // ignore
  }
};

const onSearchChange = (e) => {
  if (!e.target.value) {
    onSearch();
  }
};

const highlightText = (text) => {
  if (!text) return '';
  if (!searchKey.value) return text;
  const regex = new RegExp(`(${searchKey.value})`, 'gi');
  return text.toString().replace(regex, '<span class="highlight">$1</span>');
};

// 树操作
const onExpand = (keys) => {
  expandedKeys.value = keys;
  autoExpandParent.value = false;
  // 保存展开状态
  localStorage.setItem('admin_org_tree_expanded_keys', JSON.stringify(keys));
};

const expandAll = () => {
  expandedKeys.value = [...allKeys.value];
  // 保存展开状态
  localStorage.setItem('admin_org_tree_expanded_keys', JSON.stringify(allKeys.value));
};

const collapseAll = () => {
  expandedKeys.value = [];
  // 保存展开状态
  localStorage.setItem('admin_org_tree_expanded_keys', JSON.stringify([]));
};

const onSelect = async (selectedKeys, info) => {
  const selectedKey = selectedKeys?.[0];
  const node = selectedKey ? findNodeByKey(fullTreeData.value, selectedKey) : null;
  if (!node || node.type !== 'user') {
    selectedUser.value = null; // 如果选中的不是人员，清空详情
    return;
  }

  loading.value = true;
  try {
    const jobNo = node.jobNo || (node.id && node.id.startsWith('user_') ? node.id.substring(5) : node.id);
    const res = await axios.get(`/api/organization/user/${jobNo}`);
    selectedUser.value = res;

    // 初始化编辑表单
    Object.assign(editForm, {
      name: res.name,
      position: res.position,
      role: res.role || 'USER',
      riskLevel: res.riskLevel || (res.isKeyPersonnel ? 'KEY' : 'NORMAL')
    });

    transferDeptId.value = res.department?.id;
    newSuperiorJobNo.value = res.superiorJobNo;

    await loadDepartmentUsers(transferDeptId.value, jobNo);

    await Promise.all([
      fetchViolationRecords(jobNo),
      fetchHierarchyHistory(jobNo)
    ]);

  } catch (error) {
    message.error('获取人员详情失败，请检查后端服务');
  } finally {
    loading.value = false;
  }
};

const saveBasicInfo = async () => {
  if (!selectedUser.value) return;
  saving.value = true;
  const payload = {
    name: editForm.name,
    position: editForm.position,
    role: editForm.role,
    riskLevel: editForm.riskLevel,
    isKeyPersonnel: editForm.riskLevel === 'KEY',
    operatorJobNo: currentUser.jobNo
  };
  try {
    await axios.put(`/api/users/${selectedUser.value.jobNo}`, payload);
    message.success('保存成功');
    await fetchTree();
  } catch (error) {
    message.error(`保存失败：${getErrorMessage(error)}`);
  } finally {
    saving.value = false;
  }
};

const handleTransfer = async () => {
  if (!transferDeptId.value || !selectedUser.value?.jobNo) return;
  transferring.value = true;
  try {
    await axios.post('/api/users/transfer', {
      jobNo: selectedUser.value.jobNo,
      newDeptId: transferDeptId.value,
      adminJobNo: currentUser.jobNo
    });
    message.success('人员调动成功');
    await fetchTree();
    const movedUserKey = `user_${selectedUser.value.jobNo}`;
    const movedUserNode = findNodeByKey(fullTreeData.value, movedUserKey);
    if (movedUserNode) {
      await onSelect([movedUserKey], {});
    } else {
      selectedUser.value = null;
      newSuperiorJobNo.value = undefined;
      sameDeptUsers.value = [];
      historyRecords.value = [];
      violationRecords.value = [];
    }
  } catch (error) {
    message.error(`调动失败：${getErrorMessage(error)}`);
  } finally {
    transferring.value = false;
  }
};

const handleAssignSuperior = async () => {
  if (!selectedUser.value?.jobNo) return;
  assigning.value = true;
  try {
    await axios.post('/api/users/assign-superior', {
      jobNo: selectedUser.value.jobNo,
      superiorJobNo: newSuperiorJobNo.value,
      adminJobNo: currentUser.jobNo
    });
    message.success('分配成功');
    selectedUser.value.superiorJobNo = newSuperiorJobNo.value || null;
    await fetchTree();
    await fetchHierarchyHistory(selectedUser.value.jobNo);
  } catch (error) {
    message.error(`分配失败：${getErrorMessage(error)}`);
  } finally {
    assigning.value = false;
  }
};

const showCreateUser = () => {
  createUserForm.jobNo = '';
  createUserForm.name = '';
  createUserForm.position = '';
  createUserForm.deptId = undefined;
  createUserForm.password = '';
  createUserModal.visible = true;
};

const handleCreateUser = async () => {
  if (!createUserForm.jobNo || !createUserForm.name || !createUserForm.position || !createUserForm.deptId || !createUserForm.password) {
    message.warning('请填写完整信息');
    return;
  }
  createUserModal.submitting = true;
  try {
    await axios.post('/api/users/create', {
      jobNo: createUserForm.jobNo,
      name: createUserForm.name,
      position: createUserForm.position,
      deptId: createUserForm.deptId,
      password: createUserForm.password
    });
    message.success('创建成功');
    createUserModal.visible = false;
    await fetchTree();
  } catch (error) {
    const msg = error.response?.data?.error || error.response?.data?.message || '创建失败';
    message.error(msg);
  } finally {
    createUserModal.submitting = false;
  }
};

const handleDeleteUser = async () => {
  if (!selectedUser.value) return;
  try {
    await axios.delete(`/api/users/${selectedUser.value.jobNo}`);
    message.success('用户已删除');
    selectedUser.value = null;
    await fetchTree();
  } catch (error) {
    const msg = error.response?.data?.error || error.response?.data?.message || '删除失败';
    message.error(msg);
  }
};

const showAddViolation = () => {
  violationForm.violationTime = dayjs();
  violationForm.reason = '';
  violationForm.punishment = '';
  violationModal.visible = true;
};

const handleAddViolation = async () => {
  if (!violationForm.violationTime || !violationForm.reason || !violationForm.punishment) {
    message.warning('请填写完整信息');
    return;
  }
  violationModal.submitting = true;
  try {
    await axios.post('/api/users/violation-records', {
      targetJobNo: selectedUser.value.jobNo,
      violationTime: violationForm.violationTime.format('YYYY-MM-DD HH:mm:ss'),
      reason: violationForm.reason,
      punishment: violationForm.punishment,
      createdBy: currentUser.jobNo
    });
    message.success('添加成功');
    violationModal.visible = false;
    await fetchViolationRecords(selectedUser.value.jobNo);
  } catch (error) {
    message.error('添加失败');
  } finally {
    violationModal.submitting = false;
  }
};

const fetchViolationRecords = async (jobNo = selectedUser.value?.jobNo) => {
  if (!jobNo) return;
  violationRecords.value = await axios.get(`/api/users/${jobNo}/violation-records`);
};

const fetchHierarchyHistory = async (jobNo = selectedUser.value?.jobNo) => {
  if (!jobNo) return;
  historyRecords.value = await axios.get(`/api/users/${jobNo}/hierarchy-history`);
};

const loadDepartmentUsers = async (deptId, currentJobNo = selectedUser.value?.jobNo) => {
  if (!deptId) {
    sameDeptUsers.value = [];
    return;
  }
  const deptUsersRes = await axios.get(`/api/users/list?deptId=${deptId}`);
  sameDeptUsers.value = deptUsersRes.filter(u => u.jobNo !== currentJobNo);
};

watch(transferDeptId, async (deptId) => {
  if (!selectedUser.value) return;
  newSuperiorJobNo.value = undefined;
  await loadDepartmentUsers(deptId, selectedUser.value.jobNo);
});

// 批量导入
const importModal = reactive({
  visible: false,
  fileList: [],
  uploading: false,
  result: null
});

const showImportModal = () => {
  importModal.visible = true;
  importModal.fileList = [];
  importModal.result = null;
};

const beforeImportUpload = (file) => {
  const isExcel = file.name.endsWith('.xlsx') || file.name.endsWith('.xls');
  if (!isExcel) {
    message.error('只能上传 Excel 文件！');
    return false;
  }
  importModal.fileList = [file];
  return false;
};

const handleImportRemove = () => {
  importModal.fileList = [];
};

const handleImportSubmit = async () => {
  if (importModal.fileList.length === 0) return;
  importModal.uploading = true;
  importModal.result = null;
  try {
    const formData = new FormData();
    formData.append('file', importModal.fileList[0]);
    const res = await axios.post('/api/users/batch-import', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    });
    importModal.result = res;
    message.success(`导入完成：成功 ${res.success} 条`);
    fetchTree();
  } catch (e) {
    message.error(e.response?.data?.error || '导入失败');
  } finally {
    importModal.uploading = false;
  }
};

// 权限管理
const allPermissions = ref([]);
const selectedPermissions = ref([]);
const permLoading = ref(false);
const permSaving = ref(false);

const fetchAllPermissions = async () => {
  if (allPermissions.value.length > 0) return;
  try {
    const res = await axios.get('/api/permissions/all');
    allPermissions.value = Array.isArray(res) ? res : [];
  } catch (e) {
    console.error('获取权限列表失败:', e);
    allPermissions.value = [];
  }
};

const fetchUserPermissions = async () => {
  if (!selectedUser.value?.jobNo) return;
  permLoading.value = true;
  try {
    const res = await axios.get(`/api/permissions/user/${selectedUser.value.jobNo}`);
    selectedPermissions.value = res.permissions || [];
  } catch (e) {
    selectedPermissions.value = [];
  } finally {
    permLoading.value = false;
  }
};

const savePermissions = async () => {
  if (!selectedUser.value?.jobNo) return;
  permSaving.value = true;
  try {
    await axios.post(`/api/permissions/user/${selectedUser.value.jobNo}`, {
      permissionCodes: selectedPermissions.value
    });
    message.success('权限保存成功');
  } catch (e) {
    message.error(e.response?.data?.error || '权限保存失败');
  } finally {
    permSaving.value = false;
  }
};

watch([activeTab, selectedUser], ([tab, user]) => {
  if (tab === 'permissions' && user) {
    fetchAllPermissions();
    fetchUserPermissions();
  }
});

onMounted(() => {
  fetchTree();
  fetchDepts();
});
</script>

<style scoped>
.admin-management-container {
  padding: 24px;
  background-color: transparent;
  height: calc(100vh - 64px);
}

.full-height {
  height: 100%;
}

.tree-card, .detail-card {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: rgba(0, 21, 41, 0.85);
  border: 1px solid rgba(0, 212, 255, 0.2);
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
}


.header-toolbar {
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid rgba(0, 212, 255, 0.15);
}

.toolbar-row {
  display: flex;
  align-items: center;
  gap: 8px;
}

.toolbar-row + .toolbar-row {
  margin-top: 8px;
}

.page-title {
  font-size: 16px;
  font-weight: bold;
  color: #fff;
  flex-shrink: 0;
}

.tree-wrapper {
  flex: 1;
  overflow-y: auto;
  max-height: calc(100vh - 250px);
}

/* 自定义滚动条 */
.tree-wrapper::-webkit-scrollbar {
  width: 6px;
}
.tree-wrapper::-webkit-scrollbar-thumb {
  background: rgba(0, 212, 255, 0.3);
  border-radius: 3px;
}
.tree-wrapper::-webkit-scrollbar-track {
  background: rgba(0, 21, 41, 0.4);
}


.node-title-wrapper {
  display: flex;
  align-items: center;
  padding: 4px 8px;
  border-radius: 4px;
  transition: all 0.3s;
  width: 100%;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.user-node-hover:hover {
  background-color: rgba(0, 212, 255, 0.08) !important;
  cursor: pointer;
}

.dept-node-hover:hover {
  background-color: rgba(0, 212, 255, 0.05) !important;
  cursor: pointer;
}

.node-icon {
  margin-right: 8px;
  font-size: 16px;
}

.company-title {
  font-weight: bold;
  color: #fff;
  font-size: 15px;
}

.dept-title {
  font-weight: bold;
  color: #ccd6f6;
}

.position-title {
  color: #ccd6f6;
}

.user-name {
  color: #ccd6f6;
  font-weight: 500;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.user-position-label {
  color: #8892b0;
  font-weight: normal;
  margin-left: 4px;
}

.user-job-no {
  color: #6a7593;
  margin-left: 8px;
  font-size: 12px;
}

.node-count {
  color: #6a7593;
  font-size: 12px;
  margin-left: 8px;
}

.user-node-content {
  display: flex;
  align-items: center;
  flex: 1;
  min-width: 0;
}

.user-node-content.key-personnel {
  color: #ff4d4f;
  font-weight: bold;
}

.user-node-content.key-personnel .user-name {
  color: #ff4d4f;
}

.user-node-content.key-personnel .user-job-no {
  color: #ff7875;
}

.add-talk-btn {
  margin-left: auto;
  padding-right: 0;
}

.highlight {
  background-color: rgba(0, 212, 255, 0.3);
  color: #fff;
  padding: 0 2px;
  border-radius: 2px;
}

.admin-content {
  padding: 0 16px;
  flex: 1;
  overflow-y: auto;
}

.mgmt-form {
  max-width: 600px;
  margin-top: 16px;
}

/* Section titles */
.section-title {
  font-size: 16px;
  font-weight: bold;
  margin-bottom: 16px;
  color: #fff;
  border-left: 4px solid #00d4ff;
  padding-left: 8px;
}

.transfer-section, .violation-section {
  padding-top: 16px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.empty-container {
  padding: 80px 0;
}

/* Ant Design dark overrides */
.tree-card :deep(.ant-card-body),
.detail-card :deep(.ant-card-body) {
  background: transparent;
  color: #ccd6f6;
}

.detail-card :deep(.ant-card-head) {
  background: transparent;
  border-bottom: 1px solid rgba(0, 212, 255, 0.15);
}

.detail-card :deep(.ant-card-head-title) {
  color: #fff;
}

/* Tabs */
.admin-content :deep(.ant-tabs-nav) {
  margin-bottom: 16px;
}

.admin-content :deep(.ant-tabs-tab) {
  color: #8892b0;
}

.admin-content :deep(.ant-tabs-tab-active .ant-tabs-tab-btn) {
  color: #00d4ff;
}

.admin-content :deep(.ant-tabs-ink-bar) {
  background: #00d4ff;
}

.admin-content :deep(.ant-tabs-content-holder) {
  color: #ccd6f6;
}

/* Form labels */
.admin-content :deep(.ant-form-item-label > label) {
  color: #ccd6f6;
}

/* Input fields */
.admin-content :deep(.ant-input),
.admin-content :deep(.ant-input-affix-wrapper),
.admin-content :deep(.ant-input-password) {
  background: rgba(0, 21, 41, 0.6);
  border-color: rgba(0, 212, 255, 0.2);
  color: #ccd6f6;
}

.admin-content :deep(.ant-input::placeholder) {
  color: #5a6a8a;
}

.admin-content :deep(.ant-input:hover),
.admin-content :deep(.ant-input-affix-wrapper:hover) {
  border-color: rgba(0, 212, 255, 0.35);
}

.admin-content :deep(.ant-input:focus),
.admin-content :deep(.ant-input-focused),
.admin-content :deep(.ant-input-affix-wrapper:focus),
.admin-content :deep(.ant-input-affix-wrapper-focused) {
  border-color: #00d4ff;
  box-shadow: 0 0 0 2px rgba(0, 212, 255, 0.2);
}

/* Select */
.admin-content :deep(.ant-select-selector) {
  background: rgba(0, 21, 41, 0.6) !important;
  border-color: rgba(0, 212, 255, 0.2) !important;
  color: #ccd6f6 !important;
}

.admin-content :deep(.ant-select-selection-item) {
  color: #ccd6f6;
}

.admin-content :deep(.ant-select-selection-placeholder) {
  color: #5a6a8a;
}

.admin-content :deep(.ant-select-arrow) {
  color: #5a6a8a;
}

/* Table */
.admin-content :deep(.ant-table) {
  background: transparent;
  color: #ccd6f6;
}

.admin-content :deep(.ant-table-thead > tr > th) {
  background: rgba(0, 21, 41, 0.6);
  color: #ccd6f6;
  border-color: rgba(0, 212, 255, 0.15);
}

.admin-content :deep(.ant-table-tbody > tr > td) {
  border-color: rgba(0, 212, 255, 0.1);
  color: #ccd6f6;
}

.admin-content :deep(.ant-table-tbody > tr:hover > td) {
  background: rgba(0, 212, 255, 0.08);
}

.admin-content :deep(.ant-table-wrapper .ant-table-container) {
  border-color: rgba(0, 212, 255, 0.15);
}

/* Alert */
.admin-content :deep(.ant-alert) {
  background: rgba(0, 21, 41, 0.6);
  border-color: rgba(0, 212, 255, 0.2);
}

.admin-content :deep(.ant-alert-message) {
  color: #ccd6f6;
}

.admin-content :deep(.ant-alert-description) {
  color: #8892b0;
}

/* Section title */
.admin-content :deep(.ant-divider) {
  border-color: rgba(0, 212, 255, 0.15);
}

/* Tree dark theme */
.custom-tree :deep(.ant-tree-title) {
  color: #ccd6f6;
}

.custom-tree :deep(.ant-tree-node-content-wrapper) {
  background: transparent;
}

.custom-tree :deep(.ant-tree-node-content-wrapper:hover) {
  background: rgba(0, 212, 255, 0.08);
}

.custom-tree :deep(.ant-tree-node-selected .ant-tree-node-content-wrapper) {
  background: rgba(0, 212, 255, 0.15);
}

.custom-tree :deep(.ant-tree-switcher) {
  color: #5a6a8a;
}

/* Search input in header */
.header-toolbar :deep(.ant-input-affix-wrapper) {
  background: rgba(0, 21, 41, 0.6);
  border-color: rgba(0, 212, 255, 0.2);
  color: #ccd6f6;
}

.header-toolbar :deep(.ant-input) {
  background: transparent;
  color: #ccd6f6;
}

.header-toolbar :deep(.ant-input::placeholder) {
  color: #5a6a8a;
}

.header-toolbar :deep(.ant-input-search .ant-input-search-button) {
  background: rgba(0, 212, 255, 0.15);
  border-color: rgba(0, 212, 255, 0.3);
  color: #00d4ff;
}

</style>
