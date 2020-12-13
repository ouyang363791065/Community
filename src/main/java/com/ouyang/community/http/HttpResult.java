package com.ouyang.community.http;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import lombok.Data;

/**
 * 此类的描述是：Http的返回信息结构体
 */
@Data
public class HttpResult<T> {
    private Integer code;
    private T data;
    private String msg;

    @Override
    public String toString() {
        return JSON.toJSONString(this, SerializerFeature.WriteMapNullValue);
    }
}
