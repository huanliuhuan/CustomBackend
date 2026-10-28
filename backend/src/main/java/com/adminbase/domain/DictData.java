package com.adminbase.domain;

import lombok.Data;

import java.util.Date;

@Data
public class DictData {
    private Long id;
    private String type;
    private String label;
    private String value;
    private String tag;
    private Integer sort;
    private Integer isDefault;
    private String status;
    private String remark;
    private Date createTime;
}
