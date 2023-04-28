package okhttp3.spring.boot.metrics;

import okhttp3.Request;

import java.util.function.Function;

/**
 * @author wandl
 */
public enum UrlMapperEnum {

    /**
     * Returns the entire path of this URL encoded for use in HTTP resource resolution. The returned
     *  path will start with {@code "/"}.
     */
    ENCODED_PATH( "Encoded Path ", (request)-> request.url().encodedPath()),
    FULL_URL( "Full URL", (request) -> request.url().toString()),
    TOP_PRIVATE_DOMAIN( "Top Private Domain", (request) -> request.url().topPrivateDomain()),
    ;

    private String name;
    private Function<Request, String> urlMapper;

    UrlMapperEnum(String name, Function<Request, String> urlMapper) {
        this.name = name;
        this.urlMapper = urlMapper;
    }

    public String getName() {
        return name;
    }

    public Function<Request, String> get() {
        return urlMapper;
    }

    public boolean equals(UrlMapperEnum urlMapperEnum){
        return this.compareTo(urlMapperEnum) == 0;
    }

}
