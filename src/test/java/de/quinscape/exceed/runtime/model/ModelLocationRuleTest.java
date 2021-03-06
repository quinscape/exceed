package de.quinscape.exceed.runtime.model;

import de.quinscape.exceed.runtime.util.JSONUtil;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

public class ModelLocationRuleTest
{
    @Test
    public void thatRulesWork() throws Exception
    {
        assertThat(match(ModelLocationRules.VIEW_MODEL_PREFIX,"/models/view/test.json"), is(true));
        assertThat(match(ModelLocationRules.VIEW_MODEL_PREFIX, "/models/config.json"), is(false));
        assertThat(match(ModelLocationRules.CONFIG_MODEL_NAME, "/models/config.json"), is(true));
        assertThat(match(ModelLocationRules.PROCESS_VIEW_MODEL_PATTERN,"/models/view/test.json"), is(false));
        assertThat(match(ModelLocationRules.PROCESS_VIEW_MODEL_PATTERN,"/models/process/test/view/list.json"), is(true));
    }


    private boolean match(String s, String path)
    {
        final ModelLocationRule modelLocationRule = new ModelLocationRule(s, "test.Test");

        System.out.println("matchLocationRule(" + JSONUtil.DEFAULT_GENERATOR.forValue(modelLocationRule) +", " + JSONUtil.DEFAULT_GENERATOR.forValue(path) + ")\n");

        return modelLocationRule.matches(path);
    }
}
