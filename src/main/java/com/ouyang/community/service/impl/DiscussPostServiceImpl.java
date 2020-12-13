package com.ouyang.community.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.ouyang.community.entity.DiscussPost;
import com.ouyang.community.mapper.DiscussPostMapper;
import com.ouyang.community.service.DiscussPostService;
import com.ouyang.community.filter.SensitiveFilter;
import lombok.extern.slf4j.Slf4j;
import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.LoadingCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * @Description:
 * @Author: feixi
 * @Date: 2020/11/25 11:04
 */
@Slf4j
@Service
public class DiscussPostServiceImpl extends IBaseServiceImpl<DiscussPostMapper, DiscussPost> implements DiscussPostService {
    @Autowired
    private SensitiveFilter sensitiveFilter;

    @Value("${caffeine.posts.max-size}")
    private int maxSize;

    @Value("${caffeine.posts.expire-seconds}")
    private int expireSeconds;

    // Caffeine核心接口: Cache, LoadingCache, AsyncLoadingCache

    // 帖子列表缓存
    private LoadingCache<String, List<DiscussPost>> postListCache;

    // 帖子总数缓存
    private LoadingCache<Integer, Integer> postRowsCache;

    // PostConstruct在构造函数之后执行，init方法之前执行
    @Override
    @PostConstruct
    public void init() {
        // 初始化帖子列表缓存
        postListCache = Caffeine.newBuilder()
                .maximumSize(maxSize)
                .expireAfterWrite(expireSeconds, TimeUnit.SECONDS)
                .build((CacheLoader<String, List<DiscussPost>>) key -> {
                    if (key.length() == 0) {
                        throw new IllegalArgumentException("参数错误!");
                    }
                    String[] params = key.split(":");
                    if (params.length != 2) {
                        throw new IllegalArgumentException("参数错误!");
                    }
                    int offset = Integer.parseInt(params[0]);
                    int limit = Integer.parseInt(params[1]);
                    // TODO:二级缓存：redis->mysql
                    log.debug("load post list from DB.");
                    /**
                     * <select id="selectDiscussPosts" resultType="DiscussPost">
                     *     select <include refid="selectFields"></include>
                     *     from discuss_post
                     *     where status != 2
                     *     <if test="orderMode==1">
                     *         order by type desc, score desc, create_time desc
                     *     </if>
                     *     limit #{offset}, #{limit}
                     * </select>
                     */
                    QueryWrapper<DiscussPost> queryWrapper = new QueryWrapper<>();
                    queryWrapper.lambda()
                            .ne(DiscussPost::getStatus, 2)
                            .orderByDesc(DiscussPost::getType, DiscussPost::getScore, DiscussPost::getCreateTime)
                            .last("limit " + offset + " , " + limit);
                    return baseMapper.selectList(queryWrapper);
                });

        // 初始化帖子总数缓存
        postRowsCache = Caffeine.newBuilder()
                .maximumSize(maxSize)
                .expireAfterWrite(expireSeconds, TimeUnit.SECONDS)
                .build((CacheLoader<Integer, Integer>) key -> {
                    // TODO:二级缓存：redis->mysql
                    log.debug("load post list from DB.");
                    /**
                     * <select id="selectDiscussPostRows" resultType="int">
                     *     select count(id)
                     *     from discuss_post
                     *     where status != 2
                     *     <if test="userId!=0">
                     *         and user_id = #{userId}
                     *     </if>
                     * </select>
                     */
                    QueryWrapper<DiscussPost> queryWrapper = new QueryWrapper<>();
                    if (key != 0) {
                        queryWrapper.lambda().ne(DiscussPost::getStatus, 2).eq(DiscussPost::getUserId, key);
                    } else {
                        queryWrapper.lambda().ne(DiscussPost::getStatus, 2);
                    }
                    return baseMapper.selectCount(queryWrapper);
                });
    }

    @Override
    public List<DiscussPost> findDiscussPosts(Integer userId, Integer offset, Integer limit, Integer orderMode) {
        // 只有热门帖子(访问首页时，userId=0)
        if (userId == 0 && orderMode == 1) {
            return postListCache.get(offset + ":" + limit);
        }
        log.debug("load post list from DB.");
        /**
         * select <include refid="selectFields"></include>
         *     from discuss_post
         *     where status != 2
         *     <if test="userId!=0">
         *         and user_id = #{userId}
         *     </if>
         *     <if test="orderMode==0">
         *         order by type desc, create_time desc
         *     </if>
         *     <if test="orderMode==1">
         *         order by type desc, score desc, create_time desc
         *     </if>
         *     limit #{offset}, #{limit}
         */
        //return discussPostMapper.selectDiscussPosts(userId, offset, limit, orderMode);
        return null;
    }

    @Override
    public Integer findDiscussPostRows(Integer userId) {
        // 首页查询走缓存
        if (userId == 0) {
            return postRowsCache.get(userId);
        }
        log.debug("load post list from DB.");
        //return baseMapper.selectDiscussPostRows(userId);
        return null;
    }

    @Override
    public Integer addDiscussPost(DiscussPost post) {
        if (Objects.isNull(post)) {
            throw new IllegalArgumentException("参数不能为空!");
        }
        // 转义HTML标记
        post.setTitle(HtmlUtils.htmlEscape(post.getTitle()));
        post.setContent(HtmlUtils.htmlEscape(post.getContent()));
        // 过滤敏感词
        post.setTitle(sensitiveFilter.filter(post.getTitle()));
        post.setContent(sensitiveFilter.filter(post.getContent()));
        return baseMapper.insert(post);
    }

    @Override
    public DiscussPost findDiscussPostById(Long id) {
        return baseMapper.selectById(id);
    }

    @Override
    public Integer updateCommentCount(Long id, Integer commentCount) {
        DiscussPost discussPost = new DiscussPost();
        discussPost.setId(id);
        discussPost.setCommentCount(commentCount);
        return baseMapper.updateById(discussPost);
    }

    @Override
    public Integer updateType(Long id, int type) {
        DiscussPost discussPost = new DiscussPost();
        discussPost.setId(id);
        discussPost.setType(type);
        return baseMapper.updateById(discussPost);
    }

    @Override
    public Integer updateStatus(Long id, int status) {
        DiscussPost discussPost = new DiscussPost();
        discussPost.setId(id);
        discussPost.setStatus(status);
        return baseMapper.updateById(discussPost);
    }

    @Override
    public Integer updateScore(Long id, double score) {
        DiscussPost discussPost = new DiscussPost();
        discussPost.setId(id);
        discussPost.setScore(new BigDecimal(score));
        return baseMapper.updateById(discussPost);
    }
}
