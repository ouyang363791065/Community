package com.ouyang.community.filter;

import com.ouyang.community.exception.CommunityAuthenticationException;
import com.ouyang.community.http.HttpUtil;
import com.ouyang.community.utils.CookieUtil;
import com.ouyang.community.utils.RedisKeyUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @Description:
 * @Author: feixi
 * @Date: 2021/1/22 17:24
 */
@Slf4j
@Component
public class ValidateCodeFilter extends OncePerRequestFilter implements InitializingBean {
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    @Autowired
    private AuthenticationFailureHandler authenticationFailureHandler;

    @Override
    protected void doFilterInternal(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, FilterChain filterChain) throws ServletException, IOException {
        // 假如是登入请求，才进行校验验证码
        if ("post".equalsIgnoreCase(httpServletRequest.getMethod()) && httpServletRequest.getServletPath().equals("/login")) {
            // 从cookie中取出验证码的UUID，去查redis中的值
            String kaptcha = null;
            String kaptchaOwner = CookieUtil.getValue(httpServletRequest, "kaptchaOwner");
            if (StringUtils.isNotBlank(kaptchaOwner)) {
                String kaptchaKey = RedisKeyUtil.getKaptchaKey(kaptchaOwner);
                kaptcha = (String) redisTemplate.opsForValue().get(kaptchaKey);
            }

            // 校验验证码
            String code = httpServletRequest.getParameter("code");
            if (StringUtils.isBlank(kaptcha) || StringUtils.isBlank(code) || !kaptcha.equalsIgnoreCase(code)) {
                log.info("验证码输入有误");
                // 手动调用失败处理器，走登入失败的handler
                authenticationFailureHandler.onAuthenticationFailure(httpServletRequest, httpServletResponse,
                        new CommunityAuthenticationException("验证码输入有误"));
                return;
            }
        }
        filterChain.doFilter(httpServletRequest, httpServletResponse);
    }
}
