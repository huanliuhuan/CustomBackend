package com.adminbase.mapper;

import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

public interface DashMapper {

    @Select("select ds, count(*) c from (select ur.user_id, min(r.data_scope) ds from sys_user_role ur join sys_role r on r.id=ur.role_id group by ur.user_id) t group by ds")
    List<Map<String, Object>> scopeDist();
}
