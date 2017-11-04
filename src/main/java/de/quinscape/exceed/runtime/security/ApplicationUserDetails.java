package de.quinscape.exceed.runtime.security;

import com.google.common.collect.ImmutableSet;
import de.quinscape.exceed.runtime.domain.DomainObject;
import de.quinscape.exceed.runtime.util.AppAuthentication;
import de.quinscape.exceed.runtime.util.Util;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ApplicationUserDetails
    implements UserDetails
{
    private static final long serialVersionUID = -2748707414897979497L;

    private final String username;
    private final String password;
    private final List<GrantedAuthority> authorities;

    private final String rolesString;

    private final Set<String> roles;

    private final Boolean disabled;


    public ApplicationUserDetails(String schema, DomainObject appUser)
    {
        username = (String) appUser.getProperty("login");
        password = (String) appUser.getProperty("password");
        rolesString = (String) appUser.getProperty("roles");
        disabled = (Boolean) appUser.getProperty("disabled");

        // first we take the user roles and filter out all schema roles
        final Set<String> userRoles = Util.splitToSet(rolesString, ",");

//        ensureNoSchemaRolesInUserRoles(userRoles);

        // then we add the one correct schema role
        userRoles.add(AppAuthentication.SCHEMA_ROLE_PREFIX + schema);

        // and remember the roles ..
        roles = ImmutableSet.copyOf(userRoles);

        // and map them to GrantedAuthority instances
        authorities = roles.stream()
            .map(SimpleGrantedAuthority::new)
            .collect(Collectors.toList());
    }

//
//    private void ensureNoSchemaRolesInUserRoles(Set<String> set)
//    {
//        for (String role : set)
//        {
//            if (role.startsWith(AppAuthentication.SCHEMA_ROLE_PREFIX))
//            {
//                throw new ApplicationSecurityException("User object contains schema role.");
//            }
//        }
//    }


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities()
    {
        return authorities;
    }

    @Override
    public String getPassword()
    {
        return password;
    }

    @Override
    public String getUsername()
    {
        return username;
    }

    @Override
    public boolean isAccountNonExpired()
    {
        return true;
    }

    @Override
    public boolean isAccountNonLocked()
    {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired()
    {
        return true;
    }

    @Override
    public boolean isEnabled()
    {
        return disabled == null || !disabled;
    }


    public String getRoles()
    {
        return rolesString;
    }


    public Set<String> roles()
    {
        return roles;
    }
}
