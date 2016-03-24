package de.quinscape.exceed.runtime.model;

import de.quinscape.exceed.expression.ExpressionParser;
import de.quinscape.exceed.expression.ParseException;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

public class ExpressionRendererTest
{

    private final static Logger log = LoggerFactory.getLogger(ExpressionRendererTest.class);


    @Test
    public void testASTLogicalOr() throws ParseException
    {
        assertThat(process("true||false"), is("true || false"));
    }


    @Test
    public void testASTLogicalAnd() throws ParseException
    {
        assertThat(process("true&&false&&1"), is("true && false && 1"));
    }


    @Test
    public void testASTEquality() throws ParseException
    {
        assertThat(process("foo==1"), is("foo == 1"));
    }


    @Test
    public void testASTRelational() throws ParseException
    {
        assertThat(process("1<2"), is("1 < 2"));
        assertThat(process("1<=2"), is("1 <= 2"));
        assertThat(process("1>2"), is("1 > 2"));
        assertThat(process("1>=2"), is("1 >= 2"));
    }


    @Test
    public void testASTAdd() throws ParseException
    {
        assertThat(process("1+2"), is("1 + 2"));
        assertThat(process("1+2*3"), is("1 + 2 * 3"));
        assertThat(process("1+(2*3)"), is("1 + (2 * 3)"));
    }


    @Test
    public void testASTSub() throws ParseException
    {
        assertThat(process("1-2"), is("1 - 2"));
    }


    @Test
    public void testASTMult() throws ParseException
    {
        assertThat(process("1*2"), is("1 * 2"));
    }


    @Test
    public void testASTDiv() throws ParseException
    {
        assertThat(process("1/2"), is("1 / 2"));
    }


    @Test
    public void testASTPropertyChain() throws ParseException
    {
        assertThat(process("a.b.c"), is("a.b.c"));
        assertThat(process("a().b().c()"), is("a().b().c()"));
        assertThat(process("a.b().c"), is("a.b().c"));
    }


    @Test
    public void testASTIdentifier() throws ParseException
    {
        assertThat(process("foo"), is("foo"));
    }


    @Test
    public void testASTFunction() throws ParseException
    {
        assertThat(process("foo()"), is("foo()"));
        assertThat(process("foo(1,2)"), is("foo(1, 2)"));
    }


    @Test
    public void testASTInteger() throws ParseException
    {
        assertThat(process("123"), is("123"));
    }


    @Test
    public void testASTString() throws ParseException
    {
        assertThat(process("'\"abc'"), is("\"\\\"abc\""));
    }


    @Test
    public void testASTMap() throws ParseException
    {
        assertThat(process("{a:1,b:2}"), is("{a: 1, b: 2}"));
    }


    @Test
    public void testASTFloat() throws ParseException
    {
        assertThat(process("0.125"), is("0.125"));
    }


    @Test
    public void testASTBool() throws ParseException
    {
        assertThat(process("true"), is("true"));
        assertThat(process("false"), is("false"));
    }


    @Test
    public void testASTNull() throws ParseException
    {
        assertThat(process("null"), is("null"));
    }

    private String process(String expr) throws ParseException
    {
        ExpressionRenderer renderer = new ExpressionRenderer();
        ExpressionParser.parse(expr).childrenAccept(renderer, null);
        return renderer.getOutput();
    }
}
