<template>
  <div class="hm">
    <div class="hm-grid" :style="{ '--cols': 24 }">
      <div class="hm-corner tiny muted"></div>
      <div v-for="h in 24" :key="'h' + h" class="hm-h tiny muted">{{ (h - 1) % 3 === 0 ? pad(h - 1) : '' }}</div>
      <template v-for="(d, di) in dows" :key="'row' + di">
        <div class="hm-l tiny muted">{{ d }}</div>
        <div v-for="h in 24" :key="di + '-' + h" class="hm-cell" :style="cellStyle(di + 1, h - 1)"
             :title="tip(di + 1, h - 1)" />
      </template>
    </div>
    <div class="hm-ft tiny muted">
      <span>{{ peakText }}</span>
      <span class="hm-legend">少 <i v-for="i in 5" :key="i" :style="{ background: 'rgba(113,184,31,' + (i * 0.16 + 0.08) + ')' }" /> 多</span>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({ heat: { type: Array, default: () => [] } })
const dows = ['周一', '周二', '周三', '周四', '周五', '周六', '周日']

const map = computed(() => {
  const m = new Map()
  let max = 0
  let peak = null
  for (const r of props.heat || []) {
    const c = Number(r.c) || 0
    m.set(r.dow + '-' + r.h, c)
    if (c > max) { max = c; peak = r }
  }
  return { m, max, peak }
})

function pad(n) { return String(n).padStart(2, '0') }
function cellStyle(dow, h) {
  const c = map.value.m.get(dow + '-' + h) || 0
  const a = c === 0 ? 0 : Math.min(1, 0.22 + (c / (map.value.max || 1)) * 0.78)
  return c === 0
    ? { background: 'var(--card2)' }
    : { background: `rgba(113,184,31,${a.toFixed(2)})`, boxShadow: a > 0.75 ? '0 0 10px rgba(143,214,52,0.4)' : 'none' }
}
function tip(dow, h) { return `${dows[dow - 1]} ${pad(h)}:00 · ${map.value.m.get(dow + '-' + h) || 0} 次登录` }
const peakText = computed(() => {
  const p = map.value.peak
  return p ? `高峰:${dows[p.dow - 1]} ${pad(p.h)}:00 · ${p.c} 次登录` : '暂无数据'
})
</script>

<style scoped>
.hm { padding: 6px 2px 2px; }
.hm-grid {
  display: grid; grid-template-columns: 44px repeat(24, 1fr); gap: 4px;
}
.hm-h { text-align: left; padding-bottom: 2px; }
.hm-l { padding-right: 4px; display: flex; align-items: center; }
.hm-cell {
  aspect-ratio: 1.35; border-radius: 5px; transition: transform 0.15s, background 0.3s;
  animation: hIn 0.5s backwards;
}
.hm-cell:hover { transform: scale(1.28); outline: 1.5px solid var(--acc); z-index: 2; position: relative; }
@keyframes hIn { from { opacity: 0; transform: scale(0.6); } }
.hm-ft { display: flex; align-items: center; justify-content: space-between; padding: 12px 2px 0; }
.hm-legend { display: inline-flex; align-items: center; gap: 4px; }
.hm-legend i { width: 14px; height: 9px; border-radius: 3px; display: inline-block; }
</style>
