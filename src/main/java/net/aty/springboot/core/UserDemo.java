package net.aty.springboot.core;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.stereotype.Component;

@Component
public class UserDemo implements InitializingBean {
    @Autowired
    private UserProperties userProperties;
    @Autowired
    private RedisProperties redisProperties;

    @Override
    public void afterPropertiesSet() throws Exception {
        System.out.println("userProperties..." + userProperties);
        System.out.println("redisProperties..." + redisProperties);
    }
}
