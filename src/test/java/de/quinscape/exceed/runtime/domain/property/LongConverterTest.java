package de.quinscape.exceed.runtime.domain.property;

import de.quinscape.exceed.runtime.TestApplication;
import de.quinscape.exceed.runtime.TestApplicationBuilder;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

public class LongConverterTest
{

    @Test
    public void testConvertToJava() throws Exception
    {
        final LongConverter converter = new LongConverter();
        final TestApplication application = new TestApplicationBuilder().build();

        assertThat(converter.convertToJava(application.createRuntimeContext(), 1L), is(1L));
        assertThat(converter.convertToJava(application.createRuntimeContext(), 2L), is(2L));
    }


    @Test
    public void testConvertToJSON() throws Exception
    {
        final LongConverter converter = new LongConverter();
        final TestApplication application = new TestApplicationBuilder().build();

        assertThat(converter.convertToJSON(application.createRuntimeContext(), 3L), is(3L));
        assertThat(converter.convertToJSON(application.createRuntimeContext(), 4L), is(4L));

    }
}
