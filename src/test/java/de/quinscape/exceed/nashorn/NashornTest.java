package de.quinscape.exceed.nashorn;


import de.quinscape.exceed.runtime.ExceedRuntimeException;
import de.quinscape.exceed.runtime.util.SetImmediateFunction;
import org.junit.Ignore;
import org.junit.Test;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleScriptContext;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.*;

public class NashornTest
{
    private static final int REPEAT = 10000;

    private Random random = new Random();


    @Test
    @Ignore
    public void testThreading() throws Exception
    {
        ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");

        final SetImmediateFunction setImmediateFunction = new SetImmediateFunction();
        engine.put("setImmediate", setImmediateFunction);

        engine.eval(new FileReader("./src/test/java/de/quinscape/exceed/nashorn/dump.js"));
        engine.eval(new FileReader("./src/test/java/de/quinscape/exceed/nashorn/promise.js"));
        engine.eval(new FileReader("./src/test/java/de/quinscape/exceed/nashorn/promise-test.js"));

        ExecutorService svc = null;
        try
        {
            svc = Executors.newFixedThreadPool(16);

            List<Future<?>> futures = new ArrayList<>(REPEAT);
            for (int i = 0; i < REPEAT; i++)
            {
                Future<?> f = svc.submit(new AsyncJsTask(engine));
                futures.add(f);
            }

            for (int i = 0; i < REPEAT; i++)
            {
                futures.get(i).get();
            }

        }
        finally
        {
            if (svc != null)
            {
                svc.shutdown();
            }

        }

    }


    private class AsyncJsTask
        implements Runnable
    {
        private final ScriptEngine engine;

        private final ThreadLocal<ScriptContext> scriptContextThreadLocal;


        public AsyncJsTask(ScriptEngine engine) throws FileNotFoundException, ScriptException
        {
            this.engine = engine;

            scriptContextThreadLocal = ThreadLocal.withInitial(() ->
            {
                try
                {
                    ScriptContext scriptContext = new SimpleScriptContext();
                    Bindings defaultBindings = engine.getBindings(ScriptContext.ENGINE_SCOPE);

                    Bindings bindings = engine.createBindings();
                    bindings.putAll(defaultBindings);
                    scriptContext.setBindings(bindings, ScriptContext.ENGINE_SCOPE);

                    engine.eval(new FileReader("./src/test/java/de/quinscape/exceed/nashorn/per-task.js"),
                        scriptContext);

                    return scriptContext;
                }
                catch (Exception e)
                {
                    throw new ExceedRuntimeException(e);
                }
            });

        }


        @Override
        public void run()
        {
            try
            {
                ScriptContext scriptContext = scriptContextThreadLocal.get();

                final int value = random.nextInt();
                final int delay = 1 + random.nextInt(9);

                scriptContext.setAttribute("number", value, ScriptContext.ENGINE_SCOPE);
                assertThat(scriptContext.getAttribute("number"), is(value));

                Thread.sleep(delay);

                assertThat(scriptContext.getAttribute("number"), is(value));

            }
            catch (InterruptedException e)
            {
                throw new ExceedRuntimeException(e);
            }
        }
    }
}
