<template>
  <span class="at" :class="tone"><i v-if="dot" class="d" /><slot /></span>
</template>

<script setup>
defineProps({ tone: { type: String, default: 'gray' }, dot: Boolean })
</script>

<style scoped>
.at {
  display: inline-flex; align-items: center; gap: 5px; padding: 2.5px 11px;
  border-radius: 999px; font-size: 12px; font-weight: 500; line-height: 1.5; white-space: nowrap;
}
.d { width: 6px; height: 6px; border-radius: 50%; background: currentColor; }
.green { background: var(--acc-soft); color: var(--acc-deep); }
.blue { background: var(--blue-soft); color: var(--blue); }
.red { background: var(--red-soft); color: var(--red); }
.amber { background: var(--amber-soft); color: var(--amber); }
.violet { background: var(--violet-soft); color: var(--violet); }
.teal { background: rgba(15, 157, 143, 0.12); color: var(--teal); }
.gray { background: var(--card2); color: var(--txt2); border: 1px solid var(--line); }
</style>
