package de.quinscape.exceed.runtime.config;

import de.quinscape.exceed.runtime.security.ApplicationUserDetailsService;
import de.quinscape.exceed.runtime.service.ApplicationService;
import de.quinscape.exceed.runtime.service.DomainServiceRepository;
import de.quinscape.exceed.runtime.service.RuntimeContextFactory;
import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;

import javax.servlet.ServletContext;

@EnableWebSecurity
@Configuration
public class SecurityConfiguration
    extends WebSecurityConfigurerAdapter
{

    private final static String[] PUBLIC_URIS = new String[]
        {
            "/index.jsp",
            "/error",
            "/doc/**",
            "/res/*/style/**",
            "/res/*/fonts/**",
            "/res/*/js/**",
            "/res/*/media/**",
            //"/signup",
            "/"
        };

    private final DSLContext dslContext;

    private final ApplicationService applicationService;

    private final PersistentTokenRepository persistentTokenRepository;

    private final ServletContext servletContext;

    private final DomainServiceRepository domainServiceRepository;

    private final RuntimeContextFactory runtimeContextFactory;


    @Autowired
    public SecurityConfiguration(
        DSLContext dslContext,
        ApplicationService applicationService,
        PersistentTokenRepository persistentTokenRepository,
        ServletContext servletContext,
        DomainServiceRepository domainServiceRepository,
        RuntimeContextFactory runtimeContextFactory
    )
    {
        this.dslContext = dslContext;
        this.applicationService = applicationService;
        this.persistentTokenRepository = persistentTokenRepository;
        this.servletContext = servletContext;
        this.domainServiceRepository = domainServiceRepository;
        this.runtimeContextFactory = runtimeContextFactory;
    }


    @Override
    protected void configure(HttpSecurity http) throws Exception
    {
        http
            .authorizeRequests()
            .antMatchers(
                PUBLIC_URIS
            ).permitAll()
                .antMatchers("/editor/**").hasRole("EDITOR")
                .anyRequest()
                .hasRole("USER")
            .and()
                .formLogin()
                .loginPage("/login")
                .loginProcessingUrl("/login_check")
                .defaultSuccessUrl("/app/exceed")
                .permitAll()
            .and()
                .logout()
                .logoutUrl("/logout")
                .logoutSuccessUrl("/")
                .deleteCookies("remember-me")
            .and()
                .rememberMe()
                    .key("@7J@v9,.qDss*yR@g/SL")
                    .tokenRepository(persistentTokenRepository)
                    .userDetailsService(userDetailsServiceBean());

    }


    @Override
    public void configure(WebSecurity web) throws Exception
    {
        web.ignoring()
            .antMatchers(PUBLIC_URIS);
    }


    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception
    {
        auth
            .userDetailsService(
                new ApplicationUserDetailsService(
                    dslContext,
                    servletContext,
                    applicationService,
                    domainServiceRepository,
                    runtimeContextFactory
            ))
            .passwordEncoder(new BCryptPasswordEncoder());

    }


    @Bean
    @Override
    public UserDetailsService userDetailsServiceBean() throws Exception
    {
        return super.userDetailsServiceBean();
    }
}
