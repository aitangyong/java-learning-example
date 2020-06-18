package net.aty.springboot.core;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UserDemo implements InitializingBean {
    @Autowired
    private UserProperties userProperties;

    @Override
    public void afterPropertiesSet() throws Exception {
        System.out.println("userProperties..." + userProperties);
    }
}