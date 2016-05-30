package de.quinscape.exceed.runtime.domain;

import de.quinscape.exceed.domain.tables.pojos.AppUser;
import org.junit.Ignore;
import org.junit.Test;
import org.svenson.ClassNameBasedTypeMapper;
import org.svenson.JSONParser;
import org.svenson.matcher.SubtypeMatcher;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.*;

public class DomainObjectTest
{
    @Test
    @Ignore
    public void testJSONParsing() throws Exception
    {
        JSONParser parser = new JSONParser();
        ClassNameBasedTypeMapper typeMapper = new ClassNameBasedTypeMapper();
        typeMapper.setBasePackage(AppUser.class.getPackage().getName());
        typeMapper.setEnforcedBaseType(DomainObjectBase.class);
        typeMapper.setDiscriminatorField("_type");
        typeMapper.setPathMatcher(new SubtypeMatcher(DomainObjectBase.class));
        parser.setTypeMapper(typeMapper);

        AppUser foo = (AppUser)parser.parse(DomainObjectBase.class, "{ \"_type\" : \"AppUser\", \"login\" : " +
            "\"Foolicious\", \"password\" : \"pw123\"}");

        assertThat(foo, is(notNullValue()));
        assertThat(foo.getDomainType(), is("AppUser"));
        assertThat(foo.getLogin(), is("Foolicious"));
        assertThat(foo.getPassword(), is("pw123"));


    }
}
