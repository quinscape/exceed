package de.quinscape.exceed.runtime.service.client.provider;

import de.quinscape.exceed.expression.ParseException;
import de.quinscape.exceed.model.context.ScopePropertyData;
import de.quinscape.exceed.model.context.ScopedPropertyModel;
import de.quinscape.exceed.model.domain.DomainProperty;
import de.quinscape.exceed.model.meta.StaticFunctionReferences;
import de.quinscape.exceed.model.view.View;
import de.quinscape.exceed.runtime.RuntimeContext;
import de.quinscape.exceed.runtime.component.DataGraph;
import de.quinscape.exceed.runtime.component.DataGraphQualifier;
import de.quinscape.exceed.runtime.scope.ScopedContextChain;
import de.quinscape.exceed.runtime.service.client.ClientData;
import de.quinscape.exceed.runtime.service.client.ClientStateProvider;
import de.quinscape.exceed.runtime.service.client.ClientStateScope;
import de.quinscape.exceed.runtime.service.client.DefaultClientData;
import de.quinscape.exceed.runtime.service.client.ExceedAppProvider;
import de.quinscape.exceed.runtime.service.client.JSONData;
import de.quinscape.exceed.runtime.service.client.scope.ScopeReference;
import de.quinscape.exceed.runtime.service.client.scope.ScopeReferenceService;
import de.quinscape.exceed.runtime.view.ViewData;
import org.springframework.beans.factory.annotation.Autowired;
import org.svenson.util.JSONBuilder;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Provides a data list for the current scope definitions.
 *
 * @see StaticFunctionReferences
 */
@ExceedAppProvider
public class ScopeDataProvider
    implements ClientStateProvider
{
    private final ScopeReferenceService scopeReferenceService;


    @Autowired
    public ScopeDataProvider(ScopeReferenceService scopeReferenceService)
    {
        this.scopeReferenceService = scopeReferenceService;
    }


    @Override
    public String getName()
    {
        return "scope";
    }

    @Override
    public ClientStateScope getScope()
    {
        return ClientStateScope.REQUEST;
    }


    @Override
    public ClientData provide(HttpServletRequest request, RuntimeContext runtimeContext, ViewData viewData) throws ParseException
    {
        View view = runtimeContext.getView();
        if (view != null)
        {
            Set<ScopeReference> refs = scopeReferenceService.getClientReferences(runtimeContext, view);
            ScopedContextChain scopedContextChain = runtimeContext.getScopedContextChain();


            Map<String, DomainProperty> columns = new HashMap<>();
            Map<String, Object> map = new HashMap<>();

            if (refs.size() == 0)
            {
                return new DefaultClientData(false);
            }

            for (ScopeReference reference : refs)
            {
                String name = reference.getName();

                final ScopedPropertyModel model = scopedContextChain.getModel(name);
                final DomainProperty property = DomainProperty.builder().withName(name).withType(model.getType())
                    .withTypeParam(model.getTypeParam()).withMaxLength(model.getMaxLength()).build();

                property.setData(new ScopePropertyData(reference.getScopeType()));
                columns.put(name, property);

                map.put(name, scopedContextChain.getProperty(name));
            }

            final DataGraph dataGraph = new DataGraph(
                columns,
                map,
                1,
                DataGraphQualifier.SCOPE
            );
            
            final String json = runtimeContext.getDomainService().toJSON(dataGraph);
            return  new JSONData(
                JSONBuilder.buildObject()
                    .includeProperty("graph", json)
                    .includeProperty("dirty", "{}")
                    .output()
            );

        }
        return null;
    }


    @Override
    public boolean isMutable()
    {
        return true;
    }
}
