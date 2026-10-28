<template>
  <div class="page">
    <div class="page-head">
      <div>
        <div class="page-title">登录日志</div>
        <div class="page-sub">每一次登录尝试都会留痕:账号、来源、结果与时间。</div>
      </div>
      <div class="head-actions">
        <ATabs v-model="range" :tabs="[{ k: '24h', label: '24 小时' }, { k: '7d', label: '7 天' }, { k: '30d', label: '30 天' }]" />
      </div>
    </div>

    <div class="stats">
      <div class="card card-pad st"><span class="tiny muted">登录次数</span><RollNum :value="stats.c || 0" :size="30" /><span class="tiny muted">次</span></div>
      <div class="card card-pad st"><span class="tiny muted">成功率</span><div class="st-line"><RollNum :value="rate" :size="30" /><b class="tiny muted">%</b></div></div>
      <div class="card card-pad st"><span class="tiny muted">失败</span><div class="st-line"><RollNum :value="fails" :size="30" /><span class="tiny muted">次</span></div></div>
      <div class="card card-pad st"><span class="tiny muted">登录账号</span><div class="st-line"><RollNum :value="stats.accts || 0" :size="30" /><span class="tiny muted">个</span></div></div>
      <div class="card card-pad st"><span class="tiny muted">来源 IP</span><div class="st-line"><RollNum :value="stats.ips || 0" :size="30" /><span class="tiny muted">个</span></div></div>
    </div>

    <div class="card card-pad" style="margin: 16px 0">
      <div class="hm-hd">
        <div class="card-title">登录热力</div>
        <ATabs v-model="hmTab" :tabs="[{ k: 'heat', label: '热力' }, { k: 'trend', label: '趋势' }]" />
      </div>
      <Heatmap v-if="hmTab === 'heat'" :heat="heat" />
      <div v-else ref="trendEl" class="trend" />
    </div>

    <div class="card card-pad" style="margin-bottom: 14px">
      <div class="filter-bar">
        <AInput v-model="q.kw" icon="search" placeholder="账号 / IP" width="220px" @enter="load" />
        <ASelect v-model="q.status" :options="[{ label: '全部结果', value: '' }, { label: '成功', value: '0' }, { label: '失败', value: '1' }]" />
        <div class="grow" />
        <span class="tiny muted">共 {{ total }} 条</span>
        <ABtn variant="danger" icon="trash" :disabled="!picked.size" @click="removePicked">删除选中</ABtn>
        <ABtn icon="search" @click="load">查询</ABtn>
      </div>
    </div>

    <ATable :cols="cols" :rows="rows" check :picked="picked" @toggle-check="s => picked = s">
      <template #c_status="{ row }">
        <ATag :tone="row.status === 0 ? 'green' : 'red'" :dot="true">{{ row.status === 0 ? '成功' : '失败' }}</ATag>
      </template>
      <template #c_msg="{ row }"><span :class="{ 'fmsg': row.status !== 0 }">{{ row.msg }}</span></template>
    </ATable>
    <APager :total="total" :page="page" :size="size" @change="p => { page = p.page; size = p.size; load() }" />
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import * as echarts from 'echarts'
import ATag from '../../ui/ATag.vue'
import ABtn from '../../ui/ABtn.vue'
import AInput from '../../ui/AInput.vue'
import ASelect from '../../ui/ASelect.vue'
import ATable from '../../ui/ATable.vue'
import APager from '../../ui/APager.vue'
import ATabs from '../../ui/ATabs.vue'
import RollNum from '../../ui/RollNum.vue'
import Heatmap from '../../viz/Heatmap.vue'
import { api } from '../../api'
import { toast } from '../../ui/toast'

const range = ref('7d')
const hmTab = ref('heat')
const stats = ref({})
const heat = ref([])
const trend = ref([])
const rows = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const q = reactive({ kw: '', status: '' })
const picked = ref(new Set())
const trendEl = ref(null)
let tc

const rate = computed(() => stats.value.c ? Math.round((stats.value.s || 0) * 1000 / stats.value.c) / 10 : 0)
const fails = computed(() => (stats.value.c || 0) - (stats.value.s || 0))

const cols = [
  { k: 'account', label: '账号' },
  { k: 'ip', label: '来源 IP' },
  { k: 'zone', label: '区域', w: '90px' },
  { k: 'status', label: '结果', w: '90px' },
  { k: 'msg', label: '消息' },
  { k: 'loginTime', label: '登录时间', w: '170px', fmt: v => v ? String(v).replace('T', ' ').slice(0, 19) : '-' }
]

function drawTrend() {
  if (!tc) tc = echarts.init(trendEl.value)
  tc.setOption({
    grid: { left: 10, right: 12, top: 18, bottom: 6, containLabel: true },
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: trend.value.map(t => t.t.slice(5)), boundaryGap: false, axisLabel: { fontSize: 10.5, color: '#9aa3ad' }, axisLine: { lineStyle: { color: 'rgba(140,150,130,0.25)' } } },
    yAxis: { type: 'value', splitLine: { lineStyle: { color: 'rgba(140,150,130,0.14)', type: 'dashed' } }, axisLabel: { fontSize: 10.5, color: '#9aa3ad' } },
    series: [{ type: 'line', smooth: true, data: trend.value.map(t => t.c), lineStyle: { color: '#71b81f', width: 2.5 }, showSymbol: false, areaStyle: { color: 'rgba(113,184,31,0.14)' } }]
  })
}

async function load() {
  const [a, d] = await Promise.all([api.logAnalytics(range.value), api.loginlogs({ page: page.value, size: size.value, kw: q.kw, status: q.status, range: range.value })])
  stats.value = a.stats || {}
  heat.value = a.heat || []
  trend.value = a.trend || []
  rows.value = d.rows
  total.value = d.total
  if (hmTab.value === 'trend') setTimeout(drawTrend, 30)
}
async function removePicked() {
  if (!confirm(`删除选中的 ${picked.value.size} 条日志?`)) return
  await api.delLoginlogs([...picked.value])
  toast('已删除')
  picked.value = new Set()
  load()
}
watch(range, () => { page.value = 1; load() })
watch(hmTab, t => { if (t === 'trend') setTimeout(drawTrend, 30) })
onMounted(load)
</script>

<style scoped>
.stats { display: grid; grid-template-columns: repeat(5, 1fr); gap: 14px; }
.st { display: flex; flex-direction: column; gap: 6px; align-items: flex-start; }
.st-line { display: flex; align-items: baseline; gap: 6px; }
.hm-hd { display: flex; align-items: center; justify-content: space-between; margin-bottom: 10px; }
.trend { height: 250px; }
.fmsg { color: var(--red); }
@media (max-width: 1100px) { .stats { grid-template-columns: repeat(2, 1fr); } }
</style>
