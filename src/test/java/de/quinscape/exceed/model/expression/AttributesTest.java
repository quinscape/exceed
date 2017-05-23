package de.quinscape.exceed.model.expression;

import org.junit.Test;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.*;

public class AttributesTest
{
    @Test
    public void testFormatExpression() throws Exception
    {
        assertThat(ExpressionValue.formatExpression("{a}"), is("{ a }"));
        assertThat(ExpressionValue.formatExpression("{ a}"), is("{ a }"));
        assertThat(ExpressionValue.formatExpression("{a }"), is("{ a }"));
        assertThat(ExpressionValue.formatExpression("{ a }"), is("{ a }"));
    }

}
