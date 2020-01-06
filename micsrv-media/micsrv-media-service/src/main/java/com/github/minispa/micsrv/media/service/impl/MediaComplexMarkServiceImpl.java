package com.github.minispa.micsrv.media.service.impl;

import com.github.minispa.micsrv.cache.CacheService;
import com.github.minispa.micsrv.media.handler.ComplexMarkHandler;
import com.github.minispa.micsrv.media.service.MediaComplexMarkService;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Reference;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
@Service
public class MediaComplexMarkServiceImpl implements MediaComplexMarkService {

    @Reference
    private CacheService levelDBCacheService;
    @Autowired
    private ComplexMarkHandler complexMarkHandler;

    @Override
    public String complexMark(String filePath) {
        log.info("complexMark - filePath: {}, levelDBCacheService: {}", filePath, levelDBCacheService);
        return complexMarkHandler.handle(filePath);
    }
}
