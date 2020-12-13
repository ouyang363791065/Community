package com.ouyang.community.controller;

import com.ouyang.community.entity.User;
import com.ouyang.community.event.EventProducer;
import com.ouyang.community.http.HttpResult;
import com.ouyang.community.http.HttpUtil;
import com.ouyang.community.service.LikeService;
import com.ouyang.community.utils.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
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
    private HostHolder hostHolder;

    @Autowired
    private EventProducer eventProducer;

    @ResponseBody
    @PostMapping("/dd")
    public String like() {
        return "xxx";
    }

    @ResponseBody
    @PostMapping("/like")
    public HttpResult<HashMap<String, Integer>> like(Integer entityType, Integer entityId, Long entityUserId, Integer postId) {
        User user = hostHolder.getUser();
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

        // TODO: 触发kafka的消息推送
        // 点赞成功，触发点赞事件，仅点赞需要通知
//        if (Constant.ALREADY_LIKED.equals(likeStatus)) {
//            Event event = new Event()
//                    .setTopic(Constant.TOPIC_LIKE)
//                    .setUserId(hostHolder.getUser().getId())
//                    .setEntityType(entityType)
//                    .setEntityId(entityId)
//                    .setEntityUserId(entityUserId)
//                    .setData("postId", postId);
//            // 推送消息到kafka队列
//            // eventProducer.fireEvent(event);
//
//            if (entityType == CommunityEnum.ENTITY_TYPE_POST.getCode()) {
//                // 计算帖子分数
//                String redisKey = RedisKeyUtil.getPostScoreKey();
//                redisTemplate.opsForSet().add(redisKey, postId);
//            }
//        }
        return HttpUtil.buildSuccessResult(map);
    }
}
