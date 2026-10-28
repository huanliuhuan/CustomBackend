<template>
  <div class="lg">
    <div class="fl" v-for="i in 14" :key="i" :style="flStyle(i)" />

    <div class="lg-left">
      <div class="lg-brand">
        <span class="lg-ver">通用后台底座 · v1.0.0</span>
        <span class="lg-step">01 / 登录</span>
      </div>

      <div class="lg-hero">
        <h1>统一的后台底座,<br />专注业务本身。</h1>
        <p>用户、角色、菜单、部门、字典与审计开箱即用,新业务模块只需关注字段与流程。</p>
      </div>

      <div class="lg-clock">
        <svg width="330" height="330" viewBox="0 0 330 330">
          <g v-for="(t, i) in ticks" :key="i">
            <line :x1="t.x1" :y1="t.y1" :x2="t.x2" :y2="t.y2"
                  :stroke="tickColor(i)" :stroke-width="tickW(i)" stroke-linecap="round"
                  :style="{ transition: 'all 0.6s ' + (i % 12) * 30 + 'ms' }" />
          </g>
        </svg>
        <div class="clk-c">
          <div class="clk-t">{{ nowStr }}</div>
          <div class="clk-d">{{ dateStr }}</div>
        </div>
      </div>

      <div class="lg-feats">
        <div class="ft">
          <div class="ft-h"><span class="ft-ico ok" />服务状态</div>
          <div class="ft-v green">● 运行正常</div>
          <div class="ft-s tiny muted">平均响应 11ms</div>
        </div>
        <div class="ft">
          <div class="ft-h"><AIcon name="role" :size="13" />角色与按钮权限</div>
          <div class="ft-s">菜单、按钮两级授权,前后端同一套权限码</div>
        </div>
        <div class="ft">
          <div class="ft-h"><AIcon name="shield" :size="13" />数据权限</div>
          <div class="ft-s">全部 / 本部门及下级 / 本部门 / 本人</div>
        </div>
        <div class="ft">
          <div class="ft-h"><AIcon name="operlog" :size="13" />操作审计</div>
          <div class="ft-s">登录日志与操作日志自动记录</div>
        </div>
        <div class="ft">
          <div class="ft-h"><AIcon name="code" :size="13" />代码生成</div>
          <div class="ft-s">导入数据表,生成业务模块开发骨架</div>
        </div>
      </div>
    </div>

    <div class="lg-right">
      <transition name="pop" appear>
        <div class="fp">
          <div class="fp-tag">账号登录</div>
          <h2>登录</h2>
          <p class="muted">使用管理员分配的账号登录系统</p>

          <div class="fld">
            <label>账号</label>
            <AInput v-model="form.account" icon="user" placeholder="请输入账号" @enter="submit" />
          </div>
          <div class="fld">
            <label>密码</label>
            <AInput v-model="form.password" icon="lock" type="password" placeholder="请输入密码" @enter="submit" />
          </div>
          <div v-if="captcha.enabled" class="fld row">
            <div class="fld grow">
              <label>验证码</label>
              <AInput v-model="form.code" icon="eye" placeholder="请输入右侧字符" @enter="submit" />
            </div>
            <img v-if="captcha.img" :src="captcha.img" class="cap" title="点击刷新" @click="loadCaptcha" />
          </div>

          <label class="rm">
            <input type="checkbox" v-model="form.remember" /><i class="ckb"><AIcon name="check" :size="11" /></i>
            记住登录状态
          </label>

          <button class="go" :class="st" :disabled="st !== 'idle'" @click="submit">
            <span v-if="st === 'idle'" class="go-t">登 录</span>
            <span v-else-if="st === 'orb'" class="orb"><span class="orb-ring" /></span>
            <span v-else class="okk"><AIcon name="check" :size="26" /></span>
          </button>
          <p class="hint tiny muted">演示账号 admin / admin123</p>
        </div>
      </transition>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, onUnmounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import AIcon from '../ui/AIcon.vue'
import AInput from '../ui/AInput.vue'
import { api } from '../api'
import { useAuth } from '../stores/auth'
import { toast } from '../ui/toast'

const router = useRouter()
const auth = useAuth()
const form = reactive({ account: 'admin', password: '', code: '', key: '', remember: true })
const captcha = reactive({ enabled: false, img: '' })
const st = ref('idle')
const now = ref(new Date())
let timer

const ticks = computed(() => {
  const arr = []
  const C = 165, R1 = 118, R2 = 148
  for (let i = 0; i < 60; i++) {
    const a = (i * 6 - 90) * Math.PI / 180
    const big = i % 5 === 0
    const r1 = big ? R1 - 8 : R1
    arr.push({
      x1: C + r1 * Math.cos(a), y1: C + r1 * Math.sin(a),
      x2: C + R2 * Math.cos(a), y2: C + R2 * Math.sin(a)
    })
  }
  return arr
})
const secTick = computed(() => Math.floor(now.value.getSeconds() * 60 / 60))
const minTick = computed(() => now.value.getMinutes())

function tickColor(i) {
  const s = now.value.getSeconds()
  const bright = i === Math.floor(s)
  const warm = i <= s && i > s - 7
  if (bright) return 'var(--glow)'
  if (warm) return 'rgba(143, 214, 52, 0.85)'
  return i % 5 === 0 ? 'rgba(160, 200, 120, 0.5)' : 'rgba(140, 180, 110, 0.22)'
}
function tickW(i) {
  const s = now.value.getSeconds()
  if (i === Math.floor(s)) return 4.5
  return i % 5 === 0 ? 3 : 2
}
const nowStr = computed(() => {
  const d = now.value
  const p = n => String(n).padStart(2, '0')
  return p(d.getHours()) + ':' + p(d.getMinutes()) + ':' + p(d.getSeconds())
})
const dateStr = computed(() => {
  const d = now.value
  return d.getFullYear() + '.' + String(d.getMonth() + 1).padStart(2, '0') + '.' + String(d.getDate()).padStart(2, '0') + ' · ' + '周' + '日一二三四五六'[d.getDay()]
})

function flStyle(i) {
  return {
    left: (i * 67) % 96 + '%',
    top: (i * 37) % 88 + '%',
    animationDelay: (i * 0.7) + 's',
    animationDuration: (5 + (i % 4)) + 's',
    width: 3 + (i % 3) * 2 + 'px',
    height: 3 + (i % 3) * 2 + 'px'
  }
}

async function loadCaptcha() {
  const d = await api.captcha()
  captcha.enabled = !!d.enabled
  captcha.img = d.img
  form.key = d.key
  form.code = ''
}

async function submit() {
  if (st.value !== 'idle') return
  if (!form.account || !form.password) { toast('请输入账号和密码', 'err'); return }
  st.value = 'orb'
  try {
    await auth.login({ ...form })
    setTimeout(() => { st.value = 'ok' }, 450)
    setTimeout(() => {
      router.push('/')
      toast('欢迎回来,' + (auth.user?.nickName || ''))
    }, 1050)
  } catch (e) {
    st.value = 'idle'
    toast(e.message, 'err')
    loadCaptcha()
  }
}

onMounted(() => {
  loadCaptcha()
  timer = setInterval(() => { now.value = new Date() }, 1000)
})
onUnmounted(() => clearInterval(timer))
</script>

<style scoped>
.lg {
  position: relative; height: 100vh; overflow: hidden;
  background: radial-gradient(1200px 700px at 75% -10%, #16220f 0%, transparent 55%),
              radial-gradient(900px 600px at 10% 110%, #0f1a0c 0%, transparent 60%),
              #070a07;
  display: grid; grid-template-columns: 1.25fr 1fr; color: #e9eee6;
}
.fl {
  position: absolute; border-radius: 50%; background: rgba(150, 220, 90, 0.5);
  animation: floaty 6s ease-in-out infinite; pointer-events: none;
}
.lg-left { padding: 30px 48px; display: flex; flex-direction: column; position: relative; z-index: 2; min-width: 0; }
.lg-brand { display: flex; justify-content: space-between; align-items: center; }
.lg-ver { font-size: 12px; letter-spacing: 1px; color: #93a48d; }
.lg-step {
  font-size: 11.5px; letter-spacing: 2px; color: #b7e07e;
  border: 1px solid rgba(160, 220, 100, 0.3); padding: 4px 12px; border-radius: 999px;
}
.lg-hero { margin-top: 7vh; max-width: 46%; }
.lg-hero h1 { font-size: 44px; line-height: 1.28; letter-spacing: 1px; font-weight: 700; }
.lg-hero p { margin-top: 16px; color: #9aa896; font-size: 14.5px; max-width: 480px; line-height: 1.8; }
.lg-clock { position: absolute; right: 3vw; top: 16vh; }
.clk-c { position: absolute; inset: 0; display: flex; flex-direction: column; align-items: center; justify-content: center; }
.clk-t { font-size: 42px; font-weight: 200; letter-spacing: 4px; font-variant-numeric: tabular-nums; }
.clk-d { margin-top: 4px; font-size: 12px; color: #8fa287; letter-spacing: 3px; }
.lg-feats { margin-top: auto; display: grid; grid-template-columns: repeat(5, 1fr); gap: 10px; padding-bottom: 12px; }
.ft {
  border: 1px solid rgba(150, 200, 120, 0.14); background: rgba(20, 28, 16, 0.5);
  border-radius: 16px; padding: 13px 14px; backdrop-filter: blur(6px);
  transition: all 0.3s;
}
.ft:hover { transform: translateY(-4px); border-color: rgba(160, 220, 100, 0.35); }
.ft-h { display: flex; align-items: center; gap: 6px; font-size: 12.5px; color: #cfe0c4; margin-bottom: 7px; }
.ft-v { font-size: 15px; font-weight: 700; }
.ft-s { font-size: 11px; color: #87987f; line-height: 1.6; }
.green { color: #9fe05a; }

.lg-right { display: flex; align-items: center; justify-content: center; padding: 30px 4vw; position: relative; z-index: 2; }
.fp {
  width: 400px; max-width: 100%; background: #fbfcfa; color: #1d2126;
  border-radius: 26px; padding: 38px 40px 30px; box-shadow: 0 30px 80px rgba(0, 0, 0, 0.55);
}
.fp-tag { font-size: 11.5px; letter-spacing: 1.5px; color: #98a38f; }
.fp h2 { font-size: 30px; margin: 8px 0 4px; }
.fp > p { font-size: 13px; color: #7d8678; }
.fld { margin-top: 18px; }
.fld.row { display: flex; gap: 10px; align-items: flex-end; }
.fld.grow { flex: 1; }
.fld label { display: block; font-size: 12.5px; color: #5f6a58; margin-bottom: 7px; }
.fld :deep(.ai) { background: #fff; border-color: #e3e7de; height: 42px; }
.cap { height: 42px; border-radius: 999px; cursor: pointer; border: 1px solid #e3e7de; }
.rm { display: flex; align-items: center; gap: 8px; margin: 18px 0 6px; font-size: 13px; color: #4c564a; cursor: pointer; user-select: none; }
.rm input { display: none; }
.ckb {
  width: 17px; height: 17px; border-radius: 6px; border: 1.5px solid #cfd6c8;
  display: inline-flex; align-items: center; justify-content: center; color: transparent; transition: all 0.2s;
}
.rm input:checked + .ckb { background: var(--acc, #71b81f); border-color: transparent; color: #fff; }
.go {
  width: 100%; height: 46px; border: 0; border-radius: 999px; cursor: pointer; margin-top: 14px;
  background: linear-gradient(135deg, #79c22a, #4e8f0c); color: #fff; font-size: 15.5px; letter-spacing: 6px;
  transition: all 0.5s cubic-bezier(0.3, 1.2, 0.4, 1); position: relative; font-family: inherit;
}
.go:hover:not(:disabled) { filter: brightness(1.07); transform: translateY(-1px); box-shadow: 0 10px 26px rgba(113, 184, 31, 0.45); }
.go.orb { width: 46px; height: 46px; margin: 14px auto 0; display: block; letter-spacing: 0; animation: orbIn 0.45s cubic-bezier(0.3, 1.3, 0.4, 1); box-shadow: 0 0 0 8px rgba(143, 214, 52, 0.18), 0 0 30px rgba(143, 214, 52, 0.5); }
.go.okk { background: linear-gradient(135deg, #79c22a, #4e8f0c); }
.orb { position: absolute; inset: 0; display: flex; align-items: center; justify-content: center; }
.orb-ring { position: absolute; inset: -9px; border-radius: 50%; border: 2px solid rgba(160, 230, 90, 0.5); animation: ringPulse 1s ease-out infinite; }
.okk { position: absolute; inset: 0; display: flex; align-items: center; justify-content: center; animation: okIn 0.35s cubic-bezier(0.2, 1.6, 0.4, 1); }
@keyframes orbIn { from { width: 100%; } to { width: 46px; } }
@keyframes ringPulse { from { transform: scale(0.9); opacity: 1; } to { transform: scale(1.35); opacity: 0; } }
@keyframes okIn { from { transform: scale(0.3); opacity: 0; } to { transform: scale(1); opacity: 1; } }
.hint { text-align: center; margin-top: 14px; }

@media (max-width: 1080px) {
  .lg { grid-template-columns: 1fr; }
  .lg-left { display: none; }
}
</style>
