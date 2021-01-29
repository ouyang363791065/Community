package com.ouyang.community.controller;

import com.ouyang.community.entity.Comment;
import com.ouyang.community.entity.DiscussPost;
import com.ouyang.community.entity.Event;
import com.ouyang.community.entity.User;
import com.ouyang.community.enums.CommunityEnum;
import com.ouyang.community.kafka.EventProducer;
import com.ouyang.community.service.CommentService;
import com.ouyang.community.service.DiscussPostService;
import com.ouyang.community.utils.Constant;
import com.ouyang.community.utils.HostHolder;
import com.ouyang.community.utils.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @Description:
 * @Author: feixi
 * @Date: 2021/1/28 12:00
 */
@Controller
@RequestMapping("/comment")
public class CommentController {
    @Autowired
    private CommentService commentService;
    @Autowired
    private HostHolder hostHolder;
    @Autowired
    private EventProducer eventProducer;
    @Autowired
    private DiscussPostService discussPostService;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @PreAuthorize("hasAnyAuthority('USER','ADMIN','MODERATOR')")
    @PostMapping("/add/{discussPostId}")
    public String addComment(@PathVariable("discussPostId") Integer discussPostId, Comment comment) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        comment.setUserId(user.getId());
        comment.setStatus(0);
        comment.setCreateTime(System.currentTimeMillis());
        comment.setTargetId(0);
        commentService.addComment(comment);

        // 触发评论事件
        Event event = new Event()
                .setTopic(Constant.TOPIC_COMMENT)
                .setUserId(user.getId())
                .setEntityType(comment.getEntityType())
                .setEntityUserId(comment.getEntityId())
                .setData("postId", discussPostId);
        if (CommunityEnum.ENTITY_TYPE_POST.getCode().equals(comment.getEntityType())) {
            // 给帖子评论
            DiscussPost target = discussPostService.findDiscussPostById(comment.getEntityId().longValue());
            event.setEntityUserId(target.getUserId().intValue());
        } else if (CommunityEnum.ENTITY_TYPE_COMMENT.getCode().equals(comment.getEntityType())) {
            // 给评论点赞
            Comment target = commentService.findCommentById(comment.getEntityId());
            event.setEntityUserId(target.getUserId().intValue());
        }
        eventProducer.fireEvent(event);

        // 触发发帖事件，因为评论帖子时，帖子的评论数量就更改了，需要更新elasticsearch中的数据
//        if (comment.getEntityType() == CommunityEnum.ENTITY_TYPE_POST.getCode()) {
//            event = new Event()
//                    .setTopic(TOPIC_PUBLISH)
//                    .setUserId(hostHolder.getUser().getId())
//                    .setEntityType(ENTITY_TYPE_POST)
//                    .setEntityId(discussPostId);
//            eventProducer.fireEvent(event);
//
//            // 计算帖子分数
//            String redisKey = RedisKeyUtil.getPostScoreKey();
//            redisTemplate.opsForSet().add(redisKey, discussPostId);
//        }

        return "redirect:/discuss/detail/" + discussPostId;
    }
}

