package de.quinscape.exceed.runtime.expression.query;

import org.jooq.Condition;

public class JoinDefinition
{
    private final String joinType;
    private final QueryDomainType left;
    private final QueryDomainType right;

    private QueryCondition condition;


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


    public QueryCondition getCondition()
    {
        return condition;
    }


    public void setCondition(QueryCondition condition)
    {
        this.condition = condition;
    }
}
