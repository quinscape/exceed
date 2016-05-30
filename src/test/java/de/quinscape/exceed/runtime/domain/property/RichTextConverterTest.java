package de.quinscape.exceed.runtime.domain.property;

import de.quinscape.exceed.runtime.TestApplication;
import de.quinscape.exceed.runtime.TestApplicationBuilder;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

public class RichTextConverterTest
{

    @Test
    public void testConvertToJava() throws Exception
    {
        final RichTextConverter converter = new RichTextConverter();
        final TestApplication application = new TestApplicationBuilder().build();

        assertThat(converter.convertToJava(application.createRuntimeContext(), "abc"), is("abc"));
    }


    @Test
    public void testConvertToJSON() throws Exception
    {
        final RichTextConverter converter = new RichTextConverter();
        final TestApplication application = new TestApplicationBuilder().build();

        assertThat(converter.convertToJSON(application.createRuntimeContext(), "def"), is("def"));

    }
}
