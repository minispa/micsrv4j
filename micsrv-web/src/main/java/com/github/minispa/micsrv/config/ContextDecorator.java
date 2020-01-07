package com.github.minispa.micsrv.config;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.task.TaskDecorator;

import java.util.Map;

/**
 * @author Mr.Y
 */
@Slf4j
public class ContextDecorator implements TaskDecorator {

    @Override
    public Runnable decorate(Runnable runnable) {
        final Map<String, String> copyOfContextMap = MDC.getCopyOfContextMap();
        log.info("decorate - create copyOfContextMap: {}", JSON.toJSONString(copyOfContextMap));
        return () -> {
            try {
                for (Map.Entry<String, String> entry : copyOfContextMap.entrySet()) {
                    MDC.put(entry.getKey(), entry.getValue());
                }
                runnable.run();
            } finally {
                MDC.clear();
                log.info("decorate - remove copyOfContextMap: {}", JSON.toJSONString(MDC.getCopyOfContextMap()));
            }
        };
    }
}
