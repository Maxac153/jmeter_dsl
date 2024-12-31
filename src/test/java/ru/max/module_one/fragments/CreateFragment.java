package ru.max.module_one.fragments;

import ru.max.common.processors.preprocessors.DataPreparationClaim;
import us.abstracta.jmeter.javadsl.core.controllers.DslSimpleController;
import us.abstracta.jmeter.javadsl.core.postprocessors.DslJsonExtractor;

import static us.abstracta.jmeter.javadsl.JmeterDsl.*;

public class CreateFragment {
    public static DslSimpleController get() {
        return simpleController("TF_CREATE",
                transaction("UC_CREATE",
                        dummySampler("UR_CREATE", "${response_json}")
                                .requestBody("${request_json}")
                                .children(
                                        jsr223PreProcessor("DATA_PREPARATION", DataPreparationClaim.class),

                                        jsonAssertion("STATUS_CHECK", "status")
                                                .equalsTo("OK"),

                                        jsonExtractor("common_id", "$.id")
                                                .queryLanguage(DslJsonExtractor.JsonQueryLanguage.JSON_PATH)
                                                .matchNumber(1)
                                                .defaultValue("common_id_ERROR")
                                )
                )
        );
    }
}
