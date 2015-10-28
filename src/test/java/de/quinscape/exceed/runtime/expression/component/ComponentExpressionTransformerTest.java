package de.quinscape.exceed.runtime.expression.component;

import com.google.common.collect.ImmutableSet;
import de.quinscape.exceed.model.action.ActionModel;
import de.quinscape.exceed.runtime.action.Action;
import de.quinscape.exceed.runtime.controller.ActionNotFoundException;
import de.quinscape.exceed.runtime.controller.ActionRegistry;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.*;

public class ComponentExpressionTransformerTest
{
    private static Logger log = LoggerFactory.getLogger(ComponentExpressionTransformerTest.class);


    private ComponentExpressionTransformer env = new ComponentExpressionTransformer(true, new TestRegistry());

    @Test
    public void testComponentExpressions() throws Exception
    {
        assertThat(env.transformExpression("foo().bar({arg: 'xxx\\''})"), is("[{'action':'foo'},{'action':'bar','arg':'xxx\\''}]"));
    }


    @Test
    public void testInt() throws Exception
    {
        assertThat(env.transformExpression("foo({arg: 12})"), is("{'action':'foo','arg':12}"));
    }

    @Test
    public void testFloat() throws Exception
    {
        assertThat(env.transformExpression("foo({arg: 0.25})"), is("{'action':'foo','arg':0.25}"));
    }

    @Test
    public void testBool() throws Exception
    {
        assertThat(env.transformExpression("foo({arg: true})"), is("{'action':'foo','arg':true}"));
    }

    @Test
    public void testNull() throws Exception
    {
        assertThat(env.transformExpression("foo({arg: null})"), is("{'action':'foo','arg':null}"));
    }

    @Test
    public void testProps() throws Exception
    {
        assertThat(env.transformExpression("foo({arg: prop('xxx')})"), is("{'action':'foo','arg':props['xxx']}"));
    }

    @Test
    public void testVars() throws Exception
    {
        assertThat(env.transformExpression("foo({arg: var('xxx')})"), is("{'action':'foo','arg':vars['xxx']}"));
    }

    @Test
    public void test3Chain() throws Exception
    {
        assertThat(env.transformExpression("foo().bar().baz()"), is("[{'action':'foo'},{'action':'bar'},{'action':'baz'}]"));
    }

    @Test(expected = ActionNotFoundException.class)
    public void testUnknownAction() throws Exception
    {
        env.transformExpression("qux()");
    }


    private class TestRegistry
        implements ActionRegistry
    {
        @Override
        public ActionModel resolve(String actionJSON)
        {
            throw new UnsupportedOperationException("Not supported in test");
        }


        @Override
        public Set<String> getActionNames()
        {
            return ImmutableSet.of("foo",  "bar", "baz");
        }

        @Override
        public Action getAction(String name)
        {
            throw new UnsupportedOperationException("Not supported in test");
        }
    }
}
