package de.quinscape.exceed.model.view;

import org.junit.Test;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.*;

public class AttributesTestCase
{

    @Test
    public void testFormatExpression() throws Exception
    {
        assertThat(Attributes.formatExpression("{a}"), is("{ a }"));
        assertThat(Attributes.formatExpression("{ a}"), is("{ a }"));
        assertThat(Attributes.formatExpression("{a }"), is("{ a }"));
        assertThat(Attributes.formatExpression("{ a }"), is("{ a }"));
    }
}
