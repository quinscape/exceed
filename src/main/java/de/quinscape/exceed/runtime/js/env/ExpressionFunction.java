package de.quinscape.exceed.runtime.js.env;

import de.quinscape.exceed.expression.ExpressionParser;
import de.quinscape.exceed.expression.ParseException;
import de.quinscape.exceed.runtime.ExceedRuntimeException;
import jdk.nashorn.api.scripting.AbstractJSObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExpressionFunction
    extends AbstractJSObject
{
    private final static Logger log = LoggerFactory.getLogger(ExpressionFunction.class);


    @Override
    public boolean isFunction()
    {
        return true;
    }


    @Override
    public Object call(Object thiz, Object... args)
    {
        String expr = (String) args[0];

        try
        {
            log.debug("ExpressionFunction: {}", expr);

            return ExpressionParser.parse(expr);
        }
        catch (ParseException e)
        {
            throw new ExceedRuntimeException(e);
        }
    }
}
