package com.ouyang.community.service.impl;

import com.ouyang.community.entity.LogoImage;
import com.ouyang.community.mapper.LogoImageMapper;
import com.ouyang.community.service.LogoImageService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @Description:
 * @Author: feixi
 * @Date: 2020/12/31 16:17
 */
@Service
public class LogoImageServiceImpl extends IBaseServiceImpl<LogoImageMapper, LogoImage> implements LogoImageService {
    @Resource
    private LogoImageMapper logoImageMapper;

    @Override
    public LogoImage getLogoImage(String filename) {
        return logoImageMapper.getLogoImage(filename);
    }
}
