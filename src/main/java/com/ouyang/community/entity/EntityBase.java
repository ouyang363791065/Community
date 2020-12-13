package com.ouyang.community.entity;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;

import static com.baomidou.mybatisplus.annotation.FieldStrategy.NOT_NULL;

@Data
public abstract class EntityBase implements Serializable {
    @TableId(type = IdType.AUTO)
    Long id;
    @TableField(value = "create_time", insertStrategy = NOT_NULL)
    Long createTime;
    @TableField(value = "update_time", insertStrategy = NOT_NULL)
    Long updateTime;

    public EntityBase() {
        this.updateTime = System.currentTimeMillis();
    }

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}
