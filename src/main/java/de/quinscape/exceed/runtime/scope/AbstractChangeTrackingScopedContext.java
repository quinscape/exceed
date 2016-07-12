package de.quinscape.exceed.runtime.scope;

import de.quinscape.exceed.model.context.ContextModel;
import de.quinscape.exceed.runtime.component.DataGraph;
import de.quinscape.exceed.runtime.domain.DomainObject;

import java.util.Map;

/**
 * A scoped context that tracks if changes were made since the creation for synchronization purposes.
 *
 */
public abstract class AbstractChangeTrackingScopedContext
    extends AbstractScopedContext
{
    private boolean dirty;


    public AbstractChangeTrackingScopedContext(ContextModel contextModel)
    {
        super(contextModel);
    }


    protected AbstractChangeTrackingScopedContext(ContextModel contextModel, Map<String, Object> context)
    {
        super(contextModel, context);
    }


    public boolean isDirty()
    {
        return dirty;
    }

    protected void markDirty()
    {
        dirty = true;
    }


    public void markClean()
    {
        dirty = false;
    }

    @Override
    public void setProperty(String name, Object value)
    {
        markDirty();
        super.setProperty(name, value);
    }


    @Override
    public void setObject(String name, DomainObject value)
    {
        markDirty();
        super.setObject(name, value);
    }


    @Override
    public void setList(String name, DataGraph list)
    {
        markDirty();
        super.setList(name, list);
    }

}
