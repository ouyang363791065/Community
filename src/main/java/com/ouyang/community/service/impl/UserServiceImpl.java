package com.ouyang.community.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.ouyang.community.entity.LoginTicket;
import com.ouyang.community.entity.User;
import com.ouyang.community.enums.CommunityEnum;
import com.ouyang.community.exception.CommunityException;
import com.ouyang.community.http.HttpStatusCode;
import com.ouyang.community.mapper.UserMapper;
import com.ouyang.community.service.UserService;
import com.ouyang.community.utils.CommunityUtil;
import com.ouyang.community.utils.Constant;
import com.ouyang.community.utils.MailClient;
import com.ouyang.community.utils.RedisKeyUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.*;

/**
 * @Description:
 * @Author: feixi
 * @Date: 2020/11/5 20:22
 */
@Service
public class UserServiceImpl extends IBaseServiceImpl<UserMapper, User> implements UserService {
    @Autowired
    private MailClient mailClient;
    @Autowired
    private TemplateEngine templateEngine;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    @Value("${community.path.domain}")
    private String domain;
    @Value("${server.servlet.context-path}")
    private String contextPath;
    @Autowired
    private UserMapper userMapper;

    @Override
    public Map<String, Object> register(User user) {
        Map<String, Object> map = new HashMap<>();
        if (Objects.isNull(user)) {
            throw new IllegalArgumentException("参数不能为空!");
        }
        if (StringUtils.isBlank(user.getUsername())) {
            map.put("usernameMsg", "账号不能为空!");
            return map;
        }
        if (StringUtils.isBlank(user.getPassword())) {
            map.put("passwordMsg", "密码不能为空!");
            return map;
        }
        if (StringUtils.isBlank(user.getEmail())) {
            map.put("emailMsg", "邮箱不能为空!");
            return map;
        }
        // 验证账号
        if (Objects.nonNull(baseMapper.selectOne(new QueryWrapper<User>().lambda().eq(User::getUsername, user.getUsername())))) {
            map.put("usernameMsg", "该账号已存在!");
            return map;
        }
        // 验证邮箱
        if (Objects.nonNull(baseMapper.selectOne(new QueryWrapper<User>().lambda().eq(User::getEmail, user.getEmail())))) {
            map.put("emailMsg", "该邮箱已被注册!");
            return map;
        }
        // 注册用户，补全用户的其他信息
        user.setSalt(CommunityUtil.generateUUID().substring(0, 5));
        user.setPassword(CommunityUtil.md5(user.getPassword() + user.getSalt()));
        user.setType(0);
        user.setStatus(0);
        user.setActivationCode(CommunityUtil.generateUUID());
        user.setHeaderUrl(String.format("http://images.nowcoder.com/head/%dt.png", new Random().nextInt(1000)));
        user.setCreateTime(System.currentTimeMillis());
        baseMapper.insert(user);
        // 激活邮件
        Context context = new Context();
        context.setVariable("email", user.getEmail());
        // http://localhost:8080/community/activation/101/code
        String url = domain + contextPath + "/activation/" + user.getId() + "/" + user.getActivationCode();
        context.setVariable("url", url);
        map.put("url", url);
        // 使用模板引擎，渲染/mail/activation页面
        String content = templateEngine.process("/mail/activation", context);
        mailClient.sendMail(user.getEmail(), "激活账号", content);
        return map;
    }

    @Override
    public Map<String, Object> login(User user, int expiredSeconds) {
        Map<String, Object> map = new HashMap<>();
        // 生成登录凭证
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(user.getId());
        loginTicket.setTicket(CommunityUtil.generateUUID());
        loginTicket.setStatus(Constant.LOGIN);
        loginTicket.setExpired(new Date(System.currentTimeMillis() + expiredSeconds * 1000));
        String ticketKey = RedisKeyUtil.getTicketKey(loginTicket.getTicket());
        redisTemplate.opsForValue().set(ticketKey, loginTicket);
        map.put("ticket", loginTicket.getTicket());
        return map;
    }

    @Override
    public Integer activation(Integer userId, String code) {
        User user = baseMapper.selectById(userId);
        if (CommunityEnum.ALREADY_ACTIVATION.getCode().equals(user.getStatus())) {
            return CommunityEnum.ACTIVATION_REPEAT.getCode();
        } else if (CommunityEnum.NOT_ACTIVATION.getCode().equals(user.getStatus())) {
            UpdateWrapper<User> updateWrapper = new UpdateWrapper<>();
            updateWrapper.lambda().set(User::getStatus, 1).eq(User::getId, userId);
            baseMapper.update(user, updateWrapper);
            return CommunityEnum.ACTIVATION_SUCCESS.getCode();
        } else {
            return CommunityEnum.ACTIVATION_FAILURE.getCode();
        }
    }

    public void logout(String ticket) {
        // 使用redis，更改ticket状态。不使用删除，为了后期进行登录相关的统计
        String ticketKey = RedisKeyUtil.getTicketKey(ticket);
        LoginTicket loginTicket = (LoginTicket) redisTemplate.opsForValue().get(ticketKey);
        if (Objects.isNull(loginTicket)) {
            throw new CommunityException(HttpStatusCode.LOGOUT_ERROR);
        }
        loginTicket.setStatus(Constant.LOGOUT);
        redisTemplate.opsForValue().set(ticketKey, loginTicket);
    }

    @Override
    public void updateHeader(Long id, String headerUrl) {
        // 先去更新，再去清理缓存。可以避免更新数据库失败，缓存消失的现象。现在：数据库更新失败，缓存不会清除
        User user = new User();
        user.setId(id);
        userMapper.updateById(new User() {{
            setId(id);
            setHeaderUrl(headerUrl);
        }});
        clearCache(id);
    }

    // 更新用户信息，清除缓存数据
    public void clearCache(Long userId) {
        String userKey = RedisKeyUtil.getUserKey(userId);
        redisTemplate.delete(userKey);
    }
}
