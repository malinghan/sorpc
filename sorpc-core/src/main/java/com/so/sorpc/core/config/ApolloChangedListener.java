package com.so.sorpc.core.config;

import org.springframework.cloud.context.environment.EnvironmentChangeEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.ctrip.framework.apollo.model.ConfigChange;
import com.ctrip.framework.apollo.model.ConfigChangeEvent;
import com.ctrip.framework.apollo.spring.annotation.ApolloConfigChangeListener;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * @author someecho <linghan.ma@gmail.com>
 * Created on 2024-05-06
 */
@Slf4j
@Data
public class ApolloChangedListener implements ApplicationContextAware {

    ApplicationContext applicationContext;

    /**
     * @link https://gitee.com/malinghan/apollo-demo/blob/master/src/main/java/io/github/malinghan/apollo1/ApolloChangedListener.java
     * @param changeEvent
     */
    @ApolloConfigChangeListener({"app1","application"})
//     @ApolloConfigChangeListener("${apollo.bootstrap.namespaces}")
    private void changeHandler(ConfigChangeEvent changeEvent) {
        //get all changedKeys
        for (String key : changeEvent.changedKeys()) {
            ConfigChange change = changeEvent.getChange(key);
            log.info("Found change - {}", change.toString());
        }

        // 更新相应的bean的属性值，主要是存在@ConfigurationProperties注解的bean
        this.applicationContext.publishEvent(new EnvironmentChangeEvent(changeEvent.changedKeys()));
    }
}
