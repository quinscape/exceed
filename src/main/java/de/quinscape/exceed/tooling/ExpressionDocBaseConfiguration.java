package de.quinscape.exceed.tooling;

import de.quinscape.exceed.expression.ASTExpression;
import de.quinscape.exceed.runtime.RuntimeContext;
import de.quinscape.exceed.runtime.config.ExpressionConfiguration;
import de.quinscape.exceed.runtime.expression.query.QueryContext;
import de.quinscape.exceed.runtime.expression.query.QueryDefinition;
import de.quinscape.exceed.runtime.expression.query.QueryTransformer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Entry point of a special spring context for model docs generation. Mocks some beans
 * referenced in {@link ExpressionConfiguration} but not really needed for docs generation.
 */
@Configuration
@Import( ExpressionConfiguration.class )
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
}
