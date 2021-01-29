package com.ouyang.community.interceptor;

import com.ouyang.community.entity.LoginTicket;
import com.ouyang.community.entity.User;
import com.ouyang.community.service.LoginTicketService;
import com.ouyang.community.service.UserService;
import com.ouyang.community.utils.CookieUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

@Component
public class LoginTicketInterceptor implements HandlerInterceptor {
    @Autowired
    private LoginTicketService loginTicketService;
    @Autowired
    private UserService userService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 从cookie中获取凭证，目前已废弃，使用Security的认证
        String ticket = CookieUtil.getValue(request, "ticket");
        if (ticket != null) {
            // 从redis里面查询凭证
            LoginTicket loginTicket = loginTicketService.findLoginTicketFromRedis(ticket);
            // 检查凭证是否有效
            if (loginTicket != null && loginTicket.getStatus() == 0 && loginTicket.getExpired().after(new Date())
                    && !(SecurityContextHolder.getContext().getAuthentication().getPrincipal() instanceof User)) {
                // 根据凭证查询用户
                User user = userService.getById(loginTicket.getUserId());
                // 在本次请求中持有用户，废弃使用hostHolder存放当前用户
                // hostHolder.setUser(user);
                // 构建用户认证的结果，将用户存入SecurityContext上下文，以便于Security进行授权。
                // principal: 当前用户; credentials: 证书; authorities: 权限;
                Authentication authentication =
                        new UsernamePasswordAuthenticationToken(user, user.getPassword(), user.getAuthorities());
                // 存入SecurityContext
                SecurityContextHolder.setContext(new SecurityContextImpl(authentication));
            }
        }
        return true;
    }

    /**
     * 在模板引擎之前使用
     *
     * @param request
     * @param response
     * @param handler
     * @param modelAndView
     * @throws Exception
     */
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            // 匿名访问
            if (SecurityContextHolder.getContext().getAuthentication().getPrincipal() instanceof String) {
                System.out.println(SecurityContextHolder.getContext().getAuthentication().getPrincipal());
            }
            // 已认证
            if (SecurityContextHolder.getContext().getAuthentication().getPrincipal() instanceof User && modelAndView != null) {
                modelAndView.addObject("loginUser", SecurityContextHolder.getContext().getAuthentication().getPrincipal());
            }
        }
    }
}
