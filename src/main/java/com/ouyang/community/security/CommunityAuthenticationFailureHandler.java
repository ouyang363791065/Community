package com.ouyang.community.security;

import com.ouyang.community.exception.CommunityAuthenticationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @Description:
 * @Author: feixi
 * @Date: 2021/1/22 15:01
 */
@Slf4j
@Component
public class CommunityAuthenticationFailureHandler implements AuthenticationFailureHandler {
    @Override
    public void onAuthenticationFailure(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, AuthenticationException e) throws IOException, ServletException {
        String username = httpServletRequest.getParameter("username");
        String password = httpServletRequest.getParameter("password");
        log.info("Community账户登陆失败, {} / {}", username, password);

        // 用户名部分
        if (e instanceof UsernameNotFoundException) {
            httpServletRequest.setAttribute("usernameMsg", e.getMessage());
            httpServletRequest.getRequestDispatcher("/loginPage").forward(httpServletRequest, httpServletResponse);
            return;
        }

        // 密码部分
        if (e instanceof BadCredentialsException) {
            httpServletRequest.setAttribute("passwordMsg", e.getMessage());
            httpServletRequest.getRequestDispatcher("/loginPage").forward(httpServletRequest, httpServletResponse);
            return;
        }

        // 账号部分
        if (e instanceof LockedException) {
            httpServletRequest.setAttribute("usernameMsg", e.getMessage());
            httpServletRequest.getRequestDispatcher("/loginPage").forward(httpServletRequest, httpServletResponse);
            return;
        }

        // 验证码部分
        if (e instanceof CommunityAuthenticationException) {
            httpServletRequest.setAttribute("codeMsg", e.getMessage());
            httpServletRequest.getRequestDispatcher("/loginPage").forward(httpServletRequest, httpServletResponse);
        }
    }
}
