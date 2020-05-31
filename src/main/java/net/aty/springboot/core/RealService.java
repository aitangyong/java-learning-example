package net.aty.springboot.core;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.sound.midi.Soundbank;
import java.util.Optional;


@Component
public class RealService implements InitializingBean {

    @Autowired(required = false)
    private DemoServiceB demoServiceB;

    @Autowired
    private Optional<DemoServiceB> optionalDemoServiceB;

    @Autowired
    private ObjectProvider<DemoServiceB> provider;

    @Override
    @SuppressWarnings("all")
    public void afterPropertiesSet() throws Exception {
        if (demoServiceB != null) {
            System.out.println(demoServiceB.say());
        }

        if (optionalDemoServiceB.isPresent()) {
            System.out.println(optionalDemoServiceB.get().say());
        }

        if (provider.getIfAvailable() != null) {
            System.out.println(provider.getIfAvailable().say());
        }
    }
}
