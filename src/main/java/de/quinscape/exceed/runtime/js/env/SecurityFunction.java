package de.quinscape.exceed.runtime.js.env;

import de.quinscape.exceed.runtime.util.AppAuthentication;
import jdk.nashorn.api.scripting.AbstractJSObject;

public final class SecurityFunction
    extends AbstractJSObject
{
    public final static SecurityFunction HAS_ROLE = new SecurityFunction(null);
    public final static SecurityFunction IS_ADMIN = new SecurityFunction("ROLE_ADMIN");

    private final String role;

    private SecurityFunction(String role)
    {
        this.role = role;

    }

    @Override
    public boolean isFunction()
    {
        return true;
    }


    @Override
    public Object call(Object thiz, Object... args)
    {
        final String role;
        if (this.role == null)
        {
            role = (String) args[0];
        }
        else
        {
            role = this.role;
        }

        final AppAuthentication appAuthentication = AppAuthentication.get();

        return appAuthentication.hasRole(role);
    }
}
