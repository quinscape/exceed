package de.quinscape.exceed.runtime.config;

import de.quinscape.exceed.runtime.security.ApplicationUserDetailsService;
import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.servlet.configuration.EnableWebMvcSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;

@EnableWebMvcSecurity
@Configuration
public class SecurityConfiguration
    extends WebSecurityConfigurerAdapter
{
    @Autowired
    private DSLContext dslContext;

    @Autowired
    private JdbcTokenRepositoryImpl jdbcTokenRepository;

    @Override
    protected void configure(HttpSecurity http) throws Exception
    {
        http
            .authorizeRequests()

                .antMatchers(
                    "/index.jsp",
                    "/code/main.js",
                    "/code/main.js.map",
                    "/res/css/**",
                    "/res/fonts/**",
                    "/res/js/**",
                    "/res/media/**",
                    "/signup",
                    "/injection-update",
                    "/"
                    ).permitAll()

                .antMatchers("/editor/**").hasRole("EDITOR")
                .anyRequest()
                .hasRole("USER")
            .and()
                .formLogin()
                .loginPage("/login")
                .loginProcessingUrl("/login_check")
                .defaultSuccessUrl("/")
                .permitAll()
            .and()
                .logout()
                .logoutUrl("/logout")
                .logoutSuccessUrl("/")
                .deleteCookies("remember-me")
            .and()
                .rememberMe()
                    .key("@7J@v9,.qDss*yR@g/SL")
                    .tokenRepository(jdbcTokenRepository)
                    .userDetailsService(userDetailsServiceBean());

    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception
    {
        auth
            .userDetailsService(new ApplicationUserDetailsService(dslContext))
            .passwordEncoder(new BCryptPasswordEncoder());

    }

    @Bean
    @Override
    public UserDetailsService userDetailsServiceBean() throws Exception
    {
        return super.userDetailsServiceBean();
    }
}
