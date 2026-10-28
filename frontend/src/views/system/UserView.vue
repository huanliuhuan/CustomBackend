<template>
  <div class="page">
    <div class="page-head">
      <div>
        <div class="page-title">用户管理</div>
        <div class="page-sub">维护后台账号,所属部门、岗位与角色;停用的账号无法登录。</div>
      </div>
      <div class="head-actions">
        <ATabs v-model="view" :tabs="[{ k: 'list', label: '列表' }, { k: 'board', label: '看板' }]" />
        <ABtn variant="blue" icon="plus" @click="openForm()">新增用户</ABtn>
      </div>
    </div>

    <div class="u-wrap">
      <div class="card dept-card">
        <div class="dc-search"><AInput v-model="deptKw" icon="search" placeholder="搜索部门" width="100%" /></div>
        <div class="dc-tree">
          <div v-for="d in deptFlatView" :key="d.id" class="tr" :class="{ sel: d.id === deptId }"
               :style="{ paddingLeft: 8 + d.depth * 16 + 'px' }" @click="deptId = deptId === d.id ? null : d.id">
            <span v-if="d.kids" class="tw" :class="{ open: d.open }" @click.stop="toggleDept(d.id)">
              <AIcon name="right" :size="12" />
            </span>
            <span v-else class="tw" />
            <AIcon name="dept" :size="14" class="muted" />
            <span class="tr-t">{{ d.name }}</span>
            <span class="tiny muted">{{ d.userCount }}</span>
          </div>
        </div>
      </div>

      <div class="u-main">
        <div class="card card-pad u-filter">
          <div class="filter-bar">
            <AInput v-model="q.kw" icon="search" placeholder="账号 / 姓名 / 手机号" width="230px" @enter="load" />
            <ASelect v-model="q.status" :options="[{ label: '全部状态', value: '' }, { label: '正常', value: '0' }, { label: '停用', value: '1' }]" />
            <div class="grow" />
            <ABtn variant="ghost" @click="reset">重置</ABtn>
            <ABtn icon="search" @click="load">查询</ABtn>
          </div>
        </div>

        <template v-if="view === 'list'">
          <ATable :cols="cols" :rows="rows" :total="total" check :picked="picked" @toggle-check="s => picked = s">
            <template #c_nickName="{ row }">
              <div class="ucell">
                <span class="avatar" :style="{ background: uc(row.nickName) }">{{ (row.nickName || '?')[0] }}</span>
                <div class="ucell-t">
                  <b>{{ row.nickName }}</b>
                  <span class="tiny muted">@{{ row.account }}</span>
                </div>
              </div>
            </template>
            <template #c_postNames="{ row }">
              <ATag v-for="p in spl(row.postNames)" :key="p" tone="blue" class="m-tag">{{ p }}</ATag>
            </template>
            <template #c_roleNames="{ row }">
              <ATag v-for="r in spl(row.roleNames)" :key="r" tone="violet" class="m-tag">{{ r }}</ATag>
              <span v-if="!row.roleNames" class="tiny muted">未分配</span>
            </template>
            <template #c_status="{ row }">
              <ASwitch :model-value="row.status === '0'" @update:model-value="v => toggleStatus(row, v)" />
            </template>
            <template #actions="{ row }">
              <div class="acts">
                <button class="ib" title="编辑" @click="openForm(row)"><AIcon name="edit" :size="15" /></button>
                <button class="ib" title="重置密码" @click="openPwd(row)"><AIcon name="key" :size="15" /></button>
                <button class="ib" title="分配角色" @click="openRoles(row)"><AIcon name="role" :size="15" /></button>
                <button class="ib danger" title="删除" @click="remove(row)"><AIcon name="trash" :size="15" /></button>
              </div>
            </template>
          </ATable>
          <APager :total="total" :page="page" :size="size" @change="onPage" />
        </template>

        <template v-else>
          <div class="board">
            <div v-for="r in rows" :key="r.id" class="bcard card">
              <div class="b-top">
                <span class="avatar" :style="{ background: uc(r.nickName) }">{{ (r.nickName || '?')[0] }}</span>
                <div><b>{{ r.nickName }}</b><div class="tiny muted">@{{ r.account }}</div></div>
                <ASwitch :model-value="r.status === '0'" @update:model-value="v => toggleStatus(r, v)" />
              </div>
              <div class="b-row tiny muted"><AIcon name="dept" :size="13" /> {{ r.deptName || '-' }}</div>
              <div class="b-row tiny muted"><AIcon name="post" :size="13" /> {{ r.postNames || '-' }}</div>
              <div class="b-row">
                <template v-if="r.roleNames"><ATag v-for="x in spl(r.roleNames)" :key="x" tone="violet">{{ x }}</ATag></template>
                <span v-else class="tiny muted">未分配角色</span>
              </div>
              <div class="b-foot tiny muted">{{ r.phone || '-' }}</div>
            </div>
          </div>
          <APager :total="total" :page="page" :size="size" @change="onPage" />
        </template>
      </div>
    </div>

    <AModal v-model="formOpen" :title="form.id ? '编辑用户' : '新增用户'" width="560px">
      <div class="fm">
        <div class="fm-g"><label>所属部门</label>
          <select v-model="form.deptId" class="nat">
            <option v-for="d in deptFlat" :key="d.id" :value="d.id">{{ d.pad }}{{ d.name }}</option>
          </select>
        </div>
        <div class="fm-2">
          <div class="fm-g"><label>账号</label><AInput v-model="form.account" :disabled="!!form.id" placeholder="登录账号" /></div>
          <div class="fm-g"><label>姓名</label><AInput v-model="form.nickName" placeholder="真实姓名" /></div>
        </div>
        <div class="fm-2">
          <div class="fm-g"><label>手机号</label><AInput v-model="form.phone" placeholder="手机号" /></div>
          <div class="fm-g"><label>邮箱</label><AInput v-model="form.email" placeholder="邮箱" /></div>
        </div>
        <div class="fm-2">
          <div class="fm-g"><label>性别</label>
            <ASelect v-model="form.sex" :options="sexOpts" width="100%" />
          </div>
          <div class="fm-g"><label>状态</label>
            <div class="st-line"><ASwitch v-model="form.statusOn" /><span class="tiny muted">{{ form.statusOn ? '正常' : '停用' }}</span></div>
          </div>
        </div>
        <div class="fm-g"><label>岗位</label>
          <div class="chips"><button v-for="p in posts" :key="p.id" class="chip c-pick" :class="{ on: form.postIds.includes(p.id) }" @click="tgl(form.postIds, p.id)">{{ p.name }}</button></div>
        </div>
        <div class="fm-g"><label>角色</label>
          <div class="chips"><button v-for="r in roleAll" :key="r.id" class="chip c-pick" :class="{ on: form.roleIds.includes(r.id) }" @click="tgl(form.roleIds, r.id)">{{ r.name }}</button></div>
        </div>
      </div>
      <template #footer>
        <ABtn variant="ghost" @click="formOpen = false">取消</ABtn>
        <ABtn variant="blue" @click="save">{{ form.id ? '保存修改' : '创建用户' }}</ABtn>
      </template>
    </AModal>

    <AModal v-model="pwdOpen" title="重置密码" width="400px">
      <div class="fm-g"><label>新密码(至少 6 位)</label><AInput v-model="pwdVal" icon="lock" type="password" placeholder="请输入新密码" /></div>
      <template #footer>
        <ABtn variant="ghost" @click="pwdOpen = false">取消</ABtn>
        <ABtn variant="blue" @click="doPwd">确认重置</ABtn>
      </template>
    </AModal>

    <AModal v-model="rolesOpen" title="分配角色" width="420px">
      <div class="chips"><button v-for="r in roleAll" :key="r.id" class="chip c-pick" :class="{ on: rolePick.includes(r.id) }" @click="tgl(rolePick, r.id)">{{ r.name }}</button></div>
      <template #footer>
        <ABtn variant="ghost" @click="rolesOpen = false">取消</ABtn>
        <ABtn variant="blue" @click="doRoles">保存</ABtn>
      </template>
    </AModal>
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
import ATable from '../../ui/ATable.vue'
import APager from '../../ui/APager.vue'
import ATabs from '../../ui/ATabs.vue'
import { api } from '../../api'
import { toast } from '../../ui/toast'

const view = ref('list')
const rows = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const q = reactive({ kw: '', status: '' })
const picked = ref(new Set())
const deptKw = ref('')
const deptId = ref(null)
const deptTree = ref([])
const deptOpen = ref(new Set())
const posts = ref([])
const roleAll = ref([])
const sexMap = ref({})

const cols = [
  { k: 'nickName', label: '用户' },
  { k: 'deptName', label: '部门' },
  { k: 'postNames', label: '岗位' },
  { k: 'roleNames', label: '角色' },
  { k: 'phone', label: '手机号' },
  { k: 'status', label: '状态', w: '70px' }
]

const deptFlat = computed(() => {
  const out = []
  const walk = (list, depth) => {
    for (const d of list) {
      out.push({ ...d, depth, pad: '　'.repeat(depth) })
      if (d.children && d.children.length) walk(d.children, depth + 1)
    }
  }
  walk(deptTree.value, 0)
  return out
})
const deptFlatView = computed(() => {
  const kw = deptKw.value.trim()
  const all = deptFlat.value
  const mk = d => ({ ...d, open: deptOpen.value.has(d.id), kids: (d.children || []).length > 0 })
  if (kw) return all.filter(d => d.name.includes(kw)).map(mk)
  return all
    .filter(d => d.depth === 0 || (d.ancestors || '').split(',').every(a => a === '0' || deptOpen.value.has(Number(a))))
    .map(mk)
})

function toggleDept(id) {
  const s = new Set(deptOpen.value)
  s.has(id) ? s.delete(id) : s.add(id)
  deptOpen.value = s
}

// 点部门节点 → 过滤右侧用户列表;再点一次取消
watch(deptId, () => {
  page.value = 1
  load()
})

const sexOpts = computed(() => {
  const list = sexMap.value.sys_user_sex || []
  return list.map(d => ({ label: d.label, value: d.value }))
})

const formOpen = ref(false)
const form = reactive({ id: null, deptId: null, account: '', nickName: '', phone: '', email: '', sex: '0', statusOn: true, postIds: [], roleIds: [] })
const pwdOpen = ref(false)
const pwdVal = ref('')
const pwdId = ref(null)
const rolesOpen = ref(false)
const rolePick = ref([])
const roleId = ref(null)

function spl(s) { return s ? s.split(',') : [] }
function uc(name) {
  const colors = ['#71b81f', '#3e7ef7', '#8b5cf6', '#e59819', '#0f9d8f', '#e5484d']
  let h = 0
  for (const c of name || '') h = (h * 31 + c.charCodeAt(0)) % 997
  return colors[h % colors.length]
}
function tgl(arr, id) {
  const i = arr.indexOf(id)
  i >= 0 ? arr.splice(i, 1) : arr.push(id)
}

async function load() {
  const d = await api.users({ page: page.value, size: size.value, kw: q.kw, status: q.status, deptId: deptId.value })
  rows.value = d.rows
  total.value = d.total
}
function reset() {
  q.kw = ''
  q.status = ''
  deptId.value = null
  page.value = 1
  load()
}
function onPage(p) {
  page.value = p.page
  size.value = p.size
  load()
}

function openForm(row) {
  Object.assign(form, {
    id: row?.id || null, deptId: row?.deptId || deptFlat.value[0]?.id, account: row?.account || '',
    nickName: row?.nickName || '', phone: row?.phone || '', email: row?.email || '',
    sex: row?.sex || '0', statusOn: row ? row.status === '0' : true, postIds: [], roleIds: []
  })
  if (row) {
    api.user(row.id).then(u => {
      form.postIds = u.postIds || []
      form.roleIds = u.roleIds || []
    })
  }
  formOpen.value = true
}

async function save() {
  if (!form.account || !form.nickName) { toast('账号和姓名必填', 'err'); return }
  const body = {
    deptId: form.deptId, account: form.account, nickName: form.nickName,
    phone: form.phone, email: form.email, sex: form.sex,
    status: form.statusOn ? '0' : '1', postIds: form.postIds, roleIds: form.roleIds
  }
  try {
    form.id ? await api.updateUser(form.id, body) : await api.addUser(body)
    toast(form.id ? '已保存' : '已创建,初始密码见参数配置')
    formOpen.value = false
    load()
  } catch (e) { toast(e.message, 'err') }
}

function openPwd(row) {
  pwdId.value = row.id
  pwdVal.value = ''
  pwdOpen.value = true
}
async function doPwd() {
  try {
    await api.resetPwd(pwdId.value, pwdVal.value)
    toast('密码已重置')
    pwdOpen.value = false
  } catch (e) { toast(e.message, 'err') }
}

function openRoles(row) {
  roleId.value = row.id
  api.user(row.id).then(u => { rolePick.value = [...(u.roleIds || [])] })
  rolesOpen.value = true
}
async function doRoles() {
  try {
    await api.assignRoles(roleId.value, rolePick.value)
    toast('角色已更新')
    rolesOpen.value = false
    load()
  } catch (e) { toast(e.message, 'err') }
}

async function toggleStatus(row, on) {
  try {
    await api.updateUser(row.id, { status: on ? '0' : '1' })
    toast(on ? '已启用' : '已停用')
    load()
  } catch (e) { toast(e.message, 'err') }
}

async function remove(row) {
  if (!confirm(`确定删除用户「${row.nickName}」吗?`)) return
  try {
    await api.delUsers([row.id])
    toast('已删除')
    load()
  } catch (e) { toast(e.message, 'err') }
}

onMounted(async () => {
  const [d, p, r, dm] = await Promise.all([api.depts(), api.allPosts(), api.allRoles(), api.dictMap()])
  deptTree.value = d
  deptOpen.value = new Set(d.flatMap(x => [x.id, ...(x.children || []).map(c => c.id)]))
  posts.value = p
  roleAll.value = r
  sexMap.value = dm
  load()
})
</script>

<style scoped>
.u-wrap { display: grid; grid-template-columns: 252px 1fr; gap: 16px; align-items: start; }
.dept-card { overflow: hidden; }
.dc-search { padding: 12px 12px 8px; }
.dc-tree { max-height: 560px; overflow: auto; padding: 4px 8px 12px; }
.tr { display: flex; align-items: center; gap: 6px; padding: 7px 8px; border-radius: 9px; cursor: pointer; color: var(--txt2); font-size: 13px; }
.tr:hover { background: var(--card2); color: var(--txt); }
.tr.sel { background: var(--acc-soft); color: var(--acc-deep); font-weight: 600; }
.tw { width: 14px; display: inline-flex; transition: transform 0.2s; color: var(--txt3); }
.tw.open { transform: rotate(90deg); }
.tr-t { flex: 1; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.u-main { display: flex; flex-direction: column; gap: 14px; min-width: 0; }
.ucell { display: flex; align-items: center; gap: 10px; }
.ucell-t { line-height: 1.3; }
.ucell-t b { display: block; font-size: 13.5px; }
.m-tag { margin: 2px 4px 2px 0; }
.acts { display: inline-flex; gap: 4px; }
.ib {
  width: 29px; height: 29px; border-radius: 9px; border: 0; background: var(--card2);
  color: var(--txt2); cursor: pointer; display: inline-flex; align-items: center; justify-content: center;
  transition: all 0.2s;
}
.ib:hover { background: var(--acc-soft); color: var(--acc-deep); transform: translateY(-1px); }
.ib.danger:hover { background: var(--red-soft); color: var(--red); }
.board { display: grid; grid-template-columns: repeat(auto-fill, minmax(230px, 1fr)); gap: 14px; }
.bcard { padding: 16px; transition: all 0.25s; }
.bcard:hover { transform: translateY(-3px); box-shadow: var(--shadow-lg); }
.b-top { display: flex; align-items: center; gap: 10px; margin-bottom: 12px; }
.b-top div { flex: 1; line-height: 1.3; }
.b-row { display: flex; align-items: center; gap: 6px; margin: 7px 0; flex-wrap: wrap; }
.b-foot { border-top: 1px dashed var(--line2); padding-top: 9px; margin-top: 9px; }
.fm { display: flex; flex-direction: column; gap: 4px; }
.fm-2 { display: grid; grid-template-columns: 1fr 1fr; gap: 14px; }
.fm-g { margin-bottom: 12px; }
.fm-g label { display: block; font-size: 12.5px; color: var(--txt2); margin-bottom: 7px; }
.fm-g :deep(.ai), .fm-g :deep(.as) { width: 100%; }
.nat {
  width: 100%; height: 36px; border-radius: 999px; border: 1px solid var(--line);
  background: var(--card); color: var(--txt); padding: 0 14px; outline: none; font-family: inherit;
}
.st-line { display: flex; align-items: center; gap: 10px; height: 36px; }
.chips { display: flex; flex-wrap: wrap; gap: 8px; }
.c-pick { cursor: pointer; font-family: inherit; transition: all 0.2s; }
.c-pick.on { background: var(--acc-soft); color: var(--acc-deep); border-color: var(--acc); font-weight: 600; }
</style>
