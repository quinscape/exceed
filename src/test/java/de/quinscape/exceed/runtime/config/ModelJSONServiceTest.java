package de.quinscape.exceed.runtime.config;

import de.quinscape.exceed.model.view.AttributeValue;
import de.quinscape.exceed.model.view.Attributes;
import de.quinscape.exceed.runtime.model.ModelFactory;
import de.quinscape.exceed.runtime.model.ModelJSONService;
import de.quinscape.exceed.model.routing.RoutingTable;
import de.quinscape.exceed.model.view.AttributeValueType;
import de.quinscape.exceed.model.view.ComponentModel;
import de.quinscape.exceed.model.view.View;
import de.quinscape.exceed.runtime.model.ModelJSONServiceImpl;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.*;

public class ModelJSONServiceTest
{
    private static Logger log = LoggerFactory.getLogger(ModelJSONServiceTest.class);


    private ModelJSONService modelJSONService = new ModelJSONServiceImpl(new ModelFactory());

    @Test
    public void testToJSON() throws Exception
    {
        assertThat(modelJSONService.toJSON(new RoutingTable()), is("{\"_type\":\"routing.RoutingTable\",\"name\":\"routing\",\"rootNode\":null}"));

    }

    @Test
    public void testViewToJSON() throws Exception
    {
        View view = new View();
        view.setName("myview");
        Map<String, Object> attrs = new HashMap<>();
        attrs.put("id", "my-foo");
        attrs.put("key", "my-key");
        attrs.put("expr", "{ this.props.foo }");

        ComponentModel root = new ComponentModel();
        root.setName("Foo");
        root.setAttrs(new Attributes(attrs));
        ComponentModel kid = new ComponentModel();
        kid.setName("Bar");
        root.setKids(Collections.singletonList(kid));
        view.setRoot(root);

        assertThat(root.getComponentId(), is("my-foo"));
        assertThat(root.getAttribute("key").getType(), is(AttributeValueType.STRING));
        assertThat(root.getAttribute("expr").getType(), is(AttributeValueType.EXPRESSION));

        String json = modelJSONService.toJSON(view);

        assertThat(json, containsString("myview"));
        assertThat(json, containsString("\"my-key\""));
        assertThat(json, containsString("\"{ this.props.foo }\""));


        //log.info(JSON.formatJSON(json));
    }

    @Test
    public void testToModel() throws Exception
    {

        {
            RoutingTable routingTable = (RoutingTable) modelJSONService.toModel("{\"_type\":\"routing.RoutingTable\",\"rootNode\":{\"name\":\"foo\", \"mapping\": null}}");

            assertThat(routingTable,is(notNullValue()));
            assertThat(routingTable.getRootNode().getName(),is("foo"));
            assertThat(routingTable.getRootNode().getMapping(), is(nullValue()));
        }

        View view = (View) modelJSONService.toModel("{\"_type\":\"view.View\",\"root\":{\"name\":\"div\", \"kids\": [\"String child\"]}}");

        assertThat(view,is(notNullValue()));
        assertThat(view.getRoot().getKids().size(),is(1));
        ComponentModel strModel = view.getRoot().getKids().get(0);
        AttributeValue valueAttr = strModel.getAttribute("value");
        assertThat(strModel.getName(),is("[String]"));
        assertThat(valueAttr.getType(),is(AttributeValueType.STRING));
        assertThat(valueAttr.getValue(),is("String child"));

    }

    @Test
    public void testGetType() throws Exception
    {
        assertThat(ModelJSONServiceImpl.getType(RoutingTable.class), is("routing.RoutingTable"));
    }
}
