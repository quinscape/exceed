package de.quinscape.exceed.runtime.service.rtinfo;

import de.quinscape.exceed.expression.ParseException;
import de.quinscape.exceed.model.context.ScopedPropertyModel;
import de.quinscape.exceed.model.view.View;
import de.quinscape.exceed.runtime.RuntimeContext;
import de.quinscape.exceed.runtime.component.StaticFunctionReferences;
import de.quinscape.exceed.runtime.domain.DomainObject;
import de.quinscape.exceed.runtime.domain.property.PropertyConverter;
import de.quinscape.exceed.runtime.scope.ScopedContextChain;
import de.quinscape.exceed.runtime.scope.ScopedValueType;
import de.quinscape.exceed.runtime.util.DomainUtil;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Provides the current values of the scoped context objects referenced by view expressions and static calls
 * within component JavaScript modules.
 *
 * @see StaticFunctionReferences
 */
@Service
public class ScopedValuesProvider
    extends AbstractScopeRelatedProvider
{


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
            Set<ScopeReference> refs = getReferences(runtimeContext, view);
            ScopedContextChain scopedContextChain = runtimeContext.getScopedContextChain();

            Map<String, Object> map = new HashMap<>();
            for (ScopeReference reference : refs)
            {
                String name = reference.getName();
                ScopedValueType type = reference.getType();
                Object value = type.get(scopedContextChain, name);

                if (type == ScopedValueType.OBJECT)
                {
                    DomainObject domainObject = (DomainObject) value;

                    DomainObject copy = domainObject.getDomainService().create(domainObject.getDomainType(),
                        domainObject.getId());

                    DomainUtil.copyProperties(runtimeContext, domainObject, copy, true);
                    value = copy;

                }
                else if (type == ScopedValueType.PROPERTY)
                {
                    ScopedPropertyModel scopedPropertyModel = (ScopedPropertyModel) reference.getModel();


                    PropertyConverter converter = runtimeContext.getDomainService()
                        .getPropertyConverter(scopedPropertyModel.getType());
                    value = converter.convertToJSON(runtimeContext, value);
                }
                map.put(name, value);
            }
            return map;
        }
        return null;
    }
}
