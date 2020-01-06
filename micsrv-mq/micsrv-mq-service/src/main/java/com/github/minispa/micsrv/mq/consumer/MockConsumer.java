package com.github.minispa.micsrv.mq.consumer;

import com.github.minispa.MDCTraceHelper;
import com.github.minispa.micsrv.media.service.MediaComplexMarkService;
import com.github.minispa.micsrv.mq.AbstractMessageListenerConcurrently;
import com.github.minispa.micsrv.mq.MockMessage;
import jdk.nashorn.internal.ir.annotations.Reference;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.remoting.common.RemotingHelper;
import org.springframework.stereotype.Component;


@Slf4j
@Component
public class MockConsumer extends AbstractMessageListenerConcurrently<MockMessage> {

    @Reference
    private MediaComplexMarkService mediaComplexMarkService;

    @Override
    public void consumeMessage(MessageExt messageExt) {
        log.info("messageExt: {}", messageExt.getUserProperty(MDCTraceHelper.TraceMark));
        mediaComplexMarkService.complexMark(new String(messageExt.getBody()));
    }

    @Override
    public Class<?> getType() {
        return MockMessage.class;
    }

    @Override
    public String getConsumerGroup() {
        return "mock_consumer_group";
    }

    @Override
    public String subcribeTopic() {
        return "mock_topic";
    }
}
