import jdk.nashorn.api.scripting.AbstractJSObject;
import jdk.nashorn.api.scripting.ScriptObjectMirror;
import org.junit.Ignore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class PromiseTest
{

    private final static Logger log = LoggerFactory.getLogger(PromiseTest.class);

    private final int REPEAT = 100;


    @org.junit.Test
    @Ignore
    public void test() throws Exception
    {
        ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");

        final SetImmediateFunction setImmediateFunction = new SetImmediateFunction();
        engine.put("setImmediate", setImmediateFunction);

        engine.eval(new FileReader("./src/test/java/dump.js"));
        engine.eval(new FileReader("./src/test/java/promise.js"));
        engine.eval(new FileReader("./src/test/java/promise-test.js"));

        Invocable invocable = (Invocable) engine;

        long start = System.nanoTime();

        final PromiseHandler success = new PromiseHandler("success");
        final PromiseHandler fail = new PromiseHandler("fail");

        for (int i=0; i < REPEAT; i++)
        {
            ScriptObjectMirror result = (ScriptObjectMirror) invocable.invokeFunction("fn", success, fail);
            setImmediateFunction.flush();
        }

        log.info("{} ms", TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start) * 1.0 / REPEAT);


    }

    public class SetImmediateFunction
        extends AbstractJSObject
    {
        public SetImmediateFunction()
        {
            log.info("CTOR");
        }

        private List<ScriptObjectMirror> queue = new ArrayList<>();

        @Override
        public Object call(Object thiz, Object... args)
        {
            if (args.length == 0)
            {
                throw new IllegalStateException("No args");
            }

            final Object fn = args[0];
            //log.info("Adding {}", fn);

            queue.add((ScriptObjectMirror) fn);
            return null;
        }

        public void flush()
        {
            while (queue.size() > 0)
            {
                final ScriptObjectMirror top = queue.remove(0);
                //log.info("Flushing", top);
                top.call(null, 0);
            }
        }
        @Override
        public Object getDefaultValue(Class<?> hint)
        {
            return "[SetImmediateFunction]";
        }


        @Override
        public boolean isFunction()
        {
            return true;
        }
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
            log.debug("{}: {}", info, Arrays.toString(args));
            return null;
        }


        @Override
        public Object getDefaultValue(Class<?> hint)
        {
            return "[Handler: " + info + "]";
        }


        @Override
        public boolean isFunction()
        {
            return true;
        }
    }
}
