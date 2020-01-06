package com.github.minispa.micsrv;

import com.alibaba.fastjson.JSONObject;
import com.github.minispa.MDCTraceHelper;
import com.github.minispa.micsrv.mq.MessageSender;
import com.github.minispa.micsrv.mq.MockMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.apache.rocketmq.client.producer.SendResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.UUID;

@Slf4j
@SpringBootApplication
@EnableDubbo
public class ApplicationMain {

    @Autowired
    private MessageSender messageSender;

    @Bean
    public CommandLineRunner commandLineRunner() {
        return (args) -> {
            MockMessage traceMessage = new MockMessage();
            traceMessage.setFilePath("C:\\Users\\Mr.Y\\Videos\\filehub\\origin.mp4");
            traceMessage.setTimestamp(System.currentTimeMillis());
            final SendResult sendResult = messageSender.send("stock_topic", traceMessage);
            log.info("sendResult：{}", sendResult);
        };
    }

    public static void main(String[] args) {
        SpringApplication.run(ApplicationMain.class, args);
    }

//    public static class TriggerCommand implements CommandLineRunner {
//        @Override
//        public void run(String... args) throws Exception {
//
//        }
//    }

}