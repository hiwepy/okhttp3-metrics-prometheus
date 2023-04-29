/*
 * Copyright (c) 2018, hiwepy (https://github.com/hiwepy).
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package okhttp3.spring.boot.metrics;

import io.micrometer.common.lang.NonNull;
import io.micrometer.core.instrument.*;
import io.micrometer.core.instrument.binder.MeterBinder;
import okhttp3.Cache;
import okhttp3.ConnectionPool;
import okhttp3.Dispatcher;
import okhttp3.OkHttpClient;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;

import java.io.IOException;
import java.util.Collections;
import java.util.concurrent.TimeUnit;
import java.util.function.ToDoubleFunction;
import java.util.function.ToLongFunction;

/**
 * OkHttp3 Metrics
 */
public abstract class OkHttp3Metrics implements MeterBinder, ApplicationListener<ApplicationStartedEvent> {

	/**
	 * Prefix used for all OkHttp3 metric names.
	 */
	public static final String OKHTTP3_METRIC_NAME_PREFIX = "okhttp3";
	/**
	 * Prefix used for all OkHttp3 Request metric names.
	 */
	public static final String OKHTTP3_REQUEST_METRIC_NAME_PREFIX = "okhttp3.requests";
	/**
	 * Prefix used for all OkHttp3 Pool metric names.
	 */
	public static final String OKHTTP3_POOL_METRIC_NAME_PREFIX = "okhttp3.pool";

	/**
	 * calls
	 */
	public static final String METRIC_NAME_CALLS_STARTED 				= OKHTTP3_METRIC_NAME_PREFIX + ".calls.started";
	public static final String METRIC_NAME_CALLS_END 					= OKHTTP3_METRIC_NAME_PREFIX + ".calls.end";
	public static final String METRIC_NAME_CALLS_FAILED 				= OKHTTP3_METRIC_NAME_PREFIX + ".calls.failed";
	public static final String METRIC_NAME_CALLS_DURATION 				= OKHTTP3_METRIC_NAME_PREFIX + ".calls.duration";
	/**
	 * dns
	 */
	public static final String METRIC_NAME_DNS_STARTED 					= OKHTTP3_METRIC_NAME_PREFIX + ".dns.started";
	public static final String METRIC_NAME_DNS_END 						= OKHTTP3_METRIC_NAME_PREFIX + ".dns.end";
	public static final String METRIC_NAME_DNS_DURATION 				= OKHTTP3_METRIC_NAME_PREFIX + ".dns.duration";
	/**
	 * connections
	 */
	public static final String METRIC_NAME_CONNECTIONS_STARTED 			= OKHTTP3_METRIC_NAME_PREFIX + ".connections.started";
	public static final String METRIC_NAME_CONNECTIONS_END 				= OKHTTP3_METRIC_NAME_PREFIX + ".connections.end";
	public static final String METRIC_NAME_CONNECTIONS_FAILED 			= OKHTTP3_METRIC_NAME_PREFIX + ".connections.failed";
	public static final String METRIC_NAME_CONNECTIONS_DURATION 		= OKHTTP3_METRIC_NAME_PREFIX + ".connections.duration";
	public static final String METRIC_NAME_CONNECTIONS_ACQUIRED 		= OKHTTP3_METRIC_NAME_PREFIX + ".connections.acquired";
	public static final String METRIC_NAME_CONNECTIONS_RELEASED 		= OKHTTP3_METRIC_NAME_PREFIX + ".connections.released";
	/**
	 * requests
	 */
	public static final String METRIC_NAME_REQUESTS_HEADERS_STARTED 				= OKHTTP3_METRIC_NAME_PREFIX + ".requests.headers.started";
	public static final String METRIC_NAME_REQUESTS_HEADERS_END 					= OKHTTP3_METRIC_NAME_PREFIX + ".requests.headers.end";
	public static final String METRIC_NAME_REQUESTS_BODY_STARTED 				= OKHTTP3_METRIC_NAME_PREFIX + ".requests.body.started";
	public static final String METRIC_NAME_REQUESTS_BODY_END					= OKHTTP3_METRIC_NAME_PREFIX + ".requests.body.end";
	public static final String METRIC_NAME_REQUESTS_BODY_BYTES					= OKHTTP3_METRIC_NAME_PREFIX + ".requests.body.bytes";
	public static final String METRIC_NAME_REQUESTS_FAILED 					= OKHTTP3_METRIC_NAME_PREFIX + ".requests.failed";
	/**
	 * responses
	 */
	public static final String METRIC_NAME_RESPONSES_HEADERS_STARTED 			= OKHTTP3_METRIC_NAME_PREFIX + ".responses.headers.started";
	public static final String METRIC_NAME_RESPONSES_HEADERS_END 			= OKHTTP3_METRIC_NAME_PREFIX + ".responses.headers.end";
	public static final String METRIC_NAME_RESPONSES_BODY_STARTED 			= OKHTTP3_METRIC_NAME_PREFIX + ".responses.body.started";
	public static final String METRIC_NAME_RESPONSES_BODY_BYTES					= OKHTTP3_METRIC_NAME_PREFIX + ".responses.body.bytes";
	public static final String METRIC_NAME_RESPONSES_BODY_END 			= OKHTTP3_METRIC_NAME_PREFIX + ".responses.body.end";
	public static final String METRIC_NAME_RESPONSES_FAILED 			= OKHTTP3_METRIC_NAME_PREFIX + ".responses.failed";

	/**
	 * timeout
	 */
	public static final String METRIC_NAME_CALL_TIMEOUT_COUNT 			= OKHTTP3_METRIC_NAME_PREFIX + ".call.timeout.count";
	public static final String METRIC_NAME_CONNECT_TIMEOUT_COUNT 		= OKHTTP3_METRIC_NAME_PREFIX + ".connect.timeout.count";
	public static final String METRIC_NAME_READ_TIMEOUT_COUNT 			= OKHTTP3_METRIC_NAME_PREFIX + ".read.timeout.count";
	public static final String METRIC_NAME_WRITE_TIMEOUT_COUNT 		= OKHTTP3_METRIC_NAME_PREFIX + ".write.timeout.count";
	public static final String METRIC_NAME_PING_FAIL_COUNT 			= OKHTTP3_METRIC_NAME_PREFIX + ".ping.fail.count";


	private OkHttpClient okhttp3Client;
	private String namePrefix;
	private Iterable<Tag> tags;

	public OkHttp3Metrics(OkHttpClient okhttp3Client, String namePrefix) {
		this(okhttp3Client, namePrefix, Collections.emptyList());
	}

	public OkHttp3Metrics(OkHttpClient okhttp3Client, String namePrefix, Iterable<Tag> tags) {
		this.okhttp3Client = okhttp3Client;
		this.namePrefix = namePrefix;
		this.tags = tags;
	}

	@Override
	public void onApplicationEvent(ApplicationStartedEvent event) {
		this.bindTo(event.getApplicationContext().getBean(MeterRegistry.class));
	}

	@Override
	public void bindTo(MeterRegistry registry) {
		bindTo(registry, okhttp3Client, namePrefix, tags);
	}

	/**
	 * Bind metrics to the given registry.
	 * @param registry
	 * @param okhttp3Client
	 * @param tags
	 */
	abstract void bindTo(@NonNull MeterRegistry registry, OkHttpClient okhttp3Client, String namePrefix, Iterable<Tag> tags);

	protected  <T> void bindTimer(MeterRegistry registry, String name, String desc, T metricsHandler,
								  ToLongFunction<T> countFunc, ToDoubleFunction<T> consumer, Iterable<Tag> tags) {
		FunctionTimer.builder(name, metricsHandler, countFunc, consumer, TimeUnit.SECONDS)
				.description(desc)
				.tags(tags)
				.register(registry);
	}

	protected <T> void bindGauge(MeterRegistry registry, String name, String desc, T metricResult,
								   ToDoubleFunction<T> consumer, Iterable<Tag> tags) {
		Gauge.builder(name, metricResult, consumer)
				.description(desc)
				.tags(tags)
				.register(registry);
	}

	protected <T> void bindTimeGauge(MeterRegistry registry, String name, String desc, T metricResult,
							   ToDoubleFunction<T> consumer, Iterable<Tag> tags) {
		TimeGauge.builder(name, metricResult, TimeUnit.SECONDS, consumer)
				.description(desc)
				.tags(tags)
				.register(registry);
	}

	protected <T> void bindCounter(MeterRegistry registry, String name, String desc, T metricsHandler,
							 ToDoubleFunction<T> consumer, Iterable<Tag> tags) {
		FunctionCounter.builder(name, metricsHandler, consumer)
				.description(desc)
				.tags(tags)
				.register(registry);
	}

}
