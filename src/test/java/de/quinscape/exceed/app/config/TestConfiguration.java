package de.quinscape.exceed.app.config;

import org.jooq.DSLContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TestConfiguration
{
    @Bean
    public TransactionTestService testService(DSLContext dslContext)
    {
        return new TransactionTestService(dslContext);
    }

}
