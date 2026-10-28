<template>
  <div class="lay" v-if="auth.user">
    <aside class="sd">
      <div class="sd-logo" @click="go('/dashboard')">
        <div class="lg-orb"><AIcon name="spark" :size="16" /></div>
        <div>
          <div class="lg-t">管理后台</div>
          <div class="lg-s">ADMIN CONSOLE</div>
        </div>
      </div>
      <nav class="sd-nav">
        <template v-for="m in auth.menus" :key="m.id">
          <div v-if="m.type === 'C'" class="sd-i" :class="{ on: act(m) }" @click="go(fullPath(m))">
            <AIcon :name="m.icon || 'panel'" :size="17" /><span>{{ m.name }}</span>
          </div>
          <template v-else-if="m.type === 'M'">
            <div class="sd-g">{{ m.name }}</div>
            <div v-for="c in m.children" :key="c.id" class="sd-i" :class="{ on: act(c, m) }" @click="go(fullPath(c, m))">
              <AIcon :name="c.icon || 'panel'" :size="17" /><span>{{ c.name }}</span>
            </div>
          </template>
        </template>
      </nav>
      <div class="sd-ft">
        <span class="tiny muted">v1.0.0</span>
        <span class="tiny muted">通用后台底座</span>
      </div>
    </aside>

    <main class="mn">
      <header class="tp">
        <div class="tp-crumb">
          <AIcon name="panel" :size="14" />
          <span class="crumb-path">
            <span class="muted">{{ groupOf(route.path) }}</span>
            <template v-if="groupOf(route.path)"><span class="sep">›</span></template>
            <strong>{{ titleOf(route.path) }}</strong>
          </span>
        </div>
        <div class="tp-r">
          <button class="tp-search" @click="pal.open = true">
            <AIcon name="search" :size="14" />
            <span>搜索菜单</span>
            <kbd>⌘ K</kbd>
          </button>
          <button class="tp-ico" :title="theme.isDark ? '切到浅色' : '切到深色'" @click="theme.toggle()">
            <AIcon :name="theme.isDark ? 'sun' : 'moon'" :size="17" />
          </button>
          <div class="tp-user">
            <span class="avatar" :style="{ background: avatarColor }">{{ (auth.user.nickName || '?')[0] }}</span>
            <div class="tp-un">
              <strong>{{ auth.user.nickName }}</strong>
              <span class="tiny muted">{{ auth.roles[0] === 'super_admin' ? '超级管理员' : '平台架构部' }}</span>
            </div>
            <button class="tp-out" title="退出登录" @click="doLogout"><AIcon name="logout" :size="16" /></button>
          </div>
        </div>
      </header>
      <section class="mn-body">
        <router-view v-slot="{ Component }">
          <transition name="fade">
            <component :is="Component" :key="route.path" />
          </transition>
        </router-view>
      </section>
    </main>

    <CommandPalette v-model:open="pal.open" />
  </div>
</template>

<script setup>
import { computed, onMounted, onUnmounted, reactive, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import AIcon from '../ui/AIcon.vue'
import CommandPalette from '../components/CommandPalette.vue'
import { useAuth } from '../stores/auth'
import { useTheme } from '../stores/theme'

const auth = useAuth()
const theme = useTheme()
const route = useRoute()
const router = useRouter()
const pal = reactive({ open: false })

onMounted(async () => {
  theme.init()
  if (!auth.user) {
    try { await auth.fetchMe() } catch (e) { /* 401 handled in request.js */ }
  }
  if (!localStorage.getItem('ab_appearance_applied')) {
    try {
      const { api } = await import('../api')
      const list = await api.configs()
      const ap = list.find(c => c.key === 'sys.index.appearance')
      if (ap) theme.apply(ap.value)
    } catch (e) { /* ignore */ }
    localStorage.setItem('ab_appearance_applied', '1')
  }
})

const onKey = e => {
  if ((e.metaKey || e.ctrlKey) && e.key.toLowerCase() === 'k') {
    e.preventDefault()
    pal.open = !pal.open
  }
}
onMounted(() => window.addEventListener('keydown', onKey))
onUnmounted(() => window.removeEventListener('keydown', onKey))

watch(() => route.path, p => auth.touchRecent(p), { immediate: true })

const colors = ['#71b81f', '#3e7ef7', '#8b5cf6', '#e59819', '#0f9d8f', '#e5484d']
const avatarColor = computed(() => {
  const n = auth.user?.nickName || ''
  let h = 0
  for (const c of n) h = (h * 31 + c.charCodeAt(0)) % 997
  return colors[h % colors.length]
})

function go(p) { router.push(p) }
function fullPath(c, parent) { return '/' + (parent ? parent.path + '/' : '') + c.path }
function act(m, parent) { return route.path === fullPath(m, parent) }
function titleOf(p) {
  for (const m of walkFlat()) if (m.path === p) return m.name
  return '工作台'
}
function groupOf(p) {
  for (const m of auth.menus) {
    if (m.type === 'M' && (m.children || []).some(c => fullPath(c, m) === p)) return m.name
  }
  return ''
}
function* walkFlat() {
  for (const m of auth.menus) {
    if (m.type === 'C') yield { path: fullPath(m), name: m.name }
    else for (const c of m.children || []) yield { path: fullPath(c, m), name: c.name }
  }
}
async function doLogout() {
  await auth.logout()
  router.push('/login')
}
</script>

<style scoped>
.lay { display: flex; height: 100vh; overflow: hidden; }
.sd {
  width: 226px; flex: none; display: flex; flex-direction: column;
  background: var(--side); backdrop-filter: blur(14px);
  border-right: 1px solid var(--line2); padding: 18px 14px;
}
.sd-logo { display: flex; align-items: center; gap: 10px; padding: 2px 8px 16px; cursor: pointer; }
.lg-orb {
  width: 34px; height: 34px; border-radius: 11px; display: flex; align-items: center; justify-content: center;
  background: linear-gradient(135deg, var(--acc), var(--acc-deep)); color: #fff;
  box-shadow: 0 4px 14px var(--glow);
}
.lg-t { font-weight: 700; font-size: 15px; line-height: 1.2; }
.lg-s { font-size: 10px; letter-spacing: 1.6px; color: var(--txt3); }
.sd-nav { flex: 1; overflow: auto; padding: 4px 0; }
.sd-g {
  font-size: 11px; color: var(--txt3); letter-spacing: 1.5px; padding: 16px 12px 7px;
}
.sd-i {
  display: flex; align-items: center; gap: 10px; padding: 9px 12px; margin: 2px 0;
  border-radius: 11px; color: var(--txt2); cursor: pointer; font-size: 13.5px;
  transition: all 0.2s; border: 1px solid transparent;
}
.sd-i:hover { color: var(--txt); background: var(--card2); }
.sd-i.on { background: var(--dark-btn); color: var(--bg); font-weight: 600; box-shadow: var(--shadow); }
:root[data-theme='dark'] .sd-i.on { color: #0d120d; background: linear-gradient(135deg, var(--acc), #67a81f); }
.sd-ft { display: flex; justify-content: space-between; padding: 12px 10px 2px; border-top: 1px solid var(--line2); }

.mn { flex: 1; display: flex; flex-direction: column; min-width: 0; }
.tp {
  height: 58px; flex: none; display: flex; align-items: center; justify-content: space-between;
  padding: 0 22px; border-bottom: 1px solid var(--line2); background: color-mix(in srgb, var(--bg2) 60%, transparent);
  backdrop-filter: blur(10px); gap: 16px;
}
.tp-crumb { display: flex; align-items: center; gap: 8px; color: var(--txt2); font-size: 13px; min-width: 0; }
.crumb-path { display: flex; gap: 7px; align-items: center; white-space: nowrap; overflow: hidden; }
.sep { color: var(--txt3); }
.tp-r { display: flex; align-items: center; gap: 12px; }
.tp-search {
  display: flex; align-items: center; gap: 8px; height: 34px; padding: 0 8px 0 14px;
  border-radius: 999px; border: 1px solid var(--line); background: var(--card);
  color: var(--txt3); cursor: pointer; font-size: 13px; width: 210px; transition: all 0.2s;
}
.tp-search:hover { border-color: var(--acc); color: var(--txt2); }
.tp-search span { flex: 1; text-align: left; }
.tp-search kbd {
  font-family: inherit; font-size: 11px; background: var(--card2); border: 1px solid var(--line);
  border-radius: 6px; padding: 2px 7px;
}
.tp-ico {
  width: 34px; height: 34px; border-radius: 50%; border: 1px solid var(--line);
  background: var(--card); color: var(--txt2); cursor: pointer;
  display: flex; align-items: center; justify-content: center; transition: all 0.3s;
}
.tp-ico:hover { color: var(--amber); transform: rotate(20deg); }
.tp-user { display: flex; align-items: center; gap: 9px; padding-left: 6px; }
.tp-un { line-height: 1.25; text-align: left; }
.tp-un strong { font-size: 13px; display: block; }
.tp-out { border: 0; background: transparent; color: var(--txt3); cursor: pointer; padding: 6px; border-radius: 8px; }
.tp-out:hover { color: var(--red); background: var(--red-soft); }
.mn-body { flex: 1; overflow: auto; position: relative; }
.mn-body > * { min-height: 100%; }
</style>
