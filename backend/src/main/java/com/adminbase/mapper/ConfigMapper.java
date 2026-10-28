package com.adminbase.mapper;

import com.adminbase.domain.Config;
import org.apache.ibatis.annotations.*;

import java.util.List;

public interface ConfigMapper {

    @Select("select * from sys_config order by group_name,id")
    List<Config> all();

    @Select("select * from sys_config where id=#{id}")
    Config byId(Long id);

    @Select("select * from sys_config where `key`=#{k}")
    Config byKey(String k);

    @Insert("insert into sys_config(group_name,name,`key`,value,val_type,options,builtin,remark) " +
            "values(#{c.groupName},#{c.name},#{c.key},#{c.value},#{c.valType},#{c.options},0,#{c.remark})")
    @Options(useGeneratedKeys = true, keyProperty = "c.id")
    int insert(@Param("c") Config c);

    @Update("<script>update sys_config<set>" +
            "<if test='c.groupName!=null'>group_name=#{c.groupName},</if>" +
            "<if test='c.name!=null'>name=#{c.name},</if>" +
            "<if test='c.value!=null'>value=#{c.value},</if>" +
            "<if test='c.valType!=null'>val_type=#{c.valType},</if>" +
            "<if test='c.options!=null'>options=#{c.options},</if>" +
            "<if test='c.remark!=null'>remark=#{c.remark},</if>" +
            "</set>update_time=now() where id=#{c.id}</script>")
    int update(@Param("c") Config c);

    @Update("update sys_config set value=#{v}, update_time=now() where id=#{id}")
    int updateValue(@Param("id") Long id, @Param("v") String v);

    @Delete("delete from sys_config where id=#{id}")
    int delete(Long id);
}
