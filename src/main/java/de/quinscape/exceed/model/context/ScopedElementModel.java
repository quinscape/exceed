package de.quinscape.exceed.model.context;

import de.quinscape.exceed.runtime.RuntimeContext;
import de.quinscape.exceed.runtime.scope.ScopedContext;
import de.quinscape.exceed.runtime.scope.ScopedContextChain;
import de.quinscape.exceed.runtime.scope.ScopedValueType;

public abstract class ScopedElementModel
{
    private String name;

    private String description;


    public String getName()
    {
        return name;
    }


    public void setName(String name)
    {
        this.name = name;
    }


    public String getDescription()
    {
        return description;
    }


    public void setDescription(String description)
    {
        this.description = description;
    }


    public static ScopedElementModel find(RuntimeContext runtimeContext, String name, ScopedValueType type)
    {
        ScopedContextChain scopedContextChain = runtimeContext.getScopedContextChain();

        ScopedContext context;
        switch (type)
        {

            case PROPERTY:
                context = scopedContextChain.findScopeWithProperty(name);
                return context != null ? context.getContextModel().getProperties().get(name) : null;
            case OBJECT:
                context = scopedContextChain.findScopeWithObject(name);
                return context != null ? context.getContextModel().getObjects().get(name) : null;
            case LIST:
                context = scopedContextChain.findScopeWithList(name);
                return context != null ? context.getContextModel().getLists().get(name) : null;
            default:
                throw new IllegalStateException("Unhandled type: " + type);
        }

    }
}

