package de.quinscape.exceed.model.context;

import com.google.common.collect.ImmutableMap;
import de.quinscape.exceed.model.meta.ApplicationMetaData;
import de.quinscape.exceed.model.process.Process;
import de.quinscape.exceed.runtime.js.def.Definition;
import de.quinscape.exceed.runtime.js.def.DefinitionType;
import de.quinscape.exceed.runtime.js.def.Definitions;
import de.quinscape.exceed.runtime.js.def.DefinitionsBuilder;
import de.quinscape.exceed.runtime.js.def.IdentifierDefinitionBuilder;
import de.quinscape.exceed.runtime.js.env.CurrentTypeResolver;
import de.quinscape.exceed.runtime.scope.ProcessContext;
import de.quinscape.exceed.runtime.scope.ScopeNameCollisionException;
import de.quinscape.exceed.runtime.scope.ScopeType;
import de.quinscape.exceed.runtime.util.JSONUtil;
import de.quinscape.exceed.runtime.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.svenson.JSONProperty;

import java.util.HashMap;
import java.util.Map;

/**
 * Encapsulates references to the scope declaration of a single application location.
 */
public class ScopeDeclarations
{
    private final static Logger log = LoggerFactory.getLogger(ScopeDeclarations.class);

    private final Map<String,ScopeDeclaration> declarations;
    private final Definitions localDefinitions;

    private final String scopeLocation;


    public ScopeDeclarations(String scopeLocation, ContextModel[] contexts, Definitions systemDefinitions)
    {
        if (scopeLocation == null)
        {
            throw new IllegalArgumentException("key can't be null");
        }

        if (contexts == null)
        {
            throw new IllegalArgumentException("contexts can't be null");
        }


        this.scopeLocation = scopeLocation;

        final Map<String,ScopeDeclaration> declarations = new HashMap<>();

        final DefinitionsBuilder builder = Definition.builder().merge(systemDefinitions);

        for (int i = 0, contextsLength = contexts.length; i < contextsLength; i++)
        {
            ContextModel context = contexts[i];

            if (context != null)
            {
                final ScopeType scopeType = ScopeType.values()[i];
                for (String name : context.getProperties().keySet())
                {
                    final ScopedPropertyModel model = context.getProperties().get(name);


                    final ScopeType declarationScopeType = scopeType == ScopeType.VIEW && model.isFromLayout() ? ScopeType.LAYOUT : scopeType;
                    final ScopeDeclaration definition = new ScopeDeclaration(name, this.scopeLocation, declarationScopeType, model);
                    final ScopeDeclaration existing = declarations.put(name, definition);

                    if (existing != null)
                    {
                        throw new ScopeNameCollisionException("Context definition '" + name + "' exists in both " + existing +
                            " and " + definition);
                    }

                    final IdentifierDefinitionBuilder identifierDefinitionBuilder = builder.identifier(name)
                        .withType(DefinitionType.CONTEXT)
                        .withDescription(model.getDescription());

                    if (name.equals(ProcessContext.CURRENT))
                    {
                        identifierDefinitionBuilder.withPropertyTypeResolver(CurrentTypeResolver.INSTANCE);
                    }
                    else
                    {
                        identifierDefinitionBuilder.withPropertyType(model);
                    }

                    identifierDefinitionBuilder.build();
                }
            }
        }

        this.declarations = ImmutableMap.copyOf(declarations);
        localDefinitions = builder.build();

        if (log.isDebugEnabled())
        {
            log.debug(
                "-- Scope Definitions for '" + scopeLocation + "'{}\n\n",
                Util.join(
                    localDefinitions.getDefinitions().values(),
                    "\n"
                )
            );
        }
    }

    /**
     * Returns the complete local definitions for the scope location. Includes all builtins, definitions resulting from
     * application models and all the context types applicable to the location.
     *
     * @return definitions
     *
     * @see ApplicationMetaData#getApplicationDefinitions()
     */
    @JSONProperty(ignore = true)
    public Definitions getLocalDefinitions()
    {
        return localDefinitions;
    }

    public String getScopeLocation()
    {
        return scopeLocation;
    }


    /**
     * Returns an unmodifiable map with all valid scope declarations for this location.
     *
     * @return map of scope declarations
     */
    public Map<String, ScopeDeclaration> getDeclarations()
    {
        return declarations;
    }


    public ScopeDeclaration get(String name)
    {
        return declarations.get(name);
    }


    @Override
    public String toString()
    {
        return super.toString() + ": At '" + scopeLocation + "' :\n" + JSONUtil.formatJSON(JSONUtil.DEFAULT_GENERATOR
            .forValue(declarations));
    }

    public boolean hasDefinition(String name)
    {
        return get(name) != null;
    }
}
