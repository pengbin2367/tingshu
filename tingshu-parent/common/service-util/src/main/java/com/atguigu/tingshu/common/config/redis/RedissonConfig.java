package com.atguigu.tingshu.common.config.redis;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.config.SingleServerConfig;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties("spring.data.redis")
public class RedissonConfig {

    private String host;
    private String password;
    private String port;
    private int timeout = 3000;
    private static String ADDRESS_PREFIX = "redis://";

    @Bean
    RedissonClient redissonSingle() {
        Config config = new Config();
        if (StringUtils.isEmpty(host)) {
            throw new RuntimeException("host is empty");
        }
        SingleServerConfig singleServerConfig = config.useSingleServer()
                .setAddress(ADDRESS_PREFIX + host + ":" + port)
                .setTimeout(timeout);
        if (StringUtils.isNotEmpty(password)) {
            singleServerConfig.setPassword(password);
        }
        return Redisson.create(config);
    }
}
