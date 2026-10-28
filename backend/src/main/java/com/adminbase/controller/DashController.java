package com.adminbase.controller;

import com.adminbase.auth.TokenService;
import com.adminbase.common.R;
import com.adminbase.common.Ranges;
import com.adminbase.mapper.DashMapper;
import com.adminbase.mapper.LoginLogMapper;
import com.adminbase.mapper.OperLogMapper;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Calendar;

@RestController
@RequestMapping("/api/dashboard")
public class DashController {

    private final LoginLogMapper loginLogs;
    private final OperLogMapper operLogs;
    private final DashMapper dash;
    private final TokenService tokens;

    public DashController(LoginLogMapper loginLogs, OperLogMapper operLogs, DashMapper dash, TokenService tokens) {
        this.loginLogs = loginLogs;
        this.operLogs = operLogs;
        this.dash = dash;
        this.tokens = tokens;
    }

    @GetMapping("/pulse")
    public R<Map<String, Object>> pulse(@RequestParam(defaultValue = "24h") String range) {
        boolean hourly = "24h".equals(range);
        String from = Ranges.from(range);
        String prevFrom = Ranges.prevFrom(range);
        List<Map<String, Object>> raw, prevRaw;
        if (hourly) {
            raw = loginLogs.pulseHour(from);
            prevRaw = loginLogs.pulseHour(prevFrom);
        } else {
            String to = Ranges.now();
            raw = loginLogs.pulseDay(from, to);
            prevRaw = loginLogs.pulseDay(prevFrom, from);
        }
        Map<String, Long> idx = new LinkedHashMap<String, Long>();
        fill(idx, hourly, from);
        long total = 0;
        for (Map<String, Object> p : raw) {
            long c = ((Number) p.get("c")).longValue();
            String t = String.valueOf(p.get("t"));
            Long old = idx.get(t);
            idx.put(t, (old == null ? 0 : old) + c);
            total += c;
        }
        long prevTotal = 0;
        for (Map<String, Object> p : prevRaw) prevTotal += ((Number) p.get("c")).longValue();

        List<Map<String, Object>> points = new ArrayList<Map<String, Object>>();
        long peak = 0, sum = 0;
        for (Map.Entry<String, Long> e : idx.entrySet()) {
            long c = e.getValue();
            peak = Math.max(peak, c);
            sum += c;
            Map<String, Object> pt = new HashMap<String, Object>();
            pt.put("t", e.getKey());
            pt.put("c", c);
            points.add(pt);
        }
        Map<String, Object> resp = new HashMap<String, Object>();
        resp.put("total", total);
        resp.put("peak", peak);
        resp.put("avg", points.isEmpty() ? 0 : Math.round(sum * 10.0 / points.size()) / 10.0);
        double delta = prevTotal == 0 ? (total > 0 ? 100 : 0) : Math.round((total - prevTotal) * 1000.0 / prevTotal) / 10.0;
        resp.put("delta", delta);
        resp.put("points", points);
        return R.ok(resp);
    }

    private void fill(Map<String, Long> idx, boolean hourly, String from) {
        SimpleDateFormat dayFmt = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat hourFmt = new SimpleDateFormat("yyyy-MM-dd HH:00");
        Calendar c = Calendar.getInstance();
        if (hourly) {
            String fromHour = from.substring(0, 13) + ":00:00";
            try { c.setTime(new SimpleDateFormat("yyyy-MM-dd HH:00:00").parse(fromHour)); } catch (Exception ignore) {}
            for (int i = 0; i < 24; i++) idx.put(hourFmt.format(c.getTime()), 0L);
        } else {
            int days = idxDays(from);
            c.add(Calendar.DAY_OF_MONTH, -(days - 1));
            for (int i = 0; i < days; i++) {
                idx.put(dayFmt.format(c.getTime()), 0L);
                c.add(Calendar.DAY_OF_MONTH, 1);
            }
        }
    }

    private int idxDays(String from) {
        try {
            long ms = System.currentTimeMillis() - new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(from).getTime();
            return Math.max(1, (int) Math.ceil(ms / 86400000.0));
        } catch (Exception e) {
            return 7;
        }
    }

    @GetMapping("/summary")
    public R<Map<String, Object>> summary() {
        Map<String, Object> m = new HashMap<String, Object>();
        m.put("online", tokens.onlineCount());
        Map<String, Object> op24 = operLogs.sumSince(Ranges.from("24h"));
        long c24 = num(op24, "c"), s24 = num(op24, "s");
        m.put("apiCalls24h", c24);
        m.put("successRate", c24 == 0 ? 100 : Math.round(s24 * 1000.0 / c24) / 10.0);
        m.put("operSuccess", s24);
        m.put("operFail", c24 - s24);
        m.put("oper7d", operLogs.byDay(Ranges.from("7d")));
        m.put("scopeDist", dash.scopeDist());
        return R.ok(m);
    }

    private long num(Map<String, Object> map, String k) {
        Object v = map == null ? null : map.get(k);
        return v instanceof Number ? ((Number) v).longValue() : 0L;
    }
}
