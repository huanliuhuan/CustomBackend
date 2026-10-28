package com.adminbase.auth;

import com.adminbase.common.JwtUtil;
import com.adminbase.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TokenService {

    @Autowired
    private JwtUtil jwt;

    @Value("${admin.jwt-hours}")
    private long jwtHours;

    @Value("${admin.remember-hours}")
    private long rememberHours;

    @Value("${admin.online-window-minutes}")
    private long onlineWin;

    private final Map<String, Sess> store = new ConcurrentHashMap<String, Sess>();

    public static class Sess {
        public String token;
        public long uid;
        public String account;
        public String name;
        public long loginAt;
        public long lastSeen;
        public long expireAt;
    }

    public Sess issue(User u, boolean remember) {
        long hrs = remember ? rememberHours : jwtHours;
        String t = jwt.issue(u.getId(), u.getAccount(), hrs);
        Sess s = new Sess();
        s.token = t;
        s.uid = u.getId();
        s.account = u.getAccount();
        s.name = u.getNickName();
        long now = System.currentTimeMillis();
        s.loginAt = now;
        s.lastSeen = now;
        s.expireAt = now + hrs * 3600_000L;
        store.put(t, s);
        return s;
    }

    public Sess get(String token) {
        Sess s = store.get(token);
        if (s == null) return null;
        if (System.currentTimeMillis() > s.expireAt) {
            store.remove(token);
            return null;
        }
        return s;
    }

    public void touch(String token) {
        Sess s = store.get(token);
        if (s != null) s.lastSeen = System.currentTimeMillis();
    }

    public void revoke(String token) {
        store.remove(token);
    }

    public long onlineCount() {
        long now = System.currentTimeMillis();
        Set<Long> ids = new HashSet<Long>();
        for (Sess s : store.values()) {
            if (now - s.lastSeen <= onlineWin * 60_000L && now < s.expireAt) ids.add(s.uid);
        }
        return ids.size();
    }
}
