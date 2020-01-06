package com.github.minispa.micsrv.web.mock;

import com.alibaba.fastjson.JSONArray;
import com.github.minispa.micsrv.media.service.MatedataService;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("mock")
public class MockWeb {

    @Reference
    private MatedataService matedataService;

    @GetMapping("matedata")
    public JSONArray matedata(@RequestParam("filePath") String filePath) {
        log.info("matedata - filePath: {}", filePath);
        final JSONArray matedata = matedataService.getMatedata(filePath);
        log.info("matedate - matedata: {}", matedata);
        return matedata;
    }

}
