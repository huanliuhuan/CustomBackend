<template>
  <div class="nf">
    <div class="nf-card">
      <div class="nf-code">{{ forbidden ? '403' : '404' }}</div>
      <p>{{ forbidden ? '没有权限访问这个页面' : '页面走丢了' }}</p>
      <ABtn variant="blue" @click="$router.push('/dashboard')">回工作台</ABtn>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import ABtn from '../ui/ABtn.vue'

const route = useRoute()
const forbidden = computed(() => !!route.meta.forbidden)
</script>

<style scoped>
.nf { height: 100vh; display: flex; align-items: center; justify-content: center; }
.nf-card { text-align: center; display: flex; flex-direction: column; align-items: center; gap: 14px; }
.nf-code { font-size: 84px; font-weight: 800; color: var(--acc); letter-spacing: 4px; text-shadow: 0 10px 40px var(--glow); }
p { color: var(--txt2); }
</style>
