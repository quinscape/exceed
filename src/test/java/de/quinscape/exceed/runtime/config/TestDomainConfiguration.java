package de.quinscape.exceed.runtime.config;

import org.jooq.DSLContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TestDomainConfiguration
{
    @Bean
    public TransactionTestService testService(DSLContext dslContext)
    {
        return new TransactionTestService(dslContext);
    }

}
