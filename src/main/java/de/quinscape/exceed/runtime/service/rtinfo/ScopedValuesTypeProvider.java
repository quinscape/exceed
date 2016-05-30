package de.quinscape.exceed.runtime.service.rtinfo;

import de.quinscape.exceed.model.context.ScopedObjectModel;
import de.quinscape.exceed.model.domain.DomainType;
import de.quinscape.exceed.model.process.Process;
import de.quinscape.exceed.model.view.View;
import de.quinscape.exceed.runtime.RuntimeContext;
import de.quinscape.exceed.runtime.component.StaticFunctionReferences;
import de.quinscape.exceed.runtime.domain.DomainObject;
import de.quinscape.exceed.runtime.domain.DomainService;
import de.quinscape.exceed.runtime.scope.ProcessContext;
import de.quinscape.exceed.runtime.scope.ScopedContext;
import de.quinscape.exceed.runtime.scope.ScopedContextChain;
import de.quinscape.exceed.runtime.scope.ScopedValueType;
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
public class ScopedValuesTypeProvider
    extends AbstractScopeRelatedProvider
{

    @Override
    public String getName()
    {
        return "scopeTypes";
    }


    @Override
    public Object provide(HttpServletRequest request, RuntimeContext runtimeContext)
    {
        DomainService domainService = runtimeContext.getRuntimeApplication().getDomainService();
        View view = runtimeContext.getView();
        if (view != null)
        {
            Set<ScopeReference> refs = getReferences(runtimeContext, view);
            ScopedContextChain scopedContextChain = runtimeContext.getScopedContextChain();

            Map<String, DomainType> map = new HashMap<>();
            for (ScopeReference reference : refs)
            {
                if (reference.getType() == ScopedValueType.OBJECT)
                {
                    String name = reference.getName();

                    String typeName;
                    if (name.equals(ProcessContext.DOMAIN_OBJECT_CONTEXT))
                    {
                        DomainObject object = scopedContextChain.getObject(ProcessContext.DOMAIN_OBJECT_CONTEXT);
                        typeName = object != null ? object.getDomainType() : null;
                    }
                    else
                    {
                        ScopedContext scopeWithObject = scopedContextChain.findScopeWithObject(name);
                        ScopedObjectModel scopedObjectModel = scopeWithObject.getContextModel().getObjects().get(name);
                        typeName = scopedObjectModel.getType();
                    }

                    if (typeName != null)
                    {
                        DomainType type = map.get(typeName);
                        if (type == null)
                        {
                            map.put(typeName, domainService.getDomainType(typeName));
                        }
                    }
                }
            }
            return map;
        }
        return null;
    }


}
