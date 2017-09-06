package de.quinscape.exceed.runtime.js;

import de.quinscape.exceed.expression.ASTExpression;
import de.quinscape.exceed.model.ApplicationModel;
import de.quinscape.exceed.runtime.expression.transform.DefaultTransformationContext;
import de.quinscape.exceed.runtime.expression.transform.ExpressionTransformationContext;
import de.quinscape.exceed.runtime.expression.transform.ExpressionTransformer;
import de.quinscape.exceed.runtime.model.ExpressionModelContext;
import de.quinscape.exceed.runtime.util.Util;

import java.util.ArrayList;
import java.util.List;

public class JsExpressionRenderer
{
    private final List<ExpressionTransformer> expressionTransformers;

    public JsExpressionRenderer(List<ExpressionTransformer> expressionTransformers)
    {
        this.expressionTransformers = expressionTransformers;
    }

    public String transform(
        ApplicationModel applicationModel, ExpressionType type,
        ExpressionModelContext expressionModelContext, ASTExpression expression,
        ScriptBuffer namespace
    )
    {
        return transform(applicationModel, type, expressionModelContext, expression, null, namespace);
    }

    public String transform(
        ApplicationModel applicationModel, ExpressionType type,
        ExpressionModelContext expressionModelContext,
        ASTExpression expression,
        List<ExpressionTransformer> additionalTransformers,
        ScriptBuffer namespace
    )
    {
        additionalTransformers = Util.listOrEmpty(additionalTransformers);

        List<ExpressionTransformer> transformers = new ArrayList<>(additionalTransformers.size() + this.expressionTransformers.size());

        transformers.addAll(additionalTransformers);
        transformers.addAll(this.expressionTransformers);

        ExpressionTransformationContext ctx = new DefaultTransformationContext(applicationModel, type, expressionModelContext, transformers, namespace);
        ctx.applyRecursive(expression);
        return ctx.getOutput();
    }
}
