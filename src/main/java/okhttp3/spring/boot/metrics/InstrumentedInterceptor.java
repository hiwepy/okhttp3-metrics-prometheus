package okhttp3.spring.boot.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Timer;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.core.annotation.Order;

import java.io.IOException;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * An {@link Interceptor} that monitors the number of submitted, running, and completed network
 * requests. Also, keeps a {@link Timer} for the request duration.
 */
@Order(Integer.MIN_VALUE)
public class InstrumentedInterceptor implements Interceptor {

    private MeterRegistry registry;
    private Collection<Tag> tags;
    private final Counter submitted;
    private final Counter running;
    private final Counter completed;
    private final Timer duration;

    public InstrumentedInterceptor(MeterRegistry registry, Collection<Tag> tags) {
        this.registry = registry;
        this.tags = Objects.isNull(tags) ? Collections.emptyList() : tags;
        this.submitted = registry.counter(OkHttp3Metrics.METRIC_NAME_NETWORK_REQUESTS_SUBMITTED, this.tags);
        this.running = registry.counter(OkHttp3Metrics.METRIC_NAME_NETWORK_REQUESTS_RUNNING, this.tags);
        this.completed = registry.counter(OkHttp3Metrics.METRIC_NAME_NETWORK_REQUESTS_COMPLETED, this.tags);
        this.duration = Timer.builder(OkHttp3Metrics.METRIC_NAME_NETWORK_REQUESTS_DURATION)
                // median and 95th percentile
                .publishPercentiles(0.5, 0.75, 0.95, 0.98, 0.99, 0.999)
                .publishPercentileHistogram()
                .sla(Duration.ofMillis(100))
                .minimumExpectedValue(Duration.ofMillis(1))
                .maximumExpectedValue(Duration.ofSeconds(10))
                .tags(this.tags)
                .register(registry);
    }

    @Override
    public Response intercept(Interceptor.Chain chain) throws IOException {
        // 获取请求
        Request request = chain.request();
        if(Objects.nonNull(request.header(OkHttp3Metrics.OKHTTP3_REQUEST_METRIC_NAME_PREFIX))){
            return chain.proceed(request);
        }
        // 记录请求开始时间
        long start = System.currentTimeMillis();
        // 一次请求计数 +1
        submitted.increment();
        // 当前正在运行的请求数 +1
        running.increment();
        // 获取本次请求对应的度量指标名称
        String metric = MetricNames.name(OkHttp3Metrics.OKHTTP3_REQUEST_METRIC_NAME_PREFIX, request.url().host());
        // 获取度量指标
        List<Tag> jobTags = new ArrayList<>(tags);
        jobTags.add(Tag.of("method", request.method()));
        Timer timer = registry.timer(metric, tags);
        Response response;
        try {
            request = request.newBuilder().addHeader(OkHttp3Metrics.OKHTTP3_REQUEST_METRIC_NAME_PREFIX, metric).build();
            response = chain.proceed(request);
        } finally {
            // 记录本次请求耗时
            timer.record(System.currentTimeMillis() - start, TimeUnit.MILLISECONDS);
            duration.record(System.currentTimeMillis() - start, TimeUnit.MILLISECONDS);
            // 当前正在运行的请求数 -1
            running.increment(-1);
            // 当前已完成的请求数 +1
            completed.increment();
        }
        return response;
    }

}