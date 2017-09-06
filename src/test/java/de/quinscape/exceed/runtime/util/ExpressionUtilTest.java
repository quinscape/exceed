package de.quinscape.exceed.runtime.util;

import de.quinscape.exceed.expression.ASTExpression;
import de.quinscape.exceed.expression.ASTInteger;
import de.quinscape.exceed.expression.ExpressionParser;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;

import static de.quinscape.exceed.runtime.util.ExpressionUtil.*;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

public class ExpressionUtilTest
{
    private final static Logger log = LoggerFactory.getLogger(ExpressionUtilTest.class);


    @Test
    public void testEvaluateSimple() throws Exception
    {
        assertThat(evaluateSimple("abc def"), is("abc def"));
        assertThat(evaluateSimple("abc"), is("abc"));
        assertThat(evaluateSimple("abc.def"), is("abc.def"));
        assertThat(evaluateSimple("5"), is(5));
        assertThat(evaluateSimple("'55'"), is("55"));
        assertThat(evaluateSimple("true"), is(true));
        assertThat(evaluateSimple("false"), is(false));
        assertThat(evaluateSimple("'foo'"), is("foo"));
        assertThat(evaluateSimple("1.2"), is(new BigDecimal("1.2")));
    }


    @Test
    public void testRenderExpressionOf() throws Exception
    {
        ASTExpression expr = ExpressionParser.parse("2*(3+4)");

        final ASTInteger integer = (ASTInteger) expr.jjtGetChild(0).jjtGetChild(1).jjtGetChild(0).jjtGetChild(1);
        assertThat(integer.getValue(), is(4));
        assertThat(renderExpressionOf(integer), is("Integer 4 in { 2 * (3 + 4) }"));
    }


    @Test(expected = IllegalArgumentException.class)
    public void testComplex() throws Exception
    {
        evaluateSimple("1 + 2");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testComplex2() throws Exception
    {
        evaluateSimple("foo()");
    }
}
