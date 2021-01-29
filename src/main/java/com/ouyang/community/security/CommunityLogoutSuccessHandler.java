package com.ouyang.community.security;

import com.alibaba.fastjson.JSON;
import com.ouyang.community.entity.User;
import com.ouyang.community.service.UserService;
import com.ouyang.community.utils.CookieUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @Description:
 * @Author: feixi
 * @Date: 2021/1/26 14:14
 */
@Slf4j
@Component
public class CommunityLogoutSuccessHandler implements LogoutSuccessHandler {
    @Autowired
    private UserService userService;

    @Override
    public void onLogoutSuccess(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Authentication authentication) throws IOException, ServletException {
        String ticket = CookieUtil.getValue(httpServletRequest, "ticket");
        userService.logout(ticket);
        User user = (User) authentication.getPrincipal();
        SecurityContextHolder.clearContext();
        log.info("Community Logout Success: " + JSON.toJSONString(user));
        httpServletResponse.sendRedirect(httpServletRequest.getContextPath() + "/index");
    }
}
