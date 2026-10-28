package com.adminbase.controller;

import com.adminbase.auth.CaptchaService;
import com.adminbase.auth.LoginReq;
import com.adminbase.auth.TokenService;
import com.adminbase.common.IpUtil;
import com.adminbase.common.R;
import com.adminbase.common.UserCtx;
import com.adminbase.service.AuthService;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService auth;
    private final CaptchaService captcha;
    private final TokenService tokens;

    public AuthController(AuthService auth, CaptchaService captcha, TokenService tokens) {
        this.auth = auth;
        this.captcha = captcha;
        this.tokens = tokens;
    }

    @GetMapping("/captcha")
    public R<Map<String, Object>> captcha() {
        Map<String, Object> m = captcha.gen();
        m.put("enabled", "true".equals(auth.cfg("sys.account.captchaEnabled")));
        return R.ok(m);
    }

    @PostMapping("/login")
    public R<Map<String, Object>> login(@RequestBody LoginReq req, HttpServletRequest hsr) {
        return R.ok(auth.login(req, IpUtil.ip(hsr)));
    }

    @PostMapping("/logout")
    public R<Void> logout(HttpServletRequest hsr) {
        String t = hsr.getHeader("Authorization");
        if (t != null && t.startsWith("Bearer ")) tokens.revoke(t.substring(7));
        return R.ok();
    }

    @GetMapping("/me")
    public R<Map<String, Object>> me() {
        return R.ok(auth.me(UserCtx.get().uid));
    }
}
