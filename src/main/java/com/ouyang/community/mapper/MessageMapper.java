package com.ouyang.community.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ouyang.community.entity.Message;
import org.apache.ibatis.annotations.Mapper;

/**
 * @Description:
 * @Author: feixi
 * @Date: 2020/11/18 14:40
 */
@Mapper
public interface MessageMapper extends BaseMapper<Message> {
}
