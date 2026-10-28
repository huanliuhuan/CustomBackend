package com.adminbase.domain;

import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class User {
    private Long id;
    private Long deptId;
    private String account;
    private String nickName;
    private String email;
    private String phone;
    private String sex;
    private String password;
    private String status;
    private String delFlag;
    private String loginIp;
    private Date loginTime;
    private String remark;
    private Date createTime;

    private String deptName;
    private String postNames;
    private String roleNames;
    private String roleTags;
    private List<Long> roleIds;
    private List<Long> postIds;
}
