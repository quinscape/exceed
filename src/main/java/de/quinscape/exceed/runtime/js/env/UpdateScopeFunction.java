package de.quinscape.exceed.runtime.js.env;

import de.quinscape.exceed.expression.ParseException;
import de.quinscape.exceed.model.ApplicationModel;
import de.quinscape.exceed.model.domain.property.PropertyModel;
import de.quinscape.exceed.model.meta.PropertyType;
import de.quinscape.exceed.runtime.ExceedRuntimeException;
import de.quinscape.exceed.runtime.RuntimeContext;
import de.quinscape.exceed.runtime.RuntimeContextHolder;
import jdk.nashorn.api.scripting.JSObject;
import jdk.nashorn.api.scripting.NashornScriptEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.ScriptContext;

public class UpdateScopeFunction
    extends AbstractConvertingFunction
{
    private final static Logger log = LoggerFactory.getLogger(UpdateScopeFunction.class);


    public UpdateScopeFunction(NashornScriptEngine nashorn, ScriptContext scriptContext, ApplicationModel
        applicationModel)
    {
        super(nashorn, scriptContext, applicationModel);
    }


    @Override
    public boolean isFunction()
    {
        return true;
    }


    @Override
    public Object call(Object thiz, Object... args)
    {
        try
        {
            final RuntimeContext runtimeContext = RuntimeContextHolder.get();

            JSObject jsObject = (JSObject) args[0];

            final int len = (int) jsObject.getMember("length");

            if (len == 1)
            {
                final String name = (String) jsObject.getSlot(0);
                PropertyModel currentType = runtimeContext.getScopedContextChain().getModel(name);
                final Object value = args[1];

                final PropertyType propertyType = PropertyType.get(
                    runtimeContext,
                    currentType
                );

                final Object converted = propertyType.convertFromJs(runtimeContext, value);

                log.debug("Set {} to {}", name, value);
                runtimeContext.getScopedContextChain().setProperty(name, converted);
            }
            else
            {
                setPath(runtimeContext, jsObject, args[1]);
            }
        }
        catch (ParseException e)
        {
            throw new ExceedRuntimeException(e);
        }
        return null;
    }
}
