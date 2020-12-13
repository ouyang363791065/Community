package com.ouyang.community.utils;

/**
 * @author feixi
 * @Description Redis Key 生成工具
 */
public class RedisKeyUtil {

    private static final String SPLIT = ":";
    private static final String PREFIX_ENTITY_LIKE = "like:entity";
    private static final String PREFIX_USER_LIKE = "like:user";
    private static final String PREFIX_FOLLOWEE = "followee";
    private static final String PREFIX_FOLLOWER = "follower";

    // 验证码
    private static final String PREFIX_KAPTCHA = "kaptcha";

    // 登录凭证
    private static final String PREFIX_TICKET = "ticket";
    private static final String PREFIX_USER = "user";

    // 网站数据统计
    private static final String PREFIX_UV = "uv";
    private static final String PREFIX_DAU = "dau";

    private static final String PREFIX_POST = "post";

    /**
     * 某个实体收到的赞，如帖子，评论
     * 根据entityType和entityId生成redis的key
     *
     * @param entityType
     * @param entityId
     * @return
     */
    public static String getEntityLikeKey(Integer entityType, Integer entityId) {
        return PREFIX_ENTITY_LIKE + SPLIT + entityType + SPLIT + entityId;
    }

    /**
     * 某个用户收到的总赞数
     * 根据userId生成redis的key
     * like:user:userId -> int
     *
     * @param userId
     * @return
     */
    public static String getUserLikeKey(Long userId) {
        return PREFIX_USER_LIKE + SPLIT + userId;
    }

    /**
     * Followee 我的关注，Follower 粉丝
     * 某个用户关注的其他用户
     * 根据userId和entityType生成redis的key
     * followee:userId:entityType -> zset(entityId,date),用有序集合存，存的是关注的哪个实体，分数是当前时间。
     *
     * @param userId
     * @param entityType
     * @return
     */
    public static String getFolloweeKey(Long userId, Integer entityType) {
        return PREFIX_FOLLOWEE + SPLIT + userId + SPLIT + entityType;
    }

    /**
     * 某个实体拥有的粉丝，实体可能是用户，或者是帖子
     * follower:entityType:entityId -> zset(userId,date)，存入用户Id
     *
     * @param entityType
     * @param entityId
     * @return
     */
    public static String getFollowerKey(Integer entityType, Integer entityId) {
        return PREFIX_FOLLOWER + SPLIT + entityType + SPLIT + entityId;
    }

    /**
     * 登录验证码
     * owner是指随机生成的uuid
     *
     * @param owner
     * @return
     */
    public static String getKaptchaKey(String owner) {
        return PREFIX_KAPTCHA + SPLIT + owner;
    }

    /**
     * 登录的凭证
     *
     * @param ticket
     * @return
     */
    public static String getTicketKey(String ticket) {
        return PREFIX_TICKET + SPLIT + ticket;
    }

    /**
     * 用户
     *
     * @param userId
     * @return
     */
    public static String getUserKey(Long userId) {
        return PREFIX_USER + SPLIT + userId;
    }

    // 单日uv
    public static String getUVKey(String date) {
        return PREFIX_UV + SPLIT + date;
    }

    // 区间UV
    public static String getUVKey(String startDate, String endData) {
        return PREFIX_UV + SPLIT + startDate + SPLIT + endData;
    }

    // 单日DAU
    public static String getDAUKey(String date) {
        return PREFIX_DAU + SPLIT + date;
    }

    // 区间DAU
    public static String getDAUKey(String startDate, String endDate) {
        return PREFIX_DAU + SPLIT + startDate + SPLIT + endDate;
    }

    /**
     * 帖子分数
     *
     * @return
     */
    public static String getPostScoreKey() {
        return PREFIX_POST + SPLIT + "score";
    }
}
