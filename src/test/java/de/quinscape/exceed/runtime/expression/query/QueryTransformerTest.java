package de.quinscape.exceed.runtime.expression.query;

import com.google.common.collect.ImmutableSet;
import de.quinscape.exceed.TestDomainServiceBase;
import de.quinscape.exceed.expression.ParseException;
import de.quinscape.exceed.model.domain.property.DomainProperty;
import de.quinscape.exceed.model.domain.type.DomainType;
import de.quinscape.exceed.model.domain.type.DomainTypeModel;
import de.quinscape.exceed.model.expression.ExpressionValue;
import de.quinscape.exceed.model.meta.PropertyType;
import de.quinscape.exceed.model.view.View;
import de.quinscape.exceed.runtime.TestApplication;
import de.quinscape.exceed.runtime.TestApplicationBuilder;
import de.quinscape.exceed.runtime.expression.ExpressionService;
import de.quinscape.exceed.runtime.expression.ExpressionServiceImpl;
import org.jooq.Condition;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

public class QueryTransformerTest
{
    private final static Logger log = LoggerFactory.getLogger(QueryTransformerTest.class);

    private QueryTransformerOperations queryTransformerOperations = new QueryTransformerOperations();

    private ExpressionService expressionService = new ExpressionServiceImpl(ImmutableSet.of
        (queryTransformerOperations));

    private ComponentQueryTransformer transformer = new ComponentQueryTransformer(expressionService);

    private View view = new View("test");

    {
        queryTransformerOperations.setExpressionService(expressionService);
    }


    @Test
    public void testTypeDefinition() throws Exception
    {
        QueryDefinition def = transform("Foo.as(f).join(Bar).on(f.bar_id == Bar.id).query()");

        assertThat(def, is(notNullValue()));
        QueryDomainType q1 = def.getQueryDomainType();
        assertThat(q1, is(notNullValue()));
        assertThat(q1.getType().getName(), is("Foo"));
        assertThat(q1.getNameOrAlias(), is("f"));

        JoinDefinition j = q1.getJoinedType();
        assertThat(j, is(notNullValue()));
        QueryDomainType q2 = j.getRight();
        assertThat(q2.getType().getName(), is("Bar"));
        assertThat(q2.getNameOrAlias(), is("Bar"));

    }


    @Test
    public void testQuery() throws Exception
    {
        QueryDefinition def = transform("query(Foo.as('f').join(Bar).on(f.bar_id == Bar.id)).filter(f.type == 23)" +
            ".orderBy(f.name).limit(55).offset(12)");

        assertThat(def.getOrderBy(), is(Collections.singletonList("f.name")));
        assertThat(def.getLimit(), is(55));
        assertThat(def.getOffset(), is(12));

        Condition filter = def.getFilter().getCondition();
        assertThat(filter, is(notNullValue()));
    }


    @Test
    public void testMultiOrder() throws Exception
    {
        QueryDefinition def = transform("query(Foo.as('f')).orderBy(f.name,f.foo)");

        assertThat(def.getOrderBy(), is(Arrays.asList("f.name", "f.foo")));
    }


    @Test
    public void testMultiJoin() throws Exception
    {
        QueryDefinition def = transform("Foo.join(Bar.join(Baz).on(Bar.id == Baz.id)).on(Foo.id == Bar.id)");

        assertThat(def.getQueryDomainType().getType().getName(), is("Foo"));
        assertThat(def.getQueryDomainType().getJoinedType().getRight().getType().getName(), is("Bar"));
        assertThat(def.getQueryDomainType().getJoinedType().getRight().getJoinedType().getRight().getType().getName()
            , is("Baz"));


        List<String> list = def.getQueryDomainType().getFieldsInOrder().stream().map(DataField::getLocalName).collect
            (Collectors.toList());
        // fields in definition order
        assertThat(list, is(Arrays.asList("Foo.id", "Foo.value","Foo.type", "Foo.foo", "Foo.bar_id", "Bar.id", "Bar.value", "Bar.bar", "Baz.id", "Baz.value", "Baz.baz")));

        log.info("{}", list);
    }


    @Test
    public void testFields() throws Exception
    {
        QueryDefinition def = transform("query(Foo.join(Bar.join(Baz).on(Bar.id == Baz.id)).on(Foo.id == Bar.id).fields('foo','baz','bar'))");
        //def.getQueryDomainType().selectedFields(Arrays.asList("foo", "baz", "bar"));

        List<String> list = def.getQueryDomainType().getFieldsInOrder().stream().map(DataField::getLocalName).collect
            (Collectors.toList());
        // local fields in definition order
        assertThat(list, is(Arrays.asList("foo", "baz", "bar")));
    }


    private TestApplication app = new TestApplicationBuilder().withDomainService(new TestDomainService()).withView(view).build();
    private QueryDefinition transform(String query) throws ParseException
    {
        return (QueryDefinition) transformer.transform(
            app.createRuntimeContext(view),
            new QueryContext(
                view, null,
                null,
                null,
                null
            ),
            ExpressionValue.forValue(
                query,
                true
            ).getAstExpression()
        );
    }


    private class TestDomainService
        extends TestDomainServiceBase
    {

        public TestDomainService()
        {
            domainTypes.put("Foo", createDomainType("Foo", "Bar"));
            domainTypes.put("Bar", createDomainType("Bar"));
            domainTypes.put("Baz", createDomainType("Baz"));
        }
        public DomainType createDomainType(String name, String... joined)
        {
            DomainTypeModel domainType = new DomainTypeModel();
            domainType.setName(name);
            domainType.setAnnotation("Test domain type " + name);

            final List<DomainProperty> list = new ArrayList<>();

            list.add(
                DomainProperty.builder()
                    .withName("id")
                    .withType(PropertyType.UUID)
                    .build()
            );
            list.add(
                DomainProperty.builder()
                .withName("value")
                .withType(PropertyType.PLAIN_TEXT)
                    .build()
            );

            if (name.equals("Foo"))
            {
                list.add(
                    DomainProperty.builder()
                        .withName("type")
                        .withType(PropertyType.INTEGER)
                        .build()
                );
            }
            list.add(
                DomainProperty.builder()
                    .withName(name.toLowerCase())
                    .withType(PropertyType.PLAIN_TEXT)
                    .build()
            );

            if (joined != null)
            {
                for (String joinedTable : joined)
                {
                    list.add(
                        DomainProperty.builder()
                            .withName(joinedTable.toLowerCase() + "_id")
                            .withType(PropertyType.UUID)
                            .withForeignKey(joinedTable)
                            .build()
                    );

                }
            }

            domainType.setProperties(list);
            return domainType;
        }

        @Override
        public Map<String, DomainType> getDomainTypes()
        {
            return domainTypes;
        }
    }
}
