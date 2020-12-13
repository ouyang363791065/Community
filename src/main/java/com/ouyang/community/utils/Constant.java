package com.ouyang.community.utils;

/**
 * @author feixi
 * @Description 常量
 */
public class Constant {
    /**
     * 主题: 评论
     */
    public static final String TOPIC_COMMENT = "comment";

    /**
     * 主题: 点赞
     */
    public static final String TOPIC_LIKE = "like";

    /**
     * 主题: 关注
     */
    public static final String TOPIC_FOLLOW = "follow";

    /**
     * 主题: 发帖
     */
    public static final String TOPIC_PUBLISH = "publish";

    /**
     * 主题: 删帖
     */
    public static final String TOPIC_DELETE = "delete";

    /**
     * 主题: 分享
     */
    public static final String TOPIC_SHARE = "share";

    /**
     * 系统用户ID
     */
    public static final int SYSTEM_USER_ID = 1;

    /**
     * 权限: 普通用户
     */
    public static final String AUTHORITY_USER = "user";

    /**
     * 权限: 管理员
     */
    public static final String AUTHORITY_ADMIN = "admin";

    /**
     * 权限: 版主
     */
    public static final String AUTHORITY_MODERATOR = "moderator";

    /*** 点赞有关字段 ***/
    public static final Integer NOT_LIKED = 0;
    public static final Integer ALREADY_LIKED = 1;

    /*** 已登入/已登出 状态 ***/
    public static final Integer LOGIN = 0;
    public static final Integer LOGOUT = 1;
}
