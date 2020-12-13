package com.ouyang.community.service;

import java.util.List;
import java.util.Map;

/**
 * @Description:
 * @Author: feixi
 * @Date: 2020/11/24 10:51
 */
public interface FollowService {
    void follow(Long userId, Integer entityType, Integer entityId);
    void unfollow(Long userId, Integer entityType, Integer entityId);
    Long findFolloweeCount(Long userId, Integer entityType);
    Long findFollowerCount(Integer entityType, Integer entityId);
    Boolean hasFollowed(Long userId, Integer entityType, Integer entityId);
    List<Map<String, Object>> findFollowees(Long userId, Integer offset, Integer limit);
    List<Map<String, Object>> findFollowers(Long userId, Integer offset, Integer limit);
}
