package com.github.minispa.micsrv.web.media;

import com.github.minispa.micsrv.media.service.MediaComplexMarkService;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("media")
public class MediaWeb {

    @Reference
    private MediaComplexMarkService mediaComplexMarkService;

    @PostMapping("complex-mark")
    public String complexMark(@RequestParam String filePath) {
        log.info("complexMark - filePath: {}", filePath);
        return mediaComplexMarkService.complexMark(filePath);
    }

}
