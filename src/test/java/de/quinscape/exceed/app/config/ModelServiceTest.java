package de.quinscape.exceed.app.config;

import de.quinscape.exceed.runtime.model.ModelService;
import de.quinscape.exceed.model.routing.RoutingTable;
import de.quinscape.exceed.model.view.AttributeValueType;
import de.quinscape.exceed.model.view.ElementNode;
import de.quinscape.exceed.model.view.View;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.*;

public class ModelServiceTest
{
    private static Logger log = LoggerFactory.getLogger(ModelServiceTest.class);


    private ModelService modelService = new ModelService();

    @Test
    public void testToJSON() throws Exception
    {
        assertThat(modelService.toJSON(new RoutingTable()), is("{\"_type\":\"routing.RoutingTable\",\"rootNode\":null}"));

    }

    @Test
    public void testViewToJSON() throws Exception
    {
        View view = new View();
        view.setId("myview");
        Map<String, Object> attrs = new HashMap<>();
        attrs.put("key", "my-key");
        attrs.put("n", 12);
        attrs.put("flag", true);
        attrs.put("value", "{ this.props.foo }");
        ElementNode root = new ElementNode("Foo", attrs, new ElementNode("Bar", null));
        view.setRoot(root);

        assertThat(root.getAttribute("key").getType(), is(AttributeValueType.STRING));
        assertThat(root.getAttribute("n").getType(), is(AttributeValueType.NUMBER));
        assertThat(root.getAttribute("value").getType(), is(AttributeValueType.EXPRESSION));
        assertThat(root.getAttribute("flag").getType(), is(AttributeValueType.BOOLEAN));

        String json = modelService.toJSON(view);

        assertThat(json, containsString("myview"));
        assertThat(json, containsString("\"my-key\""));
        assertThat(json, containsString(":12"));
        assertThat(json, containsString("\"{ this.props.foo }\""));


        //log.info(JSON.formatJSON(json));

    }

    @Test
    public void testToModel() throws Exception
    {
        RoutingTable routingTable = (RoutingTable) modelService.toModel("{\"_type\":\"routing.RoutingTable\",\"rootNode\":{\"name\":\"foo\", \"mapping\": null}}");

        assertThat(routingTable,is(notNullValue()));
        assertThat(routingTable.getRootNode().getName(),is("foo"));
        assertThat(routingTable.getRootNode().getMapping(), is(nullValue()));

    }

    @Test
    public void testGetType() throws Exception
    {
        assertThat(ModelService.getType(RoutingTable.class), is("routing.RoutingTable"));
    }
}
