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
	public static final String METRIC_NAME_CACHE_REQUEST_COUNT 			= ".cache.request.count";
	public static final String METRIC_NAME_CACHE_HIT_COUNT 				= ".cache.hit.count";
	public static final String METRIC_NAME_CACHE_NETWORK_COUNT 			=  ".cache.network.count";
	public static final String METRIC_NAME_CACHE_WRITE_SUCCESS_COUNT 	=  ".cache.write.success.count";
	public static final String METRIC_NAME_CACHE_WRITE_ABORT_COUNT		=  ".cache.write.abort.count";
	public static final String METRIC_NAME_CACHE_CURRENT_SIZE 			=  ".cache.current.size";
	public static final String METRIC_NAME_CACHE_MAX_SIZE 				=  ".cache.max.size";

	public OkHttpCacheMetrics(OkHttpClient okhttp3Client) {
		super(okhttp3Client, OkHttp3Metrics.OKHTTP3_POOL_METRIC_NAME_PREFIX);
	}

	public OkHttpCacheMetrics(OkHttpClient okhttp3Client, String namePrefix) {
		super(okhttp3Client, namePrefix);
	}

	public OkHttpCacheMetrics(OkHttpClient okhttp3Client, String namePrefix, Iterable<Tag> tags) {
		super(okhttp3Client, namePrefix, tags);
	}

	@Override
	public void bindTo(MeterRegistry registry, OkHttpClient okhttp3Client, String namePrefix, Iterable<Tag> tags) {
		Cache cache = okhttp3Client.cache();
		bindGauge(registry, namePrefix  + METRIC_NAME_CACHE_REQUEST_COUNT, "Total number of cache request ", cache, Cache::requestCount, tags);
		bindGauge(registry, namePrefix  + METRIC_NAME_CACHE_HIT_COUNT, "Total number of cache hit ", cache, Cache::hitCount, tags);
		bindGauge(registry, namePrefix  + METRIC_NAME_CACHE_NETWORK_COUNT, "Total number of cache network ", cache, Cache::networkCount, tags);
		bindGauge(registry, namePrefix  + METRIC_NAME_CACHE_WRITE_SUCCESS_COUNT, "Total number of cache write success ", cache, Cache::writeSuccessCount, tags);
		bindGauge(registry, namePrefix  + METRIC_NAME_CACHE_WRITE_ABORT_COUNT, "Total number of cache write abort ", cache, Cache::writeAbortCount, tags);
		bindGauge(registry, namePrefix  + METRIC_NAME_CACHE_CURRENT_SIZE, "Total number of current cache size ", cache, (c) -> {
			try {
				return c.size();
			} catch (IOException e) {
				return 0;
			}
		}, tags);
		bindGauge(registry, METRIC_NAME_CACHE_MAX_SIZE, "Total number of cache max size ", cache, Cache::maxSize, tags);
	}

}
