package de.quinscape.exceed.runtime.service.rtinfo;

import de.quinscape.exceed.expression.ASTFunction;
import de.quinscape.exceed.expression.ASTMap;
import de.quinscape.exceed.expression.ASTString;
import de.quinscape.exceed.expression.ExpressionParserDefaultVisitor;
import de.quinscape.exceed.expression.Node;
import de.quinscape.exceed.runtime.RuntimeContext;
import de.quinscape.exceed.runtime.scope.ScopedContextChain;
import de.quinscape.exceed.runtime.scope.ViewContext;
import de.quinscape.exceed.runtime.util.ExpressionUtil;

import java.util.HashSet;
import java.util.Set;

/**
 * Expression visitor that collects scoped value references
 */
public class TransitionScopeReferenceCollector
    extends ExpressionParserDefaultVisitor
{
    private final Set<String> references;

    private final RuntimeContext runtimeContext;

    private boolean viewContextOnly = true;

    public TransitionScopeReferenceCollector(RuntimeContext runtimeContext)
    {
        this.runtimeContext = runtimeContext;
        references = new HashSet<>();
    }


    public boolean isViewContextOnly()
    {
        return viewContextOnly;
    }


    @Override
    public Object visit(ASTFunction node, Object data)
    {
        if (node.getName().equals("set"))
        {
            ASTMap map = (ASTMap) node.jjtGetChild(0);

            Node n = ExpressionUtil.getFromMap(map, "name");
            if (n == null)
            {
                throw new IllegalStateException("Set action has no name.");
            }
            if (!(n instanceof ASTString))
            {
                throw new IllegalStateException("Name property is not a string literal");
            }

            String name = ((ASTString) n).getValue();

            final ScopedContextChain scopedContextChain = runtimeContext.getScopedContextChain();



            if (scopedContextChain.findScopeWithProperty(name) instanceof ViewContext)
            {
                references.add(name);
            }
            else
            {
                viewContextOnly = false;
            }
        }
        else
        {
            // any action but view-context set means we have to do the transition
            viewContextOnly = false;
        }
        return null;
    }


    public Set<String> getReferences()
    {
        return references;
    }
}
