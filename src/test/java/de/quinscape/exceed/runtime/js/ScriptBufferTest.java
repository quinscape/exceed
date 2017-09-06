package de.quinscape.exceed.runtime.js;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

public class ScriptBufferTest
{
    @Test
    public void test() throws Exception
    {
        ScriptBuffer ns = new ScriptBuffer();

        assertThat(ns.getIdentifier("foo"), is("foo"));
        assertThat(ns.getIdentifier("foo"), is("foo2"));
        assertThat(ns.getIdentifier("foo"), is("foo3"));
        assertThat(ns.getIdentifier("fooe"), is("fooe"));
        assertThat(ns.getIdentifier("foö"), is("fooe2"));
        assertThat(ns.getIdentifier("ß"), is("ss"));
        assertThat(ns.getIdentifier("ẞ"), is("SS"));
    }


    @Test
    public void testRemove() throws Exception
    {
        ScriptBuffer ns = new ScriptBuffer();

        assertThat(ns.getIdentifier("foo"), is("foo"));
        assertThat(ns.getIdentifier("foo"), is("foo2"));

        ns.removeIdentifier("foo");
        assertThat(ns.getIdentifier("foo"), is("foo"));
        assertThat(ns.getIdentifier("foo"), is("foo3"));

    }
}
