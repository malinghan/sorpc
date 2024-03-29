package com.so.sorpc.core.filter;

import java.lang.reflect.Method;
import java.util.Arrays;

import com.so.sorpc.core.api.Filter;
import com.so.sorpc.core.api.RpcRequest;
import com.so.sorpc.core.api.RpcResponse;
import com.so.sorpc.core.utils.MethodUtils;
import com.so.sorpc.core.utils.MockUtils;

import lombok.SneakyThrows;

/**
 * @author someecho <linghan.ma@gmail.com>
 * Created on 2024-03-29
 */
public class MockFilter implements Filter {

    @SneakyThrows
    @Override
    public Object preFilter(RpcRequest rpcRequest) {
        Class service = Class.forName(rpcRequest.getService());
        Method method = findMethod(service, rpcRequest.getMethodSign());
        return MockUtils.mock(method.getReturnType(), method.getGenericParameterTypes());
    }

    private Method findMethod(Class service ,  String methodSign) {
        return Arrays.stream(service.getMethods())
                .filter(method -> !MethodUtils.checkLocalMethod(method))
                .filter(method -> methodSign.equals(MethodUtils.methodSign(method)))
                .findFirst()
                .orElse(null);
    }

    @Override
    public Object postFilter(RpcRequest request, RpcResponse response, Object result) {
        return null;
    }
}
