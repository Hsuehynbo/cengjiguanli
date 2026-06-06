<template>
  <div class="time-display">
    <span class="date">{{ currentDate }}</span>
    <span class="time">{{ currentTime }}</span>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'

const currentDate = ref('')
const currentTime = ref('')
let timer = null

const updateTime = () => {
  const now = new Date()
  currentDate.value = now.toLocaleDateString('zh-CN', { year: 'numeric', month: '2-digit', day: '2-digit', weekday: 'long' })
  currentTime.value = now.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit', second: '2-digit', hour12: false })
}

onMounted(() => {
  updateTime()
  timer = setInterval(updateTime, 1000)
})

onUnmounted(() => {
  if (timer) clearInterval(timer)
})
</script>

<style scoped>
.time-display {
  display: flex;
  align-items: center;
  gap: 16px;
  font-family: 'Courier New', monospace;
}
.time-display .date {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.7);
}
.time-display .time {
  font-size: 20px;
  font-weight: bold;
  color: #00d4ff;
  text-shadow: 0 0 10px rgba(0, 212, 255, 0.3);
}
</style>
