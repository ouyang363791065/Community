package com.ouyang.community.http;

import lombok.Getter;

/**
 * 此类的描述是：Http请求的状态码
 */
@Getter
public enum HttpStatusCode {
    SUCCESS(200, "success"),  //操作执行成功

    /*** 实体错误 ***/
    ENTITY_NOT_EXIST(-666600101, "实体不存在"),

    /*** 用户错误 ***/
    USER_NOT_EXIST(-666600201, "用户不存在"),

    /*** 普通请求错误 ***/
    FIELD_NOT_VALID(-666600301, "字段无效"),

    /*** 登入登出错误 ***/
    NOT_LOGIN(-666600401, "您未登入"),
    LOGOUT_ERROR(-666600402, "登出失败"),

    /*** Security使用到的错误 ***/
    USER_NO_AUTHORITY(-666600501,"该用户没有权限");

    private final Integer code;
    private final String msg;

    HttpStatusCode(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    @Override
    public String toString() {
        return "HttpStatusCode{" +
                "code=" + code +
                ", msg='" + msg + '\'' +
                '}';
    }
}