package com.ouyang.community.service;

import com.ouyang.community.entity.Comment;

import java.util.List;

/**
 * @Description:
 * @Author: feixi
 * @Date: 2020/12/7 20:15
 */
public interface CommentService {
    /**
     * 根据实体查找评论，帖子/用户
     *
     * @param entityType
     * @param entityId
     * @param offset
     * @param limit
     * @return
     */
    List<Comment> findCommentsByEntity(Integer entityType, Integer entityId, Integer offset, Integer limit);

    /**
     * 查找某个实体评论总数
     *
     * @param entityType
     * @param entityId
     * @return
     */
    Integer findCommentCount(Integer entityType, Integer entityId);

    /**
     * 创建评论
     *
     * @param comment
     * @return
     */
    Integer addComment(Comment comment);

    /**
     * 根据id查找评论
     *
     * @param id
     * @return
     */
    Comment findCommentById(Integer id);
}
