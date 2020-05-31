package net.aty.springboot.actuator;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import org.springframework.stereotype.Component;

/**
 * 自定义metrics只需要实现MeterBinder接口,然后托管给spring即可,springboot会自动注册到meterRegistry
 * <p>
 * 增加micrometer-registry-prometheus依赖后,springboot会自动配置一个PrometheusMeterRegistry
 * 和CollectorRegistry来收集和输出格式化的metrics数据，使得Prometheus服务器可以爬取
 * <p>
 * PrometheusMetricsExportAutoConfiguration和MeterRegistryPostProcessor和PrometheusScrapeEndpoint
 * <p>
 * MeterRegistryConfigurer.configure()将PrometheusMeterRegistry(bean)对象注入到Metrics.globalRegistry中
 */
@Component
public class GameOrderMetric implements MeterBinder {

    private volatile double value;

    @Override
    public void bindTo(MeterRegistry registry) {
        // 执行顺序比InitializingBean优先,这样设置的话,所有metric都会带上这些tags
        registry.config().commonTags("application", "cdo-pay-rpc");
        Gauge.builder("game.order.count.way1", () -> value)
                .strongReference(true)
                .tag("channel", "store")
                .register(registry);
    }

    public void setOrderCount(double value) {
        this.value = value;
    }
}
