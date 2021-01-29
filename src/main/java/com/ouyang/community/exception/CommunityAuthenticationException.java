package com.ouyang.community.exception;

import org.springframework.security.core.AuthenticationException;

/**
 * @Description: 继承Spring Security的AuthenticationException, 登录过程中的异常可被FailureHandler捕获
 * @Author: feixi
 * @Date: 2021/1/26 15:14
 */
public class CommunityAuthenticationException extends AuthenticationException {
    public CommunityAuthenticationException(String msg) {
        super(msg);
    }
}
