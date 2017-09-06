package de.quinscape.exceed.runtime.service.client.provider;

import de.quinscape.exceed.model.ApplicationModel;
import de.quinscape.exceed.model.context.ScopeDeclarations;
import de.quinscape.exceed.model.meta.StaticFunctionReferences;
import de.quinscape.exceed.runtime.RuntimeContext;
import de.quinscape.exceed.runtime.security.Roles;
import de.quinscape.exceed.runtime.service.client.ClientData;
import de.quinscape.exceed.runtime.service.client.ClientStateProvider;
import de.quinscape.exceed.runtime.service.client.ClientStateScope;
import de.quinscape.exceed.runtime.service.client.DefaultClientData;
import de.quinscape.exceed.runtime.service.client.ExceedAppProvider;
import de.quinscape.exceed.runtime.service.client.scope.ScopeReferenceService;
import de.quinscape.exceed.runtime.view.ViewData;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpServletRequest;

/**
 * Provides the current values of the scoped context objects referenced by view expressions and static calls
 * within component JavaScript modules.
 *
 * @see StaticFunctionReferences
 */
@ExceedAppProvider
public class ScopeInfoProvider
    implements ClientStateProvider
{
    private final ScopeReferenceService scopeReferenceService;


    @Autowired
    public ScopeInfoProvider(ScopeReferenceService scopeReferenceService)
    {
        this.scopeReferenceService = scopeReferenceService;
    }


    @Override
    public String getName()
    {
        return "scope";
    }


    @Override
    public ClientStateScope getScope()
    {
        return ClientStateScope.VIEW;
    }

    @Override
    public ClientData provide(HttpServletRequest request, RuntimeContext runtimeContext, ViewData viewData)
    {
        if (!runtimeContext.getAuthentication().hasRole(Roles.EDITOR))
        {
            return new DefaultClientData(false);
        }

        final ApplicationModel applicationModel = runtimeContext.getApplicationModel();
        final ScopeDeclarations declarations = applicationModel.lookup(runtimeContext.getView());

        return new DefaultClientData(declarations);
    }


    @Override
    public boolean isMutable()
    {
        return false;
    }

}
