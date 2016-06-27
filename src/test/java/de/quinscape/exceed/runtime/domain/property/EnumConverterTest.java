package de.quinscape.exceed.runtime.domain.property;

import de.quinscape.exceed.runtime.TestApplication;
import de.quinscape.exceed.runtime.TestApplicationBuilder;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

public class EnumConverterTest
{
    @Test
    public void testToJava() throws Exception
    {
        final EnumConverter converter = new EnumConverter();
        final TestApplication application = new TestApplicationBuilder().build();

        assertThat(converter.convertToJava(application.createRuntimeContext(), 1L), is(1));
        assertThat(converter.convertToJava(application.createRuntimeContext(), 2L), is(2));

    }
    @Test
    public void testToJSON() throws Exception
    {
        final EnumConverter converter = new EnumConverter();
        final TestApplication application = new TestApplicationBuilder().build();

        assertThat(converter.convertToJSON(application.createRuntimeContext(), 3), is(3L));
        assertThat(converter.convertToJSON(application.createRuntimeContext(), 4), is(4L));
    }
}
