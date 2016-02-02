package de.quinscape.exceed.runtime.model;

import com.google.common.collect.ImmutableMap;
import de.quinscape.exceed.component.ComponentDescriptor;
import de.quinscape.exceed.expression.ASTExpression;
import de.quinscape.exceed.expression.ExpressionParser;
import de.quinscape.exceed.expression.ParseException;
import de.quinscape.exceed.model.view.Attributes;
import de.quinscape.exceed.model.view.ComponentModel;
import de.quinscape.exceed.runtime.service.ComponentRegistration;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.*;

public class ClientExpressionRendererTest
{
    private static Logger log = LoggerFactory.getLogger(ClientExpressionRendererTest.class);

    private ComponentModel componentModel;

    private ComponentPath path;


    @Before
    public void setupComponent() throws ParseException
    {
        ComponentDescriptor componentDescriptor = new ComponentDescriptor(
            ImmutableMap.of("myVar", "myVarValue"),
            null,
            null,
            null,
            null,
            null,
            null,
            false,
            false
        );

        componentModel = new ComponentModel();
        componentModel.setName("TestComponent");
        componentModel.setComponentRegistration(new ComponentRegistration("TestComponent", componentDescriptor, "",
            null));


        Map<String, Object> m = new HashMap<>();
        m.put("id", "testComponentId");
        m.put("intProp", "{ 123 }");
        m.put("floatProp", "{ 123.3 }");
        m.put("boolProp", "{ true }");
        m.put("strProp", "{ 'foobar' }");
        m.put("longStrProp",
            // String longer than current inlining limit
            "'123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123'");

        componentModel.setAttrs(new Attributes(m));

        path = new ComponentPath().firstChildPath();
        path.increment();
        path.increment();
        path = path.firstChildPath();
        path.increment();


    }


    @Test
    public void testModelIdentifierTransformation() throws Exception
    {
        assertThat(transform("model"), is("_v.root.kids[2].kids[1]"));
    }

    @Test
    public void testPropsTransformation() throws Exception
    {
        assertThat(transform("props.longStrProp"), is("_v.root.kids[2].kids[1].attrs.longStrProp"));
        assertThat(transform("props['longStrProp']"), is("_v.root.kids[2].kids[1].attrs[\"longStrProp\"]"));
    }


    @Test
    public void testVarsAccessTransformation() throws Exception
    {
        assertThat(transform("vars.myVar"), is("_v.data[\"testComponentId\"].vars.myVar"));
        assertThat(transform("vars['myVar']"), is("_v.data[\"testComponentId\"].vars[\"myVar\"]"));
    }


    @Test
    public void testInlining() throws Exception
    {

        assertThat(transform("props.intProp"), is("123"));
        assertThat(transform("props.floatProp"), is("123.3"));
        assertThat(transform("props.boolProp"), is("true"));
        assertThat(transform("props.strProp"), is("\"foobar\""));

    }


    @Test(expected = InvalidClientExpressionException.class)
    public void testVarsError() throws Exception
    {
        transform("vars.nonExistingVar");
    }

    @Test(expected = InvalidClientExpressionException.class)
    public void testVarsError2() throws Exception
    {
        transform("vars['nonExistingVar']");
    }


    private String transform(String expr) throws ParseException
    {
        ClientExpressionRenderer renderer = new ClientExpressionRenderer(componentModel, path);
        ASTExpression expression = ExpressionParser.parse(expr);
        expression.childrenAccept(renderer, null);
        return renderer.getOutput();
    }
}
