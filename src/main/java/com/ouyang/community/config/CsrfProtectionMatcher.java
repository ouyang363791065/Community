package com.ouyang.community.config;

import org.apache.commons.lang3.StringUtils;
import org.springframework.security.web.util.matcher.RequestMatcher;

import javax.servlet.http.HttpServletRequest;
import java.util.HashSet;

/**
 * @Description:
 * @Author: feixi
 * @Date: 2021/1/26 10:48
 */
public class CsrfProtectionMatcher implements RequestMatcher {
    private HashSet<String> allowedMethods;

    @Override
    public boolean matches(HttpServletRequest request) {
        String token = request.getHeader("x-auth-token");
        if (StringUtils.isNotBlank(token)) {
            //固定token方式不需要
            return false;
        }
        return !this.allowedMethods.contains(request.getMethod());
    }
}