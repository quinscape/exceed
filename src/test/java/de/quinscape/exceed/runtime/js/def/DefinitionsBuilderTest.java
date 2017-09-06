package de.quinscape.exceed.runtime.js.def;

import com.google.common.collect.ImmutableMap;
import de.quinscape.exceed.model.ApplicationModel;
import de.quinscape.exceed.model.domain.property.DomainProperty;
import de.quinscape.exceed.model.meta.PropertyType;
import de.quinscape.exceed.model.view.View;
import de.quinscape.exceed.runtime.TestApplication;
import de.quinscape.exceed.runtime.TestApplicationBuilder;
import de.quinscape.exceed.runtime.model.ExpressionModelContext;
import de.quinscape.exceed.runtime.util.Util;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

public class DefinitionsBuilderTest
{
    private final static Logger log = LoggerFactory.getLogger(DefinitionsBuilderTest.class);

    @Test
    public void testBuilder() throws Exception
    {
        final TestApplication app = new TestApplicationBuilder()
            .build();
        final ApplicationModel applicationModel = app.getApplicationModel();

        {
            Map<String, Definition> def = Definition.builder().build().getDefinitions();

            assertThat(def.size(), is(0));
        }

        {
            Map<String, Definition> def = Definition.builder()

                .function("test")
                    .withType(DefinitionType.BUILTIN)
                    .withReturnType(PropertyType.INTEGER)
                    .withDescription("test desc")

                .andFunction("test2")
                    .withType(DefinitionType.BUILTIN)
                    .withParameterModels(
                        DomainProperty.builder().withType(PropertyType.PLAIN_TEXT).build(),
                        DomainProperty.builder().withType(PropertyType.INTEGER).build()
                    )
                    .withReturnType(PropertyType.PLAIN_TEXT)

                .andIdentifier("ident")
                    .withType(DefinitionType.BUILTIN)
                    .withPropertyType(PropertyType.PLAIN_TEXT)
                    .withDescription("ident desc")

                .andIdentifier("ident2")
                    .withType(DefinitionType.BUILTIN)
                    .withPropertyType(PropertyType.DOMAIN_TYPE, "Foo")

                .andIdentifier("ident3")
                    .withType(DefinitionType.BUILTIN)
                    .withPropertyType(PropertyType.DECIMAL, null, ImmutableMap.of("decimalPlaces", 3))

                .buildMap();

            log.info("DEFINITIONS:\n{}", Util.join(def.values(), "\n"));

            assertThat(def.size(), is(5));

            final FunctionDefinition fn = (FunctionDefinition) def.get("test");
            assertThat(fn.getName(), is("test"));
            assertThat(fn.getDescription(), is("test desc"));
            assertThat(fn.getReturnType().getType(), is(PropertyType.INTEGER));

            final FunctionDefinition fn2 = (FunctionDefinition) def.get("test2");
            assertThat(fn2.getName(), is("test2"));
            assertThat(fn2.getReturnType().getType(), is(PropertyType.PLAIN_TEXT));
            final List<DomainProperty> parameterModels = fn2.getParameterModels();

            assertThat( parameterModels.size(), is(2));
            assertThat( parameterModels.get(0).getType(), is(PropertyType.PLAIN_TEXT));
            assertThat( parameterModels.get(1).getType(), is(PropertyType.INTEGER));

            final IdentifierDefinition ident = (IdentifierDefinition) def.get("ident");
            assertThat(ident.getName(), is("ident"));
            assertThat(ident.getDescription(), is("ident desc"));
            assertThat(ident.getPropertyType().getType(), is(PropertyType.PLAIN_TEXT));

            final IdentifierDefinition ident2 = (IdentifierDefinition) def.get("ident2");
            assertThat(ident2.getName(), is("ident2"));
            assertThat(ident2.getPropertyType().getType(), is(PropertyType.DOMAIN_TYPE));
            assertThat(ident2.getPropertyType().getTypeParam(), is("Foo"));

            final IdentifierDefinition ident3 = (IdentifierDefinition) def.get("ident3");
            assertThat(ident3.getName(), is("ident3"));
            assertThat(ident3.getPropertyType().getType(), is(PropertyType.DECIMAL));
            assertThat(ident3.getPropertyType().getTypeParam(), is(nullValue()));
            assertThat(ident3.getPropertyType().getConfig().get("decimalPlaces"), is(3));
        }

        {
            Map<String, Definition> def = Definition.builder()

                .function("test")
                    .withType(DefinitionType.BUILTIN)
                    .withReturnTypeResolver((context, node, contextModel) -> DomainProperty.builder().withType(PropertyType.BOOLEAN).build())

                .andIdentifier("ident")
                    .withType(DefinitionType.BUILTIN)
                    .withPropertyTypeResolver((context,node, contextModel) -> DomainProperty.builder().withType(PropertyType.PLAIN_TEXT).build())

                .buildMap();

            final FunctionDefinition fn = (FunctionDefinition) def.get("test");
            assertThat(fn.getName(), is("test"));
            final ExpressionModelContext ctx = new ExpressionModelContext(new View());
            assertThat(fn.getType(null, null, ctx).getType(), is(PropertyType.BOOLEAN));

            final IdentifierDefinition ident = (IdentifierDefinition) def.get("ident");
            assertThat(ident.getType(null, null, ctx).getType(), is(PropertyType.PLAIN_TEXT));

        }
    }


    @Test(expected = IllegalStateException.class)
    public void testMissingFunctionType() throws Exception
    {
        final TestApplication app = new TestApplicationBuilder()
            .build();
        final ApplicationModel applicationModel = app.getApplicationModel();

        Map<String, Definition> def = Definition.builder()
            .function("test")
            .build().getDefinitions();

    }


    @Test(expected = IllegalStateException.class)
    public void testAmbiguousFunctionType() throws Exception
    {
        final TestApplication app = new TestApplicationBuilder()
            .build();
        final ApplicationModel applicationModel = app.getApplicationModel();

        Map<String, Definition> def = Definition.builder()
            .function("test")
                .withReturnType(PropertyType.INTEGER)
                .withReturnTypeResolver((context, node, contextModel) -> null)
            .build().getDefinitions();

    }

    @Test(expected = IllegalStateException.class)
    public void testAmbiguousIdentifierType() throws Exception
    {
        final TestApplication app = new TestApplicationBuilder()
            .build();
        final ApplicationModel applicationModel = app.getApplicationModel();

        Map<String, Definition> def = Definition.builder()
            .identifier("test")
                .withPropertyType(PropertyType.INTEGER)
                .withPropertyTypeResolver((context,node, contextModel) -> null)
            .build().getDefinitions();

    }


    @Test(expected = IllegalStateException.class)
    public void testMissingIdentifierType() throws Exception
    {
        final TestApplication app = new TestApplicationBuilder()
            .build();
        final ApplicationModel applicationModel = app.getApplicationModel();

        Map<String, Definition> def = Definition.builder()
            .identifier("test")
            .build().getDefinitions();

    }
}
