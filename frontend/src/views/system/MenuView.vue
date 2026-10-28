<template>
  <div class="page">
    <div class="page-head">
      <div>
        <div class="page-title">菜单管理</div>
        <div class="page-sub">维护导航目录、页面与按钮权限,权限码前后端共用。</div>
      </div>
      <div class="head-actions"><ABtn variant="blue" icon="plus" @click="openForm({ parentId: 0 })">新增菜单</ABtn></div>
    </div>

    <ATable :cols="cols" :rows="flat" :expanded="exp" expandable @toggle-expand="id => tgExp(id)">
      <template #c_name="{ row }">
        <div class="mname" :style="{ paddingLeft: row.depth * 22 + 'px' }">
          <button v-if="row.kids" class="tw" :class="{ open: exp.has(row.id) }" @click="tgExp(row.id)">
            <AIcon name="right" :size="12" />
          </button>
          <AIcon v-if="row.icon" :name="row.icon" :size="15" class="muted" />
          <b>{{ row.name }}</b>
        </div>
      </template>
      <template #c_type="{ row }">
        <ATag :tone="row.type === 'M' ? 'amber' : row.type === 'C' ? 'blue' : 'gray'">
          {{ row.type === 'M' ? '目录' : row.type === 'C' ? '页面' : '按钮' }}
        </ATag>
      </template>
      <template #c_perm="{ row }"><span v-if="row.perm" class="chip code">{{ row.perm }}</span><span v-else class="muted">-</span></template>
      <template #c_visible="{ row }"><ATag :tone="row.visible === '0' ? 'green' : 'gray'">{{ row.visible === '0' ? '显示' : '隐藏' }}</ATag></template>
      <template #c_status="{ row }"><ATag :tone="row.status === '0' ? 'green' : 'red'" :dot="true">{{ row.status === '0' ? '启用' : '停用' }}</ATag></template>
      <template #actions="{ row }">
        <div class="acts">
          <button v-if="row.type !== 'F'" class="ib" title="新增下级" @click="openForm({ parentId: row.id })"><AIcon name="plus" :size="15" /></button>
          <button class="ib" title="编辑" @click="openForm(row)"><AIcon name="edit" :size="15" /></button>
          <button class="ib danger" title="删除" @click="remove(row)"><AIcon name="trash" :size="15" /></button>
        </div>
      </template>
    </ATable>

    <AModal v-model="formOpen" :title="form.id ? '编辑菜单' : '新增菜单'" width="520px">
      <div class="fm">
        <div class="fm-g"><label>上级菜单</label>
          <select v-model="form.parentId" class="nat">
            <option :value="0">根目录</option>
            <option v-for="m in parentOpts" :key="m.id" :value="m.id">{{ m.pad }}{{ m.name }}</option>
          </select>
        </div>
        <div class="fm-2">
          <div class="fm-g"><label>类型</label>
            <ATabs v-model="form.type" :tabs="[{ k: 'M', label: '目录' }, { k: 'C', label: '页面' }, { k: 'F', label: '按钮' }]" />
          </div>
          <div class="fm-g"><label>名称</label><AInput v-model="form.name" placeholder="菜单名称" /></div>
        </div>
        <div class="fm-2" v-if="form.type !== 'F'">
          <div class="fm-g"><label>路由路径</label><AInput v-model="form.path" placeholder="如 user" /></div>
          <div class="fm-g"><label>组件路径</label><AInput v-model="form.component" placeholder="如 system/user" /></div>
        </div>
        <div class="fm-2">
          <div class="fm-g" v-if="form.type !== 'M'"><label>权限码</label><AInput v-model="form.perm" placeholder="如 system:user:add" /></div>
          <div class="fm-g" v-if="form.type !== 'F'"><label>图标</label><AInput v-model="form.icon" placeholder="如 user" /></div>
        </div>
        <div class="fm-2">
          <div class="fm-g"><label>排序</label><AInput v-model="form.sort" type="number" /></div>
          <div class="fm-g"><label>显示状态</label>
            <div class="st-line"><ASwitch v-model="form.visibleOn" /><span class="tiny muted">{{ form.visibleOn ? '显示' : '隐藏' }}</span></div>
          </div>
        </div>
      </div>
      <template #footer>
        <ABtn variant="ghost" @click="formOpen = false">取消</ABtn>
        <ABtn variant="blue" @click="save">保存</ABtn>
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
import ATag from '../../ui/ATag.vue'
import AModal from '../../ui/AModal.vue'
import ATable from '../../ui/ATable.vue'
import ATabs from '../../ui/ATabs.vue'
import { api } from '../../api'
import { toast } from '../../ui/toast'

const tree = ref([])
const exp = ref(new Set())
const formOpen = ref(false)

const cols = [
  { k: 'name', label: '名称' },
  { k: 'type', label: '类型', w: '80px' },
  { k: 'path', label: '路由' },
  { k: 'perm', label: '权限码' },
  { k: 'sort', label: '排序', w: '64px' },
  { k: 'visible', label: '可见', w: '80px' },
  { k: 'status', label: '状态', w: '80px' }
]

const flat = computed(() => {
  const out = []
  const walk = (list, depth) => {
    for (const m of list) {
      out.push({ ...m, depth, kids: (m.children || []).length > 0 })
      if ((m.children || []).length && exp.value.has(m.id)) walk(m.children, depth + 1)
    }
  }
  walk(tree.value, 0)
  return out
})
const parentOpts = computed(() => {
  const out = []
  const walk = (list, depth) => {
    for (const m of list) {
      if (m.type !== 'F') out.push({ ...m, depth, pad: '　'.repeat(depth) })
      if (m.children) walk(m.children, depth + 1)
    }
  }
  walk(tree.value, 0)
  return out
})

const form = reactive({ id: null, parentId: 0, type: 'C', name: '', path: '', component: '', perm: '', icon: '', sort: 0, visibleOn: true })

function tgExp(id) {
  const s = new Set(exp.value)
  s.has(id) ? s.delete(id) : s.add(id)
  exp.value = s
}
function openForm(row) {
  Object.assign(form, {
    id: row?.id || null, parentId: row?.parentId ?? 0, type: row?.type || 'C', name: row?.name || '',
    path: row?.path || '', component: row?.component || '', perm: row?.perm || '', icon: row?.icon || '',
    sort: row?.sort || 0, visibleOn: row ? row.visible === '0' : true
  })
  formOpen.value = true
}
async function save() {
  if (!form.name) { toast('名称必填', 'err'); return }
  const body = {
    parentId: form.parentId, type: form.type, name: form.name, path: form.path || null,
    component: form.component || null, perm: form.perm || null, icon: form.icon || null,
    sort: Number(form.sort) || 0, visible: form.visibleOn ? '0' : '1', status: '0'
  }
  try {
    form.id ? await api.updateMenu(form.id, body) : await api.addMenu(body)
    toast('已保存')
    formOpen.value = false
    load()
  } catch (e) { toast(e.message, 'err') }
}
async function remove(row) {
  if (!confirm(`确定删除「${row.name}」吗?`)) return
  try {
    await api.delMenu(row.id)
    toast('已删除')
    load()
  } catch (e) { toast(e.message, 'err') }
}
async function load() { tree.value = await api.menus() }
onMounted(load)
</script>

<style scoped>
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
.st-line { display: flex; align-items: center; gap: 10px; height: 36px; }
.nat { width: 100%; height: 36px; border-radius: 999px; border: 1px solid var(--line); background: var(--card); color: var(--txt); padding: 0 14px; outline: none; font-family: inherit; }
</style>
