package com.ouyang.community.service;

/**
 * @Description:
 * @Author: feixi
 * @Date: 2020/11/11 10:40
 */
public interface LikeService {
    /**
     * 某个人，对某个帖子或者评论点赞/取消赞
     * 实现思路: 我们根据entityType和entityId设置redis的key为like:entity:entityType:entityId字符串，
     * 设置value为set集合，集合里面存储userId
     * <p>
     * set: key value1 value2 value3
     *    -key like:entity:entityType:entityId
     *    -value userId1,userId2
     * @param userId       :点赞的人
     * @param entityType   :对啥玩意进行点赞，评论or帖子？？
     * @param entityId     :点赞实体的id，帖子id or 评论id
     * @param entityUserId :被赞的人，即作者
     */
    void like(Long userId, Integer entityType, Integer entityId, Long entityUserId);

    /**
     * 统计帖子或者评论的点赞数量
     *
     * @param entityType
     * @param entityId
     * @Description:
     * @return: Long
     */
    Integer findEntityLikeCount(Integer entityType, Integer entityId);

    /**
     * 某个人，是否点赞了该帖子或者评论呢？
     *
     * @param entityType
     * @param entityId
     * @param userId
     * @Description:
     * @return: Integer
     */
    Integer findEntityLikeStatus(Integer entityType, Integer entityId, Long userId);

    /**
     * 某个人，他的评论和帖子获得的赞有多少呢？
     *
     * @param userId
     * @Description: 查询某个用户获得赞，用于在个人主页查看收获了多少赞
     * @return: Integer
     */
    Integer findUserLikeCount(Long userId);
}
