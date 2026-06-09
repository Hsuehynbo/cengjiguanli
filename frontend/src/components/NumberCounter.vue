<template>
  <span class="number-counter" :style="{ color }">{{ displayValue }}</span>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'

const props = defineProps({
  value: { type: Number, default: 0 },
  duration: { type: Number, default: 1000 },
  decimals: { type: Number, default: 0 },
  color: { type: String, default: '' }
})

const displayValue = ref('0')

function animate(from, to) {
  const start = performance.now()
  function step(now) {
    const elapsed = now - start
    const progress = Math.min(elapsed / props.duration, 1)
    const eased = 1 - Math.pow(1 - progress, 3)
    const current = from + (to - from) * eased
    displayValue.value = current.toFixed(props.decimals)
    if (progress < 1) {
      requestAnimationFrame(step)
    }
  }
  requestAnimationFrame(step)
}

onMounted(() => {
  animate(0, props.value)
})

watch(() => props.value, (newVal, oldVal) => {
  animate(oldVal || 0, newVal)
})
</script>

<style scoped>
.number-counter {
  font-family: var(--font-mono);
  font-variant-numeric: tabular-nums;
  font-weight: 700;
}
</style>
