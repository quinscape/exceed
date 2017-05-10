package de.quinscape.exceed.runtime.editor.completion;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import de.quinscape.exceed.TestDomainServiceBase;
import de.quinscape.exceed.model.domain.DomainProperty;
import de.quinscape.exceed.model.domain.DomainType;
import de.quinscape.exceed.model.domain.EnumType;
import de.quinscape.exceed.model.view.AttributeValue;
import de.quinscape.exceed.model.view.ComponentModel;
import de.quinscape.exceed.model.view.View;
import de.quinscape.exceed.runtime.TestApplication;
import de.quinscape.exceed.runtime.TestApplicationBuilder;
import de.quinscape.exceed.runtime.application.RuntimeApplication;
import de.quinscape.exceed.runtime.domain.DefaultNamingStrategy;
import de.quinscape.exceed.runtime.domain.DomainObject;
import de.quinscape.exceed.runtime.domain.DomainService;
import de.quinscape.exceed.runtime.domain.property.PropertyConverter;
import de.quinscape.exceed.runtime.editor.completion.expression.PropCompleteEnvironment;
import de.quinscape.exceed.runtime.editor.completion.expression.PropCompleteOperations;
import de.quinscape.exceed.runtime.expression.ExpressionService;
import de.quinscape.exceed.runtime.expression.ExpressionServiceImpl;
import de.quinscape.exceed.runtime.expression.query.QueryTransformer;
import de.quinscape.exceed.runtime.model.ModelJSONService;
import de.quinscape.exceed.runtime.model.ModelJSONServiceImpl;
import de.quinscape.exceed.runtime.model.TestRegistry;
import de.quinscape.exceed.runtime.schema.DefaultStorageConfiguration;
import de.quinscape.exceed.runtime.schema.DefaultStorageConfigurationRepository;
import de.quinscape.exceed.runtime.schema.StorageConfiguration;
import de.quinscape.exceed.runtime.util.ComponentUtil;
import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.svenson.JSON;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

public class PropCompleteEnvironmentTest
{
    private final static Logger log = LoggerFactory.getLogger(PropCompleteEnvironmentTest.class);


    private ModelJSONService modelJSONService = new ModelJSONServiceImpl();


    @Test
    public void test() throws Exception
    {
        String json = FileUtils.readFileToString(new File("./src/test/java/de/quinscape/exceed/runtime/editor/completion/test-views/prop-complete.json"));


        // external view json
        View viewModel = modelJSONService.toModel(View.class, json);
        ComponentModel componentModel = viewModel.find(m -> {
            AttributeValue attrValue = m.getAttribute("name");
            return attrValue != null && attrValue.getValue().equals("xxx");
        });

        TestRegistry testRegistry = TestRegistry.loadComponentPackages("std/datagrid");

        ComponentUtil.updateComponentRegsAndParents(testRegistry, viewModel, null);

        TestApplication app = new TestApplicationBuilder().withDomainService(new TestDomainService()).build();
        ExpressionService expressionService = new ExpressionServiceImpl(ImmutableSet.of(new PropCompleteOperations()));

        QueryTransformer queryTransformer = new QueryTransformer(expressionService, new DefaultStorageConfigurationRepository(
            ImmutableMap.of("testStorage", new DefaultStorageConfiguration(null, new DefaultNamingStrategy(), null, null)),
            null));

        {

            PropCompleteEnvironment env = new PropCompleteEnvironment( app.createRuntimeContext(), queryTransformer,
                viewModel, componentModel, "name");

            List<AceCompletion> completions = env.evaluate(expressionService);

            // no "value", no "foo", already used
            assertThat(completions.size(), is(2));
            assertThat(completions.get(0).getCaption(), is("type"));
            assertThat(completions.get(0).getMeta(), is("Foo.type"));
            assertThat(completions.get(1).getCaption(), is("num"));
            assertThat(completions.get(1).getMeta(), is("Foo.num"));
        }

        {
            PropCompleteEnvironment env = new PropCompleteEnvironment(app.createRuntimeContext(), queryTransformer,
                viewModel, componentModel.getParent(), "type");

            List<AceCompletion> suggestions = env.evaluate(expressionService);

            log.info(JSON.defaultJSON().forValue(suggestions));

        }

    }

    private class TestDomainService
        extends TestDomainServiceBase
    {
        @Override
        public DomainType getDomainType(String name)
        {
            DomainType domainType = new DomainType();
            domainType.setName(name);
            domainType.setAnnotation("Test domain type " + name);

            DomainProperty enumProp = new DomainProperty("type", "Enum", "0", false);
            enumProp.setTypeParam("MyEnum");
            domainType.setProperties(Arrays.asList(
                new DomainProperty("value", "PlainText", null, false),
                new DomainProperty(name.toLowerCase(), "PlainText", null, false),
                enumProp,
                new DomainProperty("num", "Integer", "0", false)
            ));
            return domainType;
        }

        @Override
        public Map<String, DomainType> getDomainTypes()
        {
            return ImmutableMap.of(
                "Foo", getDomainType("Foo"),
                "Bar", getDomainType("Bar"),
                "Qux", getDomainType("Baz")
            );
        }
    }

}
