<template>
  <div class="as" :class="{ open, up }" tabindex="0" @blur="open = false">
    <div class="as-face" @click="open = !open">
      <span :class="{ ph: !label }">{{ label || placeholder }}</span>
      <AIcon name="down2" :size="14" class="car" />
    </div>
    <transition name="pop">
      <div v-if="open" class="as-list" :style="{ minWidth: w }">
        <div v-for="o in options" :key="String(o.value)" class="as-opt" :class="{ on: o.value === modelValue }"
             @click="pick(o)">
          <span>{{ o.label }}</span>
          <AIcon v-if="o.value === modelValue" name="check" :size="13" />
        </div>
      </div>
    </transition>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import AIcon from './AIcon.vue'

const props = defineProps({
  modelValue: [String, Number],
  options: { type: Array, default: () => [] },
  placeholder: { type: String, default: '请选择' },
  width: { type: String, default: '160px' },
  up: Boolean
})
const emit = defineEmits(['update:modelValue', 'change'])
const open = ref(false)
const w = computed(() => props.width)
const label = computed(() => {
  const hit = props.options.find(o => o.value === props.modelValue)
  return hit ? hit.label : ''
})
function pick(o) {
  emit('update:modelValue', o.value)
  emit('change', o.value)
  open.value = false
}
</script>

<style scoped>
.as { position: relative; display: inline-block; outline: none; }
.as-face {
  display: flex; align-items: center; justify-content: space-between; gap: 10px;
  height: 36px; padding: 0 14px; min-width: 140px; cursor: pointer;
  background: var(--card); border: 1px solid var(--line); border-radius: 999px;
  font-size: 13.5px; color: var(--txt); transition: border-color 0.2s;
}
.as-face:hover, .open .as-face { border-color: var(--acc); }
.ph { color: var(--txt3); }
.car { color: var(--txt3); transition: transform 0.25s; }
.open .car { transform: rotate(180deg); }
.as-list {
  position: absolute; top: calc(100% + 6px); left: 0; z-index: 60;
  background: var(--card); border: 1px solid var(--line); border-radius: 14px;
  box-shadow: var(--shadow-lg); padding: 6px; max-height: 280px; overflow: auto;
}
.up .as-list { top: auto; bottom: calc(100% + 6px); }
.as-opt {
  display: flex; align-items: center; justify-content: space-between; gap: 12px;
  padding: 8px 12px; border-radius: 9px; cursor: pointer; font-size: 13px; color: var(--txt2);
  white-space: nowrap;
}
.as-opt:hover { background: var(--card2); color: var(--txt); }
.as-opt.on { color: var(--acc-deep); font-weight: 600; background: var(--acc-soft); }
</style>
