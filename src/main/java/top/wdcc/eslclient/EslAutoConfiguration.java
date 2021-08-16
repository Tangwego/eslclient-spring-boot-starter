package top.wdcc.eslclient;

import org.apache.commons.pool2.impl.AbandonedConfig;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.freeswitch.esl.client.inbound.Client;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.freeswitch.esl.client.inbound.InboundConnectionFailure;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import top.wdcc.eslclient.client.EslClient;
import top.wdcc.eslclient.client.EslClientFactory;
import top.wdcc.eslclient.client.EslClientObjectPool;
import top.wdcc.eslclient.client.EslEventClient;
import top.wdcc.eslclient.config.EslProperties;

/**
 * 自动配置类，创建EslClient所使用的Bean
 */
@EnableScheduling
@Configuration
@ConditionalOnWebApplication
@ConditionalOnClass({EslClient.class, EslEventClient.class, EslClientFactory.class})
@EnableConfigurationProperties({EslProperties.class})
public class EslAutoConfiguration {

    @Autowired
    private EslProperties eslProperties;

    @Autowired
    private ApplicationContext context;

    @Bean
    @ConditionalOnMissingBean
    public EslClient eslClient(){
        // 创建监听客户端
        try {
            EslEventClient.INSTANCE.setApplicationContext(context);
            EslEventClient.INSTANCE.connect(eslProperties.getHost(), eslProperties.getPort(),
                    eslProperties.getPassword(), eslProperties.getTimeoutSec());
        } catch (InboundConnectionFailure e) {
            e.printStackTrace();
        }

        return new EslClient();
    }

    /**
     * 对象池
     * @return EslClientObjectPool
     */
    @Bean
    @ConditionalOnMissingBean
    public EslClientObjectPool objectPool(){
        // 创建连接池
        GenericObjectPoolConfig<Client> config = new GenericObjectPoolConfig<>();
        // 最大连接数
        config.setMaxTotal(eslProperties.getPoolSize());
        // 最大等待时间
        config.setMaxWaitMillis(eslProperties.getTimeoutSec());
        config.setJmxEnabled(false);
        config.setMinIdle(1);
        config.setMaxIdle(2);
        config.setTimeBetweenEvictionRunsMillis(5000);

        AbandonedConfig abandonedConfig = new AbandonedConfig();
        abandonedConfig.setRemoveAbandonedOnMaintenance(true);
        abandonedConfig.setRemoveAbandonedOnBorrow(true);
        abandonedConfig.setRemoveAbandonedTimeout(10);

        EslClientObjectPool objectPool =
                new EslClientObjectPool(new EslClientFactory(eslProperties), config, abandonedConfig);

        return objectPool;
    }

    /**
     * 每10秒检查一次连接
     */
    @Scheduled(cron = "*/10 * * * * ?")
    public void checkConnection(){
        if (!EslEventClient.INSTANCE.isConnected()) {
            try {
                EslEventClient.INSTANCE.connect(eslProperties.getHost(), eslProperties.getPort(),
                        eslProperties.getPassword(), eslProperties.getTimeoutSec());
            } catch (InboundConnectionFailure e) {
                e.printStackTrace();
            }
        }
    }
}
