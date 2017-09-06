package de.quinscape.exceed.expression;

import de.quinscape.exceed.runtime.util.ExpressionUtil;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;

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
        ASTIdentifier i2 = (ASTIdentifier) ((ASTPropertyChainDot)prop.jjtGetChild(1)).jjtGetChild(0);
        assertThat(i1.getName(), is("foo"));
        assertThat(i2.getName(), is("bar"));

        ASTInteger v = (ASTInteger) eq.jjtGetChild(1);
        assertThat(v.getValue(), is(1));

        assertThat(eq.jjtGetParent(), is(notNullValue()));
    }


    @Test
    public void testEQ2() throws Exception
    {
        ASTEquality eq = (ASTEquality) parse("foo.baz() == 2");

        assertThat(eq.getOperator(), is(Operator.EQUALS));
        ASTPropertyChain prop = (ASTPropertyChain) eq.jjtGetChild(0);
        ASTIdentifier i1 = (ASTIdentifier) prop.jjtGetChild(0);
        ASTFunction fn = (ASTFunction) prop.jjtGetChild(1).jjtGetChild(0);
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
    public void testDecimal() throws Exception
    {
        ASTEquality eq = (ASTEquality) parse("a == 3.1415");

        assertThat(eq.getOperator(), is(Operator.EQUALS));
        ASTIdentifier i1 = (ASTIdentifier) eq.jjtGetChild(0);
        ASTDecimal floatNode = (ASTDecimal) eq.jjtGetChild(1);
        assertThat(i1.getName(), is("a"));
        assertThat(floatNode.getValue(), is(new BigDecimal("3.1415")));
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

        //log.info(ExpressionUtil.dump(chain));

        ASTFunction i1 = (ASTFunction) chain.jjtGetChild(0);
        ASTFunction i2 = (ASTFunction) ((ASTPropertyChainDot)chain.jjtGetChild(1)).jjtGetChild(0);
        assertThat(i1.getName(), is("domainType"));
        assertThat(i2.getName(), is("join"));
    }


    @Test
    public void testComputedPropertyChain() throws Exception
    {
        ASTPropertyChain chain = (ASTPropertyChain) parse("context[props.name]");

        ASTIdentifier i1 = (ASTIdentifier) chain.jjtGetChild(0);
        assertThat(i1.getName(), is("context"));
        ASTPropertyChain i2 = (ASTPropertyChain) chain.jjtGetChild(1).jjtGetChild(0);
        ASTIdentifier i3 = (ASTIdentifier) i2.jjtGetChild(0);
        ASTIdentifier i4 = (ASTIdentifier) i2.jjtGetChild(1).jjtGetChild(0);
        assertThat(i3.getName(), is("props"));
        assertThat(i4.getName(), is("name"));
    }

    @Test
    public void testComputedPropertyChain2() throws Exception
    {
        ASTPropertyChain propChain = (ASTPropertyChain) parse("a.b[c].d");

        //log.info(ExpressionUtil.dump(propChain));

        ASTIdentifier i1 = (ASTIdentifier) propChain.jjtGetChild(0);
        assertThat(i1.getName(), is("a"));
        final ASTPropertyChainDot dotElem = (ASTPropertyChainDot) propChain.jjtGetChild(1);
        ASTIdentifier i2 = (ASTIdentifier) dotElem.jjtGetChild(0);
        assertThat(i2.getName(), is("b"));
        final ASTPropertyChainSquare sqElem = (ASTPropertyChainSquare) propChain.jjtGetChild(2);
        ASTIdentifier i3 = (ASTIdentifier) sqElem.jjtGetChild(0);
        assertThat(i3.getName(), is("c"));

        final ASTPropertyChainDot dotElem2 = (ASTPropertyChainDot) propChain.jjtGetChild(3);
        ASTIdentifier i4 = (ASTIdentifier) dotElem2.jjtGetChild(0);
        assertThat(i4.getName(), is("d"));
    }

    @Test
    public void testComputedPropertyChain3() throws Exception
    {
        ASTPropertyChain propChain = (ASTPropertyChain) parse("a.b['c']");

        //log.info(ExpressionUtil.dump(propChain));

        ASTIdentifier i1 = (ASTIdentifier) propChain.jjtGetChild(0);
        assertThat(i1.getName(), is("a"));
        final ASTPropertyChainDot dotElem = (ASTPropertyChainDot) propChain.jjtGetChild(1);
        ASTIdentifier i2 = (ASTIdentifier) dotElem.jjtGetChild(0);
        assertThat(i2.getName(), is("b"));
        final ASTPropertyChainSquare sqElem = (ASTPropertyChainSquare) propChain.jjtGetChild(2);
        ASTString i3 = (ASTString) sqElem.jjtGetChild(0);
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
        ASTIdentifier i4 = (ASTIdentifier) chain.jjtGetChild(1).jjtGetChild(0);
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

        assertThat(node, is(instanceOf(ASTExpressionSequence.class)));

        assertThat(node.jjtGetChild(0) instanceof ASTFunction, is(true));
        assertThat(node.jjtGetChild(1) instanceof ASTAssignment, is(true));
        assertThat(node.jjtGetChild(2) instanceof ASTFunction, is(true));

    }


    @Test
    public void testMixedPropChain() throws Exception
    {
        ASTPropertyChain chain = (ASTPropertyChain) parse("a()[1]['b'][2].c()");


        assertThat(((ASTFunction)chain.jjtGetChild(0)).getName(), is("a"));
        assertThat(((ASTInteger)((ASTPropertyChainSquare)chain.jjtGetChild(1)).jjtGetChild(0)).getValue(), is(1));
        assertThat(((ASTString)((ASTPropertyChainSquare)chain.jjtGetChild(2)).jjtGetChild(0)).getValue(), is("b"));
        assertThat(((ASTInteger)((ASTPropertyChainSquare)chain.jjtGetChild(3)).jjtGetChild(0)).getValue(), is(2));
        assertThat(((ASTFunction)((ASTPropertyChainDot)chain.jjtGetChild(4)).jjtGetChild(0)).getName(), is("c"));

    }


    @Test
    public void testChain() throws Exception
    {
        log.info(ExpressionUtil.dump(parse("(scope('foo')).foo")));
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

        assertThat(exprNode.jjtGetParent(), is(nullValue()));

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

    @Test()
    public void testTrailingSemicolon() throws Exception
    {
        ASTFunction fn = (ASTFunction) parse("a();");
        assertThat(fn.getName(),is("a"));

        ASTExpressionSequence seq = (ASTExpressionSequence) parse("a();b();");
        assertThat(seq.jjtGetNumChildren(), is(2));
        assertThat(((ASTFunction)seq.jjtGetChild(0)).getName(), is("a"));
        assertThat(((ASTFunction)seq.jjtGetChild(1)).getName(), is("b"));

        ASTFunction fn2 = (ASTFunction) parse("c();;");
        assertThat(fn2.getName(),is("c"));
    }

}
