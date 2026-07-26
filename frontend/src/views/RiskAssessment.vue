<template>
  <div class="risk-assessment">
    <div class="page-header">
      <h2>风险评估详情</h2>
      <p>AI 驱动的个人风险画像 · 17维数据综合评估</p>
    </div>

    <div class="assessment-layout">
      <!-- 左侧人员列表 -->
      <div class="person-list-panel">
        <div class="list-header">
          <a-input-search v-model:value="searchKey" placeholder="搜索人员..." size="small" />
          <a-select v-model:value="filterLevel" size="small" style="width: 100%; margin-top: 8px" placeholder="风险等级筛选">
            <a-select-option value="">全部等级</a-select-option>
            <a-select-option value="high">高危</a-select-option>
            <a-select-option value="risk">风险</a-select-option>
            <a-select-option value="attention">关注</a-select-option>
            <a-select-option value="normal">正常</a-select-option>
          </a-select>
        </div>
        <div class="person-list">
          <div v-for="(p, idx) in filteredPersons" :key="idx"
            class="person-item"
            :class="{ active: selectedIdx === idx, ['level-' + p.level]: true }"
            @click="selectPerson(idx)">
            <div class="person-avatar" :style="{ background: levelBg[p.level], color: levelColors[p.level] }">
              {{ p.name.charAt(0) }}
            </div>
            <div class="person-info">
              <div class="person-name">{{ p.name }}</div>
              <div class="person-dept">{{ p.dept }}</div>
            </div>
            <div class="person-score-box">
              <span class="person-score" :style="{ color: levelColors[p.level] }">{{ p.score }}</span>
              <a-tag :color="levelTagColors[p.level]" size="small">{{ levelLabels[p.level] }}</a-tag>
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧评估报告 -->
      <div class="report-panel" v-if="selectedPerson">
        <!-- 顶部：评分 + 雷达图 -->
        <div class="report-top">
          <div class="gauge-section">
            <div ref="gaugeRef" style="height: 260px;"></div>
            <div class="gauge-label">{{ selectedPerson.name }} · 风险评分</div>
          </div>
          <div class="radar-section">
            <div ref="radarRef" style="height: 260px;"></div>
          </div>
        </div>

        <!-- AI分析摘要 -->
        <div class="ai-summary-card">
          <div class="ai-summary-header">
            <span class="ai-icon">🤖</span>
            <span>AI 智能分析摘要</span>
            <a-tag color="blue" size="small">DeepSeek-7B</a-tag>
          </div>
          <div class="ai-summary-text">{{ typewriterText }}<span class="cursor" v-if="isTyping">|</span></div>
        </div>

        <!-- 处置建议 -->
        <div class="suggestions">
          <h3 class="section-title">处置建议</h3>
          <div class="suggestion-cards">
            <div v-for="(s, i) in selectedPerson.suggestions" :key="i" class="suggestion-card">
              <span class="suggestion-icon">{{ s.icon }}</span>
              <div class="suggestion-content">
                <div class="suggestion-title">{{ s.title }}</div>
                <div class="suggestion-desc">{{ s.desc }}</div>
              </div>
            </div>
          </div>
        </div>

        <!-- 评分趋势 + 预警时间线 -->
        <div class="bottom-row">
          <div class="trend-section">
            <h3 class="section-title">近6个月评分趋势</h3>
            <div ref="trendRef" style="height: 200px;"></div>
          </div>
          <div class="timeline-section">
            <h3 class="section-title">预警记录</h3>
            <div class="timeline">
              <div v-for="(e, i) in selectedPerson.events" :key="i" class="timeline-item" :class="'tl-' + e.level">
                <div class="tl-dot"></div>
                <div class="tl-content">
                  <div class="tl-date">{{ e.date }}</div>
                  <div class="tl-text">{{ e.text }}</div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted, onUnmounted, nextTick } from 'vue'
import * as echarts from 'echarts'

// ========== 人员数据 ==========
const persons = [
  {
    name: '马永强', dept: '治安大队', level: 'high', score: 95,
    summary: '该同志近期存在多维度异常。执法办案平台扣分事项累计达18分，超期案件5起，程序违规3次；财务报销近3个月金额异常增长（0.3万→0.8万→1.5万），存在虚报嫌疑；群众投诉3次，满意度评分低于60分；信访系统收到举报工单2条。综合评估为高危，建议立即介入调查。',
    suggestions: [
      { icon: '🚨', title: '立即介入', desc: '建议督察部门24小时内启动专项调查程序' },
      { icon: '📋', title: '财务审计', desc: '建议审计部门对近6个月报销凭证进行全面核查' },
      { icon: '💬', title: '约谈了解', desc: '建议分管领导约谈当事人，了解具体情况' },
    ],
    radarData: [25, 30, 40, 20, 35, 45, 50, 30, 25, 40, 35, 20, 45, 30, 25, 40, 35],
    trendData: [65, 72, 78, 82, 88, 95],
    events: [
      { date: '2026-07-22', text: '多维度叠加预警触发，评分95', level: 'high' },
      { date: '2026-07-15', text: '财务异常检测触发，金额超阈值', level: 'risk' },
      { date: '2026-06-28', text: '群众投诉新增2条', level: 'attention' },
      { date: '2026-06-10', text: '执法办案扣分事项突破阈值', level: 'risk' },
      { date: '2026-05-20', text: '趋势预警触发，评分连续上升', level: 'attention' },
    ],
  },
  {
    name: '李明辉', dept: '刑侦大队', level: 'high', score: 92,
    summary: '该同志存在执法和财务双维度异常。执法办案扣分12分，超期案件3起；群众投诉2次，涉及执法态度问题。财务报销近期有异常波动，需进一步核实。综合评估为高危。',
    suggestions: [
      { icon: '🚨', title: '专项调查', desc: '建议启动执法规范化专项检查' },
      { icon: '📝', title: '谈话提醒', desc: '建议进行执法规范谈话提醒' },
    ],
    radarData: [30, 35, 45, 25, 40, 50, 55, 35, 30, 45, 40, 25, 50, 35, 30, 45, 40],
    trendData: [58, 65, 70, 75, 85, 92],
    events: [
      { date: '2026-07-20', text: '高危预警触发，评分92', level: 'high' },
      { date: '2026-07-05', text: '群众投诉新增', level: 'attention' },
      { date: '2026-06-15', text: '执法扣分突破阈值', level: 'risk' },
    ],
  },
  {
    name: '王建国', dept: '交警大队', level: 'risk', score: 78,
    summary: '该同志风险评分呈持续上升趋势（52→65→78），主要异常集中在考勤纪律和值班备勤方面。近30天迟到5次，早退3次，值班脱岗1次。建议加强关注。',
    suggestions: [
      { icon: '⏰', title: '考勤整改', desc: '建议下发考勤纪律整改通知' },
      { icon: '👀', title: '加强观察', desc: '建议季度复评，持续跟踪' },
    ],
    radarData: [55, 60, 50, 65, 55, 70, 60, 55, 60, 50, 55, 65, 60, 55, 60, 50, 55],
    trendData: [52, 55, 58, 62, 65, 78],
    events: [
      { date: '2026-07-18', text: '趋势预警触发，评分连续上升', level: 'risk' },
      { date: '2026-06-20', text: '考勤异常检测触发', level: 'attention' },
    ],
  },
  {
    name: '赵德明', dept: '派出所', level: 'attention', score: 55,
    summary: '该同志近期考勤纪律有所松懈，近30天迟到5次、早退3次、值班脱岗1次。其他维度指标正常，整体处于关注级别。建议进行提醒谈话。',
    suggestions: [
      { icon: '💬', title: '提醒谈话', desc: '建议进行考勤纪律提醒谈话' },
    ],
    radarData: [70, 75, 72, 68, 70, 65, 55, 70, 72, 68, 70, 75, 72, 68, 70, 65, 55],
    trendData: [35, 38, 42, 48, 52, 55],
    events: [
      { date: '2026-07-10', text: '考勤异常检测触发', level: 'attention' },
    ],
  },
  {
    name: '陈志远', dept: '治安大队', level: 'risk', score: 71,
    summary: '该同志车辆使用数据异常，非工作时间高频用车8次，存在公车私用嫌疑。建议调取车辆GPS轨迹进行核实。',
    suggestions: [
      { icon: '🚗', title: '轨迹核查', desc: '建议调取近3个月车辆GPS轨迹记录' },
      { icon: '📋', title: '情况说明', desc: '要求当事人提交用车情况书面说明' },
    ],
    radarData: [60, 65, 55, 70, 60, 45, 50, 60, 65, 55, 70, 60, 45, 50, 60, 65, 55],
    trendData: [45, 50, 55, 60, 65, 71],
    events: [
      { date: '2026-07-15', text: '车辆使用异常检测触发', level: 'risk' },
      { date: '2026-06-25', text: '非工作时间用车记录增加', level: 'attention' },
    ],
  },
  {
    name: '刘某', dept: '刑侦大队', level: 'risk', score: 82,
    summary: '该同志信访投诉3次，舆情监测发现负面信息2条。投诉主要涉及办案态度和程序问题。建议核实投诉内容。',
    suggestions: [
      { icon: '📢', title: '投诉核实', desc: '建议逐一核实3条投诉内容' },
      { icon: '🔍', title: '舆情监控', desc: '建议持续关注相关舆情动态' },
    ],
    radarData: [50, 55, 45, 60, 50, 40, 45, 50, 55, 45, 60, 50, 40, 45, 50, 55, 45],
    trendData: [55, 60, 65, 70, 75, 82],
    events: [
      { date: '2026-07-12', text: '信访投诉+舆情叠加预警', level: 'risk' },
      { date: '2026-06-30', text: '舆情监测发现负面信息', level: 'attention' },
    ],
  },
  {
    name: '杨建', dept: '交警大队', level: 'attention', score: 48,
    summary: '该同志值班备勤签到异常4次，夜间巡逻缺席2次。其他指标正常，处于关注级别。',
    suggestions: [
      { icon: '🕐', title: '值班整改', desc: '建议加强值班纪律要求' },
    ],
    radarData: [72, 75, 70, 73, 72, 58, 50, 72, 75, 70, 73, 72, 58, 50, 72, 75, 70],
    trendData: [30, 33, 38, 42, 45, 48],
    events: [
      { date: '2026-07-08', text: '值班异常检测触发', level: 'attention' },
    ],
  },
  {
    name: '周建华', dept: '交警大队', level: 'normal', score: 25,
    summary: '该同志各项指标正常，执法办案无扣分，财务报销无异常，群众评价良好。近期表现稳定。',
    suggestions: [
      { icon: '✅', title: '持续关注', desc: '保持常规监控频率即可' },
    ],
    radarData: [85, 88, 82, 86, 85, 90, 88, 85, 88, 82, 86, 85, 90, 88, 85, 88, 82],
    trendData: [22, 25, 23, 24, 22, 25],
    events: [
      { date: '2026-07-01', text: '常规评估，各项正常', level: 'normal' },
    ],
  },
]

const levelColors = { high: '#ef4444', risk: '#f59e0b', attention: '#8b5cf6', normal: '#10b981' }
const levelBg = { high: 'rgba(239,68,68,0.1)', risk: 'rgba(245,158,11,0.1)', attention: 'rgba(139,92,246,0.1)', normal: 'rgba(16,185,129,0.1)' }
const levelTagColors = { high: 'red', risk: 'orange', attention: 'purple', normal: 'green' }
const levelLabels = { high: '高危', risk: '风险', attention: '关注', normal: '正常' }

// ========== 筛选 ==========
const searchKey = ref('')
const filterLevel = ref('')
const filteredPersons = computed(() => {
  return persons.filter(p => {
    if (searchKey.value && !p.name.includes(searchKey.value) && !p.dept.includes(searchKey.value)) return false
    if (filterLevel.value && p.level !== filterLevel.value) return false
    return true
  })
})

// ========== 选中 ==========
const selectedIdx = ref(0)
const selectedPerson = computed(() => filteredPersons.value[selectedIdx.value] || null)

const selectPerson = (idx) => {
  selectedIdx.value = idx
  isTyping.value = false
  typewriterText.value = ''
  nextTick(() => {
    initReportCharts()
    startTypewriter()
  })
}

// ========== 打字机 ==========
const typewriterText = ref('')
const isTyping = ref(false)
let typewriterTimer = null

const startTypewriter = () => {
  if (!selectedPerson.value) return
  const text = selectedPerson.value.summary
  let i = 0
  isTyping.value = true
  typewriterText.value = ''
  if (typewriterTimer) clearInterval(typewriterTimer)
  typewriterTimer = setInterval(() => {
    if (i < text.length) {
      typewriterText.value += text[i]
      i++
    } else {
      clearInterval(typewriterTimer)
      isTyping.value = false
    }
  }, 30)
}

// ========== 图表 ==========
const gaugeRef = ref(null)
const radarRef = ref(null)
const trendRef = ref(null)
let charts = []

const initReportCharts = () => {
  charts.forEach(c => c.dispose())
  charts = []

  const p = selectedPerson.value
  if (!p) return

  // 仪表盘
  if (gaugeRef.value) {
    const c = echarts.init(gaugeRef.value)
    charts.push(c)
    c.setOption({
      series: [{
        type: 'gauge',
        startAngle: 220,
        endAngle: -40,
        min: 0, max: 100,
        radius: '85%',
        center: ['50%', '55%'],
        axisLine: {
          lineStyle: {
            width: 20,
            color: [
              [0.4, '#10b981'],
              [0.7, '#f59e0b'],
              [0.9, '#ef4444'],
              [1, '#dc2626']
            ],
            roundCap: true,
          }
        },
        pointer: {
          icon: 'path://M12.8,0.7l12,40.1H0.7L12.8,0.7z',
          length: '60%', width: 10,
          offsetCenter: [0, '-10%'],
          itemStyle: { color: 'auto' }
        },
        axisTick: { distance: -20, length: 6, lineStyle: { color: '#fff', width: 1 } },
        splitLine: { distance: -24, length: 16, lineStyle: { color: '#fff', width: 2 } },
        axisLabel: { color: '#94a3b8', distance: 30, fontSize: 11 },
        detail: {
          valueAnimation: true,
          formatter: '{value}',
          color: levelColors[p.level],
          fontSize: 42,
          fontWeight: 900,
          fontFamily: 'var(--font-mono)',
          offsetCenter: [0, '35%'],
        },
        title: { show: false },
        data: [{ value: p.score }],
        animationDuration: 2000,
        animationEasing: 'bounceOut',
      }]
    })
  }

  // 雷达图
  if (radarRef.value) {
    const c = echarts.init(radarRef.value)
    charts.push(c)
    const dims = ['执法办案', '人事管理', '财务报销', '车辆使用', '枪支管控', '群众评价', '值班备勤', '出差情况', '重大事项', '培训考核', '投诉举报', '考勤纪律', '场所管理', '舆情监测', '信息化操', '信访处理', '审计结果']
    c.setOption({
      tooltip: {},
      radar: {
        indicator: dims.map(d => ({ name: d, max: 100 })),
        radius: '60%',
        center: ['50%', '52%'],
        axisName: { color: '#94a3b8', fontSize: 9 },
        splitArea: { areaStyle: { color: ['rgba(59,130,246,0.02)', 'rgba(59,130,246,0.04)'] } },
        splitNumber: 4,
      },
      series: [{
        type: 'radar',
        data: [{
          value: p.radarData,
          name: p.name,
          lineStyle: { color: levelColors[p.level], width: 2 },
          itemStyle: { color: levelColors[p.level] },
          areaStyle: { color: levelBg[p.level] },
        }],
        animationDuration: 1500,
      }],
    })
  }

  // 趋势图
  if (trendRef.value) {
    const c = echarts.init(trendRef.value)
    charts.push(c)
    c.setOption({
      tooltip: { trigger: 'axis' },
      grid: { left: 40, right: 10, top: 10, bottom: 30 },
      xAxis: {
        type: 'category', data: ['2月', '3月', '4月', '5月', '6月', '7月'],
        axisLine: { lineStyle: { color: '#e2e8f0' } }, axisLabel: { color: '#94a3b8', fontSize: 11 },
      },
      yAxis: {
        type: 'value', min: 0, max: 100,
        axisLine: { show: false }, splitLine: { lineStyle: { color: '#f1f5f9', type: 'dashed' } },
        axisLabel: { color: '#94a3b8', fontSize: 11 },
      },
      series: [{
        type: 'line', data: p.trendData,
        lineStyle: { width: 3, color: levelColors[p.level] },
        itemStyle: { color: levelColors[p.level] },
        symbol: 'circle', symbolSize: 8,
        areaStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: levelBg[p.level] }, { offset: 1, color: 'transparent' }
        ])},
        markLine: {
          silent: true,
          data: [
            { yAxis: 40, lineStyle: { color: '#f59e0b', type: 'dashed' }, label: { formatter: '关注线', color: '#f59e0b', fontSize: 10 } },
            { yAxis: 70, lineStyle: { color: '#ef4444', type: 'dashed' }, label: { formatter: '风险线', color: '#ef4444', fontSize: 10 } },
            { yAxis: 90, lineStyle: { color: '#dc2626', type: 'dashed' }, label: { formatter: '高危线', color: '#dc2626', fontSize: 10 } },
          ]
        },
      }],
      animationDuration: 1500,
    })
  }
}

const handleResize = () => charts.forEach(c => c.resize())

onMounted(() => {
  nextTick(() => {
    initReportCharts()
    startTypewriter()
  })
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  charts.forEach(c => c.dispose())
  if (typewriterTimer) clearInterval(typewriterTimer)
})
</script>

<style scoped>
.risk-assessment { max-width: 1400px; margin: 0 auto; }

.assessment-layout {
  display: grid;
  grid-template-columns: 300px 1fr;
  gap: 20px;
  min-height: calc(100vh - 180px);
}

/* 左侧人员列表 */
.person-list-panel {
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
  padding: 16px;
  display: flex;
  flex-direction: column;
}
.list-header { margin-bottom: 12px; }
.person-list { flex: 1; overflow-y: auto; display: flex; flex-direction: column; gap: 6px; }
.person-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.2s;
  border: 1px solid transparent;
}
.person-item:hover { background: var(--bg-page); }
.person-item.active { background: var(--accent-light); border-color: var(--accent); }
.person-item.level-high { border-left: 3px solid #ef4444; }
.person-item.level-risk { border-left: 3px solid #f59e0b; }
.person-item.level-attention { border-left: 3px solid #8b5cf6; }
.person-item.level-normal { border-left: 3px solid #10b981; }

.person-avatar {
  width: 36px; height: 36px; border-radius: 10px;
  display: flex; align-items: center; justify-content: center;
  font-weight: 800; font-size: 15px; flex-shrink: 0;
}
.person-info { flex: 1; min-width: 0; }
.person-name { font-weight: 700; font-size: 14px; color: var(--text-title); }
.person-dept { font-size: 11px; color: var(--text-muted); }
.person-score-box { text-align: right; flex-shrink: 0; }
.person-score { font-family: var(--font-mono); font-size: 20px; font-weight: 800; display: block; }

/* 右侧报告 */
.report-panel {
  display: flex;
  flex-direction: column;
  gap: 20px;
}
.report-top {
  display: grid;
  grid-template-columns: 1fr 1.5fr;
  gap: 20px;
}
.gauge-section, .radar-section {
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
  padding: 16px;
}
.gauge-label { text-align: center; font-weight: 700; color: var(--text-title); margin-top: -12px; }

/* AI摘要 */
.ai-summary-card {
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
  padding: 18px;
  border-left: 4px solid var(--accent);
}
.ai-summary-header {
  display: flex; align-items: center; gap: 8px;
  font-weight: 700; color: var(--text-title); margin-bottom: 10px; font-size: 14px;
}
.ai-icon { font-size: 18px; }
.ai-summary-text { font-size: 14px; color: var(--text-secondary); line-height: 1.8; min-height: 60px; }
.cursor { animation: blink 0.8s infinite; color: var(--accent); }
@keyframes blink { 0%, 100% { opacity: 1; } 50% { opacity: 0; } }

/* 建议 */
.section-title {
  font-size: 14px; font-weight: 700; color: var(--text-title); margin-bottom: 12px;
  display: flex; align-items: center; gap: 8px;
}
.section-title::before { content: ''; width: 3px; height: 14px; background: var(--accent); border-radius: 2px; }
.suggestion-cards { display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 12px; }
.suggestion-card {
  background: var(--bg-card); border: 1px solid var(--border-color);
  border-radius: var(--radius-md); padding: 14px;
  display: flex; gap: 10px; align-items: flex-start;
}
.suggestion-icon { font-size: 20px; flex-shrink: 0; }
.suggestion-title { font-weight: 700; font-size: 13px; color: var(--text-title); margin-bottom: 2px; }
.suggestion-desc { font-size: 12px; color: var(--text-muted); line-height: 1.5; }

/* 底部 */
.bottom-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
}
.trend-section, .timeline-section {
  background: var(--bg-card); border: 1px solid var(--border-color);
  border-radius: var(--radius-lg); padding: 16px;
}

/* 时间线 */
.timeline { display: flex; flex-direction: column; gap: 12px; }
.timeline-item { display: flex; gap: 10px; align-items: flex-start; }
.tl-dot {
  width: 10px; height: 10px; border-radius: 50%; flex-shrink: 0; margin-top: 4px;
}
.tl-high .tl-dot { background: #ef4444; }
.tl-risk .tl-dot { background: #f59e0b; }
.tl-attention .tl-dot { background: #8b5cf6; }
.tl-normal .tl-dot { background: #10b981; }
.tl-content { flex: 1; }
.tl-date { font-size: 11px; color: var(--text-muted); font-family: var(--font-mono); }
.tl-text { font-size: 13px; color: var(--text-primary); }
</style>
