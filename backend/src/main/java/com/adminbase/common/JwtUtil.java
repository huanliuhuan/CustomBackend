package com.adminbase.common;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {

    @Value("${admin.jwt-secret}")
    private String secret;

    @Value("${admin.jwt-hours}")
    private long hours;

    private Key key() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String issue(long uid, String account, long expireHours) {
        return Jwts.builder()
                .setSubject(String.valueOf(uid))
                .claim("acc", account)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expireHours * 3600_000L))
                .signWith(key(), SignatureAlgorithm.HS256)
                .compact();
    }

    public Claims parse(String token) {
        return Jwts.parser().setSigningKey(key()).parseClaimsJws(token).getBody();
    }
}
