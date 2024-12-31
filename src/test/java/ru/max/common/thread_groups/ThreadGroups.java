package ru.max.common.thread_groups;

import ru.max.common.models.profile.Profile;
import ru.max.common.models.profile.Step;
import us.abstracta.jmeter.javadsl.core.threadgroups.BaseThreadGroup;
import us.abstracta.jmeter.javadsl.core.threadgroups.DslDefaultThreadGroup;
import us.abstracta.jmeter.javadsl.core.threadgroups.RpsThreadGroup;

import java.time.Duration;

import static ru.max.common.helpers.ActionHelper.testAction;
import static us.abstracta.jmeter.javadsl.JmeterDsl.*;

public class ThreadGroups {
    private static int getThreads(Double rps, Double throughput) {
        double pacing = (1 / throughput) * 60;
        double requiredIntensityPerMinutes = rps * 60;
        return (int) Math.ceil(requiredIntensityPerMinutes / (60 / pacing));
    }

    private static double getThroughputPerMinute(Double rps, Integer scriptExecutionTime, Integer pacingMultiplier) {
        double pacing = scriptExecutionTime * pacingMultiplier;
        double requiredIntensityPerMinutes = rps * 60;
        int threads = (int) Math.ceil(requiredIntensityPerMinutes / (60 / pacing));
        double executionTime = threads / rps;
        double scale = Math.pow(10, 3);
        return Math.ceil(60 / executionTime * scale) / scale;
    }

    public static BaseThreadGroup<?> getThreadGroup(String name, Profile profile, Boolean debugEnable) {
        if (debugEnable) {
            return threadGroup(name, 1, 1);
        } else {
            double throughputPerMinute = getThroughputPerMinute(
                    profile.getSteps().get(0).getTps(),
                    profile.getScriptExecutionTime(),
                    profile.getPacingMultiplier()
            );

            DslDefaultThreadGroup loadProfile = threadGroup(name)
                    .children(testAction(throughputTimer(throughputPerMinute).perThread()));

            for (Step step : profile.getSteps()) {
                int threads = getThreads(step.getTps(), throughputPerMinute);
                loadProfile.rampToAndHold(threads,
                        Duration.ofSeconds((int) Math.ceil(step.getRampTime() * 60)),
                        Duration.ofSeconds((int) Math.ceil(step.getHoldTime() * 60))
                );
            }

            return loadProfile;
        }
    }

    public static BaseThreadGroup<?> getRpsThreadGroup(String name, Profile profile, Boolean debugEnable) {
        if (debugEnable) {
            return threadGroup(name, 1, 1);
        } else {
            RpsThreadGroup loadProfile = rpsThreadGroup(name)
                    .counting(RpsThreadGroup.EventType.ITERATIONS)
                    .maxThreads(profile.getMaxThreads());

            for (Step step : profile.getSteps()) {
                loadProfile.rampToAndHold(step.getTps(),
                        Duration.ofSeconds((int) Math.ceil(step.getRampTime() * 60)),
                        Duration.ofSeconds((int) Math.ceil(step.getHoldTime() * 60))
                );
            }

            return loadProfile;
        }
    }
}
