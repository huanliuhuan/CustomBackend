<template>
  <span class="rn" :style="{ fontSize: size + 'px' }">
    <span v-for="(ch, i) in chars" :key="i" class="rc">
      <span v-if="ch.digit" class="r-col" :style="{ height: '1em' }">
        <span class="r-strip" :style="{ transform: 'translateY(-' + ch.v + 'em)', transitionDelay: (i * 40) + 'ms' }">
          <i v-for="n in 10" :key="n">{{ n - 1 }}</i>
        </span>
      </span>
      <span v-else class="r-sep">{{ ch.v }}</span>
    </span>
  </span>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  value: { type: [Number, String], default: 0 },
  size: { type: Number, default: 34 }
})

const chars = computed(() => {
  const s = String(props.value ?? 0)
  return [...s].map(c => ({ v: c, digit: c >= '0' && c <= '9' }))
})
</script>

<style scoped>
.rn { display: inline-flex; align-items: baseline; font-weight: 700; font-variant-numeric: tabular-nums; line-height: 1.1; }
.rc { display: inline-block; overflow: hidden; }
.r-col { display: inline-block; overflow: hidden; vertical-align: bottom; }
.r-strip { display: flex; flex-direction: column; transition: transform 0.9s cubic-bezier(0.22, 1.1, 0.3, 1); }
.r-strip i { font-style: normal; height: 1em; line-height: 1.1; display: block; }
.r-sep { display: inline-block; }
</style>
