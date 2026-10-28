<template>
  <div class="page">
    <div class="page-head">
      <div>
        <div class="page-title">操作日志</div>
        <div class="page-sub">系统写操作自动记录:操作人、请求、来源、结果与耗时。</div>
      </div>
      <div class="head-actions">
        <ATabs v-model="range" :tabs="[{ k: '24h', label: '24 小时' }, { k: '7d', label: '7 天' }, { k: '30d', label: '30 天' }]" />
        <ABtn variant="danger" icon="trash" :disabled="!picked.size" @click="removePicked">删除选中</ABtn>
      </div>
    </div>

    <div class="card card-pad" style="margin-bottom: 14px">
      <div class="filter-bar">
        <AInput v-model="q.kw" icon="search" placeholder="操作人 / 请求地址 / 模块" width="260px" @enter="load" />
        <ASelect v-model="q.status" :options="[{ label: '全部结果', value: '' }, { label: '成功', value: '0' }, { label: '失败', value: '1' }]" />
        <div class="grow" />
        <span class="tiny muted">共 {{ total }} 条</span>
        <ABtn variant="ghost" @click="reset">重置</ABtn>
        <ABtn icon="search" @click="load">查询</ABtn>
      </div>
    </div>

    <ATable :cols="cols" :rows="rows" check expandable clickable :picked="picked" :expanded="exp"
            @toggle-check="s => picked = s" @row-click="r => tgExp(r.id)">
      <template #c_oper="{ row }">
        <div class="op"><span class="avatar" :style="{ background: uc(row.operName) }">{{ (row.operName || '?')[0] }}</span><b>{{ row.operName }}</b></div>
      </template>
      <template #c_uri="{ row }">
        <div class="uri"><span class="mtag" :data-m="row.method">{{ row.method }}</span><span class="upath">{{ row.uri }}</span></div>
      </template>
      <template #c_ip="{ row }">
        <div class="src"><span>{{ row.ip }}</span><span class="chip">{{ row.zone }}</span></div>
      </template>
      <template #c_result="{ row }">
        <ATag :tone="row.result === 0 ? 'green' : 'red'" :dot="true">{{ row.result === 0 ? '成功' : '失败' }}</ATag>
      </template>
      <template #c_costMs="{ row }">{{ row.costMs }} ms</template>
      <template #expand="{ row }">
        <div class="xp">
          <div class="xp-hd">
            <div><span class="tiny muted">操作人</span><b>{{ row.operAccount }} / {{ row.operName }}</b></div>
            <div><span class="tiny muted">模块/动作</span><b>{{ row.module }} · {{ row.action }}</b></div>
            <div><span class="tiny muted">耗时</span><b>{{ row.costMs }} ms</b></div>
            <div><span class="tiny muted">时间</span><b>{{ fmtT(row.operTime) }}</b></div>
          </div>
          <div v-if="row.errMsg" class="xp-err">{{ row.errMsg }}</div>
          <div class="tiny muted xp-lbl">请求参数</div>
          <pre class="xp-json">{{ pretty(row.params) }}</pre>
        </div>
      </template>
    </ATable>
    <APager :total="total" :page="page" :size="size" @change="p => { page = p.page; size = p.size; load() }" />
  </div>
</template>

<script setup>
import { onMounted, reactive, ref, watch } from 'vue'
import ATag from '../../ui/ATag.vue'
import ABtn from '../../ui/ABtn.vue'
import AInput from '../../ui/AInput.vue'
import ASelect from '../../ui/ASelect.vue'
import ATable from '../../ui/ATable.vue'
import APager from '../../ui/APager.vue'
import ATabs from '../../ui/ATabs.vue'
import { api } from '../../api'
import { toast } from '../../ui/toast'

const rows = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const range = ref('7d')
const q = reactive({ kw: '', status: '' })
const picked = ref(new Set())
const exp = ref(new Set())

const cols = [
  { k: 'oper', label: '操作人', w: '150px' },
  { k: 'uri', label: '请求' },
  { k: 'ip', label: '来源', w: '170px' },
  { k: 'result', label: '结果', w: '90px' },
  { k: 'costMs', label: '耗时', w: '80px' },
  { k: 'operTime', label: '操作时间', w: '170px', fmt: v => fmtT(v) }
]

function fmtT(v) { return v ? String(v).replace('T', ' ').slice(0, 19) : '-' }
function pretty(s) {
  try { return JSON.stringify(JSON.parse(s), null, 2) } catch (e) { return s || '[]' }
}
function uc(name) {
  const colors = ['#71b81f', '#3e7ef7', '#8b5cf6', '#e59819', '#0f9d8f', '#e5484d']
  let h = 0
  for (const c of name || '') h = (h * 31 + c.charCodeAt(0)) % 997
  return colors[h % colors.length]
}
function tgExp(id) {
  const s = new Set(exp.value)
  s.has(id) ? s.delete(id) : s.add(id)
  exp.value = s
}
function reset() { q.kw = ''; q.status = ''; page.value = 1; load() }

async function load() {
  const d = await api.oplogs({ page: page.value, size: size.value, kw: q.kw, status: q.status, range: range.value })
  rows.value = d.rows
  total.value = d.total
}
async function removePicked() {
  if (!confirm(`删除选中的 ${picked.value.size} 条日志?`)) return
  try {
    await api.delOplogs([...picked.value])
    toast('已删除')
    picked.value = new Set()
    load()
  } catch (e) { toast(e.message, 'err') }
}
watch(range, () => { page.value = 1; load() })
onMounted(load)
</script>

<style scoped>
.op { display: flex; align-items: center; gap: 9px; }
.uri { display: flex; align-items: center; gap: 8px; min-width: 0; }
.upath { font-family: Consolas, monospace; font-size: 12.5px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.mtag { font-size: 10.5px; font-weight: 700; border-radius: 6px; padding: 2px 7px; background: var(--blue-soft); color: var(--blue); flex: none; }
.mtag[data-m='DELETE'] { background: var(--red-soft); color: var(--red); }
.mtag[data-m='PUT'] { background: var(--amber-soft); color: var(--amber); }
.src { display: flex; align-items: center; gap: 7px; font-size: 12.5px; }
.xp { padding: 18px 22px; }
.xp-hd { display: flex; gap: 34px; flex-wrap: wrap; margin-bottom: 14px; }
.xp-hd > div { display: flex; flex-direction: column; gap: 4px; }
.xp-err { background: var(--red-soft); color: var(--red); border-radius: 10px; padding: 10px 14px; font-size: 12.5px; margin-bottom: 12px; }
.xp-lbl { margin-bottom: 6px; letter-spacing: 1px; }
.xp-json {
  background: var(--card2); border: 1px solid var(--line2); border-radius: 12px; padding: 14px 16px;
  font-family: Consolas, monospace; font-size: 12.5px; line-height: 1.7; overflow: auto; max-height: 260px;
  color: var(--txt2); white-space: pre-wrap;
}
</style>
