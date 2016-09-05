package de.quinscape.exceed.model.routing;

import de.quinscape.exceed.runtime.application.MappingNotFoundException;
import de.quinscape.exceed.runtime.application.RoutingResult;
import de.quinscape.exceed.runtime.util.JSONUtil;
import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.svenson.JSON;
import org.svenson.JSONParser;
import org.svenson.tokenize.InputStreamSource;

import java.io.File;
import java.io.IOException;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.*;

public class RoutingTableTest
{
    private final static Logger log = LoggerFactory.getLogger(RoutingTableTest.class);

    private final RoutingTable table;

    public RoutingTableTest() throws IOException
    {
        table = read();
    }


    private RoutingTable read() throws IOException
    {
        final String json = FileUtils.readFileToString(
            new File(
                "./src/test/java/de/quinscape/exceed/model/routing/test-file/routing.json"
            ),
            "UTF-8"
        );

        return JSONParser.defaultJSONParser().parse(RoutingTable.class, json);
    }


    @Test
    public void testResolveView() throws Exception
    {
        final RoutingResult result = table.resolve("/");
        assertThat(result,is(notNullValue()));
        assertThat(result.getMapping().getViewName(),is("Home"));
    }

    @Test
    public void testResolveViewWithParams() throws Exception
    {
        final RoutingResult result = table.resolve("/detail/foobar");
        assertThat(result,is(notNullValue()));
        assertThat(result.getMapping().getViewName(),is("Detail"));
        assertThat(result.getVariables().get("id"),is("foobar"));
    }

    @Test
    public void testOptionalParams() throws Exception
    {
        final RoutingResult result = table.resolve("/detail");
        assertThat(result,is(notNullValue()));
        assertThat(result.getMapping().getViewName(),is("Detail"));
        assertThat(result.getVariables().get("id"),is(nullValue()));
    }

    @Test
    public void testResolveProcess() throws Exception
    {
        final RoutingResult result = table.resolve("/scoped");
        assertThat(result,is(notNullValue()));
        assertThat(result.getMapping().getProcessName(),is("scoped"));
        assertThat(result.getVariables().get("stateId"),is(nullValue()));

        final RoutingResult result2 = table.resolve("/scoped/123");
        assertThat(result2,is(notNullValue()));
        assertThat(result2.getMapping().getProcessName(),is("scoped"));
        assertThat(result2.getVariables().get("stateId"),is("123"));
    }

    @Test
    public void testPredence() throws Exception
    {
        // non-var matches have precedence over var-matches on the same position
        final RoutingResult result = table.resolve("/foo/special");
        assertThat(result,is(notNullValue()));
        assertThat(result.getMapping().getProcessName(),is("specialFoo"));
        assertThat(result.getVariables().get("fooId"),is(nullValue()));

        final RoutingResult result2 = table.resolve("/foo/234");
        assertThat(result2,is(notNullValue()));
        assertThat(result2.getMapping().getProcessName(),is("normalFoo"));
        assertThat(result2.getVariables().get("fooId"),is("234"));
    }

    @Test
    public void testMuliParameter() throws Exception
    {
        {
            final RoutingResult result = table.resolve("/multi/1");
            assertThat(result,is(notNullValue()));
            assertThat(result.getMapping().getViewName(),is("Multi"));
            assertThat(result.getVariables().get("a"),is("1"));
            assertThat(result.getVariables().get("b"),is(nullValue()));
            assertThat(result.getVariables().get("c"),is(nullValue()));
        }

        {
            final RoutingResult result = table.resolve("/multi/1/2");
            assertThat(result,is(notNullValue()));
            assertThat(result.getMapping().getViewName(),is("Multi"));
            assertThat(result.getVariables().get("a"),is("1"));
            assertThat(result.getVariables().get("b"),is("2"));
            assertThat(result.getVariables().get("c"),is(nullValue()));
        }
        {
            final RoutingResult result = table.resolve("/multi/1/2/3");
            assertThat(result,is(notNullValue()));
            assertThat(result.getMapping().getViewName(),is("Multi"));
            assertThat(result.getVariables().get("a"),is("1"));
            assertThat(result.getVariables().get("b"),is("2"));
            assertThat(result.getVariables().get("c"),is("3"));
        }
    }


    @Test(expected = MappingNotFoundException.class)
    public void testRequired() throws Exception
    {
        table.resolve("/multi");
    }

    @Test(expected = MappingNotFoundException.class)
    public void testRequired2() throws Exception
    {
        table.resolve("/foo");
    }

    @Test(expected = MappingNotFoundException.class)
    public void testRequired3() throws Exception
    {
        table.resolve("/foo/");
    }

    @Test
    public void testVars() throws Exception
    {
        {
            final MappingNode node = node("{A}");
            assertThat(node.isVariable(), is(true));
            assertThat(node.getVarName(), is("A"));
            assertThat(node.isRequired(), is(true));
        }

        {
            final MappingNode node = node("{B?}");
            assertThat(node.isVariable(), is(true));
            assertThat(node.getVarName(), is("B"));
            assertThat(node.isRequired(), is(false));
        }
    }


    private MappingNode node(String s)
    {
        final MappingNode node = new MappingNode();
        node.setName(s);
        return node;
    }
}
