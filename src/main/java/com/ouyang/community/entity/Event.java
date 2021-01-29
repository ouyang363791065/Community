package com.ouyang.community.entity;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * @author feixi
 * @Description 事件，用于消息队列
 */
@Getter
public class Event {
    private String topic;
    private Long userId;
    private Integer entityType;
    private Integer entityId;

    /**
     * 实体的作者
     */
    private Integer entityUserId;

    /**
     * 把其他额外的数据，存入map中，具有扩展性
     * 目的：为了后期动态扩展，有些字段没有办法做出预判
     */
    private Map<String, Object> data = new HashMap<>();

    /**
     * 此处这样设计，是为了更灵活的设置属性，避免使用多个构造函数。
     * 这样设计很灵活和方便
     */
    public Event setTopic(String topic) {
        this.topic = topic;
        return this;
    }

    public Event setUserId(Long userId) {
        this.userId = userId;
        return this;
    }

    public Event setEntityType(int entityType) {
        this.entityType = entityType;
        return this;
    }

    public Event setEntityId(int entityId) {
        this.entityId = entityId;
        return this;
    }

    public Event setEntityUserId(int entityUserId) {
        this.entityUserId = entityUserId;
        return this;
    }

    public Event setData(String key, Object value) {
        this.data.put(key, value);
        return this;
    }
}
