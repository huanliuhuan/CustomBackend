package com.adminbase.auth;

import lombok.Data;

@Data
public class LoginReq {
    private String account;
    private String password;
    private String code;
    private String key;
    private Boolean remember;
}
