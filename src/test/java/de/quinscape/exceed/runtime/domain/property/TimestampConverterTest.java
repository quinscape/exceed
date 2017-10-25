package de.quinscape.exceed.runtime.domain.property;

import de.quinscape.exceed.runtime.TestApplication;
import de.quinscape.exceed.runtime.TestApplicationBuilder;
import org.junit.Test;

import java.sql.Timestamp;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

public class TimestampConverterTest
{


    @Test
    public void testConvertToJava() throws Exception
    {
        final TimestampConverter converter = new TimestampConverter();
        final TestApplication application = new TestApplicationBuilder().build();

        assertThat(converter.convertToJava(application.createRuntimeContext(), "1970-01-01T00:00:00Z"), is(new Timestamp(0)));

    }


    @Test
    public void testConvertToJSON() throws Exception
    {
        final TimestampConverter converter = new TimestampConverter();
        final TestApplication application = new TestApplicationBuilder().build();
        assertThat(converter.convertToJSON(application.createRuntimeContext(), new Timestamp(0)), is("1970-01-01T00:00:00Z"));
    }
}
