package com.adminbase.domain;

import lombok.Data;

import java.util.Date;

@Data
public class Config {
    private Long id;
    private String groupName;
    private String name;
    private String key;
    private String value;
    private String valType;
    private String options;
    private Integer builtin;
    private String remark;
    private Date updateTime;
}
