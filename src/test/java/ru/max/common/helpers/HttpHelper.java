package ru.max.common.helpers;

import us.abstracta.jmeter.javadsl.http.DslHttpDefaults;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

import static us.abstracta.jmeter.javadsl.JmeterDsl.httpDefaults;

public class HttpHelper {
    public static DslHttpDefaults getHttpDefaults() {
        return httpDefaults()
                .protocol("${__P(PROTOCOL)}")
                .host("${__P(API_GATEWAY)}")
                .encoding(StandardCharsets.UTF_8)
                .connectionTimeout(Duration.ofSeconds(5))
                .responseTimeout(Duration.ofSeconds(60));
    }
}
