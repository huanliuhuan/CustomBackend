package com.adminbase.mapper;

import com.adminbase.common.PageQ;
import com.adminbase.domain.User;
import org.apache.ibatis.annotations.*;

import java.util.List;

public interface UserMapper {

    @Select("<script>select u.*, d.name dept_name," +
            "(select group_concat(p.name) from sys_user_post up join sys_post p on p.id=up.post_id where up.user_id=u.id) post_names," +
            "(select group_concat(r.name) from sys_user_role ur join sys_role r on r.id=ur.role_id where ur.user_id=u.id) role_names " +
            "from sys_user u left join sys_dept d on d.id=u.dept_id where u.del_flag='0'" +
            "<if test='q.kw!=null and q.kw!=\"\"'> and (u.account like concat('%',#{q.kw},'%') or u.nick_name like concat('%',#{q.kw},'%') or u.phone like concat('%',#{q.kw},'%'))</if>" +
            "<if test='q.status!=null'> and u.status=#{q.status}</if>" +
            "<if test='deptId!=null'> and (u.dept_id=#{deptId} or u.dept_id in (select id from sys_dept where find_in_set(#{deptId},ancestors)))</if>" +
            "${scopeSql} order by u.id limit #{q.size} offset #{q.offset}</script>")
    List<User> page(@Param("q") PageQ q, @Param("deptId") Long deptId, @Param("scopeSql") String scopeSql);

    @Select("<script>select count(*) from sys_user u where u.del_flag='0'" +
            "<if test='q.kw!=null and q.kw!=\"\"'> and (u.account like concat('%',#{q.kw},'%') or u.nick_name like concat('%',#{q.kw},'%') or u.phone like concat('%',#{q.kw},'%'))</if>" +
            "<if test='q.status!=null'> and u.status=#{q.status}</if>" +
            "<if test='deptId!=null'> and (u.dept_id=#{deptId} or u.dept_id in (select id from sys_dept where find_in_set(#{deptId},ancestors)))</if>" +
            "${scopeSql}</script>")
    long count(@Param("q") PageQ q, @Param("deptId") Long deptId, @Param("scopeSql") String scopeSql);

    @Select("select * from sys_user where id=#{id} and del_flag='0'")
    User byId(Long id);

    @Select("select * from sys_user where account=#{a} and del_flag='0'")
    User byAccount(String a);

    @Insert("insert into sys_user(dept_id,account,nick_name,email,phone,sex,password,status,remark,create_time) " +
            "values(#{u.deptId},#{u.account},#{u.nickName},#{u.email},#{u.phone},#{u.sex},#{u.password},#{u.status},#{u.remark},now())")
    @Options(useGeneratedKeys = true, keyProperty = "u.id")
    int insert(@Param("u") User u);

    @Update("<script>update sys_user<set>" +
            "<if test='u.deptId!=null'>dept_id=#{u.deptId},</if>" +
            "<if test='u.nickName!=null'>nick_name=#{u.nickName},</if>" +
            "<if test='u.email!=null'>email=#{u.email},</if>" +
            "<if test='u.phone!=null'>phone=#{u.phone},</if>" +
            "<if test='u.sex!=null'>sex=#{u.sex},</if>" +
            "<if test='u.status!=null'>status=#{u.status},</if>" +
            "<if test='u.remark!=null'>remark=#{u.remark},</if>" +
            "</set>where id=#{u.id}</script>")
    int update(@Param("u") User u);

    @Update("<script>update sys_user set del_flag='2' where id in <foreach item='i' collection='ids' open='(' separator=',' close=')'>#{i}</foreach></script>")
    int delete(@Param("ids") List<Long> ids);

    @Update("update sys_user set password=#{pwd} where id=#{id}")
    int resetPwd(@Param("id") Long id, @Param("pwd") String pwd);

    @Update("update sys_user set login_ip=#{ip}, login_time=now() where id=#{id}")
    int updateLogin(@Param("id") Long id, @Param("ip") String ip);

    @Select("select role_id from sys_user_role where user_id=#{uid}")
    List<Long> roleIdsOf(Long uid);

    @Select("select post_id from sys_user_post where user_id=#{uid}")
    List<Long> postIdsOf(Long uid);

    @Delete("delete from sys_user_role where user_id=#{uid}")
    int clearUserRoles(Long uid);

    @Insert("<script>insert into sys_user_role(user_id,role_id) values" +
            "<foreach item='r' collection='rids' separator=','>(#{uid},#{r})</foreach></script>")
    int setUserRoles(@Param("uid") Long uid, @Param("rids") List<Long> rids);

    @Delete("delete from sys_user_post where user_id=#{uid}")
    int clearUserPosts(Long uid);

    @Insert("<script>insert into sys_user_post(user_id,post_id) values" +
            "<foreach item='p' collection='pids' separator=','>(#{uid},#{p})</foreach></script>")
    int setUserPosts(@Param("uid") Long uid, @Param("pids") List<Long> pids);
}
