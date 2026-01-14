package com.lcs.beautybackend.common;

import com.lcs.beautybackend.exception.ErrorCode;
import lombok.Data;

/*
全局响应封装类
 */
@Data
public class BaseResponse<T> {
    /*
     * 状态码
     */
    private int code;
    /*
     * 返回数据
     */
    private T data;
    /*
     * 返回信息
     */
    private String message;

    public BaseResponse(int code, T data, String message) {
        this.code = code;
        this.data = data;
        this.message = message;
    }

    public BaseResponse(int code, T data) {
        this(code, data, "");
    }

    public BaseResponse(ErrorCode errorCode) {
        this(errorCode.getCode(), null, errorCode.getMessage());
    }
}
