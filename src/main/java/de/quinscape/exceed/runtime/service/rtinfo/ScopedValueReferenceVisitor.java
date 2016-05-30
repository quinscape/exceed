package de.quinscape.exceed.runtime.service.rtinfo;

import de.quinscape.exceed.expression.ASTFunction;
import de.quinscape.exceed.expression.ASTString;
import de.quinscape.exceed.expression.ExpressionParserDefaultVisitor;
import de.quinscape.exceed.expression.Node;
import de.quinscape.exceed.model.context.ScopedElementModel;
import de.quinscape.exceed.runtime.RuntimeContext;
import de.quinscape.exceed.runtime.scope.ScopedValueType;

import java.util.HashSet;
import java.util.Set;

/**
 * Expression visitor that collects scoped value references
 */
public class ScopedValueReferenceVisitor
    extends ExpressionParserDefaultVisitor
{
    private final Set<ScopeReference> references;

    private final RuntimeContext runtimeContext;


    public ScopedValueReferenceVisitor(RuntimeContext runtimeContext)
    {
        this.runtimeContext = runtimeContext;
        references = new HashSet<>();
    }


    @Override
    public Object visit(ASTFunction node, Object data)
    {
        String fnName = node.getName();

        ScopedValueType type = null;

        switch (fnName)
        {
            case "object":
                type = ScopedValueType.OBJECT;
                break;
            case "list":
                type = ScopedValueType.LIST;
                break;
            case "property":
                type = ScopedValueType.PROPERTY;
                break;
        }

        if (type != null)
        {
            Node n = node.jjtGetChild(0);
            if (!(n instanceof ASTString))
            {
                throw new IllegalStateException("First argument for function" + fnName + " is not a string literal");
            }

            String name = ((ASTString) n).getValue();
            references.add(new ScopeReference(type, name, ScopedElementModel.find(runtimeContext, name, type)));
        }
        return null;
    }


    public Set<ScopeReference> getReferences()
    {
        return references;
    }
}
