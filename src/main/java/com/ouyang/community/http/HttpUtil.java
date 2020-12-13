package com.ouyang.community.http;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

/**
 * 此类的描述是：构造Http返回信息
 */
@Slf4j
public class HttpUtil {
    private static final String HTTP_POST_METHOD = "POST";
    private static final String HTTP_PUT_METHOD = "PUT";
    private static final long HTTP_TIMEOUT_SECOND = 15L;

    /**
     * 构造没有数据的Http返回
     *
     * @param statusCode 状态码
     * @return
     */
    public static <T> HttpResult<T> buildResult(HttpStatusCode statusCode) {
        HttpResult<T> httpResult = new HttpResult<>();
        httpResult.setCode(statusCode.getCode());
        httpResult.setMsg(statusCode.getMsg());
        return httpResult;
    }

    /**
     * 构造没有数据的Http返回, 覆盖msg字段
     *
     * @param statusCode 状态码
     * @return 返回pojo
     */
    public static <T> HttpResult<T> buildResult(String msg, HttpStatusCode statusCode) {
        HttpResult<T> httpResult = new HttpResult<>();
        httpResult.setCode(statusCode.getCode());
        httpResult.setMsg(msg);
        return httpResult;
    }

    /**
     * 构造有数据的Http返回, 并覆盖msg字段
     *
     * @param statusCode 状态码
     * @return 返回pojo
     */
    public static <T> HttpResult<T> buildResult(String msg, HttpStatusCode statusCode, T data) {
        HttpResult<T> httpResult = new HttpResult<>();
        httpResult.setCode(statusCode.getCode());
        httpResult.setMsg(msg);
        httpResult.setData(data);
        return httpResult;
    }

    /**
     * 构造带数据的Http返回
     *
     * @param statusCode 状态码
     * @param data       数据
     * @return
     */
    public static <T> HttpResult<T> buildResult(HttpStatusCode statusCode, T data) {
        HttpResult<T> httpResult = new HttpResult<>();
        httpResult.setCode(statusCode.getCode());
        httpResult.setMsg(statusCode.getMsg());
        httpResult.setData(data);
        return httpResult;
    }

    /**
     * 构造成功的Http返回
     *
     * @param data 数据
     * @return
     */
    public static <T> HttpResult<T> buildSuccessResult(T data) {
        HttpResult<T> httpResult = new HttpResult<>();
        httpResult.setCode(HttpStatusCode.SUCCESS.getCode());
        httpResult.setMsg(HttpStatusCode.SUCCESS.getMsg());
        httpResult.setData(data);
        return httpResult;
    }

    public static Object httpGet(String url) {
        return httpGet("", "", url);
    }

    public static byte[] httpGetBytes(String url) throws IOException {
        Request request = new Request.Builder().url(url).build();
        Call call = getHttpClient("", "").newCall(request);
        Response response = call.execute();
        return response.body().bytes();
    }

    /**
     * Http get 请求
     *
     * @param proxyHost 代理的主机
     * @param proxyPort 代理的端口
     * @param url       访问的Url
     * @return JSON对象列表
     */
    public static Object httpGet(String proxyHost, String proxyPort, String url) {
        if (StringUtils.isEmpty(url)) {
            log.warn("Http get failed, url is null: " + url);
            return Collections.emptyList();
        }

        Request request = new Request.Builder().url(url).build();
        return getResponse(getHttpClient(proxyHost, proxyPort), request);
    }

    public static Object httpPost(String url, Object body) {
        return httpPost("", "", url, body, null);
    }

    public static Object httpPost(String url, Object body, Headers headers) {
        return httpPost("", "", url, body, headers);
    }

    /**
     * Http post 请求
     *
     * @param proxyHost 代理的主机
     * @param proxyPort 代理的端口
     * @param url       访问的Url
     * @param body      实体内容
     * @return JSON对象列表
     */
    public static Object httpPost(String proxyHost, String proxyPort, String url, Object body, Headers headers) {
        if (StringUtils.isEmpty(url) || body == null) {
            log.warn("Http post failed, url or body is null: " + url + " " + body);
            return Collections.emptyList();
        }

        return getResponse(getHttpClient(proxyHost, proxyPort), generateRequest(url, HTTP_POST_METHOD, body, headers));
    }

    public static Object httpPut(String url, Object body) {
        return httpPut("", "", url, body);
    }

    public static Object httpPut(String proxyHost, String proxyPort, String url, Object body) {
        if (StringUtils.isEmpty(url) || body == null) {
            log.warn("Http put failed, url or body is null: " + url + " " + body);
            return Collections.emptyList();
        }

        return getResponse(getHttpClient(proxyHost, proxyPort), generateRequest(url, HTTP_PUT_METHOD, body, null));
    }

    /**
     * Http post 请求
     *
     * @param url  访问的Url
     * @param body 实体内容
     * @return JSON对象列表
     */
    private static Request generateRequest(String url, String method, Object body, Headers headers) {
        RequestBody requestBody = FormBody.create(MediaType.parse("application/json; charset=utf-8"),
                JSON.toJSONString(body));
        if (headers == null || headers.size() == 0) {
            return new Request.Builder().url(url).method(method.toUpperCase(), requestBody).build();
        } else {
            return new Request.Builder().url(url).method(method.toUpperCase(), requestBody).headers(headers).build();
        }
    }

    /**
     * 构造客户端，分为使用代理和没有代理
     *
     * @param proxyHost 代理的主机
     * @param proxyPort 代理的端口
     * @return 客户端
     */
    private static OkHttpClient getHttpClient(String proxyHost, String proxyPort) {
        OkHttpClient client;

        if (!StringUtils.isAnyEmpty(proxyHost, proxyPort)) {
            log.info("User http proxy: " + proxyHost + " " + proxyPort);
            client = new OkHttpClient().newBuilder()
                    .proxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, Integer.parseInt(proxyPort))))
                    .readTimeout(HTTP_TIMEOUT_SECOND, TimeUnit.SECONDS)
                    .build();
        } else {
            client = new OkHttpClient().newBuilder()
                    .readTimeout(HTTP_TIMEOUT_SECOND, TimeUnit.SECONDS)
                    .build();
        }

        return client;
    }

    /**
     * 构造返回结果
     *
     * @param client  客户端
     * @param request 请求
     * @return
     */
    private static Object getResponse(OkHttpClient client, Request request) {
        Response response;

        try {
            response = client.newCall(request).execute();
        } catch (IOException e) {
            log.error("Http request exception: " + request.url().toString() + "\n" + JSON.toJSONString(e));
            return Collections.emptyList();
        }

        if (!response.isSuccessful()) {
            log.error("Http request failed: " + request.url().toString() + "\n" + JSON.toJSONString(response));
            return Collections.emptyList();
        }

        HttpResult httpResult;

        try {
            httpResult = JSON.parseObject(response.body().string(), HttpResult.class);
        } catch (IOException e) {
            log.error("Http request failed, Cannot get body string: " + request.url().toString() + " " + JSON.toJSONString(response));
            return Collections.emptyList();
        }

        return httpResult.getData();
    }
}