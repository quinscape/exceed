package de.quinscape.exceed.runtime.config;

import de.quinscape.exceed.model.Model;
import de.quinscape.exceed.model.routing.RoutingTable;
import de.quinscape.exceed.model.expression.ExpressionValue;
import de.quinscape.exceed.model.expression.ExpressionValueType;
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
        assertThat(modelJSONService.toJSON(new RoutingTable()), is("{\"type\":\"xcd.routing.RoutingTable\",\"name\":\"routing\",\"extension\":0,\"mappings\":null}"));

    }


    @Test
    public void testToModel() throws Exception
    {

        {
            RoutingTable routingTable = (RoutingTable) modelJSONService.toModel(Model.class, "{\"type\":\"xcd.routing.RoutingTable\",\"rootNode\":{\"name\":\"foo\", \"mapping\": null}}");

            assertThat(routingTable,is(notNullValue()));
            assertThat(routingTable.getRootNode().getName(),is("foo"));
            assertThat(routingTable.getRootNode().getMapping(), is(nullValue()));
        }

        View view = (View) modelJSONService.toModel(Model.class, "{\"type\":\"xcd.view.View\"," +
            "\"content\":{\"main\":{\"name\":\"div\", \"kids\": [\"String child\"]}}}");

        assertThat(view,is(notNullValue()));
        assertThat(view.getContent(View.MAIN).getKids().size(),is(1));
        ComponentModel strModel = view.getContent(View.MAIN).getKids().get(0);
        ExpressionValue valueAttr = strModel.getAttribute("value");
        assertThat(strModel.getName(),is("[String]"));
        assertThat(valueAttr.getType(),is(ExpressionValueType.STRING));
        assertThat(valueAttr.getValue(),is("String child"));

    }

    @Test
    public void testGetType() throws Exception
    {
        assertThat(Model.getType(RoutingTable.class), is("xcd.routing.RoutingTable"));
    }
}
