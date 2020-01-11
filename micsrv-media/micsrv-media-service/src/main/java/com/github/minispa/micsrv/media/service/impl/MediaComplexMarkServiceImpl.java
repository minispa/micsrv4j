package com.github.minispa.micsrv.media.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.github.minispa.micsrv.cache.CacheService;
import com.github.minispa.micsrv.media.handler.ComplexMarkHandler;
import com.github.minispa.micsrv.media.model.MediaOperate;
import com.github.minispa.micsrv.media.model.Metadata;
import com.github.minispa.micsrv.media.service.FFprobeProcService;
import com.github.minispa.micsrv.media.service.MediaComplexMarkService;
import com.github.minispa.micsrv.media.service.MetadataService;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Reference;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Slf4j
@Service
public class MediaComplexMarkServiceImpl implements MediaComplexMarkService {

    @Reference
    private CacheService levelDBCacheService;
    @Autowired
    private ComplexMarkHandler complexMarkHandler;

    /**
     * 正片水印并合并片尾
     *
     * @param mediaOperate#args(originPath, qrCodeText)
     * @return
     */
    @Override
    public String complexMark(MediaOperate mediaOperate) {
        log.info("complexMark - mediaOperate: {}", JSON.toJSONString(mediaOperate));
        return complexMarkHandler.complexMark(mediaOperate);
    }

    /**
     * 两个视频左右合并
     *
     * @param mediaOperate#args(backgroundPath, foregroundPath)
     * @return
     */
    @Override
    public String complexLRConcat(MediaOperate mediaOperate){
        log.info("complexLRConcat - mediaOperate: {}", JSON.toJSONString(mediaOperate));
        return complexMarkHandler.complexLRConcat(mediaOperate);
    }

    /**
     * 两个视频上下合并
     *
     * @param mediaOperate#args(upPath, downPath)
     * @return
     */
    @Override
    public String complexUDConcat(MediaOperate mediaOperate) {
        return null;
    }
}
