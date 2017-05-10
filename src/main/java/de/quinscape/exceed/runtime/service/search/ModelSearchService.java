package de.quinscape.exceed.runtime.service.search;

import de.quinscape.exceed.model.ApplicationModel;
import de.quinscape.exceed.model.TopLevelModel;
import de.quinscape.exceed.model.TopLevelModelVisitor;
import de.quinscape.exceed.runtime.editor.search.SearchResult;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Searches the current application model for matches to a search term.
 */
@Service
public class ModelSearchService
{
    public List<SearchResult> search(ApplicationModel applicationModel, String searchTerm)
    {

        final TopLevelModelSearcher visitor = new TopLevelModelSearcher(searchTerm);



        applicationModel.getConfigModel().accept(visitor, null);
        applicationModel.getRoutingTable().accept(visitor, null);

        searchModelMap(visitor, applicationModel.getProcesses());
        searchModelMap(visitor, applicationModel.getViews());
        searchModelMap(visitor, applicationModel.getDomainTypes());
        searchModelMap(visitor, applicationModel.getEnums());
        searchModelMap(visitor, applicationModel.getLayouts());

        return visitor.getResults();
    }


    private void searchModelMap(TopLevelModelVisitor<?, ?> visitor, Map<String, ? extends TopLevelModel> map)
    {
        map.values().forEach(topLevelModel -> topLevelModel.accept(visitor, null));
    }
}
