package com.ouyang.community.service.impl;

import com.ouyang.community.entity.Comment;
import com.ouyang.community.enums.CommunityEnum;
import com.ouyang.community.mapper.CommentMapper;
import com.ouyang.community.service.CommentService;
import com.ouyang.community.service.DiscussPostService;
import com.ouyang.community.utils.Constant;
import com.ouyang.community.filter.SensitiveFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

/**
 * @Description:
 * @Author: feixi
 * @Date: 2020/12/7 20:14
 */
@Service
public class CommentServiceImpl implements CommentService {
    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private SensitiveFilter sensitiveFilter;

    @Autowired
    private DiscussPostService discussPostService;

    @Override
    public List<Comment> findCommentsByEntity(Integer entityType, Integer entityId, Integer offset, Integer limit) {
        return commentMapper.selectCommentsByEntity(entityType, entityId, offset, limit);
    }

    @Override
    public Integer findCommentCount(Integer entityType, Integer entityId) {
        return commentMapper.selectCountByEntity(entityType, entityId);
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED,rollbackFor = Throwable.class)
    public Integer addComment(Comment comment) {
        if (comment == null) {
            throw new IllegalArgumentException("参数不能为空!");
        }

        // 添加评论
        comment.setContent(HtmlUtils.htmlEscape(comment.getContent()));
        comment.setContent(sensitiveFilter.filter(comment.getContent()));
        int rows = commentMapper.insertComment(comment);

        // 更新帖子评论数量
        if (CommunityEnum.ENTITY_TYPE_POST.getCode().equals(comment.getEntityType())) {
            int count = commentMapper.selectCountByEntity(comment.getEntityType(), comment.getEntityId());
            discussPostService.updateCommentCount(new Long(comment.getEntityId()), count);
        }
        return rows;
    }

    @Override
    public Comment findCommentById(Integer id) {
        return commentMapper.selectCommentById(id);
    }
}
