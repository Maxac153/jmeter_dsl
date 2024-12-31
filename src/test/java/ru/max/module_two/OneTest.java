package ru.max.module_two;

import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import ru.max.common.models.profile.Profile;
import ru.max.common.models.redis.RedisAddType;
import ru.max.common.models.redis.RedisReadType;
import ru.max.common.models.test_parameters.CommonTestParameters;
import ru.max.common.fragments.RedisClientFragment;
import ru.max.common.helpers.DurationHelper;
import ru.max.common.helpers.PropertyHelper;
import us.abstracta.jmeter.javadsl.core.engines.EmbeddedJmeterEngine;

import java.io.IOException;
import java.util.HashMap;
import java.util.Properties;
import java.util.concurrent.TimeoutException;

import static ru.max.common.helpers.CacheHelper.getCacheDisable;
import static ru.max.common.helpers.CookiesHelper.getCookiesDisable;
import static ru.max.common.helpers.HttpHelper.getHttpDefaults;
import static ru.max.common.helpers.LogHelper.influxDbLog;
import static ru.max.common.helpers.LogHelper.xmlErrorLog;
import static ru.max.common.helpers.VisualizersHelper.resultDashboard;
import static ru.max.common.helpers.VisualizersHelper.resultTree;
import static ru.max.common.thread_groups.ThreadGroups.getRpsThreadGroup;
import static us.abstracta.jmeter.javadsl.JmeterDsl.jsr223PreProcessor;
import static us.abstracta.jmeter.javadsl.JmeterDsl.testPlan;

public class OneTest {
    private CommonTestParameters commonTestParameters;
    private HashMap<String, Profile> profileParameters;
    private final EmbeddedJmeterEngine embeddedJmeterEngine = new EmbeddedJmeterEngine();

    @BeforeTest
    public void init() throws InterruptedException {
        DurationHelper.startTime();
        Properties properties = PropertyHelper.readProperties(
                "common/common_properties.json",
                "common/redis.json",
                "module_two/module_two_properties.json",
                "module_two/module_two_groups/group_two_properties.json",
                "module_two/module_two_tests/test_two/test_two.json"
        );

        // Common Parameters
        commonTestParameters = new CommonTestParameters(properties);

        // Profile Parameters
        profileParameters = PropertyHelper.profileToMap(properties);

        // Смена порта Redis Client
        RedisClientFragment.setRedisClientPort(Integer.parseInt(properties.getProperty("REDIS_CLIENT_PORT")));

        // Data Base Parameters, Kafka Parameters ...
        // ...
        // ...
        // ...

        PropertyHelper.setPropertiesToEngine(embeddedJmeterEngine, properties);
        DurationHelper.waitTime(commonTestParameters.getWait());
    }

    @Test(testName = "OneTest")
    public void test() throws IOException, InterruptedException, TimeoutException {
        testPlan(
                getHttpDefaults(),
                getCookiesDisable(),
                getCacheDisable(),
                getRpsThreadGroup("THREAD_GROUP_ONE", profileParameters.get("THREAD_GROUP_ONE"), commonTestParameters.getDebugEnable())
                        .children(
                                RedisClientFragment.readList(RedisReadType.FIRST)
                                // StatusFragment.get()
                        ),

                getRpsThreadGroup("THREAD_GROUP_TWO", profileParameters.get("THREAD_GROUP_TWO"), commonTestParameters.getDebugEnable())
                        .children(
                                // StatusFragment.get(),
                                RedisClientFragment.addList(RedisAddType.LAST)
                                        .children(
                                                jsr223PreProcessor(s -> s.vars.put("redis_client_data", "{ ... }"))
                                        )
                        ),
                xmlErrorLog(commonTestParameters.getLogPath(), commonTestParameters.getTestName(), commonTestParameters.getErrorLogEnable()),
                influxDbLog(commonTestParameters.getInfluxDbLogEnable()),
                resultTree(commonTestParameters.getResultTreeEnable()),
                resultDashboard(commonTestParameters.getResultDashboardEnable())
        ).runIn(embeddedJmeterEngine);
    }
}
