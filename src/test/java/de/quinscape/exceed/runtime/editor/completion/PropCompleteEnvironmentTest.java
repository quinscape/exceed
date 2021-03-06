package de.quinscape.exceed.runtime.editor.completion;

import com.google.common.collect.ImmutableSet;
import de.quinscape.exceed.TestDomainServiceBase;
import de.quinscape.exceed.model.domain.EnumType;
import de.quinscape.exceed.model.domain.property.DomainProperty;
import de.quinscape.exceed.model.domain.type.DomainType;
import de.quinscape.exceed.model.domain.type.DomainTypeModel;
import de.quinscape.exceed.model.expression.ExpressionValue;
import de.quinscape.exceed.model.meta.PropertyType;
import de.quinscape.exceed.model.view.ComponentModel;
import de.quinscape.exceed.model.view.View;
import de.quinscape.exceed.runtime.TestApplication;
import de.quinscape.exceed.runtime.TestApplicationBuilder;
import de.quinscape.exceed.runtime.editor.completion.expression.PropCompleteEnvironment;
import de.quinscape.exceed.runtime.editor.completion.expression.PropCompleteOperations;
import de.quinscape.exceed.runtime.expression.ExpressionService;
import de.quinscape.exceed.runtime.expression.ExpressionServiceImpl;
import de.quinscape.exceed.runtime.expression.query.ComponentQueryTransformer;
import de.quinscape.exceed.runtime.model.ModelJSONService;
import de.quinscape.exceed.runtime.model.ModelJSONServiceImpl;
import de.quinscape.exceed.runtime.model.TestRegistry;
import de.quinscape.exceed.runtime.util.ComponentUtil;
import de.quinscape.exceed.runtime.util.JSONUtil;
import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
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
        viewModel.setName("test");
        ComponentModel componentModel = viewModel.find(m -> {
            ExpressionValue attrValue = m.getAttribute("name");
            return attrValue != null && attrValue.getValue().equals("xxx");
        });

        TestRegistry testRegistry = TestRegistry.loadComponentPackages("std/datagrid");

        ComponentUtil.updateComponentRegsAndParents(testRegistry, viewModel, null);

        EnumType myEnum = new EnumType();
        myEnum.setName("MyEnum");
        myEnum.setValues(Arrays.asList("A","B","C", "D"));
        TestApplication app = new TestApplicationBuilder().withDomainService(new TestDomainService()).withEnum("MyEnum", myEnum).withView(viewModel).build();
        ExpressionService expressionService = new ExpressionServiceImpl(ImmutableSet.of(new PropCompleteOperations()));

        ComponentQueryTransformer queryTransformer = new ComponentQueryTransformer(expressionService);

        {

            PropCompleteEnvironment env = new PropCompleteEnvironment( app.createRuntimeContext(viewModel), queryTransformer,
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

            log.info(JSONUtil.DEFAULT_GENERATOR.forValue(suggestions));

        }

    }

    private class TestDomainService
        extends TestDomainServiceBase
    {
        private Map<String, DomainType> domainTypes = new HashMap<>();


        {
            domainTypes.put("Foo", getDomainType("Foo"));
            domainTypes.put("Bar", getDomainType("Bar"));
            domainTypes.put("Baz", getDomainType("Baz"));
        }

        @Override
        public DomainType getDomainType(String name)
        {
            DomainTypeModel domainType = new DomainTypeModel();
            domainType.setName(name);
            domainType.setAnnotation("Test domain type " + name);

            DomainProperty enumProp = DomainProperty.builder().withName("type").withType("Enum").withDefaultValue
                ("0").build();
            enumProp.setTypeParam("MyEnum");
            domainType.setProperties(Arrays.asList(
                DomainProperty.builder().withName("value").withType(PropertyType.PLAIN_TEXT).build(),
                DomainProperty.builder().withName(name.toLowerCase()).withType(PropertyType.PLAIN_TEXT).build(),
                enumProp,
                DomainProperty.builder().withName("num").withType(PropertyType.INTEGER).withDefaultValue("0").build()
            ));
            return domainType;
        }

        @Override
        public Map<String, DomainType> getDomainTypes()
        {
            return domainTypes;
        }
    }

}
