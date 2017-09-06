package de.quinscape.exceed.runtime.js;

import de.quinscape.exceed.TestDomainServiceBase;
import de.quinscape.exceed.model.ApplicationModel;
import de.quinscape.exceed.model.domain.DomainRule;
import de.quinscape.exceed.model.domain.property.DomainProperty;
import de.quinscape.exceed.model.domain.type.DomainType;
import de.quinscape.exceed.model.meta.PropertyType;
import de.quinscape.exceed.model.process.DecisionState;
import de.quinscape.exceed.model.process.Transition;
import de.quinscape.exceed.model.process.ViewState;
import de.quinscape.exceed.model.view.View;
import de.quinscape.exceed.runtime.ExceedRuntimeException;
import de.quinscape.exceed.runtime.RuntimeContext;
import de.quinscape.exceed.runtime.TestApplication;
import de.quinscape.exceed.runtime.TestApplicationBuilder;
import de.quinscape.exceed.runtime.action.Action;
import de.quinscape.exceed.runtime.action.CustomLogic;
import de.quinscape.exceed.runtime.domain.CommonDomainOperations;
import de.quinscape.exceed.runtime.domain.DomainObject;
import de.quinscape.exceed.runtime.domain.GenericDomainObject;
import de.quinscape.exceed.runtime.js.def.Definitions;
import de.quinscape.exceed.runtime.js.env.Promise;
import de.quinscape.exceed.runtime.js.env.PromiseState;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.math.BigDecimal;
import java.util.Map;
import java.util.function.Consumer;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

public class JsEnvironmentTest
{
    private final static Logger log = LoggerFactory.getLogger(JsEnvironmentTest.class);

    private TestActions testActions = new TestActions();
    
    @Test
    public void testRules() throws Exception
    {

        final DomainRule positiveRule = new DomainRule();
        positiveRule.setName("Positive");
        positiveRule.setRule("value >= 0");
        positiveRule.setTarget(DomainProperty.builder()
            .withType(PropertyType.INTEGER)
            .build());

        final DomainRule belowThresholdRule = new DomainRule();
        belowThresholdRule.setName("BelowThreshold");
        belowThresholdRule.setRule("value < -10");
        belowThresholdRule.setTarget(DomainProperty.builder()
            .withType(PropertyType.INTEGER)
            .build());

        final DomainRule combined = new DomainRule();
        combined.setTarget(DomainProperty.builder()
            .withType(PropertyType.INTEGER)
            .build());
        combined.setName("Combined");
        combined.setRule("Positive(value) || BelowThreshold(value)");


        TestApplication app = new TestApplicationBuilder().
            withDomainRule(positiveRule)
            .withDomainRule(belowThresholdRule)
            .withDomainRule(combined)
            .withDomainService(new TestDomainService())
            .build();

        final ApplicationModel applicationModel = app.getApplicationModel();

//        log.info("DOMAIN RULES: {}", applicationModel.getDomainRules().values());
//
//        final DefaultExpressionCompiler compiler = new DefaultExpressionCompiler(
//            nashorn,
//            new JsExpressionRenderer(
//                Arrays.asList(
//                    new RuleTransformer(applicationModel.getDomainRules())
//                )
//            ),
//            new TypeAnalyzer(
//            )
//        );
//
//
//        final JsEnvironment jsEnvironment = new JsEnvironment(
//            actionService,
//            nashorn,
//            applicationModel,
//            compiler
//        );

        final JsEnvironment jsEnvironment = applicationModel.getMetaData().getJsEnvironment();

        final RuntimeContext runtimeContext = app.createRuntimeContext();
        
        assertThat(jsEnvironment.applyRule(runtimeContext, positiveRule, 0), is(true));
        assertThat(jsEnvironment.applyRule(runtimeContext, positiveRule, 1), is(true));
        assertThat(jsEnvironment.applyRule(runtimeContext, positiveRule, -1), is(false));

        assertThat(jsEnvironment.applyRule(runtimeContext, belowThresholdRule, -11), is(true));

        assertThat(jsEnvironment.applyRule(runtimeContext, combined, -12), is(true));
        assertThat(jsEnvironment.applyRule(runtimeContext, combined, 5), is(true));

    }


    @Test
    public void testDecimal() throws Exception
    {
        TestApplication app = new TestApplicationBuilder()
            .withSessionContext(
                DomainProperty.builder()
                    .withName("intVal")
                    .withType(PropertyType.INTEGER)
                    .withDefaultValue("2")
                    .buildScoped()
            )
            .withSessionContext(
                DomainProperty.builder()
                    .withName("decVal")
                    .withType(PropertyType.DECIMAL)
                    .withDefaultValue("1.234")
                    .buildScoped()
            )
            .withSessionContext(
                DomainProperty.builder()
                    .withName("decVal2")
                    .withType(PropertyType.DECIMAL)
                    .withDefaultValue("23.456")
                    .buildScoped()
            )
            .withSessionContext(
                DomainProperty.builder()
                    .withName("increment")
                    .withType(PropertyType.INTEGER)
                    .withDefaultValue("2")
                    .buildScoped()
            )
            .withExtensions( new File("./src/test/java/de/quinscape/exceed/runtime/js/js-action-test"))
            .withDomainService(new TestDomainService())
            .withTestActionBean(testActions)
            .build();
        final ApplicationModel applicationModel = app.getApplicationModel();

        final DecisionState processState = (DecisionState) applicationModel.getProcess("test").getStates().get("decimalCheck");
        final RuntimeContext runtimeContext = app.createRuntimeContext(processState);

        final JsEnvironment env = runtimeContext.getJsEnvironment();


        {
            // 0: decVal + decVal2 == 24.69
            assertThat(env.getValue(runtimeContext, processState.getDecisions().get(0).getExpressionAST()), is(true));
        }

        {
            // 1: decVal < decVal2
            runtimeContext.getScopedContextChain().setProperty("decVal", new BigDecimal(1));
            runtimeContext.getScopedContextChain().setProperty("decVal2", new BigDecimal(2));
            assertThat(env.getValue(runtimeContext, processState.getDecisions().get(1).getExpressionAST()), is(true));

            runtimeContext.getScopedContextChain().setProperty("decVal", new BigDecimal(2));
            runtimeContext.getScopedContextChain().setProperty("decVal2", new BigDecimal(2));
            assertThat(env.getValue(runtimeContext, processState.getDecisions().get(1).getExpressionAST()), is(false));
        }

        {
            // 2: decVal <= decVal2
            runtimeContext.getScopedContextChain().setProperty("decVal", new BigDecimal(2));
            runtimeContext.getScopedContextChain().setProperty("decVal2", new BigDecimal(2));
            assertThat(env.getValue(runtimeContext, processState.getDecisions().get(2).getExpressionAST()), is(true));

            runtimeContext.getScopedContextChain().setProperty("decVal", new BigDecimal(1));
            runtimeContext.getScopedContextChain().setProperty("decVal2", new BigDecimal(2));
            assertThat(env.getValue(runtimeContext, processState.getDecisions().get(2).getExpressionAST()), is(true));

            runtimeContext.getScopedContextChain().setProperty("decVal", new BigDecimal(3));
            runtimeContext.getScopedContextChain().setProperty("decVal2", new BigDecimal(2));
            assertThat(env.getValue(runtimeContext, processState.getDecisions().get(2).getExpressionAST()), is(false));

        }

        {
            // 3: decVal > decVal2
            runtimeContext.getScopedContextChain().setProperty("decVal", new BigDecimal(3));
            runtimeContext.getScopedContextChain().setProperty("decVal2", new BigDecimal(2));
            assertThat(env.getValue(runtimeContext, processState.getDecisions().get(3).getExpressionAST()), is(true));

            runtimeContext.getScopedContextChain().setProperty("decVal", new BigDecimal(2));
            runtimeContext.getScopedContextChain().setProperty("decVal2", new BigDecimal(2));
            assertThat(env.getValue(runtimeContext, processState.getDecisions().get(3).getExpressionAST()), is(false));
        }

        {
            // 4: decVal >= decVal2
            runtimeContext.getScopedContextChain().setProperty("decVal", new BigDecimal(2));
            runtimeContext.getScopedContextChain().setProperty("decVal2", new BigDecimal(2));
            assertThat(env.getValue(runtimeContext, processState.getDecisions().get(4).getExpressionAST()), is(true));

            runtimeContext.getScopedContextChain().setProperty("decVal", new BigDecimal(3));
            runtimeContext.getScopedContextChain().setProperty("decVal2", new BigDecimal(2));
            assertThat(env.getValue(runtimeContext, processState.getDecisions().get(4).getExpressionAST()), is(true));

            runtimeContext.getScopedContextChain().setProperty("decVal", new BigDecimal(1));
            runtimeContext.getScopedContextChain().setProperty("decVal2", new BigDecimal(2));
            assertThat(env.getValue(runtimeContext, processState.getDecisions().get(4).getExpressionAST()), is(false));

        }

        {
            // 5: decVal == decVal2
            runtimeContext.getScopedContextChain().setProperty("decVal", new BigDecimal(1));
            runtimeContext.getScopedContextChain().setProperty("decVal2", new BigDecimal(1));
            assertThat(env.getValue(runtimeContext, processState.getDecisions().get(5).getExpressionAST()), is(true));

            runtimeContext.getScopedContextChain().setProperty("decVal", new BigDecimal(1));
            runtimeContext.getScopedContextChain().setProperty("decVal2", new BigDecimal(2));
            assertThat(env.getValue(runtimeContext, processState.getDecisions().get(5).getExpressionAST()), is(false));

        }
        {
            // 6: decVal != decVal2
            runtimeContext.getScopedContextChain().setProperty("decVal", new BigDecimal(1));
            runtimeContext.getScopedContextChain().setProperty("decVal2", new BigDecimal(1));
            assertThat(env.getValue(runtimeContext, processState.getDecisions().get(6).getExpressionAST()), is(false));

            runtimeContext.getScopedContextChain().setProperty("decVal", new BigDecimal(1));
            runtimeContext.getScopedContextChain().setProperty("decVal2", new BigDecimal(2));
            assertThat(env.getValue(runtimeContext, processState.getDecisions().get(6).getExpressionAST()), is(true));

        }
    }


    @Test
    public void testEnumExpressions() throws Exception
    {
        TestApplication app = new TestApplicationBuilder()
            .withSessionContext(
                DomainProperty.builder()
                    .withName("intVal")
                    .withType(PropertyType.INTEGER)
                    .withDefaultValue("2")
                    .buildScoped()
            )
            .withSessionContext(
                DomainProperty.builder()
                    .withName("decVal")
                    .withType(PropertyType.DECIMAL)
                    .withDefaultValue("1.234")
                    .buildScoped()
            )
            .withSessionContext(
                DomainProperty.builder()
                    .withName("decVal2")
                    .withType(PropertyType.DECIMAL)
                    .withDefaultValue("23.456")
                    .buildScoped()
            )
            .withSessionContext(
                DomainProperty.builder()
                    .withName("increment")
                    .withType(PropertyType.INTEGER)
                    .withDefaultValue("2")
                    .buildScoped()
            )
            .withExtensions(new File("./src/test/java/de/quinscape/exceed/runtime/js/js-action-test"))
            .withDomainService(new TestDomainService())
            .withTestActionBean(testActions)
            .build();
        final ApplicationModel applicationModel = app.getApplicationModel();

        final DecisionState processState = (DecisionState) applicationModel.getProcess("test").getStates().get(
            "enumCheck");
        final RuntimeContext runtimeContext = app.createRuntimeContext(processState);

        final JsEnvironment env = runtimeContext.getJsEnvironment();


        {
            // 0: enumA == EnumA.AA
            assertThat(env.getValue(runtimeContext, processState.getDecisions().get(0).getExpressionAST()), is(true));
            runtimeContext.getScopedContextChain().setProperty("enumA", 1);
            assertThat(env.getValue(runtimeContext, processState.getDecisions().get(0).getExpressionAST()), is(false));
            // 1: enumB != EnumB.BB
            assertThat(env.getValue(runtimeContext, processState.getDecisions().get(1).getExpressionAST()), is(false));
            runtimeContext.getScopedContextChain().setProperty("enumB", 0);
            assertThat(env.getValue(runtimeContext, processState.getDecisions().get(1).getExpressionAST()), is(true));
            // 2: enumA == cast('EnumA', enumB)
            runtimeContext.getScopedContextChain().setProperty("enumA", 0);
            runtimeContext.getScopedContextChain().setProperty("enumB", 0);
            assertThat(env.getValue(runtimeContext, processState.getDecisions().get(2).getExpressionAST()), is(true));
            runtimeContext.getScopedContextChain().setProperty("enumB", 1);
            assertThat(env.getValue(runtimeContext, processState.getDecisions().get(2).getExpressionAST()), is(false));
        }
    }


    @Test
    public void testStateMachineExpressions() throws Exception
    {
        TestApplication app = new TestApplicationBuilder()
            .withSessionContext(
                DomainProperty.builder()
                    .withName("intVal")
                    .withType(PropertyType.INTEGER)
                    .withDefaultValue("2")
                    .buildScoped()
            )
            .withSessionContext(
                DomainProperty.builder()
                    .withName("decVal")
                    .withType(PropertyType.DECIMAL)
                    .withDefaultValue("1.234")
                    .buildScoped()
            )
            .withSessionContext(
                DomainProperty.builder()
                    .withName("decVal2")
                    .withType(PropertyType.DECIMAL)
                    .withDefaultValue("23.456")
                    .buildScoped()
            )
            .withSessionContext(
                DomainProperty.builder()
                    .withName("increment")
                    .withType(PropertyType.INTEGER)
                    .withDefaultValue("2")
                    .buildScoped()
            )
            .withExtensions(new File("./src/test/java/de/quinscape/exceed/runtime/js/js-action-test"))
            .withDomainService(new TestDomainService())
            .withTestActionBean(testActions)
            .build();
        final ApplicationModel applicationModel = app.getApplicationModel();

        final DecisionState processState = (DecisionState) applicationModel.getProcess("test").getStates().get(
            "stateMachineCheck");
        final RuntimeContext runtimeContext = app.createRuntimeContext(processState);

        final JsEnvironment env = runtimeContext.getJsEnvironment();




        {
            // 0: stateA == MachineA.START
            assertThat(runtimeContext.getScopedContextChain().getProperty("stateA"), is("A"));
            assertThat(env.getValue(runtimeContext, processState.getDecisions().get(0).getExpressionAST()), is(true));
            runtimeContext.getScopedContextChain().setProperty("stateA", "C");
            assertThat(env.getValue(runtimeContext, processState.getDecisions().get(0).getExpressionAST()), is(false));
            // 1: stateB != MachineB.A
            assertThat(env.getValue(runtimeContext, processState.getDecisions().get(1).getExpressionAST()), is(true));
            runtimeContext.getScopedContextChain().setProperty("stateB", "A");
            assertThat(env.getValue(runtimeContext, processState.getDecisions().get(1).getExpressionAST()), is(false));
            // 2: MachineA.isValid( stateA, MachineA.D)
            runtimeContext.getScopedContextChain().setProperty("stateA", "A");
            assertThat(env.getValue(runtimeContext, processState.getDecisions().get(2).getExpressionAST()), is(false));
            runtimeContext.getScopedContextChain().setProperty("stateA", "B");
            assertThat(env.getValue(runtimeContext, processState.getDecisions().get(2).getExpressionAST()), is(true));
        }
    }

    @Test
    public void testActions() throws Exception
    {
        testActionExpression( ctx -> {

            final ApplicationModel applicationModel = ctx.app.getApplicationModel();
            final JsEnvironment jsEnvironment = ctx.jsEnvironment;

            final ViewState processState = (ViewState) applicationModel.getProcess("test").getStates().get("list");
            final View view = applicationModel.getView("test/list");

            final RuntimeContext runtimeContext = ctx.app.createRuntimeContext(view);

            final Map<String, Transition> transitions = processState.getTransitions();
            {
                Promise result = jsEnvironment.execute(runtimeContext, transitions.get("inc").getActionAST());

                assertThat(result, is(notNullValue()));
                assertThat(result.getState(), is(PromiseState.FULFILLED));
                assertThat(result.getResult(), is(nullValue()));
                
                assertThat(runtimeContext.getScopedContextChain().getProperty("value"), is(2));
            }

            {
                jsEnvironment.execute(runtimeContext, transitions.get("nameFoo").getActionAST());

                final DomainObject fooVar = (DomainObject) runtimeContext.getScopedContextChain().getProperty("fooVar");
                assertThat(fooVar.getProperty("name"), is("fooName"));

            }

            {
                runtimeContext.getScopedContextChain().setProperty("routing", applicationModel.getRoutingTable());
                jsEnvironment.execute(runtimeContext, transitions.get("complex").getActionAST());
                assertThat(runtimeContext.getScopedContextChain().getProperty("strValue"), is("Home"));
            }
            
            {
                runtimeContext.getScopedContextChain().setProperty("strValue", "special");
                jsEnvironment.execute(runtimeContext, transitions.get("when2").getActionAST());
                assertThat(runtimeContext.getScopedContextChain().getProperty("value"), is(1));

                runtimeContext.getScopedContextChain().setProperty("strValue", "not special");
                jsEnvironment.execute(runtimeContext, transitions.get("when2").getActionAST());
                assertThat(runtimeContext.getScopedContextChain().getProperty("value"), is(0));
            }

            {
                runtimeContext.getScopedContextChain().setProperty("strValue", "special");
                jsEnvironment.execute(runtimeContext, transitions.get("when3").getActionAST());
                assertThat(runtimeContext.getScopedContextChain().getProperty("value"), is(2));

                runtimeContext.getScopedContextChain().setProperty("strValue", "not special");
                runtimeContext.getScopedContextChain().setProperty("value", 111);
                jsEnvironment.execute(runtimeContext, transitions.get("when3").getActionAST());
                assertThat(runtimeContext.getScopedContextChain().getProperty("value"), is(111));
            }

        });
    }

    @Test
    public void testActions2() throws Exception
    {
        testActionExpression( ctx -> {

            final ApplicationModel applicationModel = ctx.app.getApplicationModel();
            final JsEnvironment jsEnvironment = ctx.jsEnvironment;

            final ViewState processState = (ViewState) applicationModel.getProcess("test").getStates().get("list");
            final View view = applicationModel.getView("test/list");

            final RuntimeContext runtimeContext = ctx.app.createRuntimeContext(view);

            final Map<String, Transition> transitions = processState.getTransitions();
            {
                Promise result = jsEnvironment.execute(runtimeContext, transitions.get("chain").getActionAST());
                assertThat(result, is(notNullValue()));
                assertThat(result.getState(), is(PromiseState.FULFILLED));
                assertThat(result.getResult(), is("bar:foo"));
            }
            {
                Promise result = jsEnvironment.execute(runtimeContext, transitions.get("error").getActionAST());
                assertThat(result, is(notNullValue()));
                assertThat(result.getState(), is(PromiseState.REJECTED));
                assertThat(result.getResult(), instanceOf(ExceedRuntimeException.class));
            }

        });
    }


    public void testActionExpression(Consumer<ActionExpressionTestContext> consumer) throws Exception
    {

        TestApplication app = new TestApplicationBuilder()
            .withExtensions( new File("./src/test/java/de/quinscape/exceed/runtime/js/js-action-test"))
            .withSessionContext(
                DomainProperty.builder()
                    .withName("increment")
                    .withType(PropertyType.INTEGER)
                    .withDefaultValue("2")
                    .buildScoped()
            )
            .withSessionContext(
                DomainProperty.builder()
                    .withName("decVal")
                    .withType(PropertyType.DECIMAL)
                    .withDefaultValue("2")
                    .buildScoped()
            )
            .withSessionContext(
                DomainProperty.builder()
                    .withName("decVal2")
                    .withType(PropertyType.DECIMAL)
                    .withDefaultValue("2")
                    .buildScoped()
            )
            .withTestActionBean(
                testActions
            )
            .build();

        final ApplicationModel applicationModel = app.getApplicationModel();

        final Definitions definitions = applicationModel.lookup("test/list").getLocalDefinitions();

        log.info("DEFS {}", definitions.getDefinitions().values());

        final ActionExpressionTestContext ctx = new ActionExpressionTestContext(app, applicationModel.getMetaData().getJsEnvironment());
        consumer.accept(ctx);
    }
    
    private static class TestDomainService
        extends TestDomainServiceBase
    {

        public TestDomainService()
        {
//            domainTypes = ImmutableMap.of(
//                "Foo", getDomainType("Foo"),
//                "Bar", getDomainType("Bar"),
//                "Qux", getDomainType("Baz")
//            );
        }

        @Override
        public DomainObject create(RuntimeContext runtimeContext, String type, String id)
        {
            return CommonDomainOperations.create(runtimeContext, this, type, id, GenericDomainObject.class);
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

    private class ActionExpressionTestContext
    {

        public final TestApplication app;

        public final JsEnvironment jsEnvironment;


        public ActionExpressionTestContext(TestApplication app, JsEnvironment jsEnvironment)
        {
            this.app = app;
            this.jsEnvironment = jsEnvironment;
        }
    }


    @CustomLogic
    public static class TestActions
    {
        String last = null, nextToLast = null;

        @Action
        public String foo()
        {
            return "foo";
        }

        @Action
        public String bar(String s)
        {
            return "bar:" + s;
        }

        @Action
        public String error()
        {
            throw new ExceedRuntimeException("BOOM!");
        }

        @Action
        public void syslog(String s)
        {
            nextToLast = last;
            last = s;

            log.info(s);
        }
    }

}
