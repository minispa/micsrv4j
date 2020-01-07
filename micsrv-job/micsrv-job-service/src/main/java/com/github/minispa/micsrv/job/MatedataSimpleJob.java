package com.github.minispa.micsrv.job;

import com.alibaba.fastjson.JSONArray;
import com.dangdang.ddframe.job.api.ShardingContext;
import com.github.minispa.elasticjob.AbstractSimpleJob;
import com.github.minispa.micsrv.cache.CacheService;
import com.github.minispa.micsrv.config.ElasticJobScheduler;
import com.github.minispa.micsrv.media.service.MatedataService;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Reference;

@Slf4j
@ElasticJobScheduler
public class MatedataSimpleJob extends AbstractSimpleJob {

    @Reference
    private CacheService hashMapCacheService;
    @Reference
    private MatedataService matedataService;

    @Override
    protected void run(ShardingContext shardingContext) {
        log.info("shardingContext: {}", shardingContext);
        JSONArray jsonArray = matedataService.getMatedata("C:\\Users\\Mr.Y\\Videos\\material\\tail.mp4");
        hashMapCacheService.add("C:\\Users\\Mr.Y\\Videos\\material\\tail.mp4", jsonArray);
    }
}
