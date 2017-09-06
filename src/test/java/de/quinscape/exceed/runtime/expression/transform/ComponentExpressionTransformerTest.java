package de.quinscape.exceed.runtime.expression.transform;

import de.quinscape.exceed.expression.ASTExpression;
import de.quinscape.exceed.expression.ExpressionParser;
import de.quinscape.exceed.expression.ParseException;
import de.quinscape.exceed.model.component.ComponentDescriptor;
import de.quinscape.exceed.model.context.ContextModel;
import de.quinscape.exceed.model.context.ScopeDeclarations;
import de.quinscape.exceed.model.view.ComponentModel;
import de.quinscape.exceed.model.view.View;
import de.quinscape.exceed.runtime.component.ComponentInstanceRegistration;
import de.quinscape.exceed.runtime.component.ComponentRegistration;
import de.quinscape.exceed.runtime.js.ExpressionType;
import de.quinscape.exceed.runtime.js.ScriptBuffer;
import de.quinscape.exceed.runtime.js.def.Definitions;
import de.quinscape.exceed.runtime.model.ComponentRenderPath;
import de.quinscape.exceed.runtime.model.ExpressionModelContext;
import org.junit.Before;
import org.junit.Test;
import org.svenson.JSONParser;

import java.util.Collections;
import java.util.HashMap;

import static de.quinscape.exceed.model.view.ComponentModelBuilder.*;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

public class ComponentExpressionTransformerTest
{
    private ComponentModel componentModel;

    private ComponentRenderPath path;

    private View view;

    private ScriptBuffer namespace = new ScriptBuffer();


    @Before
    public void setupComponent() throws ParseException
    {
        ComponentDescriptor componentDescriptor = JSONParser.defaultJSONParser().parse(ComponentDescriptor.class, "{\n" +
            "    \"vars\" : { \"myVar\": \"myVarValue\"}\n" +
            "}");

        componentModel =
            component("TestComponent")
                .withAttribute(ComponentModel.ID_ATTRIBUTE, "testComponentId")
                .withAttribute("intProp", "{ 123 }")
                .withAttribute("floatProp", "{ 123.3 }")
                .withAttribute("boolProp", "{ true }")
                .withAttribute("strProp", "{ 'foobar' }")
                .withAttribute("longStrProp",
                    // String longer than current inlining limit
                    "'123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123'"
                )
                .getComponent();

        componentModel.setComponentRegistration(
            new ComponentInstanceRegistration(
                new ComponentRegistration(
                    "TestComponent",
                    componentDescriptor,
                    "",
                    null,
                    null,
                    null
                ),
                componentModel)
        );

        path = new ComponentRenderPath("testContent").firstChildPath("");
        path.increment("");
        path.increment("");
        path = path.firstChildPath("");
        path.increment("");

        view = new View("test");
        final HashMap<String, ComponentModel> content = new HashMap<>();
        content.put(View.ROOT, componentModel);
        view.setContent(content);
    }


    @Test
    public void testModelIdentifierTransformation() throws Exception
    {
        assertThat(transform("model"), is("_v.model([\"testContent\",2,1])"));
    }

    @Test
    public void testPropsTransformation() throws Exception
    {
        assertThat(transform("props.longStrProp"), is("_v.model([\"testContent\",2,1]).attrs.longStrProp"));
        assertThat(transform("props['longStrProp']"), is("_v.model([\"testContent\",2,1]).attrs['longStrProp']"));
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


// XXX: find better place to check this
//    @Test(expected = InvalidClientExpressionException.class)
//    public void testVarsError() throws Exception
//    {
//        transform("vars.nonExistingVar");
//    }
//
//    @Test(expected = InvalidClientExpressionException.class)
//    public void testVarsError2() throws Exception
//    {
//        transform("vars['nonExistingVar']");
//    }


    private String transform(String expr) throws ParseException
    {
        final ASTExpression expression = ExpressionParser.parse(expr);

        ScopeDeclarations decl = new ScopeDeclarations("testProcess/testView", new ContextModel[]{}, new Definitions());
        final ComponentExpressionTransformer transformer = new ComponentExpressionTransformer(componentModel, path, "test",


            path.findContextByType(null), null);

        final DefaultTransformationContext ctx = new DefaultTransformationContext(null, ExpressionType.VALUE, new ExpressionModelContext(view, componentModel), Collections.singletonList
                (transformer), namespace);
        ctx.applyRecursive(expression);
        return ctx.getOutput();
    }

}
