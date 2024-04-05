package com.so.sorpc.core.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Import;

import com.so.sorpc.core.scan.CustomScannerRegistrar;

/**
 * @author someecho <linghan.ma@gmail.com>
 * Created on 2024-04-06
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Import(CustomScannerRegistrar.class)
@Documented
public @interface SoRpcScan {
    String[] basePackage();
}
