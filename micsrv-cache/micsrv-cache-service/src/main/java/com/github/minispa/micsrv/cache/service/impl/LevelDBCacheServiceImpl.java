package com.github.minispa.micsrv.cache.service.impl;

import com.alibaba.fastjson.JSON;
import com.github.minispa.micsrv.cache.CacheService;
import com.google.common.io.Closeables;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Service;
import org.iq80.leveldb.CompressionType;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.DBFactory;
import org.iq80.leveldb.Options;
import org.iq80.leveldb.impl.Iq80DBFactory;
import org.springframework.scheduling.annotation.Async;

import java.io.File;
import java.util.concurrent.TimeUnit;

import static org.iq80.leveldb.impl.Iq80DBFactory.asString;
import static org.iq80.leveldb.impl.Iq80DBFactory.bytes;

@Slf4j
@Service(group = "levelDBCacheServiceImpl")
public class LevelDBCacheServiceImpl implements CacheService {

    private final File databaseDir = new File(System.getProperty("java.io.tmpdir"), "leveldb");

    private final DBFactory factory = Iq80DBFactory.factory;

    @Override
    @SneakyThrows
    public Object get(String key) {
        log.info("async 2");
        Options options = new Options().createIfMissing(true).compressionType(CompressionType.NONE);
        DB db = null;
        try {
            db = factory.open(databaseDir, options);
            return JSON.parse(db.get(bytes(key)) == null ? "0" : "1");
        } catch (Exception e) {
            log.error("get error", e);
            return null;
        } finally {
            log.info("async 3");
            if(db != null) {
                db.close();
            }
        }
    }

    @Override
    @SneakyThrows
    public void add(String key, Object object) {
        final byte[] bytes = JSON.toJSONBytes(object);
        Options options = new Options().createIfMissing(true).compressionType(CompressionType.NONE);
        DB db = null;
        try {
            db = factory.open(databaseDir, options);
            db.put(bytes(key), bytes);
        } finally {
            if(db != null) {
                db.close();
            }
        }
    }
}
