package com.github.minispa.micsrv.mq.consumer;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.minispa.micsrv.mq.AbstractMessageListenerConcurrently;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.common.message.MessageExt;
import org.springframework.stereotype.Component;

@Slf4j
//@Component
public class RocketMQSysTraceConsumer extends AbstractMessageListenerConcurrently<JSONObject> {

    @Override
    public void consumeMessage(MessageExt messageExt) {
        log.info("message - {}", JSON.toJSONString(messageExt.getBody()));
    }

    @Override
    public String getConsumerGroup() {
        return "RocketMQSysTraceConsumerGroup";
    }

    @Override
    public String subcribeTopic() {
        return "RMQ_SYS_TRACE_TOPIC";
    }
}
