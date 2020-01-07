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
import com.github.minispa.micsrv.job.ComplexMarkSimpleJob;
import com.github.minispa.micsrv.job.MatedataSimpleJob;
import com.github.minispa.micsrv.media.service.MatedataService;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.annotation.AnnotationBeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.StopWatch;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@Configuration
@EnableConfigurationProperties(ElasticJobProperties.class)
public class ElasticJobConfig {

    @Value("${elasticjob.regCenter.serverList}")
    private String serverList;
    @Value("${elasticjob.regCenter.namespace}")
    private String namespace;

    @Bean(initMethod = "init")
    public ZookeeperRegistryCenter regCenter() {
        return new ZookeeperRegistryCenter(new ZookeeperConfiguration(serverList, namespace));
    }

    /**
     * 监听器
     *
     * @return
     */
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
    public JobScheduler matedataJobScheduler(final SimpleJob matedataSimpleJob,
                                          @Value("${elasticjob.jobScheduler.matedataSimpleJob.cron}") final String cron,
                                          @Value("${elasticjob.jobScheduler.matedataSimpleJob.shardingTotalCount}") final int shardingTotalCount,
                                          @Value("${elasticjob.jobScheduler.matedataSimpleJob.shardingItemParameters}") final String shardingItemParameters) {

        return new SpringJobScheduler(matedataSimpleJob, regCenter(),
                getLiteJobConfiguration(matedataSimpleJob.getClass(), cron, shardingTotalCount, shardingItemParameters),
                elasticJobListener());
    }

    @Bean(initMethod = "init")
    public JobScheduler complexMarkJobScheduler(final SimpleJob complexMarkSimpleJob,
                                           @Value("${elasticjob.jobScheduler.complexMarkSimpleJob.cron}") final String cron,
                                           @Value("${elasticjob.jobScheduler.complexMarkSimpleJob.shardingTotalCount}") final int shardingTotalCount,
                                           @Value("${elasticjob.jobScheduler.complexMarkSimpleJob.shardingItemParameters}") final String shardingItemParameters) {

        return new SpringJobScheduler(complexMarkSimpleJob, regCenter(),
                getLiteJobConfiguration(complexMarkSimpleJob.getClass(), cron, shardingTotalCount, shardingItemParameters),
                elasticJobListener());
    }

}
