<template>
  <div class="page">
    <div class="page-head">
      <div>
        <div class="page-title">角色管理</div>
        <div class="page-sub">角色决定用户可见的菜单、可用的按钮以及可查看的数据范围。</div>
      </div>
      <div class="head-actions">
        <ATabs v-model="tab" :tabs="[{ k: 'list', label: '列表' }, { k: 'matrix', label: '矩阵' }, { k: 'trace', label: '溯源' }]" />
        <ABtn variant="blue" icon="plus" @click="openForm()">新增角色</ABtn>
      </div>
    </div>

    <!-- 列表 -->
    <template v-if="tab === 'list'">
      <div class="card card-pad" style="margin-bottom: 14px">
        <div class="filter-bar">
          <AInput v-model="q.kw" icon="search" placeholder="角色名 / 角色码" width="220px" @enter="load" />
          <ASelect v-model="q.status" :options="[{ label: '全部状态', value: '' }, { label: '正常', value: '0' }, { label: '停用', value: '1' }]" />
          <div class="grow" />
          <ABtn variant="ghost" @click="q.kw = ''; q.status = ''; load()">重置</ABtn>
          <ABtn icon="search" @click="load">查询</ABtn>
        </div>
      </div>
      <ATable :cols="cols" :rows="rows">
        <template #c_name="{ row }">
          <ATag tone="violet" :dot="true">{{ row.name }}</ATag>
        </template>
        <template #c_code="{ row }"><span class="chip code">{{ row.code }}</span></template>
        <template #c_dataScope="{ row }"><ATag :tone="scopeTone(row.dataScope)">{{ scopeLabel(row.dataScope) }}</ATag></template>
        <template #c_userCount="{ row }"><b>{{ row.userCount }}</b> <span class="tiny muted">人</span></template>
        <template #c_status="{ row }">
          <ATag :tone="row.status === '0' ? 'green' : 'red'" :dot="true">{{ row.status === '0' ? '正常' : '停用' }}</ATag>
        </template>
        <template #actions="{ row }">
          <div class="acts">
            <button class="ib" title="编辑" @click="openForm(row)"><AIcon name="edit" :size="15" /></button>
            <button class="ib" title="分配权限" @click="openPerm(row)"><AIcon name="role" :size="15" /></button>
            <button class="ib danger" title="删除" @click="remove(row)"><AIcon name="trash" :size="15" /></button>
          </div>
        </template>
      </ATable>
      <APager :total="total" :page="page" :size="size" @change="p => { page = p.page; size = p.size; load() }" />
    </template>

    <!-- 矩阵 -->
    <div v-else-if="tab === 'matrix'" class="card matrix">
      <table>
        <thead>
          <tr>
            <th>角色 \ 页面</th>
            <th v-for="m in matrixPages" :key="m.id">{{ m.name }}</th>
            <th>按钮权限</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="r in matrix.roles" :key="r.id">
            <td class="m-role"><ATag tone="violet" :dot="true">{{ r.name }}</ATag></td>
            <td v-for="m in matrixPages" :key="m.id" style="text-align: center">
              <span v-if="has(r.id, m.id)" class="mk on"><AIcon name="check" :size="13" /></span>
              <span v-else class="mk" />
            </td>
            <td style="text-align: center"><b>{{ r.btnCount || 0 }}</b></td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- 溯源 -->
    <div v-else class="card star-wrap">
      <StarMap :data="star" />
    </div>

    <!-- 编辑 -->
    <AModal v-model="formOpen" :title="form.id ? '编辑角色' : '新增角色'" width="480px">
      <div class="fm">
        <div class="fm-2">
          <div class="fm-g"><label>角色名称</label><AInput v-model="form.name" placeholder="如:系统管理员" /></div>
          <div class="fm-g"><label>角色码</label><AInput v-model="form.code" placeholder="如 sys_admin" /></div>
        </div>
        <div class="fm-2">
          <div class="fm-g"><label>数据权限</label>
            <ASelect v-model="form.dataScope" :options="scopeOpts" width="100%" />
          </div>
          <div class="fm-g"><label>排序</label><AInput v-model="form.sort" type="number" /></div>
        </div>
        <div class="fm-g"><label>状态</label>
          <div class="st-line"><ASwitch v-model="form.statusOn" /><span class="tiny muted">{{ form.statusOn ? '正常' : '停用' }}</span></div>
        </div>
        <div class="fm-g"><label>备注</label><AInput v-model="form.remark" placeholder="角色说明" /></div>
      </div>
      <template #footer>
        <ABtn variant="ghost" @click="formOpen = false">取消</ABtn>
        <ABtn variant="blue" @click="save">保存</ABtn>
      </template>
    </AModal>

    <!-- 权限 -->
    <ADrawer v-model="permOpen" :title="'分配权限 · ' + (permRole?.name || '')" width="420px">
      <div class="ptool tiny muted">
        <span>勾选该角色可访问的菜单与按钮</span>
        <button class="lnk2" @click="checkAllMenu">全选</button>
      </div>
      <div class="mtree">
        <div v-for="n in menuFlat" :key="n.id" class="mn" :class="'t' + n.depth" :style="{ paddingLeft: 4 + n.depth * 18 + 'px' }">
          <label class="mn-l">
            <input type="checkbox" :checked="permSet.has(n.id)" @change="tg(n.id)" />
            <span class="mn-tag" :data-t="n.type">{{ n.type === 'M' ? '目录' : n.type === 'C' ? '页面' : '按钮' }}</span>
            <span>{{ n.name }}</span>
            <span v-if="n.perm" class="chip code">{{ n.perm }}</span>
          </label>
        </div>
      </div>
      <template #footer>
        <div style="display: flex; gap: 10px; justify-content: flex-end">
          <ABtn variant="ghost" @click="permOpen = false">取消</ABtn>
          <ABtn variant="blue" @click="savePerm">保存授权</ABtn>
        </div>
      </template>
    </ADrawer>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import AIcon from '../../ui/AIcon.vue'
import ABtn from '../../ui/ABtn.vue'
import AInput from '../../ui/AInput.vue'
import ASelect from '../../ui/ASelect.vue'
import ASwitch from '../../ui/ASwitch.vue'
import ATag from '../../ui/ATag.vue'
import AModal from '../../ui/AModal.vue'
import ADrawer from '../../ui/ADrawer.vue'
import ATable from '../../ui/ATable.vue'
import APager from '../../ui/APager.vue'
import ATabs from '../../ui/ATabs.vue'
import StarMap from '../../viz/StarMap.vue'
import { api } from '../../api'
import { toast } from '../../ui/toast'

const tab = ref('list')
const rows = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const q = reactive({ kw: '', status: '' })
const matrix = ref({ roles: [], pairs: [] })
const star = ref(null)

const scopeOpts = [
  { label: '全部数据', value: 1 }, { label: '自定义', value: 2 }, { label: '本部门', value: 3 },
  { label: '本部门及以下', value: 4 }, { label: '仅本人', value: 5 }
]
const scopeLabel = v => (scopeOpts.find(o => o.value === v) || {}).label || v
const scopeTone = v => ({ 1: 'green', 2: 'amber', 3: 'blue', 4: 'teal', 5: 'gray' }[v] || 'gray')

const cols = [
  { k: 'name', label: '角色' },
  { k: 'code', label: '角色码' },
  { k: 'dataScope', label: '数据权限' },
  { k: 'userCount', label: '持有用户', w: '100px' },
  { k: 'status', label: '状态', w: '90px' },
  { k: 'remark', label: '备注' }
]

const formOpen = ref(false)
const form = reactive({ id: null, name: '', code: '', sort: 0, dataScope: 1, statusOn: true, remark: '' })
const permOpen = ref(false)
const permRole = ref(null)
const permSet = ref(new Set())
const menuTree = ref([])

const menuFlat = computed(() => {
  const out = []
  const walk = (list, depth) => {
    for (const m of list) {
      out.push({ ...m, depth })
      if (m.children && m.children.length) walk(m.children, depth + 1)
    }
  }
  walk(menuTree.value, 0)
  return out
})

const matrixPages = computed(() => menuFlat.value.filter(m => m.type === 'C'))
function has(rid, mid) {
  const r = matrix.value.roles.find(x => x.id === rid)
  if (r && r.builtin === 1) return true
  return matrix.value.pairs.some(p => Number(p.rid) === rid && Number(p.mid) === mid)
}

async function load() {
  const d = await api.roles({ page: page.value, size: size.value, kw: q.kw, status: q.status })
  rows.value = d.rows
  total.value = d.total
}

function openForm(row) {
  Object.assign(form, {
    id: row?.id || null, name: row?.name || '', code: row?.code || '', sort: row?.sort || 0,
    dataScope: row?.dataScope || 1, statusOn: row ? row.status === '0' : true, remark: row?.remark || ''
  })
  formOpen.value = true
}
async function save() {
  if (!form.name || !form.code) { toast('名称和角色码必填', 'err'); return }
  const body = { name: form.name, code: form.code, sort: Number(form.sort) || 0, dataScope: form.dataScope, status: form.statusOn ? '0' : '1', remark: form.remark }
  try {
    form.id ? await api.updateRole(form.id, body) : await api.addRole(body)
    toast('已保存')
    formOpen.value = false
    load()
  } catch (e) { toast(e.message, 'err') }
}
async function remove(row) {
  if (!confirm(`确定删除角色「${row.name}」吗?`)) return
  try {
    await api.delRole(row.id)
    toast('已删除')
    load()
  } catch (e) { toast(e.message, 'err') }
}

async function openPerm(row) {
  permRole.value = row
  const [ids, menus] = await Promise.all([api.roleMenus(row.id), api.menus()])
  menuTree.value = menus
  permSet.value = new Set(ids)
  permOpen.value = true
}
function tg(id) {
  const s = new Set(permSet.value)
  s.has(id) ? s.delete(id) : s.add(id)
  permSet.value = s
}
function checkAllMenu() {
  permSet.value = new Set(menuFlat.value.filter(m => m.type !== 'F').map(m => m.id))
}
async function savePerm() {
  try {
    await api.assignMenus(permRole.value.id, [...permSet.value])
    toast('授权已保存')
    permOpen.value = false
    load()
  } catch (e) { toast(e.message, 'err') }
}

async function loadTab() {
  if (tab.value === 'matrix') {
    const [m, menus] = await Promise.all([api.roleMatrix(), api.menus()])
    matrix.value = m
    menuTree.value = menus
  } else if (tab.value === 'trace') {
    star.value = await api.starmap()
  } else load()
}
watch(tab, loadTab)

onMounted(load)
</script>

<style scoped>
.acts { display: inline-flex; gap: 4px; }
.ib {
  width: 29px; height: 29px; border-radius: 9px; border: 0; background: var(--card2);
  color: var(--txt2); cursor: pointer; display: inline-flex; align-items: center; justify-content: center; transition: all 0.2s;
}
.ib:hover { background: var(--acc-soft); color: var(--acc-deep); }
.ib.danger:hover { background: var(--red-soft); color: var(--red); }
.matrix { overflow: auto; }
.matrix table { width: 100%; border-collapse: collapse; font-size: 13px; min-width: 720px; }
.matrix th { padding: 13px 14px; text-align: left; color: var(--txt3); font-size: 12px; border-bottom: 1px solid var(--line2); white-space: nowrap; }
.matrix td { padding: 11px 14px; border-bottom: 1px solid var(--line2); }
.matrix tbody tr:hover { background: var(--card2); }
.m-role { white-space: nowrap; }
.mk { display: inline-flex; width: 22px; height: 22px; border-radius: 7px; background: var(--card2); color: transparent; align-items: center; justify-content: center; }
.mk.on { background: var(--acc-soft); color: var(--acc-deep); }
.star-wrap { position: relative; overflow: hidden; min-height: 560px; }
.rp {
  position: absolute; top: 18px; right: 18px; width: 320px; max-height: calc(100% - 36px); overflow: auto;
  background: var(--card); border: 1px solid var(--line); border-radius: 18px;
  box-shadow: var(--shadow-lg); padding: 18px; z-index: 5;
}
.rp-hd { display: flex; align-items: center; gap: 8px; margin-bottom: 8px; }
.rp-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 12px; margin: 16px 0; }
.rp-k { background: var(--card2); border-radius: 12px; padding: 10px 12px; display: flex; flex-direction: column; gap: 4px; align-items: flex-start; }
.rp-sec { margin-top: 6px; }
.rp-chips { display: flex; flex-direction: column; gap: 9px; }
.rp-chips .cp-u { display: flex; align-items: center; gap: 8px; font-size: 13px; }
.rp-chips i { font-style: normal; margin-left: auto; }
.ptool { display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px; }
.lnk2 { border: 0; background: transparent; color: var(--acc-deep); cursor: pointer; font-size: 12px; }
.mtree { max-height: 60vh; overflow: auto; border: 1px solid var(--line2); border-radius: 12px; padding: 6px; }
.mn-l { display: flex; align-items: center; gap: 8px; padding: 6px 8px; border-radius: 8px; cursor: pointer; font-size: 13px; }
.mn-l:hover { background: var(--card2); }
.mn-l input { accent-color: var(--acc); }
.mn-tag { font-size: 10.5px; padding: 1px 7px; border-radius: 5px; background: var(--card2); color: var(--txt3); border: 1px solid var(--line); }
.fm { display: flex; flex-direction: column; }
.fm-2 { display: grid; grid-template-columns: 1fr 1fr; gap: 14px; }
.fm-g { margin-bottom: 12px; }
.fm-g label { display: block; font-size: 12.5px; color: var(--txt2); margin-bottom: 7px; }
.fm-g :deep(.ai), .fm-g :deep(.as) { width: 100%; }
.st-line { display: flex; align-items: center; gap: 10px; height: 36px; }
</style>
