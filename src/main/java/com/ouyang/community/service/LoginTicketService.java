package com.ouyang.community.service;

import com.ouyang.community.entity.LoginTicket;

/**
 * @Description:
 * @Author: feixi
 * @Date: 2020/12/31 17:37
 */
public interface LoginTicketService extends IBaseService<LoginTicket> {
    LoginTicket findLoginTicketFromRedis(String ticket);
}
