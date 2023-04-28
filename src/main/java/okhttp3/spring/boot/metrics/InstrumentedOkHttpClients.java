package okhttp3.spring.boot.metrics;

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
 * @author wandl
 **/
public final class InstrumentedOkHttpClients {

  public static OkHttpClient create(MeterRegistry registry) {
    return new InstrumentedOkHttpClient(registry, new OkHttpClient(), Collections.emptyMap(), Collections.emptyList(), Collections.emptyList(), UrlMapperEnum.ENCODED_PATH, false);
  }

  public static OkHttpClient create(MeterRegistry registry,
                                    OkHttpClient client) {
    return new InstrumentedOkHttpClient(registry, client, Collections.emptyMap(), Collections.emptyList(), Collections.emptyList(), UrlMapperEnum.ENCODED_PATH, false);
  }

  public static OkHttpClient create(MeterRegistry registry,
                                    OkHttpClient client,
                                    boolean includeHostTag) {
    return new InstrumentedOkHttpClient(registry, client, Collections.emptyMap(), Collections.emptyList(), Collections.emptyList(), UrlMapperEnum.ENCODED_PATH, includeHostTag);
  }


  public static OkHttpClient create(MeterRegistry registry,
                                    OkHttpClient client,
                                    UrlMapperEnum urlMapper,
                                    boolean includeHostTag) {
    return new InstrumentedOkHttpClient(registry, client, Collections.emptyMap(), Collections.emptyList(), Collections.emptyList(), urlMapper, includeHostTag);
  }

  public static OkHttpClient create(MeterRegistry registry,
                                    OkHttpClient client,
                                    List<BiFunction<Request, Response, KeyValue>> contextSpecificTags,
                                    UrlMapperEnum urlMapper,
                                    boolean includeHostTag) {
    return new InstrumentedOkHttpClient(registry, client, Collections.emptyMap(), Collections.emptyList(), contextSpecificTags, urlMapper, includeHostTag);
  }

  public static OkHttpClient create(MeterRegistry registry,
                                    OkHttpClient client,
                                    Map<String, String > extraTagMap,
                                    List<String> requestTagKeys,
                                    List<BiFunction<Request, Response, KeyValue>> contextSpecificTags,
                                    UrlMapperEnum urlMapper,
                                    boolean includeHostTag) {
    return new InstrumentedOkHttpClient(registry, client, extraTagMap, requestTagKeys, contextSpecificTags, urlMapper, includeHostTag);
  }

  private InstrumentedOkHttpClients() {
    // No instances.
  }
}
