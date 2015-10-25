package de.quinscape.exceed.runtime.domain;

import de.quinscape.exceed.runtime.domain.DefaultNamingStrategy;
import org.junit.Test;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.*;

public class DefaultNamingStrategyTest
{

    @Test
    public void testCamelCaseToUnderline() throws Exception
    {
        assertThat(DefaultNamingStrategy.camelCaseToUnderline("Foo"), is("foo"));
        assertThat(DefaultNamingStrategy.camelCaseToUnderline("AppUser"), is("app_user"));
        assertThat(DefaultNamingStrategy.camelCaseToUnderline("ResourceURL"), is("resource_url"));
    }
}
