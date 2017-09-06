package de.quinscape.exceed.runtime.scope;

import com.google.common.collect.ImmutableSet;
import de.quinscape.exceed.model.context.ContextModel;
import de.quinscape.exceed.runtime.RuntimeContext;
import de.quinscape.exceed.runtime.component.DataGraph;
import de.quinscape.exceed.runtime.domain.DomainObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Process scoped context implementation.
 */
public final class ProcessContext
    extends AbstractScopedContext
{

    public final static String CURRENT = "current";

    public final static Set<String> RESERVED_NAMES = ImmutableSet.of(ProcessContext.CURRENT);


    public ProcessContext(ContextModel contextModel)
    {
        super(contextModel);
    }


    public ProcessContext(ContextModel contextModel, Map<String, Object> context)
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
        return new ProcessContext(contextModel, contextCopy);
    }


    public DomainObject getCurrentDomainObject()
    {
        return (DomainObject) getProperty(CURRENT);
    }


    public void setCurrentDomainObject(DomainObject domainObject)
    {
        setProperty(CURRENT, domainObject);
    }

}
