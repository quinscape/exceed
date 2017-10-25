package de.quinscape.exceed.runtime.js;

import de.quinscape.exceed.expression.ASTExpression;
import de.quinscape.exceed.runtime.RuntimeContext;
import jdk.nashorn.api.scripting.NashornScriptEngine;

public class DefaultExpressionService
    implements JsExpressionService
{
    private final NashornScriptEngine nashorn;

    public DefaultExpressionService(NashornScriptEngine nashorn)
    {
        this.nashorn = nashorn;
    }


    @Override
    public Object evaluate(RuntimeContext runtimeContext, ASTExpression expression)
    {
        return null;
    }
}
