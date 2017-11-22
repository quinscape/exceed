package de.quinscape.exceed.runtime.security;

import de.quinscape.exceed.model.startup.AppState;
import de.quinscape.exceed.runtime.RuntimeContext;
import de.quinscape.exceed.runtime.application.RuntimeApplication;
import de.quinscape.exceed.runtime.domain.DomainObject;
import de.quinscape.exceed.runtime.domain.DomainService;
import de.quinscape.exceed.runtime.service.ApplicationService;
import de.quinscape.exceed.runtime.util.AppAuthentication;
import de.quinscape.exceed.runtime.util.JSONUtil;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.List;

public class ApplicationUserDetailsService
    implements UserDetailsService
{

    private final static Logger log = LoggerFactory.getLogger(ApplicationUserDetailsService.class);

    private final ApplicationService applicationService;


    public ApplicationUserDetailsService(
        ApplicationService applicationService
    )
    {
        this.applicationService = applicationService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException
    {
        final List<AppState> activeApplications = applicationService.getActiveApplications();

        String schema = null;
        DomainObject user = null;
        for (AppState state : activeApplications)
        {
            final RuntimeApplication runtimeApplication = applicationService.getRuntimeApplication(
                state.getName());

            final DomainService domainService = applicationService.getDomainService(state.getName());

            final RuntimeContext systemContext = runtimeApplication.createSystemContext();

            final List<DomainObject> domainObjects = AppAuthentication.queryUsers(
                systemContext,
                DSL.field("login").eq(username)
            );

            if (domainObjects.size() > 1)
            {
                throw new UsernameNotFoundException("AppUser-query returned more than one result: " + JSONUtil.DEFAULT_GENERATOR.forValue(domainObjects));
            }

            if (domainObjects.size() == 1)
            {
                user = domainObjects.get(0);
                schema = domainService.getAuthSchema();
                break;
            }
        }

        if (user == null)
        {
            throw new UsernameNotFoundException("No application knows a user '" + username + "'");
        }
        return new ApplicationUserDetails(schema, user);
    }
}
