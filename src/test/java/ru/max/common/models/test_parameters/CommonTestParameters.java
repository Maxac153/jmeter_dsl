package ru.max.common.models.test_parameters;

import lombok.Getter;

import java.util.Properties;

@Getter
public class CommonTestParameters {
    private final String wait;
    private final String logPath;
    private final String testName;
    private final Boolean debugEnable;
    private final Boolean errorLogEnable;
    private final Boolean influxDbLogEnable;
    private final Boolean resultTreeEnable;
    private final Boolean resultDashboardEnable;

    public CommonTestParameters(Properties properties) {
        wait = properties.getProperty("WAIT");
        logPath = properties.getProperty("LOG_PATH");
        testName = properties.getProperty("TEST_NAME");
        debugEnable = Boolean.parseBoolean(properties.getProperty("DEBUG_ENABLE"));
        errorLogEnable = Boolean.parseBoolean(properties.getProperty("ERROR_LOG_ENABLE"));
        influxDbLogEnable = Boolean.parseBoolean(properties.getProperty("INFLUX_DB_LOG_ENABLE"));
        resultTreeEnable = Boolean.parseBoolean(properties.getProperty("RESULT_TREE_ENABLE"));
        resultDashboardEnable = Boolean.parseBoolean(properties.getProperty("RESULT_DASHBOARD_ENABLE"));
    }
}
