package de.quinscape.exceed.runtime.model;

import de.quinscape.exceed.component.ComponentDescriptor;
import de.quinscape.exceed.model.view.View;
import de.quinscape.exceed.runtime.TestApplicationBuilder;
import de.quinscape.exceed.runtime.service.ComponentRegistry;
import de.quinscape.exceed.runtime.util.ComponentUtil;
import de.quinscape.exceed.runtime.util.SingleQuoteJSONParser;
import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.svenson.JSONParser;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.*;



public class ClientViewJSONGeneratorTest
{
    private final static Logger log = LoggerFactory.getLogger(ClientViewJSONGeneratorTest.class);

    private ModelJSONService modelJSONService = new ModelJSONServiceImpl();

    private ClientViewJSONGenerator viewJSONGenerator = new ClientViewJSONGenerator(null);


    @Test
    public void testViewModelClientTransformation() throws Exception
    {
        Map<String, ComponentDescriptor> descriptors = new HashMap<>();

        descriptors.put("Provider", descriptor("{'providesContext': 'Test'}"));
        descriptors.put("AnotherProvider", descriptor("{'providesContext' : 'AnotherContext'}"));
        descriptors.put("Consumer", descriptor("{ 'providesContext': 'Test.Deriv', 'propTypes': { 'ctx': { 'context': true, 'contextType': 'Test'} } }"));
        descriptors.put("ComputedConsumer", descriptor("{ 'providesContext': 'Test.Deriv2', 'propTypes': { 'ctx': { 'context': 'context[props.name]'} } }"));
        descriptors.put("DefaultProps", descriptor("{ 'propTypes': { 'defVal' :{ 'defaultValue': 'defVal default'}, 'defExpr' :{ 'defaultValue': '{ \\'default\\' }'}, 'defClient' :{ 'client': false, 'defaultValue': '{ \\'xxx\\' }'}} }"));

        ComponentDescriptor defaultDescriptor = descriptor("{}");
        descriptors.put("Grid", defaultDescriptor);
        descriptors.put("Row", defaultDescriptor);
        descriptors.put("Col", defaultDescriptor);
        descriptors.put("Heading", defaultDescriptor);
        descriptors.put("Foo", descriptor("{'classes': ['model-aware']}"));
        ComponentRegistry registry = new TestRegistry(descriptors);



        String json = FileUtils.readFileToString(new File("./src/test/java/de/quinscape/exceed/runtime/model/test-views/client-json-1.json"));
        // external view json
        View view = modelJSONService.toModel(View.class, json);

        ComponentUtil.updateComponentRegsAndParents(registry, view, null);

        //log.info("FLAT: {}", view.getRoot().flattened().map(ComponentModel::getName).collect(Collectors.toList()));

        // to client format
        String out = viewJSONGenerator.toJSON(new TestApplicationBuilder().build(), view, JSONFormat.CLIENT);

        //log.info(JSON.formatJSON(out));

        // .. and then parsed as Map for testing ( the client format is only used on the client side )

        Map viewAsMap = JSONParser.defaultJSONParser().parse(Map.class, out);
        Map<String,Object> grid = (Map<String, Object>) viewAsMap.get("root");

        {
            assertThat(grid.get("name"),is("Grid"));
            assertThat(attr(grid, "fluid"),is("{ true }"));
            assertThat(expr(grid, "fluid"),is("true"));
        }

        Map<String, Object> col = kid(kid(grid, 0), 0);
        assertThat(col.get("name"),is("Col"));
        assertThat(attr(col, "md"),is("{ 12 }"));
        assertThat(expr(col, "md"),is("12"));

        {

            Map<String, Object> heading = kid(col, 0);
            assertThat(heading.get("name"),is("Heading"));
            assertThat(attr(heading, "value"),is("{ 'Heading: ' + props.id }"));
            assertThat(expr(heading, "value"),is("'Heading: ' + 'myHeading'"));
        }

        /**
         * more thorough tests for expression transformation and constant inlining in {@link ViewExpressionRendererTest}
         */

        {
            Map<String, Object> provider = kid(col, 1);
            assertThat(provider.get("name"),is("Provider"));
            assertThat(attr(provider, "var"),is("customContext"));

            Map<String, Object> consumer = kid(provider, 0);
            assertThat(consumer.get("name"),is("Consumer"));
            assertThat(expr(consumer, "ctx"),is("customContext"));

            Map<String, Object> explicitCtx = kid(provider, 1);
            assertThat(explicitCtx.get("name"),is("Consumer"));
            assertThat(expr(explicitCtx, "ctx"),is("explicitContext"));

            Map<String, Object> computed = kid(provider, 2);
            assertThat(computed.get("name"),is("ComputedConsumer"));
            assertThat(expr(computed, "ctx"),is("customContext['ccName']"));

            Map<String, Object> exprComputed = kid(provider, 3);
            assertThat(exprComputed.get("name"),is("ComputedConsumer"));
            assertThat(expr(exprComputed, "ctx"),is("customContext.foo['ccName2']"));
        }

        {
            Map<String, Object> foo = kid(col, 2);
            assertThat(foo.get("name"),is("Foo"));
            assertThat(expr(foo, "model"),is("_v.root.kids[0].kids[0].kids[2]"));
        }

        {
            Map<String, Object> provider = kid(col, 3);
            assertThat(provider.get("name"), is("Provider"));
            assertThat(attr(provider, "var"),is("customContext"));

            Map<String, Object> provider2 = kid(provider, 0);
            assertThat(provider2.get("name"), is("AnotherProvider"));
            assertThat(attr(provider2, "var"),is("anotherContext"));

            Map<String, Object> typedConsumer = kid(provider2, 0);
            assertThat(typedConsumer.get("name"),is("Consumer"));
            assertThat(expr(typedConsumer, "ctx"),is("customContext"));

            Map<String, Object> untypedConsumer = kid(provider2, 1);
            assertThat(untypedConsumer.get("name"),is("ComputedConsumer"));
            assertThat(expr(untypedConsumer, "ctx"),is("anotherContext['ccName3']"));
        }

        {
            Map<String, Object> defaultPropsComponent = kid(col, 4);

            log.info("defaultPropsComponent: {}", defaultPropsComponent);

            assertThat(defaultPropsComponent.get("name"), is("DefaultProps"));
            assertThat(expr(defaultPropsComponent, "defExpr"),is("'default'"));
            assertThat(attr(defaultPropsComponent, "defVal"),is("defVal default"));

            // don't dump client=false props
            assertThat(attr(defaultPropsComponent, "defClient"),is((String)null));
        }
    }



    private ComponentDescriptor descriptor(String json)
    {
        return SingleQuoteJSONParser.INSTANCE.parse(ComponentDescriptor.class, json);
    }


    // view model map helpers

    private Object attr(Map<String, Object> root, String attrName)
    {
        return mapProp(root, "attrs", attrName);
    }

    private Object expr(Map<String, Object> root, String attrName)
    {
        return mapProp(root, "exprs", attrName);
    }

    private Map<String,Object> kid(Map<String, Object> root, int index)
    {
        Object kids = root.get("kids");
        if ((kids instanceof List))
        {
            return (Map<String, Object>) ((List) kids).get(index);
        }
        else
        {
            throw new IllegalStateException("kids is no list:" + kids);
        }
    }


    private Object mapProp(Map<String, Object> root, String mapPropertyName, String attrName)
    {
        Object attrs = root.get(mapPropertyName);

        if (attrs == null)
        {
            return null;
        }

        if ((attrs instanceof Map))
        {
            return ((Map) attrs).get(attrName);
        }
        else
        {
            throw new IllegalStateException("attrs is no map:" + attrs);
        }

    }
}
