package com.so.sorpc.core.utils;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.util.ClassUtils;

import lombok.Data;
import lombok.SneakyThrows;

/**
 * @author someecho <linghan.ma@gmail.com>
 * Created on 2024-03-29
 */
public class MockUtils {

    public static Object mock(Class clazz, Type[] generics) {
        //判断是否是原生类型
        boolean primitiveOrWrapper = ClassUtils.isPrimitiveOrWrapper(clazz);
        if(primitiveOrWrapper) return mockPrimitive(clazz);
        //判断是否是String
        if(String.class.equals(clazz)) return mockString();
        //判断是否是数字
        if (Number.class.isAssignableFrom(clazz)) {
            return 10;
        }
        //判断是否是数组
        if(clazz.isArray()) {
            return mockArray(clazz.getComponentType());
        }
        //判断是否是List
        if(List.class.isAssignableFrom(clazz)) {
            return mockList(clazz, generics[0]);
        }
        //判断是否是map
        if(Map.class.isAssignableFrom(clazz)) {
            return mockMap(clazz, generics[1]);
        }
        return mockPojo(clazz);
    }

    private static Object mockPrimitive(Class<?> clazz) {

        if (Boolean.class.equals(clazz)) {
            return true;
        }

        return 1;
    }

    private static Object mockMap(Class<?> clazz, Type generic) {
        HashMap<Object, Object> map = new HashMap<>();
        map.put("a", mock((Class)generic, null));
        map.put("b", mock((Class)generic, null));
        return map;
    }

    private static Object mockString() {
        return "this_is_a_mock_string";
    }

    public static Object mock(Class<?> type) {
        return mock(type, null);
    }

    @SneakyThrows
    private static Object mockPojo(Class type) {
        Object result = type.getDeclaredConstructor().newInstance();
        Field[] fields = type.getDeclaredFields();
        for (Field f : fields) {
            f.setAccessible(true);
            Class<?> fType = f.getType();
            Object fValue = mock(fType);
            f.set(result, fValue);
        }
        return result;
    }

    private static Object mockArray(Class<?> clazz) {
        Object array = Array.newInstance(clazz, 2);
        Array.set(array, 0, mock(clazz, null));
        Array.set(array, 1, mock(clazz,null));
        return array;
    }

    private static Object mockList(Class<?> clazz, Type generic) {
        List list = new ArrayList<>();
        list.add(mock((Class)generic, null));
        list.add(mock((Class)generic, null));
        return list;
    }

    public static void main(String[] args) {
        System.out.println(mock(UserDto.class));
        System.out.println(mock(Byte.class));
        System.out.println(mock(Character.class));
        System.out.println(mock(Boolean.class));
        System.out.println(mock(Integer.class));
        System.out.println(mock(Float.class));
        System.out.println(mock(Short.class));
        System.out.println(mock(Long.class));
        System.out.println(mock(Double.class));
        System.out.println(mock(BigInteger.class));
        System.out.println(mock(BigDecimal.class));
        System.out.println(mock(String.class));
        System.out.println(mock(Pojo.class));
        Arrays.stream(((Pojo[]) mock(Pojo[].class))).forEach(System.out::println);
        //        System.out.println(mock(ListPojo.class,null));
    }

    public static class UserDto {
        private int a;
        private String b;

        @Override
        public String toString() {
            return a + "," + b;
        }
    }

    @Data
    public static class ListPojo {
        private List<InnerPojo> list;
        private Integer inner;
        private Map<String, InnerPojo> map;
    }

    @Data
    public static class Pojo {
        private int id;
        private String name;
        private float amount;
        private InnerPojo inner;
    }

    @Data
    public static class InnerPojo {
        private int value;
        private String key;
    }
}
