<template>
  <teleport to="body">
    <div v-if="modelValue" class="dk" @click.self="close()">
      <transition name="slide" appear>
          <div class="dw" :style="{ width }">
            <div class="dw-h">
              <div class="dw-t">{{ title }}</div>
              <button class="dw-x" @click="close()"><AIcon name="close" :size="16" /></button>
            </div>
            <div class="dw-b"><slot /></div>
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
  width: { type: String, default: '460px' }
})
const emit = defineEmits(['update:modelValue'])

watch(() => props.modelValue, v => { document.body.style.overflow = v ? 'hidden' : '' })
function close() { emit('update:modelValue', false) }
</script>

<style scoped>
.dk {
  position: fixed; inset: 0; z-index: 200; background: rgba(10, 14, 10, 0.4);
  backdrop-filter: blur(5px);
}
.dw {
  position: absolute; top: 0; right: 0; bottom: 0;
  background: var(--card); border-left: 1px solid var(--line);
  box-shadow: var(--shadow-lg); display: flex; flex-direction: column;
  max-width: 94vw;
}
.dw-h { display: flex; align-items: center; justify-content: space-between; padding: 20px 24px 12px; }
.dw-t { font-size: 16.5px; font-weight: 700; }
.dw-x {
  border: 0; background: var(--card2); color: var(--txt2); width: 30px; height: 30px;
  border-radius: 50%; cursor: pointer; display: flex; align-items: center; justify-content: center;
}
.dw-x:hover { background: var(--red-soft); color: var(--red); }
.dw-b { padding: 6px 24px 26px; overflow: auto; flex: 1; }
.slide-enter-active { transition: transform 0.32s cubic-bezier(0.25, 1, 0.4, 1); }
.slide-leave-active { transition: transform 0.22s ease; }
.slide-enter-from, .slide-leave-to { transform: translateX(105%); }
</style>
