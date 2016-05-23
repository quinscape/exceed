package de.quinscape.exceed.runtime.action;

import de.quinscape.exceed.expression.ASTFunction;
import de.quinscape.exceed.model.action.ActionModel;
import de.quinscape.exceed.runtime.expression.ExpressionContext;
import de.quinscape.exceed.runtime.model.ExpressionRenderer;
import de.quinscape.exceed.runtime.service.ActionExecutionEnvironment;

/**
 * Implemented by actions wanting to customize client expression rendering.
 */
public interface ServerActionConverter<M extends ActionModel>
{
    M createModel(ExpressionContext<ActionExecutionEnvironment> ctx, ASTFunction node);
}
