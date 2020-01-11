package com.github.minispa.micsrv.media.config;

import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RocketMQConfig {

    @Bean(initMethod = "start", destroyMethod = "shutdown")
    public DefaultMQProducer rocketMQProducer(
            @Value("${rocketmq.namesrvAddr}") final String namesrvAddr,
            @Value("${rocketmq.producerGroup}") final String producerGroup
    ) {
        DefaultMQProducer defaultMQProducer = new DefaultMQProducer();
        defaultMQProducer.setProducerGroup(producerGroup);
        defaultMQProducer.setNamesrvAddr(namesrvAddr);
        defaultMQProducer.setMaxMessageSize(8 * 1024 * 1024);
        return defaultMQProducer;
    }

}
