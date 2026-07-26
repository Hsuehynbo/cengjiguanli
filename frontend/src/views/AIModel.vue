<template>
  <div class="ai-model">
    <div class="page-header">
      <h2>AI 模型训练性能</h2>
      <p>DeepSeek-7B + LoRA 微调 · 训练效果全量评估</p>
    </div>

    <!-- 模型概要卡片 -->
    <div class="model-summary">
      <div class="summary-item" v-for="(item, i) in summaryItems" :key="i">
        <div class="summary-label">{{ item.label }}</div>
        <div class="summary-value" :style="{ color: item.color }">{{ item.value }}</div>
      </div>
    </div>

    <!-- 图表区 -->
    <div class="chart-grid">
      <!-- Loss + Accuracy 曲线 -->
      <div class="chart-card">
        <h3>Loss & Accuracy 训练曲线</h3>
        <div ref="lossAccRef" style="height: 320px;"></div>
      </div>
      <!-- 雷达图 -->
      <div class="chart-card">
        <h3>模型能力雷达图</h3>
        <div ref="radarRef" style="height: 320px;"></div>
      </div>
    </div>

    <!-- 微调前后对比 -->
    <div class="comparison-section">
      <h3 class="section-title">微调前后指标对比</h3>
      <div class="comparison-grid">
        <div class="comparison-item" v-for="(m, i) in metrics" :key="i">
          <div class="metric-header">
            <span class="metric-name">{{ m.name }}</span>
            <span class="metric-change" :class="m.improved ? 'improved' : 'degraded'">
              {{ m.improved ? '↑' : '↓' }} {{ m.change }}
            </span>
          </div>
          <div class="metric-bars">
            <div class="bar-row">
              <span class="bar-label">微调前</span>
              <div class="bar-track">
                <div class="bar-fill before" :style="{ width: m.beforePct + '%' }"></div>
              </div>
              <span class="bar-value">{{ m.before }}</span>
            </div>
            <div class="bar-row">
              <span class="bar-label">微调后</span>
              <div class="bar-track">
                <div class="bar-fill after" :style="{ width: m.afterPct + '%' }"></div>
              </div>
              <span class="bar-value after-val">{{ m.after }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 底部曲线 -->
    <div class="bottom-charts">
      <div class="chart-card">
        <h3>Perplexity 变化曲线</h3>
        <div ref="pplRef" style="height: 260px;"></div>
      </div>
      <div class="chart-card">
        <h3>Validation Loss 收敛曲线</h3>
        <div ref="valLossRef" style="height: 260px;"></div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import * as echarts from 'echarts'

// ========== 概要 ==========
const summaryItems = [
  { label: '基座模型', value: 'DeepSeek-7B', color: '#3b82f6' },
  { label: '微调方法', value: 'LoRA (Rank=32)', color: '#8b5cf6' },
  { label: '训练数据', value: '2,000 条', color: '#f59e0b' },
  { label: '训练硬件', value: 'A100 80GB', color: '#10b981' },
  { label: '训练时间', value: '~3 小时', color: '#ef4444' },
  { label: '训练轮次', value: '5 Epochs', color: '#06b6d4' },
]

// ========== 对比指标 ==========
const metrics = [
  { name: 'Accuracy', before: '55.0%', after: '95.2%', beforePct: 55, afterPct: 95.2, change: '+40.2%', improved: true },
  { name: 'Precision', before: '52.3%', after: '95.5%', beforePct: 52.3, afterPct: 95.5, change: '+43.2%', improved: true },
  { name: 'Recall', before: '48.7%', after: '94.8%', beforePct: 48.7, afterPct: 94.8, change: '+46.1%', improved: true },
  { name: 'F1-Score', before: '50.4%', after: '95.1%', beforePct: 50.4, afterPct: 95.1, change: '+44.7%', improved: true },
  { name: 'Perplexity', before: '18.6', after: '3.2', beforePct: 93, afterPct: 16, change: '-82.8%', improved: true },
  { name: 'Val Loss', before: '1.82', after: '0.24', beforePct: 91, afterPct: 12, change: '-86.8%', improved: true },
]

// ========== 图表 ==========
const lossAccRef = ref(null)
const radarRef = ref(null)
const pplRef = ref(null)
const valLossRef = ref(null)
let charts = []

const initCharts = () => {
  // Loss + Accuracy
  if (lossAccRef.value) {
    const c = echarts.init(lossAccRef.value)
    charts.push(c)
    c.setOption({
      tooltip: { trigger: 'axis' },
      legend: { data: ['Training Loss', 'Validation Loss', 'Accuracy'], bottom: 0, textStyle: { color: '#94a3b8' } },
      grid: { left: 60, right: 60, top: 20, bottom: 50 },
      xAxis: {
        type: 'category', data: ['Epoch 0', 'Epoch 1', 'Epoch 2', 'Epoch 3', 'Epoch 4', 'Epoch 5'],
        axisLine: { lineStyle: { color: '#e2e8f0' } }, axisLabel: { color: '#94a3b8' },
      },
      yAxis: [
        { type: 'value', name: 'Loss', min: 0, max: 2, axisLine: { show: false }, splitLine: { lineStyle: { color: '#f1f5f9', type: 'dashed' } }, axisLabel: { color: '#94a3b8' } },
        { type: 'value', name: 'Accuracy', min: 0, max: 1, axisLine: { show: false }, splitLine: { show: false }, axisLabel: { color: '#94a3b8', formatter: v => (v * 100) + '%' } },
      ],
      series: [
        {
          name: 'Training Loss', type: 'line', data: [1.80, 0.95, 0.55, 0.35, 0.26, 0.22],
          lineStyle: { width: 3, color: '#3b82f6' }, itemStyle: { color: '#3b82f6' },
          symbol: 'circle', symbolSize: 8,
          areaStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(59,130,246,0.2)' }, { offset: 1, color: 'rgba(59,130,246,0)' }
          ])},
        },
        {
          name: 'Validation Loss', type: 'line', data: [1.82, 0.98, 0.58, 0.38, 0.28, 0.24],
          lineStyle: { width: 3, color: '#f59e0b', type: 'dashed' }, itemStyle: { color: '#f59e0b' },
          symbol: 'diamond', symbolSize: 8,
        },
        {
          name: 'Accuracy', type: 'line', yAxisIndex: 1, data: [0.55, 0.72, 0.83, 0.90, 0.94, 0.952],
          lineStyle: { width: 3, color: '#10b981' }, itemStyle: { color: '#10b981' },
          symbol: 'circle', symbolSize: 8,
          areaStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(16,185,129,0.15)' }, { offset: 1, color: 'rgba(16,185,129,0)' }
          ])},
        },
      ],
      animationDuration: 2000,
    })
  }

  // 雷达图
  if (radarRef.value) {
    const c = echarts.init(radarRef.value)
    charts.push(c)
    c.setOption({
      tooltip: {},
      legend: { data: ['微调前', '微调后'], bottom: 0, textStyle: { color: '#94a3b8' } },
      radar: {
        indicator: [
          { name: 'Accuracy', max: 100 },
          { name: 'Precision', max: 100 },
          { name: 'Recall', max: 100 },
          { name: 'F1-Score', max: 100 },
          { name: '综合能力', max: 100 },
        ],
        radius: '65%',
        axisName: { color: '#64748b', fontSize: 12 },
        splitArea: { areaStyle: { color: ['rgba(59,130,246,0.02)', 'rgba(59,130,246,0.05)'] } },
      },
      series: [{
        type: 'radar',
        data: [
          {
            value: [55, 52.3, 48.7, 50.4, 51],
            name: '微调前',
            lineStyle: { color: '#ef4444', width: 2 },
            itemStyle: { color: '#ef4444' },
            areaStyle: { color: 'rgba(239,68,68,0.1)' },
          },
          {
            value: [95.2, 95.5, 94.8, 95.1, 94],
            name: '微调后',
            lineStyle: { color: '#10b981', width: 2 },
            itemStyle: { color: '#10b981' },
            areaStyle: { color: 'rgba(16,185,129,0.15)' },
          },
        ],
        animationDuration: 2000,
      }],
    })
  }

  // Perplexity
  if (pplRef.value) {
    const c = echarts.init(pplRef.value)
    charts.push(c)
    c.setOption({
      tooltip: { trigger: 'axis' },
      grid: { left: 60, right: 20, top: 20, bottom: 40 },
      xAxis: {
        type: 'category', data: ['Epoch 0', 'Epoch 1', 'Epoch 2', 'Epoch 3', 'Epoch 4', 'Epoch 5'],
        axisLine: { lineStyle: { color: '#e2e8f0' } }, axisLabel: { color: '#94a3b8' },
      },
      yAxis: {
        type: 'value', name: 'Perplexity',
        axisLine: { show: false }, splitLine: { lineStyle: { color: '#f1f5f9', type: 'dashed' } },
        axisLabel: { color: '#94a3b8' },
      },
      series: [{
        type: 'line', data: [18.6, 10.2, 6.5, 4.8, 3.8, 3.2],
        lineStyle: { width: 3, color: '#8b5cf6' }, itemStyle: { color: '#8b5cf6' },
        symbol: 'circle', symbolSize: 8,
        areaStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(139,92,246,0.2)' }, { offset: 1, color: 'rgba(139,92,246,0)' }
        ])},
        markPoint: { data: [{ type: 'max', name: '最大值' }, { type: 'min', name: '最小值' }] },
      }],
      animationDuration: 2000,
    })
  }

  // Val Loss
  if (valLossRef.value) {
    const c = echarts.init(valLossRef.value)
    charts.push(c)
    c.setOption({
      tooltip: { trigger: 'axis' },
      legend: { data: ['Training Loss', 'Validation Loss'], bottom: 0, textStyle: { color: '#94a3b8' } },
      grid: { left: 60, right: 20, top: 20, bottom: 50 },
      xAxis: {
        type: 'category', data: ['Epoch 0', 'Epoch 1', 'Epoch 2', 'Epoch 3', 'Epoch 4', 'Epoch 5'],
        axisLine: { lineStyle: { color: '#e2e8f0' } }, axisLabel: { color: '#94a3b8' },
      },
      yAxis: {
        type: 'value', name: 'Loss',
        axisLine: { show: false }, splitLine: { lineStyle: { color: '#f1f5f9', type: 'dashed' } },
        axisLabel: { color: '#94a3b8' },
      },
      series: [
        {
          name: 'Training Loss', type: 'line', data: [1.80, 0.95, 0.55, 0.35, 0.26, 0.22],
          lineStyle: { width: 3, color: '#3b82f6' }, itemStyle: { color: '#3b82f6' },
          symbol: 'circle', symbolSize: 8,
        },
        {
          name: 'Validation Loss', type: 'line', data: [1.82, 0.98, 0.58, 0.38, 0.28, 0.24],
          lineStyle: { width: 3, color: '#ef4444' }, itemStyle: { color: '#ef4444' },
          symbol: 'diamond', symbolSize: 8,
        },
      ],
      animationDuration: 2000,
    })
  }
}

const handleResize = () => charts.forEach(c => c.resize())

onMounted(() => { nextTick(initCharts); window.addEventListener('resize', handleResize) })
onUnmounted(() => { window.removeEventListener('resize', handleResize); charts.forEach(c => c.dispose()) })
</script>

<style scoped>
.ai-model { max-width: 1400px; margin: 0 auto; }

/* 概要卡片 */
.model-summary {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 12px;
  margin-bottom: 24px;
}
.summary-item {
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  padding: 16px;
  text-align: center;
  transition: all 0.25s;
}
.summary-item:hover { border-color: var(--accent); transform: translateY(-2px); }
.summary-label { font-size: 12px; color: var(--text-muted); margin-bottom: 6px; }
.summary-value { font-size: 16px; font-weight: 800; font-family: var(--font-mono); }

/* 图表网格 */
.chart-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
  margin-bottom: 24px;
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

/* 对比区 */
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

.comparison-section { margin-bottom: 24px; }
.comparison-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}
.comparison-item {
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  padding: 18px;
}
.metric-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}
.metric-name { font-weight: 700; color: var(--text-title); font-size: 15px; }
.metric-change {
  font-size: 13px;
  font-weight: 700;
  padding: 2px 10px;
  border-radius: 12px;
  font-family: var(--font-mono);
}
.metric-change.improved { color: #10b981; background: rgba(16,185,129,0.1); }
.metric-change.degraded { color: #ef4444; background: rgba(239,68,68,0.1); }

.metric-bars { display: flex; flex-direction: column; gap: 8px; }
.bar-row { display: flex; align-items: center; gap: 10px; }
.bar-label { font-size: 12px; color: var(--text-muted); width: 50px; flex-shrink: 0; }
.bar-track { flex: 1; height: 10px; background: var(--bg-page); border-radius: 5px; overflow: hidden; }
.bar-fill { height: 100%; border-radius: 5px; transition: width 1.5s ease; }
.bar-fill.before { background: #e2e8f0; }
.bar-fill.after { background: linear-gradient(90deg, #3b82f6, #10b981); }
.bar-value { font-size: 13px; font-weight: 700; color: var(--text-title); width: 55px; text-align: right; font-family: var(--font-mono); }
.bar-value.after-val { color: #10b981; }

/* 底部图表 */
.bottom-charts {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
}
</style>
