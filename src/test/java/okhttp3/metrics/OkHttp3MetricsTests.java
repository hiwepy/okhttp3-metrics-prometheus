package okhttp3.metrics;

import io.micrometer.common.KeyValue;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import okhttp3.Cache;
import okhttp3.Call;
import okhttp3.Connection;
import okhttp3.EventListener;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OkHttp3MetricsTests {

    @Test
    void shouldRecordInterceptorSuccessBypassAndFailure() throws Exception {
        SimpleMeterRegistry registry = new SimpleMeterRegistry();
        InstrumentedInterceptor interceptor = new InstrumentedInterceptor(registry, "client", List.of(Tag.of("app", "test")));
        Request request = new Request.Builder().url("http://localhost/test").build();
        Response response = response(request);
        Interceptor.Chain chain = mock(Interceptor.Chain.class);
        when(chain.request()).thenReturn(request);
        when(chain.proceed(request)).thenReturn(response);

        assertSame(response, interceptor.intercept(chain));
        assertEquals(1.0, registry.counter("client" + InstrumentedInterceptor.METRIC_NAME_NETWORK_REQUESTS_SUBMITTED,
                "app", "test").count());
        assertEquals(1.0, registry.counter("client" + InstrumentedInterceptor.METRIC_NAME_NETWORK_REQUESTS_COMPLETED,
                "app", "test").count());

        Request bypass = request.newBuilder().header(OkHttp3Metrics.OKHTTP3_REQUEST_METRIC_NAME_PREFIX, "skip").build();
        when(chain.request()).thenReturn(bypass);
        when(chain.proceed(bypass)).thenReturn(response);
        assertSame(response, interceptor.intercept(chain));

        when(chain.request()).thenReturn(request);
        when(chain.proceed(request)).thenThrow(new IOException("boom"));
        assertThrows(IOException.class, () -> interceptor.intercept(chain));
        assertEquals(2.0, registry.counter("client" + InstrumentedInterceptor.METRIC_NAME_NETWORK_REQUESTS_COMPLETED,
                "app", "test").count());
    }

    @Test
    void shouldRecordAndDelegateAllLifecycleEvents() throws Exception {
        SimpleMeterRegistry registry = new SimpleMeterRegistry();
        EventListener delegate = mock(EventListener.class);
        InstrumentedEventListener listener = new InstrumentedEventListener(registry, delegate);
        Call call = mock(Call.class);
        Connection connection = mock(Connection.class);
        Request request = new Request.Builder().url("http://localhost/test").build();
        Response response = response(request);
        InetSocketAddress address = new InetSocketAddress("127.0.0.1", 8080);
        IOException failure = new IOException("boom");

        listener.callStart(call);
        listener.dnsStart(call, "localhost");
        listener.dnsEnd(call, "localhost", List.of(InetAddress.getLoopbackAddress()));
        listener.connectStart(call, address, Proxy.NO_PROXY);
        listener.secureConnectStart(call);
        listener.secureConnectEnd(call, null);
        listener.connectEnd(call, address, Proxy.NO_PROXY, Protocol.HTTP_1_1);
        listener.connectFailed(call, address, Proxy.NO_PROXY, Protocol.HTTP_1_1, failure);
        listener.connectionAcquired(call, connection);
        listener.connectionReleased(call, connection);
        listener.requestHeadersStart(call);
        listener.requestHeadersEnd(call, request);
        listener.requestBodyStart(call);
        listener.requestBodyEnd(call, 12L);
        listener.requestFailed(call, failure);
        listener.responseHeadersStart(call);
        listener.responseHeadersEnd(call, response);
        listener.responseBodyStart(call);
        listener.responseBodyEnd(call, 24L);
        listener.responseFailed(call, failure);
        listener.callEnd(call);
        listener.callFailed(call, failure);

        assertEquals(1.0, registry.counter(OkHttp3Metrics.METRIC_NAME_CALLS_STARTED).count());
        assertEquals(12.0, registry.summary(OkHttp3Metrics.METRIC_NAME_REQUESTS_BODY_BYTES).totalAmount());
        assertEquals(24.0, registry.summary(OkHttp3Metrics.METRIC_NAME_RESPONSES_BODY_BYTES).totalAmount());
        verify(delegate).callStart(call);
        verify(delegate).callFailed(call, failure);
    }

    @Test
    void shouldBuildInstrumentedClientsAndMapUrls() {
        SimpleMeterRegistry registry = new SimpleMeterRegistry();
        OkHttpClient raw = new OkHttpClient();
        List<java.util.function.BiFunction<Request, Response, KeyValue>> tags =
                List.of((request, response) -> KeyValue.of("status", String.valueOf(response.code())));

        OkHttpClient first = InstrumentedOkHttpClients.create(registry);
        OkHttpClient second = InstrumentedOkHttpClients.create(registry, raw);
        OkHttpClient third = InstrumentedOkHttpClients.create(registry, raw, true);
        OkHttpClient fourth = InstrumentedOkHttpClients.create(registry, raw, UrlMapperEnum.FULL_URL, true);
        OkHttpClient fifth = InstrumentedOkHttpClients.create(registry, raw, tags, UrlMapperEnum.ENCODED_PATH, false);
        OkHttpClient configured = InstrumentedOkHttpClients.create(registry, raw,
                Map.of("app", "test"), List.of("tenant"), tags, UrlMapperEnum.TOP_PRIVATE_DOMAIN, true);

        assertEquals(2, configured.networkInterceptors().size());
        assertNotNull(configured.eventListenerFactory());
        assertEquals("/a", UrlMapperEnum.ENCODED_PATH.get().apply(
                new Request.Builder().url("https://www.example.com/a").build()));
        assertTrue(UrlMapperEnum.FULL_URL.get().apply(
                new Request.Builder().url("https://www.example.com/a").build()).contains("example.com"));
        assertEquals("example.com", UrlMapperEnum.TOP_PRIVATE_DOMAIN.get().apply(
                new Request.Builder().url("https://www.example.com/a").build()));
        assertTrue(UrlMapperEnum.ENCODED_PATH.equals(UrlMapperEnum.ENCODED_PATH));
        assertEquals("Encoded Path ", UrlMapperEnum.ENCODED_PATH.getName());

        List.of(first, second, third, fourth, fifth, configured, raw).forEach(this::shutdown);
    }

    @Test
    void shouldBindDispatcherCacheAndUtilityMetrics(@TempDir Path tempDir) throws Exception {
        SimpleMeterRegistry registry = new SimpleMeterRegistry();
        Cache cache = new Cache(tempDir.toFile(), 1024L);
        OkHttpClient client = new OkHttpClient.Builder().cache(cache).build();

        new OkHttpDispatcherMetrics(client).bindTo(registry);
        new OkHttpDispatcherMetrics(client, "custom").bindTo(registry);
        new OkHttpDispatcherMetrics(client, "tagged", List.of(Tag.of("app", "test"))).bindTo(registry);
        new OkHttpCacheMetrics(client).bindTo(registry);
        new OkHttpCacheMetrics(client, "cache").bindTo(registry);
        new OkHttpCacheMetrics(client, "tagged.cache", List.of(Tag.of("app", "test"))).bindTo(registry);

        assertNotNull(registry.find(OkHttp3Metrics.OKHTTP3_POOL_METRIC_NAME_PREFIX
                + OkHttpDispatcherMetrics.METRIC_NAME_DISPATCHER_MAX_REQUESTS).meter());
        assertNotNull(registry.find("cache" + OkHttpCacheMetrics.METRIC_NAME_CACHE_CURRENT_SIZE).gauge());
        assertEquals("a.b.c", MetricNames.name("a", null, "", "b", "c"));
        assertTrue(MetricNames.name(OkHttp3MetricsTests.class, "metric").endsWith("OkHttp3MetricsTests.metric"));
        assertEquals("a", MetricNames.name("a", (String[]) null));

        cache.close();
        shutdown(client);
    }

    @Test
    void shouldConstructNestedListenerAndTagHandler() {
        EventListener listener = new EventListener() { };
        assertNotNull(new NestedEventListener(List.of(listener)));
        OKhttp3MetricsSpecificTagHandler handler = () -> (request, response) -> KeyValue.of("code", "200");
        assertEquals("200", handler.getHandler().apply(
                new Request.Builder().url("http://localhost").build(),
                response(new Request.Builder().url("http://localhost").build())).getValue());
    }

    private Response response(Request request) {
        return new Response.Builder()
                .request(request)
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .body(ResponseBody.create("ok", MediaType.get("text/plain")))
                .build();
    }

    private void shutdown(OkHttpClient client) {
        client.dispatcher().cancelAll();
        client.connectionPool().evictAll();
        client.dispatcher().executorService().shutdown();
    }
}
