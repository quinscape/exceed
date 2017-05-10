package de.quinscape.exceed.runtime.scope;

import com.google.common.collect.ImmutableMap;
import de.quinscape.exceed.model.context.ScopeDeclaration;
import de.quinscape.exceed.model.context.ScopeDeclarations;
import de.quinscape.exceed.model.context.ScopeMetaModel;
import de.quinscape.exceed.model.context.ScopedPropertyModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Encapsulates all existing scopes for an execution context.
 *
 */
public final class ScopedContextChain
    implements ScopedResolver
{
    private final List<ScopedContext> chainedScopes;

    private final static Map<Class<? extends ScopedContext>, ScopeType> scopeIndexMap = ImmutableMap.of(
        ApplicationContext.class, ScopeType.APPLICATION,
        SessionContext.class, ScopeType.SESSION,
        ProcessContext.class, ScopeType.PROCESS,
        ViewContext.class, ScopeType.VIEW
    );

    private final ScopeMetaModel scopeMetaModel;

    private String scopeLocation;

    private ScopeDeclarations scopeDeclarations;


    public ScopedContextChain(List<ScopedContext> chainedScopes, ScopeMetaModel scopeMetaModel, String scopeLocation)
    {
        if (chainedScopes == null)
        {
            throw new IllegalArgumentException("chainedScopes can't be null");
        }

        this.chainedScopes = sortByTypeOrdinal(chainedScopes);

        this.scopeMetaModel = scopeMetaModel;
        this.scopeLocation = scopeLocation;
        if (scopeLocation != null)
        {
            this.scopeDeclarations = scopeMetaModel.lookup(scopeLocation);
        }
    }


    private List<ScopedContext> sortByTypeOrdinal(List<ScopedContext> chainedScopes)
    {
        final int numScopes = ScopeType.values().length;
        final List<ScopedContext> list = new ArrayList<>(Collections.nCopies(numScopes, null));

        for (ScopedContext context : chainedScopes)
        {
            if (context != null)
            {
                final ScopeType scopeType = scopeIndexMap.get(context.getClass());
                if (scopeType == null)
                {
                    throw new IllegalArgumentException("Unknown scope type " + context);
                }

                list.set(scopeType.ordinal(), context);
            }
        }
        return list;
    }


    @Override
    public Object getProperty(String name)
    {
        final ScopeDeclaration definition = getDefinitionInternal(name);
        return chainedScopes.get(getScopeIndex(definition)).getProperty(name);
    }

    @Override
    public ScopedPropertyModel getModel(String name)
    {
        final ScopeDeclaration definition = getDefinitionInternal(name);

        final ScopedPropertyModel model = definition.getModel();

        if (definition.getScopeType() == ScopeType.PROCESS && name.equals(ProcessContext.DOMAIN_OBJECT_CONTEXT))
        {
            final ProcessContext processContext = (ProcessContext) chainedScopes.get(getScopeIndex(definition));
            return processContext.getCurrentDomainObjectModel();
        }
        return model;
    }


    @Override
    public void setProperty(String name, Object value)
    {
        final ScopeDeclaration declaration = getDefinitionInternal(name);
        chainedScopes.get(getScopeIndex(declaration)).setProperty(name, value);
    }


    private ScopeDeclaration getDefinitionInternal(String name)
    {
        final ScopeDeclaration declaration = scopeDeclarations.get(name);
        if (declaration == null)
        {
            throw new ScopeResolutionException("No property '" + name + "' in either " + describe() + ". Definitions are " + scopeDeclarations);
        }
        return declaration;
    }


    @Override
    public boolean hasProperty(String name)
    {
        final ScopeDeclaration declaration = scopeDeclarations.get(name);
        return declaration != null;
    }

    public ScopedContext findScopeWithProperty(String name)
    {
        final ScopeDeclaration declaration = scopeDeclarations.get(name);
        if (declaration != null)
        {
            return chainedScopes.get(getScopeIndex(declaration));
        }
        return null;
    }


    public int getScopeIndex(ScopeDeclaration declaration)
    {
        final ScopeType scopeType = declaration.getScopeType();
        return scopeType == ScopeType.LAYOUT ? ScopeType.VIEW.ordinal() : scopeType.ordinal();
    }


    private String describe()
    {
        StringBuilder sb = new StringBuilder();
        for (Iterator<ScopedContext> iterator = chainedScopes.iterator(); iterator.hasNext(); )
        {
            ScopedContext ctx = iterator.next();
            if (ctx != null)
            {
                sb.append(ctx.getClass().getSimpleName());

                if (iterator.hasNext())
                {
                    sb.append(", ");
                }
            }
        }
        return sb.toString();
    }


    /**
     * Updates one scoped context in its scope type determined position
     *
     * @param scopedContext new scoped context
     * @param scopeLocation new scope location identifier
     *
     */
    public void update(ScopedContext scopedContext, String scopeLocation)
    {
        final ScopeType scopeType = scopeIndexMap.get(scopedContext.getClass());
        if (scopeType == null)
        {
            throw new IllegalStateException("Unhandled scope type: " + scopedContext);
        }

        chainedScopes.set(scopeType.ordinal(), scopedContext);

        this.scopeLocation = scopeLocation;
        this.scopeDeclarations = scopeLocation == null ? null :  scopeMetaModel.lookup(scopeLocation);
    }

    public void clearContext(ScopeType scopeType)
    {
        chainedScopes.set(scopeType.ordinal(), null);
    }


    public ApplicationContext getApplicationContext()
    {
        return (ApplicationContext) chainedScopes.get(ScopeType.APPLICATION.ordinal());
    }

    public SessionContext getSessionContext()
    {
        return (SessionContext) chainedScopes.get(ScopeType.SESSION.ordinal());
    }

    public ProcessContext getProcessContext()
    {
        return (ProcessContext) chainedScopes.get(ScopeType.PROCESS.ordinal());
    }

    public ViewContext getViewContext()
    {
        return (ViewContext) chainedScopes.get(ScopeType.VIEW.ordinal());
    }


    public String getScopeLocation()
    {
        return scopeLocation;
    }


}
