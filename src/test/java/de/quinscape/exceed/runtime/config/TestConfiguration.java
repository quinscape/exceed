package de.quinscape.exceed.runtime.config;

import org.jooq.DSLContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mock.web.MockServletContext;

import javax.servlet.ServletContext;

@Configuration
public class TestConfiguration
{
    @Bean
    public TransactionTestService testService(DSLContext dslContext)
    {
        return new TransactionTestService(dslContext);
    }

    @Bean
    public ServletContext servletContext()
    {
        return new MockServletContext("./src/test/java/de/quinscape/exceed/runtime/config/test-webapp");
    }

}
