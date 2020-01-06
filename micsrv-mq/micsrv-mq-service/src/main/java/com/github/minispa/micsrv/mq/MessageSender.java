package com.github.minispa.micsrv.mq;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.github.minispa.MDCTraceHelper;
import com.github.minispa.MDCTraceHelper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.producer.MQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
public class MessageSender {

    @Autowired
    private MQProducer defaultMQProducer;

    @SneakyThrows
    public <T> SendResult send(String topic, T message) {
        Message sendMsg = new Message(topic, JSON.toJSONBytes(message));
        String traceId = MDC.get(MDCTraceHelper.TraceMark);
        boolean clear = false;
        if(StringUtils.isBlank(traceId)) {
            traceId = MDCTraceHelper.newTraceMark();
            clear = true;
        }
        sendMsg.putUserProperty(MDCTraceHelper.TraceMark, traceId);
        try {
            return defaultMQProducer.send(sendMsg);
        } finally {
            if(clear) {
                MDC.clear();
            }
        }
    }

}
