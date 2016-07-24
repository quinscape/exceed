package de.quinscape.exceed.runtime.template;

import com.google.common.collect.ImmutableMap;
import de.quinscape.exceed.runtime.util.RequestUtil;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.*;

public class BaseTemplateTest
{
    @Test
    public void testTemplate() throws Exception
    {
        assertThat(render("ABC", null), is("ABC"));
        assertThat(render("[$A]", ImmutableMap.of("A", "foo")), is("[foo]"));
        assertThat(render("[$a]", ImmutableMap.of("A", "foo")), is("[$a]"));
        assertThat(render("$A]", ImmutableMap.of("A", "foo")), is("foo]"));
        assertThat(render("[$A", ImmutableMap.of("A", "foo")), is("[foo"));
        assertThat(render("$A", ImmutableMap.of("A", "foo")), is("foo"));
        assertThat(render("1 $A 2 $B 3", ImmutableMap.of("A", "foo", "B", "bar")), is("1 foo 2 bar 3"));
        assertThat(render("$A$B", ImmutableMap.of("A", "foo", "B", "bar")), is("foobar"));
        assertThat(render("\\$A", ImmutableMap.of("A", "foo")), is("$A"));
        assertThat(render("\\\\$A", ImmutableMap.of("A", "foo")), is("\\$A"));

    }


    private String render(String s, Map<String, Object> map) throws IOException
    {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        new BaseTemplate(s).write(os, map);
        return new String(os.toByteArray(), RequestUtil.UTF_8);
    }
}
