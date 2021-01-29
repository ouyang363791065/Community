package com.ouyang.community.kafka;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @Author feixi
 * @Description
 * @Date 2020/4/24 14:56
 **/
@Component
@Slf4j
public class KafkaProducerService {
    @Resource
    private KafkaTemplate<String, String> kafkaTemplate;

    /**
     * 发送kafka消息
     *
     * @param data 要发送的字符串内容
     */
    public void send(String topic, String data) {
        try {
            kafkaTemplate.send(topic, data);
        } catch (Exception e) {
            log.error("kafkaTemplate调用send出现异常:" + e.getMessage(), e);
        }
    }
}
