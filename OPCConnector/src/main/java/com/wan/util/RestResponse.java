package com.wan.util;

import com.alibaba.druid.support.json.JSONUtils;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

/**
 * @author WanYue
 */
@Setter
@Getter
public class RestResponse<T> {

    private Boolean success;

    private Integer code;

    private String msg;

    private T data;

    /**
     * 构建无数据，消息"success"的成功返回
     *
     * @return 无数据，消息"success"的成功返回
     */
    public static <T> RestResponse<T> success() {
        return new RestResponse<T>(200, Boolean.TRUE, "Success", null);
    }

    /**
     * 构建无数据，消息"success"的成功返回
     *
     * @param code 成功状态码
     * @return 无数据，消息"success"的成功返回
     */
    public static <T> RestResponse<T> success(@NotNull Integer code) {
        return new RestResponse<T>(code, Boolean.TRUE, "Success", null);
    }

    /**
     * 构建无数据，指定消息的成功返回
     *
     * @param msg 消息
     * @return 无数据，指定消息的成功返回
     */
    public static <T> RestResponse<T> success(String msg) {
        return new RestResponse<T>(200, Boolean.TRUE, msg, null);
    }

    /**
     * 构建无数据，指定消息的成功返回
     *
     * @param code 成功状态码
     * @param msg  消息
     * @return 无数据，指定消息的成功返回
     */
    public static <T> RestResponse<T> success(@NotNull Integer code, String msg) {
        return new RestResponse<T>(code, Boolean.TRUE, msg, null);
    }

    /**
     * 构建指定数据，消息"success"的成功返回
     *
     * @param data 数据
     * @return 指定数据，消息"success"的成功返回
     */
    public static <T> RestResponse<T> success(T data) {
        return new RestResponse<T>(200, Boolean.TRUE, "Success", data);
    }

    /**
     * 构建指定数据，消息"success"的成功返回
     *
     * @param code 成功状态码
     * @param data 数据
     * @return 指定数据，消息"success"的成功返回
     */
    public static <T> RestResponse<T> success(@NotNull Integer code, T data) {
        return new RestResponse<T>(code, Boolean.TRUE, "Success", data);
    }

    /**
     * 构建指定数据，指定消息的成功返回
     *
     * @param msg  消息
     * @param data 数据
     * @return 指定数据，指定消息的成功返回
     */
    public static <T> RestResponse<T> success(String msg, T data) {
        return new RestResponse<T>(200, Boolean.TRUE, msg, data);
    }

    /**
     * 构建指定数据，指定消息的成功返回
     *
     * @param code 成功状态码
     * @param msg  消息
     * @param data 数据
     * @return 指定数据，指定消息的成功返回
     */
    public static <T> RestResponse<T> success(@NotNull Integer code, String msg, T data) {
        return new RestResponse<T>(code, Boolean.TRUE, msg, data);
    }

    /**
     * 构建无数据，指定消息的失败返回
     *
     * @param msg 消息
     * @return 无数据，指定消息的失败返回
     */
    public static <T> RestResponse<T> failure(String msg) {
        return new RestResponse<T>(500, Boolean.FALSE, msg, null);
    }

    /**
     * 构建无数据，指定消息的失败返回
     *
     * @param code 错误状态码
     * @param msg  消息
     * @return 无数据，指定消息的失败返回
     */
    public static <T> RestResponse<T> failure(@NotNull Integer code, String msg) {
        return new RestResponse<T>(code, Boolean.FALSE, msg, null);
    }


    /**
     * 构建指定数据，指定消息的失败返回
     *
     * @param msg  消息
     * @param data 数据
     * @return 指定数据，指定消息的失败返回
     */
    public static <T> RestResponse<T> failure(String msg, T data) {
        return new RestResponse<T>(500, Boolean.FALSE, msg, data);
    }


    /**
     * 构建指定数据，指定消息的失败返回
     *
     * @param code 错误状态码
     * @param msg  消息
     * @param data 数据
     * @return 指定数据，指定消息的失败返回
     */
    public static <T> RestResponse<T> failure(@NotNull Integer code, String msg, T data) {
        return new RestResponse<T>(code, Boolean.FALSE, msg, data);
    }

    private RestResponse(Integer code, Boolean success, String msg, T data) {
        this.code = code;
        this.success = success;
        this.msg = msg;
        this.data = data;
    }

    @Override
    public String toString() {
        return JSONUtils.toJSONString(this);
    }
}
