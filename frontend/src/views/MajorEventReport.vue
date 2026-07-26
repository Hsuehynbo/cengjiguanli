<template>
  <div class="major-event-page">
    <div class="page-header">
      <h2>个人重大事项申报</h2>
      <p>及时报告个人重大事项，强化纪律意识和规矩意识</p>
    </div>

    <!-- 操作栏 -->
    <div class="filter-bar">
      <a-button type="primary" @click="showModal = true">
        <PlusOutlined /> 新增申报
      </a-button>
      <a-select v-model:value="filterStatus" style="width: 140px" placeholder="申报状态">
        <a-select-option value="">全部状态</a-select-option>
        <a-select-option value="pending">待审核</a-select-option>
        <a-select-option value="approved">已通过</a-select-option>
        <a-select-option value="rejected">已退回</a-select-option>
      </a-select>
      <a-select v-model:value="filterType" style="width: 180px" placeholder="事项类型">
        <a-select-option value="">全部类型</a-select-option>
        <a-select-option v-for="t in eventTypes" :key="t" :value="t">{{ t }}</a-select-option>
      </a-select>
      <div style="flex:1;"></div>
      <a-statistic title="累计申报" :value="records.length" style="text-align: right;" />
    </div>

    <!-- 表格 -->
    <div class="table-container">
      <a-table :dataSource="filteredRecords" :columns="columns" :pagination="{ pageSize: 8 }" rowKey="id" size="middle">
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'type'">
            <a-tag color="blue">{{ record.type }}</a-tag>
          </template>
          <template v-if="column.key === 'status'">
            <a-tag :color="statusColors[record.status]">{{ statusLabels[record.status] }}</a-tag>
          </template>
          <template v-if="column.key === 'action'">
            <a-button type="link" size="small" @click="viewDetail(record)">查看详情</a-button>
          </template>
        </template>
      </a-table>
    </div>

    <!-- 新增申报弹窗 -->
    <a-modal v-model:open="showModal" title="新增重大事项申报" width="600px" @ok="handleSubmit" okText="提交申报" cancelText="取消">
      <a-form :labelCol="{ span: 5 }" style="margin-top: 16px;">
        <a-form-item label="事项类型" required>
          <a-select v-model:value="form.type" placeholder="请选择事项类型">
            <a-select-option v-for="t in eventTypes" :key="t" :value="t">{{ t }}</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="发生日期" required>
          <a-date-picker v-model:value="form.date" style="width: 100%;" placeholder="选择日期" />
        </a-form-item>
        <a-form-item label="事项摘要" required>
          <a-textarea v-model:value="form.summary" :rows="3" placeholder="简要描述重大事项内容" />
        </a-form-item>
        <a-form-item label="详细说明">
          <a-textarea v-model:value="form.detail" :rows="4" placeholder="详细说明事项经过、涉及金额等（选填）" />
        </a-form-item>
        <a-form-item label="附件材料">
          <a-upload :beforeUpload="() => false" :maxCount="3">
            <a-button><UploadOutlined /> 上传文件</a-button>
          </a-upload>
          <div style="font-size: 12px; color: var(--text-muted); margin-top: 4px;">支持 PDF、图片，最多3个文件</div>
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- 详情弹窗 -->
    <a-modal v-model:open="showDetail" title="申报详情" width="560px" :footer="null">
      <div v-if="detailRecord" class="detail-content">
        <a-descriptions :column="1" bordered size="small">
          <a-descriptions-item label="事项类型">{{ detailRecord.type }}</a-descriptions-item>
          <a-descriptions-item label="发生日期">{{ detailRecord.date }}</a-descriptions-item>
          <a-descriptions-item label="申报时间">{{ detailRecord.submitTime }}</a-descriptions-item>
          <a-descriptions-item label="审核状态">
            <a-tag :color="statusColors[detailRecord.status]">{{ statusLabels[detailRecord.status] }}</a-tag>
          </a-descriptions-item>
          <a-descriptions-item label="事项摘要">{{ detailRecord.summary }}</a-descriptions-item>
          <a-descriptions-item label="详细说明" v-if="detailRecord.detail">{{ detailRecord.detail }}</a-descriptions-item>
          <a-descriptions-item label="审核意见" v-if="detailRecord.comment">{{ detailRecord.comment }}</a-descriptions-item>
        </a-descriptions>
      </div>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { PlusOutlined, UploadOutlined } from '@ant-design/icons-vue'
import { message } from 'ant-design-vue'

const eventTypes = [
  '婚姻状况变化',
  '因私出国（境）',
  '房产购置/出售',
  '配偶子女从业变化',
  '子女就学变化',
  '投资入股',
  '被司法调查/处理',
  '其他重大事项',
]

const statusLabels = { pending: '待审核', approved: '已通过', rejected: '已退回' }
const statusColors = { pending: 'orange', approved: 'green', rejected: 'red' }

// ========== Mock数据 ==========
const records = ref([
  { id: 1, type: '房产购置', date: '2026-06-15', submitTime: '2026-06-16 09:30', summary: '购置商品房一套，面积120㎡，总价85万元', detail: '位于XX市XX区XX路XX号XX小区X栋X单元XXX室，通过商业贷款购买，首付30%，贷款59.5万元，月供3200元。', status: 'approved', comment: '材料齐全，已备案。' },
  { id: 2, type: '因私出国（境）', date: '2026-07-20', submitTime: '2026-07-10 14:15', summary: '计划赴泰国旅游，时间7天', detail: '与家属同行，已办理因私护照和泰国签证，行程为曼谷-清迈，费用自理。', status: 'pending', comment: '' },
  { id: 3, type: '婚姻状况变化', date: '2025-12-20', submitTime: '2025-12-22 10:00', summary: '登记结婚', detail: '配偶姓名：李XX，工作单位：XX市第一人民医院，职务：主治医师。', status: 'approved', comment: '已录入人事档案。' },
  { id: 4, type: '投资入股', date: '2026-03-08', submitTime: '2026-03-09 16:45', summary: '购买股票基金，金额5万元', detail: '通过证券账户购买XX基金，投资金额5万元，资金来源为个人工资储蓄。', status: 'approved', comment: '符合规定，已备案。' },
  { id: 5, type: '配偶子女从业变化', date: '2026-05-01', submitTime: '2026-05-03 08:20', summary: '配偶工作单位变更', detail: '配偶李XX从XX市第一人民医院调至XX市中心医院，职务不变，仍为主治医师。', status: 'approved', comment: '已更新信息。' },
  { id: 6, type: '子女就学变化', date: '2026-09-01', submitTime: '2026-07-15 11:30', summary: '子女入学XX市第一小学', detail: '儿子张XX，2020年出生，2026年9月入读XX市第一小学一年级。', status: 'pending', comment: '' },
  { id: 7, type: '其他重大事项', date: '2026-04-10', submitTime: '2026-04-11 09:00', summary: '获得市级表彰奖励', detail: '因工作表现突出，获得XX市公安局2025年度"优秀人民警察"称号，奖金5000元。', status: 'approved', comment: '已记录。' },
  { id: 8, type: '房产购置', date: '2024-08-20', submitTime: '2024-08-22 15:00', summary: '出售原有住房一套', detail: '出售XX市XX区XX路XX号住房，面积90㎡，售价62万元，已办理过户手续。', status: 'approved', comment: '已备案。' },
])

// ========== 筛选 ==========
const filterStatus = ref('')
const filterType = ref('')

const filteredRecords = computed(() => {
  return records.value.filter(r => {
    if (filterStatus.value && r.status !== filterStatus.value) return false
    if (filterType.value && r.type !== filterType.value) return false
    return true
  })
})

// ========== 表格列 ==========
const columns = [
  { title: '序号', dataIndex: 'id', key: 'id', width: 60 },
  { title: '事项类型', dataIndex: 'type', key: 'type', width: 150 },
  { title: '发生日期', dataIndex: 'date', key: 'date', width: 110 },
  { title: '事项摘要', dataIndex: 'summary', key: 'summary', ellipsis: true },
  { title: '申报时间', dataIndex: 'submitTime', key: 'submitTime', width: 160 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 90 },
  { title: '操作', key: 'action', width: 100 },
]

// ========== 新增申报 ==========
const showModal = ref(false)
const form = ref({ type: undefined, date: null, summary: '', detail: '' })

const handleSubmit = () => {
  if (!form.value.type || !form.value.date || !form.value.summary) {
    message.warning('请填写必填项')
    return
  }
  const now = new Date()
  const pad = (n) => String(n).padStart(2, '0')
  const dateStr = `${form.value.date.year()}-${pad(form.value.date.month() + 1)}-${pad(form.value.date.date())}`
  records.value.unshift({
    id: records.value.length + 1,
    type: form.value.type,
    date: dateStr,
    submitTime: `${now.getFullYear()}-${pad(now.getMonth() + 1)}-${pad(now.getDate())} ${pad(now.getHours())}:${pad(now.getMinutes())}`,
    summary: form.value.summary,
    detail: form.value.detail,
    status: 'pending',
    comment: '',
  })
  showModal.value = false
  form.value = { type: undefined, date: null, summary: '', detail: '' }
  message.success('申报已提交，待审核')
}

// ========== 详情 ==========
const showDetail = ref(false)
const detailRecord = ref(null)
const viewDetail = (record) => {
  detailRecord.value = record
  showDetail.value = true
}
</script>

<style scoped>
.major-event-page { max-width: 1200px; margin: 0 auto; }
.detail-content { margin-top: 8px; }
</style>
