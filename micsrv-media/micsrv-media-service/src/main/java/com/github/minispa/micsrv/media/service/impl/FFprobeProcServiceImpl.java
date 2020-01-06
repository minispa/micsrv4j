package com.github.minispa.micsrv.media.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.minispa.micsrv.media.service.FFprobeProcService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Value;

import java.util.Map;

@Slf4j
@Service
public class FFprobeProcServiceImpl implements FFprobeProcService {

    @Value("${ffprobe.install.dir:ffprobe}")
    private String ffprobe;

    @Override
    public JSONArray getStreams(String filePath) {
        log.info("getStreams - filePath :{}", filePath);
        return getMetadata(filePath).getJSONArray("streams");
    }

    private JSONObject getMetadata(String absolutePath) {
        log.info("getMetadata - absolutePath: {}", absolutePath);
        try {
            String[] commands = {ffprobe, "-loglevel", "error", "-show_streams", "-print_format", "json", absolutePath};
            ProcessBuilder processBuilder = new ProcessBuilder(commands);
            Process process = processBuilder.start();
            String metadata = IOUtils.toString(process.getInputStream(), "utf-8");
            int exitValue = process.waitFor();
            log.info("getMetadata - exitValue: {}, commands: {}", exitValue, String.join(" ", commands));
            return JSON.parseObject(metadata);
        } catch (Exception e) {
            log.error("getMetadata - error", e);
            return new JSONObject();
        }
    }
}
