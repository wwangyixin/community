package com.newcoder.community.dao;

import com.newcoder.community.entity.LoginTicket;
import com.newcoder.community.entity.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
@Deprecated // 不推荐使用
public interface LoginTicketMapper {

    @Insert("insert into login_ticket (user_id, ticket, status, expired) values (#{userId}, #{ticket}, #{status}, #{expired})")
    int insertLoginTicket(LoginTicket loginTicket);

    @Select("select id, user_id, ticket, status, expired  from login_ticket where ticket = #{ticket} and status != 2")
    LoginTicket selectByTicket(String ticket);

    @Update("update login_ticket set status = #{status} where ticket = #{ticket}")
    int updateStatus(String ticket, int status);
}
