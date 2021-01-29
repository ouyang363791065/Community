package com.ouyang.community.entity;

import lombok.Data;

import java.util.Date;

/**
 * @author feixi
 * @Description 登录凭证，不与表一一映射，将LoginTicket对象存入redis
 */
@Data
public class LoginTicket extends EntityBase{
    private Long userId;
    private String ticket;
    /**
     * 已登入/已登出
     */
    private Integer status;
    private Date expired;
}
