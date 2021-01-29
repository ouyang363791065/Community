package com.ouyang.community;

import com.ouyang.community.mapper.LogoImageMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @Description:
 * @Author: feixi
 * @Date: 2020/11/9 20:51
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class RedisConfigTest implements ApplicationContextAware {

    private ApplicationContext applicationContext;

    private RedisTemplate<String, Object> redisTemplate;

    @Test
    public void redisTemplate() {
        String redisKey = "key";
        redisTemplate.opsForValue().set(redisKey, 1);
        System.out.println(redisTemplate.opsForValue().get(redisKey));
        redisTemplate.opsForValue().increment(redisKey, 1);
        System.out.println(redisTemplate.opsForValue().get(redisKey));
    }
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
        this.redisTemplate = (RedisTemplate<String, Object>) applicationContext.getBean("redisTemplate");
    }
}