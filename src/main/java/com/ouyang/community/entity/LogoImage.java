package com.ouyang.community.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @Description:
 * @Author: feixi
 * @Date: 2020/12/31 15:55
 */
@Data
@TableName(value = "logo_image")
@EqualsAndHashCode(callSuper = true)
public class LogoImage extends EntityBase {
    @TableField(value = "filename")
    String filename;
    @TableField(value = "content")
    byte[] content;
    @TableField(value = "size")
    Long size;
}
