package ru.max.common.fragments;

import com.google.gson.Gson;
import org.apache.http.entity.ContentType;
import org.apache.jmeter.protocol.http.util.HTTPConstants;
import us.abstracta.jmeter.javadsl.core.controllers.DslSimpleController;
import us.abstracta.jmeter.javadsl.core.postprocessors.DslJsonExtractor;

import static us.abstracta.jmeter.javadsl.JmeterDsl.*;
import static us.abstracta.jmeter.javadsl.JmeterDsl.jsonExtractor;

public class LoginFragment {
    private final String name;
    private final String protocol;
    private final String host;
    private final String url;
    private final Gson gson = new Gson();

    public LoginFragment(String name, String protocol, String host, String url) {
        this.name = name;
        this.protocol = protocol;
        this.host = host;
        this.url = url;
    }

    public DslSimpleController get() {
        return simpleController("TF_" + name,
                httpSampler("UR_" + name, url)
                        .protocol(protocol)
                        .host(host)
                        .method(HTTPConstants.POST)
                        .contentType(ContentType.APPLICATION_JSON)
                        .body("{ \"username\": \"${username}\", \"password\": \"${password}\" }")
                        .children(
                                jsonExtractor("access_token", "$..access_token")
                                        .queryLanguage(DslJsonExtractor.JsonQueryLanguage.JSON_PATH)
                                        .matchNumber(1)
                                        .defaultValue("access_token_ERROR"),
                                jsonExtractor("refresh_token", "$..refresh_token")
                                        .queryLanguage(DslJsonExtractor.JsonQueryLanguage.JSON_PATH)
                                        .matchNumber(1)
                                        .defaultValue("refresh_token_ERROR")
                        )
        );
    }
}
