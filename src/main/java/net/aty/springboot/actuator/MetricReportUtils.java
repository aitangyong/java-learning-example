package net.aty.springboot.actuator;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.Metrics;

public class MetricReportUtils {

    public static void report() {
        Gauge.builder("game.order.count.way3", Math::random)
                .strongReference(true)
                .tag("channel", "theme")
                .register(Metrics.globalRegistry);

    }
}
