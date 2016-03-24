package de.quinscape.exceed.runtime.expression.query;

import de.quinscape.exceed.model.domain.DomainType;
import de.quinscape.exceed.model.view.ComponentModel;
import de.quinscape.exceed.runtime.RuntimeContext;
import de.quinscape.exceed.runtime.domain.DomainService;
import de.quinscape.exceed.runtime.domain.NamingStrategy;
import de.quinscape.exceed.runtime.expression.ExpressionEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class QueryTransformerEnvironment
    extends ExpressionEnvironment
{

    private final static Logger log = LoggerFactory.getLogger(QueryTransformerEnvironment.class);

    private final NamingStrategy namingStrategy;

    private final ComponentModel componentModel;

    private final Map<String, Object> vars;

    private final RuntimeContext runtimeContext;


    public QueryTransformerEnvironment(
        RuntimeContext runtimeContext,
        NamingStrategy namingStrategy,
        ComponentModel componentModel,
        Map<String, Object> vars
    )
    {
        this.runtimeContext = runtimeContext;
        this.namingStrategy = namingStrategy;
        this.componentModel = componentModel;
        this.vars = vars;
    }


    @Override
    public Object resolveIdentifier(String name)
    {

        DomainType domainType = runtimeContext.getRuntimeApplication().getDomainService().getDomainType(name);
        if (domainType == null)
        {
            throw new QueryTransformationException("Unknown domain type '" + name + "'");
        }
        QueryDomainType queryDomainType = new QueryDomainType(domainType);

        log.debug("Resolving {} to {}", name, queryDomainType);

        return queryDomainType;
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
        return false;
    }


    @Override
    protected boolean arithmeticOperatorsAllowed()
    {
        return false;
    }


    public ComponentModel getComponentModel()
    {
        return componentModel;
    }


    public Map<String, Object> getVars()
    {
        return vars;
    }

    public Object getVar(String name)
    {
        if (vars == null)
        {
            return null;
        }

        return vars.get(name);
    }


    public RuntimeContext getRuntimeContext()
    {
        return runtimeContext;
    }


    public NamingStrategy getNamingStrategy()
    {
        return namingStrategy;
    }
}
