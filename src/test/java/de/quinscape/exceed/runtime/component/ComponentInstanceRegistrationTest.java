package de.quinscape.exceed.runtime.component;

import de.quinscape.exceed.model.component.ComponentDescriptor;
import de.quinscape.exceed.model.component.ComponentPackageDescriptor;
import de.quinscape.exceed.model.expression.Attributes;
import de.quinscape.exceed.model.view.ComponentModel;
import de.quinscape.exceed.runtime.js.InvalidExpressionException;
import de.quinscape.exceed.runtime.model.ExpressionRenderer;
import org.junit.Test;
import org.svenson.JSONParser;
import org.svenson.tokenize.InputStreamSource;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

public class ComponentInstanceRegistrationTest
{
    private final static String COMPONENT_NAME = "Test";

    @Test
    public void testInlineProps() throws Exception
    {
        ComponentDescriptor descriptor = readDescriptor();

        // test inlining
        {
            ComponentModel componentModel = new ComponentModel();
            componentModel.setName(COMPONENT_NAME);
            final Attributes attrs = new Attributes();

            attrs.setAttribute("str", "abc");
            attrs.setAttribute("num", "{ 3 }");
            attrs.setAttribute("scope", "{ scope('myVar') }");

            componentModel.setAttrs(attrs);

            register(componentModel, descriptor);
            final ComponentInstanceRegistration registration = componentModel.getComponentRegistration();
            assertThat(var(registration, "val"), is("'non-prop-value'"));
            assertThat(var(registration, "val2"), is("5"));
            assertThat(var(registration, "val3"), is("true"));
            assertThat(var(registration, "num"), is("3"));
            assertThat(var(registration, "str"), is("'abc'"));
            assertThat(var(registration, "scope"), is("scope('myVar')"));
            assertThat(var(registration, "scope2"), is("scope('myVar').name"));
        }


        // test inlining with props default
        {
            final Attributes attrs = new Attributes();

            // not setting this creates an error (see test below)
            attrs.setAttribute("scope", "{ scope('myVar') }");

            ComponentModel componentModel = new ComponentModel();
            componentModel.setName(COMPONENT_NAME);
            componentModel.setAttrs(attrs);
            register(componentModel, descriptor);

            final ComponentInstanceRegistration registration = componentModel.getComponentRegistration();
            assertThat(var(registration, "val"), is("'non-prop-value'"));
            assertThat(var(registration, "val"), is("'non-prop-value'"));
            assertThat(var(registration, "val2"), is("5"));
            assertThat(var(registration, "val3"), is("true"));
            assertThat(var(registration, "num"), is("5"));
            assertThat(var(registration, "str"), is("'def'"));
        }
    }


    @Test(expected = InvalidExpressionException.class)
    public void testError() throws Exception
    {
        ComponentDescriptor descriptor = readDescriptor();

        {
            ComponentModel componentModel = new ComponentModel();
            componentModel.setName(COMPONENT_NAME);
            register(componentModel, descriptor);
        }
    }


    private String var(ComponentInstanceRegistration registration, String name)
    {
        return ExpressionRenderer.render(registration.getVarExpressions().get(name));
    }


    private ComponentDescriptor readDescriptor() throws FileNotFoundException
    {
        final ComponentPackageDescriptor pkgDescriptor = JSONParser.defaultJSONParser().parse(
            ComponentPackageDescriptor.class,
            new InputStreamSource(
                new FileInputStream(
                    new File("./src/test/java/de/quinscape/exceed/runtime/component/components.json")
                ),
                true
            )
        );
        return pkgDescriptor.getComponents().get(COMPONENT_NAME);
    }

    private void register(ComponentModel componentModel, ComponentDescriptor descriptor)
    {
        final ComponentRegistration componentRegistration = new ComponentRegistration(
            componentModel.getName(),
            descriptor,
            null,
            null,
            "test/test.js",
            null
        );

        final ComponentInstanceRegistration registration = new ComponentInstanceRegistration(
            componentRegistration,
            componentModel
        );
        componentModel.setComponentRegistration(
            registration
        );
    }
}
