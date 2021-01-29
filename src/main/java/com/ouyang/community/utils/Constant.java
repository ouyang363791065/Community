package com.ouyang.community.utils;

import java.util.Arrays;
import java.util.List;

/**
 * @author feixi
 * @Description 常量
 */
public class Constant {
    /*** 触发消息推送 ***/
    public static final String TOPIC_COMMENT = "COMMENT";  // 主题: 评论
    public static final String TOPIC_LIKE = "LIKE";  // 主题: 点赞
    public static final String TOPIC_FOLLOW = "FOLLOW";  // 主题: 关注
    public static final String TOPIC_PUBLISH = "PUBLISH";  // 主题: 发帖
    public static final String TOPIC_DELETE = "DELETE";  // 主题: 删帖
    public static final String TOPIC_SHARE = "SHARE";  // 主题: 分享

    /**
     * 系统用户ID
     */
    public static final int SYSTEM_USER_ID = 1;

    /*** 基于Spring Security的权限配置 ***/
    public static final String AUTHORITY_USER = "USER";
    public static final String AUTHORITY_ADMIN = "ADMIN";
    public static final String AUTHORITY_MODERATOR = "MODERATOR";  //版主

    /*** 不需要登入就能访问的请求 ***/
    public static final List<String> UNLOGIN_PERMIT_URL = Arrays.asList(
            "/user/setting",
            "/user/upload",
            "/discuss/add",
            "/comment/add/**",
            "/letter/**",
            "/notice/**",
            "/like",
            "/follow",
            "/unfollow"
    );

    /*** 点赞有关字段 ***/
    public static final Integer NOT_LIKED = 0;
    public static final Integer ALREADY_LIKED = 1;

    /*** 已登入/已登出 状态 ***/
    public static final Integer LOGIN = 0;
    public static final Integer LOGOUT = 1;

    /*** 文件系统 ***/
    public static final String FILE_SYSTEM_LOCAL = "local";
    public static final String FILE_SYSTEM_DATABASE = "database";
}
