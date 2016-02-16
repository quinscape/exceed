package de.quinscape.exceed.runtime.util;

import de.quinscape.exceed.runtime.security.ApplicationUserDetails;
import de.quinscape.exceed.runtime.security.Roles;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;
import java.util.Set;

public class AppAuthentication
{
    public static final AppAuthentication ANONYMOUS = new AppAuthentication("Anonymous", Roles.ANONYMOUS);

    private final String userName;
    private final String rolesString;
    private final Set<String> roles;


    public AppAuthentication(String userName, String rolesString, Set<String> roles)
    {
        this.userName = userName;
        this.rolesString = rolesString;
        this.roles = roles;
    }

    private AppAuthentication(String name, String role)
    {

        this(name, role, Collections.singleton(role));
    }


    public String getUserName()
    {
        return userName;
    }


    public String getRoles()
    {
        return rolesString;
    }


    public Set<String> roles()
    {
        return roles;
    }


    public boolean hasRole(String role)
    {
        return roles.contains(role);
    }


    /**
     * Accesses the spring security context to contstruct a new AppAuthentication object.
     *
     * For anonymous users, {@link #ANONYMOUS} auth is returned.
     * @return
     */
    public static AppAuthentication get()
    {
        SecurityContext context = SecurityContextHolder.getContext();

        Authentication authentication = context.getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof ApplicationUserDetails)
        {
            ApplicationUserDetails userDetails = (ApplicationUserDetails) authentication.getPrincipal();

            return new AppAuthentication(userDetails.getUsername(), userDetails.getRoles(), userDetails.roles());
        }
        else
        {
            return AppAuthentication.ANONYMOUS;
        }
    }
}
