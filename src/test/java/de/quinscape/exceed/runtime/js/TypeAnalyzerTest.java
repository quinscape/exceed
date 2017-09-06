package de.quinscape.exceed.runtime.js;

import com.google.common.collect.ImmutableMap;
import de.quinscape.exceed.TestDomainServiceBase;
import de.quinscape.exceed.expression.ASTAdd;
import de.quinscape.exceed.expression.ASTArray;
import de.quinscape.exceed.expression.ASTEquality;
import de.quinscape.exceed.expression.ASTExpression;
import de.quinscape.exceed.expression.ASTFunction;
import de.quinscape.exceed.expression.ASTMap;
import de.quinscape.exceed.expression.ASTPropertyChain;
import de.quinscape.exceed.expression.ExpressionParser;
import de.quinscape.exceed.expression.Node;
import de.quinscape.exceed.expression.ParseException;
import de.quinscape.exceed.model.ApplicationModel;
import de.quinscape.exceed.model.context.ContextModel;
import de.quinscape.exceed.model.context.ScopedPropertyModel;
import de.quinscape.exceed.model.domain.property.DomainProperty;
import de.quinscape.exceed.model.domain.property.PropertyModel;
import de.quinscape.exceed.model.domain.type.DomainType;
import de.quinscape.exceed.model.domain.type.DomainTypeModel;
import de.quinscape.exceed.model.domain.type.EnumType;
import de.quinscape.exceed.model.meta.PropertyType;
import de.quinscape.exceed.model.state.StateMachine;
import de.quinscape.exceed.model.view.View;
import de.quinscape.exceed.runtime.TestApplication;
import de.quinscape.exceed.runtime.TestApplicationBuilder;
import de.quinscape.exceed.runtime.domain.property.DecimalConverter;
import de.quinscape.exceed.runtime.js.def.Definition;
import de.quinscape.exceed.runtime.js.def.DefinitionType;
import de.quinscape.exceed.runtime.js.def.Definitions;
import de.quinscape.exceed.runtime.model.ExpressionModelContext;
import de.quinscape.exceed.runtime.util.ExpressionUtil;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

public class TypeAnalyzerTest
{
    private TestApplication app;

    private TypeAnalyzer typeAnalyzer;

    {
        final ContextModel viewContext = new ContextModel();
        Map<String, ScopedPropertyModel> props = new HashMap<>();

        define(props, "intVar", ExpressionUtil.INTEGER_TYPE);
        define(props, "decVar", ExpressionUtil.DECIMAL_TYPE);
        define(props, "domainVar", PropertyType.DOMAIN_TYPE, "Foo");
        define(props, "listVar", PropertyType.LIST, PropertyType.PLAIN_TEXT);

        define(props, "domainVar", PropertyType.DOMAIN_TYPE, "Foo");

        define(props, "enumA", PropertyType.ENUM, "EnumA");
        define(props, "enumB", PropertyType.ENUM, "EnumB");
        define(props, "enumC", PropertyType.ENUM, "EnumC");

        define(props, "stateA", PropertyType.STATE, "MachineA");
        define(props, "stateB", PropertyType.STATE, "MachineB");

        viewContext.setProperties( props );

        final View view = new View();
        view.setName("test/test");
        view.setContextModel(viewContext);


        StateMachine stateMachineA = new StateMachine();
        stateMachineA.setName("MachineA");
        stateMachineA.setStartState("A");
        StateMachine stateMachineB = new StateMachine();
        stateMachineB.setName("MachineB");
        stateMachineB.setStartState("A");

        stateMachineA.setStates(
            ImmutableMap.of(
                "A", Arrays.asList("B","C"),
                "B", Collections.singletonList("C"),
                "C", Collections.singletonList("B")
            )
        );

        stateMachineB.setStates(
            ImmutableMap.of(
                "A", Collections.singletonList("B"),
                "B", Collections.emptyList()
            )
        );

        stateMachineA.validate();
        stateMachineB.validate();

        app = new TestApplicationBuilder()
            .withDomainService(new TestDomainService())
            .withView(view)
            .withDefinitions(Definition.builder()
                .function("integer")
                    .withType(DefinitionType.BUILTIN)
                    .withParameterModels(ExpressionUtil.DECIMAL_TYPE)
                    .withReturnType(PropertyType.INTEGER)
                .build())
            .withStateMachine(stateMachineA)
            .withStateMachine(stateMachineB)
            .build();

        typeAnalyzer = new TypeAnalyzer();

    }


    private void define(Map<String, ScopedPropertyModel> props, String name, String type, String typeParam)
    {
        final ScopedPropertyModel model = new ScopedPropertyModel();
        model.setType(type);
        model.setTypeParam(typeParam);
        props.put(name, model);

    }


    private void define(Map<String, ScopedPropertyModel> props, String name, DomainProperty type)
    {
        final ScopedPropertyModel model = new ScopedPropertyModel();
        model.setType(type.getType());
        model.setTypeParam(type.getTypeParam());
        model.setConfig(type.getConfig());
        props.put(name, model);

    }


    @Test(expected = InvalidTypeException.class)
    public void testTypeConflict() throws Exception
    {
        analyze("1 + {}");

    }


    @Test
    public void testAnalyzing() throws Exception
    {
        ASTAdd astAdd = (ASTAdd) analyze("1 + 2.20");
        final PropertyModel propertyType = astAdd.annotation().getPropertyType();
        assertThat(propertyType.getType(), is(PropertyType.DECIMAL));
        assertThat(propertyType.getConfig().get("decimalPlaces"), is(2));
    }


    @Test
    public void testFunctionAnalysis() throws Exception
    {
        ASTFunction astFn = (ASTFunction) analyze("integer(1 + 2.20)");
        final PropertyModel pt2 = astFn.annotation().getPropertyType();
        assertThat(pt2.getType(), is(PropertyType.INTEGER));

        final PropertyModel pt3 = astFn.jjtGetChild(0).annotation().getPropertyType();
        assertThat(pt3.getType(), is(PropertyType.DECIMAL));

    }


    @Test
    public void testScopeIdentifiers() throws Exception
    {
        final PropertyModel intPropType = analyze("intVar").annotation().getPropertyType();
        assertThat(intPropType, is(notNullValue()));
        assertThat(intPropType.getType(), is(PropertyType.INTEGER));

        final PropertyModel docPropType = analyze("decVar").annotation().getPropertyType();
        assertThat(docPropType, is(notNullValue()));
        assertThat(docPropType.getType(), is(PropertyType.DECIMAL));
    }

    @Test
    public void testScopeIdentifierChains() throws Exception
    {
        final PropertyModel domainPropType = analyze("domainVar").annotation().getPropertyType();
        assertThat(domainPropType, is(notNullValue()));
        assertThat(domainPropType.getType(), is("DomainType"));
        assertThat(domainPropType.getTypeParam(), is("Foo"));

        final ASTPropertyChain astPropChain = (ASTPropertyChain) analyze("domainVar.value");
        final PropertyModel valuePropType = astPropChain.annotation().getPropertyType();
        assertThat(valuePropType, is(notNullValue()));
        assertThat(valuePropType.getType(), is(PropertyType.PLAIN_TEXT));

    }


    @Test
    public void testWidening() throws Exception
    {
        {
            ASTAdd astAdd = (ASTAdd) analyze("1 + 2.1 + 'foo'");
            final PropertyModel propertyType = astAdd.annotation().getPropertyType();

            assertThat(propertyType.getType(), is(PropertyType.PLAIN_TEXT));

        }

        assertThat(resultDecimalPlaces("1 + 1.1"), is(1));
        assertThat(resultDecimalPlaces("1.1 + 1.1111"), is(4));
        assertThat(resultDecimalPlaces("1 + 1.20 + decVar + 2.1"), is(3));
    }


    @Test
    public void testEnumEquality() throws Exception
    {
        final ASTEquality eq = (ASTEquality) analyze("enumA == EnumA.AA");
        assertThat(eq.annotation().getPropertyType().getType(), is(PropertyType.BOOLEAN));
        assertThat(eq.jjtGetChild(0).annotation().getPropertyType().getType(), is(PropertyType.ENUM));
        assertThat(eq.jjtGetChild(1).annotation().getPropertyType().getType(), is(PropertyType.ENUM));
    }

    @Test
    public void testStateMachineEquality() throws Exception
    {
        final ASTEquality eq = (ASTEquality) analyze("stateA == MachineA.START");
        assertThat(eq.annotation().getPropertyType().getType(), is(PropertyType.BOOLEAN));
        assertThat(eq.jjtGetChild(0).annotation().getPropertyType().getType(), is(PropertyType.STATE));
        assertThat(eq.jjtGetChild(1).annotation().getPropertyType().getType(), is(PropertyType.STATE));
    }

//    @Test(expected = InvalidTypeException.class)
//    public void testEnumTypeError() throws Exception
//    {
//        analyze("EnumA.AA + EnumA.AB");
//    }

    @Test(expected = InvalidTypeException.class)
    public void testEnumTypeError2() throws Exception
    {
        analyze("EnumA.AA * EnumA.AB");
    }

    @Test(expected = InvalidTypeException.class)
    public void testEnumTypeError3() throws Exception
    {
        analyze("EnumA.AA < EnumA.AB");
    }

    @Test(expected = InvalidTypeException.class)
    public void testIncompatibleEnumTypes() throws Exception
    {
        analyze("EnumA.AA == EnumB.AB");
    }

    @Test(expected = InvalidTypeException.class)
    public void testIncompatibleStateMachineComparison() throws Exception
    {
        analyze("MachineA.A == 1");
    }

    @Test(expected = InvalidTypeException.class)
    public void testIncompatibleStateMachineComparison2() throws Exception
    {
        analyze("MachineA.A == 'abc'");
    }

//    @Test(expected = InvalidTypeException.class)
//    public void testStateMachineError() throws Exception
//    {
//        analyze("MachineA.START + MachineA.A");
//    }


    @Test(expected = InvalidTypeException.class)
    public void testStateMachineError2() throws Exception
    {
        analyze("MachineA.START * MachineA.A");
    }

    @Test(expected = InvalidTypeException.class)
    public void testStateMachineError3() throws Exception
    {
        analyze("MachineA.START < MachineA.A");
    }

    @Test(expected = InvalidTypeException.class)
    public void testIncompatibleStateMachines() throws Exception
    {
        analyze("MachineA.A == MachineB.A");
    }

    @Test
    public void testIncompatibleEnumTypesWithCast() throws Exception
    {
        analyze("EnumA.AA == cast('EnumA', EnumB.AB)");
    }

    @Test
    public void testIncompatibleEnumComparisonWithCast() throws Exception
    {
        analyze("EnumA.AA == cast('EnumA', 1)");
    }

    @Test(expected = InvalidTypeException.class)
    public void testIncompatibleEnumComparison() throws Exception
    {
        analyze("EnumA.AA == 1");
    }

    @Test(expected = InvalidTypeException.class)
    public void testIncompatibleEnumComparison2() throws Exception
    {
        analyze("EnumA.AA == 'abc'");
    }

    @Test(expected = InvalidTypeException.class)
    public void testIncompatibleEnumTypesWithIncompatibleCast() throws Exception
    {
        analyze("EnumA.AA == cast('EnumC', EnumB.AB)");
    }


    @Test(expected = InvalidExpressionException.class)
    public void testOperatorTypeError() throws Exception
    {
        analyze("1 - 'foo'");
    }

    @Test(expected = InvalidExpressionException.class)
    public void testOperatorTypeError2() throws Exception
    {
        analyze("1 * 'foo'");
    }


    @Test(expected = InvalidReferenceException.class)
    public void testInvalidRef() throws Exception
    {
        analyze("nonExisting");
    }

    @Test(expected = InvalidReferenceException.class)
    public void testInvalidRef2() throws Exception
    {
        analyze("nonExisting()");
    }


    @Test
    public void testCollectionTypes() throws Exception
    {
        {
            ASTPropertyChain chain = (ASTPropertyChain) analyze("listVar.length");
            assertThat(chain.annotation().getPropertyType().getType(), is(PropertyType.INTEGER));

        }

        {
            ASTPropertyChain chain = (ASTPropertyChain) analyze("listVar['length']");
            assertThat(chain.annotation().getPropertyType().getType(), is(PropertyType.INTEGER));

        }

        {
            ASTPropertyChain chain = (ASTPropertyChain) analyze("listVar[0]");
            assertThat(chain.annotation().getPropertyType().getType(), is(PropertyType.PLAIN_TEXT));

        }


        {
            ASTArray array = (ASTArray) analyze("[]");
            assertThat(array.annotation().getPropertyType().getType(), is(PropertyType.LIST));
            assertThat(array.annotation().getPropertyType().getTypeParam(), is(PropertyType.OBJECT));
        }
        {
            ASTMap array = (ASTMap) analyze("{}");
            assertThat(array.annotation().getPropertyType().getType(), is(PropertyType.MAP));
            assertThat(array.annotation().getPropertyType().getTypeParam(), is(PropertyType.OBJECT));
        }
        {
            ASTArray array = (ASTArray) analyze("[0,0]");
            assertThat(array.annotation().getPropertyType().getType(), is(PropertyType.LIST));
            assertThat(array.annotation().getPropertyType().getTypeParam(), is(PropertyType.INTEGER));
        }
        {
            ASTArray array = (ASTArray) analyze("[0,0]");
            assertThat(array.annotation().getPropertyType().getType(), is(PropertyType.LIST));
            assertThat(array.annotation().getPropertyType().getTypeParam(), is(PropertyType.INTEGER));
        }

        {
            ASTArray array = (ASTArray) analyze("['a','b']");
            assertThat(array.annotation().getPropertyType().getType(), is(PropertyType.LIST));
            assertThat(array.annotation().getPropertyType().getTypeParam(), is(PropertyType.PLAIN_TEXT));
        }

        {
            ASTMap array = (ASTMap) analyze("{ a: 0, b: 0 }");
            assertThat(array.annotation().getPropertyType().getType(), is(PropertyType.MAP));
            assertThat(array.annotation().getPropertyType().getTypeParam(), is(PropertyType.INTEGER));
        }

        {
            ASTArray array = (ASTArray) analyze("[newObject('Foo'),newObject('Foo')]");
            assertThat(array.annotation().getPropertyType().getType(), is(PropertyType.LIST));
            assertThat(array.annotation().getPropertyType().getTypeParam(), is("Foo"));
        }

        {
            ASTMap array = (ASTMap) analyze("{ a: newObject('Foo'), b : newObject('Foo')}");
            assertThat(array.annotation().getPropertyType().getType(), is(PropertyType.MAP));
            assertThat(array.annotation().getPropertyType().getTypeParam(), is("Foo"));
        }

        {
            ASTArray array = (ASTArray) analyze("[newObject('Foo'),newObject('Bar')]");
            assertThat(array.annotation().getPropertyType().getType(), is(PropertyType.LIST));
            assertThat(array.annotation().getPropertyType().getTypeParam(), is(PropertyType.DOMAIN_TYPE));
        }

        {
            ASTArray array = (ASTArray) analyze("[0,'a']");
            assertThat(array.annotation().getPropertyType().getType(), is(PropertyType.LIST));
            assertThat(array.annotation().getPropertyType().getTypeParam(), is(PropertyType.OBJECT));
        }

        {
            ASTArray array = (ASTArray) analyze("[newObject('Foo'),'a']");
            assertThat(array.annotation().getPropertyType().getType(), is(PropertyType.LIST));
            assertThat(array.annotation().getPropertyType().getTypeParam(), is(PropertyType.OBJECT));
        }

    }


    private Node analyze(String s) throws ParseException
    {
        final ApplicationModel applicationModel = app.getApplicationModel();
        final Definitions definitions = applicationModel.lookup("test/test").getLocalDefinitions();

        final ASTExpression astExpression = ExpressionParser.parse(s);
        final TypeAnalyzerContext context = new TypeAnalyzerContext(applicationModel,
            ExpressionType.ACTION, definitions, new ExpressionModelContext(applicationModel.getView("test/test")), typeAnalyzer);

        typeAnalyzer.analyze(context, astExpression);
        return astExpression.jjtGetChild(0);
    }

    private int resultDecimalPlaces(String expr) throws ParseException
    {
        ASTAdd astAdd = (ASTAdd) analyze(expr);
        final PropertyModel propertyType = astAdd.annotation().getPropertyType();

        assertThat(propertyType.getType(), is(PropertyType.DECIMAL));

        return DecimalConverter.getDecimalPlaces(app.getApplicationModel().getConfigModel().getDecimalConfig().getDefaultDecimalPlaces(), propertyType.getConfig());
    }

    private class TestDomainService
        extends TestDomainServiceBase
    {
        {
            domainTypes.put("Foo", createDomainType("Foo"));
            domainTypes.put("Bar", createDomainType("Bar"));
            domainTypes.put("Baz", createDomainType("Baz"));

            enumTypes.put("EnumA", createEnumType("EnumA", "AA", "AB", "AC", "AD"));
            enumTypes.put("EnumB", createEnumType("EnumB", "BA", "BB", "BC", "BD"));
            enumTypes.put("EnumC", createEnumType("EnumC", "CA", "CB", "CC"));
        }


        private EnumType createEnumType(String name, String... values)
        {
            final EnumType enumType = new EnumType();
            enumType.setName(name);
            enumType.setValues(Arrays.asList(values));
            return enumType;
        }


        public DomainType createDomainType(String name)
        {
            DomainTypeModel domainType = new DomainTypeModel();
            domainType.setName(name);
            domainType.setAnnotation("Test domain type " + name);

//            DomainProperty enumProp = DomainProperty.builder()
//                .withName("type")
//                .withType("Enum")
//                .withTypeParam("MyEnum")
//                .withDefaultValue("0")
//                .build();

            domainType.setProperties(Arrays.asList(
                DomainProperty.builder()
                    .withName("value")
                    .withType(PropertyType.PLAIN_TEXT)
                    .build(),

                DomainProperty.builder()
                    .withName(name.toLowerCase())
                    .withType(PropertyType.PLAIN_TEXT)
                    .build(),
//                enumProp,
                DomainProperty.builder()
                    .withName("num")
                    .withType(PropertyType.INTEGER)
                    .withDefaultValue("0")
                    .build()
            ));
            return domainType;
        }

        @Override
        public Map<String, DomainType> getDomainTypes()
        {
            return domainTypes;
        }
    }

}
