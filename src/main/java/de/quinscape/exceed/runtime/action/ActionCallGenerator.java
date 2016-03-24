package de.quinscape.exceed.runtime.action;

import de.quinscape.exceed.expression.ASTFunction;
import de.quinscape.exceed.model.action.ActionModel;
import de.quinscape.exceed.runtime.model.ExpressionRenderer;

public interface ActionCallGenerator
{
    void renderJsCode(ExpressionRenderer renderer, ASTFunction node);
}