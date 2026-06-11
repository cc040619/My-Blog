package com.my.blog.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.my.blog.enums.AppHttpCodeEnum;

import java.io.Serializable;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResponseResult<T> implements Serializable {
    private Integer code;
    private String msg;
    private T data;

    public ResponseResult() {
        this.code = AppHttpCodeEnum.SUCCESS.getCode();
        this.msg = AppHttpCodeEnum.SUCCESS.getMsg();
    }

    public ResponseResult(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public static ResponseResult okResult() {
        ResponseResult result = new ResponseResult();
        return result;
    }

    public static ResponseResult okResult(Object data) {
        ResponseResult result = new ResponseResult();
        if (data != null) {
            result.setData(data);
        }
        return result;
    }

    public static ResponseResult errorResult(int code, String msg) {
        ResponseResult result = new ResponseResult(code, msg);
        return result;
    }

    public static ResponseResult errorResult(AppHttpCodeEnum enums) {
        ResponseResult result = new ResponseResult(enums.getCode(),
                enums.getMsg());
        return result;
    }

    public static ResponseResult errorResult(AppHttpCodeEnum enums, String
            msg) {
        ResponseResult result = new ResponseResult(enums.getCode(), msg);
        return result;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}