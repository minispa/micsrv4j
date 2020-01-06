package com.github.minispa.micsrv.mq;

import lombok.Data;

import java.io.Serializable;

@Data
public class MockMessage implements Serializable {

    private String filePath;
    private long timestamp;

}
