package de.quinscape.exceed.runtime.scope;

import de.quinscape.exceed.model.context.ContextModel;
import de.quinscape.exceed.runtime.RuntimeContext;

import java.util.HashMap;
import java.util.Map;

/**
 * Application scoped context implementation.
 */
public final class UserContext
    extends AbstractChangeTrackingScopedContext
{
    private String login;


    public UserContext(ContextModel contextModel)
    {
        super(contextModel);
    }

    protected UserContext(ContextModel contextModel, Map<String,Object> context)
    {
        super(contextModel, context);
    }

    @Override
    public ScopedContext copy(RuntimeContext runtimeContext)
    {
        return new UserContext(getContextModel(), new HashMap<>(context));
    }


    public Map<String, Object> getContextMap()
    {
        return context;
    }

    public void setLogin(String login)
    {
        this.login = login;
    }


    public String getLogin()
    {
        return login;
    }
}
