package de.quinscape.exceed.runtime.js.env;

import de.quinscape.exceed.runtime.ExceedRuntimeException;
import de.quinscape.exceed.runtime.RuntimeContext;
import de.quinscape.exceed.runtime.js.JsEnvironment;
import de.quinscape.exceed.runtime.util.JsUtil;
import jdk.nashorn.api.scripting.JSObject;
import jdk.nashorn.api.scripting.NashornScriptEngine;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

/**
 * Server-side java wrapper for nashorn promise objects.
 */
public class Promise
{
    private static final long FLUSH_INTERVAL = 5;

    public final static String THEN = "then";
    public final static String RESOLVE = "resolve";
    public final static String REJECT = "reject";

    private final NashornScriptEngine scriptEngine;
    private final ScriptContext scriptContext;
    private final JSObject promise;


    private PromiseState state = PromiseState.PENDING;

    private Object result;


    public Promise(NashornScriptEngine scriptEngine, ScriptContext scriptContext, JSObject promise)
    {
        this.scriptEngine = scriptEngine;
        this.scriptContext = scriptContext;

        final JsFunctionAdapter<Object> successCallback = new JsFunctionAdapter<>(this::resolve);
        final JsFunctionAdapter<Object> failCallback = new JsFunctionAdapter<>(this::reject);

        this.promise = (JSObject) ((JSObject)promise.getMember(THEN)).call(promise, successCallback, failCallback);

        JsUtil.flushImmediate(scriptContext);
    }

    public PromiseState getState()
    {
        return state;
    }

    /**
     * Returns the script engine this promise was created with.
     *
     * @return script engine
     */
    public ScriptEngine getScriptEngine()
    {
        return scriptEngine;
    }


    /**
     * Returns the script context this promise was created with.
     *
     * @return script context
     */
    public ScriptContext getScriptContext()
    {
        return scriptContext;
    }


    /**
     * Returns the underlying nashorn promise object.
     * <p>
     *     This will be the object resulting in having added this promise success/fail handlers to the originally wrapped
     *     promise.
     * </p>
     *
     * @return promise object
     */
    public JSObject getPromise()
    {
        return promise;
    }


    /**
     *  Convenience method to fetch the error message of a rejected promise.
     *
     * @return  stringified result if state is {@link PromiseState#REJECTED}.
     *
     * @throws IllegalStateException if the state is not {@link PromiseState#REJECTED}.
     */
    public String getErrorMessage()
    {
        if (state != PromiseState.REJECTED)
        {
            throw new IllegalStateException("Promise is not REJECTED: " + state);
        }
        return result.toString();
    }

    public Object getResult()
    {
        return result;
    }

    public Object waitForResult() throws InterruptedException
    {
        return waitForResult(0);
    }

    public Object waitForResult(final long timeout) throws InterruptedException
    {
        JsUtil.flushImmediate(scriptContext);

        final long start = System.currentTimeMillis();
        final boolean isInfinite = timeout == 0;

        long elapsed = 0;
        while (
            state == PromiseState.PENDING &&
            (isInfinite || elapsed < timeout)
        )
        {
            synchronized (this)
            {
                this.wait(FLUSH_INTERVAL);
            }

            JsUtil.flushImmediate(scriptContext);
            if (!isInfinite)
            {
                elapsed = System.currentTimeMillis() - start;
            }
        }

        return getResult();
    }

    private void ensurePending()
    {
        if (state != PromiseState.PENDING)
        {
            throw new IllegalStateException("Promise no longer pending, is already " + state);
        }
    }

    private void reject(Object reason)
    {
        ensurePending();

        state = PromiseState.REJECTED;
        result = reason;

        synchronized (this)
        {
            this.notifyAll();
        }
    }


    private void resolve(Object result)
    {
        ensurePending();

        state = PromiseState.FULFILLED;
        this.result = result;

        synchronized (this)
        {
            this.notifyAll();
        }
    }

    public static JSObject resolve(RuntimeContext runtimeContext, Object o)
    {
        return promiseFn(runtimeContext, RESOLVE, o);
    }

    public static JSObject reject(RuntimeContext runtimeContext, Object o)
    {
        return promiseFn(runtimeContext, REJECT, o);
    }

    private static JSObject promiseFn(RuntimeContext runtimeContext, String function, Object o)
    {
        final JsEnvironment env = runtimeContext.getJsEnvironment();
        final NashornScriptEngine nashorn = env.getNashorn();
        final ScriptContext scriptContext = env.getScriptContext(runtimeContext);


        try
        {
            return (JSObject) nashorn.invokeMethod(
                scriptContext.getAttribute("Promise"),
                function,
                o
            );
        }
        catch (ScriptException | NoSuchMethodException e)
        {
            throw new ExceedRuntimeException(e);
        }
    }

}
