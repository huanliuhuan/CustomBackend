package com.adminbase.controller;

import com.adminbase.common.*;
import com.adminbase.domain.DictData;
import com.adminbase.domain.DictType;
import com.adminbase.mapper.DictDataMapper;
import com.adminbase.mapper.DictTypeMapper;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/system/dicts")
public class DictController {

    private final DictTypeMapper types;
    private final DictDataMapper datas;

    public DictController(DictTypeMapper types, DictDataMapper datas) {
        this.types = types;
        this.datas = datas;
    }

    @GetMapping
    @RequiresPerm("system:dict:list")
    public R<List<DictType>> all() {
        List<DictType> list = types.all();
        for (DictType t : list) t.setData(datas.byType(t.getType()));
        return R.ok(list);
    }

    @PostMapping("/type")
    @RequiresPerm("system:dict:add")
    @OperLog(module = "字典管理", action = "新增字典类型")
    public R<Void> addType(@RequestBody DictType t) {
        if (types.byType(t.getType()) != null) throw new BizException("字典类型标识已存在");
        if (t.getStatus() == null) t.setStatus("0");
        types.insert(t);
        return R.ok();
    }

    @PutMapping("/type/{id}")
    @RequiresPerm("system:dict:edit")
    @OperLog(module = "字典管理", action = "修改字典类型")
    public R<Void> updateType(@PathVariable Long id, @RequestBody DictType t) {
        t.setId(id);
        types.update(t);
        return R.ok();
    }

    @DeleteMapping("/type/{id}")
    @RequiresPerm("system:dict:remove")
    @OperLog(module = "字典管理", action = "删除字典类型")
    public R<Void> deleteType(@PathVariable Long id) {
        DictType t = types.byId(id);
        types.delete(id);
        if (t != null) datas.deleteByType(t.getType());
        return R.ok();
    }

    @PostMapping("/data")
    @RequiresPerm("system:dict:add")
    @OperLog(module = "字典管理", action = "新增字典数据")
    public R<Void> addData(@RequestBody DictData d) {
        if (d.getIsDefault() != null && d.getIsDefault() == 1) datas.clearDefault(d.getType());
        if (d.getStatus() == null) d.setStatus("0");
        datas.insert(d);
        return R.ok();
    }

    @PutMapping("/data/{id}")
    @RequiresPerm("system:dict:edit")
    @OperLog(module = "字典管理", action = "修改字典数据")
    public R<Void> updateData(@PathVariable Long id, @RequestBody DictData d) {
        DictData old = datas.byId(id);
        if (old == null) throw new BizException("数据不存在");
        d.setId(id);
        if (d.getIsDefault() != null && d.getIsDefault() == 1) datas.clearDefault(old.getType());
        datas.update(d);
        return R.ok();
    }

    @DeleteMapping("/data/{id}")
    @RequiresPerm("system:dict:remove")
    @OperLog(module = "字典管理", action = "删除字典数据")
    public R<Void> deleteData(@PathVariable Long id) {
        datas.delete(id);
        return R.ok();
    }

    @GetMapping("/map")
    public R<Map<String, List<DictData>>> map() {
        Map<String, List<DictData>> m = new HashMap<String, List<DictData>>();
        for (DictType t : types.all()) m.put(t.getType(), datas.byType(t.getType()));
        return R.ok(m);
    }
}
