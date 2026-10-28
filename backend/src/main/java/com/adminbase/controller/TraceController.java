package com.adminbase.controller;

import com.adminbase.common.R;
import com.adminbase.domain.Role;
import com.adminbase.mapper.RoleMapper;
import com.adminbase.mapper.TraceMapper;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/trace")
public class TraceController {

    private final TraceMapper trace;
    private final RoleMapper roles;

    public TraceController(TraceMapper trace, RoleMapper roles) {
        this.trace = trace;
        this.roles = roles;
    }

    /** 角色授权链路:角色信息 + 授予页面(含各自按钮数)+ 持有用户 */
    @GetMapping("/role-chain")
    public R<Map<String, Object>> roleChain(@RequestParam Long rid) {
        Role r = roles.byId(rid);
        if (r == null) return R.fail("角色不存在");
        boolean builtin = r.getBuiltin() != null && r.getBuiltin() == 1;
        List<Map<String, Object>> pages = trace.rolePages(rid, builtin);
        List<Map<String, Object>> users = trace.roleUsers(rid);
        long btnSum = 0;
        for (Map<String, Object> p : pages) {
            if (p.get("btnCount") instanceof Number) btnSum += ((Number) p.get("btnCount")).longValue();
        }
        Map<String, Object> m = new HashMap<String, Object>();
        m.put("role", r);
        m.put("pages", pages);
        m.put("users", users);
        m.put("pageCount", pages.size());
        m.put("btnCount", btnSum);
        m.put("userCount", users.size());
        return R.ok(m);
    }

    @GetMapping("/starmap")
    public R<Map<String, Object>> starmap() {
        Map<String, Object> m = new HashMap<String, Object>();
        m.put("overview", trace.overview());
        m.put("roles", trace.roleStats());
        m.put("users", trace.userBrief());
        m.put("pairs", trace.userRolePairs());
        m.put("menus", trace.menuBrief());
        return R.ok(m);
    }

    @GetMapping("/who-can")
    public R<List<Map<String, Object>>> whoCan(@RequestParam String perm) {
        return R.ok(trace.whoCan(perm));
    }

    @GetMapping("/idle-roles")
    public R<List<Object>> idleRoles() {
        return R.ok(cast(trace.idleRoles()));
    }

    @GetMapping("/unassigned-users")
    public R<List<Object>> unassignedUsers() {
        return R.ok(cast(trace.unassignedUsers()));
    }

    @GetMapping("/orphan-menus")
    public R<List<Object>> orphanMenus() {
        return R.ok(cast(trace.orphanMenus()));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private List<Object> cast(List<?> l) {
        return (List) l;
    }
}
