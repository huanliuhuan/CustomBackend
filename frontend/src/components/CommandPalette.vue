<template>
  <teleport to="body">
    <div v-if="open" class="cpk" @click.self="close">
        <transition name="pop" appear>
          <div class="cp">
            <div class="cp-in">
              <AIcon name="search" :size="17" />
              <input ref="inp" v-model="q" :placeholder="isQa ? '输入问题后回车,例如「谁能删除用户」' : '搜索页面、用户或动作'"
                     @keydown="onKey" />
              <kbd v-if="!q">Esc</kbd>
              <button v-else class="cp-x" @click="q = ''"><AIcon name="close" :size="14" /></button>
            </div>

            <div class="cp-body">
              <!-- 问系统 -->
              <template v-if="isQa">
                <template v-if="!answers.length">
                  <div class="cp-lbl">猜你想问</div>
                  <div class="cp-qa">
                    <button v-for="p in qaMatched" :key="p.q" @click="ask(p)">{{ p.q }}</button>
                  </div>
                  <div class="cp-tip tiny muted">输入 ? 进入提问模式 · 撤去 ? 返回搜索</div>
                </template>
                <template v-else>
                  <div class="cp-ans" v-for="(a, i) in answers" :key="i">
                    <div class="cp-lbl"><AIcon name="spark" :size="12" /> {{ a.q }}</div>
                    <div v-if="!a.rows.length" class="cp-empty muted">没有匹配的结果</div>
                    <template v-else>
                      <div v-for="(g, gi) in a.groups" :key="gi" class="cp-row">
                        <ATag tone="violet" :dot="true">{{ g.role }}</ATag>
                        <div class="cp-us">
                          <span v-for="u in g.users" :key="u" class="cp-u"><span class="avatar uc" :style="{ background: uc(u) }">{{ u[0] }}</span>{{ u }}</span>
                        </div>
                      </div>
                    </template>
                  </div>
                </template>
              </template>

              <!-- 搜索 -->
              <template v-else>
                <div v-if="auth.recent.length && !q" class="cp-lbl">最近访问</div>
                <div v-if="!q">
                  <div v-for="r in auth.recent" :key="'r' + r.path" class="cp-item" @click="go(r.path)">
                    <AIcon :name="r.icon || 'panel'" :size="16" />
                    <span>{{ r.name }}</span>
                    <span class="cp-g">{{ r.group }}</span>
                    <AIcon name="right" :size="13" class="cp-go" />
                  </div>
                </div>
                <div class="cp-lbl">{{ q ? '页面' : '全部页面' }}</div>
                <div v-for="p in pageHits" :key="p.path" class="cp-item" @click="go(p.path)">
                  <AIcon :name="p.icon || 'panel'" :size="16" />
                  <span>{{ p.name }}</span>
                  <span class="cp-g">{{ p.group }}</span>
                  <AIcon name="right" :size="13" class="cp-go" />
                </div>
                <div v-if="!pageHits.length" class="cp-empty muted">没有匹配的页面</div>
                <div class="cp-lbl">动作</div>
                <div class="cp-item" @click="theme.toggle(); close()">
                  <AIcon :name="theme.isDark ? 'sun' : 'moon'" :size="16" />
                  <span>{{ theme.isDark ? '切换到浅色外观' : '切换到深色外观' }}</span>
                </div>
                <div class="cp-item" @click="ask(qa[0])">
                  <AIcon name="shield" :size="16" />
                  <span>问系统:谁能删除用户?</span>
                </div>
                <div class="cp-ft tiny muted"><kbd>↑↓</kbd> 选择 <kbd>↵</kbd> 打开 <kbd>?</kbd> 问系统</div>
              </template>
            </div>
          </div>
        </transition>
      </div>
  </teleport>
</template>

<script setup>
import { computed, nextTick, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import AIcon from '../ui/AIcon.vue'
import ATag from '../ui/ATag.vue'
import { api } from '../api'
import { useAuth } from '../stores/auth'
import { useTheme } from '../stores/theme'
import { toast } from '../ui/toast'

const props = defineProps({ open: Boolean })
const emit = defineEmits(['update:open'])
const router = useRouter()
const auth = useAuth()
const theme = useTheme()
const q = ref('')
const inp = ref(null)
const answers = ref([])

const isQa = computed(() => q.value.startsWith('?') || answers.value.length > 0)

const qa = [
  { q: '谁能删除用户?', run: () => api.whoCan('system:user:remove') },
  { q: '哪些角色没人用?', run: () => api.idleRoles() },
  { q: '哪些用户还没分配角色?', run: () => api.unassignedUsers() },
  { q: '哪些菜单没有授予任何角色?', run: () => api.orphanMenus() }
]
const qaMatched = computed(() => {
  const k = q.value.replace(/^\?/, '').trim()
  return k ? qa.filter(x => x.q.includes(k)) : qa
})

const pageHits = computed(() => {
  const k = q.value.trim().toLowerCase()
  const ps = auth.pages
  if (!k) return ps
  return ps.filter(p => p.name.toLowerCase().includes(k) || p.path.includes(k) || (p.group || '').includes(k))
})

watch(() => props.open, v => {
  if (v) {
    q.value = ''
    answers.value = []
    nextTick(() => inp.value && inp.value.focus())
  }
})

function close() { emit('update:open', false) }
function go(p) { close(); router.push(p) }

async function ask(p) {
  try {
    const data = await p.run()
    let rows = data || []
    let groups = []
    if (p.q.startsWith('谁能')) {
      const m = new Map()
      for (const r of rows) {
        if (!m.has(r.rname)) m.set(r.rname, [])
        m.get(r.rname).push(r.uname)
      }
      groups = [...m.entries()].map(([role, users]) => ({ role, users }))
    } else if (p.q.includes('角色没人用')) {
      groups = rows.map(r => ({ role: r.name + ' (' + r.code + ')', users: [] }))
    } else if (p.q.includes('没分配角色')) {
      groups = rows.map(r => ({ role: r.deptName || '未分配部门', users: [r.nickName] }))
    } else {
      groups = rows.map(r => ({ role: r.name, users: [] }))
    }
    answers.value = [{ q: p.q, rows, groups }]
    q.value = '?' + p.q
  } catch (e) {
    toast(e.message, 'err')
  }
}

function onKey(e) {
  if (e.key === 'Escape') { close(); return }
  if (e.key === 'Enter') {
    if (isQa.value) {
      if (qaMatched.value.length) ask(qaMatched.value[0])
    } else if (pageHits.value.length) go(pageHits.value[0].path)
  }
}

function uc(name) {
  const colors = ['#71b81f', '#3e7ef7', '#8b5cf6', '#e59819', '#0f9d8f', '#e5484d']
  let h = 0
  for (const c of name) h = (h * 31 + c.charCodeAt(0)) % 997
  return colors[h % colors.length]
}
</script>

<style scoped>
.cpk {
  position: fixed; inset: 0; z-index: 300; background: rgba(8, 12, 8, 0.42);
  backdrop-filter: blur(7px); display: flex; justify-content: center; align-items: flex-start;
  padding-top: 12vh;
}
.cp {
  width: 620px; max-width: 92vw; background: var(--card); border: 1px solid var(--line);
  border-radius: 22px; box-shadow: var(--shadow-lg); overflow: hidden;
}
.cp-in { display: flex; align-items: center; gap: 12px; padding: 17px 20px; border-bottom: 1px solid var(--line2); color: var(--txt3); }
.cp-in input { flex: 1; border: 0; outline: 0; background: transparent; color: var(--txt); font-size: 15.5px; font-family: inherit; }
.cp-in kbd { font-family: inherit; font-size: 11px; border: 1px solid var(--line); background: var(--card2); padding: 3px 8px; border-radius: 7px; }
.cp-x { border: 0; background: var(--card2); border-radius: 50%; width: 26px; height: 26px; cursor: pointer; color: var(--txt2); display: flex; align-items: center; justify-content: center; }
.cp-body { padding: 12px 10px 14px; max-height: 56vh; overflow: auto; }
.cp-lbl { font-size: 11.5px; color: var(--txt3); letter-spacing: 1px; padding: 8px 12px 5px; display: flex; align-items: center; gap: 6px; }
.cp-item {
  display: flex; align-items: center; gap: 12px; padding: 9.5px 12px; border-radius: 12px;
  cursor: pointer; color: var(--txt); font-size: 13.5px;
}
.cp-item:hover { background: var(--acc-soft); }
.cp-item:hover .cp-go { opacity: 1; transform: translateX(0); }
.cp-g { margin-left: auto; font-size: 12px; color: var(--txt3); }
.cp-go { opacity: 0; transform: translateX(-4px); transition: all 0.18s; color: var(--acc-deep); }
.cp-qa { display: flex; flex-wrap: wrap; gap: 8px; padding: 2px 12px 6px; }
.cp-qa button {
  border: 1px solid var(--line); background: var(--card2); color: var(--txt2);
  border-radius: 999px; padding: 7px 15px; cursor: pointer; font-size: 13px; transition: all 0.2s;
  font-family: inherit;
}
.cp-qa button:hover { border-color: var(--acc); color: var(--acc-deep); background: var(--acc-soft); }
.cp-tip { padding: 8px 14px 2px; }
.cp-ans { padding: 4px 12px 8px; }
.cp-row { display: flex; align-items: center; gap: 12px; padding: 8px 0; border-bottom: 1px dashed var(--line2); flex-wrap: wrap; }
.cp-row:last-child { border-bottom: 0; }
.cp-us { display: flex; flex-wrap: wrap; gap: 10px; }
.cp-u { display: inline-flex; align-items: center; gap: 6px; font-size: 13px; }
.uc { width: 24px; height: 24px; font-size: 11px; }
.cp-empty { padding: 10px 14px; font-size: 13px; }
.cp-ft { display: flex; gap: 14px; padding: 10px 14px 2px; border-top: 1px solid var(--line2); margin-top: 8px; }
.cp-ft kbd { font-family: inherit; border: 1px solid var(--line); background: var(--card2); border-radius: 5px; padding: 1px 5px; font-size: 10.5px; }
</style>
