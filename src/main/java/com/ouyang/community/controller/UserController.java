package com.ouyang.community.controller;

import com.ouyang.community.entity.User;
import com.ouyang.community.enums.CommunityEnum;
import com.ouyang.community.service.FollowService;
import com.ouyang.community.service.LikeService;
import com.ouyang.community.service.UserService;
import com.ouyang.community.utils.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Objects;

/**
 * @Description:
 * @Author: feixi
 * @Date: 2020/11/24 10:49
 */
@Controller
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private LikeService likeService;

    @Autowired
    private FollowService followService;

    @Autowired
    private HostHolder hostHolder;

    // 个人主页
    @RequestMapping(path = "/profile/{userId}", method = RequestMethod.GET)
    public String getProfilePage(@PathVariable("userId") Long userId, Model model) {
        User user = userService.getById(userId);
        if (Objects.isNull(user)) {
            throw new RuntimeException("该用户不存在!");
        }
        // 用户
        model.addAttribute("user", user);
        // 点赞数量
        int likeCount = likeService.findUserLikeCount(userId);
        model.addAttribute("likeCount", likeCount);
        // 关注数量
        long followeeCount = followService.findFolloweeCount(userId, CommunityEnum.ENTITY_TYPE_USER.getCode());
        model.addAttribute("followeeCount", followeeCount);
        // 粉丝数量
        long followerCount = followService.findFollowerCount(CommunityEnum.ENTITY_TYPE_USER.getCode(), userId.intValue());
        model.addAttribute("followerCount", followerCount);
        // 是否已关注
        boolean hasFollowed = false;
        if (hostHolder.getUser() != null) {
            hasFollowed = followService.hasFollowed(hostHolder.getUser().getId(),
                    CommunityEnum.ENTITY_TYPE_USER.getCode(), userId.intValue());
        }
        model.addAttribute("hasFollowed", hasFollowed);
        return "/site/profile";
    }
}
