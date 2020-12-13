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
    private Integer userId;
    private Integer entityType;
    private Integer entityId;
    private Integer targetId;
    private String content;
    private Integer status;
}
