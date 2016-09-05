package de.quinscape.exceed.runtime.config;

import de.quinscape.exceed.model.Model;
import de.quinscape.exceed.model.routing.RoutingTable;
import de.quinscape.exceed.model.view.AttributeValue;
import de.quinscape.exceed.model.view.AttributeValueType;
import de.quinscape.exceed.model.view.ComponentModel;
import de.quinscape.exceed.model.view.View;
import de.quinscape.exceed.runtime.model.ModelJSONService;
import de.quinscape.exceed.runtime.model.ModelJSONServiceImpl;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

public class ModelJSONServiceTest
{
    private final static Logger log = LoggerFactory.getLogger(ModelJSONServiceTest.class);


    private ModelJSONService modelJSONService = new ModelJSONServiceImpl();

    @Test
    public void testToJSON() throws Exception
    {
        assertThat(modelJSONService.toJSON(new RoutingTable()), is("{\"type\":\"routing.RoutingTable\",\"name\":\"routing\",\"extension\":0,\"mappings\":null}"));

    }


    @Test
    public void testToModel() throws Exception
    {

        {
            RoutingTable routingTable = (RoutingTable) modelJSONService.toModel(Model.class, "{\"type\":\"routing.RoutingTable\",\"rootNode\":{\"name\":\"foo\", \"mapping\": null}}");

            assertThat(routingTable,is(notNullValue()));
            assertThat(routingTable.getRootNode().getName(),is("foo"));
            assertThat(routingTable.getRootNode().getMapping(), is(nullValue()));
        }

        View view = (View) modelJSONService.toModel(Model.class, "{\"type\":\"view.View\"," +
            "\"root\":{\"name\":\"View\",\"kids\": [{\"name\":\"div\", \"kids\": [\"String child\"]}]}}");

        assertThat(view,is(notNullValue()));
        assertThat(view.getRoot().getKids().size(),is(1));
        ComponentModel strModel = view.getRoot().getKids().get(0).getKids().get(0);
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
