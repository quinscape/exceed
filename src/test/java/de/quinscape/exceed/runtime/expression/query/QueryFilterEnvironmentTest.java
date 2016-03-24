package de.quinscape.exceed.runtime.expression.query;

import com.google.common.collect.ImmutableSet;
import de.quinscape.exceed.expression.ExpressionParser;
import de.quinscape.exceed.expression.ParseException;
import de.quinscape.exceed.runtime.expression.ExpressionService;
import de.quinscape.exceed.runtime.expression.ExpressionServiceImpl;
import org.jooq.Condition;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.*;


public class QueryFilterEnvironmentTest
{
    private static Logger log = LoggerFactory.getLogger(QueryFilterEnvironmentTest.class);

    private ExpressionService expressionService = new ExpressionServiceImpl(ImmutableSet.of(new QueryFilterOperations()));

    @Test
    public void testFilter() throws Exception
    {
        Condition c = transform("Foo.barId == Bar.id");

        assertThat(c,is(notNullValue()));

    }


    private Condition transform(String expr) throws ParseException
    {
        return (Condition) expressionService.evaluate(ExpressionParser.parse(expr), new QueryFilterEnvironment(null, null));
    }
}
