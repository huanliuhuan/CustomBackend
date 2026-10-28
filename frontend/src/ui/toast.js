let cbs = []
let seq = 0

export function toast(msg, type = 'ok') {
  const id = ++seq
  cbs.forEach(f => f({ id, msg, type, act: 'add' }))
  setTimeout(() => cbs.forEach(f => f({ id, act: 'del' })), 2600)
}

export const _sub = f => { cbs.push(f); return () => { cbs = cbs.filter(x => x !== f) } }
