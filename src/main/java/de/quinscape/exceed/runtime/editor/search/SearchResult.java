package de.quinscape.exceed.runtime.editor.search;

import de.quinscape.exceed.model.TopLevelModel;
import de.quinscape.exceed.model.config.ApplicationConfig;
import de.quinscape.exceed.model.routing.RoutingTable;

public class SearchResult
{
    private final String type;
    private final String name;
    private final SearchResultType resultType;
    private final Object detail;

    public SearchResult(TopLevelModel topLevelModel, SearchResultType resultType)
    {
        this(topLevelModel, resultType, null);
    }
    
    public SearchResult(TopLevelModel topLevelModel, SearchResultType resultType, Object detail)
    {
        this(topLevelModel.getType(), getName(topLevelModel), resultType, detail);
    }

    private static String getName(TopLevelModel topLevelModel)
    {
        if (topLevelModel instanceof RoutingTable || topLevelModel instanceof ApplicationConfig)
        {
            return null;
        }
        return topLevelModel.getName();
    }

    public SearchResult(String type, String name, SearchResultType resultType, Object detail)
    {
        this.type = type;
        this.name = name;
        this.resultType = resultType;
        this.detail = detail;
    }

    public String getType()
    {
        return type;
    }


    public String getName()
    {
        return name;
    }


    public SearchResultType getResultType()
    {
        return resultType;
    }


    public Object getDetail()
    {
        return detail;
    }
}
