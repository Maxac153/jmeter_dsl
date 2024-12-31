package ru.max.common.helpers;

import org.apache.jmeter.sampler.TestAction;
import us.abstracta.jmeter.javadsl.core.preprocessors.DslJsr223PreProcessor.PreProcessorScript;
import us.abstracta.jmeter.javadsl.core.samplers.BaseSampler.SamplerChild;
import us.abstracta.jmeter.javadsl.wrapper.wrappers.DslSamplerWrapper;

import static us.abstracta.jmeter.javadsl.JmeterDsl.jsr223PreProcessor;
import static us.abstracta.jmeter.javadsl.wrapper.WrapperJmeterDsl.testElement;

public class ActionHelper {
    public static TestAction emptyAction() {
        TestAction action = new TestAction();
        action.setAction(1);
        action.setDuration("0");
        return action;
    }

    public static DslSamplerWrapper testAction(SamplerChild... children) {
        return testElement(emptyAction()).children(children);
    }

    public static DslSamplerWrapper jsr223Action(PreProcessorScript script) {
        return testElement(emptyAction()).children(
                jsr223PreProcessor(script)
        );
    }

    public static DslSamplerWrapper jsr223Action(Class<? extends PreProcessorScript> scriptClass) {
        return testElement(emptyAction()).children(
                jsr223PreProcessor(scriptClass)
        );
    }

    public static DslSamplerWrapper jsr223Action(String name, PreProcessorScript script) {
        return testElement(emptyAction()).children(
                jsr223PreProcessor(name, script)
        );
    }

    public static DslSamplerWrapper jsr223Action(String name, Class<? extends PreProcessorScript> scriptClass) {
        return testElement(emptyAction()).children(
                jsr223PreProcessor(name, scriptClass)
        );
    }
}
