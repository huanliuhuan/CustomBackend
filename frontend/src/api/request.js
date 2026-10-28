const BASE = '/api'

export function getToken() {
  return localStorage.getItem('ab_token') || ''
}

export async function req(method, url, body, opts = {}) {
  const h = { 'Content-Type': 'application/json' }
  const t = getToken()
  if (t) h.Authorization = 'Bearer ' + t
  const res = await fetch(BASE + url, {
    method, headers: h,
    body: body === undefined ? undefined : JSON.stringify(body)
  })
  if (res.status === 401) {
    localStorage.removeItem('ab_token')
    if (!location.hash.includes('login')) location.hash = '#/login'
    throw new Error('登录已过期')
  }
  const j = await res.json().catch(() => ({ code: 500, msg: '响应解析失败' }))
  if (res.status === 403) throw new Error(j.msg || '无权限')
  if (j.code !== 200) throw new Error(j.msg || '请求失败')
  return j.data
}

export const get = (url, params) => {
  const q = params ? '?' + new URLSearchParams(clean(params)).toString() : ''
  return req('GET', url + q)
}
export const post = (url, body) => req('POST', url, body)
export const put = (url, body) => req('PUT', url, body)
export const del = (url, body) => req('DELETE', url, body)

function clean(o) {
  const r = {}
  for (const k in o) if (o[k] !== undefined && o[k] !== null && o[k] !== '') r[k] = o[k]
  return r
}
