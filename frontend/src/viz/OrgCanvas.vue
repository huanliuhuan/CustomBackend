<template>
  <div class="oc">
    <div class="oc-bar">
      <div class="tiny muted">组织架构画布 · 点击部门查看详情,光点会从根部门一路追过去</div>
      <div class="oc-zoom">
        <button @click="zoomBy(-0.12)">−</button>
        <span class="tiny muted">{{ Math.round(scale * 100) }}%</span>
        <button @click="zoomBy(0.12)">+</button>
      </div>
    </div>
    <div class="oc-scroll" ref="wrap">
      <div class="oc-inner" :style="{ transform: 'scale(' + scale + ')', width: W + 'px', height: H + 'px' }">
        <svg :width="W" :height="H" style="position: absolute; inset: 0">
          <defs>
            <linearGradient id="ocLine" x1="0" y1="0" x2="0" y2="1">
              <stop offset="0%" stop-color="rgba(143,214,52,0.55)" />
              <stop offset="100%" stop-color="rgba(113,184,31,0.12)" />
            </linearGradient>
          </defs>
          <circle v-for="(s, i) in stars" :key="'st' + i" :cx="s.x" :cy="s.y" :r="s.r"
                  :fill="s.g ? 'var(--acc)' : 'var(--blue)'" :opacity="s.o" />
          <path v-for="e in edges" :key="e.id" :d="e.d" fill="none" stroke="url(#ocLine)" stroke-width="1.8" />
          <path v-if="tracePath" :id="'traceP' + gen" :d="tracePath" fill="none" stroke="transparent" />
          <circle v-if="tracePath" r="5" fill="var(--acc)" opacity="0.95">
            <animateMotion :dur="'0.9s'" begin="0.1s" fill="freeze" :path="tracePath" />
          </circle>
          <circle v-if="tracePath" r="3" fill="var(--acc)" opacity="0.7">
            <animateMotion :dur="'2.6s'" begin="0.2s" repeatCount="indefinite" :path="tracePath" />
          </circle>
        </svg>
        <div v-for="n in nodes" :key="n.id" class="oc-n" :class="{ root: n.depth === 0, sel: n.id === selectedId }"
             :style="{ left: n.x + 'px', top: n.y + 'px', width: ND_W + 'px', animationDelay: n.depth * 0.1 + 's' }"
             @click="$emit('select', n.id)">
          <div class="oc-n-top">
            <span class="oc-n-name">{{ n.name }}</span>
            <span class="oc-n-cnt">{{ n.userCount }}人</span>
          </div>
          <div class="oc-n-sub tiny muted"><AIcon name="user" :size="11" /> {{ n.leaderName || '未设负责人' }}</div>
          <span v-if="n.id === selectedId" class="oc-dot" />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import AIcon from '../ui/AIcon.vue'

const props = defineProps({ tree: { type: Array, default: () => [] }, selectedId: Number })
defineEmits(['select'])

const ND_W = 172
const ND_H = 62
const GAP_X = 22
const GAP_Y = 96

const scale = ref(1)
const gen = ref(0)
const wrap = ref(null)

/** 背景星点(固定伪随机,避免每次渲染跳动) */
const stars = computed(() => {
  const w = W.value || 1200, h = H.value || 500
  return Array.from({ length: 46 }, (_, i) => ({
    x: Math.round(((i * 137 + 61) % 1000) / 1000 * w),
    y: Math.round(((i * 89 + 23) % 1000) / 1000 * h),
    r: 1 + (i % 3) * 0.5,
    o: 0.1 + (i % 4) * 0.06,
    g: i % 3 === 0
  }))
})

function measure(list, depth, left) {
  let w = 0
  const out = []
  for (const d of list) {
    const kids = d.children || []
    let cw
    if (kids.length) {
      const r = measure(kids, depth + 1, left + w)
      cw = r.width
      out.push(...r.nodes)
    } else {
      cw = ND_W
    }
    out.push({ ...d, depth, x: left + w + (cw - ND_W) / 2, y: depth * (ND_H + GAP_Y) })
    w += cw + (kids.length ? 0 : GAP_X)
  }
  return { width: Math.max(w - GAP_X, ND_W), nodes: out }
}

const layout = computed(() => measure(props.tree, 0, 0))
const nodes = computed(() => layout.value.nodes)
const W = computed(() => layout.value.width + 40)
const H = computed(() => {
  const maxDepth = Math.max(0, ...nodes.value.map(n => n.depth))
  return (maxDepth + 1) * (ND_H + GAP_Y) + 30
})

const edges = computed(() => {
  const arr = []
  const walk = list => {
    for (const d of list) {
      for (const c of d.children || []) {
        const p = nodes.value.find(n => n.id === d.id)
        const q = nodes.value.find(n => n.id === c.id)
        if (p && q) {
          const x1 = p.x + ND_W / 2, y1 = p.y + ND_H
          const x2 = q.x + ND_W / 2, y2 = q.y
          const my = (y1 + y2) / 2
          arr.push({ id: d.id + '-' + c.id, d: `M ${x1} ${y1} C ${x1} ${my}, ${x2} ${my}, ${x2} ${y2}` })
        }
        walk([c])
      }
    }
  }
  walk(props.tree)
  return arr
})

const tracePath = computed(() => {
  const sel = props.selectedId
  if (!sel) return ''
  gen.value++
  const chain = []
  const flat = new Map(nodes.value.map(n => [n.id, n]))
  let cur = flat.get(sel)
  let guard = 0
  while (cur && guard++ < 20) {
    chain.unshift(cur)
    const pid = cur.parentId
    cur = pid ? flat.get(pid) : null
  }
  if (chain.length < 1) return ''
  let d = ''
  for (let i = 0; i < chain.length; i++) {
    const n = chain[i]
    const x = n.x + ND_W / 2
    const y = n.y
    if (i === 0) { d = `M ${x} ${y + ND_H / 2}`; continue }
    const py = chain[i - 1].y + ND_H
    d += ` L ${x} ${py} L ${x} ${y + ND_H / 2}`
  }
  return d
})

function zoomBy(dl) {
  scale.value = Math.min(1.4, Math.max(0.4, Math.round((scale.value + dl) * 100) / 100))
}
</script>

<style scoped>
.oc { display: flex; flex-direction: column; }
.oc-bar { display: flex; align-items: center; justify-content: space-between; padding: 14px 18px 8px; }
.oc-zoom { display: flex; align-items: center; gap: 8px; }
.oc-zoom button {
  width: 26px; height: 26px; border-radius: 8px; border: 1px solid var(--line);
  background: var(--card2); color: var(--txt2); cursor: pointer; font-size: 15px;
}
.oc-scroll { overflow: auto; padding: 10px 18px 22px; }
.oc-inner { position: relative; transform-origin: top left; transition: transform 0.25s; }
.oc-n {
  position: absolute; background: var(--card); border: 1px solid var(--line); border-radius: 14px;
  padding: 10px 13px; cursor: pointer; transition: all 0.25s; box-shadow: var(--shadow);
  animation: nIn 0.5s cubic-bezier(0.2, 1.2, 0.4, 1) backwards;
}
.oc-n:hover { transform: translateY(-3px); border-color: var(--acc); }
.oc-n.sel { border-color: var(--acc); box-shadow: 0 0 0 3px var(--acc-soft), var(--shadow); }
.oc-n.root { background: var(--dark-btn); border-color: transparent; }
.oc-n.root .oc-n-name { color: var(--bg); }
:root[data-theme='dark'] .oc-n.root .oc-n-name { color: #0d120d; }
.oc-n.root .oc-n-sub { color: rgba(255, 255, 255, 0.55); }
:root[data-theme='dark'] .oc-n.root .oc-n-sub { color: rgba(13, 18, 13, 0.55); }
.oc-n-top { display: flex; align-items: center; justify-content: space-between; gap: 8px; }
.oc-n-name { font-weight: 600; font-size: 13px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.oc-n-cnt {
  font-size: 10.5px; background: var(--acc-soft); color: var(--acc-deep);
  border-radius: 999px; padding: 1px 7px; flex: none;
}
.oc-n-sub { margin-top: 5px; display: flex; align-items: center; gap: 4px; }
.oc-dot {
  position: absolute; top: -5px; right: -5px; width: 11px; height: 11px; border-radius: 50%;
  background: var(--acc); box-shadow: 0 0 0 3px var(--acc-soft); animation: pulse 1.6s infinite;
}
@keyframes nIn { from { opacity: 0; transform: translateY(14px) scale(0.92); } to { opacity: 1; } }
</style>
