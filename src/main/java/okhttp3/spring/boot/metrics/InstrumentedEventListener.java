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

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.SocketTimeoutException;
import java.util.Arrays;
import java.util.List;

import io.micrometer.common.lang.NonNull;
import io.micrometer.common.lang.Nullable;
import io.micrometer.core.instrument.*;
import okhttp3.Call;
import okhttp3.Connection;
import okhttp3.EventListener;
import okhttp3.Handshake;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;

/**
 * A client-scoped {@link EventListener} that records metrics around quantity, size, and duration of
 * HTTP calls using Dropwizard Metrics.
 *
 * <p>This listener will receive ALL analytics events for {@link okhttp3.OkHttpClient the given
 * instrumented client}.
 *
 * <p>This listener WILL NOT override a user-provided listener. It will ensure the user-provided
 * listener receives ALL analytics events as expected. Users usually configure a {@link
 * EventListener listener} via {@link okhttp3.OkHttpClient.Builder#eventListener(EventListener)} or
 * {@link okhttp3.OkHttpClient.Builder#eventListenerFactory(EventListener.Factory)}).
 *
 * @see EventListener for semantics and restrictions on listener implementations.
 */
final class InstrumentedEventListener extends EventListener {

  private List<EventListener> delegates;

  private final Counter callStart;
  private final Counter callEnd;
  private final Counter callFailed;
  private final Timer callDuration;
  private final Counter dnsStart;
  private final Counter dnsEnd;
  private final Timer dnsDuration;
  private final Counter connectionStart;
  private final Counter connectionEnd;
  private final Counter connectionFailed;
  private final Timer connectionDuration;
  private final Counter connectionAcquired;
  private final Counter connectionReleased;
  private final Counter requestHeadersStart;
  private final Counter requestHeadersEnd;
  private final Counter requestBodyStart;
  private final Counter requestBodyEnd;
  private final DistributionSummary requestBodyBytes;
  private final Counter requestFailed;

  private final Counter responseHeadersStart;
  private final Counter responseHeadersEnd;
  private final Counter responseBodyStart;
  private final DistributionSummary responseBodyBytes;
  private final Counter responseBodyEnd;
  private final Counter responseFailed;

  InstrumentedEventListener(@NonNull MeterRegistry registry, EventListener ... delegates) {
    this.delegates = Arrays.asList(delegates);

    this.callStart = registry.counter(OkHttp3Metrics.METRIC_NAME_CALLS_STARTED);
    this.callEnd = registry.counter(OkHttp3Metrics.METRIC_NAME_CALLS_END);
    this.callFailed = registry.counter(OkHttp3Metrics.METRIC_NAME_CALLS_FAILED);
    this.callDuration = registry.timer(OkHttp3Metrics.METRIC_NAME_CALLS_DURATION);

    this.dnsStart = registry.counter(OkHttp3Metrics.METRIC_NAME_DNS_STARTED);
    this.dnsEnd = registry.counter(OkHttp3Metrics.METRIC_NAME_DNS_END);
    this.dnsDuration = registry.timer(OkHttp3Metrics.METRIC_NAME_DNS_DURATION);

    this.connectionStart = registry.counter(OkHttp3Metrics.METRIC_NAME_CONNECTIONS_STARTED);
    this.connectionEnd = registry.counter(OkHttp3Metrics.METRIC_NAME_CONNECTIONS_END);
    this.connectionFailed = registry.counter(OkHttp3Metrics.METRIC_NAME_CONNECTIONS_FAILED);
    this.connectionDuration = registry.timer(OkHttp3Metrics.METRIC_NAME_CONNECTIONS_DURATION);
    this.connectionAcquired = registry.counter(OkHttp3Metrics.METRIC_NAME_CONNECTIONS_ACQUIRED);
    this.connectionReleased = registry.counter(OkHttp3Metrics.METRIC_NAME_CONNECTIONS_RELEASED);

    this.requestHeadersStart = registry.counter(OkHttp3Metrics.METRIC_NAME_REQUESTS_HEADERS_STARTED);
    this.requestHeadersEnd = registry.counter(OkHttp3Metrics.METRIC_NAME_REQUESTS_HEADERS_END);
    this.requestBodyStart = registry.counter(OkHttp3Metrics.METRIC_NAME_REQUESTS_BODY_STARTED);
    this.requestBodyEnd = registry.counter(OkHttp3Metrics.METRIC_NAME_REQUESTS_BODY_END);
    this.requestBodyBytes = registry.summary(OkHttp3Metrics.METRIC_NAME_REQUESTS_BODY_BYTES);
    this.requestFailed = registry.counter(OkHttp3Metrics.METRIC_NAME_REQUESTS_FAILED);

    this.responseHeadersStart = registry.counter(OkHttp3Metrics.METRIC_NAME_RESPONSES_HEADERS_STARTED);
    this.responseHeadersEnd = registry.counter(OkHttp3Metrics.METRIC_NAME_RESPONSES_HEADERS_END);
    this.responseBodyStart = registry.counter(OkHttp3Metrics.METRIC_NAME_RESPONSES_BODY_STARTED);
    this.responseBodyBytes = registry.summary(OkHttp3Metrics.METRIC_NAME_RESPONSES_BODY_BYTES);
    this.responseBodyEnd = registry.counter(OkHttp3Metrics.METRIC_NAME_RESPONSES_BODY_END);
    this.responseFailed = registry.counter(OkHttp3Metrics.METRIC_NAME_RESPONSES_FAILED);

  }

  @Override
  public void callStart(@NonNull Call call) {
    this.callStart.increment();
    this.delegates.forEach(delegate -> delegate.callStart(call));
  }

  @Override
  public void dnsStart(@NonNull Call call, @NonNull String domainName) {
    this.dnsStart.increment();
    this.delegates.forEach(delegate -> delegate.dnsStart(call, domainName));
  }

  @Override
  public void dnsEnd(@NonNull Call call, @NonNull String domainName, @NonNull List<InetAddress> inetAddressList) {
    this.dnsEnd.increment();
    this.delegates.forEach(delegate -> delegate.dnsEnd(call, domainName, inetAddressList));
  }

  @Override
  public void connectStart(@NonNull Call call, @NonNull InetSocketAddress inetSocketAddress, @NonNull Proxy proxy) {
    this.connectionStart.increment();
     this.delegates.forEach(delegate -> delegate.connectStart(call, inetSocketAddress, proxy));
  }

  @Override
  public void secureConnectStart(@NonNull Call call) {
    this.delegates.forEach(delegate -> delegate.secureConnectStart(call));
  }

  @Override
  public void secureConnectEnd(@NonNull Call call, @Nullable Handshake handshake) {
    this.delegates.forEach(delegate -> delegate.secureConnectEnd(call, handshake));
  }

  @Override
  public void connectEnd(
      @NonNull Call call,
      @NonNull InetSocketAddress inetSocketAddress,
      @NonNull Proxy proxy,
      @Nullable Protocol protocol) {
    this.connectionEnd.increment();
    this.delegates.forEach(delegate -> delegate.connectEnd(call, inetSocketAddress, proxy, protocol));
  }

  @Override
  public void connectFailed(
      @NonNull Call call,
      @NonNull InetSocketAddress inetSocketAddress,
      @NonNull Proxy proxy,
      @Nullable Protocol protocol,
      @NonNull IOException ioe) {
    this.connectionFailed.increment();
    this.delegates.forEach(delegate -> delegate.connectFailed(call, inetSocketAddress, proxy, protocol, ioe));
  }

  @Override
  public void connectionAcquired(@NonNull Call call, @NonNull Connection connection) {
    this.connectionAcquired.increment();
    this.delegates.forEach(delegate -> delegate.connectionAcquired(call, connection));
  }

  @Override
  public void connectionReleased(@NonNull Call call, @NonNull Connection connection) {
    this.connectionReleased.increment();
    this.delegates.forEach(delegate -> delegate.connectionReleased(call, connection));
  }

  @Override
  public void requestHeadersStart(@NonNull Call call) {
    this.requestHeadersStart.increment();
    this.delegates.forEach(delegate -> delegate.requestHeadersStart(call));
  }

  @Override
  public void requestHeadersEnd(@NonNull Call call, @NonNull Request request) {
    this.requestHeadersEnd.increment();
    this.delegates.forEach(delegate -> delegate.requestHeadersEnd(call, request));
  }

  @Override
  public void requestBodyStart(@NonNull Call call) {
    this.requestBodyStart.increment();
    this.delegates.forEach(delegate -> delegate.requestBodyStart(call));
  }

  @Override
  public void requestBodyEnd(@NonNull Call call, long byteCount) {
    this.requestBodyBytes.record(byteCount);
    this.requestBodyEnd.increment();
    this.delegates.forEach(delegate -> delegate.requestBodyEnd(call, byteCount));
  }

  @Override
  public void requestFailed(@NonNull Call call, @NonNull IOException ioe) {
    this.requestFailed.increment();
    this.delegates.forEach(delegate -> delegate.requestFailed(call, ioe));
  }

  @Override
  public void responseHeadersStart(@NonNull Call call) {
    this.responseHeadersStart.increment();
    this.delegates.forEach(delegate -> delegate.responseHeadersStart(call));
  }


  @Override
  public void responseHeadersEnd(@NonNull Call call, @NonNull Response response) {
    this.responseHeadersEnd.increment();
    this.delegates.forEach(delegate -> delegate.responseHeadersEnd(call, response));
  }

  @Override
  public void responseBodyStart(@NonNull Call call) {
    this.responseBodyStart.increment();
    this.delegates.forEach(delegate -> delegate.responseBodyStart(call));
  }

  @Override
  public void responseBodyEnd(@NonNull Call call, long byteCount) {
    this.responseBodyBytes.record(byteCount);
    this.responseBodyEnd.increment();
    this.delegates.forEach(delegate -> delegate.responseBodyEnd(call, byteCount));
  }

  @Override
  public void responseFailed(@NonNull Call call, @NonNull IOException ioe) {
    this.responseFailed.increment();
    this.delegates.forEach(delegate -> delegate.responseFailed(call, ioe));
  }

  @Override
  public void callEnd(@NonNull Call call) {
    this.callEnd.increment();
    this.delegates.forEach(delegate -> delegate.callEnd(call));
  }

  @Override
  public void callFailed(@NonNull Call call, @NonNull IOException ioe) {
    this.callFailed.increment();
    this.delegates.forEach(delegate -> delegate.callFailed(call, ioe));
  }
}
