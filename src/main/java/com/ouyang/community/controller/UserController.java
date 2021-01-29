package com.ouyang.community.controller;

import com.ouyang.community.entity.User;
import com.ouyang.community.enums.CommunityEnum;
import com.ouyang.community.service.FollowService;
import com.ouyang.community.service.LikeService;
import com.ouyang.community.service.UserService;
import com.ouyang.community.service.file.FileManagementService;
import com.ouyang.community.utils.CommunityUtil;
import com.ouyang.community.utils.HostHolder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Part;
import javax.servlet.http.HttpServletResponse;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Objects;

/**
 * @Description:
 * @Author: feixi
 * @Date: 2020/11/24 10:49
 */
@Slf4j
@Controller
@RequestMapping("/user")
public class UserController {
    @Value("${community.path.upload}")
    private String uploadPath;
    @Value("${community.path.domain}")
    private String domain;
    @Value("${server.servlet.context-path}")
    private String contextPath;
    @Autowired
    private UserService userService;
    @Autowired
    private LikeService likeService;
    @Autowired
    private FollowService followService;
    @Autowired
    private FileManagementService fileManagementService;

    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'MODERATOR')")
    @GetMapping("/setting")
    public String getSettingPage() {
        return "/site/setting";
    }

    // 这是图片上传到本地database
    @PostMapping(path = "/upload")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'MODERATOR')")
    public String uploadHeader(@RequestPart Part file, Model model) {
        if (file == null) {
            model.addAttribute("error", "您还没有选择图片!");
            return "/site/setting";
        }
        String fileName = null;
        try {
            fileName = fileManagementService.uploadFile(1L, file, "user");
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 更新当前用户的头像的路径(web访问路径)
        // http://localhost:8080/community/user/header/xxx.jpg
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String headerUrl = domain + contextPath + "/file/user/header/" + fileName;
        userService.updateHeader(user.getId(), headerUrl);
        // 修改完头像时，Security上下文里存储的用户信息是过期的，需要更新修改的头像信息
        user.setHeaderUrl(headerUrl);
        return "redirect:/index";
    }

    // 废弃，现在直接放在本地database
    @RequestMapping(path = "/header/{fileName}", method = RequestMethod.GET)
    public void getHeader(@PathVariable("fileName") String fileName, HttpServletResponse response) {
        // 服务器存放路径
        fileName = uploadPath + "/" + fileName;
        // 文件后缀
        String suffix = fileName.substring(fileName.lastIndexOf("."));
        // 响应图片
        response.setContentType("image/" + suffix);
        try (
                //放在try中，final会自动关闭
                FileInputStream fis = new FileInputStream(fileName);
                OutputStream os = response.getOutputStream();
        ) {
            byte[] buffer = new byte[1024];
            int b = 0;
            while ((b = fis.read(buffer)) != -1) {
                os.write(buffer, 0, b);
            }
        } catch (IOException e) {
            log.error("读取头像失败: " + e.getMessage());
        }
    }

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
        if (SecurityContextHolder.getContext().getAuthentication().getPrincipal() != null) {
            hasFollowed = followService.hasFollowed(((User) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId(),
                    CommunityEnum.ENTITY_TYPE_USER.getCode(), userId.intValue());
        }
        model.addAttribute("hasFollowed", hasFollowed);
        return "/site/profile";
    }
}
