package de.quinscape.exceed.runtime.security;

import de.quinscape.exceed.domain.tables.pojos.AppUser;
import de.quinscape.exceed.runtime.util.Util;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.svenson.JSON;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class ApplicationUserDetails
    implements UserDetails
{
    private static final long serialVersionUID = -2748707414897979497L;

    private final String username;
    private final String password;
    private final List<GrantedAuthority> authorities;

    private final String roles;


    public ApplicationUserDetails(AppUser appUser)
    {
        username = appUser.getLogin();
        password = appUser.getPassword();

        List<String> strings = Util.splitAtComma(appUser.getRoles());

        roles = appUser.getRoles();

        authorities = strings.stream()
            .map(SimpleGrantedAuthority::new)
            .collect(Collectors.toList());
    }

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
        return true;
    }


    public String getRoles()
    {
        return roles;
    }
}
