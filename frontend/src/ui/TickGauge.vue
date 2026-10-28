<template>
  <div class="tg" :style="{ '--sz': size + 'px' }">
    <svg :width="size" :height="size" :viewBox="'0 0 ' + size + ' ' + size">
      <g :transform="'rotate(' + rot + ' ' + size / 2 + ' ' + size / 2 + ')'">
        <line v-for="(t, i) in ticks" :key="i" ref="tickEls"
              :x1="t.x1" :y1="t.y1" :x2="t.x2" :y2="t.y2"
              :stroke="i < lit ? 'var(--acc)' : 'var(--line)'" :stroke-width="t.w"
              :stroke-linecap="round" :style="{ transition: 'stroke 0.5s ' + (i * 12) + 'ms' }" />
      </g>
    </svg>
    <div class="tg-c"><slot /></div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  pct: { type: Number, default: 0 },
  size: { type: Number, default: 190 },
  count: { type: Number, default: 64 },
  from: { type: Number, default: -125 },
  to: { type: Number, default: 125 }
})

const R = computed(() => props.size / 2)
const rot = computed(() => 90)
const ticks = computed(() => {
  const arr = []
  const cx = R.value, cy = R.value
  const span = props.to - props.from
  for (let i = 0; i < props.count; i++) {
    const a = (props.from + (span * i) / (props.count - 1)) * Math.PI / 180
    const r1 = R.value - 16, r2 = R.value - 4
    arr.push({
      x1: cx + r1 * Math.cos(a), y1: cy + r1 * Math.sin(a),
      x2: cx + r2 * Math.cos(a), y2: cy + r2 * Math.sin(a),
      w: i % 8 === 0 ? 3 : 2
    })
  }
  return arr
})
const lit = computed(() => Math.round((props.pct / 100) * props.count))
</script>

<style scoped>
.tg { position: relative; width: var(--sz); height: var(--sz); display: inline-block; }
.tg-c {
  position: absolute; inset: 0; display: flex; flex-direction: column;
  align-items: center; justify-content: center; gap: 2px;
}
</style>
