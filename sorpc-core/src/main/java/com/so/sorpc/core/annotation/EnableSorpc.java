package com.so.sorpc.core.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Import;

import com.so.sorpc.core.config.ConsumerConfig;
import com.so.sorpc.core.config.ProviderConfig;

/**
 * enable sorpc for auto import config for provider and consumer
 * @author someecho <linghan.ma@gmail.com>
 * Created on 2024-04-05
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
@Import({ProviderConfig.class, ConsumerConfig.class})
public @interface EnableSorpc {
}
