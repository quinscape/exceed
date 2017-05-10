package de.quinscape.exceed.runtime.service.search;

import de.quinscape.exceed.model.ApplicationConfig;
import de.quinscape.exceed.model.DomainEditorViews;
import de.quinscape.exceed.model.TopLevelModel;
import de.quinscape.exceed.model.TopLevelModelVisitor;
import de.quinscape.exceed.model.context.ContextModel;
import de.quinscape.exceed.model.context.ScopedPropertyModel;
import de.quinscape.exceed.model.domain.DomainProperty;
import de.quinscape.exceed.model.domain.DomainType;
import de.quinscape.exceed.model.domain.DomainVersion;
import de.quinscape.exceed.model.domain.EnumType;
import de.quinscape.exceed.model.domain.PropertyType;
import de.quinscape.exceed.model.process.DecisionModel;
import de.quinscape.exceed.model.process.DecisionState;
import de.quinscape.exceed.model.process.Process;
import de.quinscape.exceed.model.process.ProcessState;
import de.quinscape.exceed.model.process.Transition;
import de.quinscape.exceed.model.process.ViewState;
import de.quinscape.exceed.model.routing.Mapping;
import de.quinscape.exceed.model.routing.RoutingTable;
import de.quinscape.exceed.model.view.ComponentModel;
import de.quinscape.exceed.model.view.LayoutModel;
import de.quinscape.exceed.model.view.View;
import de.quinscape.exceed.runtime.editor.search.SearchResult;
import de.quinscape.exceed.runtime.editor.search.SearchResultType;

import java.util.List;
import java.util.Map;

/**
 * Visitor that adds to a input list of {@link SearchResult}s from the top level model it visits
 */
class TopLevelModelSearcher
    implements TopLevelModelVisitor<Object, Object>
{
    private final SearchOperation searchOperation;


    public TopLevelModelSearcher(String searchTerm)
    {
        this.searchOperation = new SearchOperation(searchTerm);
    }


    public List<SearchResult> getResults()
    {
        return searchOperation.getResults();
    }


    @Override
    public List<SearchResult> visit(RoutingTable routingTable, Object o)
    {
        for (Map.Entry<String, Mapping> entry : routingTable.getMappings().entrySet())
        {
            final String route = entry.getKey();
            final Mapping mapping = entry.getValue();

            searchOperation.match(route, routingTable, SearchResultType.ROUTE, route);

            searchOperation.match(mapping.getName(), routingTable, SearchResultType.NAME, route);
            searchOperation.match(mapping.getProcessName(), routingTable, SearchResultType.REFERENCE, route);
            searchOperation.match(mapping.getViewName(), routingTable, SearchResultType.REFERENCE, route);
        }
        return null;
    }


    @Override
    public List<SearchResult> visit(Process process, Object o)
    {
        searchOperation.match(process.getStartTransition().getAction(), process, SearchResultType.ACTION, new
            TransitionResultDetail(null, "start"));

        searchContext(process, process.getContextModel(), searchOperation, "process");


        for (Map.Entry<String, ProcessState> entry : process.getStates().entrySet())
        {
            final String name = entry.getKey();
            final ProcessState processState = entry.getValue();

            searchOperation.match(processState.getName(), process, SearchResultType.NAME, name);

            if (processState instanceof ViewState)
            {
                final Map<String, Transition> transitions = ((ViewState) processState).getTransitions();
                if (transitions != null)
                {
                    for (Transition transition : transitions.values())
                    {
                        searchOperation.match(transition.getName(), process, SearchResultType.TRANSITION, new TransitionResultDetail
                            (name, transition.getName()));
                        searchOperation.match(transition.getDescription(), process, SearchResultType.DESCRIPTION, new TransitionResultDetail
                            (name, transition.getName()));
                        searchOperation.match(transition.getAction(), process, SearchResultType.ACTION, new TransitionResultDetail
                            (name, transition.getName()));
                    }
                }
            }
            else if (processState instanceof DecisionState)
            {
                searchOperation.match(((DecisionState) processState).getDefaultTransition().getAction(), process,
                    SearchResultType.ACTION, new TransitionResultDetail(name, null));


                List<DecisionModel> decisions = ((DecisionState) processState).getDecisions();
                for (int i = 0; i < decisions.size(); i++)
                {
                    DecisionModel decisionModel = decisions.get(i);
                    searchOperation.match(
                        decisionModel.getExpression(),
                        process,
                        SearchResultType.EXPRESSION,
                        new TransitionResultDetail(
                            name,
                            String.valueOf(i)
                        )
                    );
                    searchOperation.match(
                        decisionModel.getExpression(),
                        process,
                        SearchResultType.ACTION,
                        new TransitionResultDetail(
                            name,
                            String.valueOf(i)
                        )
                    );
                }
            }
        }

        return null;
    }


    @Override
    public List<SearchResult> visit(ApplicationConfig configModel, Object o)
    {
        searchContext(configModel, configModel.getApplicationContextModel(), searchOperation, "application");
        searchContext(configModel, configModel.getSessionContextModel(), searchOperation, "session");

        return null;
    }


    private void searchContext(TopLevelModel topLevelModel, ContextModel contextModel, Object o, String contextType)

    {
        for (ScopedPropertyModel scopedPropertyModel : contextModel.getProperties().values())
        {
            searchOperation.match(scopedPropertyModel.getName(), topLevelModel, SearchResultType.SCOPE, new ScopeResultDetail(scopedPropertyModel.getName(), contextType));
            searchOperation.match(scopedPropertyModel.getDescription(), topLevelModel, SearchResultType.DESCRIPTION, new ScopeResultDetail(scopedPropertyModel.getName(), contextType));
            searchOperation.match(scopedPropertyModel.getDefaultValue(), topLevelModel, SearchResultType.DEFAULT_VALUE, new ScopeResultDetail(scopedPropertyModel.getName(), contextType));

            if (scopedPropertyModel.getType().equals(DomainProperty.DOMAIN_TYPE_PROPERTY_TYPE))
            {
                searchOperation.match((String) scopedPropertyModel.getTypeParam(), topLevelModel, SearchResultType.REFERENCE, new ScopeResultDetail(scopedPropertyModel.getName(), contextType));
            }
        }
    }


    @Override
    public List<SearchResult> visit(View view, Object o)
    {
        searchContext(view, view.getContextModel(), searchOperation, "view");

        for (ComponentModel contentModel : view.getContent().values())
        {
            contentModel.walk(new ComponentSearcher(searchOperation, view));
        }

        return null;
    }


    @Override
    public List<SearchResult> visit(DomainType domainType, Object o)
    {
        for (DomainProperty domainProperty : domainType.getProperties())
        {
            searchOperation.match(domainProperty.getName(), domainType, SearchResultType.PROPERTY, domainProperty.getName());
            searchOperation.match(domainProperty.getDefaultValue(), domainType, SearchResultType.DEFAULT_VALUE, domainProperty.getName());
        }
        return null;
    }


    @Override
    public List<SearchResult> visit(EnumType enumType, Object o)
    {
        for (String name : enumType.getValues())
        {
            searchOperation.match(name, enumType, SearchResultType.PROPERTY, name);
        }
        return null;
    }


    @Override
    public List<SearchResult> visit(LayoutModel layoutModel, Object o)
    {
        layoutModel.getRoot().walk(new ComponentSearcher(searchOperation, layoutModel));

        return null;
    }


    @Override
    public TopLevelModel visit(DomainEditorViews domainEditorViews, Object o)
    {
        return null;
    }


    @Override
    public List<SearchResult> visit(DomainVersion domainVersion, Object o)
    {
        return null;
    }


    @Override
    public List<SearchResult> visit(PropertyType propertyType, Object o)
    {
        return null;
    }


}
