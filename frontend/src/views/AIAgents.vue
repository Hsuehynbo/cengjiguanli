<template>
  <div class="ai-agents">
    <div class="page-header">
      <h2>智能体应用</h2>
      <p>核心AI能力封装为可复用、可复制的独立智能体</p>
    </div>

    <!-- 默认视图：4个卡片 -->
    <div v-if="activeAgent === null" class="agent-grid">
      <div v-for="(agent, i) in agents" :key="i" class="agent-card" @click="enterAgent(i)">
        <div class="agent-icon-box" :style="{ background: agent.gradient }">
          <span>{{ agent.icon }}</span>
        </div>
        <h3>{{ agent.name }}</h3>
        <p>{{ agent.desc }}</p>
        <div class="agent-features">
          <span v-for="(f, j) in agent.features" :key="j" class="agent-feature">{{ f }}</span>
        </div>
        <a-button type="primary" block>进入体验</a-button>
      </div>
    </div>

    <!-- 风险评估智能体 -->
    <div v-else-if="activeAgent === 0" class="agent-view">
      <div class="view-header">
        <a-button @click="activeAgent = null" size="small">← 返回</a-button>
        <h3>🎯 风险评估智能体</h3>
      </div>
      <div class="view-body">
        <div class="demo-section">
          <div class="demo-input">
            <div class="demo-label">输入：民辅警17维数据</div>
            <div class="data-tags">
              <a-tag v-for="d in riskInputData" :key="d" color="blue">{{ d }}</a-tag>
            </div>
          </div>
          <div class="demo-arrow">→</div>
          <div class="demo-process">
            <div class="process-steps">
              <div class="p-step" :class="{ done: riskAnimStep >= 1 }">数据向量化</div>
              <div class="p-step" :class="{ done: riskAnimStep >= 2 }">AI推理分析</div>
              <div class="p-step" :class="{ done: riskAnimStep >= 3 }">风险评分</div>
              <div class="p-step" :class="{ done: riskAnimStep >= 4 }">生成报告</div>
            </div>
          </div>
          <div class="demo-arrow">→</div>
          <div class="demo-output">
            <div class="demo-label">输出：风险评估报告</div>
            <div class="output-card" v-if="riskAnimDone">
              <div class="output-score" style="color: #ef4444;">95</div>
              <a-tag color="red" style="font-size: 14px; padding: 2px 12px;">高危</a-tag>
              <div class="output-text">
                <strong>AI分析：</strong>马永强同志存在多维度严重异常：执法办案扣分累计18分，超期案件5起；财务报销近3个月异常增长；群众投诉3次，信访举报2条。<br>
                <strong>建议：</strong>立即启动专项调查，核查财务凭证，约谈当事人。
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 文书生成智能体 -->
    <div v-else-if="activeAgent === 1" class="agent-view">
      <div class="view-header">
        <a-button @click="activeAgent = null" size="small">← 返回</a-button>
        <h3>📝 文书生成智能体</h3>
      </div>
      <div class="view-body">
        <div class="demo-section">
          <div class="demo-input">
            <div class="demo-label">输入：预警信息 + 文书类型</div>
            <a-tag color="red">高危预警 - 马永强</a-tag>
            <a-tag color="blue">督察通知书</a-tag>
          </div>
          <div class="demo-arrow">→</div>
          <div class="demo-output" style="flex: 1;">
            <div class="demo-label">输出：标准化文书</div>
            <div class="doc-paper">
              <div class="doc-title">关于对马永强同志进行督察的通知</div>
              <div class="doc-body">{{ typewriterDoc }}<span class="cursor" v-if="isDocTyping">|</span></div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 数据分析智能体 -->
    <div v-else-if="activeAgent === 2" class="agent-view">
      <div class="view-header">
        <a-button @click="activeAgent = null" size="small">← 返回</a-button>
        <h3>📊 数据分析智能体</h3>
      </div>
      <div class="view-body">
        <div class="demo-section">
          <div class="demo-input">
            <div class="demo-label">输入：自然语言问题</div>
            <div class="nl-query">"本月高风险人员有哪些？"</div>
          </div>
          <div class="demo-arrow">→</div>
          <div class="demo-output" style="flex: 1;">
            <div class="demo-label">输出：结构化数据 + 图表</div>
            <div class="query-answer">本月共有 <strong>3</strong> 名高风险人员：马永强（95分，治安大队）、李明辉（92分，刑侦大队）、许伟强（91分，交警大队）。</div>
            <div ref="queryChartRef" style="height: 260px;"></div>
          </div>
        </div>
      </div>
    </div>

    <!-- 知识问答智能体 -->
    <div v-else-if="activeAgent === 3" class="agent-view">
      <div class="view-header">
        <a-button @click="activeAgent = null" size="small">← 返回</a-button>
        <h3>💡 知识问答智能体</h3>
      </div>
      <div class="view-body">
        <div class="chat-container">
          <div class="chat-messages" ref="chatRef">
            <div v-for="(msg, i) in chatMessages" :key="i" class="chat-msg" :class="msg.role">
              <div class="msg-avatar">{{ msg.role === 'user' ? '👤' : '🤖' }}</div>
              <div class="msg-bubble">
                {{ msg.text }}<span class="cursor" v-if="msg.typing">|</span>
              </div>
            </div>
          </div>
          <div class="chat-questions">
            <span class="chat-hint">试试问：</span>
            <a-button v-for="(q, i) in knowledgeQuestions" :key="i" size="small" @click="askQuestion(q)">
              {{ q }}
            </a-button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, nextTick, onUnmounted } from 'vue'
import * as echarts from 'echarts'

const agents = [
  { name: '风险评估智能体', icon: '🎯', gradient: 'linear-gradient(135deg, #ef4444, #f97316)', desc: '输入民辅警数据，自动输出风险评分、等级、分析摘要和处置建议', features: ['单人评估', '批量评估', 'AI分析'] },
  { name: '文书生成智能体', icon: '📝', gradient: 'linear-gradient(135deg, #3b82f6, #6366f1)', desc: '根据预警结果，自动生成督察通知书、谈话记录、整改通知等标准化文书', features: ['督察通知', '谈话记录', '整改通知'] },
  { name: '数据分析智能体', icon: '📊', gradient: 'linear-gradient(135deg, #10b981, #06b6d4)', desc: '支持自然语言查询，直接返回结构化图表和数据', features: ['自然语言', '图表生成', '数据洞察'] },
  { name: '知识问答智能体', icon: '💡', gradient: 'linear-gradient(135deg, #8b5cf6, #ec4899)', desc: '基于督察法规库和历史案例库，回答业务问题', features: ['法规查询', '案例对标', '智能问答'] },
]

const activeAgent = ref(null)
let queryChart = null

const enterAgent = (i) => {
  activeAgent.value = i
  if (i === 0) startRiskAnim()
  if (i === 1) startDocTypewriter()
  if (i === 2) nextTick(initQueryChart)
  if (i === 3) resetChat()
}

// ========== 风险评估 ==========
const riskInputData = ['执法办案: 扣分18', '财务报销: +1.5万', '群众投诉: 3次', '信访举报: 2条', '考勤: 正常', '车辆: 正常']
const riskAnimStep = ref(0)
const riskAnimDone = ref(false)

const startRiskAnim = () => {
  riskAnimStep.value = 0
  riskAnimDone.value = false
  const run = () => {
    if (riskAnimStep.value < 4) {
      riskAnimStep.value++
      setTimeout(run, 600)
    } else {
      riskAnimDone.value = true
    }
  }
  setTimeout(run, 400)
}

// ========== 文书生成 ==========
const typewriterDoc = ref('')
const isDocTyping = ref(false)
let docTimer = null

const docText = `治安大队：

根据AI风险预警系统监测，你大队马永强同志近期存在以下异常情况：
1. 执法办案平台扣分事项累计达18分，超期案件5起，程序违规3次；
2. 财务报销近3个月金额异常增长，存在虚报嫌疑；
3. 群众投诉3次，满意度评分低于60分；
4. 信访系统收到举报工单2条。

经综合评估，该同志风险评分为95分（高危），现决定启动专项督察程序。请于收到本通知后3个工作日内，将相关情况书面报告督察大队。

督察大队
2026年7月23日`

const startDocTypewriter = () => {
  typewriterDoc.value = ''
  isDocTyping.value = true
  let i = 0
  if (docTimer) clearInterval(docTimer)
  docTimer = setInterval(() => {
    if (i < docText.length) {
      typewriterDoc.value += docText[i]
      i++
    } else {
      clearInterval(docTimer)
      isDocTyping.value = false
    }
  }, 20)
}

// ========== 数据分析 ==========
const queryChartRef = ref(null)

const initQueryChart = () => {
  if (queryChartRef.value) {
    if (queryChart) queryChart.dispose()
    queryChart = echarts.init(queryChartRef.value)
    queryChart.setOption({
      tooltip: { trigger: 'axis' },
      grid: { left: 50, right: 20, top: 20, bottom: 40 },
      xAxis: { type: 'category', data: ['马永强', '李明辉', '许伟强'], axisLabel: { color: '#64748b', fontSize: 13 } },
      yAxis: { type: 'value', max: 100, axisLabel: { color: '#94a3b8' }, splitLine: { lineStyle: { color: '#f1f5f9', type: 'dashed' } } },
      series: [{
        type: 'bar', barWidth: 50,
        data: [
          { value: 95, itemStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: '#ef4444' }, { offset: 1, color: '#f97316' }]) } },
          { value: 92, itemStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: '#ef4444' }, { offset: 1, color: '#f97316' }]) } },
          { value: 91, itemStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: '#ef4444' }, { offset: 1, color: '#f97316' }]) } },
        ],
        itemStyle: { borderRadius: [8, 8, 0, 0] },
        label: { show: true, position: 'top', formatter: '{c}分', fontWeight: 800, fontSize: 16, color: '#1e293b' },
      }],
      animationDuration: 1500,
      animationEasing: 'elasticOut',
    })
  }
}

// ========== 知识问答 ==========
const chatMessages = ref([])
const chatRef = ref(null)

const knowledgeQuestions = [
  '督察工作的主要职责是什么？',
  '什么情况下需要启动专项督察？',
  '如何处理群众投诉举报？',
]

const knowledgeAnswers = {
  '督察工作的主要职责是什么？': '根据《公安机关督察条例》，督察工作的主要职责包括：1）对公安机关及其人民警察依法履行职责、行使职权和遵守纪律的情况进行监督；2）受理人民群众对公安机关及其人民警察的检举、控告；3）对公安机关及其人民警察的执法活动进行现场督察；4）对违法违纪行为进行查处。核心定位是"推动履职、堵漏补位"。',
  '什么情况下需要启动专项督察？': '以下情况需要启动专项督察：1）AI风险预警系统评估为"高危"等级（≥90分）的人员；2）同一科室3人以上同时触发风险预警；3）群众投诉同一事项3次以上；4）发生重大执法过错或舆情事件；5）上级机关交办的督察事项。专项督察应在24小时内启动，15个工作日内完成。',
  '如何处理群众投诉举报？': '群众投诉举报处理流程：1）受理登记：收到投诉后24小时内完成登记；2）分类转办：根据投诉内容转交相关业务部门；3）核查处理：15个工作日内完成核查并出具处理意见；4）反馈回复：将处理结果及时反馈投诉人；5）归档分析：定期汇总分析投诉数据，发现共性问题。AI系统可自动生成投诉分析报告，辅助发现规律性问题。',
}

const resetChat = () => {
  chatMessages.value = [
    { role: 'assistant', text: '您好！我是督察知识问答智能体，可以回答督察法规、历史案例、工作流程等问题。请问有什么可以帮助您的？', typing: false },
  ]
}

const askQuestion = (q) => {
  chatMessages.value.push({ role: 'user', text: q, typing: false })
  nextTick(() => { if (chatRef.value) chatRef.value.scrollTop = chatRef.value.scrollHeight })

  const answer = knowledgeAnswers[q] || '这是一个很好的问题。根据督察工作相关规定，具体情况需要结合实际案例进行分析。建议查阅《公安机关督察条例》获取详细信息。'

  setTimeout(() => {
    chatMessages.value.push({ role: 'assistant', text: '', typing: true })
    let i = 0
    const timer = setInterval(() => {
      if (i < answer.length) {
        chatMessages.value[chatMessages.value.length - 1].text += answer[i]
        i++
        nextTick(() => { if (chatRef.value) chatRef.value.scrollTop = chatRef.value.scrollHeight })
      } else {
        clearInterval(timer)
        chatMessages.value[chatMessages.value.length - 1].typing = false
      }
    }, 20)
  }, 400)
}

onUnmounted(() => {
  if (docTimer) clearInterval(docTimer)
  if (queryChart) queryChart.dispose()
})
</script>

<style scoped>
.ai-agents { max-width: 1400px; margin: 0 auto; }

/* 卡片网格 */
.agent-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 24px; }
.agent-card {
  background: var(--bg-card); border: 1px solid var(--border-color);
  border-radius: var(--radius-lg); padding: 28px; cursor: pointer; transition: all 0.3s;
}
.agent-card:hover { transform: translateY(-4px); box-shadow: var(--shadow-lg); border-color: var(--accent); }
.agent-icon-box {
  width: 64px; height: 64px; border-radius: 16px;
  display: flex; align-items: center; justify-content: center;
  font-size: 28px; margin-bottom: 16px;
}
.agent-card h3 { font-size: 18px; font-weight: 800; color: var(--text-title); margin-bottom: 8px; }
.agent-card p { font-size: 14px; color: var(--text-secondary); line-height: 1.6; margin-bottom: 14px; }
.agent-features { display: flex; gap: 8px; margin-bottom: 18px; flex-wrap: wrap; }
.agent-feature {
  font-size: 12px; padding: 3px 10px; border-radius: 12px;
  background: var(--accent-light); color: var(--accent); font-weight: 600;
}

/* 子视图 */
.agent-view { display: flex; flex-direction: column; gap: 16px; }
.view-header { display: flex; align-items: center; gap: 12px; }
.view-header h3 { font-size: 18px; font-weight: 800; color: var(--text-title); margin: 0; }
.view-body {
  background: var(--bg-card); border: 1px solid var(--border-color);
  border-radius: var(--radius-lg); padding: 28px;
}

/* Demo布局 */
.demo-section { display: flex; align-items: flex-start; gap: 20px; }
.demo-input { min-width: 200px; }
.demo-label { font-size: 13px; font-weight: 700; color: var(--text-muted); margin-bottom: 10px; text-transform: uppercase; letter-spacing: 0.5px; }
.data-tags { display: flex; flex-wrap: wrap; gap: 6px; }
.demo-arrow { font-size: 28px; color: var(--accent); font-weight: 800; flex-shrink: 0; margin-top: 40px; }
.demo-output { flex: 1; min-width: 0; }

/* 风险评估动画 */
.process-steps { display: flex; flex-direction: column; gap: 10px; min-width: 140px; }
.p-step {
  padding: 8px 14px; border-radius: 8px; font-size: 13px; font-weight: 600;
  background: var(--bg-page); color: var(--text-muted); border: 1px solid var(--border-light);
  transition: all 0.4s;
}
.p-step.done { background: var(--accent-light); color: var(--accent); border-color: var(--accent); }
.output-card { text-align: center; animation: fadeInUp 0.5s ease; }
.output-score { font-size: 72px; font-weight: 900; font-family: var(--font-mono); line-height: 1; margin-bottom: 8px; }
.output-text { font-size: 13px; color: var(--text-secondary); line-height: 1.8; margin-top: 14px; text-align: left; background: var(--bg-page); padding: 14px; border-radius: 8px; }

/* 文书 */
.nl-query {
  font-size: 16px; font-weight: 600; color: var(--accent);
  background: var(--accent-light); padding: 10px 16px; border-radius: 8px;
  display: inline-block;
}
.query-answer {
  font-size: 14px; color: var(--text-secondary); line-height: 1.8;
  margin-bottom: 16px; padding: 14px; background: var(--bg-page); border-radius: 8px;
}

.doc-paper {
  background: #fff; border: 1px solid #e2e8f0; border-radius: 8px;
  padding: 28px; box-shadow: 0 2px 12px rgba(0,0,0,0.06);
  font-family: 'SimSun', '宋体', serif; max-height: 400px; overflow-y: auto;
}
.doc-title { text-align: center; font-size: 18px; font-weight: 700; margin-bottom: 16px; color: #1e293b; }
.doc-body { font-size: 14px; line-height: 2; white-space: pre-wrap; color: #333; }
.cursor { animation: blink 0.8s infinite; color: var(--accent); }
@keyframes blink { 0%, 100% { opacity: 1; } 50% { opacity: 0; } }

/* 知识问答 */
.chat-container { display: flex; flex-direction: column; height: 500px; }
.chat-messages { flex: 1; overflow-y: auto; display: flex; flex-direction: column; gap: 14px; padding-bottom: 10px; }
.chat-msg { display: flex; gap: 10px; align-items: flex-start; }
.chat-msg.user { flex-direction: row-reverse; }
.msg-avatar {
  width: 36px; height: 36px; border-radius: 10px;
  display: flex; align-items: center; justify-content: center;
  font-size: 18px; flex-shrink: 0; background: var(--bg-page);
}
.msg-bubble {
  max-width: 70%; padding: 12px 16px; border-radius: 12px;
  font-size: 14px; line-height: 1.7;
}
.chat-msg.assistant .msg-bubble { background: var(--bg-page); color: var(--text-primary); border-bottom-left-radius: 4px; }
.chat-msg.user .msg-bubble { background: var(--accent); color: #fff; border-bottom-right-radius: 4px; }
.chat-questions {
  display: flex; gap: 8px; padding-top: 12px;
  border-top: 1px solid var(--border-light); flex-wrap: wrap; align-items: center;
}
.chat-hint { font-size: 12px; color: var(--text-muted); }

@keyframes fadeInUp { from { opacity: 0; transform: translateY(16px); } to { opacity: 1; transform: translateY(0); } }
</style>
