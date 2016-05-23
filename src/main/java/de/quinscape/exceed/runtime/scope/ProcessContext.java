package de.quinscape.exceed.runtime.scope;

import de.quinscape.exceed.model.context.ContextModel;
import de.quinscape.exceed.runtime.RuntimeContext;
import de.quinscape.exceed.runtime.component.DataList;
import de.quinscape.exceed.runtime.domain.DomainObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Process scoped context implementation.
 */
public final class ProcessContext
    extends AbstractScopedContext
{
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
            contextCopy.put(objectName, ((DomainObject)context.get(objectName)).copy());
        }

        for (String listName : contextModel.getLists().keySet())
        {
            contextCopy.put(listName, ((DataList)context.get(listName)).copy());
        }

        return new ProcessContext(contextModel, contextCopy);
    }
}
