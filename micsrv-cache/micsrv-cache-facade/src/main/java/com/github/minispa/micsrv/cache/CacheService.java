package com.github.minispa.micsrv.cache;

public interface CacheService {

    Object get(String key);

    void add(String key, Object object);

}
