package de.quinscape.exceed.runtime.service.search;

import de.quinscape.exceed.model.TopLevelModel;
import de.quinscape.exceed.model.component.ComponentDescriptor;
import de.quinscape.exceed.model.component.PropDeclaration;
import de.quinscape.exceed.model.component.PropType;
import de.quinscape.exceed.model.expression.Attributes;
import de.quinscape.exceed.model.view.ComponentModel;
import de.quinscape.exceed.runtime.editor.search.SearchResultType;
import de.quinscape.exceed.runtime.service.ComponentRegistration;

import java.util.function.Consumer;

/**
 * Consumes all components of a walk to search them for matches against the given search term.
 */
class ComponentSearcher
    implements Consumer<ComponentModel>
{
    private final TopLevelModel topLevelModel;
    private final SearchOperation searchOperation;


    ComponentSearcher(SearchOperation searchOperation, TopLevelModel
        topLevelModel)
    {
        this.searchOperation = searchOperation;
        this.topLevelModel = topLevelModel;
    }


    @Override
    public void accept(ComponentModel componentModel)
    {
        searchOperation.match(componentModel.getName(), topLevelModel, SearchResultType.COMPONENT);

        final ComponentRegistration componentRegistration = componentModel.getComponentRegistration();
        final ComponentDescriptor descriptor = componentRegistration != null ? componentRegistration
            .getDescriptor() : null;

        final Attributes attrs = componentModel.getAttrs();
        if (attrs != null)
        {
            for (String name : attrs.getNames())
            {
                final String attrValue = attrs.getAttribute(name).getValue();

                SearchResultType resultType = SearchResultType.ATTRIBUTE;

                PropDeclaration propDecl;

                if (name.equals(ComponentModel.ID_ATTRIBUTE))
                {
                    // id attribute has its own type
                    resultType = SearchResultType.COMPONENT_ID;
                }
                else if(descriptor != null &&
                    (propDecl = descriptor.getPropTypes().get(name)) != null)
                {
                    // register transition attribute matches as REFERENCE
                    if (propDecl.getType() == PropType.TRANSITION)
                    {
                        resultType = SearchResultType.REFERENCE;
                    }
                    // register action attribute matches as ACTION
                    else if (propDecl.getType() == PropType.ACTION_EXPRESSION)
                    {
                        resultType = SearchResultType.ACTION;
                    }
                    // register query part matches as QUERY
                    else if (propDecl.getType() == PropType.QUERY_EXPRESSION)
                    {
                        resultType = SearchResultType.QUERY;
                    }
                }

                // XXX: transmit identification of the matched component?
                searchOperation.match(
                    attrValue,
                    topLevelModel,
                    resultType
                );
            }
        }
    }
}
