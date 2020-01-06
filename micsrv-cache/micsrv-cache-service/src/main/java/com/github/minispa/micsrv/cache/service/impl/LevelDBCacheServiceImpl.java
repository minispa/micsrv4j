package com.github.minispa.micsrv.cache.service.impl;

import com.alibaba.fastjson.JSON;
import com.github.minispa.micsrv.cache.CacheService;
import com.google.common.io.Closeables;
import lombok.SneakyThrows;
import org.apache.dubbo.config.annotation.Service;
import org.iq80.leveldb.CompressionType;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.DBFactory;
import org.iq80.leveldb.Options;
import org.iq80.leveldb.impl.Iq80DBFactory;

import java.io.File;

import static org.iq80.leveldb.impl.Iq80DBFactory.asString;
import static org.iq80.leveldb.impl.Iq80DBFactory.bytes;

@Service
public class LevelDBCacheServiceImpl implements CacheService {

    private final File databaseDir = new File(System.getProperty("java.io.tmpdir"), "leveldb");

    private final DBFactory factory = Iq80DBFactory.factory;

    @Override
    @SneakyThrows
    public Object get(String key) {
        Options options = new Options().createIfMissing(true).compressionType(CompressionType.NONE);
        DB db = null;
        try {
            db = factory.open(databaseDir, options);
            return JSON.parse(db.get(bytes(key)));
        } finally {
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
