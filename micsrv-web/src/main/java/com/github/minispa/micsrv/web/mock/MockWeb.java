package com.github.minispa.micsrv.web.mock;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.github.minispa.micsrv.cache.CacheService;
import com.github.minispa.micsrv.clients.AsyncClients;
import com.github.minispa.micsrv.media.service.MatedataService;
import com.google.common.base.Stopwatch;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Reference;
import org.apache.dubbo.rpc.RpcContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

@Slf4j
@RestController
@RequestMapping("mock")
public class MockWeb {

    @Reference
    private MatedataService matedataService;
    @Reference(group = "hashMapCacheServiceImpl")
    private CacheService hashMapCacheService;

    @Autowired
    private AsyncClients asyncClients;


    @GetMapping("matedata")
    public JSONArray matedata(@RequestParam("filePath") String filePath) {
        log.info("matedata - filePath: {}", filePath);
        final JSONArray matedata = matedataService.getMatedata(filePath);
        log.info("matedate - matedata: {}", matedata);
        final Object o = hashMapCacheService.get(filePath);
        log.info("matedata - cache: {}", JSON.toJSONString(o));
        return matedata;
    }

    /**
     * redirect 跳转
     *
     * @param refer
     * @return
     */
    @GetMapping("refer")
    public ModelAndView refer(@RequestParam String refer) {
        return new ModelAndView("redirect:".concat(refer));
    }

    @GetMapping("async-rpc")
    @SneakyThrows
    public Object asyncRpc(@RequestParam("filePath") String filePath) {
        log.info("asyncRpc 1");
        final CompletableFuture<Object> future = RpcContext.getContext().asyncCall(() -> hashMapCacheService.get(filePath));
        final Object o = future.get();
        log.info("asyncRpc 4");
        return o;
    }

    @GetMapping("async-spring")
    @SneakyThrows
    public Object asyncSpring(@RequestParam("filePath") String filePath) {
        Stopwatch stopwatch = Stopwatch.createStarted();
        log.info("asyncSpring 1");
        final Future<Object> objectFuture = asyncClients.asyncCallCacheRpc(filePath);
        log.info("future result: {}", objectFuture.get());
        log.info("asyncSpring 4");
        return "success: " + stopwatch.elapsed(TimeUnit.MILLISECONDS);
    }

}
