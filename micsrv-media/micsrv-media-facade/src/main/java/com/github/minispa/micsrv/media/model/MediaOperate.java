package com.github.minispa.micsrv.media.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MediaOperate implements Serializable {

    private String operateId;
    private List<String> commands;
    private Map<String, String> args = new HashMap<>();
    private String callback;
    private CallbackType callbackType;
    private Integer status;

    public enum CallbackType {
        RocketMQ, Kafka, HttpUrl;
    }

}
