package com.ouyang.community.controller;

import com.ouyang.community.entity.Event;
import com.ouyang.community.entity.Page;
import com.ouyang.community.entity.User;
import com.ouyang.community.enums.CommunityEnum;
import com.ouyang.community.kafka.EventProducer;
import com.ouyang.community.http.HttpResult;
import com.ouyang.community.http.HttpStatusCode;
import com.ouyang.community.http.HttpUtil;
import com.ouyang.community.service.FollowService;
import com.ouyang.community.service.UserService;
import com.ouyang.community.utils.Constant;
import com.ouyang.community.utils.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @Description: 关注
 * @Author: feixi
 * @Date: 2020/11/24 10:51
 */
@Controller
public class FollowController {
    @Autowired
    private FollowService followService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private UserService userService;

    @Autowired
    private EventProducer eventProducer;

    /**
     * 关注用户
     *
     * @param entityType 默认为用户
     * @param entityId 默认为用户id
     * @return
     */
    @ResponseBody
    @PostMapping("/follow")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'MODERATOR')")
    public HttpResult<String> follow(Integer entityType, Integer entityId) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        followService.follow(user.getId(), entityType, entityId);
        // 目前只实现了关注人，所以setEntityUserId为entityId
        Event event = new Event()
                .setTopic(Constant.TOPIC_FOLLOW)
                .setUserId(user.getId())
                .setEntityType(entityType)
                .setEntityId(entityId)
                .setEntityUserId(entityId);
        eventProducer.fireEvent(event);
        return HttpUtil.buildResult("已关注!", HttpStatusCode.SUCCESS);
    }

    @ResponseBody
    @PostMapping("/unfollow")
    public HttpResult<String> unfollow(Integer entityType, Integer entityId) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        followService.unfollow(user.getId(), entityType, entityId);
        return HttpUtil.buildResult("已取消关注!", HttpStatusCode.SUCCESS);
    }

    @GetMapping("/followees/{userId}")
    public String getFollowees(@PathVariable("userId") Long userId, Page page, Model model) {
        User user = userService.getById(userId);
        if (Objects.isNull(user)) {
            throw new RuntimeException("该用户不存在!");
        }
        model.addAttribute("user", user);
        page.setLimit(5);
        page.setPath("/followees/" + userId);
        page.setRows(followService.findFolloweeCount(userId, CommunityEnum.ENTITY_TYPE_USER.getCode()).intValue());
        List<Map<String, Object>> userList = followService.findFollowees(userId, page.getOffset(), page.getLimit());
        if (Objects.nonNull(userList)) {
            for (Map<String, Object> map : userList) {
                User u = (User) map.get("user");
                map.put("hasFollowed", hasFollowed(u.getId()));
            }
        }
        model.addAttribute("users", userList);
        return "/site/followee";
    }

    @GetMapping("/followers/{userId}")
    public String getFollowers(@PathVariable("userId") Long userId, Page page, Model model) {
        User user = userService.getById(userId);
        if (Objects.isNull(user)) {
            throw new RuntimeException("该用户不存在!");
        }
        model.addAttribute("user", user);
        page.setLimit(5);
        page.setPath("/followers/" + userId);
        page.setRows(followService.findFollowerCount(CommunityEnum.ENTITY_TYPE_USER.getCode(), userId.intValue()).intValue());
        List<Map<String, Object>> userList = followService.findFollowers(userId, page.getOffset(), page.getLimit());
        if (Objects.nonNull(userList)) {
            for (Map<String, Object> map : userList) {
                User u = (User) map.get("user");
                // 当我们正在查看一个用户的粉丝时，是可以关注该用户的粉丝的，所以我们在这里查看一下
                // 我们是否关注了该用户的某个粉丝，标识一个状态，以供页面显示
                map.put("hasFollowed", hasFollowed(u.getId()));
            }
        }
        model.addAttribute("users", userList);
        return "/site/follower";
    }

    private boolean hasFollowed(Long userId) {
        // 若当前未登入，也是可以查看某个用户的粉丝的，显示的是未关注
        if (Objects.isNull(SecurityContextHolder.getContext().getAuthentication().getPrincipal())) {
            return false;
        }
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return followService.hasFollowed(user.getId(), CommunityEnum.ENTITY_TYPE_USER.getCode(), userId.intValue());
    }
}
