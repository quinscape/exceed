package de.quinscape.exceed.component;

import org.svenson.JSONParameter;

public class PropDeclaration
{
    private final String ruleExpression;
    private final boolean client;

    public PropDeclaration(
        @JSONParameter("rule")
        String ruleExpression,
        @JSONParameter("client")
        Boolean client)
    {
        this.ruleExpression = ruleExpression;
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
}


