package de.quinscape.exceed.runtime.service.rtinfo;

import de.quinscape.exceed.expression.ASTIdentifier;
import de.quinscape.exceed.expression.ExpressionParserDefaultVisitor;
import de.quinscape.exceed.model.view.ComponentModel;
import de.quinscape.exceed.runtime.RuntimeContext;
import de.quinscape.exceed.runtime.scope.ScopedContext;

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
    public Object visit(ASTIdentifier node, Object data)
    {
        final String name = node.getName();
        ScopedContext scope = runtimeContext.getScopedContextChain().findScopeWithProperty(name);

        if (scope != null && (filterByType == null || scope.getClass().equals(filterByType)))
        {
            references.add(new ScopeReference(name, scope.getClass(), scope.getModel(name)));
        }

        return null;
    }


    public Set<ScopeReference> getReferences()
    {
        return references;
    }
}
