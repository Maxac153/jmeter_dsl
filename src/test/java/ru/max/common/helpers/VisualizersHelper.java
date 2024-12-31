package ru.max.common.helpers;

import us.abstracta.jmeter.javadsl.core.DslTestPlan.TestPlanChild;

import java.time.Duration;

import static us.abstracta.jmeter.javadsl.JmeterDsl.constantTimer;
import static us.abstracta.jmeter.javadsl.JmeterDsl.resultsTreeVisualizer;
import static us.abstracta.jmeter.javadsl.dashboard.DashboardVisualizer.dashboardVisualizer;

public class VisualizersHelper {
    public static TestPlanChild resultTree(boolean enable) {
        if (enable)
            return resultsTreeVisualizer();

        return constantTimer(Duration.ZERO);
    }

    public static TestPlanChild resultTree() {
        return resultsTreeVisualizer();
    }

    public static TestPlanChild resultDashboard(boolean enable) {
        if (enable)
            return dashboardVisualizer();

        return constantTimer(Duration.ZERO);
    }

    public static TestPlanChild resultDashboard() {
        return dashboardVisualizer();
    }
}
