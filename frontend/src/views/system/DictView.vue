<template>
  <div class="page">
    <div class="page-head">
      <div>
        <div class="page-title">字典管理</div>
        <div class="page-sub">维护各业务页面的下拉选项和状态标签,统一选项名称、取值和显示样式。</div>
      </div>
      <div class="head-actions">
        <ATabs v-model="view" :tabs="[{ k: 'cards', label: '样册册' }, { k: 'table', label: '表格' }]" />
        <ABtn variant="blue" icon="plus" @click="openType()">新增类型</ABtn>
      </div>
    </div>

    <div v-if="view === 'cards'" class="cards">
      <div v-for="t in types" :key="t.id" class="dc card" :class="{ open: openId === t.id }">
        <div class="dc-hd" @click="toggle(t)">
          <div class="dc-tt">
            <div class="dc-name">{{ t.name }}</div>
            <div class="chip code">{{ t.type }}</div>
          </div>
          <div class="dc-cnt"><b>{{ t.data ? t.data.length : 0 }}</b><span class="tiny muted">项</span></div>
        </div>
        <div class="dc-tags">
          <ATag v-for="d in t.data" :key="d.id" :tone="d.tag || 'gray'" :dot="true">{{ d.label }}<i v-if="d.isDefault" class="def">✓</i></ATag>
        </div>
        <div class="dc-bar"><i /><i class="g" /><i class="b" /></div>
        <div class="tiny muted dc-desc">{{ t.remark || '通用字典' }}</div>

        <transition name="fade">
          <div v-if="openId === t.id" class="dc-body">
            <div class="dc-row" v-for="d in t.data" :key="'x' + d.id">
              <span class="dot" :style="{ background: tagColor(d.tag) }" />
              <b>{{ d.label }}</b>
              <span class="chip code">{{ d.value }}</span>
              <span v-if="d.isDefault" class="tiny deflab">默认</span>
              <div class="dc-ops">
                <button class="ib" @click="openData(t, d)"><AIcon name="edit" :size="14" /></button>
                <button class="ib danger" @click="removeData(d)"><AIcon name="trash" :size="14" /></button>
              </div>
            </div>
            <ABtn size="sm" variant="soft" icon="plus" style="margin-top: 12px" @click="openData(t)">新增数据</ABtn>
          </div>
        </transition>
      </div>
    </div>

    <ATable v-else :cols="tcols" :rows="types">
      <template #c_status="{ row }"><ATag :tone="row.status === '0' ? 'green' : 'red'" :dot="true">{{ row.status === '0' ? '正常' : '停用' }}</ATag></template>
      <template #actions="{ row }">
        <div class="acts">
          <button class="ib" @click="openType(row)"><AIcon name="edit" :size="15" /></button>
          <button class="ib danger" @click="removeType(row)"><AIcon name="trash" :size="15" /></button>
        </div>
      </template>
    </ATable>

    <AModal v-model="typeOpen" :title="tf.id ? '编辑类型' : '新增类型'" width="420px">
      <div class="fm">
        <div class="fm-g"><label>类型名称</label><AInput v-model="tf.name" placeholder="如:用户性别" /></div>
        <div class="fm-g"><label>类型标识</label><AInput v-model="tf.type" placeholder="如 sys_user_sex" :disabled="!!tf.id" /></div>
        <div class="fm-g"><label>备注</label><AInput v-model="tf.remark" /></div>
      </div>
      <template #footer>
        <ABtn variant="ghost" @click="typeOpen = false">取消</ABtn>
        <ABtn variant="blue" @click="saveType">保存</ABtn>
      </template>
    </AModal>

    <AModal v-model="dataOpen" :title="df.id ? '编辑字典数据' : '新增字典数据'" width="440px">
      <div class="fm">
        <div class="fm-2">
          <div class="fm-g"><label>标签</label><AInput v-model="df.label" /></div>
          <div class="fm-g"><label>取值</label><AInput v-model="df.value" /></div>
        </div>
        <div class="fm-2">
          <div class="fm-g"><label>显示色调</label>
            <ASelect v-model="df.tag" :options="tagOpts" width="100%" />
          </div>
          <div class="fm-g"><label>排序</label><AInput v-model="df.sort" type="number" /></div>
        </div>
        <div class="fm-2">
          <div class="fm-g"><label>设为默认</label>
            <div class="st-line"><ASwitch v-model="df.isDef" /><span class="tiny muted">{{ df.isDef ? '是' : '否' }}</span></div>
          </div>
          <div class="fm-g"><label>状态</label>
            <div class="st-line"><ASwitch v-model="df.statusOn" /><span class="tiny muted">{{ df.statusOn ? '正常' : '停用' }}</span></div>
          </div>
        </div>
      </div>
      <template #footer>
        <ABtn variant="ghost" @click="dataOpen = false">取消</ABtn>
        <ABtn variant="blue" @click="saveData">保存</ABtn>
      </template>
    </AModal>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import AIcon from '../../ui/AIcon.vue'
import ABtn from '../../ui/ABtn.vue'
import AInput from '../../ui/AInput.vue'
import ASelect from '../../ui/ASelect.vue'
import ASwitch from '../../ui/ASwitch.vue'
import ATag from '../../ui/ATag.vue'
import AModal from '../../ui/AModal.vue'
import ATable from '../../ui/ATable.vue'
import ATabs from '../../ui/ATabs.vue'
import { api } from '../../api'
import { toast } from '../../ui/toast'

const view = ref('cards')
const types = ref([])
const openId = ref(null)
const typeOpen = ref(false)
const dataOpen = ref(false)
const tf = reactive({ id: null, name: '', type: '', remark: '' })
const df = reactive({ id: null, type: '', label: '', value: '', tag: 'green', sort: 0, isDef: false, statusOn: true })

const tagOpts = [
  { label: '绿色', value: 'green' }, { label: '蓝色', value: 'blue' }, { label: '红色', value: 'red' },
  { label: '琥珀', value: 'amber' }, { label: '紫色', value: 'violet' }, { label: '灰色', value: 'gray' }
]
const tagColor = t => ({ green: 'var(--acc)', blue: 'var(--blue)', red: 'var(--red)', amber: 'var(--amber)', violet: 'var(--violet)', gray: 'var(--txt3)' }[t] || 'var(--txt3)')

const tcols = [
  { k: 'name', label: '类型名称' },
  { k: 'type', label: '类型标识' },
  { k: 'dataCount', label: '数据项', w: '80px' },
  { k: 'status', label: '状态', w: '80px' },
  { k: 'remark', label: '备注' }
]

function toggle(t) { openId.value = openId.value === t.id ? null : t.id }
function openType(t) {
  Object.assign(tf, { id: t?.id || null, name: t?.name || '', type: t?.type || '', remark: t?.remark || '' })
  typeOpen.value = true
}
async function saveType() {
  if (!tf.name || !tf.type) { toast('名称和标识必填', 'err'); return }
  try {
    tf.id ? await api.updateDictType(tf.id, { name: tf.name, remark: tf.remark }) : await api.addDictType({ name: tf.name, type: tf.type, remark: tf.remark })
    toast('已保存')
    typeOpen.value = false
    load()
  } catch (e) { toast(e.message, 'err') }
}
async function removeType(t) {
  if (!confirm(`删除「${t.name}」及其全部数据?`)) return
  try {
    await api.delDictType(t.id)
    toast('已删除')
    load()
  } catch (e) { toast(e.message, 'err') }
}
function openData(t, d) {
  Object.assign(df, {
    id: d?.id || null, type: t.type, label: d?.label || '', value: d?.value || '',
    tag: d?.tag || 'green', sort: d?.sort || 0, isDef: d ? d.isDefault === 1 : false, statusOn: d ? d.status === '0' : true
  })
  dataOpen.value = true
}
async function saveData() {
  if (!df.label || !df.value) { toast('标签和取值必填', 'err'); return }
  const body = { type: df.type, label: df.label, value: df.value, tag: df.tag, sort: Number(df.sort) || 0, isDefault: df.isDef ? 1 : 0, status: df.statusOn ? '0' : '1' }
  try {
    df.id ? await api.updateDictData(df.id, body) : await api.addDictData(body)
    toast('已保存')
    dataOpen.value = false
    load()
  } catch (e) { toast(e.message, 'err') }
}
async function removeData(d) {
  if (!confirm(`删除「${d.label}」?`)) return
  await api.delDictData(d.id)
  toast('已删除')
  load()
}
async function load() { types.value = await api.dicts() }
onMounted(load)
</script>

<style scoped>
.cards { display: grid; grid-template-columns: repeat(auto-fill, minmax(300px, 1fr)); gap: 16px; grid-auto-flow: dense; }
.dc { padding: 18px 20px; cursor: default; transition: all 0.35s cubic-bezier(0.25, 1, 0.4, 1); }
.dc.open { grid-column: span 2; grid-row: span 2; border-color: var(--acc); box-shadow: 0 0 0 3px var(--acc-soft), var(--shadow); }
.dc-hd { display: flex; align-items: center; justify-content: space-between; cursor: pointer; }
.dc-tt { display: flex; align-items: center; gap: 10px; flex-wrap: wrap; }
.dc-name { font-size: 16px; font-weight: 700; }
.dc-cnt b { font-size: 22px; margin-right: 4px; }
.dc-tags { display: flex; flex-wrap: wrap; gap: 7px; margin: 13px 0; }
.def { font-style: normal; margin-left: 2px; }
.dc-bar { display: flex; gap: 4px; margin: 10px 0 8px; }
.dc-bar i { height: 3px; border-radius: 3px; background: var(--amber); flex: 1; }
.dc-bar i.g { background: var(--acc); flex: 2.5; }
.dc-bar i.b { background: var(--blue); flex: 1.4; }
.dc-body { border-top: 1px dashed var(--line2); margin-top: 14px; padding-top: 14px; }
.dc-row { display: flex; align-items: center; gap: 10px; padding: 8px 0; border-bottom: 1px dashed var(--line2); }
.dc-row b { width: 52px; }
.dc-ops { margin-left: auto; display: flex; gap: 4px; }
.deflab { background: var(--acc-soft); color: var(--acc-deep); padding: 1px 8px; border-radius: 999px; }
.ib { width: 27px; height: 27px; border-radius: 8px; border: 0; background: var(--card2); color: var(--txt2); cursor: pointer; display: inline-flex; align-items: center; justify-content: center; transition: all 0.2s; }
.ib:hover { background: var(--acc-soft); color: var(--acc-deep); }
.ib.danger:hover { background: var(--red-soft); color: var(--red); }
.acts { display: inline-flex; gap: 4px; }
.fm-2 { display: grid; grid-template-columns: 1fr 1fr; gap: 14px; }
.fm-g { margin-bottom: 12px; }
.fm-g label { display: block; font-size: 12.5px; color: var(--txt2); margin-bottom: 7px; }
.fm-g :deep(.ai), .fm-g :deep(.as) { width: 100%; }
.st-line { display: flex; align-items: center; gap: 10px; height: 36px; }
</style>
