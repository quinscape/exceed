package de.quinscape.exceed.runtime.util;

import jdk.nashorn.api.scripting.AbstractJSObject;
import jdk.nashorn.api.scripting.ScriptObjectMirror;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Implements the (unstandardized) "setImmediate" function for a nashorn environment.
 * <p>
 *     The js-side Promise polyfill we use (NPM "es6-promise-polyfill") supports both working with setImmediate and setTimeout.
 *     setImmediate might not be standard in a browser-environment, but it is more performant then the setTimeout solution and
 *     also for our purposes much easier to implement.
 * </p>
 * <p>
 *     Effectively this is a FIFO queue for Nashorn js functions.
 * </p>
 */
public class SetImmediateFunction
    extends AbstractJSObject
{
    private final static Logger log = LoggerFactory.getLogger(SetImmediateFunction.class);


    private List<Object[]> queue = new ArrayList<>();


    /**
     * Make sure our object is detected as function.
     *
     * @return
     */
    @Override
    public boolean isFunction()
    {
        return true;
    }


    /**
     * setImmediate implementation.
     *
     * @param thiz      this context, not used
     * @param args      var args, the first argument is our function.
     * @return
     */
    @Override
    public Object call(Object thiz, Object... args)
    {
        if (args.length == 0)
        {
            throw new IllegalStateException("No args");
        }

        Object firstArg = args[0];
        if (!(firstArg instanceof ScriptObjectMirror) || !((ScriptObjectMirror) firstArg).isFunction())
        {
            throw new IllegalStateException("First argument is no function: " + firstArg);
        }

        queue.add(args);

        return null;
    }


    public void flush()
    {
        while (queue.size() > 0)
        {
            final Object[] args = queue.remove(0);

            // call function as first argument with the rest of the arguments
            ((ScriptObjectMirror)args[0]).call(null, removeFirst(args));
        }
    }


    private Object[] removeFirst(Object[] args)
    {
        final int elements = args.length - 1;

        Object[] copy = new Object[elements];
        if (elements > 0)
        {
            System.arraycopy(args, 1, copy, 0, elements);
        }
        return copy;
    }
}
