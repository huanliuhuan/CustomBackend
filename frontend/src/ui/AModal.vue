<template>
  <teleport to="body">
    <div v-if="modelValue" class="mk" @click.self="close()">
      <transition name="pop" appear>
          <div class="md" :style="{ width }">
            <div class="md-h">
              <div class="md-t">{{ title }}</div>
              <button class="md-x" @click="close()"><AIcon name="close" :size="16" /></button>
            </div>
            <div class="md-b"><slot /></div>
            <div v-if="$slots.footer" class="md-f"><slot name="footer" /></div>
          </div>
      </transition>
    </div>
  </teleport>
</template>

<script setup>
import { watch } from 'vue'
import AIcon from './AIcon.vue'

const props = defineProps({
  modelValue: Boolean,
  title: { type: String, default: '' },
  width: { type: String, default: '520px' }
})
const emit = defineEmits(['update:modelValue'])

watch(() => props.modelValue, v => { document.body.style.overflow = v ? 'hidden' : '' })
function close() { emit('update:modelValue', false) }
</script>

<style scoped>
.mk {
  position: fixed; inset: 0; z-index: 200; background: rgba(10, 14, 10, 0.45);
  backdrop-filter: blur(6px); display: flex; align-items: center; justify-content: center;
}
.md {
  background: var(--card); border: 1px solid var(--line); border-radius: 20px;
  box-shadow: var(--shadow-lg); max-height: 86vh; display: flex; flex-direction: column;
  max-width: 94vw;
}
.md-h { display: flex; align-items: center; justify-content: space-between; padding: 18px 22px 0; }
.md-t { font-size: 16.5px; font-weight: 700; }
.md-x {
  border: 0; background: var(--card2); color: var(--txt2); width: 30px; height: 30px;
  border-radius: 50%; cursor: pointer; display: flex; align-items: center; justify-content: center;
  transition: all 0.2s;
}
.md-x:hover { background: var(--red-soft); color: var(--red); transform: rotate(90deg); }
.md-b { padding: 16px 22px; overflow: auto; }
.md-f { padding: 0 22px 20px; display: flex; justify-content: flex-end; gap: 10px; }
</style>
