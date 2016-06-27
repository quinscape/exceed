package de.quinscape.exceed.runtime.service.rtinfo;

import de.quinscape.exceed.expression.ASTFunction;
import de.quinscape.exceed.expression.ASTString;
import de.quinscape.exceed.expression.ExpressionParserDefaultVisitor;
import de.quinscape.exceed.expression.Node;
import de.quinscape.exceed.model.view.ComponentModel;
import de.quinscape.exceed.runtime.RuntimeContext;
import de.quinscape.exceed.runtime.scope.ScopedContext;
import de.quinscape.exceed.runtime.scope.ScopedValueType;

import java.util.HashSet;
import java.util.Set;

/**
 * Expression visitor that collects scoped value references
 */
public class ScopeReferenceCollector
    extends ExpressionParserDefaultVisitor
{
    private final Set<ScopeReference> references;

    private final RuntimeContext runtimeContext;

    private final Class<? extends ScopedContext> filterByType;

    private final ComponentModel componentModel;


    public ScopeReferenceCollector(RuntimeContext runtimeContext, ComponentModel componentModel)
    {
        this(runtimeContext, null, componentModel);
    }


    public ScopeReferenceCollector(RuntimeContext runtimeContext, Class<? extends ScopedContext> filterByType, ComponentModel componentModel)
    {
        this.runtimeContext = runtimeContext;
        this.filterByType = filterByType;
        this.componentModel = componentModel;
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
            ScopedContext scope = type.findScope(runtimeContext.getScopedContextChain(), name);

            if (scope == null)
            {
                throw new IllegalStateException("Scope definition not found for '" + name + "' ( component = " + componentModel.toString() + " )");
            }

            if (filterByType == null || scope.getClass().equals(filterByType))
            {
                references.add(new ScopeReference(type, name, scope.getClass(), scope.getModel(type, name)));
            }
        }
        return null;
    }


    public Set<ScopeReference> getReferences()
    {
        return references;
    }
}
