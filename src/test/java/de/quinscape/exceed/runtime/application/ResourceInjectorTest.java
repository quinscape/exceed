package de.quinscape.exceed.runtime.application;

import com.google.common.collect.ImmutableMap;
import de.quinscape.exceed.model.annotation.InjectResource;
import de.quinscape.exceed.model.view.View;
import de.quinscape.exceed.runtime.TestApplication;
import de.quinscape.exceed.runtime.TestApplicationBuilder;
import de.quinscape.exceed.runtime.resource.DefaultResourceLoader;
import de.quinscape.exceed.runtime.resource.ResourceLoader;
import de.quinscape.exceed.runtime.resource.stream.ClassPathResourceRoot;
import de.quinscape.exceed.runtime.util.JsUtil;
import jdk.nashorn.api.scripting.NashornScriptEngine;
import jdk.nashorn.api.scripting.ScriptObjectMirror;
import org.junit.Test;

import javax.script.CompiledScript;
import javax.script.ScriptContext;
import java.util.Collections;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

public class ResourceInjectorTest
{

    private final TestApplication app = new TestApplicationBuilder().withView(new View("test")).build();

    private ResourceInjector injector = new ResourceInjector(TestInjectionTarget.class, ImmutableMap.of(
        "alwaysFalse", applicationModel -> false,
        "alwaysTrue", applicationModel -> true
    ));


    @Test
    public void testInjection() throws Exception
    {
        assertThat(injector.getProperties().size(), is(5));

        ResourceLoader resourceLoader = new DefaultResourceLoader(
            Collections.singletonList(new ClassPathResourceRoot("injector-base")));

        final TestInjectionTarget target = new TestInjectionTarget();
        final NashornScriptEngine engine = JsUtil.createEngine();
        injector.injectResources(app.getApplicationModel(), engine, resourceLoader, target);

        assertThat(target.getStringValue().trim(), is("bla bla bla"));

        ScriptContext scriptContext = JsUtil.createNewContext(engine);
        String value = "v" + Math.random();

        target.getScript().eval(scriptContext);
        scriptContext.setAttribute("globVar", value, ScriptContext.ENGINE_SCOPE);

        assertThat(
            (String) (((ScriptObjectMirror) scriptContext.getAttribute("fn")).call(null)), is("result:" + value));

        assertThat(target.getSub().getProp(), is("Prop Value"));

        assertThat(target.getStrategyFalse(), is(nullValue()));
        assertThat(target.getStrategyTrue().trim(), is("bla bla bla"));
    }


    @Test(expected = IllegalArgumentException.class)
    public void testMismatch() throws Exception
    {
        ResourceLoader resourceLoader = new DefaultResourceLoader(
            Collections.singletonList(new ClassPathResourceRoot("injector-base")));
        final NashornScriptEngine engine = JsUtil.createEngine();
        injector.injectResources(app.getApplicationModel(), engine, resourceLoader, new TestInjectionSub());
    }


    public static class TestInjectionTarget
    {
        private CompiledScript script;

        private String stringValue;

        private String strategyFalse;

        private String strategyTrue;

        private TestInjectionSub sub;


        public CompiledScript getScript()
        {
            return script;
        }


        @InjectResource("/resources/js/test.js")
        public void setScript(CompiledScript script)
        {
            this.script = script;
        }


        public String getStringValue()
        {
            return stringValue;
        }


        @InjectResource("/resources/js/test.txt")
        public void setStringValue(String stringValue)
        {
            this.stringValue = stringValue;
        }


        public TestInjectionSub getSub()
        {
            return sub;
        }


        @InjectResource("/resources/js/sub.json")
        public void setSub(TestInjectionSub sub)
        {
            this.sub = sub;
        }


        @InjectResource(value = "/resources/js/test.txt", predicate = "alwaysFalse")
        public String getStrategyFalse()
        {
            return strategyFalse;
        }


        public void setStrategyFalse(String strategyFalse)
        {
            this.strategyFalse = strategyFalse;
        }


        @InjectResource(value = "/resources/js/test.txt", predicate = "alwaysTrue")
        public String getStrategyTrue()
        {
            return strategyTrue;
        }


        public void setStrategyTrue(String strategyTrue)
        {
            this.strategyTrue = strategyTrue;
        }
    }

    public static class TestInjectionSub
    {
        private String prop;


        public String getProp()
        {
            return prop;
        }


        public void setProp(String prop)
        {
            this.prop = prop;
        }
    }

}
