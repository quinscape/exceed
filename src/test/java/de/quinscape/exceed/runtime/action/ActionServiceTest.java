package de.quinscape.exceed.runtime.action;

import com.google.common.collect.ImmutableMap;
import de.quinscape.exceed.TestDomainServiceBase;
import de.quinscape.exceed.model.domain.property.DomainProperty;
import de.quinscape.exceed.model.domain.type.DomainType;
import de.quinscape.exceed.model.domain.type.DomainTypeModel;
import de.quinscape.exceed.model.meta.PropertyType;
import de.quinscape.exceed.model.view.View;
import de.quinscape.exceed.runtime.RuntimeContext;
import de.quinscape.exceed.runtime.TestApplication;
import de.quinscape.exceed.runtime.TestApplicationBuilder;
import de.quinscape.exceed.runtime.action.param.ContextPropertyValueProviderFactory;
import de.quinscape.exceed.runtime.action.param.RuntimeContextProviderFactory;
import de.quinscape.exceed.runtime.controller.ActionNotFoundException;
import de.quinscape.exceed.runtime.domain.DomainObject;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Date;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

/**
 * @see TestActions
 */
public class ActionServiceTest
{
    private final static Logger log = LoggerFactory.getLogger(ActionServiceTest.class);


    private static View testView = new View();
    static {
        testView.setName("TestView");
    }

    private final static TestApplication app = new TestApplicationBuilder()
        .withBaseProperties(true)
        .withDomainService(new TestDomainService())
        .withSessionContext(DomainProperty.builder().withName("sessionVar").withType("PlainText").withDefaultValue("'foobar'").buildScoped())
        .withSessionContext(DomainProperty.builder().withName("nullVar").withType("PlainText").buildScoped())
        .withView(testView)
        .build();

    private static TestActions target = new TestActions();

    private static final DefaultActionService svc = new DefaultActionService(
        Collections.singleton(target),
        Arrays.asList(
            new RuntimeContextProviderFactory(),
            new ContextPropertyValueProviderFactory()
        )
    );



    @Test
    public void test() throws Exception
    {
        final RuntimeContext runtimeContext = app.createRuntimeContext();

        assertThat(execute(runtimeContext, "method", 1L), is(true));
        assertThat(execute(runtimeContext, "method", 0L), is(false));

        assertThat(target.getLastArgs().getArg(0), is(runtimeContext));
    }

    @Test
    public void testPropertyConversion() throws Exception
    {
        final RuntimeContext runtimeContext = app.createRuntimeContext();
        execute(runtimeContext, "dateParam", "1970-01-02");
        assertThat(target.getLastArgs().getArg(1), is(new Date(TimeUnit.DAYS.toMillis(1))));

        assertThat((String) executeAndConvert(runtimeContext, "dateBack"), is("1970-01-03"));
    }


    @Test(expected = ActionNotFoundException.class)
    public void testActionNotFound() throws Exception
    {
        final RuntimeContext runtimeContext = app.createRuntimeContext();
        execute(runtimeContext, "fake");
    }


    @Test(expected = InvalidActionParameterException.class)
    public void testPropertyConversionFail() throws Exception
    {
        final RuntimeContext runtimeContext = app.createRuntimeContext();
        execute(runtimeContext, "dateParam", "aaa");
    }

    @Test(expected = InvalidActionParameterException.class)
    public void testPropertyConversionFail2() throws Exception
    {
        final RuntimeContext runtimeContext = app.createRuntimeContext();
        execute(runtimeContext, "method", "aaa");
    }

    @Test
    public void testDomainObjectConversion() throws Exception
    {
        final RuntimeContext runtimeContext = app.createRuntimeContext();
        final String id = UUID.randomUUID().toString();
        DomainObject domainObject = (DomainObject) execute(runtimeContext,"domainObjectParam", ImmutableMap.of(
            "_type" ,"Foo",
            "id" , id,
            "value" , "AAA",
            "foo" , "1970-01-01"
        ));

        assertThat(domainObject.getDomainType(), is("Foo"));
        assertThat(domainObject.getId(), is(id));
        assertThat(domainObject.getProperty("value"), is("AAA"));
        assertThat(domainObject.getProperty("foo"), is(new Date(TimeUnit.DAYS.toMillis(3))));

        assertThat(execute(runtimeContext,"domainObjectParam", new Object[] { null }), is(nullValue()));
        assertThat(execute(runtimeContext,"domainObjectParam", new Object[] { }), is(nullValue()));
    }

    @Test
    public void testDomainObjectConversionAndBack() throws Exception
    {
        final RuntimeContext runtimeContext = app.createRuntimeContext();
        final String id = UUID.randomUUID().toString();
        Map<String,Object> domainObject = (Map<String,Object>) executeAndConvert(runtimeContext,"domainObjectParam", ImmutableMap.of(
            "_type" ,"Foo",
            "id" , id,
            "value" , "AAA",
            "foo" , "1970-01-01"
        ));

        assertThat(domainObject.get("_type"), is("Foo"));
        assertThat(domainObject.get("id"), is(id));
        assertThat(domainObject.get("value"), is("AAA"));
        assertThat(domainObject.get("foo"), is("1970-01-04"));

    }


    @Test
    public void testModeled() throws Exception
    {
        final RuntimeContext runtimeContext = app.createRuntimeContext();
        final String id = UUID.randomUUID().toString();
        final Bar bar = (Bar) execute(runtimeContext, "modeledDomain", ImmutableMap.of(
            "_type", "Bar",
            "id", id,
            "value", "AAA",
            "bar", "1970-01-02"
        ));
        assertThat(bar.getDomainService(), is(notNullValue()));
        assertThat(bar.getDomainType(), is("Bar"));
        assertThat(bar.getId(), is(id));
        assertThat(bar.getValue(), is("AAA"));
        assertThat(bar.getBar(), is(new Date(TimeUnit.DAYS.toMillis(1))));
    }

    @Test
    public void testModeledAndBack() throws Exception
    {
        final RuntimeContext runtimeContext = app.createRuntimeContext();
        final String id = UUID.randomUUID().toString();
        Map<String,Object> bar = (Map<String,Object>) executeAndConvert(runtimeContext, "modeledDomain", ImmutableMap.of(
            "_type", "Bar",
            "id", id,
            "value", "AAA",
            "bar", "1970-01-02"
        ));
        assertThat(bar.get("_type"), is("Bar"));
        assertThat(bar.get("id"), is(id));
        assertThat(bar.get("value"), is("AAA"));
        assertThat(bar.get("bar"), is("1970-01-02"));

    }


    @Test
    public void testRequired() throws Exception
    {
        final RuntimeContext runtimeContext = app.createRuntimeContext();
        assertThat(execute(runtimeContext, "requiredParam", "abc123"), is("abc123"));
    }

    @Test
    public void testContext() throws Exception
    {
        final RuntimeContext runtimeContext = app.createRuntimeContext(testView);
        assertThat(execute(runtimeContext, "scopedValue"), is("foobar"));
    }

    @Test
    public void testVoidReturn() throws Exception
    {
        final RuntimeContext runtimeContext = app.createRuntimeContext(testView);
        assertThat(svc.getRegistrations().get("voidMethod").getReturnType().getType(), is("Void"));
        assertThat(executeAndConvert(runtimeContext, "voidMethod"), is(nullValue()));
    }


    @Test
    public void testVarArgs() throws Exception
    {
        final RuntimeContext runtimeContext = app.createRuntimeContext(testView);

        execute(runtimeContext, "varArgsMethod", "abc", 2, false);

        assertThat(target.getLastArgs().getArg(0), is("abc"));
        assertThat(target.getLastArgs().getArg(1), is(2));
        assertThat(target.getLastArgs().getArg(2), is(false));

        execute(runtimeContext, "varArgsMethod2", "abc", 2, false);

        assertThat(target.getLastArgs().getArg(0), is("abc"));
        assertThat(target.getLastArgs().getArg(1), is(new Object[]{ 2,false}));
    }


    @Test(expected = InvalidActionParameterException.class)
    public void testRequiredMissing() throws Exception
    {
        final RuntimeContext runtimeContext = app.createRuntimeContext();
        execute(runtimeContext, "requiredParam", new Object[] { });
    }

    @Test(expected = InvalidActionParameterException.class)
    public void testRequiredMissingForProvider() throws Exception
    {
        final RuntimeContext runtimeContext = app.createRuntimeContext(testView);
        execute(runtimeContext, "requiredScopedValue", new Object[] { });
    }



    private Object execute(RuntimeContext runtimeContext, String method, Object... args)
    {
        // we don't actually test the JSON parsing of args since it is just the standard svenson parsing
        return svc.execute(
            runtimeContext,
            method,
            new JSONParameters(Arrays.asList(args))
        ).get();
    }

    private Object executeAndConvert(RuntimeContext runtimeContext, String method, Object... args)
    {
        return svc.execute(
            runtimeContext,
            method,
            new JSONParameters(Arrays.asList(args))
        ).toJSON();
    }

    private static class TestDomainService
        extends TestDomainServiceBase
    {
        private Map<String, DomainType> domainTypes = new HashMap<>();


        {
            domainTypes.put("Foo", createDomainType("Foo"));
            domainTypes.put("Bar", createDomainType("Bar"));
            domainTypes.put("Baz", createDomainType("Baz"));
        }


        public DomainType createDomainType(String name)
        {
            DomainTypeModel domainType = new DomainTypeModel();
            domainType.setName(name);
            domainType.setAnnotation("Test domain type " + name);
            domainType.setStorageConfiguration("testStorage");

            domainType.setProperties(Arrays.asList(
                DomainProperty.builder().withName("id").withType(PropertyType.UUID).build(),
                DomainProperty.builder().withName("value").withType(PropertyType.PLAIN_TEXT).build(),
                DomainProperty.builder().withName(name.toLowerCase()).withType(PropertyType.DATE).build()
            ));
            return domainType;
        }


        @Override
        public DomainType getDomainType(String name)
        {
            return domainTypes.get(name);
        }


        @Override
        public Map<String, DomainType> getDomainTypes()
        {
            return domainTypes;
        }
    }

}
