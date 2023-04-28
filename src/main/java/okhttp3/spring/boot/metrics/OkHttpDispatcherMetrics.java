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
	public static final String METRIC_NAME_DISPATCHER_MAX_REQUESTS 			= OKHTTP3_METRIC_NAME_PREFIX + ".dispatcher.max.requests";
	public static final String METRIC_NAME_DISPATCHER_MAX_REQUESTS_PERHOST 	= OKHTTP3_METRIC_NAME_PREFIX + ".dispatcher.max.requests.perhost";
	public static final String METRIC_NAME_DISPATCHER_QUEUED_CALLS_COUNT		= OKHTTP3_METRIC_NAME_PREFIX + ".dispatcher.queued.calls.count";
	public static final String METRIC_NAME_DISPATCHER_RUNNING_CALLS_COUNT		= OKHTTP3_METRIC_NAME_PREFIX + ".dispatcher.running.calls.count";

	public OkHttpDispatcherMetrics(OkHttpClient okhttp3Client) {
		super(okhttp3Client);
	}

	public OkHttpDispatcherMetrics(OkHttpClient okhttp3Client, Iterable<Tag> tags) {
		super(okhttp3Client, tags);
	}

	@Override
	public void bindTo(MeterRegistry registry, OkHttpClient okhttp3Client, Iterable<Tag> tags) {
		Dispatcher dispatcher = okhttp3Client.dispatcher();
		bindCounter(registry, METRIC_NAME_DISPATCHER_MAX_REQUESTS, "max requests of dispatcher ", dispatcher, Dispatcher::getMaxRequests);
		bindCounter(registry, METRIC_NAME_DISPATCHER_MAX_REQUESTS_PERHOST, "max requests of dispatcher by per host ", dispatcher, Dispatcher::getMaxRequestsPerHost);
		bindGauge(registry, METRIC_NAME_DISPATCHER_QUEUED_CALLS_COUNT, "Total number of queued calls ", dispatcher, Dispatcher::queuedCallsCount);
		bindGauge(registry, METRIC_NAME_DISPATCHER_RUNNING_CALLS_COUNT, "Total number of running calls ", dispatcher, Dispatcher::runningCallsCount);
	}

}
