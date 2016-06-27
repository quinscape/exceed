package de.quinscape.exceed.runtime.expression.query;

import org.jooq.Condition;

public class JoinDefinition
{
    private final String joinType;
    private final QueryDomainType left;
    private final QueryDomainType right;

    private Condition condition;


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


    public void setCondition(Condition condition)
    {
        this.condition = condition;
    }


    public Condition getCondition()
    {
        return condition;
    }
}
