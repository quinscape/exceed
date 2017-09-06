package de.quinscape.exceed.runtime.js.env;

import org.junit.Test;

import java.util.Map;
import java.util.TreeMap;

import static de.quinscape.exceed.runtime.js.env.InspectUtil.inspect;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.*;

public class InspectUtilTest
{
    @Test
    public void testInspect() throws Exception
    {
        assertThat(inspect(0), is("0"));
        assertThat(inspect("foo"), is("foo"));
        assertThat(inspect(null), is("null"));
        assertThat(inspect(false), is("false"));
        assertThat(inspect(true), is("true"));


        Map<String,Object> map = new TreeMap<>();
        map.put("a", 1);
        map.put("b", "bar");
        map.put("c", map);

        assertThat(inspect(map), startsWith("{\"a\":1,\"b\":bar,\"c\":<CYCLIC: class java.util.TreeMap@"));

        assertThat(inspect(new Bean()), is("{name:baz,value:12345}"));
    }


    public static class Bean
    {
        private String name = "baz";
        private int value = 12345;


        public String getName()
        {
            return name;
        }


        public void setName(String name)
        {
            this.name = name;
        }


        public int getValue()
        {
            return value;
        }


        public void setValue(int value)
        {
            this.value = value;
        }
    }
}
