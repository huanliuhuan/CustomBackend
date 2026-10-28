package com.adminbase.service;

import com.adminbase.auth.CaptchaService;
import com.adminbase.auth.LoginReq;
import com.adminbase.auth.TokenService;
import com.adminbase.common.BizException;
import com.adminbase.common.IpUtil;
import com.adminbase.domain.LoginLog;
import com.adminbase.domain.Menu;
import com.adminbase.domain.User;
import com.adminbase.mapper.ConfigMapper;
import com.adminbase.mapper.LoginLogMapper;
import com.adminbase.mapper.MenuMapper;
import com.adminbase.mapper.RoleMapper;
import com.adminbase.mapper.UserMapper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AuthService {

    private final UserMapper users;
    private final RoleMapper roles;
    private final MenuMapper menus;
    private final ConfigMapper configs;
    private final LoginLogMapper loginLogs;
    private final CaptchaService captcha;
    private final TokenService tokens;
    private final BCryptPasswordEncoder encoder;

    public AuthService(UserMapper users, RoleMapper roles, MenuMapper menus, ConfigMapper configs,
                       LoginLogMapper loginLogs, CaptchaService captcha, TokenService tokens,
                       BCryptPasswordEncoder encoder) {
        this.users = users;
        this.roles = roles;
        this.menus = menus;
        this.configs = configs;
        this.loginLogs = loginLogs;
        this.captcha = captcha;
        this.tokens = tokens;
        this.encoder = encoder;
    }

    public String cfg(String key) {
        com.adminbase.domain.Config c = configs.byKey(key);
        return c == null ? null : c.getValue();
    }

    public Map<String, Object> login(LoginReq req, String ip) {
        boolean capEnabled = "true".equals(cfg("sys.account.captchaEnabled"));
        if (capEnabled && !captcha.verify(req.getKey(), req.getCode())) {
            logFail(req.getAccount(), ip, "验证码错误");
            throw new BizException("验证码错误");
        }
        User u = users.byAccount(req.getAccount());
        if (u == null || !encoder.matches(req.getPassword(), u.getPassword())) {
            logFail(req.getAccount(), ip, "账号或密码错误");
            throw new BizException("账号或密码错误");
        }
        if ("1".equals(u.getStatus())) {
            logFail(req.getAccount(), ip, "账号已停用");
            throw new BizException("账号已停用,请联系管理员");
        }
        boolean remember = Boolean.TRUE.equals(req.getRemember());
        String token = tokens.issue(u, remember).token;
        users.updateLogin(u.getId(), ip);
        LoginLog l = new LoginLog();
        l.setAccount(u.getAccount());
        l.setIp(ip);
        l.setZone(IpUtil.zone(ip));
        l.setStatus(0);
        l.setMsg("登录成功");
        loginLogs.insert(l);
        return buildLoginResp(u, token);
    }

    public Map<String, Object> me(long uid) {
        User u = users.byId(uid);
        if (u == null) throw new BizException("账号不存在");
        return buildLoginResp(u, null);
    }

    private Map<String, Object> buildLoginResp(User u, String token) {
        List<String> roleCodes = roles.roleCodesOf(u.getId());
        boolean sup = roleCodes.contains("super_admin");
        List<String> perms = sup ? null : roles.permsOf(u.getId());
        List<Menu> ms = sup ? menus.allMc() : menus.ofUser(u.getId());
        Map<String, Object> user = new HashMap<String, Object>();
        user.put("id", u.getId());
        user.put("account", u.getAccount());
        user.put("nickName", u.getNickName());
        user.put("deptId", u.getDeptId());
        user.put("avatar", null);
        Map<String, Object> resp = new HashMap<String, Object>();
        if (token != null) resp.put("token", token);
        resp.put("user", user);
        resp.put("roles", roleCodes);
        resp.put("perms", perms);
        resp.put("menus", MenuService.tree(ms));
        return resp;
    }

    private void logFail(String account, String ip, String msg) {
        LoginLog l = new LoginLog();
        l.setAccount(account == null ? "-" : account);
        l.setIp(ip);
        l.setZone(IpUtil.zone(ip));
        l.setStatus(1);
        l.setMsg(msg);
        loginLogs.insert(l);
    }
}
