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

import io.micrometer.core.instrument.*;
import okhttp3.Dispatcher;
import okhttp3.OkHttpClient;

/**
 * OkHttp3 Dispatcher Metrics
 */
public class OkHttpDispatcherMetrics extends OkHttp3Metrics {

	/**
	 * dispatcher
	 */
	public static final String METRIC_NAME_DISPATCHER_MAX_REQUESTS 			= ".dispatcher.max.requests";
	public static final String METRIC_NAME_DISPATCHER_MAX_REQUESTS_PERHOST 	= ".dispatcher.max.requests.perhost";
	public static final String METRIC_NAME_DISPATCHER_QUEUED_CALLS_COUNT	= ".dispatcher.queued.calls.count";
	public static final String METRIC_NAME_DISPATCHER_RUNNING_CALLS_COUNT	= ".dispatcher.running.calls.count";

	public OkHttpDispatcherMetrics(OkHttpClient okhttp3Client) {
		super(okhttp3Client, OkHttp3Metrics.OKHTTP3_POOL_METRIC_NAME_PREFIX);
	}

	public OkHttpDispatcherMetrics(OkHttpClient okhttp3Client, String namePrefix) {
		super(okhttp3Client, namePrefix);
	}

	public OkHttpDispatcherMetrics(OkHttpClient okhttp3Client, String namePrefix, Iterable<Tag> tags) {
		super(okhttp3Client, namePrefix, tags);

	}

	@Override
	public void bindTo(MeterRegistry registry, OkHttpClient okhttp3Client, String namePrefix, Iterable<Tag> tags) {
		Dispatcher dispatcher = okhttp3Client.dispatcher();
		bindCounter(registry, namePrefix + METRIC_NAME_DISPATCHER_MAX_REQUESTS, "max requests of dispatcher ", dispatcher, Dispatcher::getMaxRequests, tags);
		bindCounter(registry, namePrefix + METRIC_NAME_DISPATCHER_MAX_REQUESTS_PERHOST, "max requests of dispatcher by per host ", dispatcher, Dispatcher::getMaxRequestsPerHost, tags);
		bindGauge(registry, namePrefix + METRIC_NAME_DISPATCHER_QUEUED_CALLS_COUNT, "Total number of queued calls ", dispatcher, Dispatcher::queuedCallsCount, tags);
		bindGauge(registry, namePrefix + METRIC_NAME_DISPATCHER_RUNNING_CALLS_COUNT, "Total number of running calls ", dispatcher, Dispatcher::runningCallsCount, tags);
	}

}
