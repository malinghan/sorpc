package com.so.sorpc.core.cluster;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.so.sorpc.core.api.Router;
import com.so.sorpc.core.meta.InstanceMeta;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * 灰度路由
 * @author someecho <linghan.ma@gmail.com>
 * Created on 2024-04-03
 */
@Data
@Slf4j
public class GrayRouter implements Router<InstanceMeta> {
    //灰度百分比，通过在发布的过程中对配置中心该参数不断修改来达到灰度发布
    private int grayRatio;
    private final Random random = new Random();

    @Override
    public List<InstanceMeta> choose(List<InstanceMeta> providers) {
        //if providers is empty or size < 2 , return
        if (providers == null || providers.size() < 2) {
            return  providers;
        }
        //start select grey and normal providers
        List<InstanceMeta> grepProviders = new ArrayList<>();
        List<InstanceMeta> normalProviders = new ArrayList<>();
        for (InstanceMeta provider : providers) {
            if ("true".equals(provider.getParameters().get("gray"))) {
                grepProviders.add(provider);
            } else {
                normalProviders.add(provider);
            }
        }
        log.debug(" grayRouter grayNodes/normalNodes,grayRatio ===> {}/{},{}",
                grepProviders.size(), normalProviders.size(), grayRatio);

        //if all grep or all normal return
        if (grepProviders.isEmpty() || normalProviders.isEmpty()) {
            return providers;
        }

        if (grayRatio <= 0) {
            return normalProviders;
        } else if (grayRatio >= 100) {
            return grepProviders;
        } else {
            if(random.nextInt(100) < grayRatio) {
                log.debug(" grayRouter grayNodes ===> {}", grepProviders);
                return grepProviders;
            } else {
                log.debug(" grayRouter normalNodes ===> {}", normalProviders);
                return normalProviders;
            }
        }
    }

    public GrayRouter(int grayRatio) {
        this.grayRatio = grayRatio;
    }
}
