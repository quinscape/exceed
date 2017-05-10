package de.quinscape.exceed.expression;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

public class ExpressionParserTest
{
    private final static Logger log = LoggerFactory.getLogger(ExpressionParserTest.class);


    @Test
    public void testEQ() throws Exception
    {
        // we're not testing the complete grammar, just accessing node data and
        // checking for correct node and operator detection

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


    @Test
    public void testEQ2() throws Exception
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


    @Test
    public void testOperations() throws Exception
    {

        testOp("2 == 1", Operator.EQUALS, ASTEquality.class);
        testOp("2 != 1", Operator.NOT_EQUALS, ASTEquality.class);


        testOp("2 < 1", Operator.LESS, ASTRelational.class);
        testOp("2 <= 1", Operator.LESS_OR_EQUALS, ASTRelational.class);
        testOp("2 > 1", Operator.GREATER, ASTRelational.class);
        testOp("2 >= 1", Operator.GREATER_OR_EQUALS, ASTRelational.class);

        testOp("2 + 1", Operator.ADD, ASTAdd.class);
        testOp("2 - 1", Operator.SUBTRACT, ASTSub.class);
        testOp("2 * 1", Operator.MULTIPLY, ASTMult.class);
        testOp("2 / 1", Operator.DIVIDE, ASTDiv.class);

    }


    @Test
    public void testIdentifierEquality() throws Exception
    {
        ASTEquality eq2 = (ASTEquality) parse("qux == 'abc\\n\\u0020'");
        ASTIdentifier ident = (ASTIdentifier) eq2.jjtGetChild(0);
        ASTString str = (ASTString) eq2.jjtGetChild(1);
        assertThat(ident.getName(), is("qux"));
        assertThat(str.getValue(), is("abc\n "));
    }


    @Test
    public void testPrecedence() throws Exception
    {
        // simple precedence test

        ASTAdd addNode = (ASTAdd) parse("2 + 3 * 4");
        assertThat(addNode, is(notNullValue()));
        assertThat(((ASTMult) addNode.jjtGetChild(1)).getOperator(), is(Operator.MULTIPLY));

        ASTMult multNode = (ASTMult) parse("(2 + 3) * 4");
        assertThat(multNode, is(notNullValue()));
        assertThat(((ASTAdd) multNode.jjtGetChild(0).jjtGetChild(0)).getOperator(), is(Operator.ADD));

    }


    @Test
    public void testNull() throws Exception
    {
        ASTEquality eq = (ASTEquality) parse("a == null");
        ASTNull nullNode = (ASTNull) eq.jjtGetChild(1);
        assertThat(nullNode, is(notNullValue()));
    }


    @Test
    public void testTrue() throws Exception
    {
        ASTEquality eq = (ASTEquality) parse("a == true");
        ASTBool bool = (ASTBool) eq.jjtGetChild(1);
        assertThat(bool.getValue(), is(is(true)));
    }


    @Test
    public void testFalse() throws Exception
    {
        ASTEquality eq = (ASTEquality) parse("a == false");
        ASTBool bool = (ASTBool) eq.jjtGetChild(1);
        assertThat(bool.getValue(), is(is(false)));
    }


    @Test
    public void testFloat() throws Exception
    {
        ASTEquality eq = (ASTEquality) parse("a == 3.1415");

        assertThat(eq.getOperator(), is(Operator.EQUALS));
        ASTIdentifier i1 = (ASTIdentifier) eq.jjtGetChild(0);
        ASTFloat floatNode = (ASTFloat) eq.jjtGetChild(1);
        assertThat(i1.getName(), is("a"));
        assertThat(floatNode.getValue(), is(is(3.1415)));

    }


    @Test
    public void testLogicalAnd() throws Exception
    {
        ASTLogicalAnd eq = (ASTLogicalAnd) parse("a && b");

        ASTIdentifier i1 = (ASTIdentifier) eq.jjtGetChild(0);
        ASTIdentifier i2 = (ASTIdentifier) eq.jjtGetChild(1);
        assertThat(i1.getName(), is("a"));
        assertThat(i2.getName(), is("b"));
    }


    @Test
    public void testLogicalOr() throws Exception
    {
        ASTLogicalOr eq = (ASTLogicalOr) parse("a || b");

        ASTIdentifier i1 = (ASTIdentifier) eq.jjtGetChild(0);
        ASTIdentifier i2 = (ASTIdentifier) eq.jjtGetChild(1);
        assertThat(i1.getName(), is("a"));
        assertThat(i2.getName(), is("b"));
    }


    @Test
    public void testPropertyChain() throws Exception
    {
        ASTPropertyChain chain = (ASTPropertyChain) parse("domainType().join()");

        ASTFunction i1 = (ASTFunction) chain.jjtGetChild(0);
        ASTFunction i2 = (ASTFunction) chain.jjtGetChild(1);
        assertThat(i1.getName(), is("domainType"));
        assertThat(i2.getName(), is("join"));
    }


    @Test
    public void testComputedPropertyChain() throws Exception
    {
        ASTComputedPropertyChain chain = (ASTComputedPropertyChain) parse("context[props.name]");

        ASTIdentifier i1 = (ASTIdentifier) chain.jjtGetChild(0);
        assertThat(i1.getName(), is("context"));
        ASTPropertyChain i2 = (ASTPropertyChain) chain.jjtGetChild(1);
        ASTIdentifier i3 = (ASTIdentifier) i2.jjtGetChild(0);
        ASTIdentifier i4 = (ASTIdentifier) i2.jjtGetChild(1);
        assertThat(i3.getName(), is("props"));
        assertThat(i4.getName(), is("name"));
    }

    @Test
    public void testComputedPropertyChain2() throws Exception
    {
        ASTComputedPropertyChain computedChain = (ASTComputedPropertyChain) parse("a.b[c]");

        ASTPropertyChain propChain = (ASTPropertyChain) computedChain.jjtGetChild(0);

        ASTIdentifier i1 = (ASTIdentifier) propChain.jjtGetChild(0);
        assertThat(i1.getName(), is("a"));
        ASTIdentifier i2 = (ASTIdentifier) propChain.jjtGetChild(1);
        assertThat(i2.getName(), is("b"));

        ASTIdentifier i3 = (ASTIdentifier) computedChain.jjtGetChild(1);
        assertThat(i3.getName(), is("c"));
    }

    @Test
    public void testComputedPropertyChain3() throws Exception
    {
        ASTComputedPropertyChain computedChain = (ASTComputedPropertyChain) parse("a.b['c']");

        ASTPropertyChain propChain = (ASTPropertyChain) computedChain.jjtGetChild(0);

        ASTIdentifier i1 = (ASTIdentifier) propChain.jjtGetChild(0);
        assertThat(i1.getName(), is("a"));
        ASTIdentifier i2 = (ASTIdentifier) propChain.jjtGetChild(1);
        assertThat(i2.getName(), is("b"));

        ASTString i3 = (ASTString) computedChain.jjtGetChild(1);
        assertThat(i3.getValue(), is("c"));
    }


    @Test
    public void testNegate() throws Exception
    {
        ASTNegate chain = (ASTNegate) parse("-10");
        ASTInteger i1 = (ASTInteger) chain.jjtGetChild(0);
        assertThat(i1.getValue(), is(10));
    }


    @Test
    public void testNot() throws Exception
    {
        ASTNot not = (ASTNot) parse("!true");
        ASTBool i1 = (ASTBool) not.jjtGetChild(0);
        assertThat(i1.getValue(), is(true));

        Node n = parse("!foo.bar");
        //log.info(dump(n));
        not = (ASTNot) n;

        ASTPropertyChain chain = (ASTPropertyChain) not.jjtGetChild(0);
        ASTIdentifier i3 = (ASTIdentifier) chain.jjtGetChild(0);
        ASTIdentifier i4 = (ASTIdentifier) chain.jjtGetChild(1);
        assertThat(i3.getName(), is("foo"));
        assertThat(i4.getName(), is("bar"));
    }


    @Test
    public void testAssignment() throws Exception
    {
        ASTAssignment assigment = (ASTAssignment) parse("testVar = a || b");
        ASTIdentifier identifier = (ASTIdentifier) assigment.jjtGetChild(0);
        ASTLogicalOr or = (ASTLogicalOr) assigment.jjtGetChild(1);

        assertThat(identifier.getName(), is("testVar"));
        assertThat(((ASTIdentifier) or.jjtGetChild(0)).getName(), is("a"));
        assertThat(((ASTIdentifier) or.jjtGetChild(1)).getName(), is("b"));
    }


    @Test
    public void testArray() throws Exception
    {

        ASTArray array = (ASTArray) parse("[foo, 8347, 'coconut']");
        ASTIdentifier identifier = (ASTIdentifier) array.jjtGetChild(0);
        ASTInteger integer = (ASTInteger) array.jjtGetChild(1);
        ASTString str = (ASTString) array.jjtGetChild(2);

        assertThat(identifier.getName(), is("foo"));
        assertThat(integer.getValue(), is(8347));
        assertThat(str.getValue(), is("coconut"));
    }


    @Test
    public void testSequence() throws Exception
    {
        Node node = parse("a() ; c('arg') = 1 + 2; b()");

        assertThat(node.jjtGetChild(0) instanceof ASTFunction, is(true));
        assertThat(node.jjtGetChild(1) instanceof ASTAssignment, is(true));
        assertThat(node.jjtGetChild(2) instanceof ASTFunction, is(true));

    }


    private String dump(Node node)
    {
        StringBuilder sb = new StringBuilder();
        dumpRec(sb, node, 0);
        return "\n" + sb.toString();
    }


    private void dumpRec(StringBuilder sb, Node node, int level)
    {
        indent(sb, level);
        sb.append(node).append("\n");

        for (int i = 0; i < node.jjtGetNumChildren(); i++)
        {
            dumpRec(sb, node.jjtGetChild(i), level + 1);
        }
    }


    private void indent(StringBuilder sb, int level)
    {
        for (int i = 0; i < level; i++)
        {
            sb.append("  ");
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


    // this used to be an error, now it's supported as expression sequence
    public void testError() throws Exception
    {

        Node node = parse("1 + 2(a)");
    }


    @Test(expected = ParseException.class)
    public void testError2() throws Exception
    {
        parse("1 < 2 < 3");
    }


    @Test(expected = ParseException.class)
    public void testError3() throws Exception
    {
        parse("1 == 2 == 3");
    }
}
