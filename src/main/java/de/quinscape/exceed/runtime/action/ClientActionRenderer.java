package de.quinscape.exceed.runtime.action;

import de.quinscape.exceed.expression.ASTFunction;
import de.quinscape.exceed.model.view.View;
import de.quinscape.exceed.runtime.model.ExpressionRenderer;

/**
 * Implemented by actions wanting to customize client expression rendering.
 */
public interface ClientActionRenderer
{
    void renderJsCode(View view, ExpressionRenderer renderer, ASTFunction node);
}
