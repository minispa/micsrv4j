package com.github.minispa.micsrv.cache.service.impl;

import com.alibaba.fastjson.JSON;
import com.github.minispa.micsrv.cache.CacheService;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Service;

import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service(group = "hashMapCacheServiceImpl")
public class HashMapCacheServiceImpl implements CacheService {

    public static final ConcurrentHashMap<String, Object> cache = new ConcurrentHashMap<>();

    static {
        cache.put("domain.baidu", "baidu.com");
        cache.put("domain.tmall", "tmall.com");
        cache.put("domain.taobao", "taobao.com");
        cache.put("domain.jd", "jd.com");
        cache.put("domain.kaola", "kaola.com");
    }


    @Override
    public Object get(String key) {
        log.info("getObject - key: {}", key);
        return cache.get(key);
    }

    @Override
    public void add(String key, Object object) {
        log.info("putObject - key: {}, object: {}", key, JSON.toJSONString(object));
        cache.put(key, object);
    }
}
