package net.aty.springboot.actuator;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Metrics;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GameOrderService implements InitializingBean {

    @Autowired
    private MeterRegistry meterRegistry;

    @Autowired
    private GameOrderMetric gameOrderMetric;

    @Override
    public void afterPropertiesSet() throws Exception {
        new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Gauge.builder("game.order.count.way2", Math::random)
                        .strongReference(true)
                        .tag("channel", "game")
                        .register(meterRegistry);
                gameOrderMetric.setOrderCount(Math.random());
                MetricReportUtils.report();
            }
        }).start();
    }
}
