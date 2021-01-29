package com.ouyang.community.security;

import com.ouyang.community.http.HttpStatusCode;
import com.ouyang.community.http.HttpUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @Description: 未登入访问
 * @Author: feixi
 * @Date: 2021/1/21 17:47
 */
@Slf4j
@Component
public class CommunityAuthenticationEntryPoint implements AuthenticationEntryPoint {
    @Override
    public void commence(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, AuthenticationException e) throws IOException, ServletException {
        String xRequestedWith = httpServletRequest.getHeader("x-requested-with");
        // 判断是普通请求还是异步请求，进行不同的处理
        if ("XMLHttpRequest".equals(xRequestedWith)) {
            // 异步请求，返回json
            log.info("you are not login");
            httpServletResponse.setContentType("text/html;charset=UTF-8");
            httpServletResponse.getWriter().write(HttpUtil.buildResult(HttpStatusCode.NOT_LOGIN).toString());
        } else {
            // 同步请求
            httpServletResponse.sendRedirect(httpServletRequest.getContextPath() + "/login");
        }
    }
}
