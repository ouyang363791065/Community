package com.ouyang.community.kafka;

import com.alibaba.fastjson.JSONObject;
import com.ouyang.community.entity.Event;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * @author feixi
 * @Description 事件生产者
 */
@Component
public class EventProducer {
    @Autowired
    private KafkaTemplate<String,String> kafkaTemplate;

    // 处理事件
    public void fireEvent(Event event){
        // 将事件发送到指定主题，其中把内容转换为json对象，消费者受到json后，可以将json转换成Event
        kafkaTemplate.send(event.getTopic(), JSONObject.toJSONString(event));
    }
}
