package com.github.minispa.micsrv.mq;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.minispa.MDCTraceHelper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.common.protocol.heartbeat.MessageModel;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public abstract class AbstractMessageListenerConcurrently<T> implements MessageListenerConcurrently {

    @Value("${rocketmq.namesrvAddr}")
    private String namesrvAddr;

    private final DefaultMQPushConsumer consumer = new DefaultMQPushConsumer();

    @Override
    public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> list, ConsumeConcurrentlyContext consumeConcurrentlyContext) {
        AtomicInteger counter = new AtomicInteger();
        for (MessageExt messageExt : list) {
            String traceMark = messageExt.getUserProperty(MDCTraceHelper.TraceMark);
            boolean clear = false;
            if (StringUtils.isBlank(traceMark)) {
                traceMark = MDCTraceHelper.newTraceMark();
                clear = true;
            }
            MDC.put(MDCTraceHelper.TraceMark, traceMark);
            try {
                consumeMessage(messageExt);
                consumeConcurrentlyContext.setAckIndex(counter.incrementAndGet());
            } catch (Exception e) {

            } finally {
                if (clear) {
                    MDC.clear();
                }
            }
            final T bean = JSON.parseObject(messageExt.getBody(), getType());
            log.info("messageExt -> T: {} -> {}", getType(), JSON.toJSONString(bean));
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
