<template>
  <button class="ab" :class="['v-' + variant, 'sz-' + size, { block, icon: iconOnly }]" :disabled="disabled" @click="$emit('click', $event)">
    <AIcon v-if="icon && !iconOnly" :name="icon" :size="size === 'sm' ? 14 : 16" />
    <slot />
  </button>
</template>

<script setup>
import AIcon from './AIcon.vue'
defineProps({
  variant: { type: String, default: 'dark' },
  size: { type: String, default: 'md' },
  icon: String,
  iconOnly: Boolean,
  block: Boolean,
  disabled: Boolean
})
defineEmits(['click'])
</script>

<style scoped>
.ab {
  display: inline-flex; align-items: center; justify-content: center; gap: 6px;
  border: 1px solid transparent; border-radius: 999px; cursor: pointer;
  font-size: 13.5px; font-family: inherit; transition: all 0.22s ease; white-space: nowrap;
  padding: 0 18px; height: 36px;
}
.sz-sm { height: 30px; padding: 0 13px; font-size: 12.5px; }
.sz-lg { height: 44px; padding: 0 26px; font-size: 15px; }
.icon { width: 34px; padding: 0; }
.block { width: 100%; }
.ab:disabled { opacity: 0.5; cursor: not-allowed; }
.v-dark { background: var(--dark-btn); color: var(--bg); }
:root[data-theme='dark'] .v-dark { color: #0d120d; }
.v-dark:hover:not(:disabled) { background: var(--dark-btn-h); transform: translateY(-1px); }
.v-blue { background: var(--blue); color: #fff; }
.v-blue:hover:not(:disabled) { filter: brightness(1.1); transform: translateY(-1px); }
.v-ghost { background: transparent; border-color: var(--line); color: var(--txt2); }
.v-ghost:hover:not(:disabled) { color: var(--txt); border-color: var(--txt3); }
.v-soft { background: var(--acc-soft); color: var(--acc-deep); }
.v-soft:hover:not(:disabled) { filter: brightness(1.05); }
.v-danger { background: transparent; border-color: var(--line); color: var(--red); }
.v-danger:hover:not(:disabled) { background: var(--red-soft); border-color: transparent; }
.v-acc { background: linear-gradient(135deg, var(--acc), var(--acc-deep)); color: #fff; }
.v-acc:hover:not(:disabled) { filter: brightness(1.08); transform: translateY(-1px); }
</style>
