package de.quinscape.exceed.model.context;

public class ScopedListModel
    extends ScopeElementDefinition
{
    private String queryExpression;


    public String getQueryExpression()
    {
        return queryExpression;
    }


    public void setQueryExpression(String queryExpression)
    {
        this.queryExpression = queryExpression;
    }
}