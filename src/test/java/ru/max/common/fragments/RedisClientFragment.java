package ru.max.common.fragments;

import org.apache.http.entity.ContentType;
import org.apache.jmeter.protocol.http.util.HTTPConstants;
import ru.max.common.models.redis.RedisAddType;
import ru.max.common.models.redis.RedisReadType;
import us.abstracta.jmeter.javadsl.core.controllers.DslSimpleController;

import static us.abstracta.jmeter.javadsl.JmeterDsl.*;

public class RedisClientFragment {
    private static Integer redisClientPort = 8080;

    public static void setRedisClientPort(Integer redisPort) {
        RedisClientFragment.redisClientPort = redisPort;
    }

    public static DslSimpleController readList(RedisReadType readMode) {
        return simpleController("DB_REDIS_CLIENT_READ_DATA_LIST",
                httpSampler("DB_REDIS_CLIENT_READ_DATA_LIST", "/readVecDeque?key=${__P(REDIS_KEY_READ)}&read_mode=" + readMode.getReadMode())
                        .host("${__P(REDIS_CLIENT_URL)}")
                        .port(redisClientPort)
                        .method(HTTPConstants.GET)
                        .children(
                                jsonAssertion("status").equalsTo("OK"),
                                jsonExtractor("redis_client_data", "data")
                                        .matchNumber(1)
                        )
        );
    }

    public static DslSimpleController addList(RedisAddType addMode) {
        return simpleController("DB_REDIS_CLIENT_ADD_DATA_LIST",
                httpSampler("DB_REDIS_CLIENT_ADD_DATA_LIST", "/addVecDeque?key=${__P(REDIS_KEY_ADD)}&add_mode=" + addMode.getAddMode())
                        .host("${__P(REDIS_CLIENT_URL)}")
                        .port(redisClientPort)
                        .method(HTTPConstants.POST)
                        .contentType(ContentType.APPLICATION_JSON)
                        .body("${redis_client_data}")
                        .children(
                                jsonAssertion("status").equalsTo("OK")
                        )
        );
    }
}
