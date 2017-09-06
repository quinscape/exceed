package de.quinscape.exceed.runtime.model;

import de.quinscape.exceed.model.component.ComponentDescriptor;
import de.quinscape.exceed.model.view.View;
import de.quinscape.exceed.runtime.TestApplication;
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

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;


public class ClientViewJSONGeneratorTest
{
    private final static Logger log = LoggerFactory.getLogger(ClientViewJSONGeneratorTest.class);

    private ModelJSONService modelJSONService = new ModelJSONServiceImpl();

    private ClientViewJSONGenerator viewJSONGenerator = new ClientViewJSONGenerator(null);

    @Test
    public void testViewModelClientTransformation() throws Exception
    {
        ComponentRegistry registry = createTestRegistry();

        String json = FileUtils.readFileToString(new File("./src/test/java/de/quinscape/exceed/runtime/model/test-views/client-json-1.json"));
        // external view json
        View view = modelJSONService.toModel(View.class, json);

        ComponentUtil.updateComponentRegsAndParents(registry, view, null);

        //log.info("FLAT: {}", view.getRoot().flattened().map(ComponentModel::getName).collect(Collectors.toList()));

        // to client format
        final TestApplication app = new TestApplicationBuilder().build();
        app.getApplicationModel().getMetaData().getScopeMetaModel().addDeclarations(view);
        String out = viewJSONGenerator.toJSON(app.getApplicationModel(), view, JSONFormat.INTERNAL);

        //log.info(JSON.formatJSON(out));

        // .. and then parsed as Map for testing ( the client format is only used on the client side )

        Map viewAsMap = JSONParser.defaultJSONParser().parse(Map.class, out);
        Map<String,Object> grid = content(viewAsMap, "main");

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

        {
            Map<String, Object> defaultPropsComponent = kid(col, 2);

            log.info("defaultPropsComponent: {}", defaultPropsComponent);

            assertThat(defaultPropsComponent.get("name"), is("DefaultProps"));
            assertThat(expr(defaultPropsComponent, "defExpr"),is("'default'"));

            // don't dump client=false props
            assertThat(attr(defaultPropsComponent, "defClient"),is((String)null));
        }
    }

    private ComponentRegistry createTestRegistry()
    {
        Map<String, ComponentDescriptor> descriptors = new HashMap<>();

        descriptors.put("DefaultProps", descriptor("{ 'propTypes': {'defExpr' :{ 'defaultValue': '\\'default\\''}, 'defClient' :{ 'client': false, 'defaultValue': '\\'xxx\\''}} }"));

        ComponentDescriptor defaultDescriptor = descriptor("{}");
        descriptors.put("View", defaultDescriptor);
        descriptors.put("Grid", defaultDescriptor);
        descriptors.put("Row", defaultDescriptor);
        descriptors.put("Col", defaultDescriptor);
        descriptors.put("Heading", defaultDescriptor);
        descriptors.put("Foo", descriptor("{'classes': ['model-aware']}"));
        return new TestRegistry(descriptors);
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

    private Map<String,Object> content(Map<String, Object> viewAsMap, String name)
    {
        final Map contentMap = (Map) viewAsMap.get("content");
        return (Map<String, Object>) contentMap.get(name);
    }
}
