package com.adminbase.mapper;

import com.adminbase.common.PageQ;
import com.adminbase.domain.LoginLog;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

public interface LoginLogMapper {

    @Select("<script>select * from sys_login_log where 1=1" +
            "<if test='q.kw!=null and q.kw!=\"\"'> and (account like concat('%',#{q.kw},'%') or ip like concat('%',#{q.kw},'%'))</if>" +
            "<if test='q.status!=null'> and status=#{q.status}</if>" +
            "<if test='q.from!=null and q.from!=\"\"'> and login_time&gt;=#{q.from}</if>" +
            "<if test='q.to!=null and q.to!=\"\"'> and login_time&lt;=#{q.to}</if>" +
            "order by login_time desc, id desc limit #{q.size} offset #{q.offset}</script>")
    List<LoginLog> page(@Param("q") PageQ q);

    @Select("<script>select count(*) from sys_login_log where 1=1" +
            "<if test='q.kw!=null and q.kw!=\"\"'> and (account like concat('%',#{q.kw},'%') or ip like concat('%',#{q.kw},'%'))</if>" +
            "<if test='q.status!=null'> and status=#{q.status}</if>" +
            "<if test='q.from!=null and q.from!=\"\"'> and login_time&gt;=#{q.from}</if>" +
            "<if test='q.to!=null and q.to!=\"\"'> and login_time&lt;=#{q.to}</if></script>")
    long count(@Param("q") PageQ q);

    @Insert("insert into sys_login_log(account,ip,zone,status,msg,login_time) values(#{l.account},#{l.ip},#{l.zone},#{l.status},#{l.msg},now())")
    int insert(@Param("l") LoginLog l);

    @Delete("<script>delete from sys_login_log where id in <foreach item='i' collection='ids' open='(' separator=',' close=')'>#{i}</foreach></script>")
    int delete(@Param("ids") List<Long> ids);

    @Select("select count(*) c, ifnull(sum(status=0),0) s, count(distinct account) accts, count(distinct ip) ips " +
            "from sys_login_log where login_time between #{from} and #{to}")
    Map<String, Object> stats(@Param("from") String from, @Param("to") String to);

    @Select("select weekday(login_time)+1 dow, hour(login_time) h, count(*) c from sys_login_log " +
            "where login_time>=#{from} group by dow,h")
    List<Map<String, Object>> heatmap(@Param("from") String from);

    @Select("select date_format(login_time,'%Y-%m-%d %H:00') t, count(*) c from sys_login_log where login_time>=#{from} group by t order by t")
    List<Map<String, Object>> pulseHour(@Param("from") String from);

    @Select("select date_format(login_time,'%Y-%m-%d') t, count(*) c from sys_login_log where login_time between #{from} and #{to} group by t order by t")
    List<Map<String, Object>> pulseDay(@Param("from") String from, @Param("to") String to);
}
