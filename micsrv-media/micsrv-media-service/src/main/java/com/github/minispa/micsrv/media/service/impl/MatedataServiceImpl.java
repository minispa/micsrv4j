package com.github.minispa.micsrv.media.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.minispa.micsrv.cache.CacheService;
import com.github.minispa.micsrv.media.service.FFprobeProcService;
import com.github.minispa.micsrv.media.service.MatedataService;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Reference;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Objects;

@Slf4j
@Service
public class MatedataServiceImpl implements MatedataService {

    @Reference
    private CacheService hashMapCacheService;
    @Autowired
    private FFprobeProcService fFprobeProcService;

    @Override
    public JSONArray getMatedata(String filePath) {
        log.info("getMatedata - filePath: {}", filePath);
        JSONArray matedata = (JSONArray) hashMapCacheService.get(filePath);
        if(Objects.isNull(matedata)) {
            matedata = fFprobeProcService.getStreams(filePath);
        }
        if(Objects.nonNull(matedata)) {
            hashMapCacheService.add(filePath, matedata);
        }
        return matedata;
    }
}
