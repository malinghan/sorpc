package com.so.sorpc.core.utils;

import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * @author someecho <linghan.ma@gmail.com>
 * Created on 2024-03-11
 */
public class MethodUtils {
    public static boolean checkLocalMethod(final String method) {
        //本地方法不代理
        if ("toString".equals(method) ||
                "hashCode".equals(method) ||
                "notifyAll".equals(method) ||
                "equals".equals(method) ||
                "wait".equals(method) ||
                "getClass".equals(method) ||
                "notify".equals(method)) {
            return true;
        }
        return false;
    }

    public static String methodSign(Method method) {
        StringBuilder methodSign = new StringBuilder(method.getName());
        methodSign.append("@")
                .append(method.getParameterCount());
        Arrays.stream(method.getParameterTypes()).forEach(
                c -> methodSign.append("_").append(c.getCanonicalName())
        );
        return methodSign.toString();
    }

    public static void main(String[] args) {
        Arrays.stream(MethodUtils.class.getMethods()).forEach(
                c -> {System.out.println(MethodUtils.methodSign(c)); });
    }
}
