package com.ouyang.community.service;

import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Collection;

/**
 * @Description:
 * @Author: feixi
 * @Date: 2020/11/18 14:48
 */
public interface IBaseService<T> extends IService<T> {
    Collection<T> createMany(Collection<T> entities);
    Collection<T> updateMany(Collection<T> entities);
}
