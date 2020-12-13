package com.ouyang.community.enums;

import lombok.Getter;

/**
 * @Description:
 * @Author: feixi
 * @Date: 2020/11/11 14:38
 */
@Getter
public enum CommunityEnum {
    /*** 账号模块 ***/
    ACTIVATION_SUCCESS(0, "激活成功"),
    ACTIVATION_REPEAT(1, "重复激活"),
    ACTIVATION_FAILURE(2, "激活失败"),
    NOT_ACTIVATION(0, "未激活"),
    ALREADY_ACTIVATION(1, "已激活"),

    /*** 登入模块 ***/
    DEFAULT_EXPIRED_SECONDS(3600 * 12, "默认状态的登录凭证的超时时间"),
    REMEMBER_EXPIRED_SECONDS(3600 * 24 * 100, "记住状态的登录凭证超时时间"),
    LOGIN(0, "已登入"),
    LOGOUT(1, "已登出"),

    /*** 实体模块 ***/
    ENTITY_TYPE_POST(1, "实体类型: 帖子"),
    ENTITY_TYPE_COMMENT(2, "实体类型: 评论"),
    ENTITY_TYPE_USER(3, "实体类型: 用户");

    private final Integer code;
    private final String msg;

    CommunityEnum(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}
