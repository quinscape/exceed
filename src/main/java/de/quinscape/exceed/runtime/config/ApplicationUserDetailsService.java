package de.quinscape.exceed.runtime.config;

import de.quinscape.exceed.domain.tables.pojos.AppUser;
import de.quinscape.exceed.domain.Tables;
import org.jooq.DSLContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.List;

public class ApplicationUserDetailsService
    implements UserDetailsService
{

    private static Logger log = LoggerFactory.getLogger(ApplicationUserDetailsService.class);

    private final DSLContext dslContext;

    public ApplicationUserDetailsService(DSLContext dslContext)
    {
        this.dslContext = dslContext;

    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException
    {
        List<AppUser> appUsers = dslContext.select().from(Tables.APP_USER).where(Tables.APP_USER.LOGIN.eq(username)).fetchInto
            (AppUser.class);

        log.debug("Auth {} => {}", username, appUsers);

        if (appUsers.size() == 0)
        {
            throw new UsernameNotFoundException("Cannot find user " + username);
        }


        return new ApplicationUserDetails(appUsers.get(0));
    }
}
