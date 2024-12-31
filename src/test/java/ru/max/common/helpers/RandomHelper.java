package ru.max.common.helpers;

import net.datafaker.Faker;

public class RandomHelper {
    private static final Faker faker = new Faker();

    public static String getName() {
        return faker.name().fullName();
    }
}
