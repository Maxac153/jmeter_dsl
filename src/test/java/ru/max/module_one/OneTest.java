package ru.max.module_one;

import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import ru.max.common.models.profile.Profile;
import ru.max.common.models.test_parameters.CommonTestParameters;
import ru.max.common.helpers.DurationHelper;
import ru.max.common.helpers.PropertyHelper;
import ru.max.module_one.fragments.CreateFragment;
import us.abstracta.jmeter.javadsl.core.engines.EmbeddedJmeterEngine;

import java.io.IOException;
import java.util.HashMap;
import java.util.Properties;
import java.util.concurrent.TimeoutException;

import static ru.max.common.helpers.ActionHelper.jsr223Action;
import static ru.max.common.helpers.CacheHelper.getCacheDisable;
import static ru.max.common.helpers.CookiesHelper.getCookiesDisable;
import static ru.max.common.helpers.HttpHelper.getHttpDefaults;
import static ru.max.common.helpers.LogHelper.influxDbLog;
import static ru.max.common.helpers.LogHelper.xmlErrorLog;
import static ru.max.common.helpers.VisualizersHelper.resultDashboard;
import static ru.max.common.helpers.VisualizersHelper.resultTree;
import static ru.max.common.thread_groups.ThreadGroups.getThreadGroup;
import static us.abstracta.jmeter.javadsl.JmeterDsl.setupThreadGroup;
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
                "module_one/module_one_properties.json",
                "module_one/module_one_groups/group_one_properties.json",
                "module_one/module_one_tests/test_one/test_one.json"
        );

        // Common Parameters
        commonTestParameters = new CommonTestParameters(properties);

        // Profile Parameters
        profileParameters = PropertyHelper.profileToMap(properties);

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
                getThreadGroup("THREAD_GROUP_ONE", profileParameters.get("THREAD_GROUP_ONE"), commonTestParameters.getDebugEnable())
                        .children(
                                // Действия перед запросом
                                jsr223Action(s -> s.vars.put("counter", Integer.toString(0))),
                                CreateFragment.get()
                        ),
                xmlErrorLog(commonTestParameters.getLogPath(), commonTestParameters.getTestName(), commonTestParameters.getErrorLogEnable()),
                influxDbLog(commonTestParameters.getInfluxDbLogEnable()),
                resultTree(commonTestParameters.getResultTreeEnable()),
                resultDashboard(commonTestParameters.getResultDashboardEnable())
        ).runIn(embeddedJmeterEngine);
    }
}
