package com.adminbase.domain;

import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class Role {
    private Long id;
    private String name;
    private String code;
    private Integer sort;
    private Integer dataScope;
    private String status;
    private Integer builtin;
    private String remark;
    private Date createTime;

    private List<Long> menuIds;
    private Long userCount;
    private Long pageCount;
    private Long btnCount;
}
