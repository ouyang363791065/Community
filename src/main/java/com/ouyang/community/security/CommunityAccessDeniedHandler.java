package com.ouyang.community.security;

import com.ouyang.community.http.HttpStatusCode;
import com.ouyang.community.http.HttpUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @Description:
 * @Author: feixi
 * @Date: 2021/1/21 19:27
 */
@Slf4j
@Component
public class CommunityAccessDeniedHandler implements AccessDeniedHandler {
    @Override
    public void handle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, AccessDeniedException e) throws IOException, ServletException {
        String xRequestedWith = httpServletRequest.getHeader("x-requested-with");
        if ("XMLHttpRequest".equals(xRequestedWith)) {
            httpServletResponse.setContentType("application/plain;charset=utf-8");
            log.info("Access has been Denied");
            httpServletResponse.setContentType("text/html;charset=UTF-8");
            httpServletResponse.getWriter().write(HttpUtil.buildResult(HttpStatusCode.USER_NO_AUTHORITY).toString());
        } else {
            httpServletResponse.sendRedirect(httpServletRequest.getContextPath() + "/denied");
        }
    }
}
