package com.adminbase.mapper;

import com.adminbase.common.PageQ;
import com.adminbase.domain.Post;
import org.apache.ibatis.annotations.*;

import java.util.List;

public interface PostMapper {

    @Select("<script>select * from sys_post where 1=1" +
            "<if test='q.kw!=null and q.kw!=\"\"'> and (name like concat('%',#{q.kw},'%') or code like concat('%',#{q.kw},'%'))</if>" +
            "<if test='q.status!=null'> and status=#{q.status}</if>" +
            "order by sort,id limit #{q.size} offset #{q.offset}</script>")
    List<Post> page(@Param("q") PageQ q);

    @Select("<script>select count(*) from sys_post where 1=1" +
            "<if test='q.kw!=null and q.kw!=\"\"'> and (name like concat('%',#{q.kw},'%') or code like concat('%',#{q.kw},'%'))</if>" +
            "<if test='q.status!=null'> and status=#{q.status}</if></script>")
    long count(@Param("q") PageQ q);

    @Select("select * from sys_post order by sort,id")
    List<Post> all();

    @Select("select * from sys_post where id=#{id}")
    Post byId(Long id);

    @Insert("insert into sys_post(code,name,sort,status,remark) values(#{p.code},#{p.name},#{p.sort},#{p.status},#{p.remark})")
    @Options(useGeneratedKeys = true, keyProperty = "p.id")
    int insert(@Param("p") Post p);

    @Update("<script>update sys_post<set>" +
            "<if test='p.code!=null'>code=#{p.code},</if>" +
            "<if test='p.name!=null'>name=#{p.name},</if>" +
            "<if test='p.sort!=null'>sort=#{p.sort},</if>" +
            "<if test='p.status!=null'>status=#{p.status},</if>" +
            "<if test='p.remark!=null'>remark=#{p.remark},</if>" +
            "</set>where id=#{p.id}</script>")
    int update(@Param("p") Post p);

    @Delete("delete from sys_post where id=#{id}")
    int delete(Long id);

    @Select("select count(*) from sys_user_post where post_id=#{id}")
    long usedCount(Long id);
}
