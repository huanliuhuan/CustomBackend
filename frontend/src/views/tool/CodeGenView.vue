<template>
  <div class="page">
    <div class="page-head">
      <div>
        <div class="page-title">代码生成</div>
        <div class="page-sub">导入数据表,生成业务模块开发骨架:实体字段、建表说明与前端表格列。</div>
      </div>
    </div>

    <div class="card card-pad" style="margin-bottom: 14px">
      <div class="filter-bar">
        <AInput v-model="kw" icon="search" placeholder="搜索表名" width="240px" @enter="load" />
        <div class="grow" />
        <ABtn variant="ghost" @click="kw = ''; load()">重置</ABtn>
        <ABtn icon="search" @click="load">查询</ABtn>
      </div>
    </div>

    <ATable :cols="cols" :rows="tables">
      <template #c_name="{ row }"><span class="chip code">{{ row.name }}</span></template>
      <template #actions="{ row }">
        <ABtn size="sm" variant="soft" icon="eye" @click="preview(row)">预览骨架</ABtn>
      </template>
    </ATable>

    <ADrawer v-model="pvOpen" :title="'开发骨架 · ' + pvTable" width="640px">
      <template v-for="s in snippets" :key="s.t">
        <div class="sn-t">{{ s.t }}</div>
        <pre class="sn">{{ s.c }}</pre>
      </template>
    </ADrawer>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import ABtn from '../../ui/ABtn.vue'
import AInput from '../../ui/AInput.vue'
import ATable from '../../ui/ATable.vue'
import ADrawer from '../../ui/ADrawer.vue'
import { api } from '../../api'
import { toast } from '../../ui/toast'

const kw = ref('')
const tables = ref([])
const pvOpen = ref(false)
const pvTable = ref('')
const snippets = ref([])

const cols = [
  { k: 'name', label: '表名' },
  { k: 'engine', label: '引擎', w: '110px' },
  { k: 'cmt', label: '注释' },
  { k: 'rowCnt', label: '行数(约)', w: '90px' },
  { k: 'ct', label: '创建时间', w: '170px', fmt: v => v ? String(v).replace('T', ' ').slice(0, 19) : '-' }
]

function pascal(s) {
  return s.split('_').filter(Boolean).map(x => x[0].toUpperCase() + x.slice(1)).join('')
}

async function preview(row) {
  const cols = await api.columns(row.name)
  pvTable.value = row.name
  const ent = pascal(row.name)
  const fields = cols.filter(c => c.name !== 'id').map(c => {
    const t = /int|bigint|decimal/i.test(c.ctype) ? 'Integer' : /date|time/i.test(c.ctype) ? 'Date' : 'String'
    const camel = c.name.replace(/_(\w)/g, (_, x) => x.toUpperCase())
    return `    /** ${c.cmt || camel} */\n    private ${t} ${camel};`
  }).join('\n')
  const vueCols = cols.slice(0, 8).map(c => `  { k: '${c.name.replace(/_(\w)/g, (_, x) => x.toUpperCase())}', label: '${c.cmt || c.name}' },`).join('\n')
  snippets.value = [
    { t: '实体类 ' + ent + '.java', c: `public class ${ent} {\n    private Long id;\n${fields}\n}` },
    { t: '前端表格列 table.js', c: `const cols = [\n${vueCols}\n]` },
    { t: '权限码建议', c: `${row.name.replace(/_/g, ':')}:list / add / edit / remove` }
  ]
  pvOpen.value = true
}

async function load() {
  tables.value = await api.tables(kw.value)
}
onMounted(() => { load().catch(e => toast(e.message, 'err')) })
</script>

<style scoped>
.sn-t { font-size: 13px; font-weight: 600; margin: 16px 0 8px; }
.sn {
  background: var(--card2); border: 1px solid var(--line2); border-radius: 12px;
  padding: 14px 16px; font-family: Consolas, monospace; font-size: 12.5px; line-height: 1.7;
  overflow: auto; color: var(--txt2); white-space: pre-wrap;
}
</style>
