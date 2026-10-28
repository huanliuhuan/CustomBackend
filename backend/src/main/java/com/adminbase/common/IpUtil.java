package com.adminbase.common;

import javax.servlet.http.HttpServletRequest;

public class IpUtil {

    public static String ip(HttpServletRequest req) {
        String[] hs = {"X-Forwarded-For", "X-Real-IP", "Proxy-Client-IP"};
        for (String h : hs) {
            String v = req.getHeader(h);
            if (v != null && !v.isEmpty() && !"unknown".equalsIgnoreCase(v)) {
                int i = v.indexOf(',');
                return i > 0 ? v.substring(0, i).trim() : v.trim();
            }
        }
        return req.getRemoteAddr();
    }

    public static String zone(String ip) {
        if (ip == null) return "未知";
        if ("127.0.0.1".equals(ip) || "0:0:0:0:0:0:0:1".equals(ip) || "localhost".equals(ip)) return "本机";
        if (ip.startsWith("192.168.") || ip.startsWith("10.") || ip.matches("^172\\.(1[6-9]|2\\d|3[01])\\..*")) return "内网";
        return "外网";
    }
}
