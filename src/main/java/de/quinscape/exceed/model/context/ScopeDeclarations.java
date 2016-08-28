package de.quinscape.exceed.model.context;

import de.quinscape.exceed.runtime.scope.ProcessContext;
import de.quinscape.exceed.runtime.scope.ScopeNameCollisionException;
import de.quinscape.exceed.runtime.scope.ScopeType;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Encapsulates references to the scope declaration of a single application location.
 */
public class ScopeDeclarations
{
    private final Map<String,ScopeDeclaration> definitions = new HashMap<>();

    private final String key;


    public ScopeDeclarations(String key)
    {

        this.key = key;
    }


    public String getKey()
    {
        return key;
    }


    public void add(ContextModel context, Set<String> names, ScopeType scopeType)
    {
        for (String name : names)
        {
            final ScopeDeclaration existing = definitions.get(name);
            final ScopedPropertyModel model = context.getProperties().get(name);
            final ScopeDeclaration definition = new ScopeDeclaration(context, name, key, scopeType, model);
            if (existing != null && !existing.equals(definition))
            {
                throw new ScopeNameCollisionException("Scoped definition '" + name + "' exists in both " + existing +
                    " and " + definition);
            }
            definitions.put(name, definition);
        }

        if (scopeType == ScopeType.PROCESS)
        {
            definitions.put(ProcessContext.DOMAIN_OBJECT_CONTEXT, new ScopeDeclaration(context, ProcessContext.DOMAIN_OBJECT_CONTEXT, key, ScopeType.PROCESS, null));
        }
    }


    public ScopeDeclaration get(String name)
    {
        return definitions.get(name);
    }
}
