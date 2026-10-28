<template>
  <div class="page">
    <div class="page-head">
      <div>
        <div class="page-title">参数配置</div>
        <div class="page-sub">系统级可变参数。改动先暂存,统一保存;内置参数受保护不可删除。</div>
      </div>
    </div>

    <div class="groups">
      <div v-for="(items, g) in grouped" :key="g" class="card card-pad gp">
        <div class="card-title gp-t">{{ g }}</div>
        <div v-for="c in items" :key="c.id" class="cfg">
          <div class="cfg-l">
            <div class="cfg-name">{{ c.name }} <AIcon v-if="c.builtin" name="lock" :size="12" class="muted" /></div>
            <div class="chip code">{{ c.key }}</div>
            <div class="tiny muted">{{ c.remark }}</div>
          </div>
          <div class="cfg-r">
            <ASwitch v-if="c.valType === 'switch'" :model-value="localVal(c) === 'true'" @update:model-value="v => setVal(c, v ? 'true' : 'false')" />
            <div v-else-if="c.valType === 'select'" class="seg">
              <button v-for="o in (c.options || '').split(',')" :key="o" :class="{ on: localVal(c) === o }" @click="setVal(c, o)">{{ segLabel(o) }}</button>
            </div>
            <AInput v-else-if="c.valType === 'pass'" width="180px" icon="lock"
                    :type="peekId === c.id ? 'text' : 'password'"
                    :model-value="localVal(c)" @update:model-value="v => setVal(c, v)">
              <template #suffix>
                <button class="peek" @click="peekId = peekId === c.id ? null : c.id"><AIcon name="eye" :size="14" /></button>
              </template>
            </AInput>
            <AInput v-else width="180px" :model-value="localVal(c)" @update:model-value="v => setVal(c, v)" />
            <span v-if="dirty.has(c.id)" class="dirty-chip tiny">原值 {{ orig.get(c.id) }} · 撤销</span>
          </div>
        </div>
      </div>
    </div>

    <transition name="fade">
      <div v-if="dirty.size" class="savebar card">
        <b>{{ dirty.size }}</b><span class="muted">项未保存</span>
        <div class="grow" />
        <ABtn variant="ghost" size="sm" @click="diffOpen = true">查看差异</ABtn>
        <ABtn variant="ghost" size="sm" @click="discard">放弃</ABtn>
        <ABtn variant="blue" size="sm" icon="check" @click="saveAll">保存改动</ABtn>
      </div>
    </transition>

    <AModal v-model="diffOpen" title="待保存的差异" width="520px">
      <div class="diff" v-for="(v, id) in dirtyMap" :key="id">
        <span class="chip code">{{ keyOf(id) }}</span>
        <span class="d-old">{{ orig.get(Number(id)) }}</span>
        <AIcon name="right" :size="13" class="muted" />
        <span class="d-new">{{ v }}</span>
      </div>
      <template #footer>
        <ABtn variant="blue" @click="diffOpen = false">知道了</ABtn>
      </template>
    </AModal>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import AIcon from '../../ui/AIcon.vue'
import ABtn from '../../ui/ABtn.vue'
import AInput from '../../ui/AInput.vue'
import ASwitch from '../../ui/ASwitch.vue'
import AModal from '../../ui/AModal.vue'
import { api } from '../../api'
import { useTheme } from '../../stores/theme'
import { toast } from '../../ui/toast'

const theme = useTheme()
const list = ref([])
const dirty = ref(new Map())
const peekId = ref(null)
const diffOpen = ref(false)

const grouped = computed(() => {
  const g = {}
  for (const c of list.value) (g[c.groupName] = g[c.groupName] || []).push(c)
  return g
})
const orig = computed(() => {
  const m = new Map()
  for (const c of list.value) m.set(c.id, c.value)
  return m
})
const dirtyMap = computed(() => Object.fromEntries(dirty.value.entries()))

function localVal(c) {
  return dirty.value.has(c.id) ? dirty.value.get(c.id) : c.value
}
function setVal(c, v) {
  const m = new Map(dirty.value)
  if (v === c.value) m.delete(c.id)
  else m.set(c.id, v)
  dirty.value = m
}
function segLabel(o) { return { light: '浅色', dark: '深色', auto: '跟随系统', true: '开启', false: '关闭' }[o] || o }
function keyOf(id) {
  const c = list.value.find(x => x.id === Number(id))
  return c ? c.key : id
}
function discard() {
  dirty.value = new Map()
  toast('已放弃未保存的改动')
}
async function saveAll() {
  const items = [...dirty.value.entries()].map(([id, value]) => ({ id, value }))
  try {
    await api.saveConfigBatch(items)
    const ap = items.find(i => keyOf(i.id) === 'sys.index.appearance')
    if (ap) theme.apply(ap.value)
    dirty.value = new Map()
    toast('已保存 ' + items.length + ' 项')
    load()
  } catch (e) { toast(e.message, 'err') }
}
async function load() { list.value = await api.configs() }
onMounted(load)
</script>

<style scoped>
.groups { display: grid; grid-template-columns: repeat(auto-fill, minmax(430px, 1fr)); gap: 16px; padding-bottom: 70px; }
.gp-t { margin-bottom: 8px; }
.cfg { display: flex; align-items: center; justify-content: space-between; gap: 18px; padding: 15px 2px; border-bottom: 1px dashed var(--line2); }
.cfg:last-child { border-bottom: 0; }
.cfg-l { display: flex; flex-direction: column; gap: 5px; align-items: flex-start; min-width: 0; }
.cfg-name { font-weight: 600; font-size: 14px; display: flex; align-items: center; gap: 6px; }
.cfg-r { display: flex; align-items: center; gap: 10px; flex: none; flex-wrap: wrap; justify-content: flex-end; }
.peek { border: 0; background: transparent; color: var(--txt3); cursor: pointer; display: flex; }
.peek:hover { color: var(--acc-deep); }
.dirty-chip { color: var(--amber); }
.savebar {
  position: fixed; left: 50%; transform: translateX(-50%); bottom: 26px; z-index: 90;
  display: flex; align-items: center; gap: 12px; padding: 10px 14px 10px 20px;
  border-radius: 999px; box-shadow: var(--shadow-lg); background: var(--dark-btn); border: 0;
}
:root[data-theme='dark'] .savebar { background: #1a241a; }
.savebar b { color: var(--acc); font-size: 16px; }
.savebar .muted { color: var(--txt2); font-size: 13px; margin-right: 6px; }
:root[data-theme='dark'] .savebar .muted { color: #9fae9f; }
.grow { flex: 1; }
.diff { display: flex; align-items: center; gap: 10px; padding: 10px 2px; border-bottom: 1px dashed var(--line2); flex-wrap: wrap; }
.d-old { color: var(--red); text-decoration: line-through; font-size: 13px; }
.d-new { color: var(--acc-deep); font-weight: 600; font-size: 13px; }
</style>
