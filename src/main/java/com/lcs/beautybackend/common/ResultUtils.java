package com.lcs.beautybackend.common;

import com.lcs.beautybackend.exception.ErrorCode;

/*
响应工具类
 */
public class ResultUtils {
/**
 * 创建一个表示成功响应的BaseResponse对象
 *
 * @param <T> 泛型类型，表示响应中数据的类型
 * @param data 成功时返回的数据
 * @return 返回一个包含成功状态码、数据和成功信息的BaseResponse对象
 */
    public static <T> BaseResponse<T> success(T data) {
    // 创建并返回一个新的BaseResponse实例
    // 状态码设为0表示成功
    // data参数作为响应数据传入
    // "ok"作为操作成功的提示信息
        return new BaseResponse<>(0, data, "ok");
    }

/**
 * 创建一个表示错误响应的BaseResponse对象
 * @param code 错误代码，用于标识具体的错误类型
 * @param message 错误信息，用于描述具体的错误内容
 * @return 返回一个包含错误代码和错误信息的BaseResponse对象
 */
    public static BaseResponse<?> error(int code, String message) {
    // 使用提供的错误代码和错误信息创建一个新的BaseResponse对象
    // 数据字段被设置为null，因为这是一个错误响应
        return new BaseResponse<>(code, null, message);
    }

/**
 * 创建一个表示错误响应的静态方法
 * @param errorCode 错误码对象，包含错误信息
 * @return 返回一个包含错误码的BaseResponse对象
 */
    public static BaseResponse<?> error(ErrorCode errorCode) {
    // 使用提供的错误码创建一个新的BaseResponse对象并返回
        return new BaseResponse<>(errorCode);
    }

/**
 * 创建一个表示错误响应的静态方法
 * @param errorCode 错误代码枚举，包含错误状态码和相关信息
 * @param message 自定义错误描述信息
 * @return 返回一个包含错误状态码和自定义消息的BaseResponse对象
 */
    public static BaseResponse<?> error(ErrorCode errorCode, String message) {
    // 使用给定的错误代码和自定义消息创建一个新的BaseResponse对象
    // 第一个参数是错误代码，第二个参数是数据(null表示无数据)，第三个参数是错误消息
        return new BaseResponse<>(errorCode.getCode(), null, message);
    }
}
