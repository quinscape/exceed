package de.quinscape.exceed.runtime.util;

import de.quinscape.exceed.expression.ASTExpression;
import de.quinscape.exceed.expression.ExpressionParser;
import de.quinscape.exceed.expression.ParseException;
import de.quinscape.exceed.runtime.model.ExpressionRenderer;
import org.junit.Test;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.*;

public class AssignmentReplacementVisitorTest
{
    @Test
    public void testReplacement() throws Exception
    {
        assertThat(replace("property('name') = 1"), is("(set({type: 'PROPERTY', name: 'name', value: 1, path: null}))"));
        assertThat(replace("object('name') = a"), is("(set({type: 'OBJECT', name: 'name', value: a, path: null}))"));
        assertThat(replace("list('name') = b"), is("(set({type: 'LIST', name: 'name', value: b, path: null}))"));

    }

    @Test
    public void testPath() throws Exception
    {
        assertThat(replace("property('name').val = 1"), is("(set({type: 'PROPERTY', name: 'name', value: 1, path: 'val'}))"));
        assertThat(replace("property('name').val.sub = 2"), is("(set({type: 'PROPERTY', name: 'name', value: 2, path: 'val.sub'}))"));

    }

    @Test
    public void testNested() throws Exception
    {
        // nested doesn't work, is not transformed => runs into Assignment errors
        assertThat(replace("property('name') = (property('name') = 1)"), is("(set({type: 'PROPERTY', name: 'name', value: (property('name') = 1), path: null}))"));
    }


    @Test(expected = IllegalStateException.class)
    public void testError() throws Exception
    {
        replace("foo('name') = b");
    }

    @Test(expected = IllegalStateException.class)
    public void testError2() throws Exception
    {
        replace("a = b");
    }


    public String replace(String expr) throws ParseException
    {
        ASTExpression ast = ExpressionParser.parse(expr);
        ast.jjtAccept(new AssignmentReplacementVisitor(), null);
        return ExpressionRenderer.render(ast);
    }
}
