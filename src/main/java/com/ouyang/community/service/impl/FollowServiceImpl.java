package com.ouyang.community.service.impl;

import com.ouyang.community.entity.User;
import com.ouyang.community.enums.CommunityEnum;
import com.ouyang.community.exception.CommunityException;
import com.ouyang.community.http.HttpStatusCode;
import com.ouyang.community.service.FollowService;
import com.ouyang.community.service.UserService;
import com.ouyang.community.utils.Constant;
import com.ouyang.community.utils.RedisKeyUtil;
import org.apache.tomcat.util.bcel.Const;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @Description:
 * @Author: feixi
 * @Date: 2020/11/24 10:57
 */
@Service
public class FollowServiceImpl implements FollowService {
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private UserService userService;

    /**
     * 关注某实体
     *
     * @param userId
     * @param entityType
     * @param entityId
     */
    @Override
    public void follow(Long userId, Integer entityType, Integer entityId) {
        redisTemplate.execute(new SessionCallback<Object>() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
                String followerKey = RedisKeyUtil.getFollowerKey(entityType, entityId);
                operations.multi();
                // 根据时间的先后顺序对(点过关注的人)进行排序
                operations.opsForZSet().add(followeeKey, entityId, System.currentTimeMillis());
                // 根据时间的先后顺序对(某人的粉丝)进行排序
                operations.opsForZSet().add(followerKey, userId, System.currentTimeMillis());
                return operations.exec();
            }
        });
    }

    /**
     * 对某实体取消关注
     *
     * @param userId
     * @param entityType
     * @param entityId
     */
    @Override
    public void unfollow(Long userId, Integer entityType, Integer entityId) {
        redisTemplate.execute(new SessionCallback<Object>() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
                String followerKey = RedisKeyUtil.getFollowerKey(entityType, entityId);
                operations.multi();
                operations.opsForZSet().remove(followeeKey, entityId);
                operations.opsForZSet().remove(followerKey, userId);
                return operations.exec();
            }
        });
    }

    /**
     * 查询 我的关注 的数量
     *
     * @param userId
     * @param entityType
     * @return
     */
    @Override
    public Long findFolloweeCount(Long userId, Integer entityType) {
        String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
        return redisTemplate.opsForZSet().zCard(followeeKey);
    }

    /**
     * 查询 我的粉丝 的数量
     *
     * @param entityType
     * @param entityId
     * @return
     */
    @Override
    public Long findFollowerCount(Integer entityType, Integer entityId) {
        String followerKey = RedisKeyUtil.getFollowerKey(entityType, entityId);
        return redisTemplate.opsForZSet().zCard(followerKey);
    }

    /**
     * 查询用户是否已关注该实体
     *
     * @param userId
     * @param entityType
     * @param entityId
     * @return
     */
    @Override
    public Boolean hasFollowed(Long userId, Integer entityType, Integer entityId) {
        String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
        // 通过查询该实体的分数，判断是否存在
        return redisTemplate.opsForZSet().score(followeeKey, entityId) != null;
    }

    /**
     * 查询某用户关注的人
     *
     * @param userId
     * @param offset 分页开始
     * @param limit
     * @return
     */
    @Override
    public List<Map<String, Object>> findFollowees(Long userId, Integer offset, Integer limit) {
        String followeeKey = RedisKeyUtil.getFolloweeKey(userId, CommunityEnum.ENTITY_TYPE_USER.getCode());
        Set<Object> targetIds = redisTemplate.opsForZSet().range(followeeKey, offset, offset + limit - 1);
        if (Objects.isNull(targetIds)) {
            return null;
        }
        List<Map<String, Object>> list = new ArrayList<>();
        for (Object targetId : targetIds) {
            Map<String, Object> map = new HashMap<>();
            User user = userService.getById((Long) targetId);
            map.put("user", user);
            Double score = redisTemplate.opsForZSet().score(followeeKey, targetId);
            assert score != null;
            map.put("followTime", new Date(score.longValue()));
            list.add(map);
        }
        return list;
    }

    /**
     * 查询某用户/帖子的粉丝
     * 关注/赞
     *
     * @param userId
     * @param offset
     * @param limit
     * @return
     */
    @Override
    public List<Map<String, Object>> findFollowers(Long userId, Integer offset, Integer limit) {
        String followerKey = RedisKeyUtil.getFollowerKey(CommunityEnum.ENTITY_TYPE_USER.getCode(), userId.intValue());
        Set<Object> targetIds = redisTemplate.opsForZSet().reverseRange(followerKey, offset, offset + limit - 1);
        if (Objects.isNull(targetIds)) {
            return null;
        }
        List<Map<String, Object>> list = new ArrayList<>();
        for (Object targetId : targetIds) {
            Map<String, Object> map = new HashMap<>();
            User user = userService.getById((Integer)targetId);
            map.put("user", user);
            Double score = redisTemplate.opsForZSet().score(followerKey, targetId);
            assert score != null;
            map.put("followTime", new Date(score.longValue()));
            list.add(map);
        }
        return list;
    }
}
