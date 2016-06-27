package de.quinscape.exceed.runtime.domain;

import de.quinscape.exceed.runtime.expression.ExpressionEnvironment;

public class PropertyDefaultEnvironment
    extends ExpressionEnvironment
{
    private final DomainService domainService;


    public PropertyDefaultEnvironment(DomainService domainService)
    {
        this.domainService = domainService;
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
        return true;
    }


    @Override
    protected boolean expressionSequenceAllowed()
    {
        return false;
    }


    public DomainService getDomainService()
    {
        return domainService;
    }

}
