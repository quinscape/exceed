package de.quinscape.exceed.runtime.expression.query;

import de.quinscape.exceed.expression.SimpleNode;

public class JoinDefinition
{
    private final String joinType;
    private final QueryDomainType left;
    private final QueryDomainType right;

    private SimpleNode condition;


    public JoinDefinition(String joinType, QueryDomainType left, QueryDomainType right)
    {
        this.joinType = joinType;
        this.left = left;
        this.right = right;
    }


    public String getJoinType()
    {
        return joinType;
    }


    public QueryDomainType getLeft()
    {
        return left;
    }


    public QueryDomainType getRight()
    {
        return right;
    }


    public void setCondition(SimpleNode condition)
    {
        this.condition = condition;
    }


    public SimpleNode getCondition()
    {
        return condition;
    }
}
