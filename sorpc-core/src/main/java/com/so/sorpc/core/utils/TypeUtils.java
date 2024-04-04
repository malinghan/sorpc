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

    public static Object castGeneric(Object data, Class<?> type, Type genericReturnType) {
        log.debug("castGeneric: data = " + data);
        log.debug("castGeneric: method.getReturnType() = " + type);
        log.debug("castGeneric: method.getGenericReturnType() = " + genericReturnType);
        //data instanceof Map map ===> JSONObject
        //JSONArray
        if (data instanceof  Map map) {
            if (Map.class.isAssignableFrom(type)) {
                // 目标类型是 Map，此时data可能是map也可能是JO
                log.debug(" ======> map -> map");
                Map resultMap = new HashMap<>();
                log.debug(genericReturnType.getTypeName());
                if (genericReturnType instanceof ParameterizedType parameterizedType) {
                    Type key = parameterizedType.getActualTypeArguments()[0];
                    log.debug(key.getTypeName());
                    Type value = parameterizedType.getActualTypeArguments()[1];
                    log.debug(value.getTypeName());
                    map.forEach((key1, value1) -> {
                        Object objectKey = TypeUtils.cast(key1, (Class<?>) key);
                        Object objectValue = TypeUtils.cast(value1, (Class<?>) value);
                        resultMap.put(objectKey, objectValue);
                    });
                }
                return resultMap;
            } else if(data instanceof JSONObject jsonObject) {// 此时是Pojo，且数据是JO
                log.debug(" ======> JSONObject -> Pojo");
                return jsonObject.toJavaObject(type);
            }else if(!Map.class.isAssignableFrom(type)){ // 此时是Pojo类型，数据是Map
                log.debug(" ======> map -> Pojo");
                return new JSONObject(map).toJavaObject(type);
            }else {
                log.debug(" ======> map -> ?");
                return data;
            }
        } else if (data instanceof List list) {
            Object[] array = list.toArray();
            if (type.isArray()) {
                Class<?> componentType = type.getComponentType();
                log.debug("receive componentType is: " + componentType);
                Object resultArray = Array.newInstance(componentType, array.length);
                for (int i = 0; i < array.length; i++) {
                    if (componentType.isPrimitive() || componentType.getPackageName().startsWith("java")) {
                        Array.set(resultArray, i, array[i]);
                    } else {
                        Object castObject = cast(array[i], componentType);
                        Array.set(resultArray, i, castObject);
                    }
                }
                return resultArray;
            } else if (List.class.isAssignableFrom(type)) {
                List<Object> res = new ArrayList<>(array.length);
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
            return TypeUtils.cast(data, type);
        }
    }

    /**
     * case1: jsonObject
     *     case1.1 Map
     * @param data
     * @param method
     * @return
     */
    public static Object castByMethod(Object data, Method method) {
        log.debug("castMethodResult: method = " + method);
        log.debug("castMethodResult: data = " + data);
        Class<?> type = method.getReturnType();
        Type genericReturnType = method.getGenericReturnType();
        return castGeneric(data, type, genericReturnType);
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
