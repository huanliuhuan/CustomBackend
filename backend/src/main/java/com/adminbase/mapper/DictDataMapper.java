package com.adminbase.mapper;

import com.adminbase.domain.DictData;
import org.apache.ibatis.annotations.*;

import java.util.List;

public interface DictDataMapper {

    @Select("select * from sys_dict_data where type=#{type} order by sort,id")
    List<DictData> byType(String type);

    @Select("select * from sys_dict_data where id=#{id}")
    DictData byId(Long id);

    @Insert("insert into sys_dict_data(type,label,value,tag,sort,is_default,status,remark) " +
            "values(#{d.type},#{d.label},#{d.value},#{d.tag},#{d.sort},#{d.isDefault},#{d.status},#{d.remark})")
    @Options(useGeneratedKeys = true, keyProperty = "d.id")
    int insert(@Param("d") DictData d);

    @Update("<script>update sys_dict_data<set>" +
            "<if test='d.label!=null'>label=#{d.label},</if>" +
            "<if test='d.value!=null'>value=#{d.value},</if>" +
            "<if test='d.tag!=null'>tag=#{d.tag},</if>" +
            "<if test='d.sort!=null'>sort=#{d.sort},</if>" +
            "<if test='d.isDefault!=null'>is_default=#{d.isDefault},</if>" +
            "<if test='d.status!=null'>status=#{d.status},</if>" +
            "<if test='d.remark!=null'>remark=#{d.remark},</if>" +
            "</set>where id=#{d.id}</script>")
    int update(@Param("d") DictData d);

    @Update("update sys_dict_data set is_default=0 where type=#{type}")
    int clearDefault(String type);

    @Delete("delete from sys_dict_data where id=#{id}")
    int delete(Long id);

    @Delete("delete from sys_dict_data where type=#{type}")
    int deleteByType(String type);
}
