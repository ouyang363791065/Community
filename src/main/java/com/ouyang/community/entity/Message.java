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
    private String conversationId;
    private String content;
    private Integer status;
}
