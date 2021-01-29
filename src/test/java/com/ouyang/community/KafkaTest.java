package com.ouyang.community;

import com.ouyang.community.kafka.KafkaProducerService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @Description:
 * @Author: feixi
 * @Date: 2021/1/29 11:04
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class KafkaTest implements ApplicationContextAware {
    @Autowired
    private KafkaProducerService kafkaProducerService;
    private ApplicationContext applicationContext;

    @Test
    public void test(){
        kafkaProducerService.send("spring.kafka.topic.test","刀哥--刀刀涨");
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
