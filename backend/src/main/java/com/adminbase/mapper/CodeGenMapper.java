package com.adminbase.mapper;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

public interface CodeGenMapper {

    @Select("<script>select table_name name, engine engine, table_comment cmt, ifnull(table_rows,0) rowCnt, create_time ct " +
            "from information_schema.tables where table_schema=database() and table_type='BASE TABLE'" +
            "<if test='kw!=null and kw!=\"\"'> and table_name like concat('%',#{kw},'%')</if>" +
            " order by table_name</script>")
    List<Map<String, Object>> tables(@Param("kw") String kw);

    @Select("select column_name name, column_type ctype, is_nullable nullable, column_key ckey, column_comment cmt " +
            "from information_schema.columns where table_schema=database() and table_name=#{t} order by ordinal_position")
    List<Map<String, Object>> columns(@Param("t") String t);
}
