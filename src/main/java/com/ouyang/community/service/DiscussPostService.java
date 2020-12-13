package com.ouyang.community.service;

import com.ouyang.community.entity.DiscussPost;

import java.util.List;

/**
 * @Description:
 * @Author: feixi
 * @Date: 2020/11/25 11:04
 */
public interface DiscussPostService extends IBaseService<DiscussPost> {
    Integer updateCommentCount(Long id, Integer commentCount);

    Integer addDiscussPost(DiscussPost post);

    DiscussPost findDiscussPostById(Long id);

    Integer updateType(Long id, int type);

    Integer updateStatus(Long id, int status);

    Integer updateScore(Long id, double score);

    void init();

    List<DiscussPost> findDiscussPosts(Integer userId, Integer offset, Integer limit, Integer orderMode);

    Integer findDiscussPostRows(Integer userId);
}
