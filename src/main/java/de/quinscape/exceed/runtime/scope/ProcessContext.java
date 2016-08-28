package de.quinscape.exceed.runtime.scope;

import com.google.common.collect.ImmutableSet;
import de.quinscape.exceed.model.context.ContextModel;
import de.quinscape.exceed.model.context.ScopedPropertyModel;
import de.quinscape.exceed.model.domain.DomainProperty;
import de.quinscape.exceed.runtime.RuntimeContext;
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

        contextCopy.put(DOMAIN_OBJECT_CONTEXT, context.get(DOMAIN_OBJECT_CONTEXT));

        return new ProcessContext(contextModel, contextCopy);
    }


    public DomainObject getDomainObjectContext()
    {
        return (DomainObject) getProperty(DOMAIN_OBJECT_CONTEXT);
    }


    public void setDomainObjectContext(DomainObject domainObjectContext)
    {
        setProperty(DOMAIN_OBJECT_CONTEXT, domainObjectContext);
    }


    @Override
    public boolean hasProperty(String name)
    {
        if (name.equals(DOMAIN_OBJECT_CONTEXT))
        {
            return true;
        }

        return super.hasProperty(name);
    }


    @Override
    public ScopedPropertyModel getModel(String name)
    {
        if (name.equals(DOMAIN_OBJECT_CONTEXT))
        {
            return getCurrentDomainObjectModel();
        }

        return super.getModel(name);
    }


    public ScopedPropertyModel getCurrentDomainObjectModel()
    {
        final DomainObject domainObject = (DomainObject) getProperty(DOMAIN_OBJECT_CONTEXT);

        final ScopedPropertyModel scopedPropertyModel = new ScopedPropertyModel();
        scopedPropertyModel.setName(DOMAIN_OBJECT_CONTEXT);
        scopedPropertyModel.setType(DomainProperty.DOMAIN_TYPE_PROPERTY_TYPE);
        scopedPropertyModel.setTypeParam(
            domainObject != null ? domainObject.getDomainType() : "None"
        );
        return scopedPropertyModel;
    }
}
