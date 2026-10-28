package com.adminbase.aspect;

import com.adminbase.common.OperLog;
import com.adminbase.common.UserCtx;
import com.adminbase.mapper.OperLogMapper;
import com.adminbase.common.IpUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Aspect
@Component
public class OperLogAspect {

    private final OperLogMapper logs;
    private final ObjectMapper om;

    public OperLogAspect(OperLogMapper logs, ObjectMapper om) {
        this.logs = logs;
        this.om = om;
    }

    @Around("@annotation(op)")
    public Object around(ProceedingJoinPoint jp, OperLog op) throws Throwable {
        long t0 = System.currentTimeMillis();
        Throwable err = null;
        try {
            return jp.proceed();
        } catch (Throwable e) {
            err = e;
            throw e;
        } finally {
            save(op, jp, System.currentTimeMillis() - t0, err);
        }
    }

    private void save(OperLog op, ProceedingJoinPoint jp, long cost, Throwable err) {
        try {
            ServletRequestAttributes ra = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            com.adminbase.domain.OperLog l = new com.adminbase.domain.OperLog();
            l.setModule(op.module());
            l.setAction(op.action());
            l.setCostMs(cost);
            l.setResult(err == null ? 0 : 1);
            l.setErrMsg(err == null ? null : String.valueOf(err.getMessage()));
            if (ra != null) {
                HttpServletRequest req = ra.getRequest();
                l.setMethod(req.getMethod());
                l.setUri(req.getRequestURI());
                l.setIp(IpUtil.ip(req));
                l.setZone(IpUtil.zone(l.getIp()));
            }
            UserCtx.Ctx c = UserCtx.get();
            l.setOperAccount(c == null ? "-" : c.account);
            l.setOperName(c == null ? "-" : c.name);
            l.setParams(scrub(jp.getArgs()));
            logs.insert(l);
        } catch (Exception ignore) {
        }
    }

    private String scrub(Object[] args) {
        StringBuilder sb = new StringBuilder("[");
        try {
            boolean first = true;
            for (Object a : args) {
                if (a instanceof HttpServletRequest || a instanceof HttpServletResponse
                        || a instanceof MultipartFile || a == null) continue;
                if (!first) sb.append(",");
                first = false;
                String json = om.writeValueAsString(a);
                sb.append(json.replaceAll("(\"(?:password|pwd|oldPassword|newPassword|confirmPassword|code)\"\\s*:\\s*\")[^\"]*", "$1***"));
            }
        } catch (Exception e) {
            sb.append("{}");
        }
        sb.append("]");
        String s = sb.toString();
        return s.length() > 4000 ? s.substring(0, 4000) : s;
    }
}
