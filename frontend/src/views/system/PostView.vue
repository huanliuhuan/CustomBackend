<template>
  <div class="page">
    <div class="page-head">
      <div>
        <div class="page-title">岗位管理</div>
        <div class="page-sub">用户的职务头衔,在用户管理中分配。</div>
      </div>
      <div class="head-actions"><ABtn variant="blue" icon="plus" @click="openForm()">新增岗位</ABtn></div>
    </div>

    <div class="card card-pad" style="margin-bottom: 14px">
      <div class="filter-bar">
        <AInput v-model="q.kw" icon="search" placeholder="岗位名 / 编码" width="220px" @enter="load" />
        <div class="grow" />
        <ABtn variant="ghost" @click="q.kw = ''; load()">重置</ABtn>
        <ABtn icon="search" @click="load">查询</ABtn>
      </div>
    </div>

    <ATable :cols="cols" :rows="rows">
      <template #c_status="{ row }"><ATag :tone="row.status === '0' ? 'green' : 'red'" :dot="true">{{ row.status === '0' ? '正常' : '停用' }}</ATag></template>
      <template #actions="{ row }">
        <div class="acts">
          <button class="ib" @click="openForm(row)"><AIcon name="edit" :size="15" /></button>
          <button class="ib danger" @click="remove(row)"><AIcon name="trash" :size="15" /></button>
        </div>
      </template>
    </ATable>
    <APager :total="total" :page="page" :size="size" @change="p => { page = p.page; size = p.size; load() }" />

    <AModal v-model="formOpen" :title="form.id ? '编辑岗位' : '新增岗位'" width="420px">
      <div class="fm">
        <div class="fm-2">
          <div class="fm-g"><label>岗位名称</label><AInput v-model="form.name" /></div>
          <div class="fm-g"><label>岗位编码</label><AInput v-model="form.code" /></div>
        </div>
        <div class="fm-2">
          <div class="fm-g"><label>排序</label><AInput v-model="form.sort" type="number" /></div>
          <div class="fm-g"><label>状态</label>
            <div class="st-line"><ASwitch v-model="form.statusOn" /><span class="tiny muted">{{ form.statusOn ? '正常' : '停用' }}</span></div>
          </div>
        </div>
        <div class="fm-g"><label>备注</label><AInput v-model="form.remark" /></div>
      </div>
      <template #footer>
        <ABtn variant="ghost" @click="formOpen = false">取消</ABtn>
        <ABtn variant="blue" @click="save">保存</ABtn>
      </template>
    </AModal>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import AIcon from '../../ui/AIcon.vue'
import ABtn from '../../ui/ABtn.vue'
import AInput from '../../ui/AInput.vue'
import ASwitch from '../../ui/ASwitch.vue'
import ATag from '../../ui/ATag.vue'
import AModal from '../../ui/AModal.vue'
import ATable from '../../ui/ATable.vue'
import APager from '../../ui/APager.vue'
import { api } from '../../api'
import { toast } from '../../ui/toast'

const rows = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const q = reactive({ kw: '' })
const formOpen = ref(false)
const form = reactive({ id: null, name: '', code: '', sort: 0, statusOn: true, remark: '' })

const cols = [
  { k: 'name', label: '岗位名称' },
  { k: 'code', label: '岗位编码' },
  { k: 'sort', label: '排序', w: '70px' },
  { k: 'status', label: '状态', w: '80px' },
  { k: 'remark', label: '备注' }
]

async function load() {
  const d = await api.posts({ page: page.value, size: size.value, kw: q.kw })
  rows.value = d.rows
  total.value = d.total
}
function openForm(row) {
  Object.assign(form, { id: row?.id || null, name: row?.name || '', code: row?.code || '', sort: row?.sort || 0, statusOn: row ? row.status === '0' : true, remark: row?.remark || '' })
  formOpen.value = true
}
async function save() {
  if (!form.name || !form.code) { toast('名称和编码必填', 'err'); return }
  const body = { name: form.name, code: form.code, sort: Number(form.sort) || 0, status: form.statusOn ? '0' : '1', remark: form.remark }
  try {
    form.id ? await api.updatePost(form.id, body) : await api.addPost(body)
    toast('已保存')
    formOpen.value = false
    load()
  } catch (e) { toast(e.message, 'err') }
}
async function remove(row) {
  if (!confirm(`确定删除岗位「${row.name}」吗?`)) return
  try {
    await api.delPost(row.id)
    toast('已删除')
    load()
  } catch (e) { toast(e.message, 'err') }
}
onMounted(load)
</script>

<style scoped>
.acts { display: inline-flex; gap: 4px; }
.ib { width: 29px; height: 29px; border-radius: 9px; border: 0; background: var(--card2); color: var(--txt2); cursor: pointer; display: inline-flex; align-items: center; justify-content: center; transition: all 0.2s; }
.ib:hover { background: var(--acc-soft); color: var(--acc-deep); }
.ib.danger:hover { background: var(--red-soft); color: var(--red); }
.fm-2 { display: grid; grid-template-columns: 1fr 1fr; gap: 14px; }
.fm-g { margin-bottom: 12px; }
.fm-g label { display: block; font-size: 12.5px; color: var(--txt2); margin-bottom: 7px; }
.fm-g :deep(.ai) { width: 100%; }
.st-line { display: flex; align-items: center; gap: 10px; height: 36px; }
</style>
