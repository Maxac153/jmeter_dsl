package ru.max.common.helpers;

import us.abstracta.jmeter.javadsl.http.DslCookieManager;

import static us.abstracta.jmeter.javadsl.JmeterDsl.httpCookies;

public class CookiesHelper {
    public static DslCookieManager getCookiesDisable() {
        return httpCookies()
                .disable();
    }
}
