package de.quinscape.exceed.runtime.expression.query;

import com.google.common.collect.ImmutableSet;
import de.quinscape.exceed.expression.ExpressionParser;
import de.quinscape.exceed.expression.ParseException;
import de.quinscape.exceed.model.domain.property.DomainProperty;
import de.quinscape.exceed.model.domain.type.DomainTypeBuilder;
import de.quinscape.exceed.model.meta.PropertyType;
import de.quinscape.exceed.runtime.domain.DefaultNamingStrategy;
import de.quinscape.exceed.runtime.domain.DomainOperations;
import de.quinscape.exceed.runtime.domain.NamingStrategy;
import de.quinscape.exceed.runtime.expression.ExpressionService;
import de.quinscape.exceed.runtime.expression.ExpressionServiceImpl;
import de.quinscape.exceed.runtime.schema.SchemaService;
import de.quinscape.exceed.runtime.schema.StorageConfiguration;
import de.quinscape.exceed.runtime.schema.StorageConfigurationRepository;
import org.jooq.Condition;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Set;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;


public class QueryFilterEnvironmentTest
{
    private static Logger log = LoggerFactory.getLogger(QueryFilterEnvironmentTest.class);

    private ExpressionService expressionService = new ExpressionServiceImpl(ImmutableSet.of(new QueryFilterOperations()));

    @Test
    public void testFilter() throws Exception
    {
        Condition c = transform("Foo.barId == Bar.id");

        assertThat(c,is(notNullValue()));

    }


    private Condition transform(String expr) throws ParseException
    {

        QueryDomainType fooType = new QueryDomainType(
            new DomainTypeBuilder("Foo")
                .withProperties(
                    DomainProperty.builder()
                        .withName("id")
                        .withType(PropertyType.UUID)
                        .build(),
                    DomainProperty.builder()
                        .withName("barId")
                        .withType(PropertyType.UUID)
                        .withForeignKey("Bar")
                        .build()
                )
                .withStorageConfiguration("Test")
                .build()
        );

        QueryDomainType barType = new QueryDomainType(
            new DomainTypeBuilder("Bar")
                .withProperties(
                    DomainProperty.builder()
                        .withName("id")
                        .withType(PropertyType.UUID)
                        .build()
                )
                .withStorageConfiguration("Test")
                .build()
        );

        fooType.join("join", barType);

        StorageConfigurationRepository repo = new StorageConfigurationRepository()
        {
            @Override
            public Set<String> getConfigurationNames()
            {
                return Collections.singleton("Test");
            }


            @Override
            public StorageConfiguration getConfiguration(String name)
            {
                return new StorageConfiguration()
                {
                    @Override
                    public NamingStrategy getNamingStrategy()
                    {
                        return new DefaultNamingStrategy();
                    }


                    @Override
                    public DomainOperations getDomainOperations()
                    {
                        return null;
                    }


                    @Override
                    public SchemaService getSchemaService()
                    {
                        return null;
                    }
                };
            }
        };
        return (Condition) expressionService.evaluate(ExpressionParser.parse(expr), new QueryFilterEnvironment(
            new QueryTransformerEnvironment(null, repo, null), fooType));
    }
}
