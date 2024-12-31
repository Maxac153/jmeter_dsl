package ru.max.common.helpers;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DurationHelper {
    private static final Logger logger = LogManager.getLogger(DurationHelper.class);
    private static long startTimerInit = -1;

    public static void startTime() {
        startTimerInit = System.nanoTime();
        String formattedDateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"));
        logger.info("Current date and time: " + formattedDateTime);
    }

    public static void waitTime(String wait) throws InterruptedException {
        long waitTime = Long.parseLong(wait) * 1_000_000_000 - (System.nanoTime() - startTimerInit);
        if (waitTime < 0)
            logger.warn("Incorrect wait time: " + Math.round(((double) waitTime / 1_000_000_000) * 100.00) / 100.00 + " sec!!!");
        else {
            logger.info("Wait time: " + Math.round(((double) waitTime / 1_000_000_000) * 100.00) / 100.00 + " sec");
            Thread.sleep(waitTime / 1_000_000);
            logger.info("End of the wait: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")));
        }
    }
}
