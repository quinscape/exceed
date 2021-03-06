package de.quinscape.exceed.runtime.expression.transform;

import de.quinscape.exceed.TestDomainServiceBase;
import de.quinscape.exceed.expression.ASTExpression;
import de.quinscape.exceed.expression.ExpressionParser;
import de.quinscape.exceed.expression.ParseException;
import de.quinscape.exceed.model.domain.property.DomainProperty;
import de.quinscape.exceed.model.domain.type.DomainType;
import de.quinscape.exceed.model.domain.type.DomainTypeBuilder;
import de.quinscape.exceed.model.meta.PropertyType;
import de.quinscape.exceed.model.view.ComponentModel;
import de.quinscape.exceed.model.view.View;
import de.quinscape.exceed.runtime.ExceedRuntimeException;
import de.quinscape.exceed.runtime.TestApplication;
import de.quinscape.exceed.runtime.TestApplicationBuilder;
import de.quinscape.exceed.runtime.js.ExpressionType;
import de.quinscape.exceed.runtime.js.InvalidExpressionException;
import de.quinscape.exceed.runtime.js.InvalidReferenceException;
import de.quinscape.exceed.runtime.js.JsExpressionRenderer;
import de.quinscape.exceed.runtime.js.ScriptBuffer;
import de.quinscape.exceed.runtime.js.def.DefinitionType;
import de.quinscape.exceed.runtime.js.def.Definitions;
import de.quinscape.exceed.runtime.js.def.FunctionDefinition;
import de.quinscape.exceed.runtime.model.ExpressionModelContext;
import de.quinscape.exceed.runtime.util.ExpressionUtil;
import jdk.nashorn.api.scripting.NashornScriptEngine;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.CompiledScript;
import javax.script.ScriptException;
import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

public class ActionExpressionTest
{
    private final static Logger log = LoggerFactory.getLogger(ActionExpressionTest.class);


    private static View view = new View("testloc");

    private static TestDomainService domainService = new TestDomainService();

    private static TestApplication testApplication = new TestApplicationBuilder()
        .withSessionContext(DomainProperty.builder()
            .withName("test")
            .withType(PropertyType.INTEGER)
            .buildScoped()
        )
        .withSessionContext(DomainProperty.builder()
            .withName("a")
            .withType(PropertyType.PLAIN_TEXT)
            .buildScoped())
        .withSessionContext(DomainProperty.builder()
            .withName("b")
            .withType(PropertyType.PLAIN_TEXT)
            .buildScoped())
        .withSessionContext(DomainProperty.builder()
            .withName("container")
            .withType(PropertyType.DOMAIN_TYPE, "Container")
            .buildScoped())
        .withSessionContext(DomainProperty.builder()
            .withName("curr")
            .withType(PropertyType.DOMAIN_TYPE, "DNameNum")
            .buildScoped())
        .withView(view)
        .withDomainService(domainService)
        .build();


    private static JsExpressionRenderer renderer = createRenderer();

    private ScriptBuffer lastBuffer;


    private static JsExpressionRenderer createRenderer()
    {

        final Definitions definitions = buildActions(
            "foo",
            "bar",
            "bar2",
            "baz",
            "baz2",
            "qux",
            "query",
            "when",
            ActionExpressionTransformer.FORK_ACTION,
            ActionExpressionTransformer.CATCH_ACTION,
            "clientOnly"
        );

        definitions.addDefinition(
            "isBla",
            new FunctionDefinition(
                "isBla",
                ExpressionUtil.PLAINTEXT_TYPE,
                "",
                null,
                Arrays.asList(
                    ExpressionUtil.PLAINTEXT_TYPE
                ),
                null,
                false,
                DefinitionType.BUILTIN,
                "test",
                null
            )
        );

        return new JsExpressionRenderer(


            Arrays.asList(
                new ActionExpressionTransformer(
                    definitions,
                    true
                ),
                new EqualityTransformer(),
                new ScopeExpressionTransformer(
                    testApplication.getApplicationModel().lookup(view).getLocalDefinitions(),
                    ExpressionType.VALUE,
                    null
                )
            )
        );
    }


    private static Definitions buildActions(String... names)
    {
        final Definitions definitions = new Definitions();
        for (String name : names)
        {
            definitions.addDefinition(name, new FunctionDefinition(
                name,
                ExpressionUtil.PLAINTEXT_TYPE,
                "",
                ExpressionType.ACTION,
                name.equals("query") ? Collections.singletonList(ExpressionUtil.EXPRESSION_TYPE) : Arrays.asList(ExpressionUtil.PLAINTEXT_TYPE,ExpressionUtil.PLAINTEXT_TYPE),
                null,
                false,
                name.equals("clientOnly") ? DefinitionType.CLIENT_SIDE_ACTION : DefinitionType.ACTION,
                "test",
                null
            ));
        }

        return definitions;
    }


    @Test
    public void testPromiseChainTransformation() throws Exception
    {

        assertThat(
            transform(("a = isBla('xxx') ; b = 'yyy' ; test = 12")),
            is("_v.updateScope(['a'], isBla('xxx')),_v.updateScope(['b'], 'yyy'),_v.updateScope(['test'], 12)")
        );

        assertThat(
            transform(("foo()")),
            is("_a.action('foo',[])")
        );

        assertThat(
            transform(("a = foo()")),
            is("_a.action('foo',[]).then(function(__a) {_v.updateScope(['a'], __a);})")
        );
        assertThat(
            transform(("foo() ; bar()")),
            is("_a.action('foo',[]).then(function(){return _a.action('bar',[])})")
        );
        assertThat(
            transform(("a.name = foo() ; bar(a)")),
            is("_a.action('foo',[]).then(function(__a) {_v.updateScope(['a','name'], __a);return _a.action('bar',[_v.scope('a')])})")
        );

        assertThat(
            transform(("foo() ; bar() ; baz()")),
            is("_a.action('foo',[]).then(function(){return _a.action('bar',[])}).then(function(){return _a.action('baz',[])})")
        );

        assertThat(
            transform(("foo() ; test = bar()")),
            is("_a.action('foo',[]).then(function(){return _a.action('bar',[])}).then(function(__test) {_v.updateScope(['test'], __test);})")
        );

    }


    @Test
    public void testSimpleAssignment() throws Exception
    {
        assertThat(
            transform(("test = 3 * 4")),
            is("_v.updateScope(['test'], 3 * 4)")
        );
        assertThat(
            transform(("foo();test = test + 1")),
            is("_a.action('foo',[]).then(function(){return _v.updateScope(['test'], _v.scope('test') + 1)})")
        );

    }

    @Test
    public void testNonActionFunction() throws Exception
    {
        assertThat(
            transform(("test = now()")),
            is("_v.updateScope(['test'], now())")
        );

    }


    @Test
    public void testAssignmentFollowedByAction() throws Exception
    {
        assertThat(
            transform(("curr.num = 1 ; foo(curr)")),
            is("_v.updateScope(['curr','num'], 1),_a.action('foo',[_v.scope('curr')])")
        );

    }

    @Test
    public void testForkAssignment() throws Exception
    {
        assertThat(
            transform(("fork( foo(); test = bar())")),
            is("Promise.all(_a.action('foo',[]).then(function(){return _a.action('bar',[])}).then(function(__test) {_v.updateScope(['test'], __test);}))")
        );

        assertThat(
            transform("fork( foo(); test = bar()) ; foo()"),
            is("Promise.all(_a.action('foo',[]).then(function(){return _a.action('bar',[])}).then(function(__test) {_v.updateScope(['test'], __test);})).then(function(){return _a.action('foo',[])})")
        );

    }


    @Test
    public void testAssignmentPath() throws Exception
    {
        assertThat(
            transform(("container.values[0] = 'foo'")),
            is("_v.updateScope(['container','values',0], 'foo')")
        );
        assertThat(
            transform(("container.values[0] = container.values[1] + 'foo'")),
            is("_v.updateScope(['container','values',0], _v.scope(['container','values',1]) + 'foo')")
        );
    }


    @Test
    public void testFork() throws Exception
    {
        assertThat(
            transform(("foo(); fork(bar()); qux()")),
            is("_a.action('foo',[]).then(function(){return Promise.all(_a.action('bar',[]))}).then(function(){return _a.action('qux',[])})")
        );
        assertThat(
            transform(("foo(); fork(bar();bar2(),baz();baz2()); qux()")),
            is("_a.action('foo',[]).then(function(){return Promise.all(_a.action('bar',[]).then(function(){return _a.action('bar2',[])}),_a.action('baz',[]).then(function(){return _a.action('baz2',[])}))}).then(function(){return _a.action('qux',[])})")
        );
    }


    @Test(expected = InvalidReferenceException.class)
    public void testClientSideOnly() throws Exception
    {
        transform("clientOnly()");
    }


    @Test
    public void testCatch() throws Exception
    {
        assertThat(
            transform((" foo() ; catch( baz() ; baz2()) ; qux()")),
            is("_a.action('foo',[]).catch(function(){return _a.action('baz',[]).then(function(){return _a.action('baz2',[])})}).then(function(){return _a.action('qux',[])})")
        );

        assertThat(
            transform(("foo() ; catch( baz()) ; qux()")),
            is("_a.action('foo',[]).catch(function(){return _a.action('baz',[])}).then(function(){return _a.action('qux',[])})")
        );

        // when( strValue == 'special' ) ; value = 1 ; catch( value = 0 )
    }

    @Test
    public void testWhen() throws Exception
    {

        assertThat(
            transform("when( a == 'special').then( value = 1)"),
            is("_v.when(Promise.resolve(_v.scope('a') === 'special'),function(){ return _v.updateScope(['value'], 1)})")
        );
        assertThat(
            transform("when( a == 'special').then( value = 1).else(value = 0)"),
            is("_v.when(Promise.resolve(_v.scope('a') === 'special'),function(){ return _v.updateScope(['value'], 1)},function(){ return _v.updateScope(['value'], 0)})")
        );
        assertThat(
            transform("foo(a,b) ; when(!isNew(a)).then(foo(b))"),
            is("_a.action('foo',[_v.scope('a'),_v.scope('b')]).then(function(){return _v.when(Promise.resolve(!isNew(_v.scope('a'))),function(){ return _a.action('foo',[_v.scope('b')])})})")
        );
        assertThat(
            transform("when(a == 0).then( foo() ).else( bar() ) ; foo()"),
            is("_v.when(Promise.resolve(_v.scope('a') === 0),function(){ return _a.action('foo',[])},function(){ return _a.action('bar',[])}).then(function(){ return _a.action('foo',[])})")
        );

        assertThat(
            transform("when(a == 0).then( foo() ).else( b = 1 ) ; foo()"),
            is("_v.when(Promise.resolve(_v.scope('a') === 0),function(){ return _a.action('foo',[])},function(){ return _v.updateScope(['b'], 1)}).then(function(){ return _a.action('foo',[])})")
        );



    }


    @Test(expected = InvalidExpressionException.class)
    public void testWhenError() throws Exception
    {
        transform("when( a == 1)");
    }

    @Test
    public void testForeignExpression() throws Exception
    {
        assertThat(
            transform("a = 0; b = query( DNameNum.as(d) )"),
            is("_v.updateScope(['a'], 0),_a.action('query',[expr]).then(function(__b) {_v.updateScope(['b'], __b);})")
        );

        assertThat(lastBuffer.getPushed().get("__Expression('query(DNameNum.as(d))')"), is("expr"));

        assertThat(
            transform("a = 0; b = query( DNameNum.as(d) ).filter(d.id == a)"),
            is("_v.updateScope(['a'], 0),_a.action('query',[expr]).then(function(__b) {_v.updateScope(['b'], __b);})")
        );

        log.info("{}", lastBuffer.getPushed());

        assertThat(lastBuffer.getPushed().get("__Expression('query(DNameNum.as(d)).filter(d.id == a)')"), is("expr"));

        assertThat(
            transform("a = 0; b = query( DNameNum.as(d) ).filter(d.id == a) ; a = 1"),
            is("_v.updateScope(['a'], 0),_a.action('query',[expr]).then(function(__b) {_v.updateScope(['b'], __b);return _v.updateScope(['a'], 1)})")
        );

    }


    private String transform(String expr) throws ParseException
    {
        final ASTExpression astExpression = ExpressionParser.parse(expr);

        final ScriptBuffer buffer = new ScriptBuffer();
        final ExpressionModelContext context = new ExpressionModelContext(
            new View(),
            new ComponentModel()
        );

        final String code = renderer.transform(
            null,
            ExpressionType.ACTION,
            context,
            astExpression,
            buffer
        );

        lastBuffer = buffer;

        return assertValidJs(code);
    }


    private String assertValidJs(String code)
    {
        // we make sure that the generated code is valid js by compiling it wrapped in a function
        final NashornScriptEngine nashorn = testApplication.createRuntimeContext().getJsEnvironment().getNashorn();
        try
        {
            final CompiledScript script = nashorn.compile("function test(){ return " + code + ";   }");
            assertThat(script, is(notNullValue()));
        }
        catch (ScriptException e)
        {
            throw new ExceedRuntimeException("Invalid code:\n" + code, e);
        }

        return code;
    }


    public static class TestDomainService
        extends TestDomainServiceBase
    {
        public TestDomainService()
        {
            try
            {
                add(
                    new DomainTypeBuilder("Container")
                        .withProperties(
                            DomainProperty.builder()
                                .withName("name")
                                .withType(PropertyType.PLAIN_TEXT)
                                .build(),
                            DomainProperty.builder()
                                .withName("values")
                                .withType(PropertyType.LIST, PropertyType.PLAIN_TEXT)
                                .build()
                        ).build()
                );

                add(
                    new DomainTypeBuilder("DNameNum")
                        .withProperties(
                            DomainProperty.builder()
                                .withName("name")
                                .withType(PropertyType.PLAIN_TEXT)
                                .build(),
                            DomainProperty.builder()
                                .withName("num")
                                .withType(PropertyType.INTEGER)
                                .build()
                        ).build()
                );
            }
            catch(Exception e)
            {
                log.error("", e);
            }


        }


        private void add(DomainType domainType)
        {
            domainTypes.put(domainType.getName(), domainType);
        }

    }
}
