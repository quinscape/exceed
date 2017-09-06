package de.quinscape.exceed.runtime.application;

import de.quinscape.exceed.model.annotation.InjectResource;
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

    @Test
    public void testInjection() throws Exception
    {
        ResourceInjector injector = new ResourceInjector(TestInjectionTarget.class);

        assertThat(injector.getProperties().size(), is(3));

        ResourceLoader resourceLoader = new ResourceLoader(Collections.singletonList(new ClassPathResourceRoot("injector-base")));

        final TestInjectionTarget target = new TestInjectionTarget();
        final NashornScriptEngine engine = JsUtil.createEngine();
        injector.injectResources(engine, resourceLoader, target);

        assertThat(target.getStringValue(), is ("bla bla bla\n"));

        ScriptContext scriptContext = JsUtil.createNewContext(engine);
        String value = "v" + Math.random();

        target.getScript().eval(scriptContext);
        scriptContext.setAttribute("globVar", value, ScriptContext.ENGINE_SCOPE);

        assertThat((String)(((ScriptObjectMirror)scriptContext.getAttribute("fn")).call(null)), is("result:" + value));

        assertThat(target.getSub().getProp(), is("Prop Value"));

    }


    @Test(expected = IllegalArgumentException.class)
    public void testMismatch() throws Exception
    {
        ResourceLoader resourceLoader = new ResourceLoader(Collections.singletonList(new ClassPathResourceRoot("injector-base")));
        final NashornScriptEngine engine = JsUtil.createEngine();
        final ResourceInjector injector = new ResourceInjector(TestInjectionTarget.class);
        injector.injectResources(engine, resourceLoader, new TestInjectionSub());
    }


    public static class TestInjectionTarget
    {
        private CompiledScript script;
        private String stringValue;
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
