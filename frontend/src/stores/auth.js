import { defineStore } from 'pinia'
import { api } from '../api'

export const useAuth = defineStore('auth', {
  state: () => ({
    token: localStorage.getItem('ab_token') || '',
    user: null,
    roles: [],
    perms: [],
    menus: JSON.parse(localStorage.getItem('ab_menus') || '[]'),
    recent: JSON.parse(localStorage.getItem('ab_recent') || '[]')
  }),
  getters: {
    pages: s => {
      const out = []
      const walk = (list, group, base) => {
        for (const m of list) {
          if (m.type === 'M') walk(m.children || [], m.name, base + '/' + (m.path || ''))
          else out.push({ id: m.id, name: m.name, path: base + '/' + (m.path || ''), icon: m.icon, group })
        }
      }
      walk(s.menus, '', '')
      return out
    },
    can: s => perm => s.roles.includes('super_admin') || s.perms.includes(perm)
  },
  actions: {
    async login(form) {
      const d = await api.login(form)
      this.token = d.token
      this.apply(d)
      localStorage.setItem('ab_token', d.token)
    },
    async fetchMe() {
      this.apply(await api.me())
    },
    apply(d) {
      this.user = d.user
      this.roles = d.roles || []
      this.perms = d.perms || []
      this.menus = d.menus || []
      localStorage.setItem('ab_menus', JSON.stringify(this.menus))
    },
    touchRecent(path) {
      const cur = this.recent.filter(r => r.path !== path)
      const p = this.pages.find(x => x.path === path)
      if (p) cur.unshift({ name: p.name, path, group: p.group, icon: p.icon })
      this.recent = cur.slice(0, 5)
      localStorage.setItem('ab_recent', JSON.stringify(this.recent))
    },
    async logout() {
      try { await api.logout() } catch (e) { /* ignore */ }
      this.token = ''
      this.user = null
      localStorage.removeItem('ab_token')
    }
  }
})
