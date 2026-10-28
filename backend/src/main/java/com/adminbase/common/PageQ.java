package com.adminbase.common;

import lombok.Data;

@Data
public class PageQ {
    private long page = 1;
    private long size = 10;
    private String kw;
    private Integer status;
    private String from;
    private String to;

    public long getOffset() { return (page - 1) * size; }
}
