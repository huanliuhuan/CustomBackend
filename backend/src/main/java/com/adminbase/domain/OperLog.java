package com.adminbase.domain;

import lombok.Data;

import java.util.Date;

@Data
public class OperLog {
    private Long id;
    private String module;
    private String action;
    private String method;
    private String uri;
    private String operAccount;
    private String operName;
    private String ip;
    private String zone;
    private Integer result;
    private String errMsg;
    private Long costMs;
    private String params;
    private Date operTime;
}
