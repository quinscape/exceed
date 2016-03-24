package de.quinscape.exceed.runtime.expression;

import com.google.common.collect.ImmutableSet;
import de.quinscape.exceed.expression.ASTExpression;
import de.quinscape.exceed.expression.ASTFunction;
import de.quinscape.exceed.expression.ExpressionParser;
import de.quinscape.exceed.expression.ParseException;
import de.quinscape.exceed.runtime.expression.ExpressionServiceImpl;
import de.quinscape.exceed.runtime.expression.ExpressionContext;
import de.quinscape.exceed.runtime.expression.ExpressionService;
import de.quinscape.exceed.runtime.expression.annotation.ExpressionOperations;
import de.quinscape.exceed.runtime.expression.annotation.Identifier;
import de.quinscape.exceed.runtime.expression.annotation.Operation;
import org.junit.Test;

import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.*;

public class ExpressionEnvironmentTest
{

    @Test
    public void testLogical() throws Exception
    {
        {
            Boolean result = (Boolean) transform("IDENT_TRUE && true");
            assertThat(result.booleanValue(), is(true));
        }
        {
            Boolean result = (Boolean) transform("true && IDENT_FALSE");
            assertThat(result.booleanValue(), is(false));
        }
        {
            Boolean result = (Boolean) transform("IDENT_TRUE || false");
            assertThat(result.booleanValue(), is(true));
        }
        {
            Boolean result = (Boolean) transform("IDENT_FALSE || false");
            assertThat(result.booleanValue(), is(false));
        }
    }


    @Test
    public void testIdentifier() throws Exception
    {
        {
            String result = (String) transform("value");
            assertThat(result, is("abc"));
        }
        {
            String result = (String) transform("value2");
            assertThat(result, is("xyz"));
        }
        {
            Boolean result = (Boolean) transform("IDENT_TRUE");
            assertThat(result, is(true));
        }
        {
            Boolean result = (Boolean) transform("IDENT_FALSE");
            assertThat(result, is(false));
        }
    }


    @Test
    public void testEquality() throws Exception
    {

        {
            Boolean result = (Boolean) transform("1 == 1");
            assertThat(result.booleanValue(), is(true));
        }
        {
            Boolean result = (Boolean) transform("1 != 1");
            assertThat(result.booleanValue(), is(false));
        }
        {
            Boolean result = (Boolean) transform("value == 'abc'");
            assertThat(result.booleanValue(), is(true));
        }
        {
            Boolean result = (Boolean) transform("value2 == 'xyz'");
            assertThat(result.booleanValue(), is(true));
        }
        {
            Boolean result = (Boolean) transform("value != value2");
            assertThat(result.booleanValue(), is(true));
        }
        {
            Boolean result = (Boolean) transform("value != null");
            assertThat(result.booleanValue(), is(true));
        }
    }


    @Test
    public void testStringConcatenation() throws Exception
    {
        {
            String result = (String) transform("value + ':' + value2");
            assertThat(result, is("abc:xyz"));
        }
    }

    @Test
    public void testAddition() throws Exception
    {

        {
            Double result = (Double) transform("1.0 + 2.0");
            assertThat(result, is(3.0));
        }
        {
            Double result = (Double) transform("1 + 1.0");
            assertThat(result, is(2.0));
        }
        {
            Integer result = (Integer) transform("11 + 12");
            assertThat(result, is(23));
        }
        {
            Integer result = (Integer) transform("1 + num");
            assertThat(result, is(124));
        }
    }

    @Test
    public void testSubtraction() throws Exception
    {

        {
            Double result = (Double) transform("2.0 - 1.0");
            assertThat(result, is(1.0));
        }
        {
            Double result = (Double) transform("10 - 1.0");
            assertThat(result, is(9.0));
        }
        {
            Integer result = (Integer) transform("12 - 11");
            assertThat(result, is(1));
        }
        {
            Integer result = (Integer) transform("num - 23");
            assertThat(result, is(100));
        }
    }

    @Test
    public void testMultiplication() throws Exception
    {

        {
            Double result = (Double) transform("2.0 * 3.0");
            assertThat(result, is(6.0));
        }
        {
            Double result = (Double) transform("10 * 2.0");
            assertThat(result, is(20.0));
        }
        {
            Integer result = (Integer) transform("2 * 7");
            assertThat(result, is(14));
        }
        {
            Integer result = (Integer) transform("num * 2");
            assertThat(result, is(246));
        }
    }


    @Test
    public void testDivision() throws Exception
    {
        {
            Double result = (Double) transform("2.0 / 3.0");
            assertThat(result, is(0.6666666666666666));
        }

        {
            Double result = (Double) transform("4.5 / 2");
            assertThat(result, is(2.0));
        }

        {
            Double result = (Double) transform("10 / 2.5");
            assertThat(result, is(4.0));
        }
    }


    @Test
    public void testRelational() throws Exception
    {

        {
            Boolean result = (Boolean) transform("1 < 2");
            assertThat(result.booleanValue(), is(true));
        }
        {
            Boolean result = (Boolean) transform("1 <= 2");
            assertThat(result.booleanValue(), is(true));
        }
        {
            Boolean result = (Boolean) transform("2 <= 2");
            assertThat(result.booleanValue(), is(true));
        }
        {
            Boolean result = (Boolean) transform("1 > 2");
            assertThat(result.booleanValue(), is(false));
        }
        {
            Boolean result = (Boolean) transform("3 >= 2");
            assertThat(result.booleanValue(), is(true));
        }
        {
            Boolean result = (Boolean) transform("2 >= 2");
            assertThat(result.booleanValue(), is(true));
        }
        {
            Boolean result = (Boolean) transform("3 > 1.0");
            assertThat(result.booleanValue(), is(true));
        }
        {
            Boolean result = (Boolean) transform("'zzz' > 'abc'");
            assertThat(result.booleanValue(), is(true));
        }

    }

    private ExpressionService svc = new ExpressionServiceImpl(ImmutableSet.of(new
        TestOperations()));

    private Object transform(String expr) throws ParseException
    {
        TestEnvironment env = new TestEnvironment();
        ASTExpression astExpression = ExpressionParser.parse(expr);
        return svc.evaluate(astExpression, env);
    }

    @Test
    public void testContextTransform() throws Exception
    {
        /**
         * @see de.quinscape.exceed.runtime.expression.ExpressionEnvironmentTest.TestEnvironment#foo(ASTFunction)
         * @see de.quinscape.exceed.runtime.expression.ExpressionEnvironmentTest.TestEnvironment#name(ASTFunction, Foo)
         */
        Foo foo = (Foo) transform("foo().name('Adam')");

        assertThat(foo, is(notNullValue()));
        assertThat(foo.getName(), is("Adam"));
    }


    @Test
    public void testMap() throws Exception
    {
        {
            Map result = (Map) transform("{}");
            assertThat(result.size(), is(0));
        }

        {
            Map result = (Map) transform("{ foo: IDENT_TRUE }");
            assertThat(result.get("foo"), is(true));
        }

        {
            Map result = (Map) transform("{ 'foo': 1, bar: 'abc'}");
            assertThat(result.get("foo"), is(1));
            assertThat(result.get("bar"), is("abc"));
        }
    }


    @Test
    public void testPropertyChain() throws Exception
    {
        assertThat(transform("({foo: 12 }).foo"), is(12));
        assertThat(transform("({foo: { bar : 123} }).foo.bar"), is(123));
    }

    @Test
    public void testComputedPropertyChain() throws Exception
    {
        assertThat(transform("({foo: 12 })['foo']"), is(12));
        assertThat(transform("({foo: { bar : 123} })['f' + 'o' + 'o']['bar']"), is(123));
    }

    @ExpressionOperations(environment = TestEnvironment.class)
    public static class TestOperations
    {
        @Operation
        public Foo foo(ExpressionContext<TestEnvironment> ctx)
        {
            return new Foo();
        }

        @Operation(context = Foo.class)
        public Foo name(ExpressionContext<TestEnvironment> ctx, Foo foo, String name)
        {
            foo.setName(name);
            return foo;
        }


        @Identifier
        public String value()
        {
            return "abc";
        }

        @Identifier
        public String value2(TestEnvironment testEnv)
        {
            return testEnv.getValue();
        }

        @Identifier(name = "IDENT_TRUE")
        public boolean identTrue()
        {
            return true;
        }

        @Identifier(name = "IDENT_FALSE")
        public boolean identFalse()
        {
            return false;
        }

    }

    public static class TestEnvironment
        extends ExpressionEnvironment
    {

        @Override
        protected boolean logicalOperatorsAllowed()
        {
            return true;
        }


        @Override
        protected boolean comparatorsAllowed()
        {
            return true;
        }


        @Override
        protected boolean complexLiteralsAllowed()
        {
            return true;
        }


        @Override
        protected boolean arithmeticOperatorsAllowed()
        {
            return true;
        }


        @Override
        public Object resolveIdentifier(String name)
        {
            if (name.equals("num"))
            {
                return 123;
            }
            else if (name.equals("value"))
            {
                throw new IllegalStateException("Should be shadowed by de.quinscape.exceed.runtime.expression.ExpressionEnvTest.TestOperations.value");
            }
            return super.resolveIdentifier(name);
        }


        public String getValue()
        {
            return "xyz";
        }
    }


    private static class Foo
    {
        private String name;


        public String getName()
        {
            return name;
        }


        public void setName(String name)
        {
            this.name = name;
        }
    }

}
