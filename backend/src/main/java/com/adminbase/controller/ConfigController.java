package com.adminbase.controller;

import com.adminbase.common.OperLog;
import com.adminbase.common.R;
import com.adminbase.common.RequiresPerm;
import com.adminbase.domain.Config;
import com.adminbase.mapper.ConfigMapper;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/system/configs")
public class ConfigController {

    private final ConfigMapper configs;

    public ConfigController(ConfigMapper configs) {
        this.configs = configs;
    }

    @GetMapping
    @RequiresPerm("system:config:list")
    public R<List<Config>> all() {
        return R.ok(configs.all());
    }

    @PostMapping
    @RequiresPerm("system:config:add")
    @OperLog(module = "参数配置", action = "新增参数")
    public R<Void> add(@RequestBody Config c) {
        if (c.getBuiltin() == null) c.setBuiltin(0);
        configs.insert(c);
        return R.ok();
    }

    @PutMapping("/{id}")
    @RequiresPerm("system:config:edit")
    @OperLog(module = "参数配置", action = "修改参数")
    public R<Void> update(@PathVariable Long id, @RequestBody Config c) {
        c.setId(id);
        configs.update(c);
        return R.ok();
    }

    @PutMapping("/batch")
    @RequiresPerm("system:config:edit")
    @OperLog(module = "参数配置", action = "批量保存参数")
    public R<Void> batch(@RequestBody List<Map<String, Object>> items) {
        for (Map<String, Object> it : items) {
            Object id = it.get("id");
            Object v = it.get("value");
            if (id != null) configs.updateValue(Long.valueOf(String.valueOf(id)), v == null ? null : String.valueOf(v));
        }
        return R.ok();
    }

    @DeleteMapping("/{id}")
    @RequiresPerm("system:config:remove")
    @OperLog(module = "参数配置", action = "删除参数")
    public R<Void> delete(@PathVariable Long id) {
        Config c = configs.byId(id);
        if (c != null && c.getBuiltin() != null && c.getBuiltin() == 1) {
            return R.fail(400, "内置参数不允许删除");
        }
        configs.delete(id);
        return R.ok();
    }
}
