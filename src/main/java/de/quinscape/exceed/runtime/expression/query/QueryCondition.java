package de.quinscape.exceed.runtime.expression.query;

import org.jooq.Condition;

import java.util.Set;

public class QueryCondition
{
    private final Condition condition;

    private final Set<DataField> fieldReferences;


    public QueryCondition(
        Condition condition, Set<DataField> fieldReferences
    )
    {
        this.condition = condition;
        this.fieldReferences = fieldReferences;
    }


    public Condition getCondition()
    {
        return condition;
    }


    public Set<DataField> getFieldReferences()
    {
        return fieldReferences;
    }
}
