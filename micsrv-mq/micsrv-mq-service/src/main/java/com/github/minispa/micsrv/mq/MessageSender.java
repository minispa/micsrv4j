package com.github.minispa.micsrv.mq;

import static com.github.minispa.MDCTraceHelper.*;

import com.alibaba.fastjson.JSON;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.MQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MessageSender {

    @Autowired
    private MQProducer defaultMQProducer;

    @SneakyThrows
    public <T> SendResult send(String topic, T message) {
        Message sendMsg = new Message(topic, JSON.toJSONBytes(message));
        sendMsg.putUserProperty(TraceMark, getNewIfAbsent());
        return defaultMQProducer.send(sendMsg);
    }

}
