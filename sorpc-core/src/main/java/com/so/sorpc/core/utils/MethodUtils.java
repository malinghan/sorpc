package com.so.sorpc.core.utils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

/**
 * @author someecho <linghan.ma@gmail.com>
 * Created on 2024-03-11
 */
@Slf4j
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

    public static boolean checkIfObjectMethod(final Method method) {
        return  Modifier.isPublic(method.getModifiers())
                && method.getDeclaringClass().equals(Object.class);
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

    public static List<Field> getAnnotatedFields(Class<?> beanClass, Class<? extends Annotation> annotationClass) {
        List<Field> annotatedFields = new ArrayList<>();
        //TODO
        while (beanClass != null) {
            //            log.debug("获取到的beanClass:" + beanClass.getCanonicalName());
            Field[] fields = beanClass.getDeclaredFields();
            for (Field field : fields) {
                if(field.isAnnotationPresent(annotationClass)) {
                    annotatedFields.add(field);
                }
            }
            beanClass = beanClass.getSuperclass();
        }
        return annotatedFields;
    }

    public static void main(String[] args) {
        Arrays.stream(MethodUtils.class.getMethods()).forEach(
                c -> {log.debug(MethodUtils.methodSign(c)); });
    }
}
