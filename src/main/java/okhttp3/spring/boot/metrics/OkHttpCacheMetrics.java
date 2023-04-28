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
import okhttp3.Cache;
import okhttp3.OkHttpClient;

import java.io.IOException;

/**
 * OkHttp3 Cache Metrics
 */
public class OkHttpCacheMetrics extends OkHttp3Metrics {

	/**
	 * http cache
	 */
	public static final String METRIC_NAME_CACHE_REQUEST_COUNT 		= OKHTTP3_METRIC_NAME_PREFIX + ".cache.request.count";
	public static final String METRIC_NAME_CACHE_HIT_COUNT 		= OKHTTP3_METRIC_NAME_PREFIX + ".cache.hit.count";
	public static final String METRIC_NAME_CACHE_NETWORK_COUNT 		= OKHTTP3_METRIC_NAME_PREFIX + ".cache.network.count";
	public static final String METRIC_NAME_CACHE_WRITE_SUCCESS_COUNT 		= OKHTTP3_METRIC_NAME_PREFIX + ".cache.write.success.count";
	public static final String METRIC_NAME_CACHE_WRITE_ABORT_COUNT 		= OKHTTP3_METRIC_NAME_PREFIX + ".cache.write.abort.count";
	public static final String METRIC_NAME_CACHE_CURRENT_SIZE 	= OKHTTP3_METRIC_NAME_PREFIX + ".cache.current.size";
	public static final String METRIC_NAME_CACHE_MAX_SIZE 	= OKHTTP3_METRIC_NAME_PREFIX + ".cache.max.size";

	public OkHttpCacheMetrics(OkHttpClient okhttp3Client) {
		super(okhttp3Client);
	}

	public OkHttpCacheMetrics(OkHttpClient okhttp3Client, Iterable<Tag> tags) {
		super(okhttp3Client, tags);
	}

	@Override
	public void bindTo(MeterRegistry registry, OkHttpClient okhttp3Client, Iterable<Tag> tags) {
		Cache cache = okhttp3Client.cache();
		bindGauge(registry, METRIC_NAME_CACHE_REQUEST_COUNT, "Total number of cache request ", cache, Cache::requestCount);
		bindGauge(registry, METRIC_NAME_CACHE_HIT_COUNT, "Total number of cache hit ", cache, Cache::hitCount);
		bindGauge(registry, METRIC_NAME_CACHE_NETWORK_COUNT, "Total number of cache network ", cache, Cache::networkCount);
		bindGauge(registry, METRIC_NAME_CACHE_WRITE_SUCCESS_COUNT, "Total number of cache write success ", cache, Cache::writeSuccessCount);
		bindGauge(registry, METRIC_NAME_CACHE_WRITE_ABORT_COUNT, "Total number of cache write abort ", cache, Cache::writeAbortCount);
		bindGauge(registry, METRIC_NAME_CACHE_CURRENT_SIZE, "Total number of current cache size ", cache, (c) -> {
			try {
				return c.size();
			} catch (IOException e) {
				return 0;
			}
		});
		bindGauge(registry, METRIC_NAME_CACHE_MAX_SIZE, "Total number of cache max size ", cache, Cache::maxSize);
	}

}
