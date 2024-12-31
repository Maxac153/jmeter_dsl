package ru.max.common.helpers;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import ru.max.common.models.profile.Profile;
import ru.max.common.models.profile.TestParam;
import us.abstracta.jmeter.javadsl.core.engines.EmbeddedJmeterEngine;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;


public class PropertyHelper {
    private static final Gson gson = new Gson();

    public static String readProperty(String filePath) {
        StringBuilder contentBuilder = new StringBuilder();
        try (InputStream inputStream = PropertyHelper.class.getClassLoader().getResourceAsStream(filePath)) {
            String line;
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            while ((line = reader.readLine()) != null) {
                contentBuilder.append(line).append(System.lineSeparator());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return contentBuilder.toString();
    }

    static public void setPropertiesToEngine(EmbeddedJmeterEngine engine, Properties properties) {
        properties.forEach((key, value) -> engine.prop(key.toString(), value));
    }

    public static Properties readProperties(
            String... pathsProperties
    ) {
        String profile = null;
        Properties properties = new Properties();

        // Параметры и профиль из файлов
        for (String pathProperties : pathsProperties) {
            TestParam testParam = gson.fromJson(PropertyHelper.readProperty(pathProperties), TestParam.class);
            if (testParam.getProperties() != null)
                testParam.getProperties().forEach(properties::setProperty);

            if (testParam.getProfile() != null)
                profile = gson.toJson(testParam.getProfile());
        }

        // Общие параметры из системы
        TestParam systemCommonProperties = gson.fromJson(System.getProperty("COMMON_SETTINGS"), TestParam.class);
        if (systemCommonProperties != null)
            systemCommonProperties.getProperties().forEach(properties::setProperty);

        // Параметры теста и параметры профиля из системы
        TestParam systemTestProperties = gson.fromJson(System.getProperty("TEST_SETTINGS"), TestParam.class);
        if (systemTestProperties != null) {
            systemTestProperties.getProperties().forEach(properties::setProperty);
            profile = gson.toJson(systemTestProperties.getProfile());
        }

        properties.setProperty("PROFILE", profile);
        return properties;
    }

    public static HashMap<String, Profile> profileToMap(Properties properties) {
        HashMap<String, Profile> profileMap = new HashMap<>();
        Type profileListType = new TypeToken<ArrayList<Profile>>() {
        }.getType();
        ArrayList<Profile> profiles = gson.fromJson(properties.getProperty("PROFILE"), profileListType);

        // Создание Map<threadGroupName, profile> профилей для каждой нагрузочной катушки в тесте
        for (Profile profile : profiles)
            profileMap.put(profile.getThreadGroupName(), profile);

        return profileMap;
    }
}
