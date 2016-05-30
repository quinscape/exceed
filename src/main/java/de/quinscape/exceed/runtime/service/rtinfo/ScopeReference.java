package de.quinscape.exceed.runtime.service.rtinfo;

import com.google.common.base.Objects;
import de.quinscape.exceed.model.context.ScopedElementModel;
import de.quinscape.exceed.runtime.scope.ProcessContext;
import de.quinscape.exceed.runtime.scope.ScopedValueType;
import de.quinscape.exceed.runtime.util.Util;

public final class ScopeReference
{
    private final ScopedValueType type;

    private final String name;

    private final ScopedElementModel model;


    public ScopeReference(ScopedValueType type, String name, ScopedElementModel model)
    {
        if (type == null)
        {
            throw new IllegalArgumentException("type can't be null");
        }
        if (model == null && !ProcessContext.RESERVED_NAMES.contains(name))
        {
            throw new IllegalArgumentException("model can't be null for non-reserved name '" + name + "'");
        }

        this.type = type;
        this.name = name;
        this.model = model;
    }


    public ScopedValueType getType()
    {
        return type;
    }


    public String getName()
    {
        return model != null ? model.getName() : ProcessContext.DOMAIN_OBJECT_CONTEXT;
    }


    public ScopedElementModel getModel()
    {
        return model;
    }


    @Override
    public boolean equals(Object obj)
    {
        if (obj instanceof ScopeReference)
        {
            ScopeReference that = (ScopeReference) obj;
            return this.type == that.type &&
                this.name.equals(that.name) &&
                Objects.equal(this.model, that.model);

        }
        return false;
    }


    @Override
    public int hashCode()
    {
        return Util.hashcodeOver(type, name, model);
    }
}
