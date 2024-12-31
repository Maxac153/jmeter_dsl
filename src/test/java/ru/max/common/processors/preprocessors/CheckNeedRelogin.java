package ru.max.common.processors.preprocessors;

import us.abstracta.jmeter.javadsl.core.preprocessors.DslJsr223PreProcessor.PreProcessorScript;
import us.abstracta.jmeter.javadsl.core.preprocessors.DslJsr223PreProcessor.PreProcessorVars;

import java.util.Arrays;

public class CheckNeedRelogin implements PreProcessorScript {
    @Override
    public void runScript(PreProcessorVars s) {
        s.vars.put("need_relogin", "false");

        String[] responseCodes = {"401", "403"};
        String prevResponseCode = s.prev.getResponseCode();

        if (!s.prev.equals("null") && !s.prev.isSuccessful()) {
            if (Arrays.asList(responseCodes).contains(prevResponseCode)) {
                s.vars.put("need_relogin", "true");
            }
        }
    }
}
