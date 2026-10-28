package com.adminbase.auth;

import com.adminbase.common.*;
import com.adminbase.mapper.RoleMapper;
import com.adminbase.mapper.UserMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    private final TokenService tokens;
    private final UserMapper users;
    private final RoleMapper roles;
    private final ObjectMapper om = new ObjectMapper();

    public AuthInterceptor(TokenService tokens, UserMapper users, RoleMapper roles) {
        this.tokens = tokens;
        this.users = users;
        this.roles = roles;
    }

    @Override
    public boolean preHandle(HttpServletRequest req, HttpServletResponse resp, Object handler) throws Exception {
        if ("OPTIONS".equalsIgnoreCase(req.getMethod())) return true;
        String uri = req.getRequestURI();
        if (uri.equals("/api/auth/captcha") || uri.equals("/api/auth/login")) return true;

        String head = req.getHeader("Authorization");
        if (head == null || !head.startsWith("Bearer ")) return deny(resp, 401, "登录已过期,请重新登录");
        String token = head.substring(7);
        TokenService.Sess s = tokens.get(token);
        if (s == null) return deny(resp, 401, "登录已过期,请重新登录");
        com.adminbase.domain.User u = users.byAccount(s.account);
        if (u == null || "1".equals(u.getStatus())) {
            tokens.revoke(token);
            return deny(resp, 401, "账号不可用");
        }

        UserCtx.Ctx ctx = new UserCtx.Ctx();
        ctx.uid = u.getId();
        ctx.account = u.getAccount();
        ctx.name = u.getNickName();
        ctx.deptId = u.getDeptId();
        List<String> roleCodes = roles.roleCodesOf(u.getId());
        ctx.roles = new HashSet<String>(roleCodes);
        ctx.superRole = ctx.roles.contains("super_admin");
        ctx.scope = ctx.superRole ? 1 : (roles.minScope(u.getId()) == null ? 5 : roles.minScope(u.getId()));
        if (!ctx.superRole) ctx.perms = new HashSet<String>(roles.permsOf(u.getId()));
        UserCtx.set(ctx);

        if (handler instanceof HandlerMethod) {
            RequiresPerm rp = ((HandlerMethod) handler).getMethodAnnotation(RequiresPerm.class);
            if (rp != null && !ctx.superRole && !ctx.perms.contains(rp.value())) {
                return deny(resp, 403, "无权限:" + rp.value());
            }
        }
        tokens.touch(token);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest req, HttpServletResponse resp, Object handler, Exception ex) {
        UserCtx.clear();
    }

    private boolean deny(HttpServletResponse resp, int code, String msg) throws Exception {
        resp.setStatus(code);
        resp.setContentType("application/json;charset=UTF-8");
        resp.getWriter().write(om.writeValueAsString(R.fail(code, msg)));
        return false;
    }
}
