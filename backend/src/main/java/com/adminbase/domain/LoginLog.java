package com.adminbase.domain;

import lombok.Data;

import java.util.Date;

@Data
public class LoginLog {
    private Long id;
    private String account;
    private String ip;
    private String zone;
    private Integer status;
    private String msg;
    private Date loginTime;
}
