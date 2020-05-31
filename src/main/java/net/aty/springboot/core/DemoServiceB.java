package net.aty.springboot.core;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.stereotype.Service;

@ConditionalOnClass(name = "net.aty.springboot.core.DemoServiceA")
@Service
public class DemoServiceB {

    @Autowired
    private DemoServiceA demoServiceA;

    public String say() {
        return "hello from b and " + demoServiceA.say();
    }
}
