package com.adminbase.controller;

import com.adminbase.common.*;
import com.adminbase.domain.Role;
import com.adminbase.mapper.RoleMapper;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/system/roles")
public class RoleController {

    private final RoleMapper roles;
    private final com.adminbase.mapper.MenuMapper menus;

    public RoleController(RoleMapper roles, com.adminbase.mapper.MenuMapper menus) {
        this.roles = roles;
        this.menus = menus;
    }

    @GetMapping
    @RequiresPerm("system:role:list")
    public R<PageResult<Role>> list(PageQ q) {
        return R.ok(PageResult.of(roles.count(q), roles.page(q)));
    }

    @GetMapping("/all")
    public R<List<Role>> all() {
        return R.ok(roles.all());
    }

    @GetMapping("/matrix")
    @RequiresPerm("system:role:list")
    public R<Map<String, Object>> matrix() {
        Map<String, Object> m = new HashMap<String, Object>();
        m.put("roles", roles.all());
        m.put("pairs", roles.roleMenuPairs());
        return R.ok(m);
    }

    @PostMapping
    @RequiresPerm("system:role:add")
    @OperLog(module = "角色管理", action = "新增角色")
    public R<Void> add(@RequestBody Role r) {
        if (r.getStatus() == null) r.setStatus("0");
        roles.insert(r);
        if (r.getMenuIds() != null) {
            roles.clearRoleMenus(r.getId());
            if (!r.getMenuIds().isEmpty()) roles.setRoleMenus(r.getId(), r.getMenuIds());
        }
        return R.ok();
    }

    @PutMapping("/{id}")
    @RequiresPerm("system:role:edit")
    @OperLog(module = "角色管理", action = "修改角色")
    public R<Void> update(@PathVariable Long id, @RequestBody Role r) {
        r.setId(id);
        roles.update(r);
        if (r.getMenuIds() != null) {
            roles.clearRoleMenus(id);
            if (!r.getMenuIds().isEmpty()) roles.setRoleMenus(id, r.getMenuIds());
        }
        return R.ok();
    }

    @DeleteMapping("/{id}")
    @RequiresPerm("system:role:remove")
    @OperLog(module = "角色管理", action = "删除角色")
    public R<Void> delete(@PathVariable Long id) {
        Role r = roles.byId(id);
        if (r == null) throw new BizException("角色不存在");
        if (r.getBuiltin() != null && r.getBuiltin() == 1) throw new BizException("内置角色不允许删除");
        if (roles.userCount(id) > 0) throw new BizException("角色下存在用户,无法删除");
        roles.delete(id);
        roles.clearRoleMenus(id);
        return R.ok();
    }

    @GetMapping("/{id}/menus")
    @RequiresPerm("system:role:list")
    public R<List<Long>> menuIds(@PathVariable Long id) {
        Role r = roles.byId(id);
        if (r != null && r.getBuiltin() != null && r.getBuiltin() == 1) {
            return R.ok(menus.allIds());
        }
        return R.ok(roles.menuIdsOf(id));
    }

    @PutMapping("/{id}/menus")
    @RequiresPerm("system:role:perm")
    @OperLog(module = "角色管理", action = "分配权限")
    public R<Void> assignMenus(@PathVariable Long id, @RequestBody Map<String, List<Long>> body) {
        roles.clearRoleMenus(id);
        List<Long> mids = body.get("menuIds");
        if (mids != null && !mids.isEmpty()) roles.setRoleMenus(id, mids);
        return R.ok();
    }
}
