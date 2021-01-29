package com.ouyang.community.config;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.ouyang.community.entity.User;
import com.ouyang.community.security.*;
import com.ouyang.community.service.LoginTicketService;
import com.ouyang.community.service.UserService;
import com.ouyang.community.utils.CommunityUtil;
import com.ouyang.community.utils.Constant;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.HashSet;

/**
 * @Description:
 * @Author: feixi
 * @Date: 2021/1/21 15:44
 */
@Configuration
public class BrowserSecurityAdapter extends WebSecurityConfigurerAdapter {
    @Autowired
    private HttpServletRequest httpServletRequest;
    @Autowired
    private LoginTicketService loginTicketService;
    @Autowired
    private UserService userService;
    @Autowired
    private ValidateCodeSecurityConfig validateCodeSecurityConfig;
    @Autowired
    private CommunityAuthenticationSuccessHandler successHandler;
    @Autowired
    private CommunityAuthenticationFailureHandler failureHandler;
    @Autowired
    private CommunityLogoutSuccessHandler logoutSuccessHandler;
    @Value("${server.servlet.context-path}")
    private String contextPath;

    // AuthenticationManager: 认证的核心接口.
    // AuthenticationManagerBuilder: 用于构建AuthenticationManager对象的工具.
    // ProviderManager: AuthenticationManager接口的默认实现类.
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        // 内置的认证规则
        // 12345 为盐值，spring security会根据userDetailService和passwordEncoder对用户名密码进行认证
        // auth.userDetailsService(userService).passwordEncoder(new Pbkdf2PasswordEncoder("12345"));

        // 自定义认证规则
        // AuthenticationProvider: ProviderManager持有一组AuthenticationProvider,每个AuthenticationProvider负责一种认证.
        // 委托模式: ProviderManager将认证委托给AuthenticationProvider.
        auth.authenticationProvider(new AuthenticationProvider() {
            // Authentication: 用于封装认证信息的接口,不同的实现类代表不同类型的认证信息.
            @Override
            public Authentication authenticate(Authentication authentication) throws AuthenticationException {
                String username = authentication.getName();
                String password = (String) authentication.getCredentials();

                // 空值处理
                if (StringUtils.isBlank(username)) {
                    throw new UsernameNotFoundException("账号不能为空!");
                }
                if (StringUtils.isBlank(password)) {
                    throw new BadCredentialsException("密码不能为空!");
                }

                // 验证账号
                User user = userService.getOne(new QueryWrapper<User>().lambda().eq(User::getUsername, username));
                if (user == null) {
                    throw new UsernameNotFoundException("账号不存在!");
                }

                // 验证密码
                password = CommunityUtil.md5(password + user.getSalt());
                if (!user.getPassword().equals(password)) {
                    throw new BadCredentialsException("密码不正确!");
                }

                // 验证状态
                if (user.getStatus() == 0) {
                    throw new LockedException("该账号未激活!");
                }

                // principal: 主要信息; credentials: 证书; authorities: 权限;
                return new UsernamePasswordAuthenticationToken(user, user.getPassword(), user.getAuthorities());
            }

            // 当前的AuthenticationProvider支持哪种类型的认证.
            @Override
            public boolean supports(Class<?> aClass) {
                // UsernamePasswordAuthenticationToken: Authentication接口的常用的实现类.
                return UsernamePasswordAuthenticationToken.class.equals(aClass);
            }
        });
    }

    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().antMatchers("/resource/**");
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // 认证
        http.formLogin()
                .loginPage("/loginPage")  // 用户未登录时，访问任何资源都转跳到该路径，即登录页面
                .loginProcessingUrl("/login")  // 登录表单form中action的地址，也就是处理认证请求的路径
                .successHandler(successHandler)  // 认证成功的回调
                .failureHandler(failureHandler);  // 认证失败的回调

        // 授权
        http.authorizeRequests()
                .antMatchers(Constant.UNLOGIN_PERMIT_URL.toArray(new String[0]))  // 匹配请求
                .hasAnyAuthority(Constant.AUTHORITY_ADMIN, Constant.AUTHORITY_USER, Constant.AUTHORITY_MODERATOR)  // 拥有的权限
                .antMatchers("/discuss/top", "/discuss/wonderful")  // 匹配请求
                .hasAnyAuthority(Constant.AUTHORITY_MODERATOR)  // 拥有的权限->版主可以置顶加精
                .antMatchers("/discuss/delete", "/data/**", "/actuator/**")  // 匹配请求
                .hasAnyAuthority(Constant.AUTHORITY_ADMIN)  // 拥有的权限->管理员可以删除
                .anyRequest().permitAll();

        // 禁用csrf
         http.csrf().disable();
        // TODO:CSRF开启
//        CookieCsrfTokenRepository repository = CookieCsrfTokenRepository.withHttpOnlyFalse();
//        repository.setCookiePath("/");
//        repository.setCookieName("XSRF-TOKEN");
//        repository.setHeaderName("X-XSRF-TOKEN");
//        //CSRF配置项过滤地址
//        http.csrf().ignoringAntMatchers(StringUtils.splitByWholeSeparatorPreserveAllTokens("/like", ","));
//        http.csrf().csrfTokenRepository(repository).requireCsrfProtectionMatcher(csrfProtectionMatcher());

        // 权限不够时的处理
        http.exceptionHandling()
                .accessDeniedHandler(new CommunityAccessDeniedHandler())  // 权限不够时的处理
                .authenticationEntryPoint(new CommunityAuthenticationEntryPoint());  // 未登入访问的处理

        // 校验验证码，这里实现了一个自定义过滤器，放在了用户名密码校验之前进行过滤，如果不通过则无法进入后面的过滤器
        http.apply(validateCodeSecurityConfig);

        // Security 底层会默认拦截退出请求
        // Security底层默认会拦截/logout请求,进行退出处理.
        // 覆盖它默认的逻辑,才能执行我们自己的退出代码.
        // 此处为一个欺骗，程序中没有"/securitylogout"，拦截到这个路径不会处理
        http.logout()
                .logoutUrl("/logout")
                .logoutSuccessHandler(logoutSuccessHandler);
    }

    /**
     * CSRF排除的HTTP方法
     */
//    @Bean
//    CsrfProtectionMatcher csrfProtectionMatcher() {
//        CsrfProtectionMatcher matcher = new CsrfProtectionMatcher();
//        //设置允许的方法
//        matcher.(new HashSet<>(Arrays.asList("GET,HEAD,TRACE,OPTIONS".split(","))));
//        return matcher;
//    }
}
