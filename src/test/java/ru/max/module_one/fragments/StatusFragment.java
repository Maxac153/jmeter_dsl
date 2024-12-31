package ru.max.module_one.fragments;

import ru.max.common.processors.preprocessors.DataPreparationClaim;
import us.abstracta.jmeter.javadsl.core.controllers.DslSimpleController;
import us.abstracta.jmeter.javadsl.core.postprocessors.DslJsonExtractor;

import static us.abstracta.jmeter.javadsl.JmeterDsl.*;

public class StatusFragment {
    public static DslSimpleController get() {
        return simpleController("TF_STATUS",
                transaction("UC_STATUS",
                        dummySampler("UR_STATUS", "${response_json}")
                                .requestBody("${request_json}")
                                .children(
                                        jsr223PreProcessor("DATA_PREPARATION", DataPreparationClaim.class),

                                        jsonAssertion("STATUS_CHECK", "status")
                                                .equalsTo("OK"),

                                        jsonExtractor("common_id", "$.id")
                                                .queryLanguage(DslJsonExtractor.JsonQueryLanguage.JSON_PATH)
                                                .matchNumber(1)
                                                .defaultValue("common_id_ERROR"),

                                        jsr223PostProcessor("DATA_PREPARATION_TO_REDIS", s -> {
                                            String commonId = s.vars.get("common_id");
                                            if (!commonId.equals("common_id_ERROR"))
                                                s.vars.put("redis_client_data", commonId);
                                        })
                                )
                )
        );
    }
}
