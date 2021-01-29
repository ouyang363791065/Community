package com.ouyang.community.service;

import com.ouyang.community.entity.User;

import java.util.Map;

/**
 * @Description: IBaseService, IService, ServiceImpl
 * @Author: feixi
 * @Date: 2020/11/5 20:23
 */
public interface UserService extends IBaseService<User> {
    Integer activation(Integer userId, String code);

    Map<String, Object> register(User user);

    Map<String, Object> login(User user, int expiredSeconds);

    void logout(String ticket);

    void updateHeader(Long id, String headerUrl);
}
