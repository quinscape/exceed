package de.quinscape.exceed.model.view;

import org.junit.Test;

import java.util.Collections;
import java.util.List;

import static de.quinscape.exceed.model.view.ComponentModelBuilder.*;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

public class ComponentModelBuilderTest
{
    @Test
    public void testBuilding() throws Exception
    {

        {
            ComponentModel component = component("Foo").withAttribute("bar", "1").getComponent();
            assertThat(component, is(notNullValue()));
            assertThat(component.getName(), is("Foo"));
            assertThat(component.getAttribute("bar").getValue(), is("1"));
        }

        {
            ComponentModel component =

                component("Foo")
                    .withKids(
                        component("Qux")
                        .withAttribute("value", "1")
                ).getComponent();

            assertThat(component, is(notNullValue()));
            assertThat(component.getName(), is("Foo"));
            assertThat(component.getKids().get(0).getName(), is("Qux"));
            assertThat(component.getKids().get(0).getAttribute("value").getValue(), is("1"));
        }

    }


    @Test
    public void testWithList() throws Exception
    {
        {
            final List<ComponentModel> list = Collections.singletonList(
                component("Baz")
                    .withAttribute("value", "2")
                    .getComponent()
            );

            ComponentModel component =
                component("Foo")
                    .withKids(list)
                    .getComponent();

            assertThat(component, is(notNullValue()));
            assertThat(component.getName(), is("Foo"));
            assertThat(component.getKids().get(0).getName(), is("Baz"));
            assertThat(component.getKids().get(0).getAttribute("value").getValue(), is("2"));
        }
    }
}
