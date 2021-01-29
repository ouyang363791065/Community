package com.ouyang.community.service.impl;

import com.ouyang.community.entity.LoginTicket;
import com.ouyang.community.mapper.LoginTicketMapper;
import com.ouyang.community.service.LoginTicketService;
import com.ouyang.community.utils.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

/**
 * @Description:
 * @Author: feixi
 * @Date: 2020/12/31 17:38
 */
@Service
public class TicketServiceImpl extends IBaseServiceImpl<LoginTicketMapper, LoginTicket> implements LoginTicketService {
    @Autowired
    private RedisTemplate<String,Object> redisTemplate;

    @Override
    public LoginTicket findLoginTicketFromRedis(String ticket) {
        String ticketKey = RedisKeyUtil.getTicketKey(ticket);
        return (LoginTicket) redisTemplate.opsForValue().get(ticketKey);
    }
}
