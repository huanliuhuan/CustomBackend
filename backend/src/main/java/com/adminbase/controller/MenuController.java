package com.adminbase.controller;

import com.adminbase.common.BizException;
import com.adminbase.common.OperLog;
import com.adminbase.common.R;
import com.adminbase.common.RequiresPerm;
import com.adminbase.domain.Menu;
import com.adminbase.mapper.MenuMapper;
import com.adminbase.service.MenuService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/system/menus")
public class MenuController {

    private final MenuMapper menus;

    public MenuController(MenuMapper menus) {
        this.menus = menus;
    }

    @GetMapping
    @RequiresPerm("system:menu:list")
    public R<List<Menu>> tree() {
        return R.ok(MenuService.tree(menus.all()));
    }

    @PostMapping
    @RequiresPerm("system:menu:add")
    @OperLog(module = "菜单管理", action = "新增菜单")
    public R<Void> add(@RequestBody Menu m) {
        menus.insert(m);
        return R.ok();
    }

    @PutMapping("/{id}")
    @RequiresPerm("system:menu:edit")
    @OperLog(module = "菜单管理", action = "修改菜单")
    public R<Void> update(@PathVariable Long id, @RequestBody Menu m) {
        m.setId(id);
        menus.update(m);
        return R.ok();
    }

    @DeleteMapping("/{id}")
    @RequiresPerm("system:menu:remove")
    @OperLog(module = "菜单管理", action = "删除菜单")
    public R<Void> delete(@PathVariable Long id) {
        if (menus.childCount(id) > 0) throw new BizException("存在下级菜单,无法删除");
        if (menus.grantCount(id) > 0) throw new BizException("菜单已授予角色,先取消授权");
        menus.delete(id);
        return R.ok();
    }
}
