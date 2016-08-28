package de.quinscape.exceed.runtime.service.rtinfo;

import de.quinscape.exceed.expression.ParseException;
import de.quinscape.exceed.model.context.ScopedPropertyModel;
import de.quinscape.exceed.model.domain.DomainProperty;
import de.quinscape.exceed.model.view.View;
import de.quinscape.exceed.runtime.RuntimeContext;
import de.quinscape.exceed.runtime.component.DataGraph;
import de.quinscape.exceed.model.component.StaticFunctionReferences;
import de.quinscape.exceed.runtime.scope.ScopedContextChain;
import de.quinscape.exceed.runtime.service.RuntimeInfoProvider;
import de.quinscape.exceed.runtime.view.ViewData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Provides a data list for the current scope definitions.
 *
 * @see StaticFunctionReferences
 */
@Service
public class ScopeDataProvider
    implements RuntimeInfoProvider
{

    @Autowired
    private ScopeReferenceService scopeReferenceService;

    @Override
    public String getName()
    {
        return "scope";
    }


    @Override
    public Object provide(HttpServletRequest request, RuntimeContext runtimeContext, ViewData viewData) throws ParseException
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
                return false;
            }

            for (ScopeReference reference : refs)
            {
                String name = reference.getName();

                final ScopedPropertyModel model = scopedContextChain.getModel(name);
                columns.put(name, new DomainProperty(name, model.getType(), null, model.isRequired(), model.getTypeParam(), model.getMaxLength(), null));

                map.put(name, scopedContextChain.getProperty(name));
            }
            return new DataGraph(columns, map, 1);

        }
        return null;
    }
}
