package com.ouyang.community.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.ouyang.community.entity.DiscussPost;
import com.ouyang.community.mapper.DiscussPostMapper;
import com.ouyang.community.service.DiscussPostService;
import com.ouyang.community.filter.SensitiveFilter;
import lombok.extern.slf4j.Slf4j;
import com.github.benmanes.caffeine.cache.LoadingCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
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
    @Resource
    private DiscussPostMapper discussPostMapper;

    /**
     * PostConstruct在构造函数之后执行，init方法之前执行
     * 初始化时设置好缓存对象，在调用get方法时，会先查询缓存对象里面是否有该key命中的缓存，
     * 若有，则返回，若没有，则调用load方法从磁盘里加载数据，然后放入缓存里，并且返回缓存里的数据
     */
    @Override
    @PostConstruct
    public void init() {
        // 初始化帖子列表缓存
        postListCache = Caffeine.newBuilder()
                .maximumSize(maxSize)
                .expireAfterWrite(expireSeconds, TimeUnit.SECONDS)
                .build(key -> {
                    // key ==> offset:limit
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
                    QueryWrapper<DiscussPost> queryWrapper = new QueryWrapper<>();
                    queryWrapper.lambda()
                            .ne(DiscussPost::getStatus, 2)
                            .orderByDesc(DiscussPost::getType)
                            .orderByDesc(DiscussPost::getScore)
                            .orderByDesc(DiscussPost::getCreateTime)
                            .last("limit " + offset + " , " + limit);
                    return baseMapper.selectList(queryWrapper);
                });

        // 初始化帖子总数缓存
        postRowsCache = Caffeine.newBuilder()
                .maximumSize(maxSize)
                .expireAfterWrite(expireSeconds, TimeUnit.SECONDS)
                .build(key -> {
                    // TODO:二级缓存：redis->mysql
                    log.debug("load post list from DB.");
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
    public List<DiscussPost> findDiscussPosts(Integer offset, Integer limit, String orderMode) {
        // 最热帖子走缓存数据
        if (orderMode.equals("close")) {
            // 返回给定的key在LoadingCache中的数据，如果cache中没有该key，
            // 将调用CacheLoader的load方法去load数据，如果load不到数据，将返回null。
            return postListCache.get(offset + ":" + limit);
        }
        // 最新帖子按照发布时间排序
        log.debug("load post list from DB.");
        return discussPostMapper.selectList(new QueryWrapper<DiscussPost>().lambda()
                .ne(DiscussPost::getStatus, 2)
                .orderByDesc(DiscussPost::getType)
                .orderByDesc(DiscussPost::getScore)
                .orderByDesc(DiscussPost::getCreateTime)
                .last("limit " + offset + " , " + limit));
    }

    @Override
    public Integer findDiscussPostRows(Integer userId) {
        // 首页查询走缓存
        if (userId == 0) {
            // 返回给定的key在LoadingCache中的数据，如果cache中没有该key，
            // 将调用CacheLoader的load方法去load数据，如果load不到数据，将返回null。
            return postRowsCache.get(userId);
        }
        log.debug("load post list from DB.");
        return baseMapper.selectList(new QueryWrapper<DiscussPost>().lambda()
                .ne(DiscussPost::getStatus, 2)
                .eq(DiscussPost::getUserId, userId))
                .size();
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
