package net.aty.springboot.core;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("classpath:user.properties")
public class UserConfiguration {

    @Bean
    @ConfigurationProperties(prefix = "user.aty")
    public UserProperties userProperties() {
        return new UserProperties();
    }
}
