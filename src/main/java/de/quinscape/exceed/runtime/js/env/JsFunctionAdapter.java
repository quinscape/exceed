package de.quinscape.exceed.runtime.js.env;

import jdk.nashorn.api.scripting.AbstractJSObject;

import java.util.function.Consumer;

/**
 * Wraps an consumer
 * 
 */
public final class JsFunctionAdapter<T>
    extends AbstractJSObject
{
    private final Consumer<T> consumer;


    /**
     * Creates a new JsFunctionAdapter.
     *
     * @param consumer  consumer to call when the js function is called.
     */
    JsFunctionAdapter(Consumer<T> consumer)
    {
        this.consumer = consumer;
    }

    @Override
    public boolean isFunction()
    {
        return true;
    }


    @Override
    public Object call(Object thiz, Object... args)
    {
        final T result = args.length > 0 ? (T) args[0] : null;
        consumer.accept(result);
        return null;
    }
}
