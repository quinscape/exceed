package de.quinscape.exceed.runtime.js.env;

import de.quinscape.exceed.runtime.RuntimeContext;
import de.quinscape.exceed.runtime.RuntimeContextHolder;
import jdk.nashorn.api.scripting.AbstractJSObject;

/**
 * Provides translation table lookup.
 * <p>
 *     Used by the i18n.js service internally to look up translation template in server side js operation.
 * </p>
 *
 */
public class TranslationFunction
    extends AbstractJSObject
{
    @Override
    public boolean isFunction()
    {
        return true;
    }


    /**
     * Returns the translation template for the first arg string
     */
    @Override
    public Object call(Object thiz, Object... args)
    {
        final RuntimeContext runtimeContext = RuntimeContextHolder.get();
        final Object value = args[0];
        if (value == null)
        {
            return "";
        }
        return runtimeContext.getTranslator().translate(runtimeContext, value.toString());
    }
}
