package de.quinscape.exceed.nashorn;

import de.quinscape.exceed.runtime.util.SetImmediateFunction;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.io.FileReader;

public class BundleTest
{

    private final static Logger log = LoggerFactory.getLogger(BundleTest.class);


    @Test
    @Ignore
    public void test() throws Exception
    {
        ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");

        final SetImmediateFunction setImmediateFunction = new SetImmediateFunction();
        engine.put("setImmediate", setImmediateFunction);

        engine.eval(new FileReader("./src/main/base/resources/js/exceed-server.js"));

        Invocable invocable = (Invocable) engine;

        log.info("{}",engine.eval("Exceed.fn()"));

    }

}
