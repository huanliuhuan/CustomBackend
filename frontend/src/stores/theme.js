import { defineStore } from 'pinia'

function sysPref() {
  return window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light'
}

export const useTheme = defineStore('theme', {
  state: () => ({
    mode: localStorage.getItem('ab_theme') || 'light'
  }),
  getters: {
    isDark: s => s.mode === 'dark'
  },
  actions: {
    init() { this.apply(this.mode) },
    apply(mode) {
      this.mode = mode
      const eff = mode === 'auto' ? sysPref() : mode
      document.documentElement.setAttribute('data-theme', eff)
      localStorage.setItem('ab_theme', mode)
    },
    toggle() { this.apply(this.isDark ? 'light' : 'dark') }
  }
})
