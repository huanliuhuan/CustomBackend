package com.adminbase.controller;

import com.adminbase.common.*;
import com.adminbase.domain.LoginLog;
import com.adminbase.mapper.LoginLogMapper;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/monitor/loginlogs")
public class LoginLogController {

    @Resource
    private LoginLogMapper logs;

    @GetMapping
    @RequiresPerm("monitor:loginlog:list")
    public R<PageResult<LoginLog>> list(PageQ q, String range) {
        if (range != null && !range.isEmpty()) {
            q.setFrom(Ranges.from(range));
            q.setTo(Ranges.now());
        }
        return R.ok(PageResult.of(logs.count(q), logs.page(q)));
    }

    @GetMapping("/analytics")
    public R<Map<String, Object>> analytics(String range) {
        String from = Ranges.from(range == null ? "7d" : range);
        String to = Ranges.now();
        Map<String, Object> m = new HashMap<String, Object>();
        m.put("stats", logs.stats(from, to));
        m.put("heat", logs.heatmap(from));
        m.put("trend", logs.pulseDay(from, to));
        return R.ok(m);
    }

    @DeleteMapping
    @RequiresPerm("monitor:loginlog:remove")
    @OperLog(module = "登录日志", action = "删除日志")
    public R<Void> delete(@RequestBody List<Long> ids) {
        logs.delete(ids);
        return R.ok();
    }
}
