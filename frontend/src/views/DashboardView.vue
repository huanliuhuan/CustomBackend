<template>
  <div class="page">
    <div class="page-head">
      <div>
        <div class="tiny muted">{{ dateLine }}</div>
        <div class="page-title">{{ hello }},{{ auth.user?.nickName }}</div>
        <div class="page-sub">系统运行概况、登录脉搏与今日操作轨迹。</div>
      </div>
    </div>

    <div class="grid">
      <div class="card card-pad pulse-card">
        <div class="pc-hd">
          <div class="card-title">登录脉搏</div>
          <div class="seg">
            <button v-for="t in ranges" :key="t.k" :class="{ on: range === t.k }" @click="range = t.k">{{ t.label }}</button>
          </div>
        </div>
        <div class="pc-main">
          <div class="pc-left">
            <div class="pc-num"><RollNum :value="pulse.total" :size="46" /><span class="unit">次</span></div>
            <div class="pc-delta" :class="pulse.delta >= 0 ? 'up' : 'down'">
              {{ pulse.delta >= 0 ? '▲' : '▼' }} {{ Math.abs(pulse.delta) }}% 较上一周期
            </div>
            <div class="pc-mini">
              <div><span class="tiny muted">峰值</span><b>{{ pulse.peak }}</b></div>
              <div><span class="tiny muted">均值</span><b>{{ pulse.avg }}</b></div>
              <div><span class="tiny muted">范围</span><b>{{ rangeLabel }}</b></div>
            </div>
            <div class="pc-acts">
              <button class="lnk" @click="$router.push('/monitor/loginlog')">查看登录日志 →</button>
              <button class="lnk ghost" @click="load"><AIcon name="refresh" :size="13" /> 刷新</button>
            </div>
          </div>
          <div ref="pulseEl" class="pc-chart" />
        </div>
      </div>

      <div class="col">
        <div class="card card-pad mini-card">
          <div class="mc-hd"><div class="card-title">在线用户 · 近 1 小时</div><AIcon name="users" :size="15" class="muted" /></div>
          <div class="mc-num"><RollNum :value="sum.online" :size="34" /><span class="unit">人</span></div>
          <div class="tiny muted">最近 1 小时活跃会话</div>
        </div>
        <div class="card card-pad mini-card">
          <div class="mc-hd"><div class="card-title">接口调用 · 近 24 小时</div><AIcon name="monitor" :size="15" class="muted" /></div>
          <div class="mc-num"><RollNum :value="sum.apiCalls24h" :size="34" /><span class="unit">次</span></div>
          <div class="tiny muted">写操作接口最近 24 小时调用量</div>
        </div>
      </div>

      <div class="card card-pad">
        <div class="mc-hd"><div class="card-title">近 7 天操作量</div><span class="tiny muted">{{ oper7Total }} 次</span></div>
        <div ref="barEl" class="chart-sm" />
      </div>

      <div class="card card-pad rate-card">
        <div class="mc-hd"><div class="card-title">操作成功率</div><span class="tiny muted">近 24 小时</span></div>
        <div class="rate-body">
          <TickGauge :pct="sum.successRate" :size="188">
            <RollNum :value="sum.successRate" :size="30" />
            <span class="tiny muted">% 成功率</span>
          </TickGauge>
          <div class="rate-side">
            <div><span class="dot" style="background: var(--acc)" /> 操作 <b>{{ sum.operSuccess }}</b></div>
            <div><span class="dot" style="background: var(--red)" /> 失败 <b>{{ sum.operFail }}</b></div>
          </div>
        </div>
      </div>

      <div class="card card-pad">
        <div class="mc-hd"><div class="card-title">数据权限分布</div><span class="tiny muted">按角色范围</span></div>
        <div class="scope">
          <div v-for="s in scopeRows" :key="s.k" class="scope-row">
            <span class="tiny muted sc-l">{{ s.label }}</span>
            <div class="sc-bar"><i :style="{ width: s.pct + '%' }" :class="s.cls" /></div>
            <span class="tiny sc-v">{{ s.count }}人</span>
          </div>
          <div v-if="!scopeRows.length" class="tiny muted" style="padding: 18px 0">暂无数据</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, onUnmounted, ref, watch } from 'vue'
import * as echarts from 'echarts'
import AIcon from '../ui/AIcon.vue'
import RollNum from '../ui/RollNum.vue'
import TickGauge from '../ui/TickGauge.vue'
import { api } from '../api'
import { useAuth } from '../stores/auth'
import { useTheme } from '../stores/theme'

const auth = useAuth()
const theme = useTheme()
const range = ref('24h')
const ranges = [{ k: '24h', label: '24 小时' }, { k: '7d', label: '7 天' }, { k: '30d', label: '30 天' }]
const pulse = ref({ total: 0, peak: 0, avg: 0, delta: 0, points: [] })
const sum = ref({ online: 0, apiCalls24h: 0, successRate: 0, operSuccess: 0, operFail: 0, oper7d: [], scopeDist: [] })

const pulseEl = ref(null)
const barEl = ref(null)
let pc, bc

const rangeLabel = computed(() => (range.value === '24h' ? '近 24 小时' : range.value === '7d' ? '近 7 天' : '近 30 天'))
const hello = computed(() => {
  const h = new Date().getHours()
  return h < 6 ? '夜深了' : h < 12 ? '上午好' : h < 14 ? '中午好' : h < 18 ? '下午好' : '晚上好'
})
const dateLine = computed(() => {
  const d = new Date()
  return d.getFullYear() + ' 年 ' + (d.getMonth() + 1) + ' 月 ' + d.getDate() + ' 日 · 星期' + '日一二三四五六'[d.getDay()]
})
const oper7Total = computed(() => (sum.value.oper7d || []).reduce((a, b) => a + (b.c || 0), 0))
const scopeRows = computed(() => {
  const labelMap = { 1: '全部数据', 2: '自定义', 3: '本部门', 4: '本部门及以下', 5: '仅本人' }
  const clsMap = { 1: 'c1', 2: 'c2', 3: 'c3', 4: 'c4', 5: 'c5' }
  const rows = (sum.value.scopeDist || []).map(s => ({ k: s.ds, label: labelMap[s.ds] || s.ds, count: s.c, cls: clsMap[s.ds] }))
  const max = Math.max(1, ...rows.map(r => r.count))
  rows.forEach(r => { r.pct = Math.round(r.count * 100 / max) })
  return rows.sort((a, b) => b.count - a.count)
})

function chartTxt() { return { color: getComputedStyle(document.documentElement).getPropertyValue('--txt2').trim() } }

function drawPulse() {
  if (!pc) pc = echarts.init(pulseEl.value)
  const pts = pulse.value.points || []
  pc.setOption({
    grid: { left: 8, right: 10, top: 14, bottom: 6, containLabel: true },
    tooltip: { trigger: 'axis' },
    xAxis: {
      type: 'category', boundaryGap: false,
      data: pts.map(p => fmtT(p.t)),
      axisLine: { lineStyle: { color: 'rgba(140,150,130,0.25)' } },
      axisLabel: { ...chartTxt(), fontSize: 10.5, interval: Math.max(0, Math.floor(pts.length / 6)) },
      axisTick: { show: false }
    },
    yAxis: {
      type: 'value', splitLine: { lineStyle: { color: 'rgba(140,150,130,0.14)', type: 'dashed' } },
      axisLabel: { ...chartTxt(), fontSize: 10.5 }
    },
    series: [{
      type: 'line', smooth: 0.5, symbol: 'circle', symbolSize: 6, showSymbol: false,
      lineStyle: { color: '#71b81f', width: 2.5 },
      itemStyle: { color: '#71b81f', borderColor: '#fff', borderWidth: 1.5 },
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(113,184,31,0.32)' },
          { offset: 1, color: 'rgba(113,184,31,0.02)' }
        ])
      },
      data: pts.map(p => p.c)
    }]
  })
}

function drawBar() {
  if (!bc) bc = echarts.init(barEl.value)
  const data = sum.value.oper7d || []
  bc.setOption({
    grid: { left: 6, right: 6, top: 16, bottom: 4, containLabel: true },
    tooltip: { trigger: 'axis' },
    xAxis: {
      type: 'category', data: data.map(d => d.d),
      axisLine: { lineStyle: { color: 'rgba(140,150,130,0.25)' } },
      axisLabel: { ...chartTxt(), fontSize: 10.5 }, axisTick: { show: false }
    },
    yAxis: { type: 'value', splitLine: { show: false }, axisLabel: { show: false } },
    series: [{
      type: 'bar', barWidth: '46%',
      itemStyle: { borderRadius: [7, 7, 7, 7], color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: '#8fd634' }, { offset: 1, color: 'rgba(113,184,31,0.35)' }]) },
      data: data.map(d => d.c)
    }]
  })
}

function fmtT(t) {
  if (!t) return ''
  if (t.length > 10) return t.slice(5, 13)
  return t.slice(5)
}

async function load() {
  const [p, s] = await Promise.all([api.pulse(range.value), api.summary()])
  pulse.value = p
  sum.value = s
  drawPulse()
  drawBar()
}

watch(range, load)
watch(() => theme.mode, () => { drawPulse(); drawBar() })

const onRz = () => { pc && pc.resize(); bc && bc.resize() }
onMounted(() => {
  load()
  window.addEventListener('resize', onRz)
})
onUnmounted(() => {
  window.removeEventListener('resize', onRz)
  pc && pc.dispose()
  bc && bc.dispose()
})
</script>

<style scoped>
.grid { display: grid; grid-template-columns: 2.2fr 1fr; gap: 16px; }
.col { display: flex; flex-direction: column; gap: 16px; }
.pulse-card { display: flex; flex-direction: column; }
.pc-hd { display: flex; align-items: center; justify-content: space-between; margin-bottom: 8px; flex-wrap: wrap; gap: 10px; }
.pc-main { display: flex; gap: 14px; flex: 1; min-height: 250px; }
.pc-left { width: 200px; flex: none; display: flex; flex-direction: column; justify-content: center; gap: 10px; }
.pc-num { display: flex; align-items: baseline; gap: 7px; color: var(--txt); }
.unit { font-size: 13px; color: var(--txt3); }
.pc-delta { font-size: 12.5px; font-weight: 600; }
.pc-delta.up { color: var(--acc-deep); }
.pc-delta.down { color: var(--red); }
.pc-mini { display: flex; gap: 18px; }
.pc-mini b { display: block; font-size: 16px; margin-top: 2px; }
.pc-acts { display: flex; gap: 12px; margin-top: 4px; flex-wrap: wrap; }
.lnk { border: 0; background: var(--dark-btn); color: var(--bg); border-radius: 999px; padding: 8px 16px; font-size: 12.5px; cursor: pointer; display: inline-flex; gap: 6px; align-items: center; transition: all 0.2s; }
:root[data-theme='dark'] .lnk { color: #0d120d; }
.lnk:hover { filter: brightness(1.15); transform: translateY(-1px); }
.lnk.ghost { background: var(--card2); color: var(--txt2); border: 1px solid var(--line); }
.pc-chart { flex: 1; min-width: 0; min-height: 240px; }
.mini-card { flex: 1; }
.mc-hd { display: flex; align-items: center; justify-content: space-between; margin-bottom: 12px; gap: 8px; }
.mc-num { display: flex; align-items: baseline; gap: 7px; margin-bottom: 6px; }
.chart-sm { height: 168px; }
.rate-card { display: flex; flex-direction: column; }
.rate-body { display: flex; align-items: center; gap: 20px; justify-content: center; flex: 1; }
.rate-side { display: flex; flex-direction: column; gap: 14px; font-size: 13px; }
.rate-side .dot { margin-right: 7px; }
.scope { display: flex; flex-direction: column; gap: 15px; padding: 8px 2px; }
.scope-row { display: flex; align-items: center; gap: 12px; }
.sc-l { width: 84px; flex: none; }
.sc-bar { flex: 1; height: 9px; border-radius: 999px; background: var(--card2); overflow: hidden; }
.sc-bar i { display: block; height: 100%; border-radius: 999px; animation: grow 0.9s cubic-bezier(0.2, 1, 0.4, 1); }
.sc-v { width: 40px; text-align: right; }
.c1 { background: linear-gradient(90deg, #8fd634, #4e8f0c); }
.c2 { background: var(--violet); }
.c3 { background: var(--teal); }
.c4 { background: var(--blue); }
.c5 { background: var(--amber); }
@keyframes grow { from { width: 0 !important; } }

@media (max-width: 1200px) {
  .grid { grid-template-columns: 1fr; }
}
</style>
