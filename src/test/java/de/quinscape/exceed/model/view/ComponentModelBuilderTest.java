package de.quinscape.exceed.model.view;

import org.junit.Test;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.*;

import static de.quinscape.exceed.model.view.ComponentModelBuilder.component;

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
}
