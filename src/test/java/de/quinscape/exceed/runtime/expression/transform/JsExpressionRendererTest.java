package de.quinscape.exceed.runtime.expression.transform;

import de.quinscape.exceed.expression.ExpressionParser;
import de.quinscape.exceed.model.ApplicationModel;
import de.quinscape.exceed.model.meta.PropertyType;
import de.quinscape.exceed.runtime.TestApplication;
import de.quinscape.exceed.runtime.TestApplicationBuilder;
import de.quinscape.exceed.runtime.js.ExpressionType;
import de.quinscape.exceed.runtime.js.JsExpressionRenderer;
import de.quinscape.exceed.runtime.js.ScriptBuffer;
import de.quinscape.exceed.runtime.js.def.Definition;
import de.quinscape.exceed.runtime.js.def.DefinitionType;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

public class JsExpressionRendererTest
{
    private final static Logger log = LoggerFactory.getLogger(JsExpressionRendererTest.class);

    private ScriptBuffer namespace = new ScriptBuffer();

    private TestApplication app = new TestApplicationBuilder()
        .withDefinitions(Definition.builder()

            .function("test")
                .withType(DefinitionType.BUILTIN)
                .withReturnType(PropertyType.INTEGER)

            .andFunction("fn")
                .withType(DefinitionType.BUILTIN)
                .withReturnType(PropertyType.INTEGER)
            
            .build()
        )
        .build();


    @Test
    public void thatNeutralWorks() throws Exception
    {
        JsExpressionRenderer neutral = new JsExpressionRenderer(Collections.emptyList());
        final ApplicationModel applicationModel = app.getApplicationModel();
        final String out = neutral.transform(applicationModel, ExpressionType.VALUE, null,ExpressionParser.parse("test(1+2)"), Collections.emptyList(),


            namespace);
        assertThat(out, is("test(1 + 2)"));
    }

    @Test
    public void thatTransformWorks() throws Exception
    {
        final ApplicationModel applicationModel = app.getApplicationModel();
        JsExpressionRenderer neutral = new JsExpressionRenderer(Arrays.asList(
            new TestTransform()
        ));
        final String extra = "superArg" + Math.round(Math.random() * 100);
        final String out = neutral.transform(applicationModel, ExpressionType.VALUE, null, ExpressionParser.parse("test(1+fn(2*3))"), Arrays.asList(
            new PlusTransform(extra)
        ), namespace);

        assertThat(out, is("_v.test({ value: add('" + extra + "', 1, fn(2 * 3))})"));
    }

    @Test
    public void thatDefaultRenderingWorks() throws Exception
    {
        final ApplicationModel applicationModel = app.getApplicationModel();
        JsExpressionRenderer neutral = new JsExpressionRenderer(Arrays.asList(
            new SpecialCaseTransformer(),
            new NumberWrapper()
        ));

        final String out = neutral.transform(applicationModel, ExpressionType.VALUE, null,ExpressionParser.parse("1 + 2"), namespace);

        assertThat(out, is("number1() + num(2)"));
    }


    @Test
    public void thatCodePushingWorks() throws Exception
    {
        JsExpressionRenderer neutral = new JsExpressionRenderer(Arrays.asList(
            new PushingTransformer()
        ));

        final ScriptBuffer namespace = new ScriptBuffer();

        namespace.getIdentifier("constant");

        final String out = neutral.transform(
            null, ExpressionType.VALUE,
            null,
            ExpressionParser.parse("pushTrigger"),
            namespace
        );

        assertThat(out, is("pushTrigger(constant2)"));

        assertThat(namespace.getPushed().get("'PUSHED'"), is("constant2"));

    }
}
