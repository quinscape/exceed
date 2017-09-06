package de.quinscape.exceed.nashorn;

import de.quinscape.exceed.runtime.util.SetImmediateFunction;
import jdk.nashorn.api.scripting.AbstractJSObject;
import jdk.nashorn.api.scripting.ScriptObjectMirror;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.Invocable;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.SimpleScriptContext;
import java.io.FileReader;
import java.util.Arrays;

public class PromiseTest
{

    private final static Logger log = LoggerFactory.getLogger(PromiseTest.class);


    @Test
    @Ignore
    public void test() throws Exception
    {
        ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");

        final SetImmediateFunction setImmediateFunction = new SetImmediateFunction();
        engine.put("setImmediate", setImmediateFunction);

        engine.eval(new FileReader("./src/test/java/de/quinscape/exceed/nashorn/dump.js"));
        engine.eval(new FileReader("./src/test/java/de/quinscape/exceed/nashorn/promise.js"));
        engine.eval(new FileReader("./src/test/java/de/quinscape/exceed/nashorn/promise-test.js"));

        Invocable invocable = (Invocable) engine;

        ScriptObjectMirror result = (ScriptObjectMirror) invocable.invokeFunction("fn", new PromiseHandler("success"),  new PromiseHandler("fail"));
        setImmediateFunction.flush();

        ScriptContext scriptContext = new SimpleScriptContext();

    }


    public class PromiseHandler
        extends AbstractJSObject
    {
        private final String info;


        private PromiseHandler(String info)
        {
            this.info = info;
        }


        @Override
        public Object call(Object thiz, Object... args)
        {
            log.info("{}: {}", info, Arrays.toString(args));
            return null;
        }

        @Override
        public boolean isFunction()
        {
            return true;
        }
    }
}
