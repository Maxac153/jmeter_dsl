package ru.max.common.processors.preprocessors;


import com.google.gson.Gson;
import ru.max.common.helpers.BusinessErrorHelper;
import ru.max.common.helpers.RandomHelper;
import ru.max.module_two.models.Claim;
import us.abstracta.jmeter.javadsl.core.preprocessors.DslJsr223PreProcessor.PreProcessorScript;
import us.abstracta.jmeter.javadsl.core.preprocessors.DslJsr223PreProcessor.PreProcessorVars;

import java.util.UUID;

public class DataPreparationClaim implements PreProcessorScript {
    private static final Gson gson = new Gson();

    @Override
    public void runScript(PreProcessorVars s) {
        String type = BusinessErrorHelper.getBusinessError(
                5.0,
                "6",
                Boolean.parseBoolean(s.props.getProperty("DEBUG_ENABLE"))
        );

        s.vars.put("request_json",
                gson.toJson(Claim.builder()
                        .name(RandomHelper.getName())
                        .type(type)
                        .amount(123456.0)
                        .build()
                ));

        if (type.equals("BUSINESS_ERROR"))
            s.vars.put("response_json", "{ \"massage\": \"Error type: " + type + "\" }");
        else
            s.vars.put("response_json", "{ \"id\": \"" + UUID.randomUUID() + "\", \"status\": \"OK\" }");

    }
}
