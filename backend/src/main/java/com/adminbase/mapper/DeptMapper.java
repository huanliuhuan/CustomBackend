package com.adminbase.mapper;

import com.adminbase.domain.Dept;
import com.adminbase.domain.User;
import org.apache.ibatis.annotations.*;

import java.util.List;

public interface DeptMapper {

    @Select("select d.*,(select count(*) from sys_user u where u.dept_id=d.id and u.del_flag='0') user_count " +
            "from sys_dept d order by d.parent_id,d.sort,d.id")
    List<Dept> all();

    @Select("select * from sys_dept where id=#{id}")
    Dept byId(Long id);

    @Insert("insert into sys_dept(parent_id,ancestors,name,leader_name,phone,email,sort,status) " +
            "values(#{d.parentId},#{d.ancestors},#{d.name},#{d.leaderName},#{d.phone},#{d.email},#{d.sort},#{d.status})")
    @Options(useGeneratedKeys = true, keyProperty = "d.id")
    int insert(@Param("d") Dept d);

    @Update("<script>update sys_dept<set>" +
            "<if test='d.parentId!=null'>parent_id=#{d.parentId},</if>" +
            "<if test='d.ancestors!=null'>ancestors=#{d.ancestors},</if>" +
            "<if test='d.name!=null'>name=#{d.name},</if>" +
            "<if test='d.leaderName!=null'>leader_name=#{d.leaderName},</if>" +
            "<if test='d.phone!=null'>phone=#{d.phone},</if>" +
            "<if test='d.email!=null'>email=#{d.email},</if>" +
            "<if test='d.sort!=null'>sort=#{d.sort},</if>" +
            "<if test='d.status!=null'>status=#{d.status},</if>" +
            "</set>where id=#{d.id}</script>")
    int update(@Param("d") Dept d);

    @Delete("delete from sys_dept where id=#{id}")
    int delete(Long id);

    @Select("select count(*) from sys_dept where parent_id=#{pid}")
    long childCount(Long pid);

    @Select("select count(*) from sys_user where dept_id=#{id} and del_flag='0'")
    long userCount(Long id);

    @Update("update sys_dept set ancestors=concat(#{newA}, substring(ancestors, char_length(#{oldA})+1)) where find_in_set(#{id}, ancestors)")
    int updateChildAncestors(@Param("id") Long id, @Param("oldA") String oldA, @Param("newA") String newA);

    @Select("select u.id,u.nick_name,u.account,u.sex,(select p.name from sys_user_post up join sys_post p on p.id=up.post_id where up.user_id=u.id limit 1) post_names " +
            "from sys_user u where u.dept_id=#{id} and u.del_flag='0' order by u.id")
    List<User> members(Long id);
}
