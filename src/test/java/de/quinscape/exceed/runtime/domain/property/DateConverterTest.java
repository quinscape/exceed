package de.quinscape.exceed.runtime.domain.property;

import de.quinscape.exceed.runtime.TestApplication;
import de.quinscape.exceed.runtime.TestApplicationBuilder;
import org.junit.Test;

import java.sql.Date;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.*;

public class DateConverterTest
{

    @Test
    public void testConvertToJava() throws Exception
    {
        final DateConverter converter = new DateConverter();
        final TestApplication application = new TestApplicationBuilder().build();

        assertThat(converter.convertToJava(application.createRuntimeContext(), "1970-01-01"), is(new Date(0)));

    }


    @Test
    public void testConvertToJSON() throws Exception
    {
        final DateConverter converter = new DateConverter();
        final TestApplication application = new TestApplicationBuilder().build();
        assertThat(converter.convertToJSON(application.createRuntimeContext(), new Date(0)), is("1970-01-01"));
    }
}
