package com.ouyang.community.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author feixi
 * @Description 评论实体类
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class Comment extends EntityBase {
    /**
     * 评论用户的id
     */
    private Long userId;
    private Integer entityType;
    private Integer entityId;
    /**
     * 实体类的用户的id
     */
    private Integer targetId;
    private String content;
    private Integer status;
}
