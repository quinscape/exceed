package de.quinscape.exceed.runtime.security;

import de.quinscape.exceed.domain.tables.pojos.AppUser;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.StringTokenizer;

public class ApplicationUserDetails
    implements UserDetails
{
    private static final long serialVersionUID = -2748707414897979497L;

    private final String username;
    private final String password;
    private final List<GrantedAuthority> authorities;

    public ApplicationUserDetails(AppUser appUser)
    {
        username = appUser.getLogin();
        password = appUser.getPassword();

        authorities = new ArrayList<>();

        StringTokenizer tokenizer = new StringTokenizer(appUser.getRoles(), ",");
        while(tokenizer.hasMoreElements())
        {
            authorities.add(new SimpleGrantedAuthority(tokenizer.nextToken()));
        }
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
}
