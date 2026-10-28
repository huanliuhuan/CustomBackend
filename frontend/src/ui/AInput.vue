<template>
  <div class="ai" :style="{ width }">
    <AIcon v-if="icon" :name="icon" :size="15" class="ai-ico" />
    <input
      :type="type" :value="modelValue" :placeholder="placeholder" :disabled="disabled"
      @input="$emit('update:modelValue', $event.target.value)"
      @keyup.enter="$emit('enter')" />
    <slot name="suffix" />
  </div>
</template>

<script setup>
import AIcon from './AIcon.vue'
defineProps({
  modelValue: [String, Number],
  placeholder: { type: String, default: '请输入' },
  icon: String,
  type: { type: String, default: 'text' },
  width: { type: String, default: 'auto' },
  disabled: Boolean
})
defineEmits(['update:modelValue', 'enter'])
</script>

<style scoped>
.ai {
  display: inline-flex; align-items: center; gap: 7px; height: 36px;
  background: var(--card); border: 1px solid var(--line); border-radius: 999px;
  padding: 0 14px; transition: all 0.2s;
}
.ai:focus-within { border-color: var(--acc); box-shadow: 0 0 0 3px var(--acc-soft); }
.ai-ico { color: var(--txt3); flex: none; }
input {
  border: 0; outline: 0; background: transparent; color: var(--txt);
  font-size: 13.5px; font-family: inherit; min-width: 0; flex: 1; height: 100%;
}
input::placeholder { color: var(--txt3); }
</style>
