package com.so.sorpc.core.registry.so;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import lombok.extern.slf4j.Slf4j;

/**
 * health checker for so registry center
 * @author someecho <linghan.ma@gmail.com>
 * Created on 2024-05-09
 */
@Slf4j
public class SoHealthChecker {

    ScheduledExecutorService consumerExecutor = null;
    ScheduledExecutorService providerExecutor = null;

    public void start() {
      log.info(" ===> [so registry] : start with health checker.");
      consumerExecutor = Executors.newScheduledThreadPool(1);
      providerExecutor = Executors.newScheduledThreadPool(1);
    }

    public void stop() {
      log.info(" ===> [so registry] : stop with health checker.");
      gracefulShutdown(consumerExecutor);
      gracefulShutdown(providerExecutor);
    }

    private void gracefulShutdown(ScheduledExecutorService executor) {
        executor.shutdown();
        try {
            executor.awaitTermination(1000, TimeUnit.MILLISECONDS);
            if (!executor.isTerminated()) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
            //ignore
        }
    }

    /**
     * use for consumer subscribe change event check consumer health
     * every 5 second check
     * @param callback
     */
    public void consumerCheck(Callback callback) {
        consumerExecutor.scheduleAtFixedRate(() -> {
            try {
                callback.call();
            } catch (Exception e) {
                log.error(" ===> [so registry] : consumer check error.", e);
            }
        }, 1, 5, TimeUnit.SECONDS);
    }

    /**
     * every 5 second check
     * @param callback
     */
    public void providerCheck(Callback callback) {
        providerExecutor.scheduleAtFixedRate(() -> {
            try {
                callback.call();
            } catch (Exception e) {
                log.error(" ===> [so registry] : provider check error.", e);
            }
        }, 1, 5, TimeUnit.SECONDS);
    }

    public interface Callback {
        void call() throws Exception;
    }
}
