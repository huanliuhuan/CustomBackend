package com.adminbase.controller;

import com.adminbase.common.BizException;
import com.adminbase.common.OperLog;
import com.adminbase.common.R;
import com.adminbase.common.RequiresPerm;
import com.adminbase.domain.Dept;
import com.adminbase.mapper.DeptMapper;
import com.adminbase.service.DeptService;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/system/depts")
public class DeptController {

    private final DeptMapper depts;
    private final DeptService deptSvc;

    public DeptController(DeptMapper depts, DeptService deptSvc) {
        this.depts = depts;
        this.deptSvc = deptSvc;
    }

    @GetMapping
    @RequiresPerm("system:dept:list")
    public R<List<Dept>> tree() {
        return R.ok(deptSvc.tree());
    }

    @GetMapping("/{id}/members")
    @RequiresPerm("system:dept:list")
    public R<List<com.adminbase.domain.User>> members(@PathVariable Long id) {
        return R.ok(depts.members(id));
    }

    @PostMapping
    @RequiresPerm("system:dept:add")
    @OperLog(module = "部门管理", action = "新增部门")
    public R<Void> add(@RequestBody Dept d) {
        d.setAncestors(deptSvc.ancestorsOf(d.getParentId()));
        if (d.getStatus() == null) d.setStatus("0");
        depts.insert(d);
        return R.ok();
    }

    @PutMapping("/{id}")
    @RequiresPerm("system:dept:edit")
    @OperLog(module = "部门管理", action = "修改部门")
    public R<Void> update(@PathVariable Long id, @RequestBody Dept d) {
        Dept old = depts.byId(id);
        if (old == null) throw new BizException("部门不存在");
        boolean moved = d.getParentId() != null && !d.getParentId().equals(old.getParentId());
        d.setId(id);
        if (moved) {
            if (d.getParentId().equals(id)
                    || Arrays.asList(old.getAncestors().split(",")).contains(String.valueOf(id))) {
                throw new BizException("不能移动到自身或自己的下级");
            }
            String oldA = old.getAncestors();
            d.setAncestors(deptSvc.ancestorsOf(d.getParentId()));
            depts.update(d);
            depts.updateChildAncestors(id, oldA, d.getAncestors());
        } else {
            d.setParentId(null);
            d.setAncestors(null);
            depts.update(d);
        }
        return R.ok();
    }

    @DeleteMapping("/{id}")
    @RequiresPerm("system:dept:remove")
    @OperLog(module = "部门管理", action = "删除部门")
    public R<Void> delete(@PathVariable Long id) {
        deptSvc.checkDelete(id);
        depts.delete(id);
        return R.ok();
    }
}
