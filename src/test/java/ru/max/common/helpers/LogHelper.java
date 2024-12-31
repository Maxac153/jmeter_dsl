package ru.max.common.helpers;

import us.abstracta.jmeter.javadsl.core.DslTestPlan.TestPlanChild;
import us.abstracta.jmeter.javadsl.core.listeners.JtlWriter;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Date;

import static us.abstracta.jmeter.javadsl.JmeterDsl.*;

public class LogHelper {
    private final static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    private static String filenameCreate(String testName, boolean timestamp, boolean error) throws UnknownHostException {
        String date;
        if (timestamp)
            date = Long.toString(new Date().getTime());
        else
            date = DATE_FORMAT.format(new Date());

        String postfix = "";
        if (error)
            postfix = "_errors";

        return String.format("%s_%s%s_%s", InetAddress.getLocalHost().getHostName(), testName, postfix, date);
    }

    private static String folderPathCreate(String directoryPath, String testName) {
        String date = DATE_FORMAT.format(new Date());
        if (directoryPath.charAt(directoryPath.length() - 1) == '/')
            return String.format("%s%s/%s/", directoryPath, date, testName);

        return String.format("%s/%s/%s/", directoryPath, date, testName);
    }

    public static TestPlanChild xmlErrorLog(String directory, String testName, boolean enable) throws UnknownHostException {
        if (enable) {
            return jtlWriter(folderPathCreate(directory, testName), filenameCreate(testName, false, true) + ".xml")
                    .withSampleAndErrorCounts(true)
                    .withResponseHeaders(true)
                    .withRequestHeaders(true)
                    .withResponseData(true)
                    .withSamplerData(true)
                    .saveAsXml(true)
                    .logOnly(JtlWriter.SampleStatus.ERROR);
        }
        return constantTimer(Duration.ZERO);
    }

    public static TestPlanChild influxDbLog(boolean enable) {
        if (enable) {
            return influxDbListener("http://${__P(INFLUXDB_HOST)}:8086/write?db=jmeter")
                    .title("Test")
                    .samplersRegex(".*")
                    .tag("nodeName", "${__machineName()}")
                    .tag("runId", "Reinsured");
        } else {
            return constantTimer(Duration.ZERO);
        }
    }

    public static TestPlanChild victoriametricsDbLog(boolean enable, String moduleName) {
        if (enable) {
            return influxDbListener("http://${__P(VICTORIAMETRICS_HOST)}/write")
                    .application("test")
                    .title("Test")
                    .percentiles(90, 95, 99)
                    .samplersRegex(".*")
                    .tag("nodeName", "${__machineName()}")
                    .tag("module", moduleName);
        }
        return constantTimer(Duration.ZERO);
    }
}
