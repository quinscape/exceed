package de.quinscape.exceed.runtime.config;

import de.quinscape.exceed.model.view.Attributes;
import de.quinscape.exceed.runtime.model.ModelFactory;
import de.quinscape.exceed.runtime.model.ModelJSONService;
import de.quinscape.exceed.model.routing.RoutingTable;
import de.quinscape.exceed.model.view.AttributeValueType;
import de.quinscape.exceed.model.view.ComponentModel;
import de.quinscape.exceed.model.view.View;
import de.quinscape.exceed.runtime.model.ModelJSONServiceImpl;
import de.quinscape.exceed.runtime.component.ComponentIdService;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.*;

public class ModelJSONServiceTest
{
    private static Logger log = LoggerFactory.getLogger(ModelJSONServiceTest.class);


    private ComponentIdService componentIdService = new ComponentIdService();
    private ModelJSONService modelJSONService = new ModelJSONServiceImpl(new ModelFactory(componentIdService));

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
        attrs.put("n", 12);
        attrs.put("flag", true);
        attrs.put("value", "{ this.props.foo }");

        ComponentModel root = new ComponentModel();
        root.setComponentIdService(componentIdService);
        root.setName("Foo");
        root.setAttrs(new Attributes(attrs));
        ComponentModel kid = new ComponentModel();
        kid.setComponentIdService(componentIdService);
        kid.setName("Bar");
        kid.init();
        root.setKids(Collections.singletonList(kid));
        root.init();
        view.setRoot(root);

        assertThat(root.getComponentId(), is("my-foo"));
        assertThat(root.getAttribute("key").getType(), is(AttributeValueType.STRING));
        assertThat(root.getAttribute("n").getType(), is(AttributeValueType.NUMBER));
        assertThat(root.getAttribute("value").getType(), is(AttributeValueType.EXPRESSION));
        assertThat(root.getAttribute("flag").getType(), is(AttributeValueType.BOOLEAN));

        assertThat(root.getKids().get(0).getAttribute("id"), is(notNullValue()));

        String json = modelJSONService.toJSON(view);

        assertThat(json, containsString("myview"));
        assertThat(json, containsString("\"my-key\""));
        assertThat(json, containsString(":12"));
        assertThat(json, containsString("\"{ this.props.foo }\""));


        //log.info(JSON.formatJSON(json));

        // ids are generated
        ComponentModel c = new ComponentModel();
        c.setComponentIdService(componentIdService);
        c.setName("Qux");
        c.init();

        String componentId = c.getComponentId();
        assertThat(componentId, is(notNullValue()));
    }

    @Test
    public void testToModel() throws Exception
    {
        RoutingTable routingTable = (RoutingTable) modelJSONService.toModel("{\"_type\":\"routing.RoutingTable\",\"rootNode\":{\"name\":\"foo\", \"mapping\": null}}");

        assertThat(routingTable,is(notNullValue()));
        assertThat(routingTable.getRootNode().getName(),is("foo"));
        assertThat(routingTable.getRootNode().getMapping(), is(nullValue()));

    }

    @Test
    public void testGetType() throws Exception
    {
        assertThat(ModelJSONServiceImpl.getType(RoutingTable.class), is("routing.RoutingTable"));
    }
}
