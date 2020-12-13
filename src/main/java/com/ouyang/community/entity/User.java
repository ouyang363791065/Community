package com.ouyang.community.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class User extends EntityBase{
    private String username;
    private String password;
    private String salt;
    private String email;

    /**
     * 0-普通用户; 1-超级管理员; 2-版主;
     */
    private Integer type;

    /**
     * 0-未激活; 1-已激活;
     */
    private Integer status;
    private String activationCode;
    private String headerUrl;
}
