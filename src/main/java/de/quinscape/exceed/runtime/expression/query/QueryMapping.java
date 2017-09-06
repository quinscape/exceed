package de.quinscape.exceed.runtime.expression.query;

import java.util.List;

public final class QueryMapping
{
    private final String domainType;

    private final String embeddingField;

    private final List<Integer> indexes;

    private final String queryName;


    public QueryMapping(String domainType, String embeddingField, List<Integer> indexes, String queryName)
    {
        this.queryName = queryName;
        if (domainType == null)
        {
            throw new IllegalArgumentException("domainType can't be null");
        }

        if (indexes == null)
        {
            throw new IllegalArgumentException("indexes can't be null");
        }

        if (embeddingField == null)
        {
            throw new IllegalArgumentException("embeddingField can't be null");
        }

        this.domainType = domainType;
        this.embeddingField = embeddingField;
        this.indexes = indexes;
    }


    public String getDomainType()
    {
        return domainType;
    }


    public String getEmbeddingField()
    {
        return embeddingField;
    }


    public List<Integer> getIndexes()
    {
        return indexes;
    }


    public String getQueryName()
    {
        return queryName;
    }
}
