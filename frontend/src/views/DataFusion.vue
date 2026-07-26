<template>
  <div class="data-fusion">
    <div class="page-header">
      <h2>数据融合中心</h2>
      <p>17维数据源全量接入 · 实时数据治理 · 质量监控</p>
    </div>

    <!-- 17维数据源卡片 -->
    <div class="source-section">
      <h3 class="section-title">数据源接入状态</h3>
      <div class="source-cards">
        <div v-for="(src, idx) in dataSources" :key="idx" class="fusion-source-card" :class="{ offline: !src.online }">
          <div class="fusion-source-header">
            <span class="fusion-source-icon">{{ src.icon }}</span>
            <span class="fusion-source-status" :class="src.online ? 'online' : 'offline'">
              {{ src.online ? '在线' : '离线' }}
            </span>
          </div>
          <div class="fusion-source-name">{{ src.name }}</div>
          <div class="fusion-source-count">{{ src.count }} 条</div>
          <div class="fusion-source-time">更新: {{ src.updated }}</div>
        </div>
      </div>
    </div>

    <!-- 桑基图 -->
    <div class="sankey-section">
      <h3 class="section-title">数据流转全景</h3>
      <div class="sankey-card">
        <div ref="sankeyRef" style="height: 400px;"></div>
      </div>
    </div>

    <!-- 数据质量仪表盘 -->
    <div class="quality-section">
      <h3 class="section-title">数据质量监控</h3>
      <div class="quality-cards">
        <div class="quality-card">
          <div ref="gauge1Ref" style="height: 220px;"></div>
          <div class="quality-label">数据完整性</div>
          <div class="quality-desc">已接入数据源 / 应接入数据源</div>
        </div>
        <div class="quality-card">
          <div ref="gauge2Ref" style="height: 220px;"></div>
          <div class="quality-label">数据准确性</div>
          <div class="quality-desc">通过校验的数据记录比例</div>
        </div>
        <div class="quality-card">
          <div ref="gauge3Ref" style="height: 220px;"></div>
          <div class="quality-label">数据时效性</div>
          <div class="quality-desc">在规定时间内更新的数据源比例</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import * as echarts from 'echarts'

// ========== 数据源 ==========
const dataSources = ref([
  { name: '执法办案', icon: '📋', online: true, count: '45,231', updated: '2分钟前' },
  { name: '人事管理', icon: '👥', online: true, count: '892', updated: '15分钟前' },
  { name: '财务报销', icon: '💰', online: true, count: '12,345', updated: '5分钟前' },
  { name: '车辆使用', icon: '🚗', online: true, count: '8,567', updated: '1分钟前' },
  { name: '枪支管控', icon: '🔫', online: true, count: '2,341', updated: '实时' },
  { name: '群众评价', icon: '💬', online: true, count: '15,678', updated: '10分钟前' },
  { name: '值班备勤', icon: '🕐', online: true, count: '6,789', updated: '3分钟前' },
  { name: '出差情况', icon: '✈️', online: true, count: '1,234', updated: '1小时前' },
  { name: '重大事项', icon: '📄', online: true, count: '456', updated: '30分钟前' },
  { name: '培训考核', icon: '🎓', online: true, count: '3,456', updated: '2小时前' },
  { name: '投诉举报', icon: '📢', online: true, count: '789', updated: '8分钟前' },
  { name: '考勤纪律', icon: '📈', online: true, count: '23,456', updated: '1分钟前' },
  { name: '场所管理', icon: '🏢', online: true, count: '567', updated: '20分钟前' },
  { name: '舆情监测', icon: '📰', online: true, count: '4,567', updated: '实时' },
  { name: '信息化操', icon: '🔧', online: false, count: '8,901', updated: '2小时前' },
  { name: '信访处理', icon: '📝', online: true, count: '2,345', updated: '12分钟前' },
  { name: '审计结果', icon: '⚖️', online: true, count: '678', updated: '1天前' },
])

// ========== 图表 ==========
const sankeyRef = ref(null)
const gauge1Ref = ref(null)
const gauge2Ref = ref(null)
const gauge3Ref = ref(null)
let charts = []

const initCharts = () => {
  // 桑基图
  if (sankeyRef.value) {
    const sankey = echarts.init(sankeyRef.value)
    charts.push(sankey)
    sankey.setOption({
      tooltip: { trigger: 'item', triggerOn: 'mousemove' },
      series: [{
        type: 'sankey',
        layout: 'none',
        emphasis: { focus: 'adjacency' },
        nodeAlign: 'left',
        layoutIterations: 32,
        nodeGap: 12,
        nodeWidth: 20,
        lineStyle: { color: 'gradient', curveness: 0.5 },
        label: { color: '#475569', fontSize: 11 },
        data: [
          // 数据源节点
          { name: '执法办案', itemStyle: { color: '#3b82f6' } },
          { name: '人事管理', itemStyle: { color: '#3b82f6' } },
          { name: '财务报销', itemStyle: { color: '#3b82f6' } },
          { name: '车辆使用', itemStyle: { color: '#3b82f6' } },
          { name: '枪支管控', itemStyle: { color: '#3b82f6' } },
          { name: '群众评价', itemStyle: { color: '#3b82f6' } },
          { name: '值班备勤', itemStyle: { color: '#3b82f6' } },
          { name: '信访处理', itemStyle: { color: '#3b82f6' } },
          { name: '审计结果', itemStyle: { color: '#3b82f6' } },
          // 治理层节点
          { name: '数据清洗', itemStyle: { color: '#f59e0b' } },
          { name: '标准化映射', itemStyle: { color: '#f59e0b' } },
          { name: '关联建模', itemStyle: { color: '#f59e0b' } },
          { name: '特征提取', itemStyle: { color: '#f59e0b' } },
          // AI引擎节点
          { name: '向量化引擎', itemStyle: { color: '#8b5cf6' } },
          { name: 'DeepSeek-7B', itemStyle: { color: '#ef4444' } },
          { name: '风险评估', itemStyle: { color: '#10b981' } },
        ],
        links: [
          // 数据源 → 数据清洗
          { source: '执法办案', target: '数据清洗', value: 45 },
          { source: '人事管理', target: '数据清洗', value: 8 },
          { source: '财务报销', target: '数据清洗', value: 12 },
          { source: '车辆使用', target: '数据清洗', value: 8 },
          { source: '枪支管控', target: '数据清洗', value: 2 },
          { source: '群众评价', target: '数据清洗', value: 15 },
          { source: '值班备勤', target: '数据清洗', value: 6 },
          { source: '信访处理', target: '数据清洗', value: 2 },
          { source: '审计结果', target: '数据清洗', value: 1 },
          // 数据清洗 → 标准化映射
          { source: '数据清洗', target: '标准化映射', value: 99 },
          // 标准化映射 → 关联建模
          { source: '标准化映射', target: '关联建模', value: 95 },
          // 关联建模 → 特征提取
          { source: '关联建模', target: '特征提取', value: 90 },
          // 特征提取 → 向量化引擎
          { source: '特征提取', target: '向量化引擎', value: 85 },
          // 向量化引擎 → DeepSeek-7B
          { source: '向量化引擎', target: 'DeepSeek-7B', value: 85 },
          // DeepSeek-7B → 风险评估
          { source: 'DeepSeek-7B', target: '风险评估', value: 85 },
        ],
        animationDuration: 2000,
        animationEasing: 'cubicOut',
      }]
    })
  }

  // Gauge图表
  const gaugeData = [
    { ref: gauge1Ref, value: 98.5, color: '#10b981', title: '完整性' },
    { ref: gauge2Ref, value: 99.2, color: '#3b82f6', title: '准确性' },
    { ref: gauge3Ref, value: 97.8, color: '#8b5cf6', title: '时效性' },
  ]

  gaugeData.forEach(({ ref: el, value, color }) => {
    if (el.value) {
      const chart = echarts.init(el.value)
      charts.push(chart)
      chart.setOption({
        series: [{
          type: 'gauge',
          startAngle: 200,
          endAngle: -20,
          min: 0,
          max: 100,
          splitNumber: 10,
          radius: '85%',
          center: ['50%', '55%'],
          axisLine: {
            lineStyle: {
              width: 18,
              color: [
                [value / 100, color],
                [1, '#e2e8f0']
              ],
              roundCap: true,
            }
          },
          pointer: {
            icon: 'path://M12.8,0.7l12,40.1H0.7L12.8,0.7z',
            length: '55%',
            width: 8,
            offsetCenter: [0, '-10%'],
            itemStyle: { color: 'auto' }
          },
          axisTick: { distance: -18, length: 6, lineStyle: { color: '#fff', width: 1 } },
          splitLine: { distance: -22, length: 14, lineStyle: { color: '#fff', width: 2 } },
          axisLabel: { color: '#94a3b8', distance: 28, fontSize: 11 },
          detail: {
            valueAnimation: true,
            formatter: '{value}%',
            color: color,
            fontSize: 28,
            fontWeight: 800,
            fontFamily: 'var(--font-mono)',
            offsetCenter: [0, '35%'],
          },
          data: [{ value }],
          animationDuration: 2000,
          animationEasing: 'bounceOut',
        }]
      })
    }
  })
}

const handleResize = () => charts.forEach(c => c.resize())

onMounted(() => {
  nextTick(initCharts)
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  charts.forEach(c => c.dispose())
})
</script>

<style scoped>
.data-fusion { max-width: 1400px; margin: 0 auto; }

.section-title {
  font-size: 16px;
  font-weight: 700;
  color: var(--text-title);
  margin-bottom: 16px;
  display: flex;
  align-items: center;
  gap: 8px;
}
.section-title::before {
  content: '';
  width: 4px;
  height: 18px;
  background: var(--accent);
  border-radius: 2px;
}

/* 数据源卡片 */
.source-section { margin-bottom: 24px; }
.source-cards {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 12px;
}
.fusion-source-card {
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  padding: 14px;
  transition: all 0.25s;
}
.fusion-source-card:hover {
  border-color: var(--accent);
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
}
.fusion-source-card.offline { opacity: 0.6; }
.fusion-source-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}
.fusion-source-icon { font-size: 20px; }
.fusion-source-status {
  font-size: 11px;
  font-weight: 600;
  padding: 2px 8px;
  border-radius: 10px;
}
.fusion-source-status.online { color: #10b981; background: rgba(16,185,129,0.1); }
.fusion-source-status.offline { color: #ef4444; background: rgba(239,68,68,0.1); }
.fusion-source-name { font-weight: 700; color: var(--text-title); font-size: 14px; margin-bottom: 4px; }
.fusion-source-count { font-family: var(--font-mono); font-size: 18px; font-weight: 800; color: var(--accent); margin-bottom: 4px; }
.fusion-source-time { font-size: 11px; color: var(--text-muted); }

/* 桑基图 */
.sankey-section { margin-bottom: 24px; }
.sankey-card {
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
  padding: 20px;
}

/* 质量仪表盘 */
.quality-cards {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
}
.quality-card {
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
  padding: 16px;
  text-align: center;
}
.quality-label {
  font-size: 15px;
  font-weight: 700;
  color: var(--text-title);
  margin-top: -8px;
}
.quality-desc {
  font-size: 12px;
  color: var(--text-muted);
  margin-top: 4px;
}
</style>
