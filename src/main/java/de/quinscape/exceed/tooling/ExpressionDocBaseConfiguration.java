package de.quinscape.exceed.tooling;

import de.quinscape.exceed.expression.ASTExpression;
import de.quinscape.exceed.runtime.RuntimeContext;
import de.quinscape.exceed.runtime.config.ExpressionConfiguration;
import de.quinscape.exceed.runtime.expression.query.QueryContext;
import de.quinscape.exceed.runtime.expression.query.QueryDefinition;
import de.quinscape.exceed.runtime.expression.query.QueryTransformer;
import de.quinscape.exceed.runtime.schema.StorageConfiguration;
import de.quinscape.exceed.runtime.schema.StorageConfigurationRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.util.Set;

@Configuration
@Import(ExpressionConfiguration.class)
public class ExpressionDocBaseConfiguration
{
    @Bean
    public QueryTransformer queryTransformer()
    {
        return new QueryTransformer()
        {
            @Override
            public QueryDefinition transform(
                RuntimeContext runtimeContext, QueryContext queryContext, ASTExpression astExpression
            )
            {
                return null;
            }
        };
    }
    @Bean
    public StorageConfigurationRepository storageConfigurationRepository()
    {
        return new StorageConfigurationRepository()
        {
            @Override
            public Set<String> getConfigurationNames()
            {
                return null;
            }


            @Override
            public StorageConfiguration getConfiguration(String name)
            {
                return null;
            }
        };
    }
}