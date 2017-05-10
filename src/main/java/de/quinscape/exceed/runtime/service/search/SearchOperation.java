package de.quinscape.exceed.runtime.service.search;

import de.quinscape.exceed.model.TopLevelModel;
import de.quinscape.exceed.runtime.editor.search.SearchResult;
import de.quinscape.exceed.runtime.editor.search.SearchResultType;

import java.util.ArrayList;
import java.util.List;

class SearchOperation
{
    private final List<SearchResult> results;

    private final String searchTerm;


    public SearchOperation(String searchTerm)
    {
        this.searchTerm = searchTerm;
        this.results = new ArrayList<>();
    }


    public List<SearchResult> getResults()
    {
        return results;
    }


    /**
     * Creates a search result by matching the search term.
     *
     * @param searchTerm        search term
     * @param topLevelModel     top level model
     * @param resultType        result type to generate
     */
    void match(String searchTerm, TopLevelModel topLevelModel, SearchResultType
        resultType)
    {
        if (searchTerm.toLowerCase().contains(this.searchTerm))
        {
            results.add(new SearchResult(topLevelModel, resultType));
        }
    }


    /**
     * Creates a search result with detail by matching the search term.
     *
     * @param searchTerm        search term
     * @param topLevelModel     top level model
     * @param resultType        result type to generate
     * @param detail            detail object to use for the result
     */
    void match(String searchTerm, TopLevelModel topLevelModel, SearchResultType
        resultType, Object detail)
    {
        if (searchTerm != null && searchTerm.contains(this.searchTerm))
        {
            results.add(new SearchResult(topLevelModel, resultType, detail));
        }
    }
}
