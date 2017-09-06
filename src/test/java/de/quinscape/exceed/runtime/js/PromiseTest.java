package de.quinscape.exceed.runtime.js;

import de.quinscape.exceed.runtime.js.env.Promise;
import de.quinscape.exceed.runtime.js.env.PromiseState;
import de.quinscape.exceed.runtime.util.JsUtil;
import jdk.nashorn.api.scripting.NashornScriptEngine;
import jdk.nashorn.api.scripting.ScriptObjectMirror;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.ScriptContext;
import javax.script.ScriptException;
import java.io.FileNotFoundException;
import java.io.FileReader;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

public class PromiseTest
{

    private final static Logger log = LoggerFactory.getLogger(de.quinscape.exceed.nashorn.PromiseTest.class);


    @Test
    public void testFulfillment() throws Exception
    {
        NashornScriptEngine nashorn = createEngine();
        nashorn.eval(new FileReader("./src/test/java/de/quinscape/exceed/runtime/js/promise-success.js"));

        {
            ScriptContext scriptContext = JsUtil.createNewContext(nashorn);

            Promise promise = new Promise(nashorn, scriptContext, (ScriptObjectMirror) nashorn.invokeFunction("succeed"));

            assertThat(promise.getState(), is(PromiseState.FULFILLED));
            assertThat(promise.getResult(), is("Promise Result"));
        }
    }


    @Test
    public void testRejection() throws Exception
    {
        NashornScriptEngine nashorn = createEngine();
        nashorn.eval(new FileReader("./src/test/java/de/quinscape/exceed/runtime/js/promise-fail.js"));
        {
            ScriptContext scriptContext = JsUtil.createNewContext(nashorn);

            Promise promise = new Promise(nashorn, scriptContext, (ScriptObjectMirror) nashorn.invokeFunction("failWithError"));

            assertThat(promise.getState(), is(PromiseState.REJECTED));
            assertThat(promise.getErrorMessage(), is("Error: Boom"));
        }

        {
            ScriptContext scriptContext = JsUtil.createNewContext(nashorn);

            Promise promise = new Promise(nashorn, scriptContext, (ScriptObjectMirror) nashorn.invokeFunction("failWithString"));

            assertThat(promise.getState(), is(PromiseState.REJECTED));
            assertThat(promise.getErrorMessage(), is("KAPOTT!"));
        }
    }

//    @Test
//    public void testWaitForResult()
//    {
//        // XXX: Test async promises
//    }

    private NashornScriptEngine createEngine() throws ScriptException, FileNotFoundException
    {
        NashornScriptEngine engine = JsUtil.createEngine();
        engine.eval(new FileReader("./src/test/java/de/quinscape/exceed/runtime/js/promise.js"));
        return engine;
    }
    
}
