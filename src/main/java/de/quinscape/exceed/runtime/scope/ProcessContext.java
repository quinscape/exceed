package de.quinscape.exceed.runtime.scope;

import com.google.common.collect.ImmutableSet;
import de.quinscape.exceed.model.context.ContextModel;
import de.quinscape.exceed.runtime.RuntimeContext;
import de.quinscape.exceed.runtime.component.DataList;
import de.quinscape.exceed.runtime.domain.DomainObject;
import de.quinscape.exceed.runtime.util.DomainUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Process scoped context implementation.
 */
public final class ProcessContext
    extends AbstractScopedContext
{

    public final static String DOMAIN_OBJECT_CONTEXT = "current";

    public final static Set<String> RESERVED_NAMES = ImmutableSet.of(ProcessContext.DOMAIN_OBJECT_CONTEXT);

    public ProcessContext(ContextModel contextModel)
    {
        super(contextModel);
    }

    protected ProcessContext(ContextModel contextModel, Map<String,Object> context)
    {
        super(contextModel, context);
    }

    @Override
    public ScopedContext copy(RuntimeContext runtimeContext)
    {

        Map<String, Object> contextCopy = new HashMap<>(context.size());
        ContextModel contextModel = getContextModel();

        for (String propertyName : contextModel.getProperties().keySet())
        {
            contextCopy.put(propertyName, context.get(propertyName));
        }

        for (String objectName : contextModel.getObjects().keySet())
        {
            contextCopy.put(objectName, DomainUtil.copy(runtimeContext, (DomainObject) context.get(objectName)));
        }

        for (String listName : contextModel.getLists().keySet())
        {
            contextCopy.put(listName, ((DataList)context.get(listName)).copy(runtimeContext));
        }

        contextCopy.put(DOMAIN_OBJECT_CONTEXT, context.get(DOMAIN_OBJECT_CONTEXT));

        return new ProcessContext(contextModel, contextCopy);
    }


    @Override
    public boolean hasObject(String name)
    {
        return DOMAIN_OBJECT_CONTEXT.equals(name) || super.hasObject(name);
    }

    public DomainObject getDomainObjectContext()
    {
        return getObject(DOMAIN_OBJECT_CONTEXT);
    }


    public void setDomainObjectContext(DomainObject domainObjectContext)
    {
        setObject(DOMAIN_OBJECT_CONTEXT, domainObjectContext);
    }
}
