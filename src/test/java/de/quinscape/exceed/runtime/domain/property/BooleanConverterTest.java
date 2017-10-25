package de.quinscape.exceed.runtime.domain.property;

import de.quinscape.exceed.runtime.TestApplication;
import de.quinscape.exceed.runtime.TestApplicationBuilder;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

public class BooleanConverterTest
{
    @Test
    public void testToJava() throws Exception
    {
        final BooleanConverter converter = new BooleanConverter();
        final TestApplication application = new TestApplicationBuilder().build();

        assertThat(converter.convertToJava(application.createRuntimeContext(), true), is(true));
        assertThat(converter.convertToJava(application.createRuntimeContext(), false), is(false));

    }
    @Test
    public void testToJSON() throws Exception
    {
        final BooleanConverter converter = new BooleanConverter();
        final TestApplication application = new TestApplicationBuilder().build();

        assertThat(converter.convertToJSON(application.createRuntimeContext(), true), is(true));
        assertThat(converter.convertToJSON(application.createRuntimeContext(), false), is(false));
    }
}
