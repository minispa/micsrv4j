package com.github.minispa.micsrv.config;

import com.dangdang.ddframe.job.api.simple.SimpleJob;
import com.dangdang.ddframe.job.config.JobCoreConfiguration;
import com.dangdang.ddframe.job.config.simple.SimpleJobConfiguration;
import com.dangdang.ddframe.job.executor.ShardingContexts;
import com.dangdang.ddframe.job.lite.api.JobScheduler;
import com.dangdang.ddframe.job.lite.api.listener.ElasticJobListener;
import com.dangdang.ddframe.job.lite.config.LiteJobConfiguration;
import com.dangdang.ddframe.job.lite.spring.api.SpringJobScheduler;
import com.dangdang.ddframe.job.reg.zookeeper.ZookeeperConfiguration;
import com.dangdang.ddframe.job.reg.zookeeper.ZookeeperRegistryCenter;
import com.google.common.base.Stopwatch;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StopWatch;

@Configuration
public class ElasticJobConfig {

    @Value("${elasticjob.regCenter.serverList}")
    private String serverList;
    @Value("${elasticjob.regCenter.namespace}")
    private String namespace;

    @Bean(initMethod = "init")
    public ZookeeperRegistryCenter regCenter(
/*
            @Value("${elasticjob.regCenter.serverList}") final String serverList,
            @Value("${elasticjob.regCenter.namespace}")  final String namespace
*/
    ) {
        return new ZookeeperRegistryCenter(new ZookeeperConfiguration(serverList, namespace));
    }

    /**
     * 监听器
     *
     * @return
     */
    @Bean
    public ElasticJobListener elasticJobListener() {
        return new TimeWatcherElasticJobListener();
    }

    @Slf4j
    public static class TimeWatcherElasticJobListener implements ElasticJobListener {

        volatile StopWatch stopWatch;

        TimeWatcherElasticJobListener() {
            log.info("Invoke TimeWatcherElasticJobListener constructor.");
        }

        @Override
        public void beforeJobExecuted(ShardingContexts shardingContexts) {
            log.info("begin job [{}]", shardingContexts.getJobName());
            stopWatch = new StopWatch(shardingContexts.getJobName() + "-" + shardingContexts.getShardingTotalCount() + "-" + shardingContexts.getJobEventSamplingCount() + "-" + shardingContexts.getCurrentJobEventSamplingCount());
            stopWatch.start(shardingContexts.getJobName());
        }

        @Override
        public void afterJobExecuted(ShardingContexts shardingContexts) {
            log.info("end job [{}]", shardingContexts.getJobName());
            stopWatch.stop();
            log.info("job exec: {}", stopWatch.prettyPrint());
            stopWatch = null;
        }
    }

    /**
     * 配置任务详细信息
     *
     * @param jobClass
     * @param cron
     * @param shardingTotalCount
     * @param shardingItemParameters
     * @return
     */
    private LiteJobConfiguration getLiteJobConfiguration(final Class<? extends SimpleJob> jobClass,
                                                         final String cron,
                                                         final int shardingTotalCount,
                                                         final String shardingItemParameters) {
        return LiteJobConfiguration.newBuilder(new SimpleJobConfiguration(
                JobCoreConfiguration.newBuilder(jobClass.getName(), cron, shardingTotalCount)
                        .shardingItemParameters(shardingItemParameters).build()
                , jobClass.getCanonicalName())
        ).overwrite(true).build();
    }

    @Bean(initMethod = "init")
    public JobScheduler simpleJobScheduler(final SimpleJob simpleJob,
                                           @Value("${elasticjob.stockSimpleJob.cron}") final String cron,
                                           @Value("${elasticjob.stockSimpleJob.shardingTotalCount}") final int shardingTotalCount,
                                           @Value("${elasticjob.stockSimpleJob.shardingItemParameters}") final String shardingItemParameters) {

        return new SpringJobScheduler(simpleJob, regCenter(),
                getLiteJobConfiguration(simpleJob.getClass(), cron, shardingTotalCount, shardingItemParameters),
                elasticJobListener());
    }


}
