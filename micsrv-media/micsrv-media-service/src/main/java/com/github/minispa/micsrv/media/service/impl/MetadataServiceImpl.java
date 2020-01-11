package com.github.minispa.micsrv.media.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.github.minispa.micsrv.cache.CacheService;
import com.github.minispa.micsrv.media.model.Metadata;
import com.github.minispa.micsrv.media.service.FFprobeProcService;
import com.github.minispa.micsrv.media.service.MetadataService;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Reference;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Objects;

@Slf4j
@Service
public class MetadataServiceImpl implements MetadataService {

    @Reference
    private CacheService hashMapCacheService;
    @Autowired
    private FFprobeProcService fFprobeProcService;

    @Override
    public List<Metadata> getMetadata(String filePath) {
        log.info("getMetadata - filePath: {}", filePath);
        List<Metadata> metadata = (List<Metadata>) hashMapCacheService.get(filePath);
        if(Objects.isNull(metadata) && !metadata.isEmpty()) {
            metadata = fFprobeProcService.getMetadata(filePath);
            hashMapCacheService.add(filePath, metadata);
        }
        return metadata;
    }

}
