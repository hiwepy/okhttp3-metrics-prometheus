/*
 * Copyright 2015 Ras Kasa Williams
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package okhttp3.spring.boot.metrics;

import io.micrometer.common.KeyValue;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.binder.okhttp3.DefaultOkHttpObservationConvention;
import io.micrometer.core.instrument.binder.okhttp3.OkHttpMetricsEventListener;
import io.micrometer.core.instrument.binder.okhttp3.OkHttpObservationConvention;
import io.micrometer.core.instrument.binder.okhttp3.OkHttpObservationInterceptor;
import io.micrometer.observation.ObservationRegistry;
import okhttp3.*;
import org.springframework.util.CollectionUtils;

import javax.net.SocketFactory;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSocketFactory;
import java.net.Proxy;
import java.net.ProxySelector;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

/** Wraps an {@link OkHttpClient} in order to provide data about its internals. */
final class InstrumentedOkHttpClient extends OkHttpClient {

  private final MeterRegistry registry;
  private OkHttpClient rawClient;

  private Collection<Tag> extraTags;

  private Collection<KeyValue> kvTags;

  List<BiFunction<Request, Response, KeyValue>> contextSpecificTags;

  List<String> requestTagKeys;

  UrlMapperEnum urlMapper;

  boolean includeHostTag;

  InstrumentedOkHttpClient(MeterRegistry registry,
                           OkHttpClient rawClient,
                           Map<String, String > extraTagMap,
                           List<String> requestTagKeys,
                           List<BiFunction<Request, Response, KeyValue>> contextSpecificTags,
                           UrlMapperEnum urlMapper,
                           boolean includeHostTag) {
    this.rawClient = rawClient;
    this.registry = registry;
    this.extraTags = CollectionUtils.isEmpty(extraTagMap) ? new ArrayList<>()  : extraTagMap
            .entrySet().stream().map(e -> Tag.of(e.getKey(), e.getValue())).collect(Collectors.toList());
    this.kvTags = CollectionUtils.isEmpty(extraTagMap) ? new ArrayList<>() : extraTagMap
            .entrySet().stream().map(e -> KeyValue.of(e.getKey(), e.getValue())).collect(Collectors.toList());
    this.contextSpecificTags = contextSpecificTags;
    this.requestTagKeys = CollectionUtils.isEmpty(requestTagKeys) ? new ArrayList<>() : requestTagKeys;
    this.urlMapper = urlMapper;
    this.includeHostTag = includeHostTag;
    instrumentNetworkRequests();
    instrumentEventListener();
  }

  private void instrumentNetworkRequests() {

    InstrumentedInterceptor metricsInterceptor = new InstrumentedInterceptor(registry, extraTags);

    OkHttpObservationConvention observationConvention = new DefaultOkHttpObservationConvention(OkHttp3Metrics.OKHTTP3_METRIC_NAME_PREFIX);

    OkHttpObservationInterceptor observationInterceptor = new OkHttpObservationInterceptor(
            ObservationRegistry.create(),
            observationConvention,
            OkHttp3Metrics.OKHTTP3_REQUEST_METRIC_NAME_PREFIX,
            urlMapper.get(),
            kvTags,
            contextSpecificTags,
            requestTagKeys,
            includeHostTag
    );

    OkHttpClient.Builder builder = rawClient.newBuilder();
    builder.networkInterceptors().add(0, metricsInterceptor);
    builder.networkInterceptors().add(1, observationInterceptor);
    rawClient = builder.build();
  }

  private void instrumentEventListener() {

    OkHttpMetricsEventListener metricsEventListener = OkHttpMetricsEventListener.builder(registry, OkHttp3Metrics.OKHTTP3_REQUEST_METRIC_NAME_PREFIX)
            .tags(extraTags)
            .requestTagKeys(requestTagKeys)
            .includeHostTag(includeHostTag)
            .uriMapper(urlMapper.get())
            .build();

    this.rawClient = this.rawClient
            .newBuilder()
            .eventListener(new InstrumentedEventListener(registry, metricsEventListener))
            .build();
  }



  @Override
  public Authenticator authenticator() {
    return rawClient.authenticator();
  }

  @Override
  public Cache cache() {
    return rawClient.cache();
  }

  @Override
  public CertificatePinner certificatePinner() {
    return rawClient.certificatePinner();
  }

  @Override
  public ConnectionPool connectionPool() {
    return rawClient.connectionPool();
  }

  @Override
  public List<ConnectionSpec> connectionSpecs() {
    return rawClient.connectionSpecs();
  }

  @Override
  public int connectTimeoutMillis() {
    return rawClient.connectTimeoutMillis();
  }

  @Override
  public CookieJar cookieJar() {
    return rawClient.cookieJar();
  }

  @Override
  public Dispatcher dispatcher() {
    return rawClient.dispatcher();
  }

  @Override
  public Dns dns() {
    return rawClient.dns();
  }

  @Override
  public boolean followRedirects() {
    return rawClient.followRedirects();
  }

  @Override
  public boolean followSslRedirects() {
    return rawClient.followSslRedirects();
  }

  @Override
  public HostnameVerifier hostnameVerifier() {
    return rawClient.hostnameVerifier();
  }

  @Override
  public List<Interceptor> interceptors() {
    return rawClient.interceptors();
  }

  @Override
  public List<Interceptor> networkInterceptors() {
    return rawClient.networkInterceptors();
  }

  @Override
  public Builder newBuilder() {
    return rawClient.newBuilder();
  }

  @Override
  public Call newCall(Request request) {
    return rawClient.newCall(request);
  }

  @Override
  public WebSocket newWebSocket(Request request, WebSocketListener listener) {
    return rawClient.newWebSocket(request, listener);
  }

  @Override
  public int pingIntervalMillis() {
    return rawClient.pingIntervalMillis();
  }

  @Override
  public List<Protocol> protocols() {
    return rawClient.protocols();
  }

  @Override
  public Proxy proxy() {
    return rawClient.proxy();
  }

  @Override
  public Authenticator proxyAuthenticator() {
    return rawClient.proxyAuthenticator();
  }

  @Override
  public ProxySelector proxySelector() {
    return rawClient.proxySelector();
  }

  @Override
  public int readTimeoutMillis() {
    return rawClient.readTimeoutMillis();
  }

  @Override
  public boolean retryOnConnectionFailure() {
    return rawClient.retryOnConnectionFailure();
  }

  @Override
  public SocketFactory socketFactory() {
    return rawClient.socketFactory();
  }

  @Override
  public SSLSocketFactory sslSocketFactory() {
    return rawClient.sslSocketFactory();
  }

  @Override
  public int writeTimeoutMillis() {
    return rawClient.writeTimeoutMillis();
  }

  @Override
  public boolean equals(Object obj) {
    return (obj instanceof InstrumentedOkHttpClient
            && rawClient.equals(((InstrumentedOkHttpClient) obj).rawClient))
        || rawClient.equals(obj);
  }

  @Override
  public String toString() {
    return rawClient.toString();
  }
}
