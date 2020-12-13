package com.ouyang.community.service.impl;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ouyang.community.entity.EntityBase;
import com.ouyang.community.exception.CommunityException;
import com.ouyang.community.http.HttpStatusCode;
import com.ouyang.community.service.IBaseService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

/**
 * @Description:
 * @Author: feixi
 * @Date: 2020/11/18 15:04
 */
public class IBaseServiceImpl<M extends BaseMapper<T>, T extends EntityBase> extends ServiceImpl<M, T> implements IBaseService<T> {
    @Override
    public Collection<T> createMany(Collection<T> entities) {
        entities.stream().forEach(entity -> entity.setCreateTime(System.currentTimeMillis()));
        entities.stream().forEach(entity -> entity.setUpdateTime(System.currentTimeMillis()));
        this.saveBatch(entities);
        return entities;
    }

    @Override
    public Collection<T> updateMany(Collection<T> entities) {
        if (CollectionUtils.isEmpty(entities)) {
            throw new CommunityException(HttpStatusCode.FIELD_NOT_VALID);
        }
        ArrayList<T> results = new ArrayList<>();
        for (T entity : entities) {
            if (Objects.isNull(entity.getId())) {
                throw new CommunityException(HttpStatusCode.FIELD_NOT_VALID);
            }
            Long createTime = entity.getCreateTime();
            entity.setCreateTime(null);
            entity.setUpdateTime(System.currentTimeMillis());
            baseMapper.updateById(entity);
            entity.setCreateTime(createTime);
            results.add(entity);
        }
        return results;
    }
}
