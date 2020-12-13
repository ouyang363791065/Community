package com.ouyang.community.service.impl;

import com.ouyang.community.exception.CommunityException;
import com.ouyang.community.http.HttpStatusCode;
import com.ouyang.community.service.LikeService;
import com.ouyang.community.utils.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * @author feixi
 * @Description 点赞Service
 */
@Service
public class LikeServiceImpl implements LikeService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public void like(Long userId, Integer entityType, Integer entityId, Long entityUserId) {
        /**
         * redisTemplate直接调用opfor..来操作redis数据库，每执行一条命令是要重新拿一个连接，
         * 因此很耗资源，让一个连接直接执行多条语句的方法就是使用SessionCallback，同样作用
         * 的还有RedisCallback，但不常用。
         */
        redisTemplate.execute(new SessionCallback<Object>() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
                String userLikeKey = RedisKeyUtil.getUserLikeKey(entityUserId);
                // 判断该用户id是否在set集合里，若在，则已点过赞
                Boolean isMember = operations.opsForSet().isMember(entityLikeKey, userId);
                // 多个更新操作，需要事务
                operations.multi();
                if (!Objects.isNull(isMember) && isMember) {
                    // 取消赞
                    operations.opsForSet().remove(entityLikeKey, userId);
                    // 增加用户收到的点赞数，类似王者荣耀，用户被点赞数
                    operations.opsForValue().decrement(userLikeKey);
                } else {
                    //点赞
                    operations.opsForSet().add(entityLikeKey, userId);
                    operations.opsForValue().increment(userLikeKey);
                }
                return operations.exec();
            }
        });
    }

    @Override
    public Integer findEntityLikeCount(Integer entityType, Integer entityId) {
        String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
        Long size = redisTemplate.opsForSet().size(entityLikeKey);
        if (Objects.isNull(size)) {
            throw new CommunityException(HttpStatusCode.ENTITY_NOT_EXIST);
        }
        return size.intValue();
    }

    @Override
    public Integer findEntityLikeStatus(Integer entityType, Integer entityId, Long userId) {
        String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
        Boolean flag = redisTemplate.opsForSet().isMember(entityLikeKey, userId);
        if (Objects.isNull(flag)) {
            throw new CommunityException(HttpStatusCode.ENTITY_NOT_EXIST);
        }
        // 此处返回Integer，是为了进行扩展。比如扩展踩，为止2.等等情况
        return flag ? 1 : 0;
    }

    @Override
    public Integer findUserLikeCount(Long userId) {
        String userLikeKey = RedisKeyUtil.getUserLikeKey(userId);
        Integer count = (Integer) redisTemplate.opsForValue().get(userLikeKey);
        if (Objects.isNull(count)) {
            throw new CommunityException(HttpStatusCode.USER_NOT_EXIST);
        }
        return count;
    }
}
