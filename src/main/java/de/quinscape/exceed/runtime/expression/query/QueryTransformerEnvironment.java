package de.quinscape.exceed.runtime.expression.query;

import de.quinscape.exceed.model.context.ScopeDeclarations;
import de.quinscape.exceed.model.domain.type.DomainType;
import de.quinscape.exceed.model.view.ComponentModel;
import de.quinscape.exceed.model.view.View;
import de.quinscape.exceed.runtime.RuntimeContext;
import de.quinscape.exceed.runtime.action.ActionRegistration;
import de.quinscape.exceed.runtime.action.ActionResult;
import de.quinscape.exceed.runtime.expression.ExpressionEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class QueryTransformerEnvironment
    extends ExpressionEnvironment
{

    private final static Logger log = LoggerFactory.getLogger(QueryTransformerEnvironment.class);


    private final RuntimeContext runtimeContext;

    private final QueryContext queryContext;

    private final ScopeDeclarations scopeDeclarations;

    public final static Object NO_RESULT = new Object();

    public QueryTransformerEnvironment(
        RuntimeContext runtimeContext,
        QueryContext queryContext
    )
    {
        this.runtimeContext = runtimeContext;
        this.queryContext = queryContext;

        if (runtimeContext != null)
        {
            scopeDeclarations = runtimeContext.getApplicationModel().lookup(
                runtimeContext.getScopedContextChain().getScopeLocation()
            );
        }
        else
        {
            scopeDeclarations = null;
        }

    }


    @Override
    public Object resolveIdentifier(String name)
    {
        Object varResult = resolveContextVariable(name);
        if (varResult != NO_RESULT)
        {
            return varResult;
        }

        DomainType domainType = runtimeContext.getDomainService().getDomainType(name);
        if (domainType == null)
        {
            throw new QueryTransformationException("Unknown domain type '" + name + "'");
        }
        QueryDomainType queryDomainType = new QueryDomainType(domainType);

        log.debug("Resolving {} to {}", name, queryDomainType);

        return queryDomainType;
    }


    public Object resolveContextVariable(String name)
    {
        if (scopeDeclarations != null && scopeDeclarations.hasDefinition(name))
        {
            return runtimeContext.getScopedContextChain().getProperty(name);
        }
        return NO_RESULT;
    }


    @Override
    protected boolean logicalOperatorsAllowed()
    {
        return true;
    }


    @Override
    protected boolean comparatorsAllowed()
    {
        return true;
    }


    @Override
    protected boolean complexLiteralsAllowed()
    {
        return true;
    }


    @Override
    protected boolean arithmeticOperatorsAllowed()
    {
        return false;
    }


    @Override
    protected boolean expressionSequenceAllowed()
    {
        return false;
    }


    public ComponentModel getComponentModel()
    {
        return queryContext.getComponentModel();
    }


    public Map<String, Object> getVars()
    {
        return queryContext.getVars();
    }

    public Object getVar(String name)
    {
        return queryContext.getVars().get(name);
    }


    public RuntimeContext getRuntimeContext()
    {
        return runtimeContext;
    }


    public View getViewModel()
    {

        return queryContext.getViewModel();
    }

    public Object executeQuery(QueryDefinition queryDefinition)
    {
        return queryContext.getExecutorFunction().apply(queryDefinition);
    }


    public boolean hasAction(String o)
    {
        final ActionRegistration actionRegistration = queryContext.getActionService().getRegistrations().get(o);
        return actionRegistration != null;
    }

    public ActionResult execute(
        String action,
        QueryTransformerActionParameters actionParameters
    )
    {
        final ActionRegistration actionRegistration = queryContext.getActionService().getRegistrations().get(action);
        return actionRegistration.execute(runtimeContext, actionParameters);
    }
}
