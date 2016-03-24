package de.quinscape.exceed.runtime.expression.query;

import com.google.common.collect.ImmutableSet;
import de.quinscape.exceed.expression.ParseException;
import de.quinscape.exceed.model.domain.DomainProperty;
import de.quinscape.exceed.model.domain.DomainType;
import de.quinscape.exceed.model.domain.EnumType;
import de.quinscape.exceed.runtime.TestApplication;
import de.quinscape.exceed.runtime.application.RuntimeApplication;
import de.quinscape.exceed.runtime.domain.DefaultNamingStrategy;
import de.quinscape.exceed.runtime.domain.DomainObject;
import de.quinscape.exceed.runtime.domain.DomainService;
import de.quinscape.exceed.runtime.domain.GenericDomainObject;
import de.quinscape.exceed.runtime.domain.NamingStrategy;
import de.quinscape.exceed.runtime.expression.ExpressionService;
import de.quinscape.exceed.runtime.expression.ExpressionServiceImpl;
import org.jooq.Condition;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.*;

public class QueryTransformerTest
{
    private final static Logger log = LoggerFactory.getLogger(QueryTransformerTest.class);

    private QueryTransformerOperations  queryTransformerOperations = new QueryTransformerOperations();
    private ExpressionService expressionService = new ExpressionServiceImpl(ImmutableSet.of(queryTransformerOperations));
    private QueryTransformer transformer = new QueryTransformer(expressionService, new DefaultNamingStrategy());
    {
        queryTransformerOperations.setExpressionService(expressionService);
    }

    @Test
    public void testTypeDefinition() throws Exception
    {
        QueryDefinition def = transform("Foo.as(f).join(Bar).on(f.bar_id == Bar.id).query()");

        assertThat(def, is(notNullValue()));
        QueryDomainType q1 = def.getQueryDomainType();
        assertThat( q1, is(notNullValue()));
        assertThat(q1.getType().getName(), is("Foo"));
        assertThat(q1.getAlias(), is("f"));

        JoinDefinition j = q1.getJoinedType();
        assertThat(j, is(notNullValue()));
        QueryDomainType q2 = j.getRight();
        assertThat(q2.getType().getName(), is("Bar"));
        assertThat(q2.getAlias(), is("Bar"));

    }

    @Test
    public void testQuery() throws Exception
    {
        QueryDefinition def = transform("query(Foo.as('f').join(Bar).on(f.bar_id == Bar.id)).filter(f.type == 23).orderBy(f.name).limit(55).offset(12)");

        assertThat(def.getOrderBy(), is(Collections.singletonList("f.name")));
        assertThat(def.getLimit(), is(55));
        assertThat(def.getOffset(), is(12));

        Condition filter = def.getFilter();
        assertThat(filter, is(notNullValue()));
    }


    @Test
    public void testMultiJoin() throws Exception
    {
        QueryDefinition def = transform("Foo.join(Bar.join(Baz).on(Bar.id == Baz.id)).on(Foo.id == Bar.id)");

        assertThat(def.getQueryDomainType().getType().getName(), is("Foo"));
        assertThat(def.getQueryDomainType().getJoinedType().getRight().getType().getName(), is("Bar"));
        assertThat(def.getQueryDomainType().getJoinedType().getRight().getJoinedType().getRight().getType().getName()
            , is("Baz"));


        List<String> list = def.getQueryDomainType().getFieldsInOrder().stream().map(DataField::getLocalName).collect(Collectors.toList());
        // fields in definition order
        assertThat(list, is(Arrays.asList("Foo.value", "Foo.foo", "Bar.value", "Bar.bar", "Baz.value", "Baz.baz")));

        log.info("{}", list);
    }

    @Test
    public void testFields() throws Exception
    {
        QueryDefinition def = transform("Foo.join(Bar.join(Baz).on(Bar.id == Baz.id)).on(Foo.id == Bar.id)");
        def.getQueryDomainType().setFields(Arrays.asList("foo", "baz", "bar"));

        List<String> list = def.getQueryDomainType().getFieldsInOrder().stream().map(DataField::getLocalName).collect(Collectors.toList());
        // local fields in definition order
        assertThat(list, is(Arrays.asList("foo", "baz", "bar")));
    }


    private QueryDefinition transform(String query) throws ParseException
    {
        TestDomainService domainService = new TestDomainService();
        return transformer.transform(new TestApplication(null, new TestDomainService()).createRuntimeContext(), query, null, null);
    }


    private class TestDomainService
        implements DomainService
    {
        @Override
        public void init(RuntimeApplication runtimeApplication, String schema)
        {

        }


        @Override
        public String toJSON(Object domainObject)
        {
            return null;
        }


        @Override
        public Object toDomainObject(Class<?> actionClass, String json)
        {
            return null;
        }


        @Override
        public DomainType getDomainType(String name)
        {
            DomainType domainType = new DomainType();
            domainType.setName(name);
            domainType.setAnnotation("Test domain type " + name);

            domainType.setProperties(Arrays.asList(
                new DomainProperty("value", "PlainText", null, false),
                new DomainProperty(name.toLowerCase(), "PlainText", null, false)
            ));
            return domainType;
        }


        @Override
        public String getSchema()
        {
            return "test";
        }


        @Override
        public Set<String> getDomainTypeNames()
        {
            return ImmutableSet.of("Foo", "Bar", "Baz");
        }


        @Override
        public Map<String, EnumType> getEnums()
        {
            return Collections.emptyMap();
        }


        @Override
        public GenericDomainObject read(String type, Object... pkFields)
        {
            return null;
        }


        @Override
        public void delete(DomainObject genericDomainObject)
        {

        }

        @Override
        public void insert(DomainObject genericDomainObject)
        {

        }


        @Override
        public void update(DomainObject genericDomainObject)
        {

        }

        @Override
        public NamingStrategy getNamingStrategy()
        {
            return null;
        }
    }
}
