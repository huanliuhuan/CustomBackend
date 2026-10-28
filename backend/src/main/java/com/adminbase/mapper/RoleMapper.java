package com.adminbase.mapper;

import com.adminbase.common.PageQ;
import com.adminbase.domain.Role;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

public interface RoleMapper {

    @Select("<script>select r.*,(select count(*) from sys_user_role ur where ur.role_id=r.id) user_count from sys_role r where 1=1" +
            "<if test='q.kw!=null and q.kw!=\"\"'> and (r.name like concat('%',#{q.kw},'%') or r.code like concat('%',#{q.kw},'%'))</if>" +
            "<if test='q.status!=null'> and r.status=#{q.status}</if>" +
            "order by r.sort,r.id limit #{q.size} offset #{q.offset}</script>")
    List<Role> page(@Param("q") PageQ q);

    @Select("<script>select count(*) from sys_role r where 1=1" +
            "<if test='q.kw!=null and q.kw!=\"\"'> and (r.name like concat('%',#{q.kw},'%') or r.code like concat('%',#{q.kw},'%'))</if>" +
            "<if test='q.status!=null'> and r.status=#{q.status}</if></script>")
    long count(@Param("q") PageQ q);

    @Select("select r.*,(select count(*) from sys_user_role ur where ur.role_id=r.id) user_count from sys_role r order by r.sort,r.id")
    List<Role> all();

    @Select("select * from sys_role where id=#{id}")
    Role byId(Long id);

    @Insert("insert into sys_role(name,code,sort,data_scope,status,builtin,remark) values(#{r.name},#{r.code},#{r.sort},#{r.dataScope},#{r.status},0,#{r.remark})")
    @Options(useGeneratedKeys = true, keyProperty = "r.id")
    int insert(@Param("r") Role r);

    @Update("<script>update sys_role<set>" +
            "<if test='r.name!=null'>name=#{r.name},</if>" +
            "<if test='r.code!=null'>code=#{r.code},</if>" +
            "<if test='r.sort!=null'>sort=#{r.sort},</if>" +
            "<if test='r.dataScope!=null'>data_scope=#{r.dataScope},</if>" +
            "<if test='r.status!=null'>status=#{r.status},</if>" +
            "<if test='r.remark!=null'>remark=#{r.remark},</if>" +
            "</set>where id=#{r.id}</script>")
    int update(@Param("r") Role r);

    @Delete("delete from sys_role where id=#{id}")
    int delete(Long id);

    @Select("select count(*) from sys_user_role where role_id=#{rid}")
    long userCount(Long rid);

    @Select("select menu_id from sys_role_menu where role_id=#{rid}")
    List<Long> menuIdsOf(Long rid);

    @Delete("delete from sys_role_menu where role_id=#{rid}")
    int clearRoleMenus(Long rid);

    @Insert("<script>insert into sys_role_menu(role_id,menu_id) values" +
            "<foreach item='m' collection='mids' separator=','>(#{rid},#{m})</foreach></script>")
    int setRoleMenus(@Param("rid") Long rid, @Param("mids") List<Long> mids);

    @Select("select role_id rid, menu_id mid from sys_role_menu")
    List<Map<String, Object>> roleMenuPairs();

    @Select("select r.code from sys_role r join sys_user_role ur on ur.role_id=r.id where ur.user_id=#{uid} and r.status='0'")
    List<String> roleCodesOf(Long uid);

    @Select("select ifnull(min(r.data_scope),5) from sys_role r join sys_user_role ur on ur.role_id=r.id where ur.user_id=#{uid}")
    Integer minScope(Long uid);

    @Select("select distinct m.perm from sys_menu m join sys_role_menu rm on rm.menu_id=m.id join sys_user_role ur on ur.role_id=rm.role_id " +
            "where ur.user_id=#{uid} and m.perm is not null and m.perm!=''")
    List<String> permsOf(Long uid);
}
