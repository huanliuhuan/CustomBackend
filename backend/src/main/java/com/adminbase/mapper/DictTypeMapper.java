package com.adminbase.mapper;

import com.adminbase.domain.DictData;
import com.adminbase.domain.DictType;
import org.apache.ibatis.annotations.*;

import java.util.List;

public interface DictTypeMapper {

    @Select("select t.*,(select count(*) from sys_dict_data x where x.type=t.type) data_count from sys_dict_type t order by t.id")
    List<DictType> all();

    @Select("select * from sys_dict_type where id=#{id}")
    DictType byId(Long id);

    @Select("select * from sys_dict_type where type=#{type}")
    DictType byType(String type);

    @Insert("insert into sys_dict_type(name,type,status,remark) values(#{t.name},#{t.type},#{t.status},#{t.remark})")
    @Options(useGeneratedKeys = true, keyProperty = "t.id")
    int insert(@Param("t") DictType t);

    @Update("<script>update sys_dict_type<set>" +
            "<if test='t.name!=null'>name=#{t.name},</if>" +
            "<if test='t.type!=null'>type=#{t.type},</if>" +
            "<if test='t.status!=null'>status=#{t.status},</if>" +
            "<if test='t.remark!=null'>remark=#{t.remark},</if>" +
            "</set>where id=#{t.id}</script>")
    int update(@Param("t") DictType t);

    @Delete("delete from sys_dict_type where id=#{id}")
    int delete(Long id);
}
