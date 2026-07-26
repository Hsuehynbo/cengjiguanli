<template>
  <div class="ai-center">
    <div class="page-header">
      <h2>AI 智能预警中心</h2>
      <p>实时监控 · 智能分析 · 自动预警</p>
    </div>

    <!-- 顶部统计卡片 -->
    <div class="stat-row">
      <div class="monitor-stat" v-for="(s, i) in statCards" :key="i" :style="{ borderTopColor: s.color }">
        <div class="monitor-stat-icon" :style="{ background: s.bg, color: s.color }">
          <component :is="s.icon" style="font-size: 22px" />
        </div>
        <div class="monitor-stat-info">
          <div class="monitor-stat-value" :style="{ color: s.color }">
            <NumberCounter :value="s.value" :duration="1500" />
          </div>
          <div class="monitor-stat-label">{{ s.label }}</div>
        </div>
        <div class="monitor-stat-trend" :class="s.trendUp ? 'up' : 'down'">
          {{ s.trendUp ? '↑' : '↓' }} {{ s.trend }}
        </div>
      </div>
    </div>

    <!-- 中部：预警列表 + 数据源状态 -->
    <div class="main-row">
      <!-- 左侧：实时预警列表 -->
      <div class="alert-panel">
        <div class="panel-header">
          <span class="panel-title">
            <span class="live-dot"></span> 实时预警流
          </span>
          <a-tag color="red">今日 {{ alerts.length }} 条</a-tag>
        </div>
        <div class="alert-list" ref="alertListRef">
          <div v-for="(alert, idx) in alerts" :key="idx"
            class="alert-item"
            :class="'level-' + alert.level">
            <div class="alert-level-dot" :class="'dot-' + alert.level"></div>
            <div class="alert-body">
              <div class="alert-top">
                <span class="alert-name">{{ alert.name }}</span>
                <a-tag :color="levelColors[alert.level]" size="small">{{ levelLabels[alert.level] }}</a-tag>
                <span class="alert-score">{{ alert.score }}分</span>
              </div>
              <div class="alert-rule">{{ alert.rule }}</div>
              <div class="alert-summary">{{ alert.summary }}</div>
            </div>
            <div class="alert-time">{{ alert.time }}</div>
          </div>
        </div>
      </div>

      <!-- 右侧：17维数据源状态 -->
      <div class="source-panel">
        <div class="panel-header">
          <span class="panel-title">数据源状态</span>
          <a-tag color="green">{{ onlineCount }}/17 在线</a-tag>
        </div>
        <div class="source-grid">
          <div v-for="(src, idx) in dataSources" :key="idx" class="source-item">
            <span class="source-icon">{{ src.icon }}</span>
            <span class="source-name">{{ src.name }}</span>
            <span class="source-status" :class="src.online ? 'online' : 'offline'"></span>
            <span class="source-count">{{ src.count }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 底部图表 -->
    <div class="chart-row">
      <div class="chart-card">
        <h3>风险等级分布</h3>
        <div ref="pieChartRef" style="height: 280px;"></div>
      </div>
      <div class="chart-card">
        <h3>近6个月风险趋势</h3>
        <div ref="lineChartRef" style="height: 280px;"></div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed, nextTick } from 'vue'
import { AlertOutlined, WarningOutlined, ExclamationCircleOutlined, ClockCircleOutlined } from '@ant-design/icons-vue'
import * as echarts from 'echarts'
import NumberCounter from '../components/NumberCounter.vue'

// ========== 统计卡片 ==========
const statCards = [
  { label: '今日预警', value: 23, color: '#3b82f6', bg: 'rgba(59,130,246,0.1)', icon: AlertOutlined, trend: '12%', trendUp: true },
  { label: '高危预警', value: 3, color: '#ef4444', bg: 'rgba(239,68,68,0.1)', icon: WarningOutlined, trend: '1', trendUp: false },
  { label: '风险预警', value: 12, color: '#f59e0b', bg: 'rgba(245,158,11,0.1)', icon: ExclamationCircleOutlined, trend: '8%', trendUp: true },
  { label: '关注人员', value: 48, color: '#8b5cf6', bg: 'rgba(139,92,246,0.1)', icon: ClockCircleOutlined, trend: '5%', trendUp: false },
]

// ========== 预警列表 ==========
const levelColors = { high: 'red', risk: 'orange', attention: 'gold', normal: 'green' }
const levelLabels = { high: '高危', risk: '风险', attention: '关注', normal: '正常' }

const alerts = ref([
  { name: '李明辉', dept: '刑侦大队', level: 'high', score: 92, rule: '多维度叠加预警', summary: '执法办案扣分连续3月上升，财务报销异常，群众投诉2次', time: '14:32' },
  { name: '张伟强', dept: '治安大队', level: 'high', score: 88, rule: '单维度突破阈值', summary: '执法办案平台扣分事项达12分，超期案件3起', time: '14:15' },
  { name: '王建国', dept: '交警大队', level: 'risk', score: 78, rule: '趋势预警', summary: '风险评分连续3个月上升：52→65→78，需关注', time: '13:48' },
  { name: '刘志强', dept: '刑侦大队', level: 'risk', score: 82, rule: '关联预警', summary: '信访投诉3次，舆情监测发现负面信息2条', time: '13:20' },
  { name: '陈志远', dept: '治安大队', level: 'risk', score: 71, rule: '单维度突破阈值', summary: '车辆使用数据异常，非工作时间高频用车8次', time: '12:55' },
  { name: '赵德明', dept: '派出所', level: 'attention', score: 55, rule: '考勤异常检测', summary: '近30天迟到5次，早退3次，值班脱岗1次', time: '12:30' },
  { name: '周建华', dept: '交警大队', level: 'attention', score: 48, rule: '值班异常检测', summary: '值班备勤签到异常4次，夜间巡逻缺席2次', time: '11:45' },
  { name: '吴国栋', dept: '刑侦大队', level: 'risk', score: 75, rule: '趋势预警', summary: '群众评价满意度下降，投诉工单新增2条', time: '11:20' },
  { name: '孙海涛', dept: '派出所', level: 'attention', score: 52, rule: '培训考核异常', summary: '季度考核成绩下滑，培训出勤率不足80%', time: '10:55' },
  { name: '马永强', dept: '治安大队', level: 'high', score: 95, rule: '多维度叠加预警', summary: '执法、财务、信访三维度同时触发阈值，建议立即介入', time: '10:30' },
  { name: '杨建军', dept: '交警大队', level: 'attention', score: 45, rule: '考勤纪律检测', summary: '请休假手续不全2次，外出报备缺失1次', time: '10:05' },
  { name: '林志豪', dept: '派出所', level: 'risk', score: 73, rule: '财务异常检测', summary: '报销金额异常增长，单月报销超均值200%', time: '09:40' },
  { name: '黄明亮', dept: '刑侦大队', level: 'attention', score: 61, rule: '舆情监测预警', summary: '网络舆情发现相关负面评论1条，需核实', time: '09:15' },
  { name: '何建华', dept: '治安大队', level: 'normal', score: 35, rule: '常规关注', summary: '各项指标正常，近期表现稳定', time: '08:50' },
  { name: '郑伟东', dept: '交警大队', level: 'risk', score: 76, rule: '枪支管控异常', summary: '枪支领用记录与值班记录不匹配2次', time: '08:25' },
  { name: '谢志强', dept: '派出所', level: 'attention', score: 58, rule: '群众评价预警', summary: '窗口服务评价连续3次低于平均分', time: '08:00' },
  { name: '罗德明', dept: '刑侦大队', level: 'risk', score: 79, rule: '趋势预警', summary: '异常报销金额呈上升趋势：0.3→0.6→1.2万', time: '07:35' },
  { name: '韩永强', dept: '治安大队', level: 'attention', score: 42, rule: '出差异常检测', summary: '出差频次异常增加，本月已出差4次', time: '07:10' },
  { name: '唐建军', dept: '交警大队', level: 'normal', score: 28, rule: '常规关注', summary: '各项指标正常，执法办案无扣分', time: '06:45' },
  { name: '冯志豪', dept: '派出所', level: 'risk', score: 84, rule: '多维度叠加预警', summary: '执法扣分+财务异常+考勤异常，三维度同时触发', time: '06:20' },
  { name: '曹明亮', dept: '刑侦大队', level: 'attention', score: 50, rule: '信息化操异常', summary: '系统操作日志异常，非工作时间登录3次', time: '05:55' },
  { name: '邓海涛', dept: '治安大队', level: 'normal', score: 22, rule: '常规关注', summary: '表现优秀，无异常记录', time: '05:30' },
  { name: '许伟强', dept: '交警大队', level: 'high', score: 91, rule: '多维度叠加预警', summary: '执法+信访+舆情+财务四维度同时异常', time: '05:05' },
])

// ========== 数据源状态 ==========
const dataSources = ref([
  { name: '执法办案', icon: '📋', online: true, count: '45,231' },
  { name: '人事管理', icon: '👥', online: true, count: '892' },
  { name: '财务报销', icon: '💰', online: true, count: '12,345' },
  { name: '车辆使用', icon: '🚗', online: true, count: '8,567' },
  { name: '枪支管控', icon: '🔫', online: true, count: '2,341' },
  { name: '群众评价', icon: '💬', online: true, count: '15,678' },
  { name: '值班备勤', icon: '🕐', online: true, count: '6,789' },
  { name: '出差情况', icon: '✈️', online: true, count: '1,234' },
  { name: '重大事项', icon: '📄', online: true, count: '456' },
  { name: '培训考核', icon: '🎓', online: true, count: '3,456' },
  { name: '投诉举报', icon: '📢', online: true, count: '789' },
  { name: '考勤纪律', icon: '📈', online: true, count: '23,456' },
  { name: '场所管理', icon: '🏢', online: true, count: '567' },
  { name: '舆情监测', icon: '📰', online: true, count: '4,567' },
  { name: '信息化操', icon: '🔧', online: false, count: '8,901' },
  { name: '信访处理', icon: '📝', online: true, count: '2,345' },
  { name: '审计结果', icon: '⚖️', online: true, count: '678' },
])

const onlineCount = computed(() => dataSources.value.filter(s => s.online).length)

// ========== 图表 ==========
const pieChartRef = ref(null)
const lineChartRef = ref(null)
let pieChart = null
let lineChart = null

const initCharts = () => {
  // 饼图
  if (pieChartRef.value) {
    pieChart = echarts.init(pieChartRef.value)
    pieChart.setOption({
      tooltip: { trigger: 'item', formatter: '{b}: {c}人 ({d}%)' },
      legend: { bottom: 0, textStyle: { color: '#94a3b8', fontSize: 12 } },
      series: [{
        type: 'pie',
        radius: ['40%', '70%'],
        center: ['50%', '45%'],
        avoidLabelOverlap: true,
        itemStyle: { borderRadius: 8, borderColor: '#fff', borderWidth: 2 },
        label: { show: true, formatter: '{b}\n{c}人', fontSize: 12 },
        emphasis: { label: { fontSize: 14, fontWeight: 'bold' } },
        data: [
          { value: 3, name: '高危', itemStyle: { color: '#ef4444' } },
          { value: 12, name: '风险', itemStyle: { color: '#f59e0b' } },
          { value: 48, name: '关注', itemStyle: { color: '#8b5cf6' } },
          { value: 437, name: '正常', itemStyle: { color: '#10b981' } },
        ],
        animationType: 'scale',
        animationEasing: 'elasticOut',
        animationDelay: (idx) => idx * 200,
      }]
    })
  }

  // 折线图
  if (lineChartRef.value) {
    lineChart = echarts.init(lineChartRef.value)
    lineChart.setOption({
      tooltip: { trigger: 'axis' },
      legend: { data: ['高危', '风险', '关注', '预警总数'], bottom: 0, textStyle: { color: '#94a3b8', fontSize: 11 } },
      grid: { left: 50, right: 20, top: 20, bottom: 50 },
      xAxis: {
        type: 'category',
        data: ['2月', '3月', '4月', '5月', '6月', '7月'],
        axisLine: { lineStyle: { color: '#e2e8f0' } },
        axisLabel: { color: '#94a3b8' },
      },
      yAxis: {
        type: 'value',
        axisLine: { show: false },
        splitLine: { lineStyle: { color: '#f1f5f9', type: 'dashed' } },
        axisLabel: { color: '#94a3b8' },
      },
      series: [
        {
          name: '高危', type: 'line', data: [5, 4, 6, 3, 4, 3],
          lineStyle: { width: 3, color: '#ef4444' }, itemStyle: { color: '#ef4444' },
          symbol: 'circle', symbolSize: 8,
          areaStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(239,68,68,0.3)' }, { offset: 1, color: 'rgba(239,68,68,0)' }
          ])},
        },
        {
          name: '风险', type: 'line', data: [15, 18, 14, 16, 13, 12],
          lineStyle: { width: 3, color: '#f59e0b' }, itemStyle: { color: '#f59e0b' },
          symbol: 'circle', symbolSize: 8,
          areaStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(245,158,11,0.2)' }, { offset: 1, color: 'rgba(245,158,11,0)' }
          ])},
        },
        {
          name: '关注', type: 'line', data: [35, 42, 38, 45, 50, 48],
          lineStyle: { width: 3, color: '#8b5cf6' }, itemStyle: { color: '#8b5cf6' },
          symbol: 'circle', symbolSize: 8,
          areaStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(139,92,246,0.15)' }, { offset: 1, color: 'rgba(139,92,246,0)' }
          ])},
        },
        {
          name: '预警总数', type: 'line', data: [55, 64, 58, 64, 67, 63],
          lineStyle: { width: 2, color: '#3b82f6', type: 'dashed' }, itemStyle: { color: '#3b82f6' },
          symbol: 'diamond', symbolSize: 10,
        },
      ],
      animationDuration: 2000,
      animationEasing: 'cubicOut',
    })
  }
}

const handleResize = () => {
  pieChart?.resize()
  lineChart?.resize()
}

onMounted(() => {
  nextTick(initCharts)
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  pieChart?.dispose()
  lineChart?.dispose()
})
</script>

<style scoped>
.ai-center {
  max-width: 1400px;
  margin: 0 auto;
}

/* 统计行 */
.stat-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 20px;
}
.monitor-stat {
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
  border-top: 4px solid #3b82f6;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 14px;
  position: relative;
  transition: all 0.3s;
}
.monitor-stat:hover {
  transform: translateY(-3px);
  box-shadow: var(--shadow-lg);
}
.monitor-stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.monitor-stat-info { flex: 1; }
.monitor-stat-value {
  font-size: 32px;
  font-weight: 800;
  line-height: 1.1;
  font-family: var(--font-mono);
}
.monitor-stat-label {
  font-size: 13px;
  color: var(--text-muted);
  margin-top: 4px;
  font-weight: 500;
}
.monitor-stat-trend {
  position: absolute;
  top: 14px;
  right: 16px;
  font-size: 12px;
  font-weight: 600;
  padding: 2px 8px;
  border-radius: 10px;
}
.monitor-stat-trend.up { color: #ef4444; background: rgba(239,68,68,0.1); }
.monitor-stat-trend.down { color: #10b981; background: rgba(16,185,129,0.1); }

/* 主内容行 */
.main-row {
  display: grid;
  grid-template-columns: 1.5fr 1fr;
  gap: 20px;
  margin-bottom: 20px;
}

/* 面板通用 */
.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 14px;
}
.panel-title {
  font-size: 15px;
  font-weight: 700;
  color: var(--text-title);
  display: flex;
  align-items: center;
  gap: 8px;
}

/* 实时预警 */
.alert-panel {
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
  padding: 20px;
  max-height: 520px;
  display: flex;
  flex-direction: column;
}
.alert-list {
  flex: 1;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.alert-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 12px 14px;
  border-radius: 10px;
  background: var(--bg-page);
  border: 1px solid var(--border-light);
  transition: all 0.2s;
  position: relative;
}
.alert-item:hover {
  border-color: var(--accent);
  box-shadow: var(--shadow-sm);
}
.alert-item.level-high {
  border-left: 4px solid #ef4444;
  animation: pulseRed 2s ease-in-out infinite;
}
.alert-item.level-risk {
  border-left: 4px solid #f59e0b;
}
.alert-item.level-attention {
  border-left: 4px solid #8b5cf6;
}
.alert-item.level-normal {
  border-left: 4px solid #10b981;
}

@keyframes pulseRed {
  0%, 100% { box-shadow: 0 0 0 0 rgba(239,68,68,0); }
  50% { box-shadow: 0 0 12px 2px rgba(239,68,68,0.15); }
}

.alert-level-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  flex-shrink: 0;
  margin-top: 6px;
}
.dot-high { background: #ef4444; animation: blink 1s ease-in-out infinite; }
.dot-risk { background: #f59e0b; }
.dot-attention { background: #8b5cf6; }
.dot-normal { background: #10b981; }

@keyframes blink {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.3; }
}

.alert-body { flex: 1; min-width: 0; }
.alert-top { display: flex; align-items: center; gap: 8px; margin-bottom: 4px; }
.alert-name { font-weight: 700; color: var(--text-title); font-size: 14px; }
.alert-score { font-family: var(--font-mono); font-weight: 700; color: var(--text-title); font-size: 14px; margin-left: auto; }
.alert-rule { font-size: 12px; color: var(--accent); font-weight: 600; margin-bottom: 2px; }
.alert-summary { font-size: 12px; color: var(--text-muted); line-height: 1.5; }
.alert-time { font-size: 11px; color: var(--text-muted); flex-shrink: 0; font-family: var(--font-mono); }

/* 数据源状态 */
.source-panel {
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
  padding: 20px;
}
.source-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 8px;
}
.source-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 10px;
  border-radius: 8px;
  background: var(--bg-page);
  border: 1px solid var(--border-light);
  font-size: 12px;
  transition: all 0.2s;
}
.source-item:hover { border-color: var(--accent); }
.source-icon { font-size: 16px; flex-shrink: 0; }
.source-name { font-weight: 600; color: var(--text-primary); flex: 1; min-width: 0; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.source-status {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  flex-shrink: 0;
}
.source-status.online { background: #10b981; animation: breathe 2s ease-in-out infinite; }
.source-status.offline { background: #ef4444; }

@keyframes breathe {
  0%, 100% { opacity: 1; box-shadow: 0 0 0 0 rgba(16,185,129,0.4); }
  50% { opacity: 0.7; box-shadow: 0 0 6px 2px rgba(16,185,129,0.2); }
}

.source-count { font-family: var(--font-mono); font-size: 11px; color: var(--text-muted); font-weight: 600; flex-shrink: 0; }

/* 图表行 */
.chart-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
}
.chart-card {
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
  padding: 20px;
}
.chart-card h3 {
  font-size: 15px;
  font-weight: 700;
  color: var(--text-title);
  margin-bottom: 12px;
}

/* live dot */
.live-dot {
  display: inline-block;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #ef4444;
  animation: blink 1.5s ease-in-out infinite;
}
</style>
