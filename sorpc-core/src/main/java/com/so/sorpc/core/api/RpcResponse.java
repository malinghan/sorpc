package com.so.sorpc.core.api;

import com.so.sorpc.core.exception.RpcException;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * response for rpc call
 * @author someecho <linghan.ma@gmail.com>
 * Created on 2024-03-07
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RpcResponse<T> {
    boolean status;
    T data;
    RpcException ex;
}
