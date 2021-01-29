package com.ouyang.community.security;

import com.alibaba.fastjson.JSON;
import com.ouyang.community.entity.User;
import com.ouyang.community.enums.CommunityEnum;
import com.ouyang.community.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

/**
 * @Description:
 * @Author: feixi
 * @Date: 2021/1/22 14:57
 */
@Slf4j
@Component
public class CommunityAuthenticationSuccessHandler implements AuthenticationSuccessHandler {
    @Autowired
    private UserService userService;
    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Authentication authentication) throws IOException, ServletException {
        httpServletResponse.setContentType("application/json;charset=UTF-8");
        User user = (User) authentication.getPrincipal();
        log.info("Community Login Success: " + JSON.toJSONString(user));
        // 检查账号，密码，设置过期时间
        int expiredSeconds = "true".equals(httpServletRequest.getParameter("rememberme"))
                ? CommunityEnum.REMEMBER_EXPIRED_SECONDS.getCode() : CommunityEnum.DEFAULT_EXPIRED_SECONDS.getCode();
        Map<String, Object> map = userService.login(user, expiredSeconds);
        Cookie cookie = new Cookie("ticket", map.get("ticket").toString());
        cookie.setPath(contextPath);
        cookie.setMaxAge(expiredSeconds);
        httpServletResponse.addCookie(cookie);
        httpServletResponse.sendRedirect(httpServletRequest.getContextPath() + "/index");
    }
}
