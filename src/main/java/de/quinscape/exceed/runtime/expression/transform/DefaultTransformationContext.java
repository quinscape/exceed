package de.quinscape.exceed.runtime.expression.transform;

import de.quinscape.exceed.expression.Node;
import de.quinscape.exceed.model.ApplicationModel;
import de.quinscape.exceed.runtime.js.ExpressionType;
import de.quinscape.exceed.runtime.js.ScriptBuffer;
import de.quinscape.exceed.runtime.model.ExpressionModelContext;
import de.quinscape.exceed.runtime.model.ExpressionRenderer;

import java.util.List;

public class DefaultTransformationContext
    implements ExpressionTransformationContext
{
    private final ExpressionType expressionType;

    private final List<ExpressionTransformer> expressionTransformers;

    private final Renderer renderer;

    private final StringBuilder output;

    private final ExpressionModelContext modelContext;

    private final ScriptBuffer namespace;

    private final ApplicationModel applicationModel;

    private boolean renderDefault;

    public DefaultTransformationContext(
        ApplicationModel applicationModel,
        ExpressionType expressionType,
        ExpressionModelContext modelContext,
        List<ExpressionTransformer> expressionTransformers,
        ScriptBuffer namespace
    )
    {
        this.applicationModel = applicationModel;
        if (expressionType == null)
        {
            throw new IllegalArgumentException("expressionType can't be null");
        }

        if (expressionTransformers == null)
        {
            throw new IllegalArgumentException("expressionTransformers can't be null");
        }

        this.expressionType = expressionType;
        this.expressionTransformers = expressionTransformers;
        this.modelContext = modelContext;
        this.namespace = namespace;

        output = new StringBuilder();
        renderer = new Renderer(output);
    }


    @Override
    public void applyRecursive(Node node)
    {
        boolean oldRenderDefault = renderDefault;

        if (!transform(node))
        {
            node.jjtAccept(renderer, null);
        }

        renderDefault = oldRenderDefault;
    }


    public ExpressionModelContext getModelContext()
    {
        return modelContext;
    }


    @Override
    public String pushCodeBlock(String identifierBase, String code)
    {
        return namespace.pushCodeBlock(identifierBase, code);
    }

    @Override
    public void output(Object s)
    {
        output.append(s);
    }


    @Override
    public String getOutput()
    {
        return output.toString();
    }


    @Override
    public void renderMultiBinary(Node node, String op)
    {
        renderer.renderMultiBinary(node, op, null);
    }


    public boolean isRenderDefault()
    {
        return renderDefault;
    }


    public void renderDefault()
    {
        this.renderDefault = true;
    }


    public void clearRenderDefault()
    {
        this.renderDefault = false;
    }


    /**
     * Specialized renderer for transformations that routes every child rendering back to {@link #applyRecursive(Node)}.
     */
    public class Renderer
        extends ExpressionRenderer
    {
        public Renderer(StringBuilder output)
        {
            super(output);
        }


        public Object renderMultiBinary(Node node, String op, Object data)
        {
            return super.renderMultiBinary(node, op, data);
        }


        @Override
        protected Object visitChild(Node kid, Object data)
        {
            applyRecursive(kid);
            return null;
        }
    }


    private boolean transform(Node node)
    {
        for (ExpressionTransformer transformer : expressionTransformers)
        {
            if (transformer.appliesTo(this,node))
            {
                this.clearRenderDefault();
                transformer.apply(this, node);

                if (!this.isRenderDefault())
                {
                    return true;
                }
            }
        }
        return false;
    }


    public ApplicationModel getApplicationModel()
    {
        return applicationModel;
    }


    public ExpressionType getExpressionType()
    {
        return expressionType;
    }
}
