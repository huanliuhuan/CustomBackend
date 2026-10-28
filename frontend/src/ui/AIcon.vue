<template>
  <svg :width="s" :height="s" viewBox="0 0 24 24" fill="none" stroke="currentColor"
       stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round" v-html="d" />
</template>

<script setup>
import { computed } from 'vue'

const P = {
  dashboard: '<rect x="3" y="3" width="7" height="9" rx="1.5"/><rect x="14" y="3" width="7" height="5" rx="1.5"/><rect x="14" y="12" width="7" height="9" rx="1.5"/><rect x="3" y="16" width="7" height="5" rx="1.5"/>',
  user: '<circle cx="12" cy="8" r="4"/><path d="M4 21c0-4 4-6 8-6s8 2 8 6"/>',
  users: '<circle cx="9" cy="8" r="3.5"/><path d="M2.5 20c0-3.5 3-5.5 6.5-5.5s6.5 2 6.5 5.5"/><path d="M16 4.6a3.5 3.5 0 010 6.8M18.5 14.8c1.8.8 3 2.3 3 5.2"/>',
  role: '<path d="M12 3l8 3v6c0 5-3.5 8-8 9-4.5-1-8-4-8-9V6z"/><path d="M9 12l2 2 4-4"/>',
  menu: '<path d="M8 6h13M8 12h13M8 18h13"/><circle cx="4" cy="6" r="1" fill="currentColor"/><circle cx="4" cy="12" r="1" fill="currentColor"/><circle cx="4" cy="18" r="1" fill="currentColor"/>',
  dept: '<rect x="9" y="2.5" width="6" height="5" rx="1.2"/><rect x="2.5" y="16.5" width="6" height="5" rx="1.2"/><rect x="15.5" y="16.5" width="6" height="5" rx="1.2"/><path d="M12 7.5v3.5M5.5 16.5V13h13v3.5M12 11v2"/>',
  post: '<rect x="3" y="8" width="18" height="12" rx="2"/><path d="M9 8V6a2 2 0 012-2h2a2 2 0 012 2v2M3 13h18"/>',
  dict: '<path d="M4 19V5a2 2 0 012-2h14v16H6a2 2 0 00-2 2zm0 0a2 2 0 002 2h14"/><path d="M9 7h6"/>',
  config: '<circle cx="12" cy="12" r="3.2"/><path d="M12 2.5v2.8M12 18.7v2.8M2.5 12h2.8M18.7 12h2.8M5.3 5.3l2 2M16.7 16.7l2 2M18.7 5.3l-2 2M7.3 16.7l-2 2"/>',
  monitor: '<path d="M3 12h4l3-7.5 4 15 3-7.5h4"/>',
  operlog: '<path d="M6.5 3h8l4 4v14h-12z"/><path d="M14 3v5h4.5M9.5 13h6M9.5 17h6"/>',
  loginlog: '<circle cx="7.5" cy="15.5" r="4"/><path d="M10.5 12.5l9.5-9.5M17 6l2.5 2.5M13.8 9.2l2 2"/>',
  tool: '<path d="M14.5 6.5a4.2 4.2 0 016-4L17 6l1 1 3.5-3.5a4.2 4.2 0 01-4 6L8 19a2 2 0 01-3-3z"/>',
  code: '<path d="M8.5 8l-4.5 4 4.5 4M15.5 8l4.5 4-4.5 4"/>',
  search: '<circle cx="11" cy="11" r="7"/><path d="M21 21l-4.2-4.2"/>',
  sun: '<circle cx="12" cy="12" r="4"/><path d="M12 2.5v2M12 19.5v2M2.5 12h2M19.5 12h2M5 5l1.4 1.4M17.6 17.6L19 19M19 5l-1.4 1.4M6.4 17.6L5 19"/>',
  moon: '<path d="M20.5 14A8.5 8.5 0 1110 3.5 7 7 0 0020.5 14z"/>',
  logout: '<path d="M9 21H6a2 2 0 01-2-2V5a2 2 0 012-2h3M16 17l5-5-5-5M21 12H9"/>',
  plus: '<path d="M12 5v14M5 12h14"/>',
  edit: '<path d="M17 3.5a2.6 2.6 0 013.7 3.7L7.5 20.5 2.5 21.5l1-5z"/><path d="M14.5 6l3.5 3.5"/>',
  key: '<circle cx="8" cy="16" r="4.5"/><path d="M11.5 12.5L20 4M16.5 7.5l3 3M13.8 10.2l2 2"/>',
  trash: '<path d="M3.5 6.5h17M8.5 6.5V4.8a1.3 1.3 0 011.3-1.3h4.4a1.3 1.3 0 011.3 1.3v1.7M18.5 6.5l-.9 13a2 2 0 01-2 1.9H8.4a2 2 0 01-2-1.9l-.9-13M10 11v6M14 11v6"/>',
  eye: '<path d="M2.5 12S6 5.5 12 5.5 21.5 12 21.5 12 18 18.5 12 18.5 2.5 12 2.5 12z"/><circle cx="12" cy="12" r="3"/>',
  refresh: '<path d="M20 11a8 8 0 10.4 3"/><path d="M20 4.5V11h-6.5"/>',
  download: '<path d="M12 3.5V15M7 10.5l5 5 5-5M4 20.5h16"/>',
  down2: '<path d="M6 9.5l6 6 6-6"/>',
  right: '<path d="M9.5 6l6 6-6 6"/>',
  left: '<path d="M14.5 6l-6 6 6 6"/>',
  close: '<path d="M6 6l12 12M18 6L6 18"/>',
  check: '<path d="M4.5 12.5l5 5L20 6.5"/>',
  play: '<path d="M7.5 4.5l12 7.5-12 7.5z"/>',
  pause: '<path d="M8 5h2.8v14H8zM13.2 5H16v14h-2.8z"/>',
  lock: '<rect x="4.5" y="10.5" width="15" height="10" rx="2"/><path d="M8 10.5V7.5a4 4 0 018 0v3"/>',
  clock: '<circle cx="12" cy="12" r="9"/><path d="M12 7v5l3.5 2.5"/>',
  shield: '<path d="M12 3l8 3v6c0 5-3.5 8-8 9-4.5-1-8-4-8-9V6z"/>',
  grid: '<rect x="3.5" y="3.5" width="7" height="7" rx="1.4"/><rect x="13.5" y="3.5" width="7" height="7" rx="1.4"/><rect x="3.5" y="13.5" width="7" height="7" rx="1.4"/><rect x="13.5" y="13.5" width="7" height="7" rx="1.4"/>',
  panel: '<rect x="3.5" y="4" width="17" height="16" rx="2"/><path d="M9.5 4v16"/>',
  star: '<path d="M12 3l2.7 5.6 6.1.8-4.5 4.2 1.1 6-5.4-3-5.4 3 1.1-6L3.2 9.4l6.1-.8z"/>',
  swap: '<path d="M7 4v13M7 17l-3.5-3.5M7 17l3.5-3.5M17 20V7M17 7l-3.5 3.5M17 7l3.5 3.5"/>',
  building: '<path d="M4 21V5a2 2 0 012-2h8a2 2 0 012 2v16M4 21h16M16 9h3a1 1 0 011 1v11M8 7h4M8 11h4M8 15h4"/>',
  spark: '<path d="M12 2l1.8 6.2L20 10l-6.2 1.8L12 18l-1.8-6.2L4 10l6.2-1.8z"/>',
  bell: '<path d="M18 9a6 6 0 10-12 0c0 6-2.5 7-2.5 7h17S18 15 18 9zM10 20a2 2 0 004 0"/>'
}

const props = defineProps({ name: { type: String, required: true }, size: { type: Number, default: 17 } })
const d = computed(() => P[props.name] || '')
const s = computed(() => props.size)
</script>
