package de.quinscape.exceed.runtime.security;

import de.quinscape.exceed.domain.tables.pojos.AppState;
import de.quinscape.exceed.runtime.RuntimeContext;
import de.quinscape.exceed.runtime.application.RuntimeApplication;
import de.quinscape.exceed.runtime.component.DataGraph;
import de.quinscape.exceed.runtime.domain.DomainObject;
import de.quinscape.exceed.runtime.domain.DomainService;
import de.quinscape.exceed.runtime.service.ApplicationService;
import de.quinscape.exceed.runtime.service.DomainServiceRepository;
import de.quinscape.exceed.runtime.service.RuntimeContextFactory;
import de.quinscape.exceed.runtime.util.AppAuthentication;
import de.quinscape.exceed.runtime.util.JSONUtil;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import javax.servlet.ServletContext;
import java.util.List;

public class ApplicationUserDetailsService
    implements UserDetailsService
{

    private final static Logger log = LoggerFactory.getLogger(ApplicationUserDetailsService.class);

    private final DSLContext dslContext;
    private final ServletContext servletContext;
    private final ApplicationService applicationService;
    private final DomainServiceRepository domainServiceRepository;
    private final RuntimeContextFactory runtimeContextFactory;


    public ApplicationUserDetailsService(
        DSLContext dslContext,
        ServletContext servletContext,
        ApplicationService applicationService,
        DomainServiceRepository domainServiceRepository,
        RuntimeContextFactory runtimeContextFactory
    )
    {
        this.dslContext = dslContext;
        this.servletContext = servletContext;

        this.applicationService = applicationService;
        this.domainServiceRepository = domainServiceRepository;
        this.runtimeContextFactory = runtimeContextFactory;
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
                servletContext, state.getName());

            final DomainService domainService = domainServiceRepository.getDomainService(state.getName());

            final RuntimeContext systemContext = runtimeApplication.createSystemContext();

            final DataGraph graph = AppAuthentication.queryUsers(
                systemContext,
                DSL.field("login").eq(username)
            );

            if (graph.getCount() > 1)
            {
                throw new UsernameNotFoundException("AppUser-query returned more than one result: " + JSONUtil.DEFAULT_GENERATOR.forValue(graph));
            }

            if (graph.getCount() == 1)
            {
                user = (DomainObject) graph.getRootCollection().iterator().next();
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
