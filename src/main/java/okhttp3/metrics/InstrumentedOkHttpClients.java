package okhttp3.metrics;

import io.micrometer.common.KeyValue;
import io.micrometer.core.instrument.MeterRegistry;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

/**
 * Static factory methods for instrumenting an {@link OkHttpClient}.
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 **/
public final class InstrumentedOkHttpClients {

  public static OkHttpClient create(MeterRegistry registry) {
    return InstrumentedOkHttpClient.create(registry, new OkHttpClient(), Collections.emptyMap(), Collections.emptyList(), Collections.emptyList(), UrlMapperEnum.ENCODED_PATH, false);
  }

  public static OkHttpClient create(MeterRegistry registry,
                                    OkHttpClient client) {
    return InstrumentedOkHttpClient.create(registry, client, Collections.emptyMap(), Collections.emptyList(), Collections.emptyList(), UrlMapperEnum.ENCODED_PATH, false);
  }

  public static OkHttpClient create(MeterRegistry registry,
                                    OkHttpClient client,
                                    boolean includeHostTag) {
    return InstrumentedOkHttpClient.create(registry, client, Collections.emptyMap(), Collections.emptyList(), Collections.emptyList(), UrlMapperEnum.ENCODED_PATH, includeHostTag);
  }


  public static OkHttpClient create(MeterRegistry registry,
                                    OkHttpClient client,
                                    UrlMapperEnum urlMapper,
                                    boolean includeHostTag) {
    return InstrumentedOkHttpClient.create(registry, client, Collections.emptyMap(), Collections.emptyList(), Collections.emptyList(), urlMapper, includeHostTag);
  }

  public static OkHttpClient create(MeterRegistry registry,
                                    OkHttpClient client,
                                    List<BiFunction<Request, Response, KeyValue>> contextSpecificTags,
                                    UrlMapperEnum urlMapper,
                                    boolean includeHostTag) {
    return InstrumentedOkHttpClient.create(registry, client, Collections.emptyMap(), Collections.emptyList(), contextSpecificTags, urlMapper, includeHostTag);
  }

  public static OkHttpClient create(MeterRegistry registry,
                                    OkHttpClient client,
                                    Map<String, String > extraTagMap,
                                    List<String> requestTagKeys,
                                    List<BiFunction<Request, Response, KeyValue>> contextSpecificTags,
                                    UrlMapperEnum urlMapper,
                                    boolean includeHostTag) {
    return InstrumentedOkHttpClient.create(registry, client, extraTagMap, requestTagKeys, contextSpecificTags, urlMapper, includeHostTag);
  }

  private InstrumentedOkHttpClients() {
    // No instances.
  }
}
