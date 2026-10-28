import { get, post, put, del } from './request'

export const api = {
  captcha: () => get('/auth/captcha'),
  login: b => post('/auth/login', b),
  logout: () => post('/auth/logout'),
  me: () => get('/auth/me'),

  users: q => get('/system/users', q),
  user: id => get('/system/users/' + id),
  addUser: b => post('/system/users', b),
  updateUser: (id, b) => put('/system/users/' + id, b),
  delUsers: ids => del('/system/users', ids),
  resetPwd: (id, pwd) => put(`/system/users/${id}/pwd`, { password: pwd }),
  assignRoles: (id, roleIds) => put(`/system/users/${id}/roles`, { roleIds }),

  roles: q => get('/system/roles', q),
  allRoles: () => get('/system/roles/all'),
  roleMatrix: () => get('/system/roles/matrix'),
  addRole: b => post('/system/roles', b),
  updateRole: (id, b) => put('/system/roles/' + id, b),
  delRole: id => del('/system/roles/' + id),
  roleMenus: id => get(`/system/roles/${id}/menus`),
  assignMenus: (id, menuIds) => put(`/system/roles/${id}/menus`, { menuIds }),

  menus: () => get('/system/menus'),
  addMenu: b => post('/system/menus', b),
  updateMenu: (id, b) => put('/system/menus/' + id, b),
  delMenu: id => del('/system/menus/' + id),

  depts: () => get('/system/depts'),
  deptMembers: id => get(`/system/depts/${id}/members`),
  addDept: b => post('/system/depts', b),
  updateDept: (id, b) => put('/system/depts/' + id, b),
  delDept: id => del('/system/depts/' + id),

  posts: q => get('/system/posts', q),
  allPosts: () => get('/system/posts/all'),
  addPost: b => post('/system/posts', b),
  updatePost: (id, b) => put('/system/posts/' + id, b),
  delPost: id => del('/system/posts/' + id),

  dicts: () => get('/system/dicts'),
  addDictType: b => post('/system/dicts/type', b),
  updateDictType: (id, b) => put('/system/dicts/type/' + id, b),
  delDictType: id => del('/system/dicts/type/' + id),
  addDictData: b => post('/system/dicts/data', b),
  updateDictData: (id, b) => put('/system/dicts/data/' + id, b),
  delDictData: id => del('/system/dicts/data/' + id),
  dictMap: () => get('/system/dicts/map'),

  configs: () => get('/system/configs'),
  saveConfig: (id, b) => put('/system/configs/' + id, b),
  saveConfigBatch: items => put('/system/configs/batch', items),
  addConfig: b => post('/system/configs', b),
  delConfig: id => del('/system/configs/' + id),

  oplogs: q => get('/monitor/oplogs', q),
  oplog: id => get('/monitor/oplogs/' + id),
  delOplogs: ids => del('/monitor/oplogs', ids),
  opTrend: r => get('/monitor/oplogs/trend', { range: r }),

  loginlogs: q => get('/monitor/loginlogs', q),
  logAnalytics: r => get('/monitor/loginlogs/analytics', { range: r }),
  delLoginlogs: ids => del('/monitor/loginlogs', ids),

  pulse: r => get('/dashboard/pulse', { range: r }),
  summary: () => get('/dashboard/summary'),

  starmap: () => get('/trace/starmap'),
  roleChain: rid => get('/trace/role-chain', { rid }),
  whoCan: perm => get('/trace/who-can', { perm }),
  idleRoles: () => get('/trace/idle-roles'),
  unassignedUsers: () => get('/trace/unassigned-users'),
  orphanMenus: () => get('/trace/orphan-menus'),

  tables: kw => get('/tool/codegen/tables', { kw }),
  columns: t => get(`/tool/codegen/tables/${t}/columns`)
}
