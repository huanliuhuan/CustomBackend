package com.adminbase.mapper;

import com.adminbase.domain.Menu;
import org.apache.ibatis.annotations.*;

import java.util.List;

public interface MenuMapper {

    @Select("select * from sys_menu order by sort,id")
    List<Menu> all();

    @Select("select id from sys_menu")
    List<Long> allIds();

    @Select("select * from sys_menu where type in ('M','C') and status='0' order by sort,id")
    List<Menu> allMc();

    @Select("<script>select distinct m.* from sys_menu m where m.type in ('M','C') and m.status='0' and" +
            " m.id in (select rm.menu_id from sys_role_menu rm join sys_user_role ur on ur.role_id=rm.role_id where ur.user_id=#{uid})" +
            " order by m.sort,m.id</script>")
    List<Menu> ofUser(Long uid);

    @Select("select * from sys_menu where id=#{id}")
    Menu byId(Long id);

    @Insert("insert into sys_menu(parent_id,name,type,path,component,perm,icon,sort,visible,status,remark) " +
            "values(#{m.parentId},#{m.name},#{m.type},#{m.path},#{m.component},#{m.perm},#{m.icon},#{m.sort},#{m.visible},#{m.status},#{m.remark})")
    @Options(useGeneratedKeys = true, keyProperty = "m.id")
    int insert(@Param("m") Menu m);

    @Update("<script>update sys_menu<set>" +
            "<if test='m.parentId!=null'>parent_id=#{m.parentId},</if>" +
            "<if test='m.name!=null'>name=#{m.name},</if>" +
            "<if test='m.type!=null'>type=#{m.type},</if>" +
            "<if test='m.path!=null'>path=#{m.path},</if>" +
            "<if test='m.component!=null'>component=#{m.component},</if>" +
            "<if test='m.perm!=null'>perm=#{m.perm},</if>" +
            "<if test='m.icon!=null'>icon=#{m.icon},</if>" +
            "<if test='m.sort!=null'>sort=#{m.sort},</if>" +
            "<if test='m.visible!=null'>visible=#{m.visible},</if>" +
            "<if test='m.status!=null'>status=#{m.status},</if>" +
            "<if test='m.remark!=null'>remark=#{m.remark},</if>" +
            "</set>where id=#{m.id}</script>")
    int update(@Param("m") Menu m);

    @Delete("delete from sys_menu where id=#{id}")
    int delete(Long id);

    @Select("select count(*) from sys_menu where parent_id=#{pid}")
    long childCount(Long pid);

    @Select("select count(*) from sys_role_menu where menu_id=#{mid}")
    long grantCount(Long mid);
}
