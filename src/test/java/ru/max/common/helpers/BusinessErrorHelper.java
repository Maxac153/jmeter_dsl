package ru.max.common.helpers;

public class BusinessErrorHelper {
    public static String getBusinessError(double limit, String correctValue, Boolean debugEnable) {
        if (Math.random() * 100 < limit && !debugEnable)
            return "BUSINESS_ERROR";

        return correctValue;
    }
}
