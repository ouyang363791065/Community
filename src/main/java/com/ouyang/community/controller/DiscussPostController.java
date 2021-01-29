package com.ouyang.community.controller;

import com.ouyang.community.entity.*;
import com.ouyang.community.enums.CommunityEnum;
import com.ouyang.community.kafka.EventProducer;
import com.ouyang.community.exception.CommunityException;
import com.ouyang.community.http.HttpResult;
import com.ouyang.community.http.HttpStatusCode;
import com.ouyang.community.http.HttpUtil;
import com.ouyang.community.service.CommentService;
import com.ouyang.community.service.DiscussPostService;
import com.ouyang.community.service.LikeService;
import com.ouyang.community.service.UserService;
import com.ouyang.community.utils.CommunityUtil;
import com.ouyang.community.utils.Constant;
import com.ouyang.community.utils.HostHolder;
import com.ouyang.community.utils.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * @Description: 帖子发布
 * @Author: feixi
 * @Date: 2020/11/25 10:59
 */
@Controller
@RequestMapping("/discuss")
public class DiscussPostController {
    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private UserService userService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private LikeService likeService;

    @Autowired
    private EventProducer eventProducer;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    // TODO: es
//    @Autowired
//    private ElasticsearchService elasticsearchService;

    @ResponseBody
    @PostMapping("/add")
    public HttpResult<String> addDiscussPost(String title, String content) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (user == null) {
            throw new CommunityException("你还没有登录哦!", HttpStatusCode.NOT_LOGIN);
        }
        DiscussPost post = new DiscussPost();
        post.setUserId(user.getId());
        post.setTitle(title);
        post.setContent(content);
        post.setCreateTime(System.currentTimeMillis());
        discussPostService.addDiscussPost(post);

        // 触发发帖事件
        Event event = new Event()
                .setTopic(Constant.TOPIC_PUBLISH)
                .setUserId(user.getId())
                .setEntityType(CommunityEnum.ENTITY_TYPE_POST.getCode())
                .setEntityId(post.getId().intValue());

        // TODO:
        // eventProducer.fireEvent(event);

        // 计算帖子分数
        String redisKey = RedisKeyUtil.getPostScoreKey();
        redisTemplate.opsForSet().add(redisKey, post.getId());

        // 报错的情况,将来统一处理。
        return HttpUtil.buildResult("发布成功", HttpStatusCode.SUCCESS);
    }

    @GetMapping("/detail/{discussPostId}")
    public String getDiscussPost(@PathVariable("discussPostId") Integer discussPostId, Model model, Page page) {
        // 帖子
        DiscussPost post = discussPostService.findDiscussPostById(new Long(discussPostId));
        model.addAttribute("post", post);
        // 作者
        User user = userService.getById(post.getUserId());
        model.addAttribute("user", user);

        // 点赞数量
        long likeCount = likeService.findEntityLikeCount(CommunityEnum.ENTITY_TYPE_POST.getCode(), discussPostId);
        model.addAttribute("likeCount", likeCount);
        // 点赞状态
        int likeStatus = SecurityContextHolder.getContext().getAuthentication().getPrincipal() instanceof String ? 0 :
                likeService.findEntityLikeStatus(CommunityEnum.ENTITY_TYPE_POST.getCode(), discussPostId, ((User) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId());
        model.addAttribute("likeStatus", likeStatus);

        // 评论分页信息
        page.setLimit(5);
        page.setPath("/discuss/detail/" + discussPostId);
        page.setRows(post.getCommentCount());

        // 评论: 给帖子的评论
        // 回复: 给评论的评论
        // 评论列表
        List<Comment> commentList = commentService.findCommentsByEntity(
                CommunityEnum.ENTITY_TYPE_POST.getCode(), post.getId().intValue(), page.getOffset(), page.getLimit());
        // 评论VO列表
        List<Map<String, Object>> commentVoList = new ArrayList<>();
        if (commentList != null) {
            for (Comment comment : commentList) {
                // 评论VO
                Map<String, Object> commentVo = new HashMap<>();
                // 评论
                commentVo.put("comment", comment);
                // 作者
                commentVo.put("user", userService.getById(comment.getUserId()));
                //点赞数量
                likeCount = likeService.findEntityLikeCount(CommunityEnum.ENTITY_TYPE_COMMENT.getCode(), comment.getId().intValue());
                commentVo.put("likeCount", likeCount);
                //点赞状态,需要判断当前用户是否登录，没有登录无法点赞
                likeStatus = SecurityContextHolder.getContext().getAuthentication().getPrincipal() instanceof String ? 0 : likeService.findEntityLikeStatus(
                        CommunityEnum.ENTITY_TYPE_COMMENT.getCode(), comment.getId().intValue(), ((User) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId());
                commentVo.put("likeStatus", likeStatus);
                // 回复列表
                List<Comment> replyList = commentService.findCommentsByEntity(
                        CommunityEnum.ENTITY_TYPE_COMMENT.getCode(), comment.getId().intValue(), 0, Integer.MAX_VALUE);
                // 回复VO列表
                List<Map<String, Object>> replyVoList = new ArrayList<>();
                if (replyList != null) {
                    for (Comment reply : replyList) {
                        Map<String, Object> replyVo = new HashMap<>();
                        // 回复
                        replyVo.put("reply", reply);
                        // 作者
                        replyVo.put("user", userService.getById(reply.getUserId()));
                        // 回复目标
                        User target = reply.getTargetId() == 0 ? null : userService.getById(reply.getTargetId());
                        replyVo.put("target", target);
                        //点赞数量
                        likeCount = likeService.findEntityLikeCount(CommunityEnum.ENTITY_TYPE_COMMENT.getCode(), reply.getId().intValue());
                        replyVo.put("likeCount", likeCount);
                        //点赞状态,需要判断当前用户是否登录，没有登录无法点赞
                        likeStatus = SecurityContextHolder.getContext().getAuthentication().getPrincipal() instanceof String ? 0 : likeService.findEntityLikeStatus(
                                CommunityEnum.ENTITY_TYPE_COMMENT.getCode(), reply.getId().intValue(), ((User) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId());
                        replyVo.put("likeStatus", likeStatus);
                        replyVoList.add(replyVo);
                    }
                }
                commentVo.put("replys", replyVoList);
                // 回复数量
                int replyCount = commentService.findCommentCount(CommunityEnum.ENTITY_TYPE_COMMENT.getCode(), comment.getId().intValue());
                commentVo.put("replyCount", replyCount);

                commentVoList.add(commentVo);
            }
        }
        model.addAttribute("comments", commentVoList);

        return "/site/discuss-detail";
    }

    // 置顶
    @ResponseBody
    @PostMapping("/top")
    public HttpResult setTop(Integer id) {
        discussPostService.updateType(new Long(id), 1);

        // 触发发帖事件
        Event event = new Event()
                .setTopic(Constant.TOPIC_PUBLISH)
                .setUserId(((User) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId())
                .setEntityType(CommunityEnum.ENTITY_TYPE_POST.getCode())
                .setEntityId(id);
        eventProducer.fireEvent(event);

        return HttpUtil.buildSuccessResult(0);
    }

    // 加精
    @ResponseBody
    @PostMapping("/wonderful")
    public String setWonderful(Integer id) {
        discussPostService.updateStatus(new Long(id), 1);

        // 触发发帖事件
        Event event = new Event()
                .setTopic(Constant.TOPIC_PUBLISH)
                .setUserId(((User) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId())
                .setEntityType(CommunityEnum.ENTITY_TYPE_POST.getCode())
                .setEntityId(id);
        eventProducer.fireEvent(event);

        // 计算帖子分数
        String redisKey = RedisKeyUtil.getPostScoreKey();
        redisTemplate.opsForSet().add(redisKey, id);

        return CommunityUtil.getJSONString(0);
    }

    // 删除
    @RequestMapping(path = "/delete", method = RequestMethod.POST)
    @ResponseBody
    public String setDelete(Integer id) {
        discussPostService.updateStatus(new Long(id), 2);

        // 触发删帖事件
        Event event = new Event()
                .setTopic(Constant.TOPIC_DELETE)
                .setUserId(((User) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId())
                .setEntityType(CommunityEnum.ENTITY_TYPE_POST.getCode())
                .setEntityId(id);
        eventProducer.fireEvent(event);
        // TODO:ES
        // elasticsearchService.deleteDiscussPost(id);
        return CommunityUtil.getJSONString(0);
    }
}
