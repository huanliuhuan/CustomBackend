<template>
  <div class="pg">
    <span class="tiny muted">共 {{ total }} 条,当前 {{ page }}/{{ pages }} 页</span>
    <div class="p-ct">
      <ASelect :model-value="size" :options="sizeOpts" width="104px" @change="s => $emit('change', { page: 1, size: s })" />
      <div class="p-btns">
        <button :disabled="page <= 1" @click="$emit('change', { page: page - 1, size })"><AIcon name="left" :size="14" /></button>
        <button class="cur">{{ page }}</button>
        <button :disabled="page >= pages" @click="$emit('change', { page: page + 1, size })"><AIcon name="right" :size="14" /></button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import AIcon from './AIcon.vue'
import ASelect from './ASelect.vue'

const props = defineProps({
  total: { type: Number, default: 0 },
  page: { type: Number, default: 1 },
  size: { type: Number, default: 10 }
})
defineEmits(['change'])
const sizeOpts = [10, 20, 50].map(v => ({ label: v + ' 条/页', value: v }))
const pages = computed(() => Math.max(1, Math.ceil(props.total / props.size)))
</script>

<style scoped>
.pg { display: flex; align-items: center; justify-content: space-between; padding: 14px 4px 2px; gap: 12px; flex-wrap: wrap; }
.p-ct { display: flex; align-items: center; gap: 12px; }
.p-btns { display: flex; gap: 6px; }
.p-btns button {
  min-width: 30px; height: 30px; border-radius: 9px; border: 1px solid var(--line);
  background: var(--card); color: var(--txt2); cursor: pointer;
  display: inline-flex; align-items: center; justify-content: center; transition: all 0.2s;
}
.p-btns button:hover:not(:disabled) { border-color: var(--acc); color: var(--acc-deep); }
.p-btns button:disabled { opacity: 0.4; cursor: not-allowed; }
.p-btns .cur { background: var(--dark-btn); color: var(--bg); border-color: transparent; font-weight: 600; }
:root[data-theme='dark'] .p-btns .cur { color: #0d120d; }
</style>
