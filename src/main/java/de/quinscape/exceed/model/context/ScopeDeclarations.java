package de.quinscape.exceed.model.context;

import de.quinscape.exceed.runtime.scope.ProcessContext;
import de.quinscape.exceed.runtime.scope.ScopeNameCollisionException;
import de.quinscape.exceed.runtime.scope.ScopeType;
import de.quinscape.exceed.runtime.util.JSONUtil;
import org.svenson.JSON;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Encapsulates references to the scope declaration of a single application location.
 */
public class ScopeDeclarations
{
    private final Map<String,ScopeDeclaration> definitions = new HashMap<>();
    private final Map<String,ScopeDeclaration> definitionsRO = Collections.unmodifiableMap(definitions);

    private final String key;


    public ScopeDeclarations(String key)
    {
        if (key == null)
        {
            throw new IllegalArgumentException("key can't be null");
        }


        this.key = key;
    }


    public String getKey()
    {
        return key;
    }


    /**
     * Returns an unmodifiable map with all valid scope declarations for this location.
     *
     * @return map of scope declarations
     */
    public Map<String, ScopeDeclaration> getDeclarations()
    {
        return definitionsRO;
    }


    public void add(ContextModel context, ScopeType scopeType)
    {
        for (String name : context.getProperties().keySet())
        {
            final ScopeDeclaration existing = definitions.get(name);
            final ScopedPropertyModel model = context.getProperties().get(name);


            final ScopeType declarationScopeType = scopeType == ScopeType.VIEW && model.isFromLayout() ? ScopeType.LAYOUT : scopeType;
            final ScopeDeclaration definition = new ScopeDeclaration(name, key, declarationScopeType, model);
            if (existing != null && !existing.equals(definition))
            {
                throw new ScopeNameCollisionException("Scoped definition '" + name + "' exists in both " + existing +
                    " and " + definition);
            }
            definitions.put(name, definition);
        }

        if (scopeType == ScopeType.PROCESS)
        {
            definitions.put(ProcessContext.DOMAIN_OBJECT_CONTEXT, new ScopeDeclaration(ProcessContext.DOMAIN_OBJECT_CONTEXT, key, ScopeType.PROCESS, null));
        }
    }


    public ScopeDeclaration get(String name)
    {
        return definitions.get(name);
    }


    @Override
    public String toString()
    {
        return super.toString() + ": At '" + key + "' :\n" + JSONUtil.formatJSON(JSON.defaultJSON().forValue(definitions));
    }
}
