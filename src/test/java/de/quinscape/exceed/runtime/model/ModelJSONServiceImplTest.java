package de.quinscape.exceed.runtime.model;

import com.google.common.collect.ImmutableMap;
import de.quinscape.exceed.component.ComponentDescriptor;
import de.quinscape.exceed.component.PropDeclaration;
import de.quinscape.exceed.model.view.ComponentModel;
import de.quinscape.exceed.model.view.View;
import de.quinscape.exceed.runtime.service.ComponentRegistry;
import de.quinscape.exceed.runtime.util.ComponentUtil;
import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.svenson.JSON;
import org.svenson.JSONParser;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.*;



public class ModelJSONServiceImplTest
{
    private static Logger log = LoggerFactory.getLogger(ModelJSONServiceImplTest.class);

    private ModelJSONService modelJSONService = new ModelJSONServiceImpl(new ModelFactory());

    @Test
    public void testViewModelClientJSON() throws Exception
    {
        String json = FileUtils.readFileToString(new File("./src/test/java/de/quinscape/exceed/runtime/model/test-views/client-json-1.json"));

        // external view json
        View view = modelJSONService.toModel(View.class, json);
        // to client format
        Map<String, ComponentDescriptor> descriptors = new HashMap<>();

        descriptors.put("Provider", new ComponentDescriptor(null, null, null, null, null, null, null, true, false));
        descriptors.put("Consumer", new ComponentDescriptor(null, null, ImmutableMap.of("ctx", new PropDeclaration("", false, true)), null, null, null, null, true, false));
        descriptors.put("ComputedConsumer", new ComponentDescriptor(null, null, ImmutableMap.of("ctx", new PropDeclaration("", false, "context[props.name]")), null, null, null, null, true, false));
        descriptors.put("Grid", new ComponentDescriptor(null, null,null, null, null, null, null, true, false));
        descriptors.put("Row", new ComponentDescriptor(null, null,null, null, null, null, null, true, false));
        descriptors.put("Col", new ComponentDescriptor(null, null,null, null, null, null, null, true, false));
        descriptors.put("Heading", new ComponentDescriptor(null, null,null, null, null, null, null, true, false));
        descriptors.put("Foo", new ComponentDescriptor(null, null,null, null, null, null, null, false, true));
        ComponentRegistry registry = new TestRegistry(descriptors);

        ComponentUtil.updateComponentRegistrations(registry, view.getRoot(), null);
        String out = modelJSONService.toJSON(view, JSONFormat.CLIENT);

        log.info(JSON.formatJSON(out));

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
            assertThat(expr(heading, "value"),is("\"Heading: \" + \"myHeading\""));
        }

        /**
         * more thorough tests for expression transformation and constant inlining in {@link ClientExpressionRendererTest}
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
            assertThat(expr(computed, "ctx"),is("customContext[\"ccName\"]"));

            Map<String, Object> exprComputed = kid(provider, 3);
            assertThat(exprComputed.get("name"),is("ComputedConsumer"));
            assertThat(expr(exprComputed, "ctx"),is("customContext.foo[\"ccName2\"]"));
        }


        Map<String, Object> foo = kid(col, 2);
        assertThat(foo.get("name"),is("Foo"));
        assertThat(expr(foo, "model"),is("_v.root.kids[0].kids[0].kids[2]"));
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
