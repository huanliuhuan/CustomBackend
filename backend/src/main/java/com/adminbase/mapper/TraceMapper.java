package com.adminbase.mapper;

import com.adminbase.domain.Menu;
import com.adminbase.domain.Role;
import com.adminbase.domain.User;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

public interface TraceMapper {

    @Select("select (select count(*) from sys_user where del_flag='0') users," +
            "(select count(*) from sys_role) roles," +
            "(select count(*) from sys_menu) menus," +
            "(select count(*) from sys_role r where not exists(select 1 from sys_user_role ur where ur.role_id=r.id)) idleRoles," +
            "(select count(*) from sys_menu m where m.type!='F' and not exists(select 1 from sys_role_menu rm where rm.menu_id=m.id)) orphanMenus," +
            "(select count(*) from sys_user u where u.del_flag='0' and not exists(select 1 from sys_user_role ur where ur.user_id=u.id)) noRoleUsers")
    Map<String, Object> overview();

    @Select("select r.id,r.name,r.code,r.status,r.builtin,r.data_scope dataScope,r.remark," +
            "(select case when r.builtin=1 then (select count(*) from sys_menu m2 where m2.type='C') else (select count(*) from sys_role_menu rm join sys_menu m on m.id=rm.menu_id and m.type='C' where rm.role_id=r.id) end) pageCount," +
            "(select case when r.builtin=1 then (select count(*) from sys_menu m2 where m2.type='F') else (select count(*) from sys_role_menu rm join sys_menu m on m.id=rm.menu_id and m.type='F' where rm.role_id=r.id) end) btnCount," +
            "(select count(*) from sys_user_role ur where ur.role_id=r.id) userCount " +
            "from sys_role r order by r.sort,r.id")
    List<Role> roleStats();

    @Select("<script>select m.id, m.name, m.path, m.perm," +
            " (select count(*) from sys_menu f where f.parent_id=m.id and f.type='F'" +
            " <if test='!builtin'> and exists(select 1 from sys_role_menu r2 where r2.role_id=#{rid} and r2.menu_id=f.id)</if>" +
            " ) btnCount" +
            " from sys_menu m where m.type='C'" +
            " <if test='!builtin'> and exists(select 1 from sys_role_menu r1 where r1.role_id=#{rid} and r1.menu_id=m.id)</if>" +
            " order by m.sort, m.id</script>")
    java.util.List<java.util.Map<String, Object>> rolePages(@Param("rid") Long rid, @Param("builtin") boolean builtin);

    @Select("select u.nick_name uname, u.account, d.name dname from sys_user_role ur " +
            "join sys_user u on u.id=ur.user_id and u.del_flag='0' left join sys_dept d on d.id=u.dept_id " +
            "where ur.role_id=#{rid} order by u.id")
    java.util.List<java.util.Map<String, Object>> roleUsers(Long rid);

    @Select("select ur.user_id uid, ur.role_id rid from sys_user_role ur")
    List<Map<String, Object>> userRolePairs();

    @Select("select u.id,u.nick_name nickName,u.account,d.name deptName from sys_user u left join sys_dept d on d.id=u.dept_id where u.del_flag='0'")
    List<User> userBrief();

    @Select("select id,parent_id pid,name,type,perm from sys_menu")
    List<Menu> menuBrief();

    @Select("select m.name menuName, m.perm, r.id rid, r.name rname, r.code rcode, u.id uid, u.nick_name uname, u.account, d.name dname " +
            "from sys_menu m join sys_role_menu rm on rm.menu_id=m.id join sys_role r on r.id=rm.role_id and r.status='0' " +
            "join sys_user_role ur on ur.role_id=r.id join sys_user u on u.id=ur.user_id and u.del_flag='0' " +
            "left join sys_dept d on d.id=u.dept_id where m.perm=#{perm}")
    List<Map<String, Object>> whoCan(String perm);

    @Select("select u.id,u.nick_name nickName,u.account,d.name deptName from sys_user u " +
            "left join sys_dept d on d.id=u.dept_id where u.del_flag='0' " +
            "and not exists(select 1 from sys_user_role ur where ur.user_id=u.id)")
    List<User> unassignedUsers();

    @Select("select r.id,r.name,r.code from sys_role r where not exists(select 1 from sys_user_role ur where ur.role_id=r.id)")
    List<Role> idleRoles();

    @Select("select m.id,m.name,m.type,m.perm from sys_menu m where m.type!='F' and not exists(select 1 from sys_role_menu rm where rm.menu_id=m.id)")
    List<Menu> orphanMenus();
}
