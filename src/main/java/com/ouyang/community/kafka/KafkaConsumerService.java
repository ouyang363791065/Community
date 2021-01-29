package com.ouyang.community.kafka;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * @Author feixi
 * @Description
 * @Date 2020/4/24 14:56
 **/
@Component
@Slf4j
public class KafkaConsumerService {
    /**
     * 监听主题，有消息时使用该方法进行处理
     *
     * @param consumerRecord
     */
    @KafkaListener(topics = "spring.kafka.topic.test", groupId = "${spring.kafka.consumer.group-id}")
    public void feedbackData(ConsumerRecord<String,String> consumerRecord) {
        Object value = consumerRecord.value();
        if (log.isInfoEnabled()) {
            log.info("feedbackLog:offset {}, value {}", consumerRecord.offset(), consumerRecord.value());
        }

        if (null == value) {
            log.error("kafka消费feedbackData数据为空");
        }
    }
}
