<template>
  <div class="sm">
    <div class="sm-hd">
      <div class="sm-crumb tiny muted">
        <AIcon name="star" :size="13" />
        <a class="crlk" :class="{ on: !activeNode }" @click="reset">权限全景</a>
        <template v-if="activeNode">
          <span class="sep">/</span>
          <a class="crlk" :class="{ on: activeNode && !(activeNode === 'role' && stage >= 2) }"
             @click="activeNode === 'role' && stage >= 2 ? backToRoleList() : null">{{ nodeLabel }}</a>
        </template>
        <template v-if="activeNode === 'role' && stage >= 2 && sel">
          <span class="sep">/</span>
          <b class="on">{{ sel.name }}</b>
        </template>
      </div>
      <div class="sm-ops">
        <span class="ovc">
          <span class="oc"><i class="d du" />用户 <b>{{ ov.users }}</b></span>
          <span class="oc"><i class="d dr" />角色 <b>{{ ov.roles }}</b></span>
          <span class="oc"><i class="d dm" />菜单 <b>{{ ov.menus }}</b></span>
          <span class="oc wm" :class="{ hot: ov.idleRoles > 0 }"><i class="d" style="background: var(--amber)" />闲置角色 <b>{{ ov.idleRoles }}</b></span>
          <span class="oc wm" :class="{ hot: ov.orphanMenus > 0 }"><i class="d" style="background: var(--amber)" />孤立菜单 <b>{{ ov.orphanMenus }}</b></span>
        </span>
        <span class="tiny muted sm-tip">{{ tipText }}</span>
        <button class="sop" @click="replay"><AIcon name="refresh" :size="13" /> 重播</button>
        <button class="sop" @click="togglePause"><AIcon :name="paused ? 'play' : 'pause'" :size="13" /> {{ paused ? '播放' : '暂停' }}</button>
      </div>
    </div>

    <div class="sm-stage" :class="{ paused }">
      <div class="sm-canvas" ref="canvasEl">
      <svg ref="svgEl" :key="'s' + gen" class="sm-svg" viewBox="0 0 1460 560" preserveAspectRatio="xMidYMid meet" @click="onSvgClick">
        <defs>
          <radialGradient id="orbG2" cx="38%" cy="32%">
            <stop offset="0%" stop-color="#d9f3ae" />
            <stop offset="55%" stop-color="#8fd634" />
            <stop offset="100%" stop-color="#4e8f0c" />
          </radialGradient>
          <linearGradient id="panG" x1="0" y1="0" x2="1" y2="0">
            <stop offset="0%" stop-color="rgba(99,102,241,0.12)" />
            <stop offset="50%" stop-color="rgba(99,102,241,0.55)" />
            <stop offset="100%" stop-color="rgba(99,102,241,0.12)" />
          </linearGradient>
        </defs>

        <!-- 全景:连线 / 粒子 / 放射扇 / 装饰点 -->
        <g :opacity="expanded ? 0.28 : 1" style="transition: opacity 0.5s">
          <path class="panlk" v-for="(l, i) in baseLinks" :key="'b' + i + gen" :d="l" :style="dashStyle(i, 0.15)" />
          <circle v-for="(p, i) in baseParticles" :key="'bp' + i + gen" r="4" fill="var(--violet)" opacity="0.85">
            <animateMotion :path="p.path" :dur="p.dur + 's'" :begin="p.begin + 's'" repeatCount="indefinite" />
          </circle>
          <g v-for="(f, i) in roleFan" :key="'rf' + i">
            <path :d="f.line" fill="none" stroke="rgba(139,92,246,0.65)" stroke-width="1.8" :style="dashStyle(i, 0.4, 90)" />
            <circle :cx="f.x" :cy="f.y" r="3.6" fill="var(--violet)" opacity="0.75"
                    :style="{ animation: 'dotIn 0.5s ' + (0.5 + i * 0.07) + 's backwards' }" />
          </g>
          <g v-for="(f, i) in menuFan" :key="'mf' + i">
            <path :d="f.line" fill="none" stroke="rgba(113,184,31,0.7)" stroke-width="1.8" :style="dashStyle(i, 0.55, 90)" />
            <circle :cx="f.x" :cy="f.y" r="3.2" fill="var(--acc)" opacity="0.7"
                    :style="{ animation: 'dotIn 0.5s ' + (0.65 + i * 0.06) + 's backwards' }" />
          </g>
          <circle v-for="(u, i) in userDots" :key="'ud' + i" :cx="u.x" :cy="u.y" r="3" fill="var(--blue)" opacity="0.6" />
        </g>

        <!-- 展开:节点 → 行 散射连线 -->
        <g v-if="expanded">
          <path v-for="(l, i) in rowLinks" :key="'rl' + i + gen" :d="l.d" fill="none"
                :class="['rlink', l.tone, { hot: l.hot, dimmed: l.dim }]" :style="dashStyle(i, 0.3, 300)" />
          <circle v-for="(l, i) in rowLinks" :key="'rp' + i + gen" r="3" :fill="l.dot || 'var(--violet)'" opacity="0.8">
            <animateMotion :path="l.d" dur="2.2s" :begin="(0.5 + i * 0.22) + 's'" repeatCount="indefinite" />
          </circle>
        </g>

        <!-- 角色链:胶囊 → 授权页面行 连线 -->
        <g v-if="activeNode === 'role' && stage >= 2">
          <path v-for="(l, i) in pageLinks" :key="'pl' + i + gen" :d="l.d" fill="none" class="plink"
                :style="dashStyle(i, 0.35, 260)" />
          <circle v-for="(l, i) in pageLinks" :key="'pd' + i + gen" r="3" fill="var(--acc)">
            <animateMotion :path="l.d" dur="1.6s" :begin="(0.5 + i * 0.12) + 's'" repeatCount="indefinite" />
          </circle>
        </g>
      </svg>

      <!-- 基础节点 -->
      <div class="dial pop" :style="at(U.x, U.y, 0.15)" :class="{ dim: expanded && activeNode !== 'user' }" @click="clickDial('user')">
        <div class="dring"><span class="dnum">{{ ov.users }}</span></div>
        <div class="dname">用户</div>
        <div class="dsub">已分配角色占比</div>
        <span class="hint tiny" v-if="hintFor('user')">{{ hintText('user') }}</span>
      </div>
      <div class="orbw pop" :style="at(420, 290, 0.05)" :class="{ dim: expanded && activeNode !== 'role' }">
        <div class="orb"><i class="orb-dash" /></div>
        <div class="dname">权限全景</div>
        <div class="dsub">{{ ov.menus }} 个菜单 · {{ roles.length }} 个角色</div>
      </div>
      <div class="dial pop clickable" :style="at(R.x, R.y, 0.3)" :class="{ dim: expanded && activeNode !== 'role' }" @click="clickDial('role')">
        <div class="dring"><span class="dnum">{{ ov.roles }}</span></div>
        <div class="dname">角色</div>
        <div class="dsub">有人持有占比</div>
        <span class="hint tiny" v-if="hintFor('role')">{{ hintText('role') }}</span>
      </div>
      <div class="dial pop clickable" :style="at(M.x, M.y, 0.4)" :class="{ dim: expanded && activeNode !== 'menu' }" @click="clickDial('menu')">
        <div class="dring"><span class="dnum">{{ ov.menus }}</span></div>
        <div class="dname">菜单</div>
        <div class="dsub">已被授权页面占比</div>
        <span class="hint tiny" v-if="hintFor('menu')">{{ hintText('menu') }}</span>
      </div>

      <!-- 用户行 -->
      <template v-if="activeNode === 'user'">
        <div v-for="(u, i) in userRows" :key="'u' + u.id" class="nrow urow"
             :style="{ left: userLeftFor(i), top: userTop(i), animationDelay: 0.12 + i * 0.06 + 's' }">
          <span class="avatar nav" :style="{ background: uc(u.nickName) }">{{ (u.nickName || '?')[0] }}</span>
          <div class="rt">
            <b>{{ u.nickName }}</b>
            <span class="tiny muted">@{{ u.account }} · {{ u.deptName || '未分配部门' }}</span>
          </div>
          <span class="chip" :class="{ warn: !u.roleNames.length }">{{ u.roleNames.length ? u.roleNames.join('/') : '未分配角色' }}</span>
        </div>
      </template>

      <!-- 角色行 -->
      <template v-if="activeNode === 'role'">
        <div v-for="(r, i) in roles" :key="r.id" class="nrow rrow" :data-rid="r.id"
             :class="{ sel: sel && sel.id === r.id, dimmed: stage >= 2 && sel && sel.id !== r.id, loading: loading === r.id }"
             :style="{ left: roleLeft, top: rowTop(i), animationDelay: 0.15 + i * 0.08 + 's' }"
             @click="pickRole(r)">
          <span class="rc"><i /></span>
          <div class="rt">
            <b>{{ r.name }}</b>
            <span class="tiny muted">{{ r.userCount }} 人持有 · {{ r.pageCount }} 个页面</span>
          </div>
          <AIcon v-if="stage === 1" name="right" :size="13" class="rla" />
        </div>
      </template>

      <!-- 菜单行 -->
      <template v-if="activeNode === 'menu'">
        <div v-for="(m, i) in menuRows" :key="'m' + m.id" class="nrow mrow"
             :style="{ left: menuLeftFor(i), top: menuTop(i), animationDelay: 0.1 + i * 0.045 + 's' }" :title="m.perm || ''">
          <span class="rc gm"><i /></span>
          <div class="rt one">
            <b>{{ m.name }}</b>
            <span class="tiny muted">{{ m.type === 'M' ? '目录' : '页面' }}<i v-if="m.perm" class="code">{{ m.perm }}</i></span>
          </div>
          <span class="chip">{{ m.grant }} 角色</span>
        </div>
      </template>

      <!-- 角色链:授权页面行 -->
      <template v-if="activeNode === 'role' && stage >= 2 && chain">
        <button class="pf-x" title="收起,返回角色列表" @click="backToRoleList"><AIcon name="close" :size="13" /></button>
        <div v-for="(p, i) in chain.pages" :key="'pg' + p.id" class="prow"
             :style="{ left: pageLeft, top: pageTop(i), animationDelay: 0.3 + i * 0.06 + 's' }" :title="p.perm || ''">
          <span class="pg-dot" />
          <b>{{ p.name }}</b>
          <span class="chip">{{ p.btnCount }} 按钮</span>
        </div>
      </template>

      <!-- 角色链:详情面板 -->
      <transition name="pnl">
        <div v-if="activeNode === 'role' && stage >= 2 && chain" class="rp">
          <div class="rp-hd">
            <ATag tone="violet" :dot="true">{{ chain.role.name }}</ATag>
            <span class="chip code">{{ chain.role.code }}</span>
            <button class="rp-x" @click="backToRoleList"><AIcon name="close" :size="14" /></button>
          </div>
          <div class="tiny muted">{{ scopeLabel(chain.role.dataScope) }} · {{ chain.role.builtin ? '内置角色' : '自定义角色' }}</div>

          <div class="rp-card">
            <div class="rp-bignum"><RollNum :value="chain.pageCount" :size="30" /><span class="tiny muted">个授予页面</span></div>
            <div class="rp-bars">
              <i v-for="(b, i) in bars" :key="i" :style="{ height: b + '%', animationDelay: 0.25 + i * 0.05 + 's' }" />
            </div>
            <div class="tiny muted">各页面按钮数分布</div>
          </div>

          <div class="rp-grid">
            <div class="rp-k"><RollNum :value="chain.btnCount" :size="20" /><span class="tiny muted">按钮权限</span></div>
            <div class="rp-k"><RollNum :value="chain.userCount" :size="20" /><span class="tiny muted">持有用户</span></div>
            <div class="rp-k"><ATag :tone="chain.role.status === '0' ? 'green' : 'red'">{{ chain.role.status === '0' ? '正常' : '停用' }}</ATag><span class="tiny muted">状态</span></div>
          </div>

          <div v-if="chain.role.remark" class="rp-remark tiny muted">{{ chain.role.remark }}</div>

          <div class="rp-sec">
            <div class="rp-lbl">持有用户 · {{ chain.users.length }}</div>
            <div class="rp-chips">
              <span v-for="u in chain.users" :key="u.account" class="rp-u">
                <span class="avatar" :style="{ background: uc(u.uname) }">{{ (u.uname || '?')[0] }}</span>
                <b>{{ u.uname }}</b>
                <i class="tiny muted">{{ u.dname || '未分配部门' }}</i>
              </span>
              <span v-if="!chain.users.length" class="tiny muted">暂无用户持有该角色</span>
            </div>
          </div>

          <div class="rp-sec">
            <div class="rp-lbl">可访问页面</div>
            <div class="rp-pages">
              <span v-for="p in chain.pages" :key="'v' + p.id" class="rp-pg">
                <b>{{ p.name }}</b>
                <i v-if="p.perm" class="chip code">{{ p.perm }}</i>
              </span>
            </div>
          </div>
        </div>
      </transition>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, nextTick, onMounted, onUnmounted, ref } from 'vue'
import AIcon from '../ui/AIcon.vue'
import ATag from '../ui/ATag.vue'
import RollNum from '../ui/RollNum.vue'
import { api } from '../api'
import { toast } from '../ui/toast'

const props = defineProps({ data: Object })

const activeNode = ref('')
const stage = ref(0)
const sel = ref(null)
const chain = ref(null)
const loading = ref(null)
const gen = ref(0)
const paused = ref(false)
const svgEl = ref(null)
const canvasEl = ref(null)
const rowLinks = ref([])
const pageLinks = ref([])

const ov = computed(() => props.data?.overview || {})
const roles = computed(() => props.data?.roles || [])
const users = computed(() => props.data?.users || [])
const menus = computed(() => (props.data?.menus || []).filter(m => m.type !== 'F'))
const expanded = computed(() => !!activeNode.value)

const DIALS = { role: { x: 500, y: 150 }, user: { x: 152, y: 318 }, menu: { x: 918, y: 318 } }
const R = DIALS.role
const O = { x: 420, y: 290 }
const U = DIALS.user
const M = DIALS.menu

const roleLeft = '38.4%'
const userLeft = '19%'
const menuLeft = '64%'
const pageLeft = '61%'
const rowTop = i => py(44 + i * 47)
const pageTop = i => py(52 + i * 47)
const userTop = i => py(44 + (i % 6) * 52)
const userLeftFor = i => (i < 6 ? userLeft : 'calc(19% + 278px)')
const menuTop = i => py(32 + (i % 8) * 36)
const menuLeftFor = i => (i < 8 ? menuLeft : 'calc(64% + 196px)')

const px = x => (x / 1460 * 100).toFixed(2) + '%'
const py = y => (y / 560 * 100).toFixed(2) + '%'
const at = (x, y, delay) => ({ left: px(x), top: py(y), animationDelay: delay + 's' })

const tipText = computed(() => {
  if (!activeNode.value) return '点击「用户」「角色」「菜单」任一节点,展开它的明细'
  if (activeNode.value === 'role' && stage.value >= 2) return '点空白处或面包屑可收起'
  return '点空白处可收起'
})

const scopeMap = { 1: '全部数据权限', 2: '自定义数据权限', 3: '本部门数据权限', 4: '本部门及以下', 5: '仅本人数据' }
const scopeLabel = v => scopeMap[v] || '数据权限'
const nodeNames = { role: '角色', user: '用户', menu: '菜单' }
const nodeLabel = computed(() => nodeNames[activeNode.value] || '')

const bars = computed(() => {
  if (!chain.value) return []
  const vs = chain.value.pages.slice(0, 12).map(p => Number(p.btnCount) || 0)
  const max = Math.max(1, ...vs)
  return vs.map(v => Math.round(16 + (v / max) * 84))
})

const userRows = computed(() => {
  const roleName = rid => (roles.value.find(r => r.id === rid) || {}).name
  const pairs = props.data?.pairs || []
  return users.value.map(u => {
    const rids = pairs.filter(p => Number(p.uid) === u.id).map(p => Number(p.rid))
    return { ...u, roleNames: rids.map(rid => roleName(rid)).filter(Boolean) }
  })
})

const menuRows = computed(() => {
  const pairs = props.data?.pairs || []
  const builtinCount = roles.value.filter(r => r.builtin === 1).length
  return menus.value.map(m => ({
    ...m,
    grant: m.type === 'F' ? 0 : pairs.filter(p => Number(p.mid) === m.id).length + builtinCount
  }))
})

function bez(a, b, bend = 0.22) {
  const mx = (a.x + b.x) / 2
  const my = (a.y + b.y) / 2
  const cx = mx + (b.y - a.y) * bend
  const cy = my - (b.x - a.x) * bend
  return `M ${a.x} ${a.y} Q ${cx} ${cy} ${b.x} ${b.y}`
}

const baseLinks = computed(() => [
  bez({ x: U.x + 40, y: U.y - 28 }, { x: O.x - 46, y: O.y - 20 }),
  bez({ x: R.x + 10, y: R.y - 42 }, { x: O.x + 10, y: O.y - 52 }),
  bez({ x: O.x + 46, y: O.y - 12 }, { x: M.x - 42, y: M.y - 24 })
])

const baseParticles = computed(() => baseLinks.value.slice(0, 3).map((d, i) => ({ path: d, dur: 3.2 + i * 0.7, begin: i * 1.1 })))

function arcDots(n, node, radius, a0, a1) {
  const arr = []
  for (let i = 0; i < n; i++) {
    const t = n === 1 ? 0.5 : i / (n - 1)
    const a = ((a0 + (a1 - a0) * t) * Math.PI) / 180
    arr.push({ x: node.x + radius * Math.cos(a), y: node.y + radius * Math.sin(a) })
  }
  return arr
}
const userDots = computed(() => arcDots(Math.min(users.value.length, 12), U, 62, -200, -20))

const roleFan = computed(() => {
  const n = Math.max(roles.value.length, 4)
  const arr = []
  for (let i = 0; i < n; i++) {
    const t = n === 1 ? 0.5 : i / (n - 1)
    const ang = (-205 + t * 130) * Math.PI / 180
    const len = 42 + (i % 3) * 20 + t * 14
    const x0 = R.x + 4
    const y0 = R.y - 38
    arr.push({ line: `M ${x0} ${y0} L ${x0 + len * Math.cos(ang)} ${y0 + len * Math.sin(ang)}`, x: x0 + len * Math.cos(ang), y: y0 + len * Math.sin(ang) })
  }
  return arr
})

const menuFan = computed(() => {
  const n = Math.min(Math.max(menus.value.length, 6), 14)
  const arr = []
  for (let i = 0; i < n; i++) {
    const t = n === 1 ? 0.5 : i / (n - 1)
    const ang = (-55 + t * 100) * Math.PI / 180
    const len = 44 + (i % 3) * 18 + t * 12
    const x0 = M.x + 40
    const y0 = M.y - 6
    arr.push({ line: `M ${x0} ${y0} L ${x0 + len * Math.cos(ang)} ${y0 + len * Math.sin(ang) * 0.85}`, x: x0 + len * Math.cos(ang), y: y0 + len * Math.sin(ang) * 0.85 })
  }
  return arr
})

function dashStyle(i, base, len = 1600) {
  return {
    strokeDasharray: len,
    strokeDashoffset: len,
    animation: `smDraw 0.8s ${base + i * 0.07}s cubic-bezier(0.3, 0.8, 0.4, 1) forwards`
  }
}

function reset() {
  activeNode.value = ''
  stage.value = 0
  sel.value = null
  chain.value = null
  rowLinks.value = []
  pageLinks.value = []
}

function clickDial(name) {
  if (activeNode.value === name && !(name === 'role' && stage.value >= 1 && false)) {
    reset()
    return
  }
  activeNode.value = name
  sel.value = null
  chain.value = null
  pageLinks.value = []
  rowLinks.value = []
  if (name === 'role') stage.value = 1
  nextTick(() => setTimeout(buildAll, 80))
}

function backToRoleList() {
  stage.value = 1
  sel.value = null
  chain.value = null
  pageLinks.value = []
  nextTick(() => setTimeout(buildAll, 80))
}

function onSvgClick() {
  if (activeNode.value === 'role' && stage.value >= 2) backToRoleList()
  else if (expanded.value) reset()
}

async function pickRole(r) {
  if (loading.value) return
  loading.value = r.id
  try {
    const d = await api.roleChain(r.id)
    sel.value = r
    chain.value = d
    stage.value = 2
    await nextTick()
    setTimeout(buildAll, 120)
  } catch (e) {
    toast(e.message, 'err')
  } finally {
    loading.value = null
  }
}

function hintFor(name) {
  if (activeNode.value === name) {
    if (name === 'role' && stage.value >= 2) return ''
    return '再点收起'
  }
  return expanded.value ? '' : '点击展开'
}
function hintText() {
  return ''
}

/** 运行时测量行位置,生成 dial→行 / 胶囊→页面行 连线(viewBox 坐标) */
function buildAll() {
  if (!canvasEl.value) return
  const cv = canvasEl.value
  const w = cv.clientWidth, h = cv.clientHeight
  if (!w || !h) return
  const sx = 1460 / w, sy = 560 / h
  const sel2 = s => cv.querySelector(s)

  const dial = DIALS[activeNode.value]
  const rowSel = { role: '.rrow', user: '.urow', menu: '.mrow' }[activeNode.value]
  const tone = { role: '', user: 'blue', menu: 'green' }[activeNode.value] || ''
  const dot = { role: 'var(--violet)', user: 'var(--blue)', menu: 'var(--acc)' }[activeNode.value]
  const anchor = { role: [R.x + 34, R.y - 6], user: [U.x + 38, U.y - 8], menu: [M.x + 40, M.y - 6] }[activeNode.value]

  if (dial && rowSel) {
    const [x1, y1] = anchor
    rowLinks.value = [...cv.querySelectorAll(rowSel)].map(el => {
      const x2 = (el.offsetLeft - 8) * sx
      const y2 = (el.offsetTop + el.offsetHeight / 2) * sy
      const mx = (x1 + x2) / 2
      return {
        rid: Number(el.dataset.rid),
        tone: activeNode.value === 'menu' ? 'green' : activeNode.value === 'user' ? 'blue' : '',
        dot,
        d: `M ${x1} ${y1} C ${mx} ${y1}, ${mx} ${y2}, ${x2} ${y2}`
      }
    })
  } else {
    rowLinks.value = []
  }

  if (activeNode.value === 'role' && stage.value >= 2) {
    const selEl = activeNode.value === 'role' ? cv.querySelector('.rrow.sel') : null
    if (selEl) {
      const x0 = (selEl.offsetLeft + selEl.offsetWidth + 4) * sx
      const y0 = (selEl.offsetTop + selEl.offsetHeight / 2) * sy
      pageLinks.value = [...cv.querySelectorAll('.prow')].map(el => {
        const x1 = (el.offsetLeft - 8) * sx
        const y1 = (el.offsetTop + el.offsetHeight / 2) * sy
        const mx = (x0 + x1) / 2
        return { d: `M ${x0} ${y0} C ${mx} ${y0}, ${mx} ${y1}, ${x1} ${y1}` }
      })
    }
  } else {
    pageLinks.value = []
  }
  void sel2
}

function replay() {
  gen.value++
  reset()
  paused.value = false
}

function togglePause() {
  paused.value = !paused.value
  const s = svgEl.value
  if (s) paused.value ? s.pauseAnimations() : s.unpauseAnimations()
}

function uc(name) {
  const colors = ['#71b81f', '#3e7ef7', '#8b5cf6', '#e59819', '#0f9d8f', '#e5484d']
  let h = 0
  for (const c of name || '') h = (h * 31 + c.charCodeAt(0)) % 997
  return colors[h % colors.length]
}

const onRz = () => buildAll()
onMounted(() => window.addEventListener('resize', onRz))
onUnmounted(() => window.removeEventListener('resize', onRz))
</script>

<style scoped>
.sm { display: flex; flex-direction: column; height: 100%; }
.sm-hd { display: flex; align-items: center; justify-content: space-between; padding: 14px 18px 4px; gap: 10px; flex-wrap: wrap; }
.sm-crumb { display: flex; align-items: center; gap: 7px; }
.crlk { cursor: pointer; }
.crlk:hover { color: var(--acc-deep); }
.sep { margin: 0 3px; color: var(--txt3); }
.on { color: var(--txt); }
.sm-ops { display: flex; align-items: center; gap: 8px; flex-wrap: wrap; }
.sm-tip { margin-right: 4px; }
.ovc { display: inline-flex; gap: 6px; flex-wrap: wrap; margin-right: 6px; }
.oc {
  display: inline-flex; align-items: center; gap: 4px; font-size: 11px; color: var(--txt2);
  background: var(--card2); border: 1px solid var(--line); border-radius: 999px; padding: 3px 9px;
}
.oc b { font-size: 12px; }
.oc.wm { color: var(--amber); }
.oc.wm.hot { color: var(--amber); border-color: var(--amber); background: var(--amber-soft); }
.d { width: 6px; height: 6px; border-radius: 50%; }
.du { background: var(--blue); }
.dr { background: var(--violet); }
.dm { background: var(--acc); }
.sop {
  display: inline-flex; align-items: center; gap: 6px; border: 1px solid var(--line);
  background: var(--card2); color: var(--txt2); border-radius: 999px; padding: 6px 14px;
  cursor: pointer; font-size: 12px; transition: all 0.2s; font-family: inherit;
}
.sop:hover { border-color: var(--acc); color: var(--acc-deep); }

.sm-stage { position: relative; flex: 1; min-height: 460px; overflow: hidden; display: flex; align-items: center; justify-content: center; }
.sm-canvas { position: relative; width: 100%; aspect-ratio: 1460 / 560; flex: none; }
.sm-svg { position: absolute; inset: 0; width: 100%; height: 100%; }
.paused >>> * { animation-play-state: paused !important; }

.panlk { fill: none; stroke: url(#panG); stroke-width: 2; }
.rlink { fill: none; stroke: rgba(139, 92, 246, 0.35); stroke-width: 1.4; transition: stroke 0.3s, opacity 0.3s; }
.rlink.hot { stroke: var(--violet); stroke-width: 2.2; }
.rlink.dimmed { stroke: rgba(139, 92, 246, 0.1); }
.rlink.blue { stroke: rgba(62, 126, 247, 0.4); }
.rlink.green { stroke: rgba(113, 184, 31, 0.45); }
.plink { fill: none; stroke: rgba(113, 184, 31, 0.5); stroke-width: 1.6; }

.dial, .orbw { position: absolute; transform: translate(-50%, -50%); width: 128px; text-align: center; z-index: 3; transition: opacity 0.4s, filter 0.4s; }
.dial.dim, .orbw.dim { opacity: 0.35; }
.dial.clickable { cursor: pointer; }
.dial.clickable:hover .dring { border-color: var(--acc); transform: scale(1.06); box-shadow: 0 0 0 6px var(--acc-soft); }
.dring {
  width: 74px; height: 74px; margin: 0 auto; border-radius: 50%; border: 1px solid var(--line);
  background: var(--card); box-shadow: var(--shadow); position: relative;
  display: flex; align-items: center; justify-content: center; transition: all 0.25s;
}
.dring::before {
  content: ''; position: absolute; inset: 5px; border-radius: 50%;
  border: 2px dashed var(--acc); opacity: 0.5; animation: rotate 30s linear infinite;
}
.dnum { font-weight: 700; font-size: 18px; }
.dname { margin-top: 7px; font-weight: 600; font-size: 13.5px; }
.dsub { font-size: 10.5px; color: var(--txt3); margin-top: 2px; }
.hint {
  position: absolute; left: 50%; transform: translateX(-50%); bottom: -20px; white-space: nowrap;
  color: var(--acc-deep); background: var(--acc-soft); border-radius: 999px; padding: 2px 10px;
  animation: floaty 1.6s ease-in-out infinite;
}
.orb {
  width: 84px; height: 84px; margin: 0 auto; border-radius: 50%; position: relative;
  background: radial-gradient(circle at 38% 32%, #d9f3ae, #8fd634 55%, #4e8f0c);
  box-shadow: 0 10px 40px var(--glow);
}
.orb-dash {
  position: absolute; inset: -10px; border-radius: 50%;
  border: 2px dashed rgba(143, 214, 52, 0.45); animation: rotate 26s linear infinite;
}

/* 散开的行:用户 / 角色 / 菜单 共用 */
.nrow {
  position: absolute; z-index: 5; width: 262px; height: 7.8%; min-height: 40px;
  display: flex; align-items: center; gap: 10px; padding: 4px 14px;
  background: var(--card); border: 1.5px solid var(--line); border-radius: 999px;
  cursor: pointer; transition: all 0.25s;
  animation: rowIn 0.5s cubic-bezier(0.2, 1, 0.4, 1) backwards;
  box-shadow: var(--shadow);
}
.nrow:hover { border-color: var(--acc); transform: translateX(4px); }
.nrow.sel { border-color: var(--acc); box-shadow: 0 0 0 5px var(--acc-soft), var(--shadow-lg); transform: scale(1.05); z-index: 6; }
.nrow.dimmed { opacity: 0.3; pointer-events: none; }
.nrow.loading { opacity: 0.6; pointer-events: none; }
.nrow.loading .rc i { animation: pulse 0.8s infinite; }
.nav { width: 26px; height: 26px; font-size: 11.5px; flex: none; }
.rc {
  width: 16px; height: 16px; border-radius: 50%; border: 1.5px solid var(--line); flex: none;
  display: inline-flex; align-items: center; justify-content: center; transition: all 0.2s; background: var(--card);
}
.rc i { width: 7px; height: 7px; border-radius: 50%; background: transparent; display: block; }
.rc.gm { border-color: rgba(113, 184, 31, 0.5); }
.rc.gm i { background: var(--acc); }
.nrow:hover .rc { border-color: var(--acc); }
.nrow:hover .rc i, .nrow.sel .rc i { background: var(--acc); }
.rt { flex: 1; line-height: 1.25; min-width: 0; }
.rt b { display: block; font-size: 12.5px; }
.rt .tiny { font-size: 10.5px; display: block; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.rt.one { display: flex; align-items: center; gap: 6px; }
.rt.one b { display: inline; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.rt.one .tiny { white-space: nowrap; }
.rt.one .code { font-family: Consolas, monospace; font-size: 10px; font-style: normal; color: var(--txt3); }
.rla { color: var(--txt3); }

/* 授权页面行 */
.pf-x {
  position: absolute; right: 292px; top: 3.5%; z-index: 7;
  border: 1px solid var(--line); background: var(--card); color: var(--txt2); width: 26px; height: 26px;
  border-radius: 50%; cursor: pointer; display: inline-flex; align-items: center; justify-content: center;
  transition: all 0.2s;
}
.pf-x:hover { background: var(--red-soft); color: var(--red); border-color: transparent; transform: rotate(90deg); }
.prow {
  position: absolute; z-index: 5; width: 196px; height: 7.8%; min-height: 40px;
  display: flex; align-items: center; gap: 8px; padding: 4px 13px;
  background: var(--card); border: 1px solid var(--line); border-radius: 12px;
  box-shadow: var(--shadow); animation: rowIn 0.45s cubic-bezier(0.2, 1, 0.4, 1) backwards;
  transition: transform 0.2s, border-color 0.2s;
}
.mrow { height: 6%; min-height: 24px; padding: 2px 12px; border-radius: 10px; }
.mrow .chip { font-size: 10.5px; padding: 1px 7px; }
.prow:hover { transform: translateX(4px); border-color: var(--acc); }
.pg-dot { width: 7px; height: 7px; border-radius: 50%; background: var(--acc); box-shadow: 0 0 8px var(--glow); flex: none; }
.prow b { font-size: 12.5px; flex: 1; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }

.rp {
  position: absolute; right: 8px; top: 12px; bottom: 12px; width: 272px; z-index: 8;
  background: var(--card); border: 1px solid var(--line); border-radius: 18px;
  box-shadow: var(--shadow-lg); padding: 16px 16px; overflow: auto;
}
.pnl-enter-active { transition: all 0.45s cubic-bezier(0.25, 1, 0.4, 1); }
.pnl-enter-from { opacity: 0; transform: translateX(46px); }
.rp-hd { display: flex; align-items: center; gap: 8px; margin-bottom: 6px; }
.rp-x {
  margin-left: auto; width: 28px; height: 28px; border-radius: 50%; border: 0;
  background: var(--card2); color: var(--txt2); cursor: pointer;
  display: inline-flex; align-items: center; justify-content: center;
}
.rp-x:hover { background: var(--red-soft); color: var(--red); }
.rp-card {
  margin: 12px 0; background: var(--card2); border: 1px solid var(--line2);
  border-radius: 14px; padding: 12px 14px;
}
.rp-bignum { display: flex; align-items: baseline; gap: 7px; }
.rp-bars { display: flex; align-items: flex-end; gap: 3px; height: 36px; margin: 10px 0 6px; }
.rp-bars i {
  flex: 1; border-radius: 2px 2px 0 0;
  background: linear-gradient(180deg, var(--acc), var(--acc-deep));
  animation: barUp 0.6s cubic-bezier(0.2, 1, 0.4, 1) backwards;
}
.rp-grid { display: grid; grid-template-columns: 1fr 1fr 1fr; gap: 9px; margin-bottom: 12px; }
.rp-k {
  background: var(--card2); border-radius: 12px; padding: 9px 11px;
  display: flex; flex-direction: column; align-items: flex-start; gap: 3px;
}
.rp-remark {
  border-left: 3px solid var(--acc); background: var(--acc-soft);
  border-radius: 0 10px 10px 0; padding: 8px 12px; margin-bottom: 12px; line-height: 1.7;
}
.rp-sec { margin-bottom: 12px; }
.rp-lbl { font-size: 11.5px; color: var(--txt3); letter-spacing: 1px; margin-bottom: 8px; }
.rp-chips { display: flex; flex-direction: column; gap: 8px; }
.rp-u { display: flex; align-items: center; gap: 8px; font-size: 13px; }
.rp-u .avatar { width: 26px; height: 26px; font-size: 11.5px; }
.rp-u i { font-style: normal; margin-left: auto; }
.rp-pages { display: flex; flex-wrap: wrap; gap: 6px; }
.rp-pg {
  display: inline-flex; align-items: center; gap: 6px; font-size: 12px;
  border: 1px solid var(--line); border-radius: 999px; padding: 3px 10px;
}

.pop { animation: popIn 0.55s cubic-bezier(0.2, 1.3, 0.4, 1) backwards; }
@keyframes popIn { from { opacity: 0; transform: translate(-50%, -50%) scale(0.3); } to { opacity: 1; transform: translate(-50%, -50%) scale(1); } }
@keyframes rowIn { from { opacity: 0; transform: translateX(30px); } to { opacity: 1; transform: none; } }
@keyframes barUp { from { height: 8% !important; opacity: 0; } }
@keyframes dotIn { from { opacity: 0; } }
@keyframes rotate { to { transform: rotate(360deg); } }
@keyframes floaty { 0%, 100% { transform: translate(-50%, 0); } 50% { transform: translate(-50%, -4px); } }
</style>

<style>
/* 内联 animation 字符串引用的 keyframes 必须全局,scoped 会改名导致失效 */
@keyframes smDraw { to { stroke-dashoffset: 0; } }
</style>
