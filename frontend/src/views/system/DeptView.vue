<template>
  <div class="page">
    <div class="page-head">
      <div>
        <div class="page-title">部门管理</div>
        <div class="page-sub">组织架构。部门层级同时决定「本部门及以下」数据权限的范围。</div>
      </div>
      <div class="head-actions">
        <ATabs v-model="tab" :tabs="[{ k: 'canvas', label: '画布' }, { k: 'table', label: '表格' }]" />
        <ABtn variant="blue" icon="plus" @click="openForm({ parentId: 0 })">新增部门</ABtn>
      </div>
    </div>

    <div v-if="tab === 'canvas'" class="cv-wrap card">
      <div class="cv-detail">
        <template v-if="selDept">
          <div class="cd-tag tiny muted">根部门</div>
          <div class="cd-name">{{ selDept.name }}</div>
          <div class="cd-leader"><span class="avatar" :style="{ background: uc(selDept.leaderName) }">{{ (selDept.leaderName || '?')[0] }}</span>
            <div><b>{{ selDept.leaderName || '未设负责人' }}</b><div class="tiny muted">负责人</div></div>
          </div>
          <div class="cd-rows tiny">
            <div><span class="muted">电话</span><b>{{ selDept.phone || '-' }}</b></div>
            <div><span class="muted">邮箱</span><b>{{ selDept.email || '-' }}</b></div>
            <div><span class="muted">状态</span><ATag tone="green" :dot="true">正常</ATag></div>
          </div>
          <div class="cd-mem">
            <div class="cp-lbl">成员 · 含下级 {{ memberTotal }} 人</div>
            <div class="mem-row" v-for="m in members" :key="m.id">
              <span class="avatar" :style="{ background: uc(m.nickName) }">{{ m.nickName[0] }}</span>
              <b>{{ m.nickName }}</b>
              <span class="tiny muted">{{ m.postNames || '未设岗位' }}</span>
            </div>
            <div v-if="!members.length" class="tiny muted">该部门暂无直属成员</div>
          </div>
          <div class="cd-acts">
            <ABtn size="sm" variant="soft" icon="plus" @click="openForm({ parentId: selDept.id })">新增下级</ABtn>
            <ABtn size="sm" variant="ghost" icon="edit" @click="openForm(selDept)">编辑</ABtn>
            <ABtn size="sm" variant="danger" icon="trash" @click="remove(selDept)">删除</ABtn>
          </div>
        </template>
        <div v-else class="cd-empty muted">
          <AIcon name="dept" :size="30" />
          <p>点击画布中的任意部门<br />查看详情,光点会从根节点一路追过去</p>
        </div>
      </div>
      <div class="cv-main">
        <OrgCanvas :tree="tree" :selected-id="selId" @select="onSel" />
      </div>
    </div>

    <template v-else>
      <ATable :cols="tcols" :rows="tflat" :expanded="exp" expandable @toggle-expand="tgExp">
        <template #c_name="{ row }">
          <div class="mname" :style="{ paddingLeft: row.depth * 22 + 'px' }">
            <button v-if="row.kids" class="tw" :class="{ open: exp.has(row.id) }" @click="tgExp(row.id)"><AIcon name="right" :size="12" /></button>
            <AIcon name="dept" :size="15" class="muted" />
            <b>{{ row.name }}</b>
          </div>
        </template>
        <template #c_status="{ row }"><ATag tone="green" :dot="true">正常</ATag></template>
        <template #actions="{ row }">
          <div class="acts">
            <button class="ib" title="新增下级" @click="openForm({ parentId: row.id })"><AIcon name="plus" :size="15" /></button>
            <button class="ib" title="编辑" @click="openForm(row)"><AIcon name="edit" :size="15" /></button>
            <button class="ib danger" title="删除" @click="remove(row)"><AIcon name="trash" :size="15" /></button>
          </div>
        </template>
      </ATable>
    </template>

    <AModal v-model="formOpen" :title="form.id ? '编辑部门' : '新增部门'" width="480px">
      <div class="fm">
        <div class="fm-g"><label>上级部门</label>
          <select v-model="form.parentId" class="nat" :disabled="!!form.id">
            <option :value="0">根部门</option>
            <option v-for="d in pOpts" :key="d.id" :value="d.id">{{ d.pad }}{{ d.name }}</option>
          </select>
        </div>
        <div class="fm-2">
          <div class="fm-g"><label>部门名称</label><AInput v-model="form.name" placeholder="部门名称" /></div>
          <div class="fm-g"><label>负责人</label><AInput v-model="form.leaderName" placeholder="负责人姓名" /></div>
        </div>
        <div class="fm-2">
          <div class="fm-g"><label>电话</label><AInput v-model="form.phone" placeholder="联系电话" /></div>
          <div class="fm-g"><label>邮箱</label><AInput v-model="form.email" placeholder="部门邮箱" /></div>
        </div>
        <div class="fm-g"><label>排序</label><AInput v-model="form.sort" type="number" /></div>
      </div>
      <template #footer>
        <ABtn variant="ghost" @click="formOpen = false">取消</ABtn>
        <ABtn variant="blue" @click="save">保存</ABtn>
      </template>
    </AModal>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import AIcon from '../../ui/AIcon.vue'
import ABtn from '../../ui/ABtn.vue'
import AInput from '../../ui/AInput.vue'
import ATag from '../../ui/ATag.vue'
import AModal from '../../ui/AModal.vue'
import ATable from '../../ui/ATable.vue'
import ATabs from '../../ui/ATabs.vue'
import OrgCanvas from '../../viz/OrgCanvas.vue'
import { api } from '../../api'
import { toast } from '../../ui/toast'

const tab = ref('canvas')
const tree = ref([])
const selId = ref(null)
const members = ref([])
const exp = ref(new Set())
const formOpen = ref(false)

const flatAll = computed(() => {
  const out = []
  const walk = (list, depth) => {
    for (const d of list) {
      out.push({ ...d, depth, kids: (d.children || []).length > 0 })
      if (d.children && exp.value.has(d.id)) walk(d.children, depth + 1)
    }
  }
  walk(tree.value, 0)
  return out
})
const tflat = flatAll
const pOpts = computed(() => {
  const out = []
  const walk = (list, depth) => {
    for (const d of list) {
      out.push({ ...d, depth, pad: '　'.repeat(depth) })
      if (d.children) walk(d.children, depth + 1)
    }
  }
  walk(tree.value, 0)
  return out
})
const allFlat = computed(() => {
  const out = []
  const walk = (list, depth) => {
    for (const d of list) {
      out.push({ ...d, depth, kids: (d.children || []).length > 0 })
      if (d.children) walk(d.children, depth + 1)
    }
  }
  walk(tree.value, 0)
  return out
})
const selDept = computed(() => allFlat.value.find(d => d.id === selId.value) || null)
const memberTotal = computed(() => (selDept.value ? selDept.value.userCount : 0))

const tcols = [
  { k: 'name', label: '部门' },
  { k: 'leaderName', label: '负责人' },
  { k: 'phone', label: '电话' },
  { k: 'userCount', label: '人数', w: '80px' },
  { k: 'status', label: '状态', w: '80px' }
]

const form = reactive({ id: null, parentId: 0, name: '', leaderName: '', phone: '', email: '', sort: 0 })

function tgExp(id) {
  const s = new Set(exp.value)
  s.has(id) ? s.delete(id) : s.add(id)
  exp.value = s
}
async function onSel(id) {
  selId.value = id
  members.value = await api.deptMembers(id)
}
function openForm(row) {
  Object.assign(form, {
    id: row?.id || null, parentId: row?.parentId ?? 0, name: row?.name || '',
    leaderName: row?.leaderName || '', phone: row?.phone || '', email: row?.email || '', sort: row?.sort || 0
  })
  formOpen.value = true
}
async function save() {
  if (!form.name) { toast('部门名称必填', 'err'); return }
  const body = { parentId: Number(form.parentId) || 0, name: form.name, leaderName: form.leaderName, phone: form.phone, email: form.email, sort: Number(form.sort) || 0 }
  try {
    form.id ? await api.updateDept(form.id, body) : await api.addDept(body)
    toast('已保存')
    formOpen.value = false
    load()
  } catch (e) { toast(e.message, 'err') }
}
async function remove(row) {
  if (!confirm(`确定删除部门「${row.name}」吗?`)) return
  try {
    await api.delDept(row.id)
    toast('已删除')
    if (selId.value === row.id) selId.value = null
    load()
  } catch (e) { toast(e.message, 'err') }
}
async function load() {
  tree.value = await api.depts()
}
watch(tab, () => { if (tab.value === 'table' && !exp.value.size && tree.value[0]) exp.value = new Set([tree.value[0].id]) })

function uc(name) {
  const colors = ['#71b81f', '#3e7ef7', '#8b5cf6', '#e59819', '#0f9d8f', '#e5484d']
  let h = 0
  for (const c of name || '') h = (h * 31 + c.charCodeAt(0)) % 997
  return colors[h % colors.length]
}

onMounted(() => {
  load().then(() => {
    if (tree.value.length) onSel(tree.value[0].id)
  })
})
</script>

<style scoped>
.cv-wrap { display: grid; grid-template-columns: 300px 1fr; min-height: 560px; overflow: hidden; }
.cv-detail { border-right: 1px solid var(--line2); padding: 22px; overflow: auto; }
.cd-tag { letter-spacing: 1px; }
.cd-name { font-size: 22px; font-weight: 700; margin: 6px 0 16px; }
.cd-leader { display: flex; align-items: center; gap: 10px; padding-bottom: 16px; border-bottom: 1px dashed var(--line2); }
.cd-rows { display: flex; flex-direction: column; gap: 11px; padding: 16px 0; border-bottom: 1px dashed var(--line2); }
.cd-rows > div { display: flex; align-items: center; justify-content: space-between; gap: 10px; }
.cd-rows .muted { width: 36px; flex: none; }
.cd-mem { padding: 16px 0; }
.cp-lbl { font-size: 11.5px; color: var(--txt3); letter-spacing: 1px; margin-bottom: 10px; }
.mem-row { display: flex; align-items: center; gap: 9px; padding: 6px 0; }
.mem-row b { font-size: 13px; }
.mem-row .tiny { margin-left: auto; }
.cd-acts { display: flex; gap: 8px; margin-top: 10px; }
.cd-empty { height: 100%; display: flex; flex-direction: column; align-items: center; justify-content: center; gap: 14px; text-align: center; line-height: 1.9; }
.cv-main { min-width: 0; }
.mname { display: flex; align-items: center; gap: 7px; }
.tw { border: 0; background: transparent; color: var(--txt3); cursor: pointer; display: inline-flex; transition: transform 0.2s; width: 14px; }
.tw.open { transform: rotate(90deg); }
.acts { display: inline-flex; gap: 4px; }
.ib { width: 29px; height: 29px; border-radius: 9px; border: 0; background: var(--card2); color: var(--txt2); cursor: pointer; display: inline-flex; align-items: center; justify-content: center; transition: all 0.2s; }
.ib:hover { background: var(--acc-soft); color: var(--acc-deep); }
.ib.danger:hover { background: var(--red-soft); color: var(--red); }
.fm-2 { display: grid; grid-template-columns: 1fr 1fr; gap: 14px; }
.fm-g { margin-bottom: 12px; }
.fm-g label { display: block; font-size: 12.5px; color: var(--txt2); margin-bottom: 7px; }
.fm-g :deep(.ai) { width: 100%; }
.nat { width: 100%; height: 36px; border-radius: 999px; border: 1px solid var(--line); background: var(--card); color: var(--txt); padding: 0 14px; outline: none; font-family: inherit; }
</style>
