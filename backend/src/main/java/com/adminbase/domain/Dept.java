package com.adminbase.domain;

import lombok.Data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
public class Dept {
    private Long id;
    private Long parentId;
    private String ancestors;
    private String name;
    private String leaderName;
    private String phone;
    private String email;
    private Integer sort;
    private String status;
    private Date createTime;

    private List<Dept> children = new ArrayList<Dept>();
    private Long userCount;
    private List<User> members;
}
