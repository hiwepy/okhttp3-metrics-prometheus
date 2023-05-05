package okhttp3.spring.boot;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;

/**
 * okhttp3 client auto configuration tests
 */
@SpringBootApplication
public class OkHttp3MetricsApplicationTests implements CommandLineRunner {

    @Bean
    public MeterRegistry registry() {
        return new SimpleMeterRegistry();
    }

    public static void main(String[] args) {
        SpringApplication.run(OkHttp3MetricsApplicationTests.class, args);
    }

    @Autowired
    @Lazy
    OkHttpClient okHttpClient;

    @Override
    public void run(String... args) throws Exception {
        System.err.println("Spring Boot Application（OkHttp3-Metrics-Application） Started !");

        String url = "https://baidu.com";
        for (int i = 0; i < 1000; i++) {
            Request request = new Request.Builder().url(url).build();
            Response response = okHttpClient.newCall(request).execute();
            System.out.println(response.body().string());
            Thread.sleep(10);
        }

    }

}