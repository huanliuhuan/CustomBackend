package com.adminbase.mapper;

import com.adminbase.common.PageQ;
import com.adminbase.domain.OperLog;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

public interface OperLogMapper {

    @Select("<script>select * from sys_oper_log where 1=1" +
            "<if test='q.kw!=null and q.kw!=\"\"'> and (uri like concat('%',#{q.kw},'%') or oper_account like concat('%',#{q.kw},'%') or module like concat('%',#{q.kw},'%') or action like concat('%',#{q.kw},'%'))</if>" +
            "<if test='q.status!=null'> and result=#{q.status}</if>" +
            "<if test='q.from!=null and q.from!=\"\"'> and oper_time&gt;=#{q.from}</if>" +
            "<if test='q.to!=null and q.to!=\"\"'> and oper_time&lt;=#{q.to}</if>" +
            "order by oper_time desc, id desc limit #{q.size} offset #{q.offset}</script>")
    List<OperLog> page(@Param("q") PageQ q);

    @Select("<script>select count(*) from sys_oper_log where 1=1" +
            "<if test='q.kw!=null and q.kw!=\"\"'> and (uri like concat('%',#{q.kw},'%') or oper_account like concat('%',#{q.kw},'%') or module like concat('%',#{q.kw},'%') or action like concat('%',#{q.kw},'%'))</if>" +
            "<if test='q.status!=null'> and result=#{q.status}</if>" +
            "<if test='q.from!=null and q.from!=\"\"'> and oper_time&gt;=#{q.from}</if>" +
            "<if test='q.to!=null and q.to!=\"\"'> and oper_time&lt;=#{q.to}</if></script>")
    long count(@Param("q") PageQ q);

    @Insert("insert into sys_oper_log(module,action,method,uri,oper_account,oper_name,ip,zone,result,err_msg,cost_ms,params,oper_time) " +
            "values(#{l.module},#{l.action},#{l.method},#{l.uri},#{l.operAccount},#{l.operName},#{l.ip},#{l.zone},#{l.result},#{l.errMsg},#{l.costMs},#{l.params},now())")
    int insert(@Param("l") OperLog l);

    @Select("select * from sys_oper_log where id=#{id}")
    OperLog byId(Long id);

    @Delete("<script>delete from sys_oper_log where id in <foreach item='i' collection='ids' open='(' separator=',' close=')'>#{i}</foreach></script>")
    int delete(@Param("ids") List<Long> ids);

    @Select("select count(*) c, ifnull(sum(case when result=0 then 1 else 0 end),0) s from sys_oper_log where oper_time>=#{from}")
    Map<String, Object> sumSince(@Param("from") String from);

    @Select("select date_format(oper_time,'%m-%d') d, count(*) c from sys_oper_log where oper_time>=#{from} group by d order by d")
    List<Map<String, Object>> byDay(@Param("from") String from);
}
