package com.so.sorpc.core.exception;

import lombok.Data;

/**
 * @author someecho <linghan.ma@gmail.com>
 * Created on 2024-03-29
 */

@Data
public class RpcException extends RuntimeException {

    private String errorcode;

    public RpcException() {

    }

    public RpcException(String message) {
         super(message);
    }

    public RpcException(String message, String errorcode) {
        super(message);
        this.errorcode = errorcode;
    }

    public RpcException(Throwable throwable, String errorcode) {
        super(throwable);
        this.errorcode = errorcode;
    }

    public RpcException(String message, Throwable throwable) {
        super(message, throwable);
    }

    public RpcException(Throwable cause) {
        super(cause);
    }

    // X => 技术类异常：
    // Y => 业务类异常：
    // Z => unknown, 搞不清楚，再归类到X或Y
    public static final String SocketTimeoutEx = "X001" + "-" + "http_invoke_timeout";
    public static final String NoSuchMethodEx  = "X002" + "-" + "method_not_exists";
    public static final String UnknownEx  = "Z001" + "-" + "unknown";
}
