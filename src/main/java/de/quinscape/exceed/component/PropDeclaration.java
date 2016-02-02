package de.quinscape.exceed.component;

import org.svenson.JSONParameter;
import org.svenson.JSONProperty;

public class PropDeclaration
{
    private final String ruleExpression;
    private final boolean client;
    private final String context;

    public PropDeclaration(
        @JSONParameter("rule")
        String ruleExpression,
        @JSONParameter("client")
        Boolean client,
        @JSONParameter("context")
        Object context)
    {
        this.ruleExpression = ruleExpression;

        if (context instanceof Boolean)
        {
            context = ((Boolean)context) ? "context" : null;
        }
        else if (context != null && !(context instanceof String))
        {
            throw new IllegalArgumentException("context must be boolean or expression string.");
        }
        this.context = (String) context;
        this.client = client != null ? client : true;
    }

    public boolean isClient()
    {
        return client;
    }


    public String getRuleExpression()
    {
        return ruleExpression;
    }


    @JSONProperty(ignoreIfNull = true)
    public String getContext()
    {
        return context;
    }
}


