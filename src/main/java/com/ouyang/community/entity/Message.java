package com.ouyang.community.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author feixi
 * @Description 私信实体类
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class Message extends EntityBase {
    private Integer fromId;
    private Integer toId;
    /**
     * 存通知对应的主题
     */
    private String conversationId;
    private String content;
    private Integer status;
}
