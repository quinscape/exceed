package de.quinscape.exceed.runtime.util;

import org.junit.Test;
import org.svenson.JSONParser;

import java.util.Arrays;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.*;

public class ComponentPathTest
{
    @Test
    public void testParsing() throws Exception
    {
        ComponentPath path = JSONParser.defaultJSONParser().parse(ComponentPath.class, "{\n" +
            "    \"content\": \"test\",\n" +
            "    \"path\": [11, 22]\n" +
            "}");


        assertThat(path.getContent(), is("test"));
        assertThat(path.getPath(), is(Arrays.asList(11L,22L)));
    }
}
