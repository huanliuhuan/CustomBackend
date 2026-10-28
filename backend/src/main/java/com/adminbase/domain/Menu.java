package com.adminbase.domain;

import lombok.Data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
public class Menu {
    private Long id;
    private Long parentId;
    private String name;
    private String type;
    private String path;
    private String component;
    private String perm;
    private String icon;
    private Integer sort;
    private String visible;
    private String status;
    private String remark;
    private Date createTime;

    private List<Menu> children = new ArrayList<Menu>();
    private Long roleCount;
}
