package com.ouyang.community.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ouyang.community.entity.LogoImage;
import org.apache.ibatis.annotations.Mapper;

/**
 * @Description:
 * @Author: feixi
 * @Date: 2020/12/31 15:57
 */
@Mapper
public interface LogoImageMapper extends BaseMapper<LogoImage> {
    void insertLogoImage(LogoImage logoImage);
    LogoImage getLogoImage(String filename);
}
