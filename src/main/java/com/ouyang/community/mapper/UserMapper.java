package com.ouyang.community.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ouyang.community.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * @Description:
 * @Author: feixi
 * @Date: 2020/11/18 14:37
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
}
