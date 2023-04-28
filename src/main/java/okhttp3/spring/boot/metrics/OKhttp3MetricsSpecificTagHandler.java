package okhttp3.spring.boot.metrics;

import io.micrometer.common.KeyValue;
import okhttp3.Request;
import okhttp3.Response;

import java.util.function.BiFunction;

/**
 * @author wandl
 */
public interface OKhttp3MetricsSpecificTagHandler {

    BiFunction<Request, Response, KeyValue> getHandler();

}
