package com.adminbase.common;

import java.util.HashSet;
import java.util.Set;

public class UserCtx {
    private static final ThreadLocal<Ctx> TL = new ThreadLocal<Ctx>();

    public static class Ctx {
        public long uid;
        public String account;
        public String name;
        public Long deptId;
        public int scope;
        public boolean superRole;
        public Set<String> perms = new HashSet<String>();
        public Set<String> roles = new HashSet<String>();
    }

    public static void set(Ctx c) { TL.set(c); }

    public static Ctx get() { return TL.get(); }

    public static void clear() { TL.remove(); }
}
