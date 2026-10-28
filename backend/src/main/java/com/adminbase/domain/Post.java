package com.adminbase.domain;

import lombok.Data;

import java.util.Date;

@Data
public class Post {
    private Long id;
    private String code;
    private String name;
    private Integer sort;
    private String status;
    private String remark;
    private Date createTime;
}
