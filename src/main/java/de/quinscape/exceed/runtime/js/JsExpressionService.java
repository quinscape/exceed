package de.quinscape.exceed.runtime.js;

import de.quinscape.exceed.expression.ASTExpression;
import de.quinscape.exceed.runtime.RuntimeContext;

public interface JsExpressionService
{
    Object evaluate(RuntimeContext runtimeContext, ASTExpression expression);
}
