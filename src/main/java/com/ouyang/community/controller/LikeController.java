package com.ouyang.community.controller;

import com.ouyang.community.entity.Event;
import com.ouyang.community.entity.User;
import com.ouyang.community.enums.CommunityEnum;
import com.ouyang.community.kafka.EventProducer;
import com.ouyang.community.http.HttpResult;
import com.ouyang.community.http.HttpUtil;
import com.ouyang.community.service.LikeService;
import com.ouyang.community.utils.Constant;
import com.ouyang.community.utils.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;

/**
 * @author feixi
 * @Description 点赞 Controller
 */
@Controller
public class LikeController {
    @Autowired
    private LikeService likeService;

    @Autowired
    private RedisTemplate<String,Object> redisTemplate;

    @Autowired
    private EventProducer eventProducer;

    /**
     *
     * @param entityType 帖子/评论
     * @param entityId 帖子/评论 的id
     * @param entityUserId 实体所属用户id
     * @param postId 帖子id
     * @return
     */
    @ResponseBody
    @PostMapping("/like")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'MODERATOR')")
    public HttpResult<HashMap<String, Integer>> like(Integer entityType, Integer entityId, Long entityUserId, Integer postId) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        // 点赞
        likeService.like(user.getId(), entityType, entityId, entityUserId);
        // 点赞数量
        Integer likeCount = likeService.findEntityLikeCount(entityType, entityId);
        // 点赞状态
        Integer likeStatus = likeService.findEntityLikeStatus(entityType, entityId, user.getId());
        // 返回的结果
        HashMap<String, Integer> map = new HashMap<>();
        map.put("likeCount", likeCount);
        map.put("likeStatus", likeStatus);

        // 点赞成功，触发点赞事件，仅点赞需要通知
        if (Constant.ALREADY_LIKED.equals(likeStatus)) {
            Event event = new Event()
                    .setTopic(Constant.TOPIC_LIKE)
                    .setUserId(user.getId())
                    .setEntityType(entityType)
                    .setEntityId(entityId)
                    .setEntityUserId(entityUserId.intValue())
                    .setData("postId", postId);
            // 推送消息到kafka
            eventProducer.fireEvent(event);

            if (CommunityEnum.ENTITY_TYPE_POST.getCode().equals(entityType)) {
                // 计算帖子分数
                String redisKey = RedisKeyUtil.getPostScoreKey();
                redisTemplate.opsForSet().add(redisKey, postId);
            }
        }
        return HttpUtil.buildSuccessResult(map);
    }
}
