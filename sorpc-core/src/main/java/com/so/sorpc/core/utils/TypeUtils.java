package com.so.sorpc.core.utils;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson2.JSON;

import lombok.extern.slf4j.Slf4j;

/**
 * @author someecho <linghan.ma@gmail.com>
 * Created on 2024-03-14
 */
@Slf4j
public class TypeUtils {

    public static Object cast(Object obj, Class<?> clazz) {
        if (obj == null || clazz == null) {
            return null;
        }
        Class<?> objClass = obj.getClass();
        if (clazz.isAssignableFrom(objClass)) {
            return obj;
        }

        return JSON.to(clazz, JSON.toJSONString(obj));
    }

    public static Object castByMethod(Object data, Method method) {
        if (data instanceof JSONObject jsonResult) {
            if (Map.class.isAssignableFrom(method.getReturnType())) {
                Map map = new HashMap<>();
                Type genericReturnType = method.getGenericReturnType();
                log.debug(genericReturnType.getTypeName());
                if (genericReturnType instanceof ParameterizedType parameterizedType) {
                    Type key = parameterizedType.getActualTypeArguments()[0];
                    log.debug(key.getTypeName());
                    Type value = parameterizedType.getActualTypeArguments()[1];
                    log.debug(value.getTypeName());
                    jsonResult.forEach((key1, value1) -> {
                        Object objectKey = TypeUtils.cast(key1, (Class<?>) key);
                        Object objectValue = TypeUtils.cast(value1, (Class<?>) value);
                        map.put(objectKey, objectValue);
                    });
                }
                return  map;
            }
            return jsonResult.toJavaObject(method.getReturnType());
        } else if (data instanceof JSONArray jsonArray) {
            Object[] array = jsonArray.toArray();
            if (method.getReturnType().isArray()) {
                Class<?> componentType = method.getReturnType().getComponentType();
                log.debug("获取到的componentType为: " + componentType);
                Object resultArray = Array.newInstance(componentType, array.length);
                for (int i = 0; i < array.length; i++) {
                    Array.set(resultArray, i, array[i]);
                }
                return resultArray;
            } else if (List.class.isAssignableFrom(method.getReturnType())) {
                List<Object> res = new ArrayList<>();
                Type genericReturnType = method.getGenericReturnType();
                log.debug(genericReturnType.getTypeName());
                if (genericReturnType instanceof ParameterizedType parameterizedType) {
                    Type actualType = parameterizedType.getActualTypeArguments()[0];
                    log.debug(actualType.getTypeName());
                    for (Object o : array) {
                        res.add(TypeUtils.cast(o, (Class<?>) actualType));
                    }
                } else {
                    res.addAll(List.of(array));
                }
                return res;
            } else {
                return null;
            }
        } else {
            return TypeUtils.cast(data, method.getReturnType());
        }
    }

    public static Object cast1(Object origin,  Class<?> targetType) {
        //如果origin为空，return
        if (origin == null) { return null;}

        //如果原始类型和目标类型一致，return
        if (origin.getClass().equals(targetType)) { return origin;}
        Class<?>  objectType = origin.getClass();

        //如果是目标类是子类,就不转了,比如包装类
        if (targetType.isAssignableFrom(objectType)) {
            return origin;
        }

        //如果输入输出是数组的话
        if (targetType.isArray()) {
            //首先判断是List还是arr
            if (origin instanceof List list) {
                origin = list.toArray();
            }
            //将arr转为目标
            int length = Array.getLength(origin);
            Object resultArray = Array.newInstance(targetType.getComponentType() ,length);
            for (int i = 0; i < length; i++) {
                Array.set(resultArray, i, Array.get(origin, i));
            }
            return resultArray;
        }
        //如果原始类型是map的话
        if (origin instanceof HashMap map) {
            JSONObject jsonObject = new JSONObject(map);
            return jsonObject.toJavaObject(targetType);
        }

        if (origin instanceof JSONObject jsonObject) {
            return jsonObject.toJavaObject(targetType);
        }

        //如果是基本数据类型
        if (targetType.equals(Long.class) || targetType.equals(Long.TYPE)) {
            return Long.valueOf(origin.toString());
        } else if (targetType.equals(Integer.class) || targetType.equals(Integer.TYPE)) {
            return Integer.valueOf(origin.toString());
        } else if (targetType.equals(Byte.class) || targetType.equals(Byte.TYPE)) {
            return Byte.valueOf(origin.toString());
        } else if (targetType.equals(Short.class) || targetType.equals(Short.TYPE)) {
            return Short.valueOf(origin.toString());
        } else if (targetType.equals(Double.class) || targetType.equals(Double.TYPE)) {
            return Double.valueOf(origin.toString());
        } else if (targetType.equals(Float.class) || targetType.equals(Float.TYPE)) {
            return Float.valueOf(origin.toString());
        } else if (targetType.equals(Boolean.class) || targetType.equals(Boolean.TYPE)) {
            return Boolean.valueOf(origin.toString());
        } else if (targetType.equals(Character.class) || targetType.equals(Character.TYPE)) {
            return origin.toString().charAt(0);
        }

        return null;
    }
}
