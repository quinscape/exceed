package de.quinscape.exceed.runtime.model;

import de.quinscape.exceed.component.ComponentDescriptor;
import de.quinscape.exceed.expression.ASTExpression;
import de.quinscape.exceed.expression.ExpressionParser;
import de.quinscape.exceed.expression.ParseException;
import de.quinscape.exceed.model.domain.DomainType;
import de.quinscape.exceed.model.view.ComponentModel;
import de.quinscape.exceed.runtime.service.ComponentRegistration;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.svenson.JSONParser;

import static de.quinscape.exceed.model.view.ComponentModelBuilder.*;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

public class ViewExpressionRendererTest
{
    private final static Logger log = LoggerFactory.getLogger(ViewExpressionRendererTest.class);

    private ComponentModel componentModel;

    private ComponentPath path;


    @Before
    public void setupComponent() throws ParseException
    {
        ComponentDescriptor componentDescriptor = JSONParser.defaultJSONParser().parse(ComponentDescriptor.class, "{\n" +
            "    \"vars\" : { \"myVar\": \"myVarValue\"}\n" +
            "}");

        componentModel =
            component("TestComponent")
                .withAttribute(DomainType.ID_PROPERTY, "testComponentId")
                .withAttribute("intProp", "{ 123 }")
                .withAttribute("floatProp", "{ 123.3 }")
                .withAttribute("boolProp", "{ true }")
                .withAttribute("strProp", "{ 'foobar' }")
                .withAttribute("longStrProp",
                    // String longer than current inlining limit
                    "'123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123'"
                )
            .getComponent();

        componentModel.setComponentRegistration(new ComponentRegistration("TestComponent", componentDescriptor, "",
            null, null));

        path = new ComponentPath().firstChildPath("");
        path.increment("");
        path.increment("");
        path = path.firstChildPath("");
        path.increment("");
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
        assertThat(transform("props['longStrProp']"), is("_v.root.kids[2].kids[1].attrs['longStrProp']"));
    }


    @Test
    public void testVarsAccessTransformation() throws Exception
    {
        assertThat(transform("vars.myVar"), is("_v.data['testComponentId'].vars.myVar"));
        assertThat(transform("vars['myVar']"), is("_v.data['testComponentId'].vars['myVar']"));
    }


    @Test
    public void testInlining() throws Exception
    {

        assertThat(transform("props.intProp"), is("123"));
        assertThat(transform("props.floatProp"), is("123.3"));
        assertThat(transform("props.boolProp"), is("true"));
        assertThat(transform("props.strProp"), is("'foobar'"));

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
        ActionExpressionBaseRenderer renderer = new ViewExpressionRenderer(null, null, componentModel, "test", path, null);
        ASTExpression expression = ExpressionParser.parse(expr);
        expression.childrenAccept(renderer, null);
        return renderer.getOutput();
    }
}
