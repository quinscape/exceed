package de.quinscape.exceed.runtime.domain;

import de.quinscape.exceed.domain.tables.pojos.AppUser;
import org.junit.Test;
import org.svenson.ClassNameBasedTypeMapper;
import org.svenson.JSONParser;
import org.svenson.matcher.SubtypeMatcher;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.*;

public class DomainObjectTest
{
    @Test
    public void testJSONParsing() throws Exception
    {
        JSONParser parser = new JSONParser();
        ClassNameBasedTypeMapper typeMapper = new ClassNameBasedTypeMapper();
        typeMapper.setBasePackage(AppUser.class.getPackage().getName());
        typeMapper.setEnforcedBaseType(DomainObject.class);
        typeMapper.setDiscriminatorField("_type");
        typeMapper.setPathMatcher(new SubtypeMatcher(DomainObject.class));
        parser.setTypeMapper(typeMapper);

        AppUser foo = (AppUser)parser.parse(DomainObject.class, "{ \"_type\" : \"AppUser\", \"login\" : " +
            "\"Foolicious\", \"password\" : \"pw123\"}");

        assertThat(foo, is(notNullValue()));
        assertThat(foo.getType(), is("AppUser"));
        assertThat(foo.getLogin(), is("Foolicious"));
        assertThat(foo.getPassword(), is("pw123"));


    }
}
