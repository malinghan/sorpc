package com.so.sorpc.core.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author someecho <linghan.ma@gmail.com>
 * Created on 2024-03-11
 */
@Documented
@Retention(RetentionPolicy.RUNTIME) //运行时解析
@Target(ElementType.FIELD) //用在实例上
@Inherited //自动集成
public @interface SoRpcConsumer {
}
