package com.adminbase.domain;

import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class DictType {
    private Long id;
    private String name;
    private String type;
    private String status;
    private String remark;
    private Date createTime;

    private Long dataCount;
    private List<DictData> data;
}
