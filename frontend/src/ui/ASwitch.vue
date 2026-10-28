<template>
  <button type="button" class="sw" :class="{ on }" @click="$emit('update:modelValue', !on)">
    <i />
  </button>
</template>

<script setup>
import { computed } from 'vue'
const props = defineProps({ modelValue: { type: Boolean, default: false } })
defineEmits(['update:modelValue'])
const on = computed(() => props.modelValue)
</script>

<style scoped>
.sw {
  width: 40px; height: 22px; border-radius: 999px; border: 0; cursor: pointer;
  background: var(--line); position: relative; transition: background 0.25s; flex: none;
}
.sw i {
  position: absolute; top: 2px; left: 2px; width: 18px; height: 18px; border-radius: 50%;
  background: #fff; transition: all 0.25s cubic-bezier(0.3, 1.4, 0.5, 1);
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.25);
}
.sw.on { background: var(--acc); }
.sw.on i { left: 20px; }
</style>
