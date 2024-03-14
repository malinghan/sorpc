package com.so.sorpc.core.meta;

import java.lang.reflect.Method;

import lombok.Data;

/**
 * @author someecho <linghan.ma@gmail.com>
 * Created on 2024-03-13
 */
@Data
public class ProviderMeta {
    Method method;
    String methodSign;
    Object serviceImpl;
}
