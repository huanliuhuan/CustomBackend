<template>
  <div class="seg-tab"><button v-for="t in tabs" :key="t.k" :class="{ on: t.k === modelValue }" @click="$emit('update:modelValue', t.k)">{{ t.label }}</button></div>
</template>

<script setup>
defineProps({ tabs: { type: Array, default: () => [] }, modelValue: String })
defineEmits(['update:modelValue'])
</script>

<style scoped>
.seg-tab { display: inline-flex; background: var(--card2); border: 1px solid var(--line); border-radius: 999px; padding: 3px; gap: 2px; }
button {
  border: 0; background: transparent; color: var(--txt2); padding: 6px 18px;
  border-radius: 999px; cursor: pointer; font-size: 13px; transition: all 0.25s; font-family: inherit;
}
button.on { background: var(--dark-btn); color: var(--bg); font-weight: 600; box-shadow: var(--shadow); }
:root[data-theme='dark'] button.on { color: #0d120d; }
</style>
