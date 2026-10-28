<template>
  <div class="toasts">
    <transition-group name="tst">
      <div v-for="t in items" :key="t.id" class="tst" :class="t.type">
        <AIcon :name="t.type === 'err' ? 'close' : 'check'" :size="15" />
        <span>{{ t.msg }}</span>
      </div>
    </transition-group>
  </div>
</template>

<script setup>
import { onMounted, onUnmounted, ref } from 'vue'
import AIcon from './AIcon.vue'
import { _sub } from './toast'

const items = ref([])
let off
onMounted(() => {
  off = _sub(e => {
    if (e.act === 'add') items.value.push(e)
    else items.value = items.value.filter(x => x.id !== e.id)
  })
})
onUnmounted(() => off && off())
</script>

<style scoped>
.toasts { position: fixed; top: 22px; left: 50%; transform: translateX(-50%); z-index: 500; display: flex; flex-direction: column; gap: 10px; align-items: center; }
.tst {
  display: flex; align-items: center; gap: 8px; padding: 10px 20px; border-radius: 999px;
  background: var(--dark-btn); color: var(--bg); font-size: 13.5px; box-shadow: var(--shadow-lg);
}
:root[data-theme='dark'] .tst { color: #0d120d; }
.tst.err { background: var(--red); color: #fff; }
.tst-leave-active { position: absolute; }
.tst-enter-active, .tst-leave-active { transition: all 0.3s ease; }
.tst-enter-from, .tst-leave-to { opacity: 0; transform: translateY(-14px); }
</style>
