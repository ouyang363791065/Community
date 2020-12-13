package com.ouyang.community.mapper;

import com.ouyang.community.entity.Comment;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @Description:
 * @Author: feixi
 * @Date: 2020/11/18 14:38
 */
@Mapper
public interface CommentMapper {
    List<Comment> selectCommentsByEntity(Integer entityType, Integer entityId, Integer offset, Integer limit);

    Integer selectCountByEntity(Integer entityType, Integer entityId);

    Integer insertComment(Comment comment);

    Comment selectCommentById(Integer id);
}
