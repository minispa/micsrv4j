package com.github.minispa.micsrv.media.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.github.minispa.micsrv.media.model.Metadata;
import com.github.minispa.micsrv.media.service.FFprobeProcService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Service
public class FFprobeProcServiceImpl implements FFprobeProcService {

    @Value("${ffprobe.install.dir:ffprobe}")
    private String ffprobe;

    @Override
    public JSONArray getStreams(String filePath) {
        log.info("getStreams - filePath :{}", filePath);
        return getStreamsObject(filePath).getJSONArray("streams");
    }

    @Override
    public List<Metadata> getMetadata(String absolutePath) {
        log.info("getMetadata - absolutePath: {}", absolutePath);
        final JSONArray streams = getStreams(absolutePath);
        return streams.toJavaList(Metadata.class);
    }

    private JSONObject getStreamsObject(String absolutePath) {
        if(StringUtils.isNotBlank(absolutePath)) {
            try {
                String[] commands = {ffprobe, "-loglevel", "error", "-show_streams", "-print_format", "json", absolutePath};
                ProcessBuilder processBuilder = new ProcessBuilder(commands);
                Process process = processBuilder.start();
                String streamsText = IOUtils.toString(process.getInputStream(), "utf-8");
                int exitValue = process.waitFor();
                log.info("getStreamsObject - exitValue: {}, streamsText: {}", exitValue, streamsText);
                return JSON.parseObject(streamsText);
            } catch (Exception e) {
                log.error("[" + absolutePath + "] getStreams fail.", e);
            }
        }
        return new JSONObject();
    }
}
