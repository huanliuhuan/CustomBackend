package com.adminbase.common;

public class R<T> {
    public int code;
    public String msg;
    public T data;

    public static <T> R<T> ok() { return ok(null); }

    public static <T> R<T> ok(T data) {
        R<T> r = new R<T>();
        r.code = 200;
        r.msg = "success";
        r.data = data;
        return r;
    }

    public static <T> R<T> fail(String msg) { return fail(500, msg); }

    public static <T> R<T> fail(int code, String msg) {
        R<T> r = new R<T>();
        r.code = code;
        r.msg = msg;
        return r;
    }
}
