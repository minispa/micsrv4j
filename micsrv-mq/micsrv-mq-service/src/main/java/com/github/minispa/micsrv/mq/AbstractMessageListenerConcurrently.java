package com.github.minispa.micsrv.mq;

import com.alibaba.fastjson.JSONObject;

import static com.github.minispa.MDCTraceHelper.*;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.common.protocol.heartbeat.MessageModel;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.List;

@Slf4j
public abstract class AbstractMessageListenerConcurrently<T> implements MessageListenerConcurrently {

    @Value("${rocketmq.namesrvAddr}")
    private String namesrvAddr;

    private final DefaultMQPushConsumer consumer = new DefaultMQPushConsumer();

    @Override
    public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> list, ConsumeConcurrentlyContext consumeConcurrentlyContext) {
        for (int i = 0; i < list.size(); i++) {
            MessageExt messageExt = list.get(i);
            try {
                log.info("consumeMessage - messageExt: {}", messageExt);
                setNewIfAbsent(messageExt.getUserProperty(TraceMark));
                consumeMessage(messageExt);
            } finally {
                clear();
            }
        }
        return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
    }

    public abstract void consumeMessage(MessageExt messageExt);

    protected Class<?> getType() {
        return JSONObject.class;
    }

    public abstract String getConsumerGroup();

    public abstract String subcribeTopic();

    @PostConstruct
    @SneakyThrows
    public void start() {
        log.info("{} consumer start", getConsumerGroup());
        consumer.setNamesrvAddr(namesrvAddr);
        consumer.setConsumerGroup(getConsumerGroup());
        consumer.setMessageModel(MessageModel.CLUSTERING);
        consumer.subscribe(subcribeTopic(), "*");
        consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_FIRST_OFFSET);
        consumer.registerMessageListener(this);
        consumer.start();
    }

    @PreDestroy
    public void shutdown() {
        if (consumer != null) {
            consumer.shutdown();
            log.warn("{} consumer shutdown", getConsumerGroup());
        }
    }
}
