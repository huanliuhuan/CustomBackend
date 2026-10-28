package com.adminbase.common;

import java.util.List;

public class PageResult<T> {
    public long total;
    public List<T> rows;

    public static <T> PageResult<T> of(long total, List<T> rows) {
        PageResult<T> p = new PageResult<T>();
        p.total = total;
        p.rows = rows;
        return p;
    }
}
