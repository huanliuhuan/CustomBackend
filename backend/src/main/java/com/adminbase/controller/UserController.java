package com.adminbase.controller;

import com.adminbase.common.*;
import com.adminbase.domain.User;
import com.adminbase.mapper.DeptMapper;
import com.adminbase.mapper.PostMapper;
import com.adminbase.mapper.RoleMapper;
import com.adminbase.mapper.UserMapper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/system/users")
public class UserController {

    private final UserMapper users;
    private final RoleMapper roles;
    private final PostMapper posts;
    private final DeptMapper depts;
    private final com.adminbase.service.AuthService cfg;
    private final BCryptPasswordEncoder encoder;

    public UserController(UserMapper users, RoleMapper roles, PostMapper posts, DeptMapper depts,
                          com.adminbase.service.AuthService cfg, BCryptPasswordEncoder encoder) {
        this.users = users;
        this.roles = roles;
        this.posts = posts;
        this.depts = depts;
        this.cfg = cfg;
        this.encoder = encoder;
    }

    private String initPwd() {
        String v = cfg.cfg("sys.user.initPassword");
        return v == null || v.isEmpty() ? "123456" : v;
    }

    @GetMapping
    @RequiresPerm("system:user:list")
    public R<PageResult<User>> list(PageQ q, Long deptId) {
        String scopeSql = scopeSql();
        return R.ok(PageResult.of(users.count(q, deptId, scopeSql), users.page(q, deptId, scopeSql)));
    }

    private String scopeSql() {
        UserCtx.Ctx c = UserCtx.get();
        if (c == null || c.superRole) return "";
        Integer s = c.scope;
        if (s == null) return "";
        switch (s) {
            case 3: return " and u.dept_id=" + c.deptId;
            case 4: return " and (u.dept_id=" + c.deptId + " or u.dept_id in (select id from sys_dept where find_in_set(" + c.deptId + ",ancestors)))";
            case 5: return " and u.id=" + c.uid;
            default: return "";
        }
    }

    @GetMapping("/{id}")
    @RequiresPerm("system:user:list")
    public R<User> detail(@PathVariable Long id) {
        User u = users.byId(id);
        if (u != null) {
            u.setRoleIds(users.roleIdsOf(id));
            u.setPostIds(users.postIdsOf(id));
        }
        return R.ok(u);
    }

    @PostMapping
    @RequiresPerm("system:user:add")
    @OperLog(module = "用户管理", action = "新增用户")
    public R<Void> add(@RequestBody User u) {
        if (users.byAccount(u.getAccount()) != null) throw new BizException("账号已存在");
        u.setPassword(encoder.encode(initPwd()));
        if (u.getStatus() == null) u.setStatus("0");
        users.insert(u);
        bind(u.getId(), u);
        return R.ok();
    }

    @PutMapping("/{id}")
    @RequiresPerm("system:user:edit")
    @OperLog(module = "用户管理", action = "修改用户")
    public R<Void> update(@PathVariable Long id, @RequestBody User u) {
        u.setId(id);
        users.update(u);
        if (u.getRoleIds() != null || u.getPostIds() != null) bind(id, u);
        return R.ok();
    }

    @DeleteMapping
    @RequiresPerm("system:user:remove")
    @OperLog(module = "用户管理", action = "删除用户")
    public R<Void> delete(@RequestBody List<Long> ids) {
        UserCtx.Ctx c = UserCtx.get();
        for (Long id : ids) {
            if (c != null && c.uid == id) throw new BizException("不能删除当前登录账号");
            User u = users.byId(id);
            if (u != null && "admin".equals(u.getAccount())) throw new BizException("内置账号不允许删除");
        }
        users.delete(ids);
        return R.ok();
    }

    @PutMapping("/{id}/pwd")
    @RequiresPerm("system:user:resetPwd")
    @OperLog(module = "用户管理", action = "重置密码")
    public R<Void> resetPwd(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String pwd = body.get("password");
        if (pwd == null || pwd.length() < 6) throw new BizException("密码至少6位");
        users.resetPwd(id, encoder.encode(pwd));
        return R.ok();
    }

    @PutMapping("/{id}/roles")
    @RequiresPerm("system:user:assignRole")
    @OperLog(module = "用户管理", action = "分配角色")
    public R<Void> assignRoles(@PathVariable Long id, @RequestBody Map<String, List<Long>> body) {
        users.clearUserRoles(id);
        List<Long> rids = body.get("roleIds");
        if (rids != null && !rids.isEmpty()) users.setUserRoles(id, rids);
        return R.ok();
    }

    private void bind(Long uid, User u) {
        users.clearUserRoles(uid);
        if (u.getRoleIds() != null && !u.getRoleIds().isEmpty()) users.setUserRoles(uid, u.getRoleIds());
        users.clearUserPosts(uid);
        if (u.getPostIds() != null && !u.getPostIds().isEmpty()) users.setUserPosts(uid, u.getPostIds());
    }
}
