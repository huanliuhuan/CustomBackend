<template>
  <div class="tb card">
    <table>
      <thead>
        <tr>
          <th v-if="check" class="w-ck"><input type="checkbox" :checked="allCk" @change="toggleAll" /></th>
          <th v-for="c in cols" :key="c.k" :style="{ width: c.w, textAlign: c.align || 'left' }">{{ c.label }}</th>
          <th v-if="$slots.actions" style="width: 130px; text-align: right">操作</th>
        </tr>
      </thead>
      <tbody>
        <template v-for="r in rows" :key="String(r.id ?? r.$idx)">
          <tr :class="{ clickable }" @click="clickable && $emit('row-click', r)">
            <td v-if="check" class="w-ck" @click.stop>
              <input type="checkbox" :checked="picked.has(r.id)" @change="toggle(r.id)" />
            </td>
            <td v-for="c in cols" :key="c.k" :style="{ textAlign: c.align || 'left' }">
              <slot :name="'c_' + c.k" :row="r">
                <span v-if="c.tag" class="mini-tag" :data-tone="String(fmt(r, c))">{{ fmt(r, c) }}</span>
                <span v-else>{{ fmt(r, c) }}</span>
              </slot>
            </td>
            <td v-if="$slots.actions" style="text-align: right" @click.stop>
              <slot name="actions" :row="r" />
            </td>
          </tr>
          <tr v-if="expandable && expanded.has(r.id)">
            <td :colspan="cols.length + ($slots.actions ? 2 : 1)" class="expand-cell">
              <slot name="expand" :row="r" />
            </td>
          </tr>
        </template>
        <tr v-if="!rows.length">
          <td :colspan="cols.length + 2" class="empty">
            <div class="e-ico">◍</div>
            <div class="muted">{{ loading ? '加载中…' : '暂无数据' }}</div>
          </td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  cols: { type: Array, default: () => [] },
  rows: { type: Array, default: () => [] },
  check: Boolean,
  loading: Boolean,
  expandable: Boolean,
  clickable: Boolean,
  expanded: { type: Set, default: () => new Set() },
  picked: { type: Set, default: () => new Set() }
})
const emit = defineEmits(['toggle-check', 'toggle-expand', 'row-click'])

const allCk = computed(() => props.rows.length > 0 && props.rows.every(r => props.picked.has(r.id)))

function fmt(r, c) {
  let v = r[c.k]
  if (c.fmt) v = c.fmt(v, r)
  return v === null || v === undefined ? '-' : v
}
function toggle(id) {
  const s = new Set(props.picked)
  s.has(id) ? s.delete(id) : s.add(id)
  emit('toggle-check', s)
}
function toggleAll() {
  const s = new Set(allCk.value ? [] : props.rows.map(r => r.id))
  emit('toggle-check', s)
}
</script>

<style scoped>
.tb { overflow: hidden; }
table { width: 100%; border-collapse: collapse; font-size: 13.5px; }
thead th {
  text-align: left; padding: 13px 16px; color: var(--txt3); font-size: 12px; font-weight: 500;
  letter-spacing: 0.6px; border-bottom: 1px solid var(--line2); white-space: nowrap;
}
tbody td { padding: 13px 16px; border-bottom: 1px solid var(--line2); vertical-align: middle; }
tbody tr:last-child td { border-bottom: 0; }
tbody tr { transition: background 0.18s; }
tbody tr:hover:not(:has(.expand-cell)) { background: var(--card2); }
tr.clickable { cursor: pointer; }
.w-ck { width: 38px; text-align: center; }
.w-ck input { accent-color: var(--acc); width: 15px; height: 15px; cursor: pointer; }
.expand-cell { background: var(--card2); padding: 0 !important; }
.empty { text-align: center; padding: 48px 0 !important; }
.e-ico { font-size: 34px; color: var(--txt3); margin-bottom: 8px; }
.mini-tag {
  display: inline-block; padding: 2px 10px; border-radius: 999px; font-size: 12px;
  background: var(--card2); color: var(--txt2); border: 1px solid var(--line);
}
.mini-tag[data-tone='0'], .mini-tag[data-tone='正常'], .mini-tag[data-tone='成功'] { background: var(--acc-soft); color: var(--acc-deep); border: 0; }
.mini-tag[data-tone='1'], .mini-tag[data-tone='停用'], .mini-tag[data-tone='失败'] { background: var(--red-soft); color: var(--red); border: 0; }
</style>
