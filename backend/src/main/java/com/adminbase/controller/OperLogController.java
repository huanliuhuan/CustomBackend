package com.adminbase.controller;

import com.adminbase.common.*;
import com.adminbase.domain.OperLog;
import com.adminbase.mapper.OperLogMapper;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/monitor/oplogs")
public class OperLogController {

    private final OperLogMapper logs;

    public OperLogController(OperLogMapper logs) {
        this.logs = logs;
    }

    @GetMapping
    @RequiresPerm("monitor:operlog:list")
    public R<PageResult<OperLog>> list(PageQ q, String range) {
        if (range != null && !range.isEmpty()) {
            q.setFrom(Ranges.from(range));
            q.setTo(Ranges.now());
        }
        return R.ok(PageResult.of(logs.count(q), logs.page(q)));
    }

    @GetMapping("/{id}")
    @RequiresPerm("monitor:operlog:list")
    public R<OperLog> detail(@PathVariable Long id) {
        return R.ok(logs.byId(id));
    }

    @DeleteMapping
    @RequiresPerm("monitor:operlog:remove")
    @com.adminbase.common.OperLog(module = "操作日志", action = "删除日志")
    public R<Void> delete(@RequestBody List<Long> ids) {
        logs.delete(ids);
        return R.ok();
    }

    @GetMapping("/trend")
    public R<List<Map<String, Object>>> trend(String range) {
        return R.ok(logs.byDay(Ranges.from(range == null ? "7d" : range)));
    }
}
