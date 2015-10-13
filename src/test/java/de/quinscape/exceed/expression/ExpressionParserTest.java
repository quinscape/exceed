package de.quinscape.exceed.expression;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.*;

public class ExpressionParserTest
{
    private static Logger log = LoggerFactory.getLogger(ExpressionParserTest.class);

    @Test
    public void testParse() throws Exception
    {
        // we're not testing the complete grammar, just accessing node data and
        // checking for correct node and operator detection

        {
            ASTEquality eq = (ASTEquality) parse("foo.bar == 1");

            assertThat(eq.getOperator(), is(Operator.EQUALS));
            ASTPropertyChain prop = (ASTPropertyChain) eq.jjtGetChild(0);
            ASTIdentifier i1 = (ASTIdentifier) prop.jjtGetChild(0);
            ASTIdentifier i2 = (ASTIdentifier) prop.jjtGetChild(1);
            assertThat(i1.getName(), is("foo"));
            assertThat(i2.getName(), is("bar"));

            ASTInteger v = (ASTInteger) eq.jjtGetChild(1);
            assertThat(v.getValue(), is(1));
        }

        {
            ASTEquality eq = (ASTEquality) parse("foo.baz() == 2");

            assertThat(eq.getOperator(), is(Operator.EQUALS));
            ASTPropertyChain prop = (ASTPropertyChain) eq.jjtGetChild(0);
            ASTIdentifier i1 = (ASTIdentifier) prop.jjtGetChild(0);
            ASTFunction fn = (ASTFunction) prop.jjtGetChild(1);
            assertThat(i1.getName(), is("foo"));
            assertThat(fn.getName(), is("baz"));

            ASTInteger v = (ASTInteger) eq.jjtGetChild(1);
            assertThat(v.getValue(), is(2));
        }

        testOp("2 == 1", Operator.EQUALS, ASTEquality.class);
        testOp("2 != 1", Operator.NOT_EQUALS, ASTEquality.class);


        testOp("2 < 1", Operator.LESS, ASTRelational.class);
        testOp("2 <= 1", Operator.LESS_OR_EQUALS, ASTRelational.class);
        testOp("2 > 1", Operator.GREATER, ASTRelational.class);
        testOp("2 >= 1", Operator.GREATER_OR_EQUALS, ASTRelational.class);

        testOp("2 + 1", Operator.ADD, ASTAdd.class);
        testOp("2 - 1", Operator.SUBTRACT, ASTAdd.class);
        testOp("2 * 1", Operator.MULTIPLY, ASTMult.class);
        testOp("2 / 1", Operator.DIVIDE, ASTMult.class);

        ASTEquality eq2 = (ASTEquality) parse("qux == 'abc\\n\\u0020'");
        ASTIdentifier ident = (ASTIdentifier) eq2.jjtGetChild(0);
        ASTString str = (ASTString) eq2.jjtGetChild(1);
        assertThat(ident.getName(), is("qux"));
        assertThat(str.getValue(), is("abc\n "));

        // simple precedence test

        ASTAdd addNode = (ASTAdd) parse("2 + 3 * 4");
        assertThat(addNode, is(notNullValue()));
        assertThat(((ASTMult)addNode.jjtGetChild(1)).getOperator(), is(Operator.MULTIPLY));

        ASTMult multNode = (ASTMult) parse("(2 + 3) * 4");
        assertThat(multNode, is(notNullValue()));
        assertThat(((ASTAdd)((ASTExpression) multNode.jjtGetChild(0)).jjtGetChild(0)).getOperator(), is(Operator.ADD));

        {
            ASTEquality eq = (ASTEquality) parse("a == null");
            ASTNull nullNode = (ASTNull) eq.jjtGetChild(1);
            assertThat(nullNode, is(notNullValue()));
        }

        {
            ASTEquality eq = (ASTEquality) parse("a == true");
            ASTBool bool = (ASTBool) eq.jjtGetChild(1);
            assertThat(bool.getValue(), is(is(true)));
        }

        {
            ASTEquality eq = (ASTEquality) parse("a == false");
            ASTBool bool = (ASTBool) eq.jjtGetChild(1);
            assertThat(bool.getValue(), is(is(false)));
        }


        {
            ASTEquality eq = (ASTEquality) parse("a == 3.1415");

            assertThat(eq.getOperator(), is(Operator.EQUALS));
            ASTIdentifier i1 = (ASTIdentifier) eq.jjtGetChild(0);
            ASTFloat floatNode = (ASTFloat) eq.jjtGetChild(1);
            assertThat(i1.getName(), is("a"));
            assertThat(floatNode.getValue(), is(is(3.1415)));

        }

        {
            ASTLogicalAnd eq = (ASTLogicalAnd) parse("a && b");

            ASTIdentifier i1 = (ASTIdentifier) eq.jjtGetChild(0);
            ASTIdentifier i2 = (ASTIdentifier) eq.jjtGetChild(1);
            assertThat(i1.getName(), is("a"));
            assertThat(i2.getName(), is("b"));
        }

        {
            ASTLogicalOr eq = (ASTLogicalOr) parse("a || b");

            ASTIdentifier i1 = (ASTIdentifier) eq.jjtGetChild(0);
            ASTIdentifier i2 = (ASTIdentifier) eq.jjtGetChild(1);
            assertThat(i1.getName(), is("a"));
            assertThat(i2.getName(), is("b"));
        }
    }


    private void testOp(String expr, Operator op, Class<? extends OperatorNode> cls) throws ParseException
    {
        OperatorNode eq2 = (OperatorNode) parse(expr);
        assertThat(eq2.getOperator(), is(op));
        assertThat("'" + expr + "' does not parse to " + cls, cls.isInstance(eq2), is(true));
    }


    private Node parse(String expr) throws ParseException
    {
        ASTExpression exprNode = ExpressionParser.parse(expr);
        return exprNode.jjtGetChild(0);
    }


    @Test(expected = ParseException.class)
    public void testName() throws Exception
    {
        parse("1 + 2(a)");

    }
}
