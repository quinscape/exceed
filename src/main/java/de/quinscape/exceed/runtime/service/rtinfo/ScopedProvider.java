package de.quinscape.exceed.runtime.service.rtinfo;

import de.quinscape.exceed.expression.ParseException;
import de.quinscape.exceed.model.context.ScopedObjectModel;
import de.quinscape.exceed.model.context.ScopedPropertyModel;
import de.quinscape.exceed.model.domain.DomainProperty;
import de.quinscape.exceed.model.domain.DomainType;
import de.quinscape.exceed.model.view.View;
import de.quinscape.exceed.runtime.RuntimeContext;
import de.quinscape.exceed.runtime.component.DataGraph;
import de.quinscape.exceed.runtime.component.StaticFunctionReferences;
import de.quinscape.exceed.runtime.scope.ProcessContext;
import de.quinscape.exceed.runtime.scope.ScopedContext;
import de.quinscape.exceed.runtime.scope.ScopedContextChain;
import de.quinscape.exceed.runtime.scope.ScopedValueType;
import de.quinscape.exceed.runtime.service.RuntimeInfoProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Provides a data list for the current scope definitions.
 *
 * @see StaticFunctionReferences
 */
@Service
public class ScopedProvider
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
    public Object provide(HttpServletRequest request, RuntimeContext runtimeContext) throws ParseException
    {
        View view = runtimeContext.getView();
        if (view != null)
        {
            Set<ScopeReference> refs = scopeReferenceService.getClientReferences(runtimeContext, view);
            ScopedContextChain scopedContextChain = runtimeContext.getScopedContextChain();

            Map<String, DomainProperty> columns = new HashMap<>();
            Map<String, DomainType> types = runtimeContext.getDomainService().getDomainTypes();
            Map<String, Object> map = new HashMap<>();

            if (refs.size() == 0)
            {
                return false;
            }

            for (ScopeReference reference : refs)
            {
                String name = reference.getName();
                ScopedValueType type = reference.getType();

                Object value = type.get(scopedContextChain, name);
                final ScopedContext scope = type.findScope(scopedContextChain, name);
                switch (type)
                {
                    case LIST:
                    {
                        columns.put(name, new DomainProperty(name, DomainProperty.DATA_LIST_PROPERTY_TYPE, null, false));
                        break;
                    }
                    case OBJECT:
                    {
                        final String domainType;
                        if (scope instanceof ProcessContext && name.equals(ProcessContext.DOMAIN_OBJECT_CONTEXT))
                        {
                            domainType = ((ProcessContext) scope).getDomainObjectContext().getDomainType();

                        }
                        else
                        {
                            final ScopedObjectModel model = scope.getContextModel().getObjects().get(name);
                            if (model == null)
                            {
                                throw new IllegalStateException("No scoped object '" + name + "' found");
                            }
                            domainType = model.getType();
                        }

                        columns.put(name, new DomainProperty(name, DomainProperty.DOMAIN_TYPE_PROPERTY_TYPE, null, false, domainType, -1, null));
                        break;
                    }
                    case PROPERTY:
                    {
                        final ScopedPropertyModel model = scope.getContextModel().getProperties().get(name);
                        if (model == null)
                        {
                            throw new IllegalStateException("No scoped property '" + name + "' found");
                        }
                        columns.put(name, new DomainProperty(name, model.getType(), null, model.isRequired(), model.getTypeParam(), model.getMaxLength(), null));
                        break;
                    }
                    default:
                        throw new IllegalStateException("Unhandled type " + type);
                }

                map.put(name, value);
            }
            return new DataGraph(columns, map, 1);

        }
        return null;
    }
}
